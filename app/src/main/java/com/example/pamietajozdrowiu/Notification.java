package com.example.pamietajozdrowiu;

public class Notification {
    private int id;
    private int drugId;
    private String reminderTime;
    private String date; // Data powiadomienia
    private boolean isTaken;

    public Notification(int id, int drugId, String date, String reminderTime, boolean isTaken) {
        this.id = id;
        this.drugId = drugId;
        this.date = date;
        this.reminderTime = reminderTime;
        this.isTaken = isTaken;
    }

    // Gettery
    public int getId() {
        return id;
    }

    public int getDrugId() {
        return drugId;
    }

    public String getDate() {
        return date;
    }

    public String getReminderTime() {
        return reminderTime;
    }

    public boolean isTaken() {
        return isTaken;
    }

    // Setter
    public void setTaken(boolean taken) {
        isTaken = taken;
    }
}
