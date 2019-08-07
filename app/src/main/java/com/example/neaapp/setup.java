package com.example.neaapp;

import android.content.Intent;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class setup extends AppCompatActivity {

    dbHelper mainDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup2);

        //buttons Pickers
        Button startTime = findViewById(R.id.startTime);
        startTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Boolean startOrEnd = true;
                openTimePicker(startOrEnd);


            }
        });

        Button endTime = findViewById(R.id.endTime);
        endTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Boolean startOrEnd = false;
                openTimePicker(startOrEnd);
            }
        });

        ////SQL save code here
        ///data for time is available in the text views
        ///name is available from name box

        Button save = findViewById(R.id.saveBtn);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /// grabbing data to be saved into variables
                EditText Dname = findViewById(R.id.UserName);
                TextView time1 = findViewById(R.id.startOutput);
                TextView time2 = findViewById(R.id.endOutput);
                String sTime = time1.getText().toString();
                String eTime = time2.getText().toString();
                String name = Dname.getText().toString();
                boolean Not = ((CheckBox) findViewById(R.id.notBox)).isChecked();

                saveData(name,sTime,eTime,Not);
                Intent intent = new Intent(setup.this, flightList.class);
                startActivity(intent);









            }
        });


    }

    private void saveData(String name,String sTime, String eTime, boolean Not ) {
        mainDb = new dbHelper(this);///open db
        boolean insertCheck = mainDb.insertData(name,sTime,eTime,Not);////insert values

        if (insertCheck == true){
            Toast.makeText(setup.this,"Data Saved",Toast.LENGTH_LONG).show();
        }else{
            Toast.makeText(setup.this,"An error in saving has occured",Toast.LENGTH_LONG).show();
        }

        mainDb.close();


    } /// saves SQL user settings

    private void openTimePicker(boolean startOrEnd) {
        Integer resultCode = 0;
        Intent intent = new Intent(this,timepicker.class);
        intent.putExtra("START",startOrEnd);

        startActivityForResult(intent, resultCode);


    }//// opens the picker

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        Integer returnedHours = data.getIntExtra("HOURS",0);
        Integer returnedMins = data.getIntExtra("MINUTES",0);
        Integer start = data.getIntExtra("WOOPS",0);

        ////turn to string to display

        String hours = toDisplay(returnedHours);
        String mins = toDisplay(returnedMins);



        String returnedTime = (hours+":"+mins);


        if (start ==1){
            TextView startOutput = findViewById(R.id.startOutput);
            startOutput.setText(returnedTime);

        }else{
            TextView endOutput = findViewById(R.id.endOutput);
            endOutput.setText(returnedTime);
        }



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
