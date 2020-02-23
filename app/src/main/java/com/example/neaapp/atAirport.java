package com.example.neaapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
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

import java.net.URL;
import java.util.Calendar;


public class atAirport extends AppCompatActivity implements OnMapReadyCallback {

    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";

    private GoogleMap mymap;
    private MapView mapView;
    private LocationListener listener;
    private LocationManager locManager;
    private AlarmManager alarmManager;
    TextView SDT;
    TextView EDT;
    TextView gate;
    TextView delay;
    LatLng currentLocation;
    Button landed;




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

        String sdt=null;
        String edt=null;
        String g=null;
        String fNum=null;

       /* Intent data = getIntent();
        Bundle info = data.getExtras();
        sdt = info.getString("scheduled");
        edt = info.getString("estimated");
        g = info.getString("gate");
        fNum = info.getString("fnum");*/

        dbHelper db = new dbHelper(this);
        fNum = db.checkAnyActive();

        Cursor results = db.searchByNum(fNum);
        while(results.moveToNext()){
            int i;
            i = results.getColumnIndexOrThrow("dTime");
            sdt = results.getString(i);
        }

        results = db.activeInfo(fNum);
        while(results.moveToNext()){
            int i;
            i = results.getColumnIndexOrThrow("estDepTime");
            edt = results.getString(i);
            i =results.getColumnIndexOrThrow("gate");
            g = results.getString(i);
        }





        SDT = findViewById(R.id.SDT);
        EDT = findViewById(R.id.EDTText);
        gate= findViewById(R.id.gateView);
        delay = findViewById(R.id.delay);
        landed = findViewById(R.id.landedBtn);

        SDT.setText(sdt);
        EDT.setText(edt);
        gate.setText(g);
        if(!edt.equals("null")){
            delay.setText(calcDelay(edt,sdt));
        }else{
            delay.setText("");
        }

        Boolean alarmExists = (PendingIntent.getBroadcast(this,102, new Intent(this,Notification_reciever.class),PendingIntent.FLAG_NO_CREATE) !=null);
        //start repeating alarms if no gate is found
        alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        if(g.equals("null")||(edt.equals("null"))){
            if(!alarmExists){
                Intent intent = new Intent(this,Notification_reciever.class);
                intent.putExtra("condition",fNum);
                intent.putExtra("airport","true");
                PendingIntent pendingIntent = PendingIntent.getBroadcast(this,102,intent,0);
                alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, 5000,300000,pendingIntent);
                db.atAirport(fNum,"true");
                Toast.makeText(this, "Refresh Started", Toast.LENGTH_LONG).show();
            }//else no alarm is needed, as the refresh is already set

        }else{//if gate is known, stop refresh
            //cancel notifications
            Intent intent = new Intent(this,Notification_reciever.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this,102,intent,0);
            alarmManager.cancel(pendingIntent);
        }


        final String finalFNum = fNum;
        landed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //start timetable fetcher to find arrival time, calc delay, save to logbook table

                //build url for request
                dbHelper db  = new dbHelper(atAirport.this);
                Cursor result = db.searchByNum(finalFNum);
                String arr = "";
                while(result.moveToNext()){
                    int index;
                    index = result.getColumnIndexOrThrow("arr");
                    arr = result.getString(index);
                }
                String URL_TEXT = "&iataCode="+arr+"&type=arrival";
                activeStart start = new activeStart(atAirport.this);
                start.end(finalFNum,URL_TEXT);



            }
        });


    }


    private String calcDelay(String est, String sch){

        String[] estimated = est.split(":");
        String[] scheduled = sch.split(":");
        Integer estHour = Integer.valueOf(estimated[0]);
        Integer estMin = Integer.valueOf(estimated[1]);
        Integer schHour= Integer.valueOf(scheduled[0]);
        Integer schMin = Integer.valueOf(scheduled[1]);

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
