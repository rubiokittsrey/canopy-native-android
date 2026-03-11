package app.practice.canopy_native_android.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import app.practice.canopy_native_android.database.entities.BiodiversityEntity;

@Dao
public interface BiodiversityDao {

    // -- insert, update

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(BiodiversityEntity data);

    @Update
    void update(BiodiversityEntity data);

    // -- queries

    @Query("SELECT * FROM biodiversity_data WHERE survey_id = :surveyId")
    LiveData<BiodiversityEntity> getBySurveyId(String surveyId);

    @Query("SELECT * FROM biodiversity_data WHERE survey_id = :surveyId")
    BiodiversityEntity getBySurveyIdSync(String surveyId);

    // -- delete

    @Query("DELETE FROM biodiversity_data WHERE survey_id = :surveyId")
    void deleteBySurveyId(String surveyId);

    @Query("DELETE FROM biodiversity_data")
    void deleteAll();
}