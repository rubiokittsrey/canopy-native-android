package app.practice.canopy_native_android.database.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "hazard_data",
        foreignKeys = @ForeignKey(
                entity = SurveyEntity.class,
                parentColumns = "survey_id",
                childColumns = "survey_id",
                onDelete = ForeignKey.CASCADE
        ),
        indices = @Index(value = "survey_id")
)
public class HazardEntity {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "survey_id")
    private String surveyId;

    @ColumnInfo(name = "landslide_risk")
    private String landslideRisk;

    @ColumnInfo(name = "erosion_risk")
    private String erosionRisk;

    @ColumnInfo(name = "flood_risk")
    private String floodRisk;

    @ColumnInfo(name = "contamination_signs")
    private Boolean contaminationSigns;

    @ColumnInfo(name = "contamination_type")
    private String contaminationType;

    @ColumnInfo(name = "proximity_to_industrial_m")
    private Double proximityToIndustrialM;

    public HazardEntity(@NonNull String surveyId) {
        this.surveyId = surveyId;
    }

    @NonNull
    public String getSurveyId() { return surveyId; }
    public void setSurveyId(@NonNull String surveyId) { this.surveyId = surveyId; }

    public String getLandslideRisk() { return landslideRisk; }
    public void setLandslideRisk(String landslideRisk) { this.landslideRisk = landslideRisk; }

    public String getErosionRisk() { return erosionRisk; }
    public void setErosionRisk(String erosionRisk) { this.erosionRisk = erosionRisk; }

    public String getFloodRisk() { return floodRisk; }
    public void setFloodRisk(String floodRisk) { this.floodRisk = floodRisk; }

    public Boolean getContaminationSigns() { return contaminationSigns; }
    public void setContaminationSigns(Boolean contaminationSigns) { this.contaminationSigns = contaminationSigns; }

    public String getContaminationType() { return contaminationType; }
    public void setContaminationType(String contaminationType) { this.contaminationType = contaminationType; }

    public Double getProximityToIndustrialM() { return proximityToIndustrialM; }
    public void setProximityToIndustrialM(Double proximityToIndustrialM) { this.proximityToIndustrialM = proximityToIndustrialM; }
}