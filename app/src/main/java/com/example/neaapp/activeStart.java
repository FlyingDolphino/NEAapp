package com.example.neaapp;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.view.View;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.common.util.Strings;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;


public class activeStart extends FragmentActivity implements AsynchResponse {

    private Context context;
    private AlarmManager alarmManager;

    public  activeStart(Context context){
        this.context=context;
        alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
    }

    dbHelper maindb;


    public void start(String fNum,Boolean state){

        if(!state){
            changeActiveState(fNum,false);//changes the current state
        }



        String latlong = cacheCheck(fNum);



//if statement to check if current latlong is already cached for the flight
        if(latlong!=null){

            handleLatLong(latlong,fNum); //sends the cached latlong to be processed

        }else{
            // the lat long + offset is not cached

            //1st pull dep and arr airports from database
            maindb = new dbHelper(context);
            Cursor result = maindb.searchByNum(fNum);
            String dep = "";
            String arr = "";
            while(result.moveToNext()){
                int index;
                index = result.getColumnIndexOrThrow("dep");
                dep = result.getString(index);
                index = result.getColumnIndexOrThrow("arr");
                arr = result.getString(index);
            }
            //2nd start asynch to fetch long lats
            airportFetcher asynchTask = new airportFetcher(context);
            asynchTask.delegate= this;
            asynchTask.execute(dep,arr,fNum);
        }

    }

    public void end(String fNum,String URL_TEXT){
        maindb = new dbHelper(context);
        maindb.deleteActive(fNum);
        maindb.logStart(fNum);
        maindb.deleteByNum(fNum);
        maindb.close();
        removeNotifications(fNum);
        new timetableFetcher(context).execute(fNum,URL_TEXT,"arrival");

    }



    private String cacheCheck(String fNum){
        maindb = new dbHelper(context);  //connects to dbhelper
        Cursor cache = maindb.searchLatLong(fNum); // returns lat long column from current flight number
        String latlong = ""; //inits latlong
        while (cache.moveToNext()){
            int index;
            index = cache.getColumnIndexOrThrow("latlong");
            latlong = cache.getString(index);
        }
        maindb.close();
        return latlong;

    }


    @Override
    public void proccessFinish(String output) {
        handleResult(output);
    }

    private void handleResult(String jsonString){
        try {
            JSONObject JSONresult = new JSONObject(jsonString);


            //prep relevant data
            String fNum = JSONresult.getString("fnum");
            String deplatlong = JSONresult.getString("deplatlong");
            String arrlatlong = JSONresult.getString("arrlatlong");
            String latlongs = deplatlong+","+arrlatlong;
            String aoffset = JSONresult.getString("AOffset");
            String doffset = JSONresult.getString("DOffset");


            //save each of them
            maindb = new dbHelper(context);
            maindb.saveInfo(latlongs,fNum,"latlong");

            maindb.saveInfo(doffset,fNum,"dtimeOffset");
            maindb.saveInfo(aoffset,fNum,"atimeOffset");
            maindb.close();




            handleLatLong(latlongs,fNum);
        } catch (JSONException e) {
            e.printStackTrace();
        }





    }

    private void handleLatLong(String rawOutput,String fNum){
        String result = rawOutput; // formatted as "[ xxx, xxx ,xxx , xxx, flightnum ]" need to split into seperate xxx groups
        String depLat;
        String depLon;
        String arrLat;
        String arrLon;

        String[] results = result.split(",");
        depLat = results[0];
        depLon = results[1];
        arrLat = results[2];
        arrLon = results[3];



        // now start the track activity with the coords passed
        Intent intent = new Intent(context,track.class);
        intent.putExtra("fNum",fNum);
        intent.putExtra("depLat",depLat);
        intent.putExtra("depLon",depLon);
        intent.putExtra("arrLat",arrLat);
        intent.putExtra("arrLon",arrLon);
        context.startActivity(intent);

    }

    private void changeActiveState(String fNum, Boolean state){

        // needs to change state
        maindb = new dbHelper(context);
        boolean newState = maindb.ActiveOnOff(fNum); // newState = true, active newState = false, not active


        if (newState){
            //alarms set here
            setNotifications(fNum);
            maindb.close();
        }else{
            //remove alarms as flight is no longer active and delete the active flight information
            maindb.deleteActive(fNum);
            maindb.close();
            removeNotifications(fNum);
        }





    }

    private void removeNotifications(String fNum) {
        Intent intent = new Intent(context, Notification_reciever.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,100,intent,PendingIntent.FLAG_CANCEL_CURRENT);
        Boolean alarmExists = (PendingIntent.getBroadcast(context,102, new Intent(context,Notification_reciever.class),PendingIntent.FLAG_NO_CREATE) !=null);
        if(alarmExists){
            try{
                PendingIntent pendingIntent2 = PendingIntent.getBroadcast(this,102,intent,0);
                alarmManager.cancel(pendingIntent2);
            } catch (Exception e) {
                //error due to android alarm detection, when alarm actually is cancelled already
            }
            dbHelper db = new dbHelper(context);
            db.atAirport(fNum,"false");

        }
        alarmManager.cancel(pendingIntent);


    }

    private void setNotifications(String fNum){
        // this is triggered when flight is set active. A alarm should be set for the day of the flight at 00:00.

        maindb = new dbHelper(context);
        Cursor results = maindb.searchByNum(fNum);
        String date = "";
        while(results.moveToNext()) {
            int index;
            index = results.getColumnIndexOrThrow("date");
            date = results.getString(index);
            //date is in form YYYY/MM/DD
        }

            String[] dateChar = date.split("/");
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.YEAR, Integer.parseInt(dateChar[0]));
            calendar.set(Calendar.MONTH, Integer.parseInt(dateChar[1]));
            calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dateChar[2]));
            calendar.set(Calendar.HOUR_OF_DAY,00);
            calendar.set(Calendar.MINUTE,00);
            calendar.set(Calendar.SECOND,00);

        Intent intent = new Intent(context,Notification_reciever.class);
        intent.putExtra("condition",fNum);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,100,intent,0);


        alarmManager.setExact(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(),pendingIntent);

    }




}
