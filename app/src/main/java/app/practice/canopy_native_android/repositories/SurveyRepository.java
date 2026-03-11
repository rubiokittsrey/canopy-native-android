package app.practice.canopy_native_android.repositories;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
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
import app.practice.canopy_native_android.models.dto.SurveyListResponse;
import app.practice.canopy_native_android.models.dto.SurveyResponse;
import app.practice.canopy_native_android.utils.Constants;
import app.practice.canopy_native_android.utils.Resource;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/// Repository for survey operations
/// Handles CRUD operations with offline-first sync support
public class SurveyRepository {

    private final SurveyApi surveyApi;
    private final SurveyDao surveyDao;
    private final TopographicDao topographicDao;
    private final VegetationDao vegetationDao;
    private final SoilDao soilDao;
    private final WaterDao waterDao;
    private final BiodiversityDao biodiversityDao;
    private final HazardDao hazardDao;
    private final InfrastructureDao infrastructureDao;

    private final ExecutorService executor;
    private final Handler mainHandler;
    private final Gson gson;

    public SurveyRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        this.surveyApi = ApiClient.getInstance(context).getSurveyApi();
        this.surveyDao = db.surveyDao();
        this.topographicDao = db.topographicDao();
        this.vegetationDao = db.vegetationDao();
        this.soilDao = db.soilDao();
        this.waterDao = db.waterDao();
        this.biodiversityDao = db.biodiversityDao();
        this.hazardDao = db.hazardDao();
        this.infrastructureDao = db.infrastructureDao();

        this.executor = Executors.newFixedThreadPool(4);
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.gson = new Gson();
    }

    // ---- paged queries

    // this observes the local database - call refreshSurveys() to fetch from API
    public LiveData<PagingData<SurveyEntity>> getSurveysPaged() {
        return PagingLiveData.getLiveData(
            new Pager<>(
                new PagingConfig(
                    Constants.PAGE_SIZE,
                        Constants.PAGE_SIZE / 2,  // prefetchDistance
                        false,  // enablePlaceholders
                        Constants.PAGE_SIZE * 3   // initialLoadSize
                    ),
                    surveyDao::getSurveysPaged
            )
        );
    }

    // retrieve surveys with filtering and paging
    public LiveData<PagingData<SurveyEntity>> getSurveysFilteredPaged(SurveyFilter filter) {
        return PagingLiveData.getLiveData(
            new Pager<>(
                new PagingConfig(
                    Constants.PAGE_SIZE,
                    Constants.PAGE_SIZE / 2,
                    false,
                    Constants.PAGE_SIZE * 3
                    ),
                    () -> surveyDao.getSurveysFilteredPaged(buildFilterQuery(filter))
                )
        );
    }

    public LiveData<PagingData<SurveyEntity>> searchSurveysPaged(String query) {
        return PagingLiveData.getLiveData(
            new Pager<>(
                new PagingConfig(
                    Constants.PAGE_SIZE,
                    Constants.PAGE_SIZE / 2,
                    false,
                    Constants.PAGE_SIZE * 3
                ),
                () -> surveyDao.searchSurveysPaged(query)
            )
        );
    }

    // dynamic SQL queries for filtering
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

        queryBuilder.append(" ORDER BY survey_date DESC, survey_time DESC");

        return new SimpleSQLiteQuery(queryBuilder.toString(), args.toArray());
    }

    // --- fetch from api

    // refresh surveys from API and save to local database
    // fetches multiple pages until all the data is loaded
    public LiveData<Resource<Integer>> refreshSurveys() {
        MutableLiveData<Resource<Integer>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        fetchAllPages(1, 0, result);

        return result;
    }

    private void fetchAllPages(int page, int totalFetched, MutableLiveData<Resource<Integer>> result) {
        surveyApi.getSurveys(page, Constants.PAGE_SIZE).enqueue(new Callback<SurveyListResponse>() {
            @Override
            public void onResponse(@NonNull Call<SurveyListResponse> call,
                                   @NonNull Response<SurveyListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    SurveyListResponse listResponse = response.body();
                    List<SurveyResponse> surveys = listResponse.getResults();

                    executor.execute(() -> {
                        for (SurveyResponse surveyResponse : surveys) {
                            saveSurveyFromResponse(surveyResponse);
                        }

                        int newTotal = totalFetched + surveys.size();

                        if (listResponse.hasNext()) {
                            mainHandler.post(() -> fetchAllPages(page + 1, newTotal, result));
                        } else {
                            mainHandler.post(() -> result.setValue(Resource.success(newTotal)));
                        }
                    });
                } else {
                    String errorMessage = parseError(response);
                    result.setValue(Resource.error(errorMessage, totalFetched));
                }
            }

            @Override
            public void onFailure(@NonNull Call<SurveyListResponse> call, @NonNull Throwable t) {
                result.setValue(Resource.error("Network error: " + t.getMessage(), totalFetched));
            }
        });
    }

    // fetch single survey by id
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

    // --- local queries (from local db)

    public LiveData<SurveyEntity> getSurveyById(String surveyId) {
        return surveyDao.getSurveyById(surveyId);
    }

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

    // get distinct values for filter dropdowns
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

    // --- create


    // new survey locally with pending sync status as default
    // will return generated survey id
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
                if (survey.getSurveyId().isEmpty()) {
                    survey.setSurveyId(UUID.randomUUID().toString());
                }

                // set timestamps
                String now = getCurrentTimestamp();
                survey.setCreatedAt(now);
                survey.setUpdatedAt(now);
                survey.setSyncStatus(Constants.SYNC_PENDING);

                // save survey entry
                surveyDao.insert(survey);

                // save sections with survey id on each
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

                mainHandler.post(() -> result.setValue(Resource.success(surveyId)));

            } catch (Exception e) {
                mainHandler.post(() -> result.setValue(Resource.error("Failed to create survey: " + e.getMessage())));
            }
        });

        return result;
    }

    // --- update

    // updates an existing survey locally with pending sync status
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
                // update timestamp and sync status
                survey.setUpdatedAt(getCurrentTimestamp());
                survey.setSyncStatus(Constants.SYNC_PENDING);

                // update survey entry
                surveyDao.update(survey);

                String surveyId = survey.getSurveyId();

                // update sections
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

                mainHandler.post(() -> result.setValue(Resource.success(true)));

            } catch (Exception e) {
                mainHandler.post(() -> result.setValue(Resource.error("Failed to update survey: " + e.getMessage())));
            }
        });

        return result;
    }

    // --- delete

    // delete survey locally first (optimistic), then sync delete to server
    public LiveData<Resource<Boolean>> deleteSurvey(String surveyId) {
        MutableLiveData<Resource<Boolean>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        // delete operation, cascade will delete sections
        executor.execute(() -> {
            surveyDao.deleteById(surveyId);

            // attempt delete on server
            mainHandler.post(() -> {
                surveyApi.deleteSurvey(surveyId).enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                        result.setValue(Resource.success(true));
                    }

                    @Override
                    public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                        // TODO: probably implement better delete mechanism
                        // like soft delete survey items until confirmed delete on server
                        result.setValue(Resource.success(true));
                    }
                });
            });
        });

        return result;
    }

    // --- sync

    // sync all pending surveys to the server
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

    // build API payload from local entities
    private Map<String, Object> buildSurveyPayload(SurveyEntity survey) {
        Map<String, Object> payload = new HashMap<>();

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

        // parse JSON fields
        if (survey.getPhotos() != null) {
            payload.put("photos", gson.fromJson(survey.getPhotos(), List.class));
        }
        if (survey.getDeviceInfo() != null) {
            payload.put("device_info", gson.fromJson(survey.getDeviceInfo(), Map.class));
        }

        String surveyId = survey.getSurveyId();

        TopographicEntity topo = topographicDao.getBySurveyIdSync(surveyId);
        if (topo != null) {
            payload.put("topographic", buildTopographicMap(topo));
        }

        VegetationEntity veg = vegetationDao.getBySurveyIdSync(surveyId);
        if (veg != null) {
            payload.put("vegetation", buildVegetationMap(veg));
        }

        SoilEntity soil = soilDao.getBySurveyIdSync(surveyId);
        if (soil != null) {
            payload.put("soil", buildSoilMap(soil));
        }

        WaterEntity water = waterDao.getBySurveyIdSync(surveyId);
        if (water != null) {
            payload.put("water", buildWaterMap(water));
        }

        BiodiversityEntity bio = biodiversityDao.getBySurveyIdSync(surveyId);
        if (bio != null) {
            payload.put("biodiversity", buildBiodiversityMap(bio));
        }

        HazardEntity hazard = hazardDao.getBySurveyIdSync(surveyId);
        if (hazard != null) {
            payload.put("hazard", buildHazardMap(hazard));
        }

        InfrastructureEntity infra = infrastructureDao.getBySurveyIdSync(surveyId);
        if (infra != null) {
            payload.put("infrastructure", buildInfrastructureMap(infra));
        }

        return payload;
    }

    // --- section map builders

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

    // --- helpers


    // save survey and all sections from API response
    private SurveyEntity saveSurveyFromResponse(SurveyResponse response) {
        SurveyEntity entity = response.toSurveyEntity();
        surveyDao.insert(entity);

        String surveyId = entity.getSurveyId();

        TopographicEntity topo = response.toTopographicEntity();
        if (topo != null) {
            topographicDao.insert(topo);
        }

        VegetationEntity veg = response.toVegetationEntity();
        if (veg != null) {
            vegetationDao.insert(veg);
        }

        SoilEntity soil = response.toSoilEntity();
        if (soil != null) {
            soilDao.insert(soil);
        }

        WaterEntity water = response.toWaterEntity();
        if (water != null) {
            waterDao.insert(water);
        }

        BiodiversityEntity bio = response.toBiodiversityEntity();
        if (bio != null) {
            biodiversityDao.insert(bio);
        }

        HazardEntity hazard = response.toHazardEntity();
        if (hazard != null) {
            hazardDao.insert(hazard);
        }

        InfrastructureEntity infra = response.toInfrastructureEntity();
        if (infra != null) {
            infrastructureDao.insert(infra);
        }

        return entity;
    }

    private String getCurrentTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        return sdf.format(new Date());
    }

    private String parseError(Response<?> response) {

        try (ResponseBody errorBody = response.errorBody()) {
            if (errorBody != null) {
                String errorJson = errorBody.string();
                ErrorResponse errorResponse = new com.google.gson.Gson()
                        .fromJson(errorJson, ErrorResponse.class);

                if (errorResponse != null) {
                    return errorResponse.getDisplayMessage();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "Error: " + response.code();
    }

    // filtered criteria for survey queries
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

    public static class SyncResult {
        public int total = 0;
        public int synced = 0;
        public int failed = 0;

        public boolean isFullSuccess() {
            return failed == 0 && synced == total;
        }
    }

}
