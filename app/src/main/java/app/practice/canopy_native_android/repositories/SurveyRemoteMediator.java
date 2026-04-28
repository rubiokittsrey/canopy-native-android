package app.practice.canopy_native_android.repositories;

import androidx.annotation.NonNull;
import androidx.concurrent.futures.CallbackToFutureAdapter;
import androidx.paging.ExperimentalPagingApi;
import androidx.paging.ListenableFutureRemoteMediator;
import androidx.paging.LoadType;
import androidx.paging.PagingState;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import app.practice.canopy_native_android.api.SurveyApi;
import app.practice.canopy_native_android.database.AppDatabase;
import app.practice.canopy_native_android.database.dao.RemoteKeyDao;
import app.practice.canopy_native_android.database.dao.SurveyDao;
import app.practice.canopy_native_android.database.entities.RemoteKeyEntity;
import app.practice.canopy_native_android.database.entities.SurveyEntity;
import app.practice.canopy_native_android.models.dto.SurveyListResponse;
import app.practice.canopy_native_android.models.dto.SurveyResponse;
import app.practice.canopy_native_android.utils.Constants;
import retrofit2.Response;

/**
 * RemoteMediator for infinite scrolling with offline-first support.
 *
 * How it works:
 * 1. User opens app -> Room data displayed immediately via PagingSource
 * 2. User scrolls to end -> RemoteMediator.load(APPEND) called
 * 3. Fetches next page from API -> Saves to Room
 * 4. PagingSource invalidated -> UI updates with new data
 * 5. Offline? -> Shows cached data, MediatorResult.Error returned but cached data still shown
 *
 * LoadType explained:
 * - REFRESH: Initial load or pull-to-refresh. Fetches page 1, resets pagination state.
 * - PREPEND: Scroll to top needs older data. We return endOfPagination=true (not supported).
 * - APPEND: Scroll to bottom needs newer data. Fetches next page based on RemoteKey.
 */
@ExperimentalPagingApi
public class SurveyRemoteMediator extends ListenableFutureRemoteMediator<Integer, SurveyEntity> {

    private final SurveyApi surveyApi;
    private final AppDatabase database;
    private final SurveyDao surveyDao;
    private final RemoteKeyDao remoteKeyDao;
    private final SurveyDatabaseHelper dbHelper;
    private final Executor executor;

    private static final String REMOTE_KEY_ID = "surveys_all";
    private static final long CACHE_TIMEOUT_MS = 60 * 60 * 1000; // 1 hour

    public SurveyRemoteMediator(SurveyApi surveyApi, AppDatabase database) {
        this.surveyApi = surveyApi;
        this.database = database;
        this.surveyDao = database.surveyDao();
        this.remoteKeyDao = database.remoteKeyDao();
        this.dbHelper = new SurveyDatabaseHelper(database);
        this.executor = Executors.newSingleThreadExecutor();
    }

    @NonNull
    @Override
    public ListenableFuture<InitializeAction> initializeFuture() {
        return CallbackToFutureAdapter.getFuture(completer -> {
            executor.execute(() -> {
                try {
                    RemoteKeyEntity remoteKey = remoteKeyDao.getRemoteKey(REMOTE_KEY_ID);

                    if (remoteKey == null) {
                        completer.set(InitializeAction.LAUNCH_INITIAL_REFRESH);
                    } else {
                        long cacheAge = System.currentTimeMillis() - remoteKey.getCreatedAt();
                        if (cacheAge > CACHE_TIMEOUT_MS) {
                            completer.set(InitializeAction.LAUNCH_INITIAL_REFRESH);
                        } else {
                            completer.set(InitializeAction.SKIP_INITIAL_REFRESH);
                        }
                    }
                } catch (Exception e) {
                    completer.set(InitializeAction.LAUNCH_INITIAL_REFRESH);
                }
            });
            return "SurveyRemoteMediator.initialize";
        });
    }

    @NonNull
    @Override
    public ListenableFuture<MediatorResult> loadFuture(@NonNull LoadType loadType,
                                                        @NonNull PagingState<Integer, SurveyEntity> state) {
        return CallbackToFutureAdapter.getFuture(completer -> {
            executor.execute(() -> {
                try {
                    completer.set(doLoad(loadType));
                } catch (IOException e) {
                    completer.set(new MediatorResult.Error(e));
                } catch (Exception e) {
                    completer.set(new MediatorResult.Error(e));
                }
            });
            return "SurveyRemoteMediator.load(" + loadType + ")";
        });
    }

    private MediatorResult doLoad(LoadType loadType) throws IOException {
        // Step 1: Determine which page to load
        Integer page = getPageForLoadType(loadType);

        if (page == null) {
            return new MediatorResult.Success(true);
        }

        // Step 2: Fetch from API
        Response<SurveyListResponse> response = surveyApi
                .getSurveys(page, Constants.PAGE_SIZE)
                .execute();

        if (!response.isSuccessful()) {
            return new MediatorResult.Error(
                    new IOException("API error: " + response.code() + " " + response.message())
            );
        }

        if (response.body() == null) {
            return new MediatorResult.Error(
                    new IOException("Empty response body")
            );
        }

        SurveyListResponse data = response.body();
        List<SurveyResponse> surveys = data.getResults();
        boolean endOfPaginationReached = !data.hasNext();

        // Step 3: Save to database in a transaction
        // On REFRESH, we reset the remote key but DON'T delete all synced surveys.
        // This prevents stomping data loaded by other mediators (e.g., filtered).
        // Insert uses REPLACE strategy, so existing surveys get updated.
        database.runInTransaction(() -> {
            if (loadType == LoadType.REFRESH) {
                remoteKeyDao.deleteByKey(REMOTE_KEY_ID);
            }

            for (int i = 0; i < surveys.size(); i++) {
                int order = (page - 1) * Constants.PAGE_SIZE + i;
                dbHelper.saveSurveyWithSections(surveys.get(i), order);
            }

            Integer nextPage = endOfPaginationReached ? null : page + 1;
            RemoteKeyEntity key = new RemoteKeyEntity(REMOTE_KEY_ID, nextPage, page);
            remoteKeyDao.insert(key);
        });

        return new MediatorResult.Success(endOfPaginationReached);
    }

    private Integer getPageForLoadType(LoadType loadType) {
        switch (loadType) {
            case REFRESH:
                return 1;

            case PREPEND:
                return null;

            case APPEND:
                RemoteKeyEntity remoteKey = remoteKeyDao.getRemoteKey(REMOTE_KEY_ID);
                if (remoteKey == null) {
                    return 1;
                }
                return remoteKey.getNextPage();

            default:
                return null;
        }
    }
}
