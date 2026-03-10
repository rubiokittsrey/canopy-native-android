package app.practice.canopy_native_android.models.enums;

/// `SurveyEntry` sync status with the API
public enum SyncStatus {
    PENDING("pending"),
    SYNCED("synced"),
    FAILED("failed");

    private final String value;

    SyncStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static SyncStatus fromValue(String value) {
        for (SyncStatus status : values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        return PENDING;
    }
}


