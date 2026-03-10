package app.practice.canopy_native_android.database.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Main survey entry (Metadata section).
 * This is the parent record - section tables reference this via survey_id.
 */
@Entity(
        tableName = "survey_entries",
        indices = {
                @Index(value = "observer_id"),
                @Index(value = "survey_date"),
                @Index(value = "sync_status")
        }
)
public class SurveyEntity {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "survey_id")
    private String surveyId;

    @NonNull
    @ColumnInfo(name = "observer_id")
    private String observerId;

    @NonNull
    @ColumnInfo(name = "observer_name")
    private String observerName;

    private String organization;

    @NonNull
    @ColumnInfo(name = "survey_date")
    private String surveyDate;

    @NonNull
    @ColumnInfo(name = "survey_time")
    private String surveyTime;

    @NonNull
    @ColumnInfo(name = "weather_condition")
    private String weatherCondition;

    @NonNull
    @ColumnInfo(name = "survey_method")
    private String surveyMethod;

    @ColumnInfo(name = "gps_latitude")
    private Double gpsLatitude;

    @ColumnInfo(name = "gps_longitude")
    private Double gpsLongitude;

    @ColumnInfo(name = "gps_accuracy_meters")
    private Float gpsAccuracyMeters;

    private String notes;

    // JSON string: ["path1", "path2", ...]
    private String photos;

    @ColumnInfo(name = "survey_type")
    private String surveyType;

    // JSON string for device info object
    @ColumnInfo(name = "device_info")
    private String deviceInfo;

    // app-side only field for offline sync
    @NonNull
    @ColumnInfo(name = "sync_status")
    private String syncStatus;

    @NonNull
    @ColumnInfo(name = "created_at")
    private String createdAt;

    @NonNull
    @ColumnInfo(name = "updated_at")
    private String updatedAt;

    public SurveyEntity(
            @NonNull String surveyId,
            @NonNull String observerId,
            @NonNull String observerName,
            @NonNull String surveyDate,
            @NonNull String surveyTime,
            @NonNull String weatherCondition,
            @NonNull String surveyMethod,
            @NonNull String syncStatus,
            @NonNull String createdAt,
            @NonNull String updatedAt
    ) {
        this.surveyId = surveyId;
        this.observerId = observerId;
        this.observerName = observerName;
        this.surveyDate = surveyDate;
        this.surveyTime = surveyTime;
        this.weatherCondition = weatherCondition;
        this.surveyMethod = surveyMethod;
        this.syncStatus = syncStatus;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    @NonNull
    public String getSurveyId() { return surveyId; }
    public void setSurveyId(@NonNull String surveyId) { this.surveyId = surveyId; }

    @NonNull
    public String getObserverId() { return observerId; }
    public void setObserverId(@NonNull String observerId) { this.observerId = observerId; }

    @NonNull
    public String getObserverName() { return observerName; }
    public void setObserverName(@NonNull String observerName) { this.observerName = observerName; }

    public String getOrganization() { return organization; }
    public void setOrganization(String organization) { this.organization = organization; }

    @NonNull
    public String getSurveyDate() { return surveyDate; }
    public void setSurveyDate(@NonNull String surveyDate) { this.surveyDate = surveyDate; }

    @NonNull
    public String getSurveyTime() { return surveyTime; }
    public void setSurveyTime(@NonNull String surveyTime) { this.surveyTime = surveyTime; }

    @NonNull
    public String getWeatherCondition() { return weatherCondition; }
    public void setWeatherCondition(@NonNull String weatherCondition) { this.weatherCondition = weatherCondition; }

    @NonNull
    public String getSurveyMethod() { return surveyMethod; }
    public void setSurveyMethod(@NonNull String surveyMethod) { this.surveyMethod = surveyMethod; }

    public Double getGpsLatitude() { return gpsLatitude; }
    public void setGpsLatitude(Double gpsLatitude) { this.gpsLatitude = gpsLatitude; }

    public Double getGpsLongitude() { return gpsLongitude; }
    public void setGpsLongitude(Double gpsLongitude) { this.gpsLongitude = gpsLongitude; }

    public Float getGpsAccuracyMeters() { return gpsAccuracyMeters; }
    public void setGpsAccuracyMeters(Float gpsAccuracyMeters) { this.gpsAccuracyMeters = gpsAccuracyMeters; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getPhotos() { return photos; }
    public void setPhotos(String photos) { this.photos = photos; }

    public String getSurveyType() { return surveyType; }
    public void setSurveyType(String surveyType) { this.surveyType = surveyType; }

    public String getDeviceInfo() { return deviceInfo; }
    public void setDeviceInfo(String deviceInfo) { this.deviceInfo = deviceInfo; }

    @NonNull
    public String getSyncStatus() { return syncStatus; }
    public void setSyncStatus(@NonNull String syncStatus) { this.syncStatus = syncStatus; }

    @NonNull
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(@NonNull String createdAt) { this.createdAt = createdAt; }

    @NonNull
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(@NonNull String updatedAt) { this.updatedAt = updatedAt; }
}
