package app.practice.canopy_native_android.models.dto;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

import app.practice.canopy_native_android.database.entities.BiodiversityEntity;
import app.practice.canopy_native_android.database.entities.HazardEntity;
import app.practice.canopy_native_android.database.entities.InfrastructureEntity;
import app.practice.canopy_native_android.database.entities.SoilEntity;
import app.practice.canopy_native_android.database.entities.SurveyEntity;
import app.practice.canopy_native_android.database.entities.TopographicEntity;
import app.practice.canopy_native_android.database.entities.VegetationEntity;
import app.practice.canopy_native_android.database.entities.WaterEntity;
import app.practice.canopy_native_android.utils.Constants;


/// Full survey response including all sections.
/// Maps directly to the API response shape.
public class SurveyResponse {

    // -- metadata

    @SerializedName("survey_id")
    private String surveyId;

    @SerializedName("observer_id")
    private String observerId;

    @SerializedName("observer_name")
    private String observerName;

    @SerializedName("organization")
    private String organization;

    @SerializedName("survey_date")
    private String surveyDate;

    @SerializedName("survey_time")
    private String surveyTime;

    @SerializedName("weather_condition")
    private String weatherCondition;

    @SerializedName("survey_method")
    private String surveyMethod;

    @SerializedName("gps_latitude")
    private Double gpsLatitude;

    @SerializedName("gps_longitude")
    private Double gpsLongitude;

    @SerializedName("gps_accuracy_meters")
    private Float gpsAccuracyMeters;

    @SerializedName("notes")
    private String notes;

    @SerializedName("photos")
    private List<String> photos;

    @SerializedName("survey_type")
    private String surveyType;

    @SerializedName("device_info")
    private Map<String, Object> deviceInfo;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("updated_at")
    private String updatedAt;

    // -- sections

    @SerializedName("topographic")
    private TopographicSection topographic;

    @SerializedName("vegetation")
    private VegetationSection vegetation;

    @SerializedName("soil")
    private SoilSection soil;

    @SerializedName("water")
    private WaterSection water;

    @SerializedName("biodiversity")
    private BiodiversitySection biodiversity;

    @SerializedName("hazard")
    private HazardSection hazard;

    @SerializedName("infrastructure")
    private InfrastructureSection infrastructure;

    // -- entity conversion

    public SurveyEntity toSurveyEntity() {
        SurveyEntity entity = new SurveyEntity(
                surveyId,
                observerId,
                observerName,
                surveyDate,
                surveyTime,
                weatherCondition,
                surveyMethod,
                Constants.SYNC_SYNCED,  // data from srv is synced
                createdAt,
                updatedAt
        );
        entity.setOrganization(organization);
        entity.setGpsLatitude(gpsLatitude);
        entity.setGpsLongitude(gpsLongitude);
        entity.setGpsAccuracyMeters(gpsAccuracyMeters);
        entity.setNotes(notes);
        entity.setSurveyType(surveyType);

        // convert lists / maps to JSON string
        if (photos != null) {
            entity.setPhotos(new com.google.gson.Gson().toJson(photos));
        }
        if (deviceInfo != null) {
            entity.setDeviceInfo(new com.google.gson.Gson().toJson(deviceInfo));
        }

        return entity;
    }

    public TopographicEntity toTopographicEntity() {
        if (topographic == null) return null;
        return topographic.toEntity(surveyId);
    }

    public VegetationEntity toVegetationEntity() {
        if (vegetation == null) return null;
        return vegetation.toEntity(surveyId);
    }

    public SoilEntity toSoilEntity() {
        if (soil == null) return null;
        return soil.toEntity(surveyId);
    }

    public WaterEntity toWaterEntity() {
        if (water == null) return null;
        return water.toEntity(surveyId);
    }

    public BiodiversityEntity toBiodiversityEntity() {
        if (biodiversity == null) return null;
        return biodiversity.toEntity(surveyId);
    }

    public HazardEntity toHazardEntity() {
        if (hazard == null) return null;
        return hazard.toEntity(surveyId);
    }

    public InfrastructureEntity toInfrastructureEntity() {
        if (infrastructure == null) return null;
        return infrastructure.toEntity(surveyId);
    }

    // -- getters

    public String getSurveyId() { return surveyId; }
    public String getObserverId() { return observerId; }
    public String getObserverName() { return observerName; }
    public String getOrganization() { return organization; }
    public String getSurveyDate() { return surveyDate; }
    public String getSurveyTime() { return surveyTime; }
    public String getWeatherCondition() { return weatherCondition; }
    public String getSurveyMethod() { return surveyMethod; }
    public Double getGpsLatitude() { return gpsLatitude; }
    public Double getGpsLongitude() { return gpsLongitude; }
    public Float getGpsAccuracyMeters() { return gpsAccuracyMeters; }
    public String getNotes() { return notes; }
    public List<String> getPhotos() { return photos; }
    public String getSurveyType() { return surveyType; }
    public Map<String, Object> getDeviceInfo() { return deviceInfo; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }
    public TopographicSection getTopographic() { return topographic; }
    public VegetationSection getVegetation() { return vegetation; }
    public SoilSection getSoil() { return soil; }
    public WaterSection getWater() { return water; }
    public BiodiversitySection getBiodiversity() { return biodiversity; }
    public HazardSection getHazard() { return hazard; }
    public InfrastructureSection getInfrastructure() { return infrastructure; }

    // -- section classes

    public static class TopographicSection {

        @SerializedName("elevation_m")
        private Double elevationM;

        @SerializedName("slope_gradient_deg")
        private double slopeGradientDeg;

        @SerializedName("slope_aspect")
        private String slopeAspect;

        @SerializedName("landform_type")
        private String landformType;

        @SerializedName("drainage_pattern")
        private String drainagePattern;

        @SerializedName("land_use")
        private String landUse;

        @SerializedName("land_cover_description")
        private String landCoverDescription;

        public TopographicEntity toEntity(String surveyId) {
            TopographicEntity entity = new TopographicEntity(surveyId);
            entity.setElevationM(elevationM);
            entity.setSlopeGradientDeg(slopeGradientDeg);
            entity.setSlopeAspect(slopeAspect);
            entity.setLandformType(landformType);
            entity.setDrainagePattern(drainagePattern);
            entity.setLandUse(landUse);
            entity.setLandCoverDescription(landCoverDescription);
            return entity;
        }

        public Double getElevationM() { return elevationM; }
        public Double getSlopeGradientDeg() { return slopeGradientDeg; }
        public String getSlopeAspect() { return slopeAspect; }
        public String getLandformType() { return landformType; }
        public String getDrainagePattern() { return drainagePattern; }
        public String getLandUse() { return landUse; }
        public String getLandCoverDescription() { return landCoverDescription; }

    }

    public static class VegetationSection {

        @SerializedName("vegetation_type")
        private String vegetationType;

        @SerializedName("canopy_cover_pct")
        private Integer canopyCoverPct;

        @SerializedName("canopy_height_m")
        private Double canopyHeightM;

        @SerializedName("dominant_species")
        private String dominantSpecies;

        @SerializedName("invasive_species_present")
        private Boolean invasiveSpeciesPresent;

        @SerializedName("invasive_species_names")
        private List<String> invasiveSpeciesNames;

        @SerializedName("dbh_cm")
        private Double dbhCm;

        @SerializedName("ndvi_observation")
        private String ndviObservation;

        @SerializedName("vegetation_disturbance")
        private String vegetationDisturbance;

        public VegetationEntity toEntity(String surveyId) {
            VegetationEntity entity = new VegetationEntity(surveyId);
            entity.setVegetationType(vegetationType);
            entity.setCanopyCoverPct(canopyCoverPct);
            entity.setCanopyHeightM(canopyHeightM);
            entity.setDominantSpecies(dominantSpecies);
            entity.setInvasiveSpeciesPresent(invasiveSpeciesPresent);
            if (invasiveSpeciesNames != null) {
                entity.setInvasiveSpeciesNames(new com.google.gson.Gson().toJson(invasiveSpeciesNames));
            }
            entity.setDbhCm(dbhCm);
            entity.setNdviObservation(ndviObservation);
            entity.setVegetationDisturbance(vegetationDisturbance);
            return entity;
        }

        public String getVegetationType() { return vegetationType; }
        public Integer getCanopyCoverPct() { return canopyCoverPct; }
        public Double getCanopyHeightM() { return canopyHeightM; }
        public String getDominantSpecies() { return dominantSpecies; }
        public Boolean getInvasiveSpeciesPresent() { return invasiveSpeciesPresent; }
        public List<String> getInvasiveSpeciesNames() { return invasiveSpeciesNames; }
        public Double getDbhCm() { return dbhCm; }
        public String getNdviObservation() { return ndviObservation; }
        public String getVegetationDisturbance() { return vegetationDisturbance; }

    }

    public static class SoilSection {

        @SerializedName("soil_type")
        private String soilType;

        @SerializedName("soil_texture")
        private String soilTexture;

        @SerializedName("soil_color_munsell")
        private String soilColorMunsell;

        @SerializedName("erosion_level")
        private String erosionLevel;

        @SerializedName("erosion_type")
        private String erosionType;

        @SerializedName("exposed_roots")
        private Boolean exposedRoots;

        @SerializedName("compaction_signs")
        private Boolean compactionSigns;

        @SerializedName("disturbance_signs")
        private Boolean disturbanceSigns;

        public SoilEntity toEntity(String surveyId) {
            SoilEntity entity = new SoilEntity(surveyId);
            entity.setSoilType(soilType);
            entity.setSoilTexture(soilTexture);
            entity.setSoilColorMunsell(soilColorMunsell);
            entity.setErosionLevel(erosionLevel);
            entity.setErosionType(erosionType);
            entity.setExposedRoots(exposedRoots);
            entity.setCompactionSigns(compactionSigns);
            entity.setDisturbanceSigns(disturbanceSigns);
            return entity;
        }

        public String getSoilType() { return soilType; }
        public String getSoilTexture() { return soilTexture; }
        public String getSoilColorMunsell() { return soilColorMunsell; }
        public String getErosionLevel() { return erosionLevel; }
        public String getErosionType() { return erosionType; }
        public Boolean getExposedRoots() { return exposedRoots; }
        public Boolean getCompactionSigns() { return compactionSigns; }
        public Boolean getDisturbanceSigns() { return disturbanceSigns; }

    }

    public static class WaterSection {

        @SerializedName("water_body_present")
        private Boolean waterBodyPresent;

        @SerializedName("water_body_type")
        private String waterBodyType;

        @SerializedName("water_clarity")
        private String waterClarity;

        @SerializedName("flooding_signs")
        private Boolean floodingSigns;

        @SerializedName("waterlogging_level")
        private String waterloggingLevel;

        @SerializedName("distance_to_drainage_m")
        private Double distanceToDrainageM;

        @SerializedName("flow_direction")
        private String flowDirection;

        public WaterEntity toEntity(String surveyId) {
            WaterEntity entity = new WaterEntity(surveyId);
            entity.setWaterBodyPresent(waterBodyPresent);
            entity.setWaterBodyType(waterBodyType);
            entity.setWaterClarity(waterClarity);
            entity.setFloodingSigns(floodingSigns);
            entity.setWaterloggingLevel(waterloggingLevel);
            entity.setDistanceToDrainageM(distanceToDrainageM);
            entity.setFlowDirection(flowDirection);
            return entity;
        }

        public Boolean getWaterBodyPresent() { return waterBodyPresent; }
        public String getWaterBodyType() { return waterBodyType; }
        public String getWaterClarity() { return waterClarity; }
        public Boolean getFloodingSigns() { return floodingSigns; }
        public String getWaterloggingLevel() { return waterloggingLevel; }
        public Double getDistanceToDrainageM() { return distanceToDrainageM; }
        public String getFlowDirection() { return flowDirection; }

    }

    public static class BiodiversitySection {

        @SerializedName("wildlife_sighting")
        private Boolean wildlifeSighting;

        @SerializedName("wildlife_species")
        private List<String> wildlifeSpecies;

        @SerializedName("wildlife_evidence_type")
        private String wildlifeEvidenceType;

        @SerializedName("sensitive_habitat_present")
        private Boolean sensitiveHabitatPresent;

        @SerializedName("sensitive_habitat_type")
        private String sensitiveHabitatType;

        @SerializedName("protected_species_flagged")
        private Boolean protectedSpeciesFlagged;

        @SerializedName("protected_species_notes")
        private String protectedSpeciesNotes;

        public BiodiversityEntity toEntity(String surveyId) {
            BiodiversityEntity entity = new BiodiversityEntity(surveyId);
            entity.setWildlifeSighting(wildlifeSighting);
            if (wildlifeSpecies != null) {
                entity.setWildlifeSpecies(new com.google.gson.Gson().toJson(wildlifeSpecies));
            }
            entity.setWildlifeEvidenceType(wildlifeEvidenceType);
            entity.setSensitiveHabitatPresent(sensitiveHabitatPresent);
            entity.setSensitiveHabitatType(sensitiveHabitatType);
            entity.setProtectedSpeciesFlagged(protectedSpeciesFlagged);
            entity.setProtectedSpeciesNotes(protectedSpeciesNotes);
            return entity;
        }

        public Boolean getWildlifeSighting() { return wildlifeSighting; }
        public List<String> getWildlifeSpecies() { return wildlifeSpecies; }
        public String getWildlifeEvidenceType() { return wildlifeEvidenceType; }
        public Boolean getSensitiveHabitatPresent() { return sensitiveHabitatPresent; }
        public String getSensitiveHabitatType() { return sensitiveHabitatType; }
        public Boolean getProtectedSpeciesFlagged() { return protectedSpeciesFlagged; }
        public String getProtectedSpeciesNotes() { return protectedSpeciesNotes; }

    }

    public static class HazardSection {

        @SerializedName("landslide_risk")
        private String landslideRisk;

        @SerializedName("erosion_risk")
        private String erosionRisk;

        @SerializedName("flood_risk")
        private String floodRisk;

        @SerializedName("contamination_signs")
        private Boolean contaminationSigns;

        @SerializedName("contamination_type")
        private String contaminationType;

        @SerializedName("proximity_to_industrial_m")
        private Double proximityToIndustrialM;

        public HazardEntity toEntity(String surveyId) {
            HazardEntity entity = new HazardEntity(surveyId);
            entity.setLandslideRisk(landslideRisk);
            entity.setErosionRisk(erosionRisk);
            entity.setFloodRisk(floodRisk);
            entity.setContaminationSigns(contaminationSigns);
            entity.setContaminationType(contaminationType);
            entity.setProximityToIndustrialM(proximityToIndustrialM);
            return entity;
        }

        public String getLandslideRisk() { return landslideRisk; }
        public String getErosionRisk() { return erosionRisk; }
        public String getFloodRisk() { return floodRisk; }
        public Boolean getContaminationSigns() { return contaminationSigns; }
        public String getContaminationType() { return contaminationType; }
        public Double getProximityToIndustrialM() { return proximityToIndustrialM; }

    }



    public static class InfrastructureSection {

        @SerializedName("roads_or_tracks_present")
        private Boolean roadsOrTracksPresent;

        @SerializedName("road_condition")
        private String roadCondition;

        @SerializedName("structures_present")
        private Boolean structuresPresent;

        @SerializedName("utilities_present")
        private Boolean utilitiesPresent;

        @SerializedName("land_disturbance_history")
        private String landDisturbanceHistory;

        @SerializedName("illegal_dumping_present")
        private Boolean illegalDumpingPresent;

        @SerializedName("pollution_points")
        private List<Map<String, Double>> pollutionPoints;

        public InfrastructureEntity toEntity(String surveyId) {
            InfrastructureEntity entity = new InfrastructureEntity(surveyId);
            entity.setRoadsOrTracksPresent(roadsOrTracksPresent);
            entity.setRoadCondition(roadCondition);
            entity.setStructuresPresent(structuresPresent);
            entity.setUtilitiesPresent(utilitiesPresent);
            entity.setLandDisturbanceHistory(landDisturbanceHistory);
            entity.setIllegalDumpingPresent(illegalDumpingPresent);
            if (pollutionPoints != null) {
                entity.setPollutionPoints(new com.google.gson.Gson().toJson(pollutionPoints));
            }
            return entity;
        }

        public Boolean getRoadsOrTracksPresent() { return roadsOrTracksPresent; }
        public String getRoadCondition() { return roadCondition; }
        public Boolean getStructuresPresent() { return structuresPresent; }
        public Boolean getUtilitiesPresent() { return utilitiesPresent; }
        public String getLandDisturbanceHistory() { return landDisturbanceHistory; }
        public Boolean getIllegalDumpingPresent() { return illegalDumpingPresent; }
        public List<Map<String, Double>> getPollutionPoints() { return pollutionPoints; }

    }

}