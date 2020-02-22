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
    public static final String TABLE_NAME =  "user";
    public static final String COL1 = "Name";


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

//// Table 3 for holding information of the active flight (gate, times etc)

    public static final String TABLE_NAME3 = "activeFlight";
    public static final String COL31 = "flightNum";
    public static final String COL32 = "estDepTime";
    public static final String COL33 = "estArrTime";
    public static final String COL34 = "gate";
    public static final String COL35 = "atAirport";


///Table 4 for saving past flights

    public static final String TABLE_NAME4 = "logbook";
    public static final String COL41 = "flightNum";
    public static final String COL42 = "dep";
    public static final String COL43 = "arr";
    public static final String COL44 = "latlong";
    public static final String COL45 = "flightTime";
    public static final String COL46 = "delay";




    public dbHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, 1);
        SQLiteDatabase db = this.getWritableDatabase();
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_NAME + " (Name TEXT)");
        db.execSQL("create table " + TABLE_NAME2 + "(flightNum TEXT, dep TEXT, arr TEXT, date TEXT, dTime TEXT, aTime TEXT, active INTEGER,terminal TEXT,latlong TEXT,dtimeOffset TEXT,atimeOffset TEXT)");
        db.execSQL("create table " +TABLE_NAME3 + "(flightNum TEXT, estDepTime TEXT, estArrTime TEXT,gate TEXT,atAirport TEXT)");
        db.execSQL("create table " + TABLE_NAME4 + "(flightNum TEXT, dep TEXT, arr TEXT, latlong TEXT, flightTime TEXT, delay TEXT )");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);
            onCreate(db);
    }

    public boolean insertData(String name){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL1, name);
        long result = db.insert(TABLE_NAME,null,contentValues);
        db.close();
        if(result == -1){
            return false;
        }else{
            return true;
        }

    }

    public void saveLogbook(String fnum,String flightTime, String delay){
        SQLiteDatabase db = this.getWritableDatabase();
        String selection = (COL41 + " LIKE ?");
        String[]args = {fnum};
        ContentValues data = new ContentValues();
        data.put(COL45,flightTime);
        data.put(COL46,delay);
        db.update(TABLE_NAME4,data,selection,args);
        db.close();

    }
    public Cursor lookupLogbook(String fnum){
        SQLiteDatabase db = this.getReadableDatabase();
        String[] args = {fnum};
        Cursor results = db.rawQuery("Select * from logbook Where flightNum=?",args);
        return results;
    }
    public void finished(String fnum){
        SQLiteDatabase db = this.getWritableDatabase();
        String[] args = {fnum};
        ContentValues data = new ContentValues();
        data.put(COL24,"finished");
        String selection = (COL24 + " LIKE ?");
        db.update(TABLE_NAME2,data,selection,args);
        db.close();
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
        db.close();
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
        db.close();

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
        db.close();

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
        db.close();
        if (activeCheck ==1){
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
            db.close();
            return activeFlight;

        }else{
            db.close();
            return activeFlight;
        }

    }


    public void timetableData(String fnum,String estTime,String gate,String Terminal,String estATime,String schTime,String atAirport){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues data = new ContentValues();

        data.put(COL31,fnum);
        data.put(COL32,estTime);
        data.put(COL33,estATime);
        data.put(COL34,gate);
        data.put(COL35,atAirport);
        db.insert(TABLE_NAME3,null,data);


        if(Terminal!=null){
            saveInfo(Terminal,fnum,COL28);
            saveInfo(schTime,fnum,COL25);
        }
        db.close();

    }
    public void updateTimeTable(String fnum,String estTime,String gate,String Terminal,String estATime,String schTime,String atAirport){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues data = new ContentValues();
        data.put(COL31,fnum);
        data.put(COL32,estTime);
        data.put(COL33,estATime);
        data.put(COL34,gate);
        data.put(COL35,atAirport);

        String selection = (COL31 + " LIKE ?");
        String[] args = {fnum};
        db.update(TABLE_NAME3,data,selection,args);
        db.close();
    }

    public void atAirport(String fnum,String atAiport){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues data = new ContentValues();

        data.put(COL35,atAiport);
        String selection = (COL31 + " LIKE ?");
        String[] args = {fnum};

        db.update(TABLE_NAME3,data,selection,args);
        db.close();

    }


    public Cursor activeInfo(String fnum){
        SQLiteDatabase db = this.getReadableDatabase();
        String[] args = {fnum};
        Cursor results = db.rawQuery("Select * from activeFlight Where flightNum =?",args);
        return results;
    }

    public void deleteActive(String fNum){
        SQLiteDatabase db = this.getWritableDatabase();
        String[] args = {fNum};
        db.delete(TABLE_NAME3,"flightNum=?",args);
        db.close();

    }

    public void logStart(String fNum) {
        SQLiteDatabase db = this.getWritableDatabase();
        String dep=null;
        String arr=null;
        String latlong=null;

        Cursor results = searchByNum(fNum);
        while(results.moveToNext()){
            int index;
            index = results.getColumnIndexOrThrow("dep");
            dep = results.getString(index);
            index = results.getColumnIndexOrThrow("arr");
            arr = results.getString(index);
            index = results.getColumnIndexOrThrow("latlong");
            latlong = results.getString(index);
        }
        ContentValues data = new ContentValues();
        data.put(COL41,fNum);
        data.put(COL42,dep);
        data.put(COL43,arr);
        data.put(COL44,latlong);
        data.put(COL45,"");
        data.put(COL46,"");

        db.insert(TABLE_NAME4,null,data);
        db.close();

    }
}
