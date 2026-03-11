package app.practice.canopy_native_android.models.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/// Paginated response for survey list endpoint
public class SurveyListResponse {

    @SerializedName("count")
    private int count;

    @SerializedName("next")
    private String nextUrl;

    @SerializedName("previous")
    private String previousUrl;

    @SerializedName("results")
    private List<SurveyResponse> results;

    public int getCount() { return count; }
    public String getNextUrl() { return nextUrl; }
    public List<SurveyResponse> getResults() { return results; }

    public boolean hasNext() {
        return nextUrl != null && !nextUrl.isEmpty();
    }

    public boolean hasPrevious() {
        return previousUrl != null && !previousUrl.isEmpty();
    }

}