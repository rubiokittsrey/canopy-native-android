package app.practice.canopy_native_android.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import app.practice.canopy_native_android.database.entities.TopographicEntity;

@Dao
public interface TopographicDao {

    // -- insert, update

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(TopographicEntity data);

    @Update
    void update(TopographicEntity data);

    // -- queries

    @Query("SELECT * FROM topographic_data WHERE survey_id = :surveyId")
    LiveData<TopographicEntity> getBySurveyId(String surveyId);

    @Query("SELECT * FROM topographic_data WHERE survey_id = :surveyId")
    TopographicEntity getBySurveyIdSync(String surveyId);

    // -- delete

    @Query("DELETE FROM topographic_data WHERE survey_id = :surveyId")
    void deleteBySurveyId(String surveyId);

    @Query("DELETE FROM topographic_data")
    void deleteAll();
}