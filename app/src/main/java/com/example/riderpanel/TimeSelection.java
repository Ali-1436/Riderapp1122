package com.example.riderpanel;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class TimeSelection extends AppCompatActivity {
        TextView t1,t2,t3,t4,t5;
        ImageButton im1,im2,im3,im4;

        String s1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_selection2);
        t1=findViewById(R.id.text_10);
        t2=findViewById(R.id.text_9);
        t3=findViewById(R.id.text_8);
        t4=findViewById(R.id.textView10);
        im1=findViewById(R.id.imageButton5);
        im2=findViewById(R.id.imageButton6);
        im3=findViewById(R.id.imageButton7);
            im4=findViewById(R.id.downaerrow);



            im1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    s1=t1.getText().toString();
                    Intent in=new Intent(TimeSelection.this,BookingNo.class);
                    in.putExtra("code",s1);
                    startActivity(in);
                }
            });
        im2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                s1=t2.getText().toString();
                Intent in=new Intent(TimeSelection.this,BookingNo.class);
                in.putExtra("code",s1);
                startActivity(in);
            }
        });
        im3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                s1=t3.getText().toString();
                Intent in=new Intent(TimeSelection.this,BookingNo.class);
                in.putExtra("code",s1);
                startActivity(in);
            }
        });
//        im4.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                s1=t4.getText().toString();
//                Intent in=new Intent(TimeSelection.this,BookingNo.class);
//                in.putExtra("code",s1);
//                startActivity(in);
//            }
//        });


        im4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                s1=t4.getText().toString();
                Intent in=new Intent(TimeSelection.this,BookingNo.class);
                in.putExtra("code",s1);
                startActivity(in);
            }
        });
//        Toast.makeText( TimeSelection.this,s1,Toast.LENGTH_LONG).show();
//        Log.d("message",s1);

    }




}