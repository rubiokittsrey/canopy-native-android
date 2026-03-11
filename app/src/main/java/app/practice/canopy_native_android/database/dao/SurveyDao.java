package app.practice.canopy_native_android.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.RawQuery;
import androidx.room.Update;
import androidx.paging.PagingSource;
import androidx.sqlite.db.SupportSQLiteQuery;

import java.util.List;

import app.practice.canopy_native_android.database.entities.SurveyEntity;

@Dao
public interface SurveyDao {

    // -- insert

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(SurveyEntity survey);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<SurveyEntity> surveys);

    // -- update

    @Update
    void update(SurveyEntity survey);

    @Query("UPDATE survey_entries SET sync_status = :status WHERE survey_id = :surveyId")
    void updateSyncStatus(String surveyId, String status);

    @Query("UPDATE survey_entries SET sync_status = :status, updated_at = :updatedAt WHERE survey_id = :surveyId")
    void updateSyncStatusWithTimestamp(String surveyId, String status, String updatedAt);

    // -- delete

    @Query("DELETE FROM survey_entries WHERE survey_id = :surveyId")
    void deleteById(String surveyId);

    @Query("DELETE FROM survey_entries")
    void deleteAll();

    @Query("DELETE FROM survey_entries WHERE sync_status = :status")
    void deleteBySyncStatus(String status);

    // -- single queries

    @Query("SELECT * FROM survey_entries WHERE survey_id = :surveyId")
    LiveData<SurveyEntity> getSurveyById(String surveyId);

    @Query("SELECT * FROM survey_entries WHERE survey_id = :surveyId")
    SurveyEntity getSurveyByIdSync(String surveyId);

    // -- list queries
    // TODO: ORDERING

    @Query("SELECT * FROM survey_entries ORDER BY survey_date DESC, survey_time DESC")
    LiveData<List<SurveyEntity>> getAllSurveys();

    @Query("SELECT * FROM survey_entries ORDER BY survey_date DESC, survey_time DESC")
    List<SurveyEntity> getAllSurveysSync();

    @Query("SELECT * FROM survey_entries WHERE observer_id = :observerId ORDER BY survey_date DESC, survey_time DESC")
    LiveData<List<SurveyEntity>> getSurveysByObserver(String observerId);

    @Query("SELECT * FROM survey_entries WHERE sync_status = :status ORDER BY created_at ASC")
    List<SurveyEntity> getSurveysBySyncStatus(String status);

    @Query("SELECT * FROM survey_entries WHERE sync_status IN ('pending', 'failed') ORDER BY created_at ASC")
    List<SurveyEntity> getPendingSurveys();

    // -- paging

    @Query("SELECT * FROM survey_entries ORDER BY survey_date DESC, survey_time DESC")
    PagingSource<Integer, SurveyEntity> getSurveysPaged();

    @Query("SELECT * FROM survey_entries WHERE observer_id = :observerId ORDER BY survey_date DESC, survey_time DESC")
    PagingSource<Integer, SurveyEntity> getSurveysByObserverPaged(String observerId);

    // -- filtered paging

    // dynamic query for filtering
    @RawQuery(observedEntities = SurveyEntity.class)
    PagingSource<Integer, SurveyEntity> getSurveysFilteredPaged(SupportSQLiteQuery query);

    @Query("SELECT * FROM survey_entries WHERE " +
            "(observer_name LIKE '%' || :searchQuery || '%' OR " +
            "organization LIKE '%' || :searchQuery || '%' OR " +
            "survey_type LIKE '%' || :searchQuery || '%' OR " +
            "notes LIKE '%' || :searchQuery || '%') " +
            "ORDER BY survey_date DESC, survey_time DESC")
    PagingSource<Integer, SurveyEntity> searchSurveysPaged(String searchQuery);

    // -- counts

    @Query("SELECT COUNT(*) FROM survey_entries")
    LiveData<Integer> getTotalCount();

    @Query("SELECT COUNT(*) FROM survey_entries WHERE sync_status = :status")
    LiveData<Integer> getCountBySyncStatus(String status);

    @Query("SELECT COUNT(*) FROM survey_entries WHERE sync_status = 'pending'")
    int getPendingCountSync();

    // -- distinct values (for filters)

    @Query("SELECT DISTINCT survey_type FROM survey_entries WHERE survey_type IS NOT NULL ORDER BY survey_type")
    LiveData<List<String>> getDistinctSurveyTypes();

    @Query("SELECT DISTINCT weather_condition FROM survey_entries ORDER BY weather_condition")
    LiveData<List<String>> getDistinctWeatherConditions();

    @Query("SELECT DISTINCT observer_name FROM survey_entries ORDER BY observer_name")
    LiveData<List<String>> getDistinctObserverNames();

}