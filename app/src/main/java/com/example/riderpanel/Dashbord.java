package com.example.riderpanel;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

public class Dashbord extends AppCompatActivity {
    ImageButton bookride;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashbord);
        getSupportActionBar().hide();

        bookride=findViewById(R.id.imageButton2);


        bookride.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(Dashbord.this,Home_Screen.class));
            }
        });
    }
}