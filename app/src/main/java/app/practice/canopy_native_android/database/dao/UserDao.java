package app.practice.canopy_native_android.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import app.practice.canopy_native_android.database.entities.UserEntity;

@Dao
public interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(UserEntity user);

    @Update
    void update(UserEntity user);

    // -- queries

    @Query("SELECT * FROM users WHERE user_id = :userId")
    LiveData<UserEntity> getUserById(String userId);

    @Query("SELECT * FROM users WHERE user_id = :userId")
    UserEntity getUserByIdSync(String userId);

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    UserEntity getUserByEmail(String email);

    // -- delete

    @Query("DELETE FROM users WHERE user_id = :userId")
    void deleteById(String userId);

    @Query("DELETE FROM users")
    void deleteAll();

}