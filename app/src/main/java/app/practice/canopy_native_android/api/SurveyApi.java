package app.practice.canopy_native_android.api;

import java.util.Map;

import app.practice.canopy_native_android.models.dto.SurveyListResponse;
import app.practice.canopy_native_android.models.dto.SurveyResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

public interface SurveyApi {

    // -- list

    @GET("api/surveys/")
    Call<SurveyListResponse> getSurveys(
            @Query("page") int page,
            @Query("page_size") int pageSize
    );

    @GET("api/surveys/")
    Call<SurveyListResponse> getSurveysFiltered(
            @Query("page") int page,
            @Query("page_size") int pageSize,
            @QueryMap Map<String, String> filters
    );

    @GET("api/surveys/mine/")
    Call<SurveyListResponse> getMySurveys(
            @Query("page") int page,
            @Query("page_size") int pageSize
    );

    // -- detail

    @GET("api/surveys/{survey_id}/")
    Call<SurveyResponse> getSurvey(@Path("survey_id") String surveyId);

    // -- create, update

    @POST("api/surveys/")
    Call<SurveyResponse> createSurvey(@Body Map<String, Object> survey);

    @PUT("api/surveys/{survey_id}/")
    Call<SurveyResponse> updateSurvey(
            @Path("survey_id") String surveyId,
            @Body Map<String, Object> survey
    );

    // -- delete

    @DELETE("api/surveys/{survey_id}/")
    Call<Void> deleteSurvey(@Path("survey_id") String surveyId);

    // -- sync (idempotent upsert)

    @PUT("api/surveys/sync/")
    Call<SurveyResponse> syncSurvey(@Body Map<String, Object> survey);

}
