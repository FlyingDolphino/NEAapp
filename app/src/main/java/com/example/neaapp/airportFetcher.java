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
    } //all context calls contextRef.get()

    dbHelper maindb;
    @Override
    protected void onPostExecute(String results) {
        super.onPostExecute(results);
        delegate.proccessFinish(results); // the results are passed through AsynchResponse interface
    }

    @Override
    protected String doInBackground(String... strings) {

        String s ="";
        try {
            int counter = 0;
            List<String> latLon = new ArrayList<>();
            JSONObject results = new JSONObject();
            while(counter<2){//makes two calls to the api, one for the departure, the other for arrival airport

                URL avEdgeEndpoint = new URL("http://aviation-edge.com/v2/public/airportDatabase?key=e97b69-6d8993&codeIataAirport=" +strings[counter]);
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
                    JSONObject airportData = array.getJSONObject(0);

                    String latitude = airportData.getString("latitudeAirport");
                    String longitude = airportData.getString("longitudeAirport");
                    String offset = airportData.getString("GMT");


                    latLon.add(latitude); //adds the latlon information to the array
                    latLon.add(longitude);



                    if(counter==0){// means that departure is currently being processed
                        results.put("dep",strings[counter]);
                        results.put("deplatlong",latitude+","+longitude);
                        results.put("DOffset",offset);


                    }else if(counter==1){//means that arrival is being processed
                        results.put("arr",strings[counter]);
                        results.put("arrlatlong",latitude+","+longitude);
                        results.put("AOffset",offset);
                        results.put("fnum",strings[2]);
                        return results.toString();
                    }
                    s= "success";
                }else{
                    s="request Timeout";
                }
                counter += 1;
            }
        } catch (Exception e) {
            s = e.toString();
        }
        return s;
    }

}















