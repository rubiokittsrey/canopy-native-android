package app.practice.canopy_native_android.repositories;

import android.content.Context;
import android.os.Looper;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import app.practice.canopy_native_android.api.ApiClient;
import app.practice.canopy_native_android.api.AuthApi;
import app.practice.canopy_native_android.database.AppDatabase;
import app.practice.canopy_native_android.database.dao.UserDao;
import app.practice.canopy_native_android.database.entities.UserEntity;
import app.practice.canopy_native_android.models.dto.AuthResponse;
import app.practice.canopy_native_android.models.dto.ErrorResponse;
import app.practice.canopy_native_android.models.dto.LoginRequest;
import app.practice.canopy_native_android.models.dto.RegisterRequest;
import app.practice.canopy_native_android.models.dto.UserResponse;
import app.practice.canopy_native_android.utils.Resource;
import app.practice.canopy_native_android.utils.TokenManager;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/// Handles login, registration, profile management, and token storage
public class AuthRepository {

    private final AuthApi authApi;
    private final UserDao userDao;
    private final TokenManager tokenManager;
    private final ExecutorService executor;
    private final Handler mainHandler;

    public AuthRepository(Context context) {
        this.authApi = ApiClient.getInstance(context).getAuthApi();
        this.userDao = AppDatabase.getInstance(context).userDao();
        this.tokenManager = TokenManager.getInstance(context);
        this.executor = Executors.newSingleThreadExecutor();
        this.mainHandler = new android.os.Handler(Looper.getMainLooper());
    }

    public LiveData<Resource<UserEntity>> login(String email, String password) {
        MutableLiveData<Resource<UserEntity>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        LoginRequest request = new LoginRequest(email, password);

        authApi.login(request).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(@NonNull Call<AuthResponse> call, @NonNull Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authResponse = response.body();

                    tokenManager.saveTokens(
                            authResponse.getAccessToken(),
                            authResponse.getRefreshToken()
                    );

                    if (authResponse.getUser() != null) {
                        UserEntity userEntity = authResponse.getUser().toEntity();
                        tokenManager.saveUserId(userEntity.getUserId());

                        executor.execute(() -> {
                            userDao.insert(userEntity);
                            mainHandler.post(() -> result.setValue(Resource.success(userEntity)));
                        });
                    } else {
                        result.setValue(Resource.success(null));
                    }
                } else {
                    String errorMessage = parseError(response);
                    result.setValue(Resource.error(errorMessage));
                }
            }

            @Override
            public void onFailure(@NonNull Call<AuthResponse> call, @NonNull Throwable t) {
                result.setValue(Resource.error("Network error: " + t.getMessage()));
            }
        });

        return result;
    }

    public LiveData<Resource<UserEntity>> register(
            String email, String password, String fullName, String organization, String phoneNumber) {

        MutableLiveData<Resource<UserEntity>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        RegisterRequest request = new RegisterRequest(email, password, fullName)
                .withOrganization(organization)
                .withPhoneNumber(phoneNumber);

        authApi.register(request).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(@NonNull Call<AuthResponse> call, @NonNull Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authResponse = response.body();

                    tokenManager.saveTokens(
                            authResponse.getAccessToken(),
                            authResponse.getRefreshToken()
                    );

                    // save user to db
                    if (authResponse.getUser() != null) {
                        UserEntity userEntity = authResponse.getUser().toEntity();
                        tokenManager.saveUserId(userEntity.getUserId());

                        executor.execute(() -> {
                            userDao.insert(userEntity);
                            mainHandler.post(() -> result.setValue(Resource.success(userEntity)));
                        });
                    } else {
                        result.setValue(Resource.success(null));
                    }
                } else {
                    String errorMessage = parseError(response);
                    result.setValue(Resource.error(errorMessage));
                }
            }

            @Override
            public void onFailure(@NonNull Call<AuthResponse> call, @NonNull Throwable t) {
                result.setValue(Resource.error("Network error: " + t.getMessage()));
            }
        });

        return result;
    }

    public LiveData<Resource<UserEntity>> fetchProfile() {
        MutableLiveData<Resource<UserEntity>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        authApi.getProfile().enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(@NonNull Call<UserResponse> call, @NonNull Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserEntity userEntity = response.body().toEntity();

                    executor.execute(() -> {
                        userDao.insert(userEntity);
                        mainHandler.post(() -> result.setValue(Resource.success(userEntity)));
                    });
                } else {
                    String errorMessage = parseError(response);
                    result.setValue(Resource.error(errorMessage));
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserResponse> call, @NonNull Throwable t) {
                result.setValue(Resource.error("Network error: " + t.getMessage()));
            }
        });

        return result;
    }

    public LiveData<UserEntity> getCachedUser() {
        String userId = tokenManager.getUserId();
        if (userId != null) {
            return userDao.getUserById(userId);
        }
        return new MutableLiveData<>(null);
    }

    // get cached user (synchronously, for non-ui threads)
    public UserEntity getCachedUserSync() {
        String userId = tokenManager.getUserId();
        if (userId != null) {
            return userDao.getUserByIdSync(userId);
        }
        return null;
    }

    public boolean isLoggedIn() {
        return tokenManager.isLoggedIn();
    }

    public String getCurrentUserId() {
        return tokenManager.getUserId();
    }

    public void logout(boolean clearLocalData) {
        tokenManager.clearTokens();

        if (clearLocalData) {
            executor.execute(userDao::deleteAll);
        }
    }

    private String parseError(Response<?> response) {
        try (ResponseBody errorBody = response.errorBody()) {
            if (errorBody != null) {
                String errorJson = errorBody.string();
                ErrorResponse errorResponse = new com.google.gson.Gson()
                        .fromJson(errorJson, ErrorResponse.class);

                if (errorResponse != null) {
                    return errorResponse.getDisplayMessage();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        switch (response.code()) {
            case 400:
                return "Invalid request. Please check your input.";
            case 401:
                return "Invalid credentials.";
            case 403:
                return "Access denied.";
            case 404:
                return "Resource not found.";
            case 500:
                return "Server error. Please try again later.";
            default:
                return "Error: " + response.code();
        }

    }

}
