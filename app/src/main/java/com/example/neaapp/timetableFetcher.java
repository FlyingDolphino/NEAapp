package com.example.neaapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

public class timetableFetcher extends AsyncTask<String,String,String> {
    private WeakReference<Context> contextRef;
   public timetableFetcher(Context context){
        contextRef = new WeakReference<>(context);
    }//all context calls made as contextRef.get()

    private AlarmManager alarmManager;

    @Override
    protected void onPostExecute(String s){
        super.onPostExecute(s);
        if (s.equals("success")){ //if successful, a new instance of main activity is made so screen selection logic in main activity will decide what screen to show
            Intent intent = new Intent(contextRef.get(),MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            contextRef.get().startActivity(intent);
            Toast.makeText(contextRef.get(), "Flight info loaded", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(contextRef.get(), s, Toast.LENGTH_SHORT).show();
        }

    }
    @Override
    protected String doInBackground(String... strings) {
        String s ="";
        try {
            URL avEdgeEndpoint = new URL("http://aviation-edge.com/v2/public/timetable?key=5d26e4-9e1694"+strings[1]); //opens url connection
            HttpURLConnection myConnection = (HttpURLConnection) avEdgeEndpoint.openConnection();
            myConnection.setConnectTimeout(10000); // time out is set at 10 seconds
            if (myConnection.getResponseCode() == 200) {
                //success
                InputStream response = myConnection.getInputStream();
                InputStreamReader inputStreamreader = new InputStreamReader(response);
                BufferedReader streamReader = new BufferedReader(inputStreamreader);

                StringBuilder builder = new StringBuilder();
                String inputString;
                while ((inputString = streamReader.readLine()) != null) {
                    builder.append(inputString);
                }
                JSONArray array = new JSONArray(builder.toString());

                for (int i =0; i< array.length();i++) { //go through each object in the array, until the flight number is found
                    JSONObject tempObj = array.getJSONObject(i);
                    JSONObject flight = tempObj.getJSONObject("flight");
                    String number = flight.getString("iataNumber");

                    if(number.equals(strings[0])&&(strings[2].equals("departure"))) { //if correct flight number, and the api call was for departure data
                        JSONObject departure = tempObj.getJSONObject("departure");
                        JSONObject arrival = tempObj.getJSONObject("arrival");
                        String estTime = departure.getString("estimatedTime");
                        String gate = departure.getString("gate");
                        String terminal = departure.getString("terminal");
                        String estATime = arrival.getString("estimatedTime");
                        String schTime = departure.getString("scheduledTime");
                        s = "success";
                        saveInfo(strings[0], estTime, gate, terminal, estATime, schTime,strings[3]);
                        break;
                    }else if (number.equals(strings[0])){
                        JSONObject arrival = tempObj.getJSONObject("arrival");
                        JSONObject departure = tempObj.getJSONObject("departure");
                        String arr = arrival.getString("iataCode");
                        String aTime = arrival.getString("actualRunway");
                      //  if (aTime.equals("null")){
                    //        aTime = arrival.getString("estimatedTime");
                     //   }
                        String sTime = arrival.getString("scheduledTime");
                        String dTime = departure.getString("actualTime");
                        //method call to save to logbook
                        s = "success";
                        saveToLogbook(strings[0],aTime,sTime,dTime,arr);
                        break;

                    }
                    else{
                        s = "Flight not found";
                    }
                }
            }

        } catch (Exception e) {
            s = e.toString();
        }
        return s;
    }

    private void saveInfo(String fnum,String estTime,String gate,String Terminal,String estATime,String schTime,String atAirport){

        estTime = timeFormat(estTime);//formats times to be saved
        estATime = timeFormat(estATime);
        schTime = timeFormat(schTime);

        //checks if a record already exists, if it does updates the record
        //else it creates a new one
        dbHelper db = new dbHelper(contextRef.get());
        Cursor result = db.activeInfo(fnum);
        String check="null";
        while(result.moveToNext()){
            int i;
            i = result.getColumnIndexOrThrow("flightNum");
            check = result.getString(i);
        }
        if(check.equals(fnum)){
            db.updateTimeTable(fnum,estTime,gate,Terminal,estATime,schTime,atAirport);
        }else{
            db.timetableData(fnum,estTime,gate,Terminal,estATime,schTime,atAirport);
        }
    }

    private void saveToLogbook(String fnum,String aTime, String sTime,String dTime,String arr){//this is called when arrival data is needed
        //so flight is being saved to logbook
       aTime = timeFormat(aTime);//formats times to be saved
       sTime = timeFormat(sTime);
       dTime = timeFormat(dTime);

       dbHelper db = new dbHelper(contextRef.get());
       String delay;
       String flightTime;

       try{//tries to calculate the delay and flight time
           delay = String.valueOf(calcDelay(aTime,sTime));
           flightTime = calcDelay(aTime,dTime);

       } catch (Exception e) {//if that fails, this the arrival information is incomplete
           //so the app will try again in 5 mins, to see if the data gets added then
           delay="";
           flightTime="";
           alarmManager = (AlarmManager)contextRef.get().getSystemService(Context.ALARM_SERVICE);
           Intent intent = new Intent(contextRef.get(),Notification_reciever.class);
           intent.putExtra("condition",fnum);
           intent.putExtra("arrival","true");
           intent.putExtra("arr",arr);
           PendingIntent pendingIntent = PendingIntent.getBroadcast(contextRef.get(),103,intent,0);
           alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP,300000,pendingIntent);//alarm set for 5 mins time
       }
        db.saveLogbook(fnum,flightTime,delay);


    }

    private String timeFormat(String time){
        //time is fetched from the api in the form (<date>T<time>)
        //spliting the result about the "T" then taking index =1 gives us the time
        if(time=="null"){
            return time;
        }else{
            String[] timeList = time.split("T");
            time = timeList[1];

            time = time.substring(0,5); //trims the string so only 5 characters

            return time;

        }

    }

    private String calcDelay(String est, String sch){

        String[] estimated = est.split(":");
        String[] scheduled = sch.split(":");
        Integer estHour = Integer.valueOf(estimated[0]);
        Integer estMin = Integer.valueOf(estimated[1]);
        Integer schHour= Integer.valueOf(scheduled[0]);
        Integer schMin = Integer.valueOf(scheduled[1]);

        double delayHour = (estHour-schHour);
        double delayMin = (estMin-schMin);

        if(delayHour<0){
            delayHour=delayHour+24;
        }
        if(delayMin<0){
            delayMin = delayMin+60;
        }
        delayMin = delayMin/60;
        delayHour = delayHour+delayMin;
        delayHour = (double)Math.round(delayHour*100d)/100d; //rounds value to 2dp
        String delay = Double.toString(delayHour);

        if(est.equals(sch)){
            delay = "0";
        }

        return delay;

    }


}

