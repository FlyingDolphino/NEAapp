package com.example.neaapp;

import android.content.Context;
import android.os.AsyncTask;
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
    protected void onPostExecute(String results) {
        super.onPostExecute(results);

        delegate.proccessFinish(results);


    }

    @Override
    protected String doInBackground(String... strings) {

        String s ="";
        try {
            int counter = 0;
            List<String> latLon = new ArrayList<>();
            JSONObject results = new JSONObject();
            while(counter<2){

                URL avEdgeEndpoint = new URL("http://aviation-edge.com/v2/public/airportDatabase?key=e97b69-6d8993&codeIataAirport=" +strings[counter]); //needs url build
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
                    String offset = airportData.getString("GMT");


                    latLon.add(latitude);
                    latLon.add(longitude);



                    if(counter==0){
                        results.put("dep",strings[counter]);
                        results.put("deplatlong",latitude+","+longitude);
                        results.put("DOffset",offset);


                    }else if(counter==1){
                        results.put("arr",strings[counter]);
                        results.put("arrlatlong",latitude+","+longitude);
                        results.put("AOffset",offset);
                        results.put("fnum",strings[2]);
                       // saveLatLongs(latLon.toString(),strings[2]);
                        return results.toString();
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

}















