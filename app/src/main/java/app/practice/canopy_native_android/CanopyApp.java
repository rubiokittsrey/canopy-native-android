package app.practice.canopy_native_android;

import android.app.Application;

import app.practice.canopy_native_android.api.ApiClient;
import app.practice.canopy_native_android.database.AppDatabase;
import app.practice.canopy_native_android.utils.TokenManager;

public class CanopyApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // init singletons
        TokenManager.getInstance(this);
        AppDatabase.getInstance(this);
        ApiClient.getInstance(this);
    }

}
