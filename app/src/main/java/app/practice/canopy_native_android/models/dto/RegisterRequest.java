package app.practice.canopy_native_android.models.dto;

import com.google.gson.annotations.SerializedName;

public class RegisterRequest {

    @SerializedName("email")
    private String email;

    @SerializedName("password")
    private String password;

    @SerializedName("full_name")
    private String fullName;

    @SerializedName("organization")
    private String organization;

    @SerializedName("phone_number")
    private String phoneNumber;

    public RegisterRequest(String email, String password, String fullName) {
        this.email = email;
        this.password = password;
        this.fullName = fullName;
    }

    public RegisterRequest withOrganization(String organization) {
        this.organization = organization;
        return this;
    }

    public RegisterRequest withPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        return this;
    }

    // -- getters

    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getFullName() { return fullName; }
    public String getOrganization() { return organization; }
    public String getPhoneNumber() { return phoneNumber; }

}