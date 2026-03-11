package app.practice.canopy_native_android.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import app.practice.canopy_native_android.database.entities.HazardEntity;

@Dao
public interface HazardDao {

    // -- insert, update

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(HazardEntity data);

    @Update
    void update(HazardEntity data);

    // -- queries

    @Query("SELECT * FROM hazard_data WHERE survey_id = :surveyId")
    LiveData<HazardEntity> getBySurveyId(String surveyId);

    @Query("SELECT * FROM hazard_data WHERE survey_id = :surveyId")
    HazardEntity getBySurveyIdSync(String surveyId);

    // -- delete

    @Query("DELETE FROM hazard_data WHERE survey_id = :surveyId")
    void deleteBySurveyId(String surveyId);

    @Query("DELETE FROM hazard_data")
    void deleteAll();

}