package app.practice.canopy_native_android.api;

import app.practice.canopy_native_android.models.dto.AuthResponse;
import app.practice.canopy_native_android.models.dto.LoginRequest;
import app.practice.canopy_native_android.models.dto.RegisterRequest;
import app.practice.canopy_native_android.models.dto.TokenRefreshRequest;
import app.practice.canopy_native_android.models.dto.TokenRefreshResponse;
import app.practice.canopy_native_android.models.dto.UserResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;

public interface AuthApi {

    @POST("api/auth/login/")
    Call<AuthResponse> login(@Body LoginRequest request);

    @POST("api/auth/register/")
    Call<AuthResponse> register(@Body RegisterRequest request);

    @POST("api/auth/token/refresh/")
    Call<TokenRefreshResponse> refreshToken(@Body TokenRefreshRequest request);

    @GET("api/auth/me/")
    Call<UserResponse> getProfile();

    @PUT("api/auth/me/")
    Call<UserResponse> updateProfile(@Body UserResponse request);

}
