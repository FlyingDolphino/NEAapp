package com.example.neaapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

public class viewFlightInfo extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_flight_info);
        // fetch flight number
        final String fNum = getIntent().getStringExtra("FLIGHT_NUMBER");

        //initialise text views & buttons
        TextView date = findViewById(R.id.date);
        TextView displayFlight = findViewById(R.id.displayFlightNumber);
        TextView displayDep = findViewById(R.id.departureCode);
        TextView displayArr = findViewById(R.id.arrivalCode);
        TextView displayDTime = findViewById(R.id.departureTime);
        TextView displayATime = findViewById(R.id.arrivalTime);
        TextView displayStatus = findViewById(R.id.status);

        Button deleteBtn = findViewById(R.id.dltButton);
        Button trackBtn = findViewById(R.id.trackButton);


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





    }
}
