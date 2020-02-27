package com.example.neaapp;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

import androidx.fragment.app.FragmentActivity;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;


public class activeStart extends FragmentActivity implements AsynchResponse {

    private Context context;
    private AlarmManager alarmManager;

    public  activeStart(Context context){ //as activeStart is not linked to an activity, it must inherit its context from the class it was called by
        this.context=context;              //Therefore all context calls within the class will be referenced as the variable "context"
        alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
    }

    dbHelper maindb;

    public void start(String fNum,Boolean state){

        if(!state){ //if the state is false, then the app will change the current state of the flight
            changeActiveState(fNum,false);//changes the current state
        }

        String latlong = cacheCheck(fNum); //calls cacheCheck to see if the flight has info already stored


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
            //2nd start async to fetch long lats
            airportFetcher asynchTask = new airportFetcher(context);
            asynchTask.delegate= this;
            asynchTask.execute(dep,arr,fNum); //launches the async task
        }

    }

    public void end(String fNum,String URL_TEXT){//used when flight is finished, and needs to be saved to logbook
        maindb = new dbHelper(context);
        //deletes the flight from the active table, and the itinerary. And opens a record for it in the logbook table
        maindb.deleteActive(fNum);
        maindb.logStart(fNum);
        maindb.deleteByNum(fNum);
        maindb.close();
        removeNotifications(fNum);//deletes all notifications relating to that flight
        new timetableFetcher(context).execute(fNum,URL_TEXT,"arrival"); //calls api to fetch arrival data to save

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
        handleResult(output);//result from the api is passed here once the async task is done
    }

    private void handleResult(String jsonString){
        try {
            //tries to convert the result into a jsonobject
            JSONObject JSONresult = new JSONObject(jsonString);
            //fetches the data from the json object
            String fNum = JSONresult.getString("fnum");
            String deplatlong = JSONresult.getString("deplatlong");
            String arrlatlong = JSONresult.getString("arrlatlong");
            String latlongs = deplatlong+","+arrlatlong;//formats latlong to be stored in table
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
        String result = rawOutput; // formatted as "[ xxx, xxx ,xxx , xxx]" need to split into seperate xxx groups
        String depLat;
        String depLon;
        String arrLat;
        String arrLon;

        String[] results = result.split(",");
        depLat = results[0];
        depLon = results[1];
        arrLat = results[2];
        arrLon = results[3];
        //coords now split into separate parts

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
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,100,intent,PendingIntent.FLAG_CANCEL_CURRENT);  // builds pending intent of code 100
        Boolean alarmExists = (PendingIntent.getBroadcast(context,102, new Intent(context,Notification_reciever.class),PendingIntent.FLAG_NO_CREATE) !=null);
        if(alarmExists){//if any code 102 notifications exist, delete them
            try{
                Intent intent2 = new Intent(this,Notification_reciever.class);
                PendingIntent pendingIntent2 = PendingIntent.getBroadcast(this,102,intent2,0);
                alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP,300000,pendingIntent2); //builds an alarm in order to overwrite any old ones on 102
                alarmManager.cancel(pendingIntent2);//cancels the new code 102 alarm that was set
            } catch (Exception e) {
                //error due to android alarm detection, when alarm actually is cancelled already
            }
            dbHelper db = new dbHelper(context);
            db.atAirport(fNum,"false");
        }
        alarmManager.cancel(pendingIntent);//cancels the code 100 alarm
    }

    private void setNotifications(String fNum){
        // this is triggered when flight is set active. A alarm should be set for the day of the flight at 00:00.
        //this allows the fetching of accurate data pertaining to the flight on the day of the flight.
        maindb = new dbHelper(context);
        Cursor results = maindb.searchByNum(fNum);
        String date = "";
        while(results.moveToNext()) {
            int index;
            index = results.getColumnIndexOrThrow("date");
            date = results.getString(index);
            //date is in form YYYY/MM/DD
        }

            String[] dateChar = date.split("/");//splits the date into year month and day
            //calendar is then set to time 00:00 of the date of the flight
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.YEAR, Integer.parseInt(dateChar[0]));
            calendar.set(Calendar.MONTH, Integer.parseInt(dateChar[1]));
            calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dateChar[2]));
            calendar.set(Calendar.HOUR_OF_DAY,00);
            calendar.set(Calendar.MINUTE,00);
            calendar.set(Calendar.SECOND,00);

        Intent intent = new Intent(context,Notification_reciever.class);
        intent.putExtra("condition",fNum);// this data is passed so that notification receiver knows what to do when alarm is fired off
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,100,intent,0);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(),pendingIntent);//sets alarm for time set by calendar

    }




}
