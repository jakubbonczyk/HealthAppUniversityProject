package com.example.pamietajozdrowiu;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "android_database.db";
    private static final int DATABASE_VERSION = 10; // Zwiększenie wersji bazy danych

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Tworzenie tabeli DRUGS z IMAGE jako BLOB
        String createDrugsTable = "CREATE TABLE DRUGS (" +
                "ID_DRUG INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "ID_USER INTEGER, " +
                "NAME TEXT NOT NULL, " +
                "PILLS_QUANTITY INTEGER NOT NULL, " +
                "IMAGE BLOB, " + // Kolumna IMAGE jako BLOB
                "EXPIRATION_DATE TEXT NOT NULL, " +
                "ID_SICKNESS INTEGER, " +
                "FOREIGN KEY(ID_SICKNESS) REFERENCES SICKNESSES(ID_SICKNESS), " +
                "FOREIGN KEY(ID_USER) REFERENCES USERS(ID_USER)" +
                ")";
        db.execSQL(createDrugsTable);

        // Tworzenie tabeli DRUGS_RAPORT_LOG
        String createDrugsRaportLogTable = "CREATE TABLE DRUGS_RAPORT_LOG (" +
                "ID_DRUG_RAPORT_LOG INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "ID_DRUG INTEGER NOT NULL, " +
                "ID_USER INTEGER NOT NULL, " +
                "QUANTITY_TAKEN INTEGER NOT NULL, " +
                "DATE_OF_TAKING TEXT NOT NULL, " +
                "FOREIGN KEY(ID_DRUG) REFERENCES DRUGS(ID_DRUG), " +
                "FOREIGN KEY(ID_USER) REFERENCES USERS(ID_USER)" +
                ")";
        db.execSQL(createDrugsRaportLogTable);

        // Tworzenie tabeli NOTIFICATION_SCHEDULE
        String createNotificationScheduleTable = "CREATE TABLE NOTIFICATION_SCHEDULE (" +
                "ID_NOTIFICATION_SCHEDULE INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "ID_DRUG INTEGER NOT NULL, " +
                "FREQUENCY INTEGER NOT NULL, " +
                "REMINDER_TIME TEXT, " +
                "START_DATE INTEGER, " +
                "END_DATE INTEGER, " +
                "IS_TAKEN INTEGER DEFAULT 0, " + // Dodane pole
                "FOREIGN KEY(ID_DRUG) REFERENCES DRUGS(ID_DRUG)" +
                ")";
        db.execSQL(createNotificationScheduleTable);

        // Tworzenie tabeli SICKNESSES
        String createSicknessesTable = "CREATE TABLE SICKNESSES (" +
                "ID_SICKNESS INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "NAME TEXT NOT NULL" +
                ")";
        db.execSQL(createSicknessesTable);

        // Tworzenie tabeli USERS
        String createUsersTable = "CREATE TABLE USERS (" +
                "ID_USER INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "FIRST_NAME TEXT NOT NULL, " +
                "SECOND_NAME TEXT NOT NULL, " +
                "AGE INTEGER NOT NULL, " +
                "DATE_OF_BIRTH TEXT NOT NULL" +
                ")";
        db.execSQL(createUsersTable);

        // Tworzenie tabeli USER_FEELINGS
        String createUserFeelingsTable = "CREATE TABLE USER_FEELINGS (" +
                "ID_USER_FEELINGS INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "NAME TEXT NOT NULL, " + // NAME powinno być typu TEXT, nie INTEGER
                "VALUE INTEGER NOT NULL, " +
                "ID_USER INTEGER NOT NULL, " +
                "FOREIGN KEY(ID_USER) REFERENCES USERS(ID_USER)" +
                ")";
        db.execSQL(createUserFeelingsTable);

        // Tworzenie tabeli USER_NOTIFICATIONS
        String createUserNotificationsTable = "CREATE TABLE USER_NOTIFICATIONS (" +
                "ID_USER_NOTIFICATION INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "ID_USER INTEGER NOT NULL, " +
                "ID_NOTIFICATION_SCHEDULE INTEGER NOT NULL, " +
                "NOTIFICATION_STATUS INTEGER, " +
                "LAST_NOTIFIED TEXT, " +
                "FOREIGN KEY(ID_NOTIFICATION_SCHEDULE) REFERENCES NOTIFICATION_SCHEDULE(ID_NOTIFICATION_SCHEDULE), " +
                "FOREIGN KEY(ID_USER) REFERENCES USERS(ID_USER)" +
                ")";
        db.execSQL(createUserNotificationsTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < newVersion) {
            db.execSQL("DROP TABLE IF EXISTS DRUGS_RAPORT_LOG");
            db.execSQL("DROP TABLE IF EXISTS NOTIFICATION_SCHEDULE");
            db.execSQL("DROP TABLE IF EXISTS DRUGS");
            db.execSQL("DROP TABLE IF EXISTS SICKNESSES");
            db.execSQL("DROP TABLE IF EXISTS USERS");
            db.execSQL("DROP TABLE IF EXISTS USER_FEELINGS");
            db.execSQL("DROP TABLE IF EXISTS USER_NOTIFICATIONS");
            onCreate(db);
        }
    }

}
