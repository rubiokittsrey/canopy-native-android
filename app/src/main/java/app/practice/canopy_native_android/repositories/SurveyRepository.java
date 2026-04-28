package app.practice.canopy_native_android.repositories;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.ExperimentalPagingApi;
import androidx.paging.Pager;
import androidx.paging.PagingConfig;
import androidx.paging.PagingData;
import androidx.paging.PagingLiveData;
import androidx.sqlite.db.SimpleSQLiteQuery;
import androidx.sqlite.db.SupportSQLiteQuery;

import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import app.practice.canopy_native_android.api.ApiClient;
import app.practice.canopy_native_android.api.SurveyApi;
import app.practice.canopy_native_android.database.AppDatabase;
import app.practice.canopy_native_android.database.dao.BiodiversityDao;
import app.practice.canopy_native_android.database.dao.HazardDao;
import app.practice.canopy_native_android.database.dao.InfrastructureDao;
import app.practice.canopy_native_android.database.dao.RemoteKeyDao;
import app.practice.canopy_native_android.database.dao.SoilDao;
import app.practice.canopy_native_android.database.dao.SurveyDao;
import app.practice.canopy_native_android.database.dao.TopographicDao;
import app.practice.canopy_native_android.database.dao.VegetationDao;
import app.practice.canopy_native_android.database.dao.WaterDao;
import app.practice.canopy_native_android.database.entities.BiodiversityEntity;
import app.practice.canopy_native_android.database.entities.HazardEntity;
import app.practice.canopy_native_android.database.entities.InfrastructureEntity;
import app.practice.canopy_native_android.database.entities.SoilEntity;
import app.practice.canopy_native_android.database.entities.SurveyEntity;
import app.practice.canopy_native_android.database.entities.TopographicEntity;
import app.practice.canopy_native_android.database.entities.VegetationEntity;
import app.practice.canopy_native_android.database.entities.WaterEntity;
import app.practice.canopy_native_android.models.dto.ErrorResponse;
import app.practice.canopy_native_android.models.dto.SurveyResponse;
import app.practice.canopy_native_android.utils.Constants;
import app.practice.canopy_native_android.utils.Resource;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Repository for survey operations.
 * Handles CRUD operations with offline-first sync support.
 *
 * Key features:
 * - Infinite scrolling via RemoteMediator (fetches pages as user scrolls)
 * - Offline-first (shows cached data immediately, syncs when online)
 * - Local-first creates (saves locally with pending status, syncs later)
 */
public class SurveyRepository {

    private final Context context;
    private final AppDatabase database;
    private final SurveyApi surveyApi;

    private final SurveyDao surveyDao;
    private final TopographicDao topographicDao;
    private final VegetationDao vegetationDao;
    private final SoilDao soilDao;
    private final WaterDao waterDao;
    private final BiodiversityDao biodiversityDao;
    private final HazardDao hazardDao;
    private final InfrastructureDao infrastructureDao;
    private final RemoteKeyDao remoteKeyDao;

    private final SurveyDatabaseHelper dbHelper;

    private final ExecutorService executor;
    private final Handler mainHandler;
    private final Gson gson;

    public SurveyRepository(Context context) {
        this.context = context.getApplicationContext();
        this.database = AppDatabase.getInstance(context);
        this.surveyApi = ApiClient.getInstance(context).getSurveyApi();

        this.surveyDao = database.surveyDao();
        this.topographicDao = database.topographicDao();
        this.vegetationDao = database.vegetationDao();
        this.soilDao = database.soilDao();
        this.waterDao = database.waterDao();
        this.biodiversityDao = database.biodiversityDao();
        this.hazardDao = database.hazardDao();
        this.infrastructureDao = database.infrastructureDao();
        this.remoteKeyDao = database.remoteKeyDao();

        this.dbHelper = new SurveyDatabaseHelper(database);

        this.executor = Executors.newFixedThreadPool(4);
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.gson = new Gson();
    }

    // ==================== PAGED QUERIES WITH INFINITE SCROLL ====================

    /**
     * Get all surveys with infinite scrolling support.
     *
     * How it works:
     * 1. Initial load: Shows cached Room data immediately
     * 2. RemoteMediator checks if refresh needed (cache age > 1 hour)
     * 3. If refresh needed: Fetches page 1 from API, clears old synced data, saves new data
     * 4. User scrolls to end: RemoteMediator fetches next page
     * 5. Offline: Shows cached data, no network errors shown to user
     *
     * @return LiveData of PagingData that automatically loads more as user scrolls
     */
    @ExperimentalPagingApi
    public LiveData<PagingData<SurveyEntity>> getSurveysPaged() {
        return PagingLiveData.getLiveData(
                new Pager<>(
                        new PagingConfig(
                                /* pageSize = */ Constants.PAGE_SIZE,
                                /* prefetchDistance = */ Constants.PAGE_SIZE / 2,
                                /* enablePlaceholders = */ false,
                                /* initialLoadSize = */ Constants.PAGE_SIZE * 2,
                                /* maxSize = */ Constants.PAGE_SIZE * 10
                        ),
                        /* initialKey = */ null,
                        /* remoteMediator = */ new SurveyRemoteMediator(surveyApi, database),
                        /* pagingSourceFactory = */ () -> surveyDao.getSurveysPaged()
                )
        );
    }

    /**
     * Get surveys for a specific observer with infinite scrolling.
     * Uses a filtered RemoteMediator.
     */
    @ExperimentalPagingApi
    public LiveData<PagingData<SurveyEntity>> getMySurveysPaged(String observerId) {
        return PagingLiveData.getLiveData(
                new Pager<>(
                        new PagingConfig(
                                Constants.PAGE_SIZE,
                                Constants.PAGE_SIZE / 2,
                                false,
                                Constants.PAGE_SIZE * 2
                        ),
                        null,
                        new SurveyRemoteMediatorFiltered(surveyApi, database, observerId),
                        () -> surveyDao.getSurveysByObserverPaged(observerId)
                )
        );
    }

    /**
     * Get filtered surveys (local only, no RemoteMediator).
     *
     * For filtered views, we only paginate cached local data.
     * User should use pull-to-refresh to fetch fresh data from server.
     *
     * Use cases:
     * - Search by text
     * - Filter by survey type, weather, date range
     * - Combined filters
     */
    public LiveData<PagingData<SurveyEntity>> getSurveysFilteredPaged(SurveyFilter filter) {
        return PagingLiveData.getLiveData(
                new Pager<>(
                        new PagingConfig(
                                Constants.PAGE_SIZE,
                                Constants.PAGE_SIZE / 2,
                                false,
                                Constants.PAGE_SIZE * 2
                        ),
                        () -> surveyDao.getSurveysFilteredPaged(buildFilterQuery(filter))
                )
        );
    }

    /**
     * Search surveys with paging (local only).
     */
    public LiveData<PagingData<SurveyEntity>> searchSurveysPaged(String query) {
        return PagingLiveData.getLiveData(
                new Pager<>(
                        new PagingConfig(
                                Constants.PAGE_SIZE,
                                Constants.PAGE_SIZE / 2,
                                false,
                                Constants.PAGE_SIZE * 2
                        ),
                        () -> surveyDao.searchSurveysPaged(query)
                )
        );
    }

    /**
     * Build dynamic SQL query for filtering.
     */
    private SupportSQLiteQuery buildFilterQuery(SurveyFilter filter) {
        StringBuilder queryBuilder = new StringBuilder("SELECT * FROM survey_entries WHERE 1=1");
        List<Object> args = new ArrayList<>();

        if (filter.getSurveyType() != null && !filter.getSurveyType().isEmpty()) {
            queryBuilder.append(" AND survey_type = ?");
            args.add(filter.getSurveyType());
        }

        if (filter.getWeatherCondition() != null && !filter.getWeatherCondition().isEmpty()) {
            queryBuilder.append(" AND weather_condition = ?");
            args.add(filter.getWeatherCondition());
        }

        if (filter.getObserverId() != null && !filter.getObserverId().isEmpty()) {
            queryBuilder.append(" AND observer_id = ?");
            args.add(filter.getObserverId());
        }

        if (filter.getDateFrom() != null && !filter.getDateFrom().isEmpty()) {
            queryBuilder.append(" AND survey_date >= ?");
            args.add(filter.getDateFrom());
        }

        if (filter.getDateTo() != null && !filter.getDateTo().isEmpty()) {
            queryBuilder.append(" AND survey_date <= ?");
            args.add(filter.getDateTo());
        }

        if (filter.getSearchQuery() != null && !filter.getSearchQuery().isEmpty()) {
            queryBuilder.append(" AND (observer_name LIKE ? OR organization LIKE ? OR survey_type LIKE ? OR notes LIKE ?)");
            String searchPattern = "%" + filter.getSearchQuery() + "%";
            args.add(searchPattern);
            args.add(searchPattern);
            args.add(searchPattern);
            args.add(searchPattern);
        }

        queryBuilder.append(" ORDER BY remote_order ASC");

        return new SimpleSQLiteQuery(queryBuilder.toString(), args.toArray());
    }

    // ==================== MANUAL REFRESH ====================

    /**
     * Force refresh from API (clears remote keys to trigger full refresh).
     * Called on pull-to-refresh.
     */
    public LiveData<Resource<Boolean>> forceRefresh() {
        MutableLiveData<Resource<Boolean>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        executor.execute(() -> {
            try {
                // Clear remote keys to force RemoteMediator to refresh
                remoteKeyDao.deleteAll();
                mainHandler.post(() -> result.setValue(Resource.success(true)));
            } catch (Exception e) {
                mainHandler.post(() -> result.setValue(Resource.error(e.getMessage())));
            }
        });

        return result;
    }

    // ==================== CLEAR CACHE (DEBUG) ====================

    /**
     * Clear all cached data from the database (surveys, sections, remote keys).
     * Used for debugging purposes.
     */
    public LiveData<Resource<Boolean>> clearCache() {
        MutableLiveData<Resource<Boolean>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        executor.execute(() -> {
            try {
                database.clearAllTables();
                mainHandler.post(() -> result.setValue(Resource.success(true)));
            } catch (Exception e) {
                mainHandler.post(() -> result.setValue(Resource.error(e.getMessage())));
            }
        });

        return result;
    }

    // ==================== SINGLE SURVEY FETCH ====================

    /**
     * Fetch a single survey by ID from API.
     */
    public LiveData<Resource<SurveyEntity>> fetchSurvey(String surveyId) {
        MutableLiveData<Resource<SurveyEntity>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        surveyApi.getSurvey(surveyId).enqueue(new Callback<SurveyResponse>() {
            @Override
            public void onResponse(@NonNull Call<SurveyResponse> call,
                                   @NonNull Response<SurveyResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    executor.execute(() -> {
                        SurveyEntity entity = saveSurveyFromResponse(response.body());
                        mainHandler.post(() -> result.setValue(Resource.success(entity)));
                    });
                } else {
                    String errorMessage = parseError(response);
                    result.setValue(Resource.error(errorMessage));
                }
            }

            @Override
            public void onFailure(@NonNull Call<SurveyResponse> call, @NonNull Throwable t) {
                result.setValue(Resource.error("Network error: " + t.getMessage()));
            }
        });

        return result;
    }

    // ==================== LOCAL QUERIES ====================

    /**
     * Get survey by ID from local database.
     */
    public LiveData<SurveyEntity> getSurveyById(String surveyId) {
        return surveyDao.getSurveyById(surveyId);
    }

    /**
     * Get all section data for a survey.
     */
    public LiveData<TopographicEntity> getTopographicData(String surveyId) {
        return topographicDao.getBySurveyId(surveyId);
    }

    public LiveData<VegetationEntity> getVegetationData(String surveyId) {
        return vegetationDao.getBySurveyId(surveyId);
    }

    public LiveData<SoilEntity> getSoilData(String surveyId) {
        return soilDao.getBySurveyId(surveyId);
    }

    public LiveData<WaterEntity> getWaterData(String surveyId) {
        return waterDao.getBySurveyId(surveyId);
    }

    public LiveData<BiodiversityEntity> getBiodiversityData(String surveyId) {
        return biodiversityDao.getBySurveyId(surveyId);
    }

    public LiveData<HazardEntity> getHazardData(String surveyId) {
        return hazardDao.getBySurveyId(surveyId);
    }

    public LiveData<InfrastructureEntity> getInfrastructureData(String surveyId) {
        return infrastructureDao.getBySurveyId(surveyId);
    }

    /**
     * Get distinct values for filter dropdowns.
     */
    public LiveData<List<String>> getDistinctSurveyTypes() {
        return surveyDao.getDistinctSurveyTypes();
    }

    public LiveData<List<String>> getDistinctWeatherConditions() {
        return surveyDao.getDistinctWeatherConditions();
    }

    public LiveData<Integer> getTotalCount() {
        return surveyDao.getTotalCount();
    }

    public LiveData<Integer> getPendingCount() {
        return surveyDao.getCountBySyncStatus(Constants.SYNC_PENDING);
    }

    // ==================== CREATE ====================

    /**
     * Create a new survey locally with pending sync status.
     * Returns the generated survey ID.
     */
    public LiveData<Resource<String>> createSurvey(SurveyEntity survey,
                                                   TopographicEntity topographic,
                                                   VegetationEntity vegetation,
                                                   SoilEntity soil,
                                                   WaterEntity water,
                                                   BiodiversityEntity biodiversity,
                                                   HazardEntity hazard,
                                                   InfrastructureEntity infrastructure) {
        MutableLiveData<Resource<String>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        executor.execute(() -> {
            try {
                // Generate UUID if not set
                if (survey.getSurveyId() == null || survey.getSurveyId().isEmpty()) {
                    survey.setSurveyId(UUID.randomUUID().toString());
                }

                // Set timestamps
                String now = getCurrentTimestamp();
                survey.setCreatedAt(now);
                survey.setUpdatedAt(now);
                survey.setSyncStatus(Constants.SYNC_PENDING);

                // Save in transaction
                database.runInTransaction(() -> {
                    // Save survey entry
                    surveyDao.insert(survey);

                    // Save sections (set survey_id on each)
                    String surveyId = survey.getSurveyId();

                    if (topographic != null) {
                        topographic.setSurveyId(surveyId);
                        topographicDao.insert(topographic);
                    }
                    if (vegetation != null) {
                        vegetation.setSurveyId(surveyId);
                        vegetationDao.insert(vegetation);
                    }
                    if (soil != null) {
                        soil.setSurveyId(surveyId);
                        soilDao.insert(soil);
                    }
                    if (water != null) {
                        water.setSurveyId(surveyId);
                        waterDao.insert(water);
                    }
                    if (biodiversity != null) {
                        biodiversity.setSurveyId(surveyId);
                        biodiversityDao.insert(biodiversity);
                    }
                    if (hazard != null) {
                        hazard.setSurveyId(surveyId);
                        hazardDao.insert(hazard);
                    }
                    if (infrastructure != null) {
                        infrastructure.setSurveyId(surveyId);
                        infrastructureDao.insert(infrastructure);
                    }
                });

                mainHandler.post(() -> result.setValue(Resource.success(survey.getSurveyId())));

            } catch (Exception e) {
                mainHandler.post(() -> result.setValue(Resource.error("Failed to create survey: " + e.getMessage())));
            }
        });

        return result;
    }

    // ==================== UPDATE ====================

    /**
     * Update an existing survey locally with pending sync status.
     */
    public LiveData<Resource<Boolean>> updateSurvey(SurveyEntity survey,
                                                    TopographicEntity topographic,
                                                    VegetationEntity vegetation,
                                                    SoilEntity soil,
                                                    WaterEntity water,
                                                    BiodiversityEntity biodiversity,
                                                    HazardEntity hazard,
                                                    InfrastructureEntity infrastructure) {
        MutableLiveData<Resource<Boolean>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        executor.execute(() -> {
            try {
                // Update timestamp and sync status
                survey.setUpdatedAt(getCurrentTimestamp());
                survey.setSyncStatus(Constants.SYNC_PENDING);

                database.runInTransaction(() -> {
                    // Update survey entry
                    surveyDao.update(survey);

                    String surveyId = survey.getSurveyId();

                    // Update sections (use insert with REPLACE strategy)
                    if (topographic != null) {
                        topographic.setSurveyId(surveyId);
                        topographicDao.insert(topographic);
                    } else {
                        topographicDao.deleteBySurveyId(surveyId);
                    }

                    if (vegetation != null) {
                        vegetation.setSurveyId(surveyId);
                        vegetationDao.insert(vegetation);
                    } else {
                        vegetationDao.deleteBySurveyId(surveyId);
                    }

                    if (soil != null) {
                        soil.setSurveyId(surveyId);
                        soilDao.insert(soil);
                    } else {
                        soilDao.deleteBySurveyId(surveyId);
                    }

                    if (water != null) {
                        water.setSurveyId(surveyId);
                        waterDao.insert(water);
                    } else {
                        waterDao.deleteBySurveyId(surveyId);
                    }

                    if (biodiversity != null) {
                        biodiversity.setSurveyId(surveyId);
                        biodiversityDao.insert(biodiversity);
                    } else {
                        biodiversityDao.deleteBySurveyId(surveyId);
                    }

                    if (hazard != null) {
                        hazard.setSurveyId(surveyId);
                        hazardDao.insert(hazard);
                    } else {
                        hazardDao.deleteBySurveyId(surveyId);
                    }

                    if (infrastructure != null) {
                        infrastructure.setSurveyId(surveyId);
                        infrastructureDao.insert(infrastructure);
                    } else {
                        infrastructureDao.deleteBySurveyId(surveyId);
                    }
                });

                mainHandler.post(() -> result.setValue(Resource.success(true)));

            } catch (Exception e) {
                mainHandler.post(() -> result.setValue(Resource.error("Failed to update survey: " + e.getMessage())));
            }
        });

        return result;
    }

    // ==================== DELETE ====================

    /**
     * Delete survey locally first (optimistic), then sync delete to server.
     */
    public LiveData<Resource<Boolean>> deleteSurvey(String surveyId) {
        MutableLiveData<Resource<Boolean>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        // Delete locally first (cascade will delete sections)
        executor.execute(() -> {
            surveyDao.deleteById(surveyId);

            // Then try to delete on server
            mainHandler.post(() -> {
                surveyApi.deleteSurvey(surveyId).enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                        // Success or 404 (already deleted) - both are fine
                        result.setValue(Resource.success(true));
                    }

                    @Override
                    public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                        // Still return success since local delete succeeded
                        result.setValue(Resource.success(true));
                    }
                });
            });
        });

        return result;
    }

    // ==================== SYNC PENDING SURVEYS ====================

    /**
     * Sync all pending surveys to the server.
     */
    public LiveData<Resource<SyncResult>> syncPendingSurveys() {
        MutableLiveData<Resource<SyncResult>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        executor.execute(() -> {
            List<SurveyEntity> pendingSurveys = surveyDao.getPendingSurveys();
            SyncResult syncResult = new SyncResult();
            syncResult.total = pendingSurveys.size();

            if (pendingSurveys.isEmpty()) {
                mainHandler.post(() -> result.setValue(Resource.success(syncResult)));
                return;
            }

            for (SurveyEntity survey : pendingSurveys) {
                try {
                    Map<String, Object> payload = buildSurveyPayload(survey);
                    Response<SurveyResponse> response = surveyApi.syncSurvey(payload).execute();

                    if (response.isSuccessful()) {
                        surveyDao.updateSyncStatusWithTimestamp(
                                survey.getSurveyId(),
                                Constants.SYNC_SYNCED,
                                getCurrentTimestamp()
                        );
                        syncResult.synced++;
                    } else {
                        surveyDao.updateSyncStatus(survey.getSurveyId(), Constants.SYNC_FAILED);
                        syncResult.failed++;
                    }
                } catch (Exception e) {
                    surveyDao.updateSyncStatus(survey.getSurveyId(), Constants.SYNC_FAILED);
                    syncResult.failed++;
                }
            }

            mainHandler.post(() -> result.setValue(Resource.success(syncResult)));
        });

        return result;
    }

    /**
     * Build API payload from local entities.
     */
    private Map<String, Object> buildSurveyPayload(SurveyEntity survey) {
        Map<String, Object> payload = new HashMap<>();

        // Metadata fields
        payload.put("survey_id", survey.getSurveyId());
        payload.put("observer_id", survey.getObserverId());
        payload.put("observer_name", survey.getObserverName());
        payload.put("organization", survey.getOrganization());
        payload.put("survey_date", survey.getSurveyDate());
        payload.put("survey_time", survey.getSurveyTime());
        payload.put("weather_condition", survey.getWeatherCondition());
        payload.put("survey_method", survey.getSurveyMethod());
        payload.put("gps_latitude", survey.getGpsLatitude());
        payload.put("gps_longitude", survey.getGpsLongitude());
        payload.put("gps_accuracy_meters", survey.getGpsAccuracyMeters());
        payload.put("notes", survey.getNotes());
        payload.put("survey_type", survey.getSurveyType());

        // Parse JSON fields
        if (survey.getPhotos() != null) {
            payload.put("photos", gson.fromJson(survey.getPhotos(), List.class));
        }
        if (survey.getDeviceInfo() != null) {
            payload.put("device_info", gson.fromJson(survey.getDeviceInfo(), Map.class));
        }

        // Add sections
        String surveyId = survey.getSurveyId();

        TopographicEntity topo = topographicDao.getBySurveyIdSync(surveyId);
        if (topo != null) {
            payload.put("topographic", buildTopographicMap(topo));
        }

        VegetationEntity veg = vegetationDao.getBySurveyIdSync(surveyId);
        if (veg != null) {
            payload.put("vegetation", buildVegetationMap(veg));
        }

        SoilEntity soilEntity = soilDao.getBySurveyIdSync(surveyId);
        if (soilEntity != null) {
            payload.put("soil", buildSoilMap(soilEntity));
        }

        WaterEntity waterEntity = waterDao.getBySurveyIdSync(surveyId);
        if (waterEntity != null) {
            payload.put("water", buildWaterMap(waterEntity));
        }

        BiodiversityEntity bio = biodiversityDao.getBySurveyIdSync(surveyId);
        if (bio != null) {
            payload.put("biodiversity", buildBiodiversityMap(bio));
        }

        HazardEntity hazardEntity = hazardDao.getBySurveyIdSync(surveyId);
        if (hazardEntity != null) {
            payload.put("hazard", buildHazardMap(hazardEntity));
        }

        InfrastructureEntity infra = infrastructureDao.getBySurveyIdSync(surveyId);
        if (infra != null) {
            payload.put("infrastructure", buildInfrastructureMap(infra));
        }

        return payload;
    }

    // Section map builders
    private Map<String, Object> buildTopographicMap(TopographicEntity e) {
        Map<String, Object> map = new HashMap<>();
        map.put("elevation_m", e.getElevationM());
        map.put("slope_gradient_deg", e.getSlopeGradientDeg());
        map.put("slope_aspect", e.getSlopeAspect());
        map.put("landform_type", e.getLandformType());
        map.put("drainage_pattern", e.getDrainagePattern());
        map.put("land_use", e.getLandUse());
        map.put("land_cover_description", e.getLandCoverDescription());
        return map;
    }

    private Map<String, Object> buildVegetationMap(VegetationEntity e) {
        Map<String, Object> map = new HashMap<>();
        map.put("vegetation_type", e.getVegetationType());
        map.put("canopy_cover_pct", e.getCanopyCoverPct());
        map.put("canopy_height_m", e.getCanopyHeightM());
        map.put("dominant_species", e.getDominantSpecies());
        map.put("invasive_species_present", e.getInvasiveSpeciesPresent());
        if (e.getInvasiveSpeciesNames() != null) {
            map.put("invasive_species_names", gson.fromJson(e.getInvasiveSpeciesNames(), List.class));
        }
        map.put("dbh_cm", e.getDbhCm());
        map.put("ndvi_observation", e.getNdviObservation());
        map.put("vegetation_disturbance", e.getVegetationDisturbance());
        return map;
    }

    private Map<String, Object> buildSoilMap(SoilEntity e) {
        Map<String, Object> map = new HashMap<>();
        map.put("soil_type", e.getSoilType());
        map.put("soil_texture", e.getSoilTexture());
        map.put("soil_color_munsell", e.getSoilColorMunsell());
        map.put("erosion_level", e.getErosionLevel());
        map.put("erosion_type", e.getErosionType());
        map.put("exposed_roots", e.getExposedRoots());
        map.put("compaction_signs", e.getCompactionSigns());
        map.put("disturbance_signs", e.getDisturbanceSigns());
        return map;
    }

    private Map<String, Object> buildWaterMap(WaterEntity e) {
        Map<String, Object> map = new HashMap<>();
        map.put("water_body_present", e.getWaterBodyPresent());
        map.put("water_body_type", e.getWaterBodyType());
        map.put("water_clarity", e.getWaterClarity());
        map.put("flooding_signs", e.getFloodingSigns());
        map.put("waterlogging_level", e.getWaterloggingLevel());
        map.put("distance_to_drainage_m", e.getDistanceToDrainageM());
        map.put("flow_direction", e.getFlowDirection());
        return map;
    }

    private Map<String, Object> buildBiodiversityMap(BiodiversityEntity e) {
        Map<String, Object> map = new HashMap<>();
        map.put("wildlife_sighting", e.getWildlifeSighting());
        if (e.getWildlifeSpecies() != null) {
            map.put("wildlife_species", gson.fromJson(e.getWildlifeSpecies(), List.class));
        }
        map.put("wildlife_evidence_type", e.getWildlifeEvidenceType());
        map.put("sensitive_habitat_present", e.getSensitiveHabitatPresent());
        map.put("sensitive_habitat_type", e.getSensitiveHabitatType());
        map.put("protected_species_flagged", e.getProtectedSpeciesFlagged());
        map.put("protected_species_notes", e.getProtectedSpeciesNotes());
        return map;
    }

    private Map<String, Object> buildHazardMap(HazardEntity e) {
        Map<String, Object> map = new HashMap<>();
        map.put("landslide_risk", e.getLandslideRisk());
        map.put("erosion_risk", e.getErosionRisk());
        map.put("flood_risk", e.getFloodRisk());
        map.put("contamination_signs", e.getContaminationSigns());
        map.put("contamination_type", e.getContaminationType());
        map.put("proximity_to_industrial_m", e.getProximityToIndustrialM());
        return map;
    }

    private Map<String, Object> buildInfrastructureMap(InfrastructureEntity e) {
        Map<String, Object> map = new HashMap<>();
        map.put("roads_or_tracks_present", e.getRoadsOrTracksPresent());
        map.put("road_condition", e.getRoadCondition());
        map.put("structures_present", e.getStructuresPresent());
        map.put("utilities_present", e.getUtilitiesPresent());
        map.put("land_disturbance_history", e.getLandDisturbanceHistory());
        map.put("illegal_dumping_present", e.getIllegalDumpingPresent());
        if (e.getPollutionPoints() != null) {
            map.put("pollution_points", gson.fromJson(e.getPollutionPoints(), List.class));
        }
        return map;
    }

    // ==================== HELPERS ====================

    /**
     * Save survey and all sections from API response.
     */
    private SurveyEntity saveSurveyFromResponse(SurveyResponse response) {
        return dbHelper.saveSurveyWithSections(response);
    }

    private String getCurrentTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        return sdf.format(new Date());
    }

    private String parseError(Response<?> response) {
        try {
            if (response.errorBody() != null) {
                String errorJson = response.errorBody().string();
                ErrorResponse errorResponse = gson.fromJson(errorJson, ErrorResponse.class);
                if (errorResponse != null) {
                    return errorResponse.getDisplayMessage();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Error: " + response.code();
    }

    // ==================== INNER CLASSES ====================

    /**
     * Filter criteria for survey queries.
     */
    public static class SurveyFilter {
        private String surveyType;
        private String weatherCondition;
        private String observerId;
        private String dateFrom;
        private String dateTo;
        private String searchQuery;

        public String getSurveyType() { return surveyType; }
        public void setSurveyType(String surveyType) { this.surveyType = surveyType; }

        public String getWeatherCondition() { return weatherCondition; }
        public void setWeatherCondition(String weatherCondition) { this.weatherCondition = weatherCondition; }

        public String getObserverId() { return observerId; }
        public void setObserverId(String observerId) { this.observerId = observerId; }

        public String getDateFrom() { return dateFrom; }
        public void setDateFrom(String dateFrom) { this.dateFrom = dateFrom; }

        public String getDateTo() { return dateTo; }
        public void setDateTo(String dateTo) { this.dateTo = dateTo; }

        public String getSearchQuery() { return searchQuery; }
        public void setSearchQuery(String searchQuery) { this.searchQuery = searchQuery; }

        public boolean isEmpty() {
            return (surveyType == null || surveyType.isEmpty()) &&
                    (weatherCondition == null || weatherCondition.isEmpty()) &&
                    (observerId == null || observerId.isEmpty()) &&
                    (dateFrom == null || dateFrom.isEmpty()) &&
                    (dateTo == null || dateTo.isEmpty()) &&
                    (searchQuery == null || searchQuery.isEmpty());
        }

        public void clear() {
            surveyType = null;
            weatherCondition = null;
            observerId = null;
            dateFrom = null;
            dateTo = null;
            searchQuery = null;
        }
    }

    /**
     * Result of sync operation.
     */
    public static class SyncResult {
        public int total = 0;
        public int synced = 0;
        public int failed = 0;

        public boolean isFullSuccess() {
            return failed == 0 && synced == total;
        }
    }
}