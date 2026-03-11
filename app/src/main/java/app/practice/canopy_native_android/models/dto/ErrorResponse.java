package app.practice.canopy_native_android.models.dto;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

/**
 * Generic error response from API.
 * **/
public class ErrorResponse {

    @SerializedName("detail")
    private String detail;

    @SerializedName("message")
    private String message;

    @SerializedName("errors")
    private Map<String, List<String>> fieldErrors;

    public String getDetail() { return detail; }
    public String getMessage() { return message; }
    public Map<String, List<String>> getFieldErrors() { return fieldErrors; }

    // human-readable message
    public String getDisplayMessage() {
        if (detail != null && !detail.isEmpty()) {
            return detail;
        }
        if (message != null && !message.isEmpty()) {
            return message;
        }
        if (fieldErrors != null && !fieldErrors.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, List<String>> entry : fieldErrors.entrySet()) {
                sb.append(entry.getKey()).append(": ");
                sb.append(String.join(", ", entry.getValue()));
                sb.append("\n");
            }
            return sb.toString().trim();
        }
        return "An unknown error occurred";
    }

}