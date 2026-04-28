package app.practice.canopy_native_android.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import app.practice.canopy_native_android.database.entities.RemoteKeyEntity;

@Dao
public interface RemoteKeyDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(RemoteKeyEntity key);

    @Query("SELECT * FROM remote_keys WHERE key_id = :keyId")
    RemoteKeyEntity getRemoteKey(String keyId);

    @Query("DELETE FROM remote_keys WHERE key_id = :keyId")
    void deleteByKey(String keyId);

    @Query("DELETE FROM remote_keys")
    void deleteAll();

}
