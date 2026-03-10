package app.practice.canopy_native_android.models.enums;

public enum SurveyMethod {
    WALKTHROUGH("Walkthrough"),
    TRANSECT("Transect"),
    PLOT_BASED("Plot-based"),
    REMOTE("Remote");

    private final String value;

    SurveyMethod(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static SurveyMethod fromValue(String value) {
        for (SurveyMethod method : values()) {
            if (method.value.equalsIgnoreCase(value)) {
                return method;
            }
        }
        return WALKTHROUGH;
    }

    public static String[] getDisplayValues() {
        SurveyMethod[] methods = values();
        String[] displayValues = new String[methods.length];
        for (int i = 0; i < methods.length; i++) {
            displayValues[i] = methods[i].value;
        }
        return displayValues;
    }
}