package app.practice.canopy_native_android.utils;

public final class Constants {
    private Constants() {}

    public static final String BASE_URL = "https://10.0.2.2:8000"; // emulator localhost

    // shared prefs
    public static final String PREFS_NAME = "canopy_prefs";
    public static final String KEY_ACCESS_TOKEN = "access_token";
    public static final String KEY_REFRESH_TOKEN = "refresh_token";
    public static final String KEY_USER_ID = "user_id";

    // pagination
    public static final int PAGE_SIZE = 20;

    // sync status
    public static final String SYNC_PENDING = "pending";
    public static final String SYNC_SYNCED = "synced";
    public static final String SYNC_FAILED = "failed";
}
