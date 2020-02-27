package com.example.neaapp;

import android.app.Activity;
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
import java.util.List;

public class flightFetcher extends AsyncTask<String,String,String> {
    private WeakReference<Context> contextRef;
    public flightFetcher(Context context){
        contextRef = new WeakReference<>(context);
    } //async tasks do not have their own context. Hence a WeakReference context is used. Any context calls in this class must use contextRef.get()

    dbHelper maindb;

    @Override
    protected void onPostExecute(String s) {//this method is executed once the doInBackground task is finished.
        super.onPostExecute(s);

        if(s=="success"){ // the data has been saved
            Intent intent = new Intent(contextRef.get(),flightList.class);
            contextRef.get().startActivity(intent); //start the flightList activity
            Toast.makeText(contextRef.get(),"Data Saved",Toast.LENGTH_LONG).show();//inform the user that the flight has been saved
        }else{
            Toast.makeText(contextRef.get(),s,Toast.LENGTH_LONG).show();//inform the user of the error
        }
    }

    @Override
    protected String doInBackground(String... strings) {
        String s ="";
            try {
            URL avEdgeEndpoint = new URL("http://aviation-edge.com/v2/public/routes?key=e97b69-6d8993&" + strings[2]); //opens url connection,
            HttpURLConnection myConnection = (HttpURLConnection) avEdgeEndpoint.openConnection();      //strings[2] references second the last string passed ino flightFetcher
            myConnection.setConnectTimeout(10000); // time out is set at 10 seconds
            if (myConnection.getResponseCode() == 200) {
                //success

                InputStream response = myConnection.getInputStream();//get the response from the request
                InputStreamReader inputStreamreader = new InputStreamReader(response);
                BufferedReader streamReader = new BufferedReader(inputStreamreader);

                StringBuilder builder = new StringBuilder();
                String inputString;
                while ((inputString = streamReader.readLine()) != null) {//go through the response and build a string with it
                    builder.append(inputString);
                }
                JSONArray array = new JSONArray(builder.toString());  // the response of the URL query is put into string from first,
                                                                        // then as a JSON array before the first JSON object in that array is taken
                JSONObject flightData = array.getJSONObject(0);

                String dep = flightData.getString("departureIata");
                String arr = flightData.getString("arrivalIata");
                String dTime = flightData.getString("departureTime");
                String aTime = flightData.getString("arrivalTime");
                String terminal = flightData.getString("departureTerminal");
                saveFlight(strings[0], dep, arr, strings[1], dTime, aTime, 0, terminal); //saves the data from the json
                s = "success";
            }else{
                s = "request timeout";
            }
        } catch (Exception e) {
            s = e.toString();
        }
     return s;
    }
    private void saveFlight(String fNum, String dep, String arr, String Dte, String dTime, String aTime, Integer active, String terminal) {
        maindb = new dbHelper(contextRef.get());
        maindb.insertFlight(fNum, dep, arr, Dte, dTime, aTime, active, terminal);

        maindb.close();

    }


}






