package app.practice.canopy_native_android.repositories;

import androidx.annotation.NonNull;
import androidx.concurrent.futures.CallbackToFutureAdapter;
import androidx.paging.ListenableFutureRemoteMediator;
import androidx.paging.LoadType;
import androidx.paging.PagingState;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import app.practice.canopy_native_android.api.SurveyApi;
import app.practice.canopy_native_android.database.AppDatabase;
import app.practice.canopy_native_android.database.dao.RemoteKeyDao;
import app.practice.canopy_native_android.database.entities.RemoteKeyEntity;
import app.practice.canopy_native_android.database.entities.SurveyEntity;
import app.practice.canopy_native_android.models.dto.SurveyListResponse;
import app.practice.canopy_native_android.models.dto.SurveyResponse;
import app.practice.canopy_native_android.utils.Constants;
import retrofit2.Response;

/**
 * RemoteMediator for filtered surveys (e.g., "My Surveys" for a specific observer).
 * Uses the same shared table as SurveyRemoteMediator but with a separate remote key
 * per observer, so pagination state is tracked independently.
 *
 * On REFRESH, only the remote key is reset (not the survey data), so this mediator
 * and SurveyRemoteMediator don't stomp on each other's cached data.
 */
@androidx.paging.ExperimentalPagingApi
public class SurveyRemoteMediatorFiltered extends ListenableFutureRemoteMediator<Integer, SurveyEntity> {

    private final SurveyApi surveyApi;
    private final AppDatabase database;
    private final RemoteKeyDao remoteKeyDao;
    private final SurveyDatabaseHelper dbHelper;
    private final Executor executor;

    private final String observerId;
    private final String remoteKeyId;

    private static final long CACHE_TIMEOUT_MS = 60 * 60 * 1000; // 1 hour

    public SurveyRemoteMediatorFiltered(SurveyApi surveyApi, AppDatabase database, String observerId) {
        this.surveyApi = surveyApi;
        this.database = database;
        this.observerId = observerId;
        this.remoteKeyId = "surveys_observer_" + observerId;
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
                    RemoteKeyEntity remoteKey = remoteKeyDao.getRemoteKey(remoteKeyId);

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
            return "SurveyRemoteMediatorFiltered.initialize";
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
                } catch (Exception e) {
                    completer.set(new MediatorResult.Error(e));
                }
            });
            return "SurveyRemoteMediatorFiltered.load(" + loadType + ")";
        });
    }

    private MediatorResult doLoad(LoadType loadType) throws IOException {
        Integer page = getPageForLoadType(loadType);

        if (page == null) {
            return new MediatorResult.Success(true);
        }

        Map<String, String> filters = new HashMap<>();
        filters.put("observer", observerId);

        Response<SurveyListResponse> response = surveyApi
                .getSurveysFiltered(page, Constants.PAGE_SIZE, filters)
                .execute();

        if (!response.isSuccessful() || response.body() == null) {
            return new MediatorResult.Error(
                    new IOException("API error: " + response.code())
            );
        }

        SurveyListResponse data = response.body();
        List<SurveyResponse> surveys = data.getResults();
        boolean endOfPaginationReached = !data.hasNext();

        database.runInTransaction(() -> {
            if (loadType == LoadType.REFRESH) {
                remoteKeyDao.deleteByKey(remoteKeyId);
            }

            for (int i = 0; i < surveys.size(); i++) {
                int order = (page - 1) * Constants.PAGE_SIZE + i;
                dbHelper.saveSurveyWithSections(surveys.get(i), order);
            }

            Integer nextPage = endOfPaginationReached ? null : page + 1;
            RemoteKeyEntity key = new RemoteKeyEntity(remoteKeyId, nextPage, page);
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
                RemoteKeyEntity remoteKey = remoteKeyDao.getRemoteKey(remoteKeyId);
                if (remoteKey == null) {
                    return 1;
                }
                return remoteKey.getNextPage();
            default:
                return null;
        }
    }
}
