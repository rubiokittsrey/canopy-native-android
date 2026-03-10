package app.practice.canopy_native_android.database.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "vegetation_data",
        foreignKeys = @ForeignKey(
                entity = SurveyEntity.class,
                parentColumns = "survey_id",
                childColumns = "survey_id",
                onDelete = ForeignKey.CASCADE
        ),
        indices = @Index(value = "survey_id")
)
public class VegetationEntity {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "survey_id")
    private String surveyId;

    @ColumnInfo(name = "vegetation_type")
    private String vegetationType;

    @ColumnInfo(name = "canopy_cover_pct")
    private Integer canopyCoverPct;

    @ColumnInfo(name = "canopy_height_m")
    private Double canopyHeightM;

    @ColumnInfo(name = "dominant_species")
    private String dominantSpecies;

    @ColumnInfo(name = "invasive_species_present")
    private Boolean invasiveSpeciesPresent;

    // JSON array string: ["species1", "species2"]
    @ColumnInfo(name = "invasive_species_names")
    private String invasiveSpeciesNames;

    @ColumnInfo(name = "dbh_cm")
    private Double dbhCm;

    @ColumnInfo(name = "ndvi_observation")
    private String ndviObservation;

    @ColumnInfo(name = "vegetation_disturbance")
    private String vegetationDisturbance;

    public VegetationEntity(@NonNull String surveyId) {
        this.surveyId = surveyId;
    }

    // Getters and Setters
    @NonNull
    public String getSurveyId() { return surveyId; }
    public void setSurveyId(@NonNull String surveyId) { this.surveyId = surveyId; }

    public String getVegetationType() { return vegetationType; }
    public void setVegetationType(String vegetationType) { this.vegetationType = vegetationType; }

    public Integer getCanopyCoverPct() { return canopyCoverPct; }
    public void setCanopyCoverPct(Integer canopyCoverPct) { this.canopyCoverPct = canopyCoverPct; }

    public Double getCanopyHeightM() { return canopyHeightM; }
    public void setCanopyHeightM(Double canopyHeightM) { this.canopyHeightM = canopyHeightM; }

    public String getDominantSpecies() { return dominantSpecies; }
    public void setDominantSpecies(String dominantSpecies) { this.dominantSpecies = dominantSpecies; }

    public Boolean getInvasiveSpeciesPresent() { return invasiveSpeciesPresent; }
    public void setInvasiveSpeciesPresent(Boolean invasiveSpeciesPresent) { this.invasiveSpeciesPresent = invasiveSpeciesPresent; }

    public String getInvasiveSpeciesNames() { return invasiveSpeciesNames; }
    public void setInvasiveSpeciesNames(String invasiveSpeciesNames) { this.invasiveSpeciesNames = invasiveSpeciesNames; }

    public Double getDbhCm() { return dbhCm; }
    public void setDbhCm(Double dbhCm) { this.dbhCm = dbhCm; }

    public String getNdviObservation() { return ndviObservation; }
    public void setNdviObservation(String ndviObservation) { this.ndviObservation = ndviObservation; }

    public String getVegetationDisturbance() { return vegetationDisturbance; }
    public void setVegetationDisturbance(String vegetationDisturbance) { this.vegetationDisturbance = vegetationDisturbance; }
}