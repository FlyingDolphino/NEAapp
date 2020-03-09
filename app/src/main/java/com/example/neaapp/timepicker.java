package com.example.neaapp;

import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TimePicker;

public class timepicker extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timepicker);
        // draws time picker, settings are set below. 24 hour view, once its changed the values are saved into Hour and mins
        TimePicker timePicker = findViewById(R.id.TimePicker);
        timePicker.setIs24HourView(true);

        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) { //when time is changed, update variables hour and mins
                 final Integer Hour = hourOfDay;
                 final Integer mins = minute;
                Button confirm = findViewById(R.id.conButton);
                confirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ///retrieve if start or end time is being edited
                        boolean start = getIntent().getBooleanExtra("START",false);
                        int check = 5;

                        if (start == true){
                            check = 1;
                        }else{
                            check = 0;
                        }
                        //store the times as extras to send back to activity that started timepicker
                        Intent data = new Intent();
                        data.putExtra("HOURS",Hour);
                        data.putExtra("MINUTES",mins);
                        data.putExtra("WOOPS",check);
                        setResult(RESULT_OK,data);
                        finish();
                    }
                });

            }
        });


    }
}
