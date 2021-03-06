package com.example.neaapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.util.Strings;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class viewFlightInfo extends AppCompatActivity {
    dbHelper maindb;
    String fNum;
    TextView date;
    TextView displayFlight;
    TextView displayDep;
    TextView displayArr;
    TextView displayDTime;
    TextView displayATime;
    TextView displayStatus;

    Button deleteBtn;
    Button trackBtn;
    Button editDtime;
    Button editAtime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_flight_info);

        fNum = getIntent().getStringExtra("FLIGHT_NUMBER");
        date = findViewById(R.id.date);
        displayFlight = findViewById(R.id.displayFlightNumber);
        displayDep = findViewById(R.id.departureCode);
        displayArr = findViewById(R.id.arrivalCode);
        displayDTime = findViewById(R.id.departureTime);
        displayATime = findViewById(R.id.arrivalTime);
        displayStatus = findViewById(R.id.status);

        deleteBtn = findViewById(R.id.dltButton);
        trackBtn = findViewById(R.id.trackButton);
        editAtime = findViewById(R.id.changeAtime);
        editDtime = findViewById(R.id.changeDtime);



        //SQL lookup
        dbHelper mainDb = new dbHelper(this);
        Cursor results = mainDb.searchByNum(fNum);

        String flightNumber=null;
        String Fdate=null;
        String dep=null;
        String arr=null;
        String dTime=null;
        String aTime=null;
        Boolean active=false;
        ///grab details from result
        while(results.moveToNext()) {
            int index;
            index = results.getColumnIndexOrThrow("flightNum");
            flightNumber = results.getString(index);
            index = results.getColumnIndexOrThrow("date");
            Fdate = results.getString(index);
            index = results.getColumnIndexOrThrow("dep");
            dep = results.getString(index);
            index = results.getColumnIndexOrThrow("arr");
            arr = results.getString(index);
            index = results.getColumnIndexOrThrow("dTime");
            dTime = results.getString(index);
            index = results.getColumnIndexOrThrow("aTime");
            aTime = results.getString(index);
            index = results.getColumnIndexOrThrow("active");
            active = results.getInt(index) > 0;

        }
        displayStatus.setText("This flight is not being tracked");
        date.setText(Fdate);
        displayFlight.setText(flightNumber);
        displayDep.setText(dep);
        displayArr.setText(arr);
        displayDTime.setText(dTime);
        displayATime.setText(aTime);

            if (active) { //if flight is active, set the text as you
                displayStatus.setText("You are tracking this flight");
                results = mainDb.activeInfo(fNum);
                while (results.moveToNext()) { //checks if flight is in activeInfo table, and if it is uses the times from that table
                    int i;
                    i = results.getColumnIndexOrThrow("estDepTime");
                    dTime = results.getString(i);
                    if(!dTime.equals("null")){
                        displayDTime.setText(dTime);
                    }
                    i = results.getColumnIndexOrThrow("estArrTime");
                        aTime = results.getString(i);
                        if (!aTime.equals("null")){
                            displayATime.setText(aTime);
                        }
                }
            }





        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // delete data when clicked
                dbHelper maindb = new dbHelper(viewFlightInfo.this);
                maindb.deleteByNum(fNum); //deletes flight from itinerary
                maindb.deleteActive(fNum);//deletes flight from activeFlight
                maindb.close();
                Toast.makeText(viewFlightInfo.this, "Flight Deleted", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(viewFlightInfo.this,flightList.class); // launched flightlist activity
                startActivity(intent);

            }
        });


        trackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activeStart start = new activeStart(viewFlightInfo.this);
                start.start(fNum,true); //starts active start, in order to open the track activity. The active state is not changed
            }
        });

        editDtime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openTimePicker(true); //true indicates the departure time is being fetched

            }
        });

        editAtime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openTimePicker(false);
            }
        });

    }

    private void openTimePicker(boolean timeToChange) {
        Integer resultCode = 0;
        Intent intent = new Intent(this,timepicker.class);
        intent.putExtra("START",timeToChange);
        startActivityForResult(intent, resultCode); //starts timepicker, app returns to this activity once the time is received from the timepicker activity
    }//// opens the picker

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //fetches the result from the time picker
        Integer returnedHours = data.getIntExtra("HOURS",0);
        Integer returnedMins = data.getIntExtra("MINUTES",0);
        Integer start = data.getIntExtra("WOOPS",0); //if 1 its departure, 0 is arrival

        String hours = toDisplay(returnedHours);
        String mins = toDisplay(returnedMins);

        String returnedTime = (hours+":"+mins); //formats it for saving
        maindb = new dbHelper(this);

        if(start==1){ //checks if to save it into departure or arrival
            maindb.saveInfo(returnedTime,fNum,"dTime");
            displayDTime.setText(returnedTime);

        }else{
            maindb.saveInfo(returnedTime,fNum,"aTime");
            displayATime.setText(returnedTime);
        }
        maindb.close();

    }///Gets data back from picker(start)

    private String toDisplay(Integer time) {
        if (time <10){
            String rTime = ("0"+ (time));
            return rTime;
        }else{
            String rTime = Integer.toString(time);
            return rTime;
        }
    }///converts returned values into a good display format

}


