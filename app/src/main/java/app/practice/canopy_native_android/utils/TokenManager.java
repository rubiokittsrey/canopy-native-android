package app.practice.canopy_native_android.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.io.IOException;
import java.security.GeneralSecurityException;


/// Manages JWT tokens using encrypted SharedPreferences.
public class TokenManager {
    private static volatile TokenManager instance;
    private final SharedPreferences prefs;

    private TokenManager(Context context) {
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            prefs = EncryptedSharedPreferences.create(
                    context,
                    Constants.PREFS_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to create encrypted preferences", e);
        }
    }


    // singleton pattern
    public static TokenManager getInstance(Context context) {
        if (instance == null) {
            synchronized (TokenManager.class) {
                if (instance == null) {
                    instance = new TokenManager(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    public void saveTokens(String accessToken, String refreshToken) {
        prefs.edit()
                .putString(Constants.KEY_ACCESS_TOKEN, accessToken)
                .putString(Constants.KEY_REFRESH_TOKEN, refreshToken)
                .apply();
    }

    public void saveAccessToken(String accessToken) {
        prefs.edit()
                .putString(Constants.KEY_ACCESS_TOKEN, accessToken)
                .apply();
    }

    public String getAccessToken() {
        return prefs.getString(Constants.KEY_ACCESS_TOKEN, null);
    }

    public String getRefreshToken() {
        return prefs.getString(Constants.KEY_REFRESH_TOKEN, null);
    }

    public void saveUserId(String userId) {
        prefs.edit()
                .putString(Constants.KEY_USER_ID, userId)
                .apply();
    }

    public String getUserId() {
        return prefs.getString(Constants.KEY_USER_ID, null);
    }

    public boolean isLoggedIn() {
        return getAccessToken() != null;
    }

    public void clearTokens() {
        prefs.edit()
                .remove(Constants.KEY_ACCESS_TOKEN)
                .remove(Constants.KEY_REFRESH_TOKEN)
                .remove(Constants.KEY_USER_ID)
                .apply();
    }
}
