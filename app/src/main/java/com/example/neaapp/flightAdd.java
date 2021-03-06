package com.example.neaapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.util.JsonReader;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Array;
import java.util.Calendar;

public class flightAdd extends AppCompatActivity {

    DatePickerDialog picker;
    Button openPicker;
    EditText flightNum;
    dbHelper maindb;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flight_add);


        Button saveBtn =  findViewById(R.id.addbtn);
        flightNum = findViewById(R.id.flightNumber);
        openPicker = findViewById(R.id.openPicker);

        openPicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Calendar cal = Calendar.getInstance();  // fetches current date
                int day = cal.get(Calendar.DAY_OF_MONTH);
                int month = cal.get(Calendar.MONTH);
                int year = cal.get(Calendar.YEAR);

                ///////Code to show date picker when text entry is clicked, and then to save the data in the text view
                picker = new DatePickerDialog(flightAdd.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                        openPicker.setText(i+"/"+i1+"/"+i2);
                    }
                },year,month,day);
                picker.show();
            }
        });



        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //search values for URL
                String fNum = flightNum.getText().toString().toUpperCase(); //fetches the entered flight number
                String date = openPicker.getText().toString();

                if(!fNum.equals("")){
                    String URL_TEXT = checkFnum(fNum); //sends the flight number to the checkfNum to check format
                    if (date.equals("Select Date")){
                        Toast.makeText(flightAdd.this, "Please enter date of flight", Toast.LENGTH_LONG).show();
                    }
                    else{
                        if (!URL_TEXT.equals("error")){
                            searchData(URL_TEXT,fNum,date);
                        }else {
                            Toast.makeText(flightAdd.this,"Flight Number you have entered is invalid",Toast.LENGTH_LONG).show();
                        }

                    }
                }else{
                    Toast.makeText(flightAdd.this, "Please Enter a flight number", Toast.LENGTH_LONG).show();
                }

            }
        });

    }

    public void searchData(final String URL_TEXT,final String fNum,final String date){

        new flightFetcher(this).execute(fNum,date,URL_TEXT); //Starts the flightFetcher async task
    }



    private String checkFnum(String fNum){
        String code = "";
        String number = "";
        String sCode = "Iata";
        //separates numbers from letters and then checks format

        char checkChar = fNum.charAt(2);

        if (Character.isAlphabetic(checkChar)){
            Toast.makeText(flightAdd.this,"Invalid flight number",Toast.LENGTH_LONG).show();
            String URL_TEXT = "error"; //returns error if the format is not an IATA callsign
            return URL_TEXT;
        }else{
            for(int i = 0, n = 2; i<n;i++){ //grabs the first two characters, storing the airline code part of the flight number
                char a = fNum.charAt(i);
                code = code+a;
            }
            for (int i =2,n=fNum.length();i<n;i++){ //grabs the number portion of the the flight number
                char a = fNum.charAt(i);
                number = number+a;
            }
            String URL_TEXT = ("airline"+sCode+"="+code+"&flightNumber="+number); //builds and returns the URL text for API lookup later
            return URL_TEXT;


        }
    }



}

