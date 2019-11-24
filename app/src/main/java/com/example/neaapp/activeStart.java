package com.example.neaapp;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.view.View;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;


public class activeStart  implements AsynchResponse {

    private Context context;

    public  activeStart(Context context){
        this.context=context;
    }

    dbHelper maindb;


    public void start(String fNum,Boolean state){


        changeActiveState(fNum,state);//changes to active if required
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
    private void handleState(String fNum,Boolean state){ //checks if currently active  !!!! not needed, redundant now
        if(!state){
            //state must be changed
            maindb = new dbHelper(context);
            boolean insertcheck = maindb.ActiveOnOff(fNum);
            // error message here
            maindb.close();

            Toast.makeText(context,"flight now active",Toast.LENGTH_LONG).show();
        } //no else, as if its true flight is active and no change is required
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
        depLat = results[0]; //.substring(1);  //removes first character "["
        depLon = results[1];
        arrLat = results[2];
        arrLon = results[3];//.replace("]","");



        // now start the track activity with the coords passed
        Intent intent = new Intent(context,track.class);
        intent.putExtra("fNum",fNum);
        intent.putExtra("depLat",depLat);
        intent.putExtra("depLon",depLon);
        intent.putExtra("arrLat",arrLat);
        intent.putExtra("arrLon",arrLon);
        //intent.putExtra("depTime",displayDTime.getText().toString()); //need to pull time from database either in here or in other
        context.startActivity(intent);

    }

    private void changeActiveState(String fNum, Boolean state){

        if(!state){
            // needs to change to active
            maindb = new dbHelper(context);
            boolean insertcheck = maindb.ActiveOnOff(fNum);
            // error message here
            maindb.close();

        }// no else needed, flight is already active


    }




}
