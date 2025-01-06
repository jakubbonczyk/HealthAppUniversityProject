package com.example.pamietajozdrowiu;

public class HealthResult {
    private String type;
    private String value;
    private String date;

    public HealthResult(String type, String value, String date) {
        this.type = type;
        this.value = value;
        this.date = date;
    }

    public String getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public String getDate() {
        return date;
    }
}
