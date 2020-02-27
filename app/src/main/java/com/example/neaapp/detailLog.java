package com.example.neaapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.w3c.dom.Text;

public class detailLog extends AppCompatActivity {
    private dbHelper maindb;
    TableLayout table;
    Button main;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_log);

        table = findViewById(R.id.infoTable);
        main = findViewById(R.id.menuBtn);
        maindb = new dbHelper(this);
        //looks up all flights in the logbook table, and creates a tableview with all the information added
        Cursor lookup = maindb.logbook();
        while(lookup.moveToNext()){
            int i;
            i = lookup.getColumnIndexOrThrow("flightNum");
            String fNum = lookup.getString(i);
            i = lookup.getColumnIndexOrThrow("dep");
            String dep = lookup.getString(i);
            i = lookup.getColumnIndexOrThrow("arr");
            String arr = lookup.getString(i);
            i = lookup.getColumnIndexOrThrow("flightTime");
            String time = lookup.getString(i);
            i = lookup.getColumnIndexOrThrow("delay");
            String delay = lookup.getString(i);
            table.addView(makeRow(fNum,dep,arr,time,delay)); //rows are made by the makeRow method
            table.setStretchAllColumns(true); //sets table so all rows are stretched (makes it more readable)

        }

        main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(detailLog.this,flightList.class);
                startActivity(intent); //returns user to flightList page
            }
        });


    }

    private View makeRow(String fNum, String dep, String arr, String time, String delay) {

        // builds the table that shows all flights in the table, and displays it to the users.
        TableRow firstRow = new TableRow(this);
        TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
        firstRow.setLayoutParams(layoutParams);
        //initialises the textviews used to display the info for the table
        TextView flight = new TextView(this);
        TextView departure = new TextView(this);
        TextView arrival = new TextView(this);
        TextView flightTime = new TextView(this);
        TextView delayTime = new TextView(this);

        flight.setText(fNum);
        departure.setText(dep);
        arrival.setText(arr);
        flightTime.setText(time);
        delayTime.setText(delay);
        //sets text size for each item in table
        flight.setTextSize(20);
        departure.setTextSize(20);
        arrival.setTextSize(20);
        flightTime.setTextSize(20);
        delayTime.setTextSize(20);

        //adds each element to the row
        firstRow.addView(flight,0);
        firstRow.addView(departure,1);
        firstRow.addView(arrival,2);
        firstRow.addView(flightTime,3);
        firstRow.addView(delayTime,4);
        return firstRow;
    }
}
