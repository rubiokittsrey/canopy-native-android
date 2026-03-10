package app.practice.canopy_native_android.models.enums;

public enum WeatherCondition {
    SUNNY("Sunny"),
    CLOUDY("Cloudy"),
    RAINY("Rainy"),
    WINDY("Windy"),
    FOGGY("Foggy");

    private final String value;

    WeatherCondition(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static WeatherCondition fromValue(String value) {
        for (WeatherCondition condition : values()) {
            if (condition.value.equalsIgnoreCase(value)) {
                return condition;
            }
        }
        return SUNNY;
    }

    public static String[] getDisplayValues() {
        WeatherCondition[] conditions = values();
        String[] displayValues = new String[conditions.length];
        for (int i = 0; i < conditions.length; i++) {
            displayValues[i] = conditions[i].value;
        }
        return displayValues;
    }
}