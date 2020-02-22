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
    }

    dbHelper maindb;

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);

        if(s=="success"){
            Intent intent = new Intent(contextRef.get(),flightList.class);
            contextRef.get().startActivity(intent);
            Toast.makeText(contextRef.get(),"Data Saved",Toast.LENGTH_LONG).show();
        }else{
            Toast.makeText(contextRef.get(),s,Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected String doInBackground(String... strings) {
        String s ="";
            try {
            URL avEdgeEndpoint = new URL("http://aviation-edge.com/v2/public/routes?key=e97b69-6d8993&" + strings[2]); //opens url connection, strings[2] references second the last string passed ino flightFetcher
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

                JSONObject flightData = array.getJSONObject(0);

                String dep = flightData.getString("departureIata");
                String arr = flightData.getString("arrivalIata");
                String dTime = flightData.getString("departureTime");
                String aTime = flightData.getString("arrivalTime");
                String terminal = flightData.getString("departureTerminal");
                saveFlight(strings[0], dep, arr, strings[1], dTime, aTime, 0, terminal); // passes the data returned from the json object and passes it into the saveFlight method to be saved into the db
                s = "success";


            }/// else statement for timeout etc


        } catch (Exception e) {
            s = e.toString();


        }

     return s;
    }
    private void saveFlight(String fNum, String dep, String arr, String Dte, String dTime, String aTime, Integer active, String terminal) {
        maindb = new dbHelper(contextRef.get());
        boolean insertCheck = maindb.insertFlight(fNum, dep, arr, Dte, dTime, aTime, active, terminal);

        maindb.close(); // add error return if SQL fails to save data

    }


}






