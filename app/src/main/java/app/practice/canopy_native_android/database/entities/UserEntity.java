package app.practice.canopy_native_android.database.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class UserEntity {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "user_id")
    private String userId;

    @NonNull
    private String email;


    @NonNull
    @ColumnInfo(name = "full_name")
    private String fullName;

    @NonNull
    @ColumnInfo(name = "phone_number")
    private String phoneNumber;

    private String organization;

    @ColumnInfo(name = "profile_photo")
    private String profilePhoto;

    private String bio;

    private String location;

    @ColumnInfo(name = "is_active")
    private boolean isActive;

    @ColumnInfo(name = "date_joined")
    private String dateJoined;

    @ColumnInfo(name = "updated_at")
    private String updatedAt;

    public UserEntity(@NonNull String userId, @NonNull String email, @NonNull String fullName) {
        this.userId = userId;
        this.email = email;
        this.fullName = fullName;
        this.isActive = true;
    }

    @NonNull
    public String getUserId() { return userId; }
    public void setUserId(@NonNull String userId) { this.userId = userId; }

    @NonNull
    public String getEmail() { return email; }
    public void setEmail(@NonNull String email) { this.email = email; }

    @NonNull
    public String getFullName() { return fullName; }
    public void setFullName(@NonNull String fullName) { this.fullName = fullName; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getOrganization() { return organization; }
    public void setOrganization(String organization) { this.organization = organization; }

    public String getProfilePhoto() { return profilePhoto; }
    public void setProfilePhoto(String profilePhoto) { this.profilePhoto = profilePhoto; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public String getDateJoined() { return dateJoined; }
    public void setDateJoined(String dateJoined) { this.dateJoined = dateJoined; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}
