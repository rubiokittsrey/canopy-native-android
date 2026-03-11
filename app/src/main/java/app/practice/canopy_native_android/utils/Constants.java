package app.practice.canopy_native_android.utils;

import okhttp3.logging.HttpLoggingInterceptor;

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

    // network configs

    // api client
    public static final HttpLoggingInterceptor.Level API_CLIENT_LOGGING_LEVEL
            = HttpLoggingInterceptor.Level.BODY;
    // timeout constants (in seconds)
    public static final Integer API_CONNECTION_TIMEOUT = 30;
    public static final Integer API_READ_TIMEOUT = 30;
    public static final Integer API_WRITE_TIMEOUT = 30;

    public static final Integer AUTHENTICATE_MAX_ATTEMPTS = 2;

}
