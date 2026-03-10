package app.practice.canopy_native_android.database.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "water_data",
        foreignKeys = @ForeignKey(
                entity = SurveyEntity.class,
                parentColumns = "survey_id",
                childColumns = "survey_id",
                onDelete = ForeignKey.CASCADE
        ),
        indices = @Index(value = "survey_id")
)
public class WaterEntity {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "survey_id")
    private String surveyId;

    @ColumnInfo(name = "water_body_present")
    private Boolean waterBodyPresent;

    @ColumnInfo(name = "water_body_type")
    private String waterBodyType;

    @ColumnInfo(name = "water_clarity")
    private String waterClarity;

    @ColumnInfo(name = "flooding_signs")
    private Boolean floodingSigns;

    @ColumnInfo(name = "waterlogging_level")
    private String waterloggingLevel;

    @ColumnInfo(name = "distance_to_drainage_m")
    private Double distanceToDrainageM;

    @ColumnInfo(name = "flow_direction")
    private String flowDirection;

    public WaterEntity(@NonNull String surveyId) {
        this.surveyId = surveyId;
    }

    @NonNull
    public String getSurveyId() { return surveyId; }
    public void setSurveyId(@NonNull String surveyId) { this.surveyId = surveyId; }

    public Boolean getWaterBodyPresent() { return waterBodyPresent; }
    public void setWaterBodyPresent(Boolean waterBodyPresent) { this.waterBodyPresent = waterBodyPresent; }

    public String getWaterBodyType() { return waterBodyType; }
    public void setWaterBodyType(String waterBodyType) { this.waterBodyType = waterBodyType; }

    public String getWaterClarity() { return waterClarity; }
    public void setWaterClarity(String waterClarity) { this.waterClarity = waterClarity; }

    public Boolean getFloodingSigns() { return floodingSigns; }
    public void setFloodingSigns(Boolean floodingSigns) { this.floodingSigns = floodingSigns; }

    public String getWaterloggingLevel() { return waterloggingLevel; }
    public void setWaterloggingLevel(String waterloggingLevel) { this.waterloggingLevel = waterloggingLevel; }

    public Double getDistanceToDrainageM() { return distanceToDrainageM; }
    public void setDistanceToDrainageM(Double distanceToDrainageM) { this.distanceToDrainageM = distanceToDrainageM; }

    public String getFlowDirection() { return flowDirection; }
    public void setFlowDirection(String flowDirection) { this.flowDirection = flowDirection; }
}