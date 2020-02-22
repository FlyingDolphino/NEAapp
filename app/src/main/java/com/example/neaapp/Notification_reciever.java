package com.example.neaapp;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class Notification_reciever extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {


        Bundle data = intent.getExtras();
        String type;
        try{
            type = data.get("condition").toString();
        } catch (Exception e) {
            dbHelper db = new dbHelper(context);
            type = db.checkAnyActive();
        }



        if (type.equals("setNoti")){
            //for leaving
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "1")
                    .setSmallIcon(android.R.drawable.arrow_up_float)
                    .setContentTitle("Trip Advice")
                    .setContentText("You should leave your house now")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true);

            notificationManager.notify(101,builder.build());


        }else{

            //logic to fetch the timetable from API. and put it in the active flight need to change it so that flight info screens etc use that instead
            String fNum = type;
            String airport;
            try{
                airport = data.get("airport").toString();
            } catch (Exception e) {
                airport = "false";
            }

            //build url for request
            dbHelper db  = new dbHelper(context);
            Cursor result = db.searchByNum(fNum);
            String dep = "";
            while(result.moveToNext()){
                int index;
                index = result.getColumnIndexOrThrow("dep");
                dep = result.getString(index);
            }
            String URL_TEXT = "&iataCode="+dep+"&type=departure";


            new timetableFetcher(context).execute(fNum,URL_TEXT,"departure",airport);





        }

    }

}
