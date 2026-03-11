// app/src/main/java/app/practice/native_java_mvvm/models/dto/UserResponse.java

package app.practice.canopy_native_android.models.dto;

import com.google.gson.annotations.SerializedName;

import app.practice.canopy_native_android.database.entities.UserEntity;

public class UserResponse {

    @SerializedName("user_id")
    private String userId;

    @SerializedName("email")
    private String email;

    @SerializedName("full_name")
    private String fullName;

    @SerializedName("phone_number")
    private String phoneNumber;

    @SerializedName("organization")
    private String organization;

    @SerializedName("profile_photo")
    private String profilePhoto;

    @SerializedName("bio")
    private String bio;

    @SerializedName("location")
    private String location;

    @SerializedName("is_active")
    private boolean isActive;

    @SerializedName("date_joined")
    private String dateJoined;

    @SerializedName("updated_at")
    private String updatedAt;

    // convert to room entity
    public UserEntity toEntity() {
        UserEntity entity = new UserEntity(userId, email, fullName);
        entity.setPhoneNumber(phoneNumber);
        entity.setOrganization(organization);
        entity.setProfilePhoto(profilePhoto);
        entity.setBio(bio);
        entity.setLocation(location);
        entity.setActive(isActive);
        entity.setDateJoined(dateJoined);
        entity.setUpdatedAt(updatedAt);
        return entity;
    }

    public String getUserId() { return userId; }
    public String getEmail() { return email; }
    public String getFullName() { return fullName; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getOrganization() { return organization; }
    public String getProfilePhoto() { return profilePhoto; }
    public String getBio() { return bio; }
    public String getLocation() { return location; }
    public boolean isActive() { return isActive; }
    public String getDateJoined() { return dateJoined; }
    public String getUpdatedAt() { return updatedAt; }
}