package app.practice.canopy_native_android.repositories;

import app.practice.canopy_native_android.database.AppDatabase;
import app.practice.canopy_native_android.database.entities.SurveyEntity;
import app.practice.canopy_native_android.models.dto.SurveyResponse;

/**
 * Shared helper for saving survey responses to the database.
 * Used by RemoteMediators and SurveyRepository to avoid duplicating
 * the survey + sections insert logic.
 */
public class SurveyDatabaseHelper {

    private final AppDatabase database;

    public SurveyDatabaseHelper(AppDatabase database) {
        this.database = database;
    }

    /**
     * Save a survey and all its sections from an API response.
     * Uses REPLACE conflict strategy so existing data is updated.
     *
     * @return the saved SurveyEntity
     */
    public SurveyEntity saveSurveyWithSections(SurveyResponse response) {
        return saveSurveyWithSections(response, Integer.MAX_VALUE);
    }

    /**
     * Save a survey with a specific remote order for paging position.
     *
     * @param remoteOrder the positional order from the API (page * pageSize + index)
     * @return the saved SurveyEntity
     */
    public SurveyEntity saveSurveyWithSections(SurveyResponse response, int remoteOrder) {
        SurveyEntity entity = response.toSurveyEntity();
        entity.setRemoteOrder(remoteOrder);
        database.surveyDao().insert(entity);

        if (response.getTopographic() != null) {
            database.topographicDao().insert(response.toTopographicEntity());
        }
        if (response.getVegetation() != null) {
            database.vegetationDao().insert(response.toVegetationEntity());
        }
        if (response.getSoil() != null) {
            database.soilDao().insert(response.toSoilEntity());
        }
        if (response.getWater() != null) {
            database.waterDao().insert(response.toWaterEntity());
        }
        if (response.getBiodiversity() != null) {
            database.biodiversityDao().insert(response.toBiodiversityEntity());
        }
        if (response.getHazard() != null) {
            database.hazardDao().insert(response.toHazardEntity());
        }
        if (response.getInfrastructure() != null) {
            database.infrastructureDao().insert(response.toInfrastructureEntity());
        }

        return entity;
    }
}
