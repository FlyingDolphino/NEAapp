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



public class flightList extends AppCompatActivity {

    dbHelper mainDb;
    TableLayout flightTable;
    Button logBook;
    Button newFlightBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flight_list);


        flightTable = findViewById(R.id.flightTable);
        newFlightBtn = findViewById(R.id.newFlightBtn);
        logBook = findViewById(R.id.logBtn);



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




        newFlightBtn.setOnClickListener(new View.OnClickListener() {
            @Override
           public void onClick(View view) {


                Intent intent  = new Intent(flightList.this, flightAdd.class);
                startActivity(intent);


        }
    });
        logBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(flightList.this,logbook.class);
                startActivity(intent);

            }
        });

}

private TableRow makeRow(final String Fnum, String dte){
    // builds the table that shows all flights in the table, and displays it to the users.
    TableRow firstRow = new TableRow(this);
    TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
    firstRow.setLayoutParams(layoutParams);

    Button viewInfo = new Button(this);
    viewInfo.setText(Fnum);
    viewInfo.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            //code to open next screen, passing flight number
            Intent intent = new Intent(flightList.this,viewFlightInfo.class);
            intent.putExtra("FLIGHT_NUMBER",Fnum);
            startActivity(intent);

        }
    });
    firstRow.addView(viewInfo,0);

    TextView date = new TextView(this);
    date.setText(dte);
    firstRow.addView(date,1);

    return firstRow;
}

}




