package com.example.neaapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;

import org.w3c.dom.Text;

public class atAirport extends AppCompatActivity implements OnMapReadyCallback {

    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";

    private MapView mapView;
    TextView SDT;
    TextView EDT;
    TextView gate;
    TextView delay;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_at_airport);



        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(mapViewBundle);

        mapView.getMapAsync(this);

        Intent data = getIntent();
        Bundle info = data.getExtras();
        String sdt = info.getString("scheduled");
        String edt = info.getString("estimated");
        String g = info.getString("gate");


        SDT = findViewById(R.id.SDT);
        EDT = findViewById(R.id.EDTText);
        gate= findViewById(R.id.gateView);
        delay = findViewById(R.id.delay);


        SDT.setText(sdt);
        EDT.setText(edt);
        gate.setText(g);
        delay.setText(calcDelay(edt,sdt));

    }
    private String calcDelay(String est, String sch){
        String[] estimated = est.split(":");
        String[] scheduled = sch.split(":");
        Integer estHour = Integer.valueOf(estimated[0]);
        Integer estMin = Integer.valueOf(scheduled[0]);
        Integer schHour= Integer.valueOf(estimated[1]);
        Integer schMin = Integer.valueOf(estimated[1]);

        Integer delayHour = estHour-schHour;
        Integer delayMin = estMin-schHour;

        if(delayHour<0){
            delayHour=delayHour+24;
        }
        if(delayMin<0){
            delayMin = delayMin+60;
        }

        String delay = (delayHour.toString()+" Hours "+delayMin.toString()+" Mins");
        if(est.equals(sch)){
            delay = "";
        }
        return delay;

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
    public void onMapReady(GoogleMap googleMap) {

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
