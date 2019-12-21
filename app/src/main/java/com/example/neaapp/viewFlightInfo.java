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
        ///grab details from result
        while(results.moveToNext()) {
            int index;
            index = results.getColumnIndexOrThrow("flightNum");
            String flightNumber = results.getString(index);
            index = results.getColumnIndexOrThrow("date");
            String Fdate = results.getString(index);
            index = results.getColumnIndexOrThrow("dep");
            String dep = results.getString(index);
            index = results.getColumnIndexOrThrow("arr");
            String arr = results.getString(index);
            index = results.getColumnIndexOrThrow("dTime");
            String dTime = results.getString(index);
            index = results.getColumnIndexOrThrow("aTime");
            String aTime = results.getString(index);
            index = results.getColumnIndexOrThrow("active");
            Boolean active = results.getInt(index) > 0;

            date.setText(Fdate);
            displayFlight.setText(flightNumber);
            displayDep.setText(dep);
            displayArr.setText(arr);
            displayDTime.setText(dTime);
            displayATime.setText(aTime);

            if (active){
                displayStatus.setText("You are tracking this flight");
            }else{
                displayStatus.setText("This flight is not being tracked");
            }
        }

        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // delete data when clicked
                dbHelper maindb = new dbHelper(viewFlightInfo.this);
                maindb.deleteByNum(fNum);
                maindb.close();
                Toast.makeText(viewFlightInfo.this, "Flight Deleted", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(viewFlightInfo.this,flightList.class);
                startActivity(intent);

            }
        });


        trackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activeStart start = new activeStart(viewFlightInfo.this);
                start.start(fNum,true); // logic here needs to change
            }
        });

        editDtime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openTimePicker(true);

            }
        });

        editAtime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String timeToChange= "arr";
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

        Integer returnedHours = data.getIntExtra("HOURS",0);
        Integer returnedMins = data.getIntExtra("MINUTES",0);
        Integer start = data.getIntExtra("WOOPS",0);

        String hours = toDisplay(returnedHours);
        String mins = toDisplay(returnedMins);

        String returnedTime = (hours+":"+mins);
        maindb = new dbHelper(this);

        if(start==1){
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


