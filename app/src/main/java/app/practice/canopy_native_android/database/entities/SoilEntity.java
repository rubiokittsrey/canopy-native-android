package app.practice.canopy_native_android.database.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "soil_data",
        foreignKeys = @ForeignKey(
                entity = SurveyEntity.class,
                parentColumns = "survey_id",
                childColumns = "survey_id",
                onDelete = ForeignKey.CASCADE
        ),
        indices = @Index(value = "survey_id")
)
public class SoilEntity {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "survey_id")
    private String surveyId;

    @ColumnInfo(name = "soil_type")
    private String soilType;

    @ColumnInfo(name = "soil_texture")
    private String soilTexture;

    @ColumnInfo(name = "soil_color_munsell")
    private String soilColorMunsell;

    @ColumnInfo(name = "erosion_level")
    private String erosionLevel;

    @ColumnInfo(name = "erosion_type")
    private String erosionType;

    @ColumnInfo(name = "exposed_roots")
    private Boolean exposedRoots;

    @ColumnInfo(name = "compaction_signs")
    private Boolean compactionSigns;

    @ColumnInfo(name = "disturbance_signs")
    private Boolean disturbanceSigns;

    public SoilEntity(@NonNull String surveyId) {
        this.surveyId = surveyId;
    }

    @NonNull
    public String getSurveyId() { return surveyId; }
    public void setSurveyId(@NonNull String surveyId) { this.surveyId = surveyId; }

    public String getSoilType() { return soilType; }
    public void setSoilType(String soilType) { this.soilType = soilType; }

    public String getSoilTexture() { return soilTexture; }
    public void setSoilTexture(String soilTexture) { this.soilTexture = soilTexture; }

    public String getSoilColorMunsell() { return soilColorMunsell; }
    public void setSoilColorMunsell(String soilColorMunsell) { this.soilColorMunsell = soilColorMunsell; }

    public String getErosionLevel() { return erosionLevel; }
    public void setErosionLevel(String erosionLevel) { this.erosionLevel = erosionLevel; }

    public String getErosionType() { return erosionType; }
    public void setErosionType(String erosionType) { this.erosionType = erosionType; }

    public Boolean getExposedRoots() { return exposedRoots; }
    public void setExposedRoots(Boolean exposedRoots) { this.exposedRoots = exposedRoots; }

    public Boolean getCompactionSigns() { return compactionSigns; }
    public void setCompactionSigns(Boolean compactionSigns) { this.compactionSigns = compactionSigns; }

    public Boolean getDisturbanceSigns() { return disturbanceSigns; }
    public void setDisturbanceSigns(Boolean disturbanceSigns) { this.disturbanceSigns = disturbanceSigns; }
}