package com.example.riderpanel;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Timer;
import java.util.TimerTask;

public class Splash_Screen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash__screen);

        getSupportActionBar().hide();

        Timer time = new Timer();
        time.schedule(new TimerTask() {
            @Override
            public void run() {
                Intent inte =new Intent(getApplicationContext(),Login_screen.class) ;
                startActivity(inte);
                finish();
            }
        },3000);
    }
}