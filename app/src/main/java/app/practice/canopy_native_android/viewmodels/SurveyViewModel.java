package app.practice.canopy_native_android.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.paging.PagingData;

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

public class SurveyViewModel extends AndroidViewModel {

    private final SurveyRepository surveyRepository;

    // filter state
    private final MutableLiveData<SurveyFilter> currentFilter = new MutableLiveData<>(new SurveyFilter());

    // paged survey list (reacts to filter changes)
    private final LiveData<PagingData<SurveyEntity>> pagedSurveys;

    // selected survey for detail view
    private final MutableLiveData<String> selectedSurveyId = new MutableLiveData<>();
    private final LiveData<SurveyEntity> selectedSurvey;

    // section data for selected survey
    private final LiveData<TopographicEntity> selectedTopographic;
    private final LiveData<VegetationEntity> selectedVegetation;
    private final LiveData<SoilEntity> selectedSoil;
    private final LiveData<WaterEntity> selectedWater;
    private final LiveData<BiodiversityEntity> selectedBiodiversity;
    private final LiveData<HazardEntity> selectedHazard;
    private final LiveData<InfrastructureEntity> selectedInfrastructure;

    // operation results
    private final MutableLiveData<Resource<Integer>> refreshResult = new MutableLiveData<>();
    private final MutableLiveData<Resource<String>> createResult = new MutableLiveData<>();
    private final MutableLiveData<Resource<Boolean>> updateResult = new MutableLiveData<>();
    private final MutableLiveData<Resource<Boolean>> deleteResult = new MutableLiveData<>();
    private final MutableLiveData<Resource<SyncResult>> syncResult = new MutableLiveData<>();

    // ui state
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    // filter options (for dropdowns)
    private final LiveData<List<String>> surveyTypes;
    private final LiveData<List<String>> weatherConditions;

    // counts
    private final LiveData<Integer> totalCount;
    private final LiveData<Integer> pendingCount;

    public SurveyViewModel(@NonNull Application application) {
        super(application);
        surveyRepository = new SurveyRepository(application);

        // paged surveys that react to filter changes
        pagedSurveys = Transformations.switchMap(currentFilter, filter -> {
            if (filter == null || filter.isEmpty()) {
                return surveyRepository.getSurveysPaged();
            } else if (filter.getSearchQuery() != null && !filter.getSearchQuery().isEmpty()
                    && filter.getSurveyType() == null && filter.getWeatherCondition() == null
                    && filter.getDateFrom() == null && filter.getDateTo() == null) {
                return surveyRepository.searchSurveysPaged(filter.getSearchQuery());
            } else {
                return surveyRepository.getSurveysFilteredPaged(filter);
            }
        });

        selectedSurvey = Transformations.switchMap(selectedSurveyId, id -> {
            if (id == null || id.isEmpty()) {
                return new MutableLiveData<>(null);
            }
            return surveyRepository.getSurveyById(id);
        });

        // setup section data observation
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

        // load filter options
        surveyTypes = surveyRepository.getDistinctSurveyTypes();
        weatherConditions = surveyRepository.getDistinctWeatherConditions();

        // counts
        totalCount = surveyRepository.getTotalCount();
        pendingCount = surveyRepository.getPendingCount();
    }

    // -- list operations

    /// Refresh surveys from API.
    public void refreshSurveys() {
        isLoading.setValue(true);
        refreshResult.setValue(Resource.loading());

        surveyRepository.refreshSurveys().observeForever(resource -> {
            refreshResult.setValue(resource);
            if (resource.getStatus() != Resource.Status.LOADING) {
                isLoading.setValue(false);
            }
        });
    }

    /// Apply filter to survey list
    public void setFilter(SurveyFilter filter) {
        currentFilter.setValue(filter);
    }

    /// Update search query (debounce should be handled in UI)
    public void setSearchQuery(String query) {
        SurveyFilter filter = currentFilter.getValue();
        if (filter == null) {
            filter = new SurveyFilter();
        }
        filter.setSearchQuery(query);
        currentFilter.setValue(filter);
    }

    /// Clear all filters
    public void clearFilters() {
        currentFilter.setValue(new SurveyFilter());
    }

    // -- detail operations

    /// Select a survey for detail view
    public void selectSurvey(String surveyId) {
        selectedSurveyId.setValue(surveyId);
    }

    /// Fetch latest data for selected survey from API
    public void refreshSelectedSurvey() {
        String surveyId = selectedSurveyId.getValue();
        if (surveyId != null) {
            isLoading.setValue(true);
            surveyRepository.fetchSurvey(surveyId).observeForever(resource -> {
                if (resource.getStatus() != Resource.Status.LOADING) {
                    isLoading.setValue(false);
                }
            });
        }
    }

    // -- crud ops

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

        surveyRepository.createSurvey(survey, topographic, vegetation, soil, water,
                biodiversity, hazard, infrastructure).observeForever(resource -> {
            createResult.setValue(resource);
            if (resource.getStatus() != Resource.Status.LOADING) {
                isLoading.setValue(false);
            }
        });
    }

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

        surveyRepository.updateSurvey(survey, topographic, vegetation, soil, water,
                biodiversity, hazard, infrastructure).observeForever(resource -> {
            updateResult.setValue(resource);
            if (resource.getStatus() != Resource.Status.LOADING) {
                isLoading.setValue(false);
            }
        });
    }

    public void deleteSurvey(String surveyId) {
        isLoading.setValue(true);
        deleteResult.setValue(Resource.loading());

        surveyRepository.deleteSurvey(surveyId).observeForever(resource -> {
            deleteResult.setValue(resource);
            if (resource.getStatus() != Resource.Status.LOADING) {
                isLoading.setValue(false);
            }
        });
    }

    // -- sync

    public void syncPendingSurveys() {
        isLoading.setValue(true);
        syncResult.setValue(Resource.loading());

        surveyRepository.syncPendingSurveys().observeForever(resource -> {
            syncResult.setValue(resource);
            if (resource.getStatus() != Resource.Status.LOADING) {
                isLoading.setValue(false);
            }
        });
    }

    // -- getters

    public LiveData<PagingData<SurveyEntity>> getPagedSurveys() {
        return pagedSurveys;
    }

    public LiveData<SurveyFilter> getCurrentFilter() {
        return currentFilter;
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

    public LiveData<Resource<Integer>> getRefreshResult() {
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

    // -- helpers
    public void clearResults() {
        createResult.setValue(null);
        updateResult.setValue(null);
        deleteResult.setValue(null);
        syncResult.setValue(null);
    }

}
