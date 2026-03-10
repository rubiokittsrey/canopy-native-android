package app.practice.canopy_native_android.database.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "infrastructure_data",
        foreignKeys = @ForeignKey(
                entity = SurveyEntity.class,
                parentColumns = "survey_id",
                childColumns = "survey_id",
                onDelete = ForeignKey.CASCADE
        ),
        indices = @Index(value = "survey_id")
)
public class InfrastructureEntity {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "survey_id")
    private String surveyId;

    @ColumnInfo(name = "roads_or_tracks_present")
    private Boolean roadsOrTracksPresent;

    @ColumnInfo(name = "road_condition")
    private String roadCondition;

    @ColumnInfo(name = "structures_present")
    private Boolean structuresPresent;

    @ColumnInfo(name = "utilities_present")
    private Boolean utilitiesPresent;

    @ColumnInfo(name = "land_disturbance_history")
    private String landDisturbanceHistory;

    @ColumnInfo(name = "illegal_dumping_present")
    private Boolean illegalDumpingPresent;

    // JSON array string: [{"latitude": 8.45, "longitude": 124.63}, ...]
    @ColumnInfo(name = "pollution_points")
    private String pollutionPoints;

    public InfrastructureEntity(@NonNull String surveyId) {
        this.surveyId = surveyId;
    }

    @NonNull
    public String getSurveyId() { return surveyId; }
    public void setSurveyId(@NonNull String surveyId) { this.surveyId = surveyId; }

    public Boolean getRoadsOrTracksPresent() { return roadsOrTracksPresent; }
    public void setRoadsOrTracksPresent(Boolean roadsOrTracksPresent) { this.roadsOrTracksPresent = roadsOrTracksPresent; }

    public String getRoadCondition() { return roadCondition; }
    public void setRoadCondition(String roadCondition) { this.roadCondition = roadCondition; }

    public Boolean getStructuresPresent() { return structuresPresent; }
    public void setStructuresPresent(Boolean structuresPresent) { this.structuresPresent = structuresPresent; }

    public Boolean getUtilitiesPresent() { return utilitiesPresent; }
    public void setUtilitiesPresent(Boolean utilitiesPresent) { this.utilitiesPresent = utilitiesPresent; }

    public String getLandDisturbanceHistory() { return landDisturbanceHistory; }
    public void setLandDisturbanceHistory(String landDisturbanceHistory) { this.landDisturbanceHistory = landDisturbanceHistory; }

    public Boolean getIllegalDumpingPresent() { return illegalDumpingPresent; }
    public void setIllegalDumpingPresent(Boolean illegalDumpingPresent) { this.illegalDumpingPresent = illegalDumpingPresent; }

    public String getPollutionPoints() { return pollutionPoints; }
    public void setPollutionPoints(String pollutionPoints) { this.pollutionPoints = pollutionPoints; }
}