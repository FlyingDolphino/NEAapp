package com.example.neaapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;


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

        // MapView requires that the Bundle passed contain _ONLY_ MapView SDK
        // objects or sub-Bundles.
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);//gets the mapview bundle
        }

        String depLat;
        String depLon;
        String arrLat;
        String arrLon;
        String fNum;
        String latlong="";

        //try to get extras

        try{
            Intent data = getIntent();
            Bundle coords = data.getExtras();
            depLat = coords.get("depLat").toString();
            depLon = coords.get("depLon").toString();
            arrLat = coords.get("arrLat").toString();
            arrLon = coords.get("arrLon").toString();
            fNum = coords.get("fNum").toString();

        } catch (Exception e) {
            //extras not avail, get from db (flight active)
            maindb = new dbHelper(this);
            fNum = maindb.checkAnyActive();
            Cursor results = maindb.searchByNum(fNum);
            while(results.moveToNext()){
                int i;
                i = results.getColumnIndexOrThrow("latlong");
                latlong = results.getString(i);
            }
            String[] coords = latlong.split(","); // splits the latlong value returned, formated as lat,lon,lat,lon the first set being for the departure
            depLat = coords[0];                          // the second set for the arrival
            depLon = coords[1];
            arrLat = coords[2];
            arrLon = coords[3];

        }




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



        String[] times = fetchTime(fNum); // fetchTime returns the departure and arrival times as a string[]
        String dTime = times[0];
        String aTime = times[1];

        String flightTime =FlightTimeCalculator(fNum,dTime,aTime) ; // calculates the flight time


        String term = fetchTerminal(fNum); //fetches terminal
        String gate = fetchGate(fNum);//fetches gate

        //sets displays with fetched information
        gateView.setText(gate);
        ETE.setText(flightTime);
        terminal.setText(term);
        depTime.setText(dTime);
        flightNumber.setText(fNum);

        checkActive(); //checks if there is an active flight, and if the current displayed flight is active or not

        mapView = findViewById(R.id.mapView); //initialises google map
        mapView.onCreate(mapViewBundle); //builds google map
        mapView.getMapAsync(this);

        //checks if the flight is flagged as "at airport" if it is, it calls startAirport
        String atAirport="";
        Cursor result = maindb.activeInfo(fNum);
        while(result.moveToNext()){
            int i;
            i = result.getColumnIndexOrThrow("atAirport");
            atAirport = result.getString(i);
        }
        if (atAirport.equals("true")){
            startAirport(fNum);
        }


        //on clicks

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //starts flightList page
                Intent intent = new Intent(track.this,flightList.class);
                startActivity(intent);


            }
        });

        alarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // start alarm page
                Intent intent = new Intent(track.this,alarmPage.class);
                startActivity(intent);
            }
        });


        active.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dbHelper db = new dbHelper(track.this);
                String check = db.checkAnyActive();
                String state = active.getText().toString();
                if(!check.equals("")){ // if check is not a blank string, then there is an active flight
                    if(!state.equals("Stop Tracking!")){ //there is an active flight, so checks if the current displayed flight is active
                        Toast.makeText(track.this, "There is already an active flight, unable to have more than one active flight", Toast.LENGTH_LONG).show();
                    }else{ //the currently viewed flight is the active one, launches activeStart
                        activeStart start = new activeStart(track.this);
                        start.start(flightNumber.getText().toString(),false); //passes false, which instructs the class to toggle the state of the flight
                    }
                }else{ //no active flight
                    activeStart start = new activeStart(track.this);
                    start.start(flightNumber.getText().toString(),false); // begins activeStart, makes flight active
                }
            }
        });

        final String finalFNum = fNum;
        airport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String state = active.getText().toString();
                if(!state.equals("Stop Tracking!")){ //checks if the flight is active
                    Toast.makeText(track.this, "Flight needs to be active in order to continue to next phase", Toast.LENGTH_LONG).show();
                }//flight found as active, so starts startAirport method
                startAirport(finalFNum);

            }
        });

    }
    private void startAirport(String fNum){
        maindb = new dbHelper(track.this);
        String schTime="";
        Cursor result = maindb.searchByNum(fNum); //fetches the flights information from db
        while(result.moveToNext()){
            int i;
            i = result.getColumnIndexOrThrow("dTime");
            schTime = result.getString(i); //looks and stored the departure time
        }

        //passes extras into the intent, these values are passed to the activity once launched
        Intent intent = new Intent(track.this, atAirport.class);
        intent.putExtra("estimated",depTime.getText());
        intent.putExtra("scheduled",schTime);
        intent.putExtra("gate",gateView.getText());
        intent.putExtra("fnum",fNum);
        startActivity(intent); //starts atAirport.class with the extras passed above
    }

    private String fetchGate(String fNum) {
        maindb = new dbHelper(this);
        Cursor result = maindb.activeInfo(fNum); //fetches the information from the activeFlight table
        String gate="";
        while(result.moveToNext()){
            int index;
            index = result.getColumnIndexOrThrow("gate");
            gate = result.getString(index);
        }
        if(gate.equals("null")){
            return "";
        }else{
            return gate; //returns the gate from activeFlight table (if it exists)
        }

    }


    private String fetchTerminal(String fNum) {
        //sql look up to fetch the terminal
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
        } //searches the database and gets the departure and arrival times
        String activecheck = maindb.checkAnyActive(); //checks if there is any active flight
        if(!activecheck.equals("null")){ //if the flight is active, if so the times are taken from the activeFlight table instead (more accurate times)
            result = maindb.activeInfo(fNum);
            while (result.moveToNext()) {
                int i;
                i = result.getColumnIndexOrThrow("estDepTime");
                String DTime = result.getString(i);
                if(!DTime.equals("null")){
                    dTime = result.getString(i);
                }
                i = result.getColumnIndexOrThrow("estArrTime");
                String ATime = result.getString(i);
                if (!ATime.equals("null")){
                    aTime = result.getString(i);
                }
            }
        }
        String[] times = new String[2]; //creates a string of length 2, and stores the times into it, returns the string
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
        }//fetches timezone offsets for the departure and arrival airports

        double dTimeHours = toHours(dTime); //converts times to hours, in double format
        double aTimeHours = toHours(aTime);

        double finalDtime;
        double finalAtime;

        if(dOffset!="0"){ //offset, so departure timezone is converted into UTC
            finalDtime = dTimeHours - Float.parseFloat(dOffset);

        }else{
            finalDtime = dTimeHours;
        }

        if(aOffset!="0"){//offset, so arrival timezone is converted into UTC
            finalAtime  = aTimeHours - Float.parseFloat(aOffset);
        }else{
            finalAtime = aTimeHours;
        }


        double flightTime = finalAtime-finalDtime; // calculates the difference between the two times

        if (flightTime<0){ //if the result of the calculation is negative, +24
            flightTime += 24;
        }

        String time =String.format("%.2f",flightTime); //format the double so that it can be displayed

        String[] strs = time.split("\\.");
        double mins = Integer.parseInt(strs[1]); //takes the decimal part of the time, and converts it into mins
        mins = mins*0.6;
        String hours = strs[0];


        String formattedTime = (hours+" hours "+String.format("%.0f",mins)+" minutes"); //returns message to be displayed
        return formattedTime;


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

    //google map code and methods for the handling of map situations
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
        LatLng dep = new LatLng(depLatitude,depLongitude); //gets latlng format for the positions of departure and arrival airports
        LatLng arr = new LatLng(arrLatitude,arrLongitude);

        map.addMarker(new MarkerOptions().position(dep).title("Departure")); //adds markers to map
        map.addMarker(new MarkerOptions().position(arr).title("Arrival"));
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(dep,5));
        Polyline routeOverView = map.addPolyline(new PolylineOptions().clickable(false).add(dep,arr)); //draws a line between the two markers


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
