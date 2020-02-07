package com.example.neaapp;

import android.content.Context;
import android.content.Intent;
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

    @Override
    protected void onPostExecute(String s){
        super.onPostExecute(s);
        if (s.equals("success")){
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
                JSONArray array = new JSONArray(builder.toString());  // the response of the URL query is put into string from first, then as a JSON array before the first JSON object in that array is taken

                for (int i =0; i< array.length();i++) {
                    JSONObject tempObj = array.getJSONObject(i);
                    JSONObject flight = tempObj.getJSONObject("flight");
                    String number = flight.getString("iataNumber");

                    if(number.equals(strings[0])) {
                        JSONObject departure = tempObj.getJSONObject("departure");
                        JSONObject arrival = tempObj.getJSONObject("arrival");
                        String estTime = departure.getString("estimatedTime");
                        String gate = departure.getString("gate");
                        String terminal = departure.getString("terminal");
                        String estATime = arrival.getString("estimatedTime");
                        saveInfo(strings[0],estTime,gate,terminal,estATime);
                        break;
                    }
                }

                s = "success";


            }/// else statement for timeout etc


        } catch (Exception e) {
            s = e.toString();


        }

        return s;
    }

    private void saveInfo(String fnum,String estTime,String gate,String Terminal,String estATime){

        estTime = timeFormat(estTime);
        estATime = timeFormat(estATime);


        dbHelper db = new dbHelper(contextRef.get());
        db.timetableData(fnum,estTime,gate,Terminal,estATime);
        db.close();

    }
    private String timeFormat(String time){

        if(time=="null"){
            return time;
        }else{
            String[] timeList = time.split("T");
            time = timeList[1];
            return time;
        }

    }

}

