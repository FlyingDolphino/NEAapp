package com.example.neaapp;

import android.content.Intent;


import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class setup extends AppCompatActivity {

    dbHelper mainDb;
    Button save;
    EditText nameBox;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup2);

        save = findViewById(R.id.saveBtn);
        nameBox = findViewById(R.id.UserName);

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = nameBox.getText().toString();

                if(!name.equals("")){ //checks if user has entered anything
                    mainDb = new dbHelper(setup.this);
                    Boolean check = mainDb.insertData(name);
                    if (check){ //if insertion succeeds then go back to main activity
                        Toast.makeText(setup.this, "Saved", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(setup.this,MainActivity.class);
                        startActivity(intent);
                        mainDb.close();
                    }else{
                        Toast.makeText(setup.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                    }

                }else{
                    Toast.makeText(setup.this, "Please enter your name", Toast.LENGTH_SHORT).show();
                }
            }
        });




    }
}
