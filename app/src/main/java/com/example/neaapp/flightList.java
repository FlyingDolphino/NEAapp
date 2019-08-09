package com.example.neaapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.JsonReader;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class flightList extends AppCompatActivity {

    dbHelper mainDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flight_list);


        TableLayout flightTable = findViewById(R.id.flightTable);


        mainDb = new dbHelper(this);
        Cursor results = mainDb.flightGetter();

        while(results.moveToNext()){
            int index;
            index = results.getColumnIndexOrThrow("flightNum");
            String flightNumber = results.getString(index);

            index = results.getColumnIndexOrThrow("date");
            String Fdate = results.getString(index);

            flightTable.addView(makeRow(flightNumber, Fdate));




        }

        Button newFlightBtn = findViewById(R.id.newFlightBtn);
        newFlightBtn.setOnClickListener(new View.OnClickListener() {
            @Override
           public void onClick(View view) {


                Intent intent  = new Intent(flightList.this, flightAdd.class);
                startActivity(intent);


        }
    });

}

private TableRow makeRow(String Fnum, String dte){

    TableRow firstRow = new TableRow(this);
    TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
    firstRow.setLayoutParams(layoutParams);

    Button viewInfo = new Button(this);
    viewInfo.setText(Fnum);
    firstRow.addView(viewInfo,0);

    TextView date = new TextView(this);
    date.setText(dte);
    firstRow.addView(date,1);

    return firstRow;
}

}




