package com.example.neaapp;


import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class dbHelper extends SQLiteOpenHelper {





////table 1 user settings
    public static final String DATABASE_NAME = "main.db";
    public static final String TABLE_NAME =  "userSettings";
    public static final String COL1 = "Name";
    public static final String COL2 = "StartTime";
    public static final String COL3 = "EndTime";
    public static final String COL4 = "Notifications";

/////// flight table database

    public static final String TABLE_NAME2 = "itinerary ";
    public static final String COL21 = "flightNu";
    public static final String COL22 =  "dep";
    public static final String COL23 = "arr";
    public static final String COL24 = "date";
    public static final String COL25 = "dTime";
    public static final String COL26 = "aTime";
    public static final String COL27 = "active";





    public dbHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, 1);
        SQLiteDatabase db = this.getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_NAME + " (Name TEXT, StartTime INTEGER, EndTime INTEGER, Notifications BOOLEAN)");
        db.execSQL("create table " + TABLE_NAME2 + "(flightNu TEXT, dep TEXT, arr TEXT, date TEXT, dTime TEXT, aTime TEXT, active BOOLEAN )");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);
            onCreate(db);
    }

    public boolean insertData(String name,String time1, String time2, boolean not){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL1, name);
        contentValues.put(COL2, time1);
        contentValues.put(COL3, time2);
        contentValues.put(COL4, not);
        long result = db.insert(TABLE_NAME,null,contentValues);
        if(result == -1){
            return false;
        }else{
            return true;
        }
    }

}
