package com.example.neaapp;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class Notification_reciever extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {


        Bundle data = intent.getExtras();
        String type = data.get("condition").toString();

        if (type=="startTable"){
            //logic to fetch the timetable from API. and put it in the active flight need to change it so that flight info screens etc use that instead
        }else{
            //for leaving
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "1")
                    .setSmallIcon(android.R.drawable.arrow_up_float)
                    .setContentTitle("Trip Advice")
                    .setContentText("You should leave your house now")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true);

            notificationManager.notify(101,builder.build());


        }

    }
}
