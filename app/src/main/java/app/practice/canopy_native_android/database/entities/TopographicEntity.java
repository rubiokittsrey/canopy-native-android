package app.practice.canopy_native_android.database.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "topographic_data",
        foreignKeys = @ForeignKey(
                entity = SurveyEntity.class,
                parentColumns = "survey_id",
                childColumns = "survey_id",
                onDelete = ForeignKey.CASCADE
        ),
        indices = @Index(value = "survey_id")
)
public class TopographicEntity {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "survey_id")
    private String surveyId;

    @ColumnInfo(name = "elevation_m")
    private Double elevationM;

    @ColumnInfo(name = "slope_gradient_deg")
    private Double slopeGradientDeg;

    @ColumnInfo(name = "slope_aspect")
    private String slopeAspect;

    @ColumnInfo(name = "landform_type")
    private String landformType;

    @ColumnInfo(name = "drainage_pattern")
    private String drainagePattern;

    @ColumnInfo(name = "land_use")
    private String landUse;

    @ColumnInfo(name = "land_cover_description")
    private String landCoverDescription;

    public TopographicEntity(@NonNull String surveyId) {
        this.surveyId = surveyId;
    }

    @NonNull
    public String getSurveyId() { return surveyId; }
    public void setSurveyId(@NonNull String surveyId) { this.surveyId = surveyId; }

    public Double getElevationM() { return elevationM; }
    public void setElevationM(Double elevationM) { this.elevationM = elevationM; }

    public Double getSlopeGradientDeg() { return slopeGradientDeg; }
    public void setSlopeGradientDeg(Double slopeGradientDeg) { this.slopeGradientDeg = slopeGradientDeg; }

    public String getSlopeAspect() { return slopeAspect; }
    public void setSlopeAspect(String slopeAspect) { this.slopeAspect = slopeAspect; }

    public String getLandformType() { return landformType; }
    public void setLandformType(String landformType) { this.landformType = landformType; }

    public String getDrainagePattern() { return drainagePattern; }
    public void setDrainagePattern(String drainagePattern) { this.drainagePattern = drainagePattern; }

    public String getLandUse() { return landUse; }
    public void setLandUse(String landUse) { this.landUse = landUse; }

    public String getLandCoverDescription() { return landCoverDescription; }
    public void setLandCoverDescription(String landCoverDescription) { this.landCoverDescription = landCoverDescription; }
}