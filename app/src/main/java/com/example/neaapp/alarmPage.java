package com.example.neaapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.Calendar;

public class alarmPage extends AppCompatActivity {
    //init display variables
    Button cancel;
    Button set;
    Button openPicker;
    Button openDate;
    TextView status;
    TextView status2;
    DatePickerDialog picker;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_page);

        //init UI elements
        cancel = findViewById(R.id.cancelButton);
        set = findViewById(R.id.setButton);
        openPicker = findViewById(R.id.openPicker);
        status = findViewById(R.id.statusText);
        status2=findViewById(R.id.statusText2);
        openDate = findViewById(R.id.datePickerOpener);

        openPicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openTimePicker();

            }
        });


        openDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final Calendar cal = Calendar.getInstance();  // fetches current date
                int day = cal.get(Calendar.DAY_OF_MONTH);
                int month = cal.get(Calendar.MONTH);
                int year = cal.get(Calendar.YEAR);
                picker = new DatePickerDialog(alarmPage.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        status2.setText(year+"/"+month+"/"+dayOfMonth);
                    }
                },year,month,day);
                picker.show();
            }
        });

        set.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // set alarms + notifcations
                String date = status2.getText().toString();
                String time = status.getText().toString();

                String[] dateChar = date.split("/");
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.YEAR, Integer.parseInt(dateChar[0]));
                calendar.set(Calendar.MONTH, Integer.parseInt(dateChar[1]));
                calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dateChar[2]));

                String[] timeChar = time.split(":");
                calendar.set(Calendar.HOUR_OF_DAY,Integer.parseInt(timeChar[0]));
                calendar.set(Calendar.MINUTE,Integer.parseInt(timeChar[1]));
                calendar.set(Calendar.SECOND,00);


                Intent intent = new Intent(alarmPage.this,Notification_reciever.class);
                intent.putExtra("condition","setNoti");
                PendingIntent pendingIntent = PendingIntent.getBroadcast(alarmPage.this,101,intent,0);

                AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                alarmManager.setExact(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(),pendingIntent);
                Toast.makeText(alarmPage.this, "Notification set", Toast.LENGTH_SHORT).show();

            }
        });
        
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
                Intent intent = new Intent(alarmPage.this,Notification_reciever.class);
                intent.putExtra("condition","setNoti");
                PendingIntent pendingIntent = PendingIntent.getBroadcast(alarmPage.this,101,intent,0);
                alarmManager.cancel(pendingIntent);
                Toast.makeText(alarmPage.this, "Alarm cancelled", Toast.LENGTH_SHORT).show();
                
            }
        });


    }

    private void openTimePicker() {
        Integer resultCode = 0;
        Intent intent = new Intent(this, timepicker.class);
        startActivityForResult(intent, resultCode); //starts timepicker, app returns to this activity once the time is received from the timepicker activity
    }


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

        status.setText(returnedTime);

    }


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
