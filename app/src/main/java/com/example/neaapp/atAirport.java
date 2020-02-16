package com.example.neaapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import org.w3c.dom.Text;

public class atAirport extends AppCompatActivity implements OnMapReadyCallback {

    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";

    private GoogleMap mymap;
    private MapView mapView;
    private LocationListener listener;
    private LocationManager locManager;
    TextView SDT;
    TextView EDT;
    TextView gate;
    TextView delay;
    LatLng currentLocation;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_at_airport);

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PackageManager.PERMISSION_GRANTED);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PackageManager.PERMISSION_GRANTED);


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
        Integer delayMin = estMin-schMin;

        if(delayHour<0){
            delayHour=delayHour+24;
        }
        if(delayMin<0){
            delayMin = delayMin+60;
        }

        String delay = (delayHour.toString()+" Hours "+delayMin.toString()+" Mins");
        if(est.equals(sch)){
            delay = "On Time";
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
    public void onMapReady(final GoogleMap googleMap) {
        mymap = googleMap;
        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                mymap.clear();
                currentLocation = new LatLng(location.getLatitude(),location.getLongitude());
                mymap.addMarker(new MarkerOptions().position(currentLocation).title("You"));
                mymap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation,19));
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }

        };
        locManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        try{ locManager.requestLocationUpdates(locManager.GPS_PROVIDER,5000,5,listener); }
        catch (SecurityException e){
            e.printStackTrace();
        }
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
