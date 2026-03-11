package app.practice.canopy_native_android.api;

import androidx.annotation.NonNull;

import java.io.IOException;

import app.practice.canopy_native_android.utils.TokenManager;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Interceptor that adds JWT access token to all requests.
 * Skips auth endpoints that don't require auth (api/surveys/, api/surveys/{survey_id}) & api/auth/token/refresh
 * **/
public class TokenInterceptor implements Interceptor {

    private final TokenManager tokenManager;

    public TokenInterceptor(TokenManager tokenManager) {
        this.tokenManager = tokenManager;
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Interceptor.Chain chain) throws IOException {
        Request originalRequest = chain.request();
        String path = originalRequest.url().encodedPath();

        // skip auth for public endpoints
        if (isPublicEndpoint(path)) {
            return chain.proceed(originalRequest);
        }

        // intercept with token if available
        String accessToken = tokenManager.getAccessToken();
        if (accessToken != null) {
            Request authenicatedRequest = originalRequest.newBuilder()
                    .header("Authorization", "Bearer " + accessToken)
                    .build();
            return chain.proceed(authenicatedRequest);
        }

        return chain.proceed(originalRequest);

    }

    private boolean isPublicEndpoint(String path) {
        return path.contains("/auth/login") ||
               path.contains("/auth/register") ||
               path.contains("/auth/token/refresh");
    }

}
