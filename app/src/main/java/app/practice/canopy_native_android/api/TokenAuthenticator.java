package app.practice.canopy_native_android.api;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;

import app.practice.canopy_native_android.models.dto.TokenRefreshRequest;
import app.practice.canopy_native_android.models.dto.TokenRefreshResponse;
import app.practice.canopy_native_android.utils.Constants;
import app.practice.canopy_native_android.utils.TokenManager;
import okhttp3.Authenticator;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Authenticator that handles 401 responses by refreshing acess token
 * If refresh fails, clear tokens (users will need to login again).
 * **/
public class TokenAuthenticator implements Authenticator {

    private final TokenManager tokenManager;
    private final Object lock = new Object();

    public TokenAuthenticator(TokenManager tokenManager) {
        this.tokenManager = tokenManager;
    }

    @Nullable
    @Override
    public Request authenticate(@Nullable Route route, @NonNull Response response) throws IOException {
        if (responseCount(response) >= Constants.AUTHENTICATE_MAX_ATTEMPTS) {
            return null;
        }

        synchronized (lock) {
            String refreshToken = tokenManager.getRefreshToken();

            // no refresh token
            if (refreshToken == null) {
                tokenManager.clearTokens();
                return null;
            }

            // attempt to refresh access token
            try {
                TokenRefreshResponse refreshResponse = refreshAccessToken(refreshToken);
                if (refreshResponse != null && refreshResponse.getAccessToken() != null) {
                    tokenManager.saveAccessToken(refreshResponse.getAccessToken());

                    return response.request().newBuilder()
                            .header("Authorization", "Bearer " + refreshResponse.getAccessToken())
                            .build();
                }
            } catch (Exception e) {
                // TODO: implement robust logging
                e.printStackTrace();
            }

            // refresh failed
            tokenManager.clearTokens();
            return null;
        }
    }

    // walks back through the response chain
    private int responseCount(Response response) {
        int count = 1;
        while ((response = response.priorResponse()) != null) {
            count++;
        }
        return count;
    }

    private TokenRefreshResponse refreshAccessToken(String refreshToken) throws IOException {
        // creates a separate Retrofit instance without the authenticator to avoid infinite loops
        OkHttpClient client = new OkHttpClient.Builder().build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        AuthApi authApi = retrofit.create(AuthApi.class);
        retrofit2.Response<TokenRefreshResponse> response = authApi
                .refreshToken(new TokenRefreshRequest(refreshToken))
                .execute();

        if (response.isSuccessful()) {
            return response.body();
        }
        return null;
    }

}
