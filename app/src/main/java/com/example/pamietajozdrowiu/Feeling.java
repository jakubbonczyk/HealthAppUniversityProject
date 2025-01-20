package com.example.pamietajozdrowiu;

public class Feeling {
    private String name;
    private String date;
    private String notes;

    public Feeling(String name, String date, String notes) {
        this.name = name;
        this.date = date;
        this.notes = notes;
    }

    public String getName() {
        return name;
    }

    public String getDate() {
        return date;
    }

    public String getNotes() {
        return notes;
    }
}
