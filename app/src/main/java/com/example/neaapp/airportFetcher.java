package com.example.neaapp;

import android.content.Context;
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
import java.util.ArrayList;
import java.util.List;

public class airportFetcher extends AsyncTask<String,String,String> {
    public AsynchResponse delegate=null;
    private WeakReference<Context> contextRef;
    public airportFetcher(Context context){
        contextRef = new WeakReference<>(context);
    }

    dbHelper maindb;


    @Override
    protected void onPostExecute(String latLon) {
        super.onPostExecute(latLon);

        delegate.proccessFinish(latLon);


    }

    @Override
    protected String doInBackground(String... strings) {
        String s ="";
        List<String> latLon = new ArrayList<>();
        try {
            Integer counter = 0;
            while(counter<2){

                URL avEdgeEndpoint = new URL("http://aviation-edge.com/v2/public/airportDatabase?key=5d26e4-9e1694&codeIataAirport=" +strings[counter]); //needs url build
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

                    JSONObject airportData = array.getJSONObject(0);

                    String latitude = airportData.getString("latitudeAirport");
                    String longitude = airportData.getString("longitudeAirport");


                    latLon.add(latitude);
                    latLon.add(longitude);



                    if(counter==1){

                        saveLatLongs(latLon.toString(),strings[2]);
                        latLon.add(strings[2]);
                        return latLon.toString();
                    }

                    s= "success";

                }
                counter += 1;
            }

        } catch (Exception e) {
            s = e.toString();



        }

        return s;
    }
    public void saveLatLongs(String latlongs,String fnum){
        //sql call
        maindb = new dbHelper(contextRef.get());
        maindb.saveLatLong(latlongs,fnum);
        maindb.close();
    }

}















