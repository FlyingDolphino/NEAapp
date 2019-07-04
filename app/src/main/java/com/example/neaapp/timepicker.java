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




        TimePicker timePicker = findViewById(R.id.TimePicker);

        timePicker.setIs24HourView(true);
        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                 final Integer Hour = hourOfDay;
                 final Integer mins = minute;



                Button comfirm = findViewById(R.id.conButton);
                comfirm.setOnClickListener(new View.OnClickListener() {
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
