package com.example.pamietajozdrowiu;

public class Drug {
    private int id;
    private String name;
    private int pillsQuantity;
    private String expirationDate;

    public Drug(int id, String name, int pillsQuantity, String expirationDate) {
        this.id = id;
        this.name = name;
        this.pillsQuantity = pillsQuantity;
        this.expirationDate = expirationDate;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getPillsQuantity() {
        return pillsQuantity;
    }

    public String getExpirationDate() {
        return expirationDate;
    }
}
