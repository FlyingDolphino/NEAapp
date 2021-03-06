package com.example.neaapp;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    dbHelper maindb;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        createNotificationChannel(); //calls method to create notification channels

        SQLiteDatabase mainDb =  openOrCreateDatabase("main.db",MODE_PRIVATE,null);// connects to database
        Cursor cursor = mainDb.rawQuery("select DISTINCT tbl_name from sqlite_master where tbl_name = 'user'", null);//searches to see if table exists
        int TableExcists = cursor.getCount();

        if (TableExcists == 1){ //if table exists, calls method to check for active flight
            checkActive();

        }else{
            FirstTimeSetUp();  //if not, call first time setup.
        }
        mainDb.close();


    }

    private void FirstTimeSetUp() {
        Intent intent  = new Intent(this, setup.class); //starts the activity, setup.class
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
    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library

        String CHANNEL_ID = "1";
        String name = "Reminder NEA";
        String description = "reminding you to leave your house";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
