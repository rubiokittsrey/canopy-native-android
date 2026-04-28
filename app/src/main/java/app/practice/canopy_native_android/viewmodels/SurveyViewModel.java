package app.practice.canopy_native_android.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.Transformations;
import androidx.paging.PagingData;

import java.util.ArrayList;
import java.util.List;

import app.practice.canopy_native_android.database.entities.BiodiversityEntity;
import app.practice.canopy_native_android.database.entities.HazardEntity;
import app.practice.canopy_native_android.database.entities.InfrastructureEntity;
import app.practice.canopy_native_android.database.entities.SoilEntity;
import app.practice.canopy_native_android.database.entities.SurveyEntity;
import app.practice.canopy_native_android.database.entities.TopographicEntity;
import app.practice.canopy_native_android.database.entities.VegetationEntity;
import app.practice.canopy_native_android.database.entities.WaterEntity;
import app.practice.canopy_native_android.repositories.SurveyRepository;
import app.practice.canopy_native_android.repositories.SurveyRepository.SurveyFilter;
import app.practice.canopy_native_android.repositories.SurveyRepository.SyncResult;
import app.practice.canopy_native_android.utils.Resource;

/**
 * ViewModel for survey list and detail screens.
 *
 * Supports:
 * - Infinite scrolling with automatic API fetching (via RemoteMediator)
 * - Local filtering and search (no remote fetch)
 * - CRUD operations with offline-first sync
 */
@androidx.paging.ExperimentalPagingApi
public class SurveyViewModel extends AndroidViewModel {

    private final SurveyRepository surveyRepository;

    // ==================== LIST STATE ====================

    // Current filter (changes trigger new paged data)
    private final MutableLiveData<SurveyFilter> currentFilter = new MutableLiveData<>(new SurveyFilter());

    // View mode: all surveys or my surveys only
    private final MutableLiveData<ViewMode> viewMode = new MutableLiveData<>(ViewMode.ALL);

    // Current user ID (for "My Surveys" mode)
    private String currentUserId;

    // Paged survey list - reacts to filter and view mode changes
    private final LiveData<PagingData<SurveyEntity>> pagedSurveys;

    // ==================== DETAIL STATE ====================

    // Selected survey for detail view
    private final MutableLiveData<String> selectedSurveyId = new MutableLiveData<>();
    private final LiveData<SurveyEntity> selectedSurvey;

    // Section data for selected survey (lazy loaded)
    private final LiveData<TopographicEntity> selectedTopographic;
    private final LiveData<VegetationEntity> selectedVegetation;
    private final LiveData<SoilEntity> selectedSoil;
    private final LiveData<WaterEntity> selectedWater;
    private final LiveData<BiodiversityEntity> selectedBiodiversity;
    private final LiveData<HazardEntity> selectedHazard;
    private final LiveData<InfrastructureEntity> selectedInfrastructure;

    // ==================== OPERATION RESULTS ====================

    private final MutableLiveData<Resource<Boolean>> refreshResult = new MutableLiveData<>();
    private final MutableLiveData<Resource<String>> createResult = new MutableLiveData<>();
    private final MutableLiveData<Resource<Boolean>> updateResult = new MutableLiveData<>();
    private final MutableLiveData<Resource<Boolean>> deleteResult = new MutableLiveData<>();
    private final MutableLiveData<Resource<SyncResult>> syncResult = new MutableLiveData<>();

    // ==================== UI STATE ====================

    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    // Filter options (for dropdown population)
    private final LiveData<List<String>> surveyTypes;
    private final LiveData<List<String>> weatherConditions;

    // Counts for badges/indicators
    private final LiveData<Integer> totalCount;
    private final LiveData<Integer> pendingCount;

    // Track observers for cleanup
    private final List<Runnable> observerCleanups = new ArrayList<>();

    public enum ViewMode {
        ALL,        // Show all surveys with infinite scroll
        MINE,       // Show only current user's surveys
        FILTERED    // Show filtered results (local only)
    }

    public SurveyViewModel(@NonNull Application application) {
        super(application);
        surveyRepository = new SurveyRepository(application);

        // Setup paged surveys based on view mode and filter
        pagedSurveys = Transformations.switchMap(viewMode, mode -> {
            SurveyFilter filter = currentFilter.getValue();

            switch (mode) {
                case ALL:
                    if (filter != null && !filter.isEmpty()) {
                        // Filtered mode - local only
                        return surveyRepository.getSurveysFilteredPaged(filter);
                    }
                    // Infinite scroll mode
                    return surveyRepository.getSurveysPaged();

                case MINE:
                    if (currentUserId != null) {
                        return surveyRepository.getMySurveysPaged(currentUserId);
                    }
                    // Fallback to all if no user ID
                    return surveyRepository.getSurveysPaged();

                case FILTERED:
                    if (filter != null) {
                        return surveyRepository.getSurveysFilteredPaged(filter);
                    }
                    return surveyRepository.getSurveysPaged();

                default:
                    return surveyRepository.getSurveysPaged();
            }
        });

        // Setup selected survey observation
        selectedSurvey = Transformations.switchMap(selectedSurveyId, id -> {
            if (id == null || id.isEmpty()) {
                return new MutableLiveData<>(null);
            }
            return surveyRepository.getSurveyById(id);
        });

        // Setup section data observation (lazy - only loads when survey selected)
        selectedTopographic = Transformations.switchMap(selectedSurveyId, id -> {
            if (id == null) return new MutableLiveData<>(null);
            return surveyRepository.getTopographicData(id);
        });

        selectedVegetation = Transformations.switchMap(selectedSurveyId, id -> {
            if (id == null) return new MutableLiveData<>(null);
            return surveyRepository.getVegetationData(id);
        });

        selectedSoil = Transformations.switchMap(selectedSurveyId, id -> {
            if (id == null) return new MutableLiveData<>(null);
            return surveyRepository.getSoilData(id);
        });

        selectedWater = Transformations.switchMap(selectedSurveyId, id -> {
            if (id == null) return new MutableLiveData<>(null);
            return surveyRepository.getWaterData(id);
        });

        selectedBiodiversity = Transformations.switchMap(selectedSurveyId, id -> {
            if (id == null) return new MutableLiveData<>(null);
            return surveyRepository.getBiodiversityData(id);
        });

        selectedHazard = Transformations.switchMap(selectedSurveyId, id -> {
            if (id == null) return new MutableLiveData<>(null);
            return surveyRepository.getHazardData(id);
        });

        selectedInfrastructure = Transformations.switchMap(selectedSurveyId, id -> {
            if (id == null) return new MutableLiveData<>(null);
            return surveyRepository.getInfrastructureData(id);
        });

        // Load filter options
        surveyTypes = surveyRepository.getDistinctSurveyTypes();
        weatherConditions = surveyRepository.getDistinctWeatherConditions();

        // Counts
        totalCount = surveyRepository.getTotalCount();
        pendingCount = surveyRepository.getPendingCount();
    }

    // ==================== VIEW MODE ====================

    /**
     * Set current user ID for "My Surveys" mode.
     */
    public void setCurrentUserId(String userId) {
        this.currentUserId = userId;
    }

    /**
     * Switch to show all surveys (with infinite scroll).
     */
    public void showAllSurveys() {
        currentFilter.setValue(new SurveyFilter());
        viewMode.setValue(ViewMode.ALL);
    }

    /**
     * Switch to show only current user's surveys.
     */
    public void showMySurveys() {
        if (currentUserId != null) {
            viewMode.setValue(ViewMode.MINE);
        }
    }

    // ==================== FILTERING ====================

    /**
     * Apply filter to survey list.
     * Switches to FILTERED mode (local only).
     */
    public void applyFilter(SurveyFilter filter) {
        currentFilter.setValue(filter);
        if (filter != null && !filter.isEmpty()) {
            viewMode.setValue(ViewMode.FILTERED);
        } else {
            viewMode.setValue(ViewMode.ALL);
        }
    }

    /**
     * Update search query with debounce (UI should handle debounce timing).
     */
    public void setSearchQuery(String query) {
        SurveyFilter filter = currentFilter.getValue();
        if (filter == null) {
            filter = new SurveyFilter();
        }
        filter.setSearchQuery(query);
        applyFilter(filter);
    }

    /**
     * Clear all filters and return to infinite scroll mode.
     */
    public void clearFilters() {
        currentFilter.setValue(new SurveyFilter());
        viewMode.setValue(ViewMode.ALL);
    }

    // ==================== REFRESH ====================

    /**
     * Force refresh from API.
     * Clears cache timestamp to trigger RemoteMediator refresh on next observation.
     */
    public void forceRefresh() {
        isLoading.setValue(true);
        refreshResult.setValue(Resource.loading());

        observeUntilComplete(surveyRepository.forceRefresh(), resource -> {
            refreshResult.setValue(resource);
            if (resource.getStatus() != Resource.Status.LOADING) {
                isLoading.setValue(false);
            }
        });
    }

    // ==================== DETAIL OPERATIONS ====================

    /**
     * Select a survey for detail view.
     */
    public void selectSurvey(String surveyId) {
        selectedSurveyId.setValue(surveyId);
    }

    /**
     * Clear selected survey.
     */
    public void clearSelection() {
        selectedSurveyId.setValue(null);
    }

    /**
     * Fetch latest data for selected survey from API.
     */
    public void refreshSelectedSurvey() {
        String surveyId = selectedSurveyId.getValue();
        if (surveyId != null) {
            isLoading.setValue(true);
            observeUntilComplete(surveyRepository.fetchSurvey(surveyId), resource -> {
                if (resource.getStatus() != Resource.Status.LOADING) {
                    isLoading.setValue(false);
                }
            });
        }
    }

    // ==================== CRUD OPERATIONS ====================

    /**
     * Create a new survey (saves locally with pending sync status).
     */
    public void createSurvey(SurveyEntity survey,
                             TopographicEntity topographic,
                             VegetationEntity vegetation,
                             SoilEntity soil,
                             WaterEntity water,
                             BiodiversityEntity biodiversity,
                             HazardEntity hazard,
                             InfrastructureEntity infrastructure) {
        isLoading.setValue(true);
        createResult.setValue(Resource.loading());

        observeUntilComplete(
                surveyRepository.createSurvey(survey, topographic, vegetation, soil, water,
                        biodiversity, hazard, infrastructure),
                resource -> {
                    createResult.setValue(resource);
                    if (resource.getStatus() != Resource.Status.LOADING) {
                        isLoading.setValue(false);
                    }
                });
    }

    /**
     * Update an existing survey (saves locally with pending sync status).
     */
    public void updateSurvey(SurveyEntity survey,
                             TopographicEntity topographic,
                             VegetationEntity vegetation,
                             SoilEntity soil,
                             WaterEntity water,
                             BiodiversityEntity biodiversity,
                             HazardEntity hazard,
                             InfrastructureEntity infrastructure) {
        isLoading.setValue(true);
        updateResult.setValue(Resource.loading());

        observeUntilComplete(
                surveyRepository.updateSurvey(survey, topographic, vegetation, soil, water,
                        biodiversity, hazard, infrastructure),
                resource -> {
                    updateResult.setValue(resource);
                    if (resource.getStatus() != Resource.Status.LOADING) {
                        isLoading.setValue(false);
                    }
                });
    }

    /**
     * Delete a survey (deletes locally, then syncs to server).
     */
    public void deleteSurvey(String surveyId) {
        isLoading.setValue(true);
        deleteResult.setValue(Resource.loading());

        observeUntilComplete(surveyRepository.deleteSurvey(surveyId), resource -> {
            deleteResult.setValue(resource);
            if (resource.getStatus() != Resource.Status.LOADING) {
                isLoading.setValue(false);
            }
        });
    }

    // ==================== SYNC ====================

    /**
     * Sync all pending surveys to server.
     */
    public void syncPendingSurveys() {
        isLoading.setValue(true);
        syncResult.setValue(Resource.loading());

        observeUntilComplete(surveyRepository.syncPendingSurveys(), resource -> {
            syncResult.setValue(resource);
            if (resource.getStatus() != Resource.Status.LOADING) {
                isLoading.setValue(false);
            }
        });
    }

    // ==================== CLEAR CACHE (DEBUG) ====================

    private final MutableLiveData<Resource<Boolean>> clearCacheResult = new MutableLiveData<>();

    /**
     * Clear all cached data from the database.
     * Used for debugging purposes.
     */
    public void clearCache() {
        isLoading.setValue(true);
        clearCacheResult.setValue(Resource.loading());

        observeUntilComplete(surveyRepository.clearCache(), resource -> {
            clearCacheResult.setValue(resource);
            if (resource.getStatus() != Resource.Status.LOADING) {
                isLoading.setValue(false);
            }
        });
    }

    public LiveData<Resource<Boolean>> getClearCacheResult() {
        return clearCacheResult;
    }

    // ==================== GETTERS ====================

    public LiveData<PagingData<SurveyEntity>> getPagedSurveys() {
        return pagedSurveys;
    }

    public LiveData<SurveyFilter> getCurrentFilter() {
        return currentFilter;
    }

    public LiveData<ViewMode> getViewMode() {
        return viewMode;
    }

    public LiveData<SurveyEntity> getSelectedSurvey() {
        return selectedSurvey;
    }

    public LiveData<TopographicEntity> getSelectedTopographic() {
        return selectedTopographic;
    }

    public LiveData<VegetationEntity> getSelectedVegetation() {
        return selectedVegetation;
    }

    public LiveData<SoilEntity> getSelectedSoil() {
        return selectedSoil;
    }

    public LiveData<WaterEntity> getSelectedWater() {
        return selectedWater;
    }

    public LiveData<BiodiversityEntity> getSelectedBiodiversity() {
        return selectedBiodiversity;
    }

    public LiveData<HazardEntity> getSelectedHazard() {
        return selectedHazard;
    }

    public LiveData<InfrastructureEntity> getSelectedInfrastructure() {
        return selectedInfrastructure;
    }

    public LiveData<Resource<Boolean>> getRefreshResult() {
        return refreshResult;
    }

    public LiveData<Resource<String>> getCreateResult() {
        return createResult;
    }

    public LiveData<Resource<Boolean>> getUpdateResult() {
        return updateResult;
    }

    public LiveData<Resource<Boolean>> getDeleteResult() {
        return deleteResult;
    }

    public LiveData<Resource<SyncResult>> getSyncResult() {
        return syncResult;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<List<String>> getSurveyTypes() {
        return surveyTypes;
    }

    public LiveData<List<String>> getWeatherConditions() {
        return weatherConditions;
    }

    public LiveData<Integer> getTotalCount() {
        return totalCount;
    }

    public LiveData<Integer> getPendingCount() {
        return pendingCount;
    }

    // ==================== HELPERS ====================

    public void clearResults() {
        createResult.setValue(null);
        updateResult.setValue(null);
        deleteResult.setValue(null);
        syncResult.setValue(null);
        refreshResult.setValue(null);
    }

    /**
     * Observe a LiveData source, forwarding values to the callback,
     * and auto-removing the observer after a terminal (non-LOADING) state.
     */
    @SuppressWarnings("unchecked")
    private <T> void observeUntilComplete(LiveData<Resource<T>> source,
                                           Observer<Resource<T>> callback) {
        Observer<Resource<T>>[] holder = new Observer[1];
        holder[0] = resource -> {
            callback.onChanged(resource);
            if (resource.getStatus() != Resource.Status.LOADING) {
                source.removeObserver(holder[0]);
            }
        };
        source.observeForever(holder[0]);
        observerCleanups.add(() -> source.removeObserver(holder[0]));
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        for (Runnable cleanup : observerCleanups) {
            cleanup.run();
        }
        observerCleanups.clear();
    }
}
