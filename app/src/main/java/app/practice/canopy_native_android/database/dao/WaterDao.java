package app.practice.canopy_native_android.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import app.practice.canopy_native_android.database.entities.WaterEntity;

@Dao
public interface WaterDao {

    // -- insert, update

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(WaterEntity data);

    @Update
    void update(WaterEntity data);

    // -- queries

    @Query("SELECT * FROM water_data WHERE survey_id = :surveyId")
    LiveData<WaterEntity> getBySurveyId(String surveyId);

    @Query("SELECT * FROM water_data WHERE survey_id = :surveyId")
    WaterEntity getBySurveyIdSync(String surveyId);

    // -- delete

    @Query("DELETE FROM water_data WHERE survey_id = :surveyId")
    void deleteBySurveyId(String surveyId);

    @Query("DELETE FROM water_data")
    void deleteAll();
}