package app.practice.canopy_native_android.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import app.practice.canopy_native_android.database.entities.VegetationEntity;

@Dao
public interface VegetationDao {

    // -- insert, update

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(VegetationEntity data);

    @Update
    void update(VegetationEntity data);

    // -- queries

    @Query("SELECT * FROM vegetation_data WHERE survey_id = :surveyId")
    LiveData<VegetationEntity> getBySurveyId(String surveyId);

    @Query("SELECT * FROM vegetation_data WHERE survey_id = :surveyId")
    VegetationEntity getBySurveyIdSync(String surveyId);

    // -- delete

    @Query("DELETE FROM vegetation_data WHERE survey_id = :surveyId")
    void deleteBySurveyId(String surveyId);

    @Query("DELETE FROM vegetation_data")
    void deleteAll();
}