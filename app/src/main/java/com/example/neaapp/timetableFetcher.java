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
    }

    private AlarmManager alarmManager;


    @Override
    protected void onPostExecute(String s){
        super.onPostExecute(s);
        if (s.equals("success")){
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
            URL avEdgeEndpoint = new URL("http://aviation-edge.com/v2/public/timetable?key=e97b69-6d8993"+strings[1]); //opens url connection
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
                JSONArray array = new JSONArray(builder.toString());  // the response of the URL query is put into string from first, then as a JSON array before the first JSON object in that array is taken

                for (int i =0; i< array.length();i++) {
                    JSONObject tempObj = array.getJSONObject(i);
                    JSONObject flight = tempObj.getJSONObject("flight");
                    String number = flight.getString("iataNumber");

                    if(number.equals(strings[0])&&(strings[2].equals("departure"))) {
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

        estTime = timeFormat(estTime);
        estATime = timeFormat(estATime);
        schTime = timeFormat(schTime);


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
    private void saveToLogbook(String fnum,String aTime, String sTime,String dTime,String arr){
       aTime = timeFormat(aTime);
       sTime = timeFormat(sTime);
       dTime = timeFormat(dTime);

       dbHelper db = new dbHelper(contextRef.get());


       String delay;
       String flightTime;

       try{
           delay = String.valueOf(calcDelay(aTime,sTime));
           flightTime = calcDelay(aTime,dTime);

       } catch (Exception e) {
           delay="";
           flightTime="";
           //flight time unavailable currently, try in 10 mins;
           alarmManager = (AlarmManager)contextRef.get().getSystemService(Context.ALARM_SERVICE);
           Intent intent = new Intent(contextRef.get(),Notification_reciever.class);
           intent.putExtra("condition",fnum);
           intent.putExtra("arrival","true");
           intent.putExtra("arr",arr);
           PendingIntent pendingIntent = PendingIntent.getBroadcast(contextRef.get(),103,intent,0);
           alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP,300000,pendingIntent);

       }

        db.saveLogbook(fnum,flightTime,delay);






    }

    private String timeFormat(String time){

        if(time=="null"){
            return time;
        }else{
            String[] timeList = time.split("T");
            time = timeList[1];

            time = time.substring(0,5);

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
        delayHour = (double)Math.round(delayHour*100d)/100d;
        String delay = Double.toString(delayHour);

        if(est.equals(sch)){
            delay = "0";
        }

        return delay;

    }


}

