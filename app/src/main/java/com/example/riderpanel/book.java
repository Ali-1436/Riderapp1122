package com.example.riderpanel;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class book extends AppCompatActivity {
        TextView t1;
        Button traknow;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book);

        traknow=findViewById(R.id.textView6);
        t1=findViewById(R.id.textView10);
        t1.setText(getIntent().getStringExtra("code1"));
        traknow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(book.this,Home_Screen.class));
            }
        });
    }
}