package com.example.neaapp;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SQLiteDatabase mainDb =  openOrCreateDatabase("main.db",MODE_PRIVATE,null);
        Cursor cursor = mainDb.rawQuery("select DISTINCT tbl_name from sqlite_master where tbl_name = 'userSettings'", null);
        int TableExcists = cursor.getCount();

        if (TableExcists == 1){
            Intent intent  = new Intent(this, flightList.class);
            startActivity(intent);

        }else{
            FirstTimeSetUp();
        }
        mainDb.close();


    }

    private void FirstTimeSetUp() {
        Intent intent  = new Intent(this, setup.class);
        startActivity(intent);
    }
    private void checkActive(){
        //here SQL query to check active status//
        //if active launch flight activity
        //else launch itinerary screen
    }


}



