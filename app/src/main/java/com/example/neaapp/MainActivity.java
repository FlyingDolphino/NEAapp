package com.example.neaapp;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    dbHelper maindb;
    @Override
    protected void onCreate(Bundle savedInstanceState) {


        // need some menu buttons


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SQLiteDatabase mainDb =  openOrCreateDatabase("main.db",MODE_PRIVATE,null);
        Cursor cursor = mainDb.rawQuery("select DISTINCT tbl_name from sqlite_master where tbl_name = 'userSettings'", null);
        int TableExcists = cursor.getCount();

        if (TableExcists == 1){
            checkActive();

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
        maindb = new dbHelper(this);
        String check = maindb.checkAnyActive();
        if (check==""){ //"" means no active flight
            //no active flights, start flight list
            Intent intent  = new Intent(this, flightList.class);
            startActivity(intent);
        }else{
            //now open track screen with fnum information, stored as check
            activeStart start = new activeStart(this);
            start.start(check,true);

        }

    }


}



