package app.practice.canopy_native_android.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import app.practice.canopy_native_android.database.entities.InfrastructureEntity;

@Dao
public interface InfrastructureDao {

    // -- insert, update

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(InfrastructureEntity data);

    @Update
    void update(InfrastructureEntity data);

    // -- queries

    @Query("SELECT * FROM infrastructure_data WHERE survey_id = :surveyId")
    LiveData<InfrastructureEntity> getBySurveyId(String surveyId);

    @Query("SELECT * FROM infrastructure_data WHERE survey_id = :surveyId")
    InfrastructureEntity getBySurveyIdSync(String surveyId);

    // -- delete

    @Query("DELETE FROM infrastructure_data WHERE survey_id = :surveyId")
    void deleteBySurveyId(String surveyId);

    @Query("DELETE FROM infrastructure_data")
    void deleteAll();
}