package app.practice.canopy_native_android.database.entities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "remote_keys")
public class RemoteKeyEntity {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "key_id")
    private String keyId;

    @Nullable
    @ColumnInfo(name = "next_page")
    private Integer nextPage;

    @Nullable
    @ColumnInfo(name = "current_page")
    private Integer currentPage;

    @ColumnInfo(name = "created_at")
    private long createdAt;

    public RemoteKeyEntity(@NonNull String keyId, @Nullable Integer nextPage, @Nullable Integer currentPage) {
        this.keyId = keyId;
        this.nextPage = nextPage;
        this.currentPage = currentPage;
        this.createdAt = System.currentTimeMillis();
    }

    @NonNull
    public String getKeyId() { return keyId; }
    public void setKeyId(@NonNull String keyId) { this.keyId = keyId; }

    @Nullable
    public Integer getNextPage() { return nextPage; }
    public void setNextPage(@Nullable Integer nextPage) { this.nextPage = nextPage; }

    @Nullable
    public Integer getCurrentPage() { return currentPage; }
    public void setCurrentPage(@Nullable Integer currentPage) { this.currentPage = currentPage; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

}
