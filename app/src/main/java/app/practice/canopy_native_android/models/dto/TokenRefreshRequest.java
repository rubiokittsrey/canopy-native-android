package app.practice.canopy_native_android.models.dto;

import com.google.gson.annotations.SerializedName;

public class TokenRefreshRequest {

    @SerializedName("refresh")
    private String refreshToken;

    public TokenRefreshRequest(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getRefreshToken() { return refreshToken; }

}