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
    //initialise variables
    dbHelper mainDb;
    TableLayout flightTable;
    Button logBook;
    Button newFlightBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flight_list);

        //initialise buttons and text views
        flightTable = findViewById(R.id.flightTable);
        newFlightBtn = findViewById(R.id.newFlightBtn);
        logBook = findViewById(R.id.logBtn);


        //open db connection, then call flightGetter() class which returns all flights in table
        mainDb = new dbHelper(this);
        Cursor results = mainDb.flightGetter();



        while(results.moveToNext()){ //goes through the returned results
            int index;
            index = results.getColumnIndexOrThrow("flightNum");
            String flightNumber = results.getString(index);
            index = results.getColumnIndexOrThrow("date");
            String Fdate = results.getString(index);
            flightTable.addView(makeRow(flightNumber, Fdate)); // calls the makeRow method, which creates a row to add to the flightTable
        }




        newFlightBtn.setOnClickListener(new View.OnClickListener() { //when button is clicked, start flightAdd.class activity
            @Override
           public void onClick(View view) {


                Intent intent  = new Intent(flightList.this, flightAdd.class);
                startActivity(intent);


        }
    });
        logBook.setOnClickListener(new View.OnClickListener() { //starts the logbook.class activity when clicked
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(flightList.this,logbook.class);
                startActivity(intent);

            }
        });

}

private TableRow makeRow(final String Fnum, String dte){
    // builds the row to be passed back to add to the table
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
    firstRow.addView(viewInfo,0); //adds button to row

    TextView date = new TextView(this);
    date.setText(dte);
    firstRow.addView(date,1); //adds date to row

    return firstRow;
}

}




