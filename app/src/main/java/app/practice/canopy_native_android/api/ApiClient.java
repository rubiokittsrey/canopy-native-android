package app.practice.canopy_native_android.api;

import android.content.Context;

import java.util.concurrent.TimeUnit;

import app.practice.canopy_native_android.utils.Constants;
import app.practice.canopy_native_android.utils.TokenManager;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Single API client that provides Retrofit instances
 * Configures okHTTP with token interceptor, authenticator, and logging
 * **/
public class ApiClient {

    private static volatile ApiClient instance;

    private final Retrofit retrofit;
    private final AuthApi authApi;
    private final SurveyApi surveyApi;

    private ApiClient(Context context) {
        TokenManager tokenManager = TokenManager.getInstance(context);

        // logging
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(Constants.API_CLIENT_LOGGING_LEVEL);

        // adds authorization header
        TokenInterceptor tokenInterceptor = new TokenInterceptor(tokenManager);

        // handles 401 by refreshing token
        TokenAuthenticator tokenAuthenticator = new TokenAuthenticator(tokenManager);

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .addInterceptor(tokenInterceptor)
                .authenticator(tokenAuthenticator)
                .connectTimeout(Constants.API_CONNECTION_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(Constants.API_READ_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(Constants.API_WRITE_TIMEOUT, TimeUnit.SECONDS)
                .build();

        retrofit = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        authApi = retrofit.create(AuthApi.class);
        surveyApi = retrofit.create(SurveyApi.class);

    }

    public static ApiClient getInstance(Context context) {
        if (instance == null) {
            synchronized (ApiClient.class) {
                if (instance == null) {
                    instance = new ApiClient(context.getApplicationContext());
                }
            }
        }
        return  instance;
    }

    // instance getters

    public AuthApi getAuthApi() {
        return authApi;
    }

    public SurveyApi getSurveyApi() {
        return surveyApi;
    }

    public Retrofit getRetrofit() {
        return retrofit;
    }

}
