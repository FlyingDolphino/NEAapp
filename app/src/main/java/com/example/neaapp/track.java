package com.example.neaapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

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
        String fNum = coords.get("fNum").toString();

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

        checkActive();


        String dTime = fetchTime(fNum);



        depTime.setText(dTime);
        flightNumber.setText(fNum);
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
            }
        });

        active.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //code to change state
                activeStart start = new activeStart(track.this);
                start.start(flightNumber.getText().toString(),false);
            }
        });



    }

    private void checkActive(){
        maindb = new dbHelper(this);
        String check = maindb.checkAnyActive();
        if (check==""){ //"" means no active flight
            //no active flights, start flight list
            active.setText("Track!");
        }else{
            //now open track screen with fnum information, stored as check
            active.setText("Stop Tracking!");

        }

    }

    private String fetchTime(String fNum){
        //sql look up to fetch the time
        maindb = new dbHelper(this);
        Cursor result = maindb.searchByNum(fNum);
        String time ="";
        while(result.moveToNext()){
            int index;
            index = result.getColumnIndexOrThrow("dTime");
            time = result.getString(index);
        }
        return time;
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
