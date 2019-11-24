package com.example.neaapp;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class dbHelper extends SQLiteOpenHelper {





////table 1 user settings  will change to include weather settings, time will only be applicable if booking system is added
    public static final String DATABASE_NAME = "main.db";
    public static final String TABLE_NAME =  "userSettings";
    public static final String COL1 = "Name";
    public static final String COL2 = "StartTime";
    public static final String COL3 = "EndTime";
    public static final String COL4 = "Notifications";

/////// flight table database

    // Syntax for columns is COL <tablenumber><column number>

    public static final String TABLE_NAME2 = "itinerary ";
    public static final String COL21 = "flightNum";
    public static final String COL22 = "dep";
    public static final String COL23 = "arr";
    public static final String COL24 = "date";
    public static final String COL25 = "dTime";
    public static final String COL26 = "aTime";
    public static final String COL27 = "active";
    public static final String COL28 = "terminal";
    public static final String COL29 = "latlong";
    public static final String COL210 = "dtimeOffset";
    public static final String COL211 = "atimeOffset";




    public dbHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, 1);
        SQLiteDatabase db = this.getWritableDatabase();
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_NAME + " (Name TEXT, StartTime INTEGER, EndTime INTEGER, Notifications BOOLEAN)");
        db.execSQL("create table " + TABLE_NAME2 + "(flightNum TEXT, dep TEXT, arr TEXT, date TEXT, dTime TEXT, aTime TEXT, active INTEGER,terminal TEXT,latlong TEXT,dtimeOffset TEXT,atimeOffset TEXT)");
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


    ////saving new flight

    public boolean insertFlight(String fNum, String dep, String arr,String Dte,String dTime, String aTime, Integer active, String terminal){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL21, fNum);
        contentValues.put(COL22, dep);
        contentValues.put(COL23, arr);
        contentValues.put(COL24, Dte);
        contentValues.put(COL25, dTime);
        contentValues.put(COL26, aTime);
        contentValues.put(COL27, active);
        contentValues.put(COL28, terminal);
        long result = db.insert(TABLE_NAME2,null,contentValues);
        if (result ==-1){
            return false;
        }else{
            return true;
        }
    }

    //// Fetching flights

    public Cursor flightGetter(){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor results = db.rawQuery("Select flightNum, date from itinerary",null);

        return results;
    }

    public Cursor searchByNum(String num){
        SQLiteDatabase db = this.getReadableDatabase();
        String[] args = {num};
        Cursor results = db.rawQuery("Select * from itinerary Where flightNum =?",args);

        return results;
    }


    /// deleting entries

    public void deleteByNum(String num){
        SQLiteDatabase db = this.getWritableDatabase();
        String[] args = {num};
        db.delete("itinerary","flightNum=?",args);

    }

    // saving lat long
    public void saveInfo(String saveData,String fNum,String column){
        SQLiteDatabase db = this.getWritableDatabase();
        //update statements
        ContentValues data = new ContentValues();
        data.put(column,saveData);

        String selection = (COL21 + " LIKE ?");
        String[] args = {fNum};

        db.update(TABLE_NAME2,data,selection,args);

    }

    public Cursor searchLatLong(String fNum){
        SQLiteDatabase db = this.getReadableDatabase();
        String[] args = {fNum};
        Cursor results = db.rawQuery("Select latlong from itinerary Where flightNum=?",args);
        return results;
    }

    public boolean ActiveOnOff(String fNum){
        // purpose of this is to check if the flight is active, and then change it. If active, set un active etc
        SQLiteDatabase db = this.getWritableDatabase();
        String[] args = {fNum};
        Cursor result = db.rawQuery("Select active from itinerary Where flightNum=?",args);
        Integer activeCheck = 0;
        while(result.moveToNext()){
            int index;
            index = result.getColumnIndexOrThrow("active");
            activeCheck = result.getInt(index);
        }
        ContentValues data = new ContentValues();
        if(activeCheck==1){
            //active, so change to non active
            data.put(COL27,0);

        }else{
            //not active, change active
            data.put(COL27,1);
        }
        String selection = (COL21 + " LIKE ?");
        long check = db.update(TABLE_NAME2,data,selection,args);
        if (check ==-1){
            return false;
        }else{
            return true;
        }

    }

    public String checkAnyActive(){
        SQLiteDatabase db = this.getReadableDatabase();
        String[] args = {"1"};
        Cursor result = db.rawQuery("select * from itinerary where active=?",args);
        String activeFlight="";
        if (result!=null){
            while(result.moveToNext()){
                Integer index;
                index = result.getColumnIndexOrThrow("flightNum");
                activeFlight = result.getString(index); //insert counter for error check
            }
            return activeFlight;

        }else{
            return activeFlight;
        }

    }




}
