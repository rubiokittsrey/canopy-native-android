package app.practice.canopy_native_android.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import app.practice.canopy_native_android.database.entities.SoilEntity;

@Dao
public interface SoilDao {


    // -- insert, update

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(SoilEntity data);

    @Update
    void update(SoilEntity data);

    // -- queries

    @Query("SELECT * FROM soil_data WHERE survey_id = :surveyId")
    LiveData<SoilEntity> getBySurveyId(String surveyId);

    @Query("SELECT * FROM soil_data WHERE survey_id = :surveyId")
    SoilEntity getBySurveyIdSync(String surveyId);

    // -- delete

    @Query("DELETE FROM soil_data WHERE survey_id = :surveyId")
    void deleteBySurveyId(String surveyId);

    @Query("DELETE FROM soil_data")
    void deleteAll();
}