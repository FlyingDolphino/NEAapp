package com.example.neaapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.FloatMath;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.util.Strings;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class track extends AppCompatActivity implements OnMapReadyCallback {


    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";


    //views + buttons init
    private MapView mapView;
    TextView flightNumber;
    TextView ETE;
    TextView depTime;
    Button back;
    Button alarm;
    Button active;
    TextView terminal;
    Button airport;
    TextView gate;
    TextView gateView;

    // variable init
    private double depLatitude;
    private double depLongitude;
    private double arrLatitude;
    private double arrLongitude;
    dbHelper maindb;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track);

        // *** IMPORTANT ***
        // MapView requires that the Bundle you pass contain _ONLY_ MapView SDK
        // objects or sub-Bundles.
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }


        //get extras
        Intent data = getIntent();
        Bundle coords = data.getExtras();
        String depLat = coords.get("depLat").toString();
        String depLon = coords.get("depLon").toString();
        String arrLat = coords.get("arrLat").toString();
        String arrLon = coords.get("arrLon").toString();
        final String fNum = coords.get("fNum").toString();

        //fetch flight information, and departure time
        //dbhelper call


        //set lat and longs to correct type (string to double)
        depLatitude = Double.valueOf(depLat);
        depLongitude = Double.valueOf(depLon);
        arrLatitude = Double.valueOf(arrLat);
        arrLongitude = Double.valueOf(arrLon);

        //set displays
        flightNumber = findViewById(R.id.flightNumber);
        ETE = findViewById(R.id.ete);
        depTime = findViewById(R.id.departureTime);
        back = findViewById(R.id.backButton);
        alarm = findViewById(R.id.alarmButton);
        active =findViewById(R.id.activeButton);
        terminal=findViewById(R.id.terminalText);
        gateView=findViewById(R.id.gateView);
        airport = findViewById(R.id.airportButton);



        String[] times = fetchTime(fNum);
        String dTime = times[0];
        String aTime = times[1];

        String flightTime =FlightTimeCalculator(fNum,dTime,aTime) ;


        String term = fetchTerminal(fNum);
        String gate = fetchGate(fNum);

        gateView.setText(gate);
        ETE.setText(flightTime);
        terminal.setText(term);
        depTime.setText(dTime);
        flightNumber.setText(fNum);
        checkActive();
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(mapViewBundle);

        mapView.getMapAsync(this);


        //on clicks

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(track.this,flightList.class);
                startActivity(intent);
                // might want to unset active with this button

            }
        });

        alarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // start new screen to set alarms
                Intent intent = new Intent(track.this,alarmPage.class);
                startActivity(intent);
            }
        });

        active.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //code to change state
                dbHelper db = new dbHelper(track.this);
                String check = db.checkAnyActive();
                if(!check.equals("")){
                    Toast.makeText(track.this, "There is already an active flight, unable to have more than one active flight", Toast.LENGTH_LONG).show();
                }else{
                    activeStart start = new activeStart(track.this);
                    start.start(flightNumber.getText().toString(),false);
                }

            }
        });

        airport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                maindb = new dbHelper(track.this);
                String schTime="";
                Cursor result = maindb.searchByNum(fNum);
                while(result.moveToNext()){
                    int i;
                    i = result.getColumnIndexOrThrow("dTime");
                    schTime = result.getString(i);
                }

                Intent intent = new Intent(track.this, atAirport.class);
                intent.putExtra("estimated",depTime.getText());
                intent.putExtra("scheduled",schTime);
                intent.putExtra("gate",gateView.getText());
                startActivity(intent);
            }
        });

    }

    private String fetchGate(String fNum) {
        maindb = new dbHelper(this);
        Cursor result = maindb.activeInfo(fNum);
        String gate="";
        while(result.moveToNext()){
            int index;
            index = result.getColumnIndexOrThrow("gate");
            gate = result.getString(index);

        }
        return gate;
    }


    private String fetchTerminal(String fNum) {
        //sql look up to fetch the time
        maindb = new dbHelper(this);
        Cursor result = maindb.searchByNum(fNum);
        String term ="";
        while(result.moveToNext()){
            int index;
            index = result.getColumnIndexOrThrow("terminal");
            term = result.getString(index);
        }

        if (term.equals("null")){
            term = "";
        }
        return term;

    }

    private void checkActive(){
        maindb = new dbHelper(this);
        String check = maindb.checkAnyActive();
        String fNum = flightNumber.getText().toString();
        if (check.equals(fNum)){ //"" means no active flight
            //no active flights, start flight list
            active.setText("Stop Tracking!");
        }else{
            //now open track screen with fnum information, stored as check
            active.setText("Track!");

        }

    }

    private String[] fetchTime(String fNum){
        //sql look up to fetch the time
        maindb = new dbHelper(this);
        Cursor result;
        String dTime =null;
        String aTime=null;

        result = maindb.searchByNum(fNum);


        while(result.moveToNext()){
            int index;
            index = result.getColumnIndexOrThrow("dTime");
            dTime = result.getString(index);
            index = result.getColumnIndexOrThrow("aTime");
            aTime = result.getString(index);
        }
        String activecheck = maindb.checkAnyActive();

        if(!activecheck.equals("null")){
            result = maindb.activeInfo(fNum);
            while (result.moveToNext()) {
                int i;
                i = result.getColumnIndexOrThrow("estDepTime");
                dTime = result.getString(i);
                if(!dTime.equals("null")){
                    dTime = result.getString(i);
                }
                i = result.getColumnIndexOrThrow("estArrTime");
                aTime = result.getString(i);
                if (!aTime.equals("null")){
                    aTime = result.getString(i);
                }
            }
        }
        String[] times = new String[2];
        times[0] = dTime;
        times[1] = aTime;

        return times;
    }

    private String FlightTimeCalculator(String fNum,String dTime, String aTime){

        maindb = new dbHelper(this);
        Cursor result=maindb.searchByNum(fNum);
        String dOffset="";
        String aOffset="";
        while(result.moveToNext()){
            int index;
            index = result.getColumnIndexOrThrow("dtimeOffset");
            dOffset=result.getString(index);
            index = result.getColumnIndexOrThrow("atimeOffset");
            aOffset=result.getString(index);
        }

        double dTimeHours = toHours(dTime);
        double aTimeHours = toHours(aTime);

        double finalDtime;
        double finalAtime;

        if(dOffset!="0"){

            finalDtime = dTimeHours - Float.parseFloat(dOffset);
            //maindb.saveInfo(dTime,fNum,"dTime");
        }else{
            finalDtime = dTimeHours;
        }

        if(aOffset!="0"){
            finalAtime  = aTimeHours - Float.parseFloat(aOffset);
            //maindb.saveInfo(aTime,fNum,"aTime");
        }else{
            finalAtime = aTimeHours;
        }


        double flightTime = finalAtime-finalDtime;

        if (flightTime<0){
            flightTime += 24;
        }

        String time =String.format("%.2f",flightTime);

        String[] strs = time.split("\\.");
        double mins = Integer.parseInt(strs[1]);
        mins = mins*0.6;
        String hours = strs[0];


        String formattedTime = (hours+" hours "+String.format("%.0f",mins)+" minutes");
        return formattedTime;
        //return String.format("%.2f",flightTime);

    }

    private double toHours(String time){
        // format is HH:MM:SS list is made
        String[] splitTime = time.split(":");
        double hours = Float.parseFloat(splitTime[0]);
        double mins = Float.parseFloat(splitTime[1]);

        mins = mins/60;


        double totalHours = (hours+mins);
        return totalHours;
    }



    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
        }

        mapView.onSaveInstanceState(mapViewBundle);
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onMapReady(GoogleMap map) {
        //map.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
        LatLng dep = new LatLng(depLatitude,depLongitude);
        LatLng arr = new LatLng(arrLatitude,arrLongitude);

        map.addMarker(new MarkerOptions().position(dep).title("Departure"));
        map.addMarker(new MarkerOptions().position(arr).title("Arrival"));

        Polyline routeOverView = map.addPolyline(new PolylineOptions().clickable(false).add(dep,arr));


    }

    @Override
    public void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }


}
