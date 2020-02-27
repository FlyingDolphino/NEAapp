package com.example.neaapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

public class logbook extends AppCompatActivity implements OnMapReadyCallback {

    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    private MapView mapView;
    dbHelper maindb;
    Button detail;
    TextView avgFlight;
    TextView totFlight;
    TextView numFlight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logbook);
        //gets map bundle
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }

        detail = findViewById(R.id.detailBtn);
        avgFlight = findViewById(R.id.avgFlightTime);
        totFlight = findViewById(R.id.flightTime);
        numFlight = findViewById(R.id.numFlights);
        mapView = findViewById(R.id.mapView);
        //builds map
        mapView.onCreate(mapViewBundle);
        mapView.getMapAsync(this);


        //calc statistics
        maindb = new dbHelper(this);
        Double totalFlightTime = maindb.sumColoumn("flightTime"); //returns the sum of flight times from logbook table
        Integer totalFlights = maindb.sumFlights(); //returns number of flights
        Double avgTime = (totalFlightTime/totalFlights);
        //formats data to display to user
        avgFlight.setText(String.format("%.2f",avgTime)+" hours");
        totFlight.setText(String.format("%.2f",totalFlightTime)+" hours");
        numFlight.setText(String.valueOf(totalFlights));

        detail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(logbook.this,detailLog.class);
                startActivity(intent);//starts detailLog activity when button is clicked
            }
        });




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
        //code for lat longs
        maindb = new dbHelper(this);
        Cursor results = maindb.latGetter();
        //fetches information needed in order to add flights to map overview
        while (results.moveToNext()){
            int i;
            i = results.getColumnIndexOrThrow("flightNum");
            String fNum = results.getString(i);
            i = results.getColumnIndexOrThrow("dep");
            String dep = results.getString(i);
            i = results.getColumnIndexOrThrow("arr");
            String arr = results.getString(i);
            i = results.getColumnIndexOrThrow("latlong");
            String latlong =results.getString(i);
            String[] coords = latlong.split(",");
            LatLng depCoord = coordMaker(coords[0],coords[1]); //the latlongs are seperated into departure and arrival
            LatLng arrCoord = coordMaker(coords[2],coords[3]);// and sent to coordMaker to be formated

            map.addMarker(new MarkerOptions().position(depCoord).title(dep).snippet("Flight: "+fNum)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))); //markers are added
            map.addMarker(new MarkerOptions().position(arrCoord).title(arr).snippet("Flight: "+fNum)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
            Polyline routeOverView = map.addPolyline(new PolylineOptions().clickable(true).add(depCoord,arrCoord)); //line drawn between markers
        }
    }
    private LatLng coordMaker(String lat,String lon) {
        Double dLat = Double.valueOf(lat);
        Double dLon = Double.valueOf(lon);
        LatLng converted = new LatLng(dLat,dLon);
        return  converted;
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

