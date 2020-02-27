package com.example.neaapp;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;


import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class Notification_reciever extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        // bundles any extras into variable data
        Bundle data = intent.getExtras();
        String type;
        try{
            type = data.get("condition").toString();
        } catch (Exception e) {
            dbHelper db = new dbHelper(context);
            type = db.checkAnyActive(); //if no extra is recieved, fetch active flights fnum from database
        }



        if (type.equals("setNoti")){//type tells app what type of alarm has been triggered. setNoti means that a user set alert has been triggered
            //for leaving their home
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "1")
                    .setSmallIcon(android.R.drawable.arrow_up_float)
                    .setContentTitle("Trip Advice")
                    .setContentText("You should leave your house now")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true);

            notificationManager.notify(101,builder.build());


        }else{//in this case the flight number was passed

            //logic to fetch the timetable from API.
            String fNum = type;
            String airport;
            String arr;
            //try to fetch any additional extras that may be passed
            try{
                airport = data.get("airport").toString();
            } catch (Exception e) {
                airport = "false";
            }
            String arrival;
            try{
                arrival = data.get("arrival").toString();
                arr = data.get("arr").toString();

            } catch (Exception e) {
                arrival = "false";
                arr="";
            }


            dbHelper db  = new dbHelper(context);
            Cursor result = db.searchByNum(fNum);
            String dep = "";

            while(result.moveToNext()){
                int index;
                index = result.getColumnIndexOrThrow("dep");
                dep = result.getString(index);
            }

            //now either the departure, or arrival airports are known. This tells us if we need to fetch the arrival, or departure information
            String URL_TEXT;
            if(arrival.equals("true")){//starts the api call for either arrival or departure information
                URL_TEXT = "&iataCode="+arr+"&type=arrival";
                new timetableFetcher(context).execute(fNum,URL_TEXT,"arrival");
            }else{
                URL_TEXT = "&iataCode="+dep+"&type=departure";
                new timetableFetcher(context).execute(fNum,URL_TEXT,"departure",airport);
            }







        }

    }

}
