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

import org.w3c.dom.Text;

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

                if(!name.equals("null")){
                    mainDb = new dbHelper(setup.this);
                    Boolean check = mainDb.insertData(name);
                    if (check){
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
