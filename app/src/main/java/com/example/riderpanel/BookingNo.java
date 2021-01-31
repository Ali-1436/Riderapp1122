package com.example.riderpanel;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class BookingNo extends AppCompatActivity {
    EditText book1;
    Button cancel,booking;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_selection);
        cancel=findViewById(R.id.cancel_ride_text);
        book1=findViewById(R.id.editTextTextPersonName);
        booking=findViewById(R.id.booking);
       book1.setText(getIntent().getStringExtra("code"));



       cancel.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               startActivity(new Intent(BookingNo.this,Dashbord.class));
           }
       });
        booking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               String s1;
               s1=book1.getText().toString();
                Intent in=new Intent(BookingNo.this,book.class);
                in.putExtra("code1",s1);
                startActivity(in);
            }
        });
    }
}