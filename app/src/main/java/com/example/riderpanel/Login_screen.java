package com.example.riderpanel;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class Login_screen extends AppCompatActivity {
    EditText username,password;
    Button submit,signUp;
    TextView textView,login;
    String username_1,password_1;
    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_screen);
        getSupportActionBar().hide();
        username= findViewById(R.id.username);
        password=findViewById(R.id.password);
        login=findViewById(R.id.login);
        signUp=findViewById(R.id.button2);
        textView=findViewById(R.id.textView3);


        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);

       login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exist_user();
            }
        });
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Login_screen.this,Sign_up.class));
            }
        });

    }



    public void exist_user(){

        username_1 = username.getText().toString().trim();
        password_1 = password.getText().toString().trim();

        if(TextUtils.isEmpty(username_1)){
            Toast.makeText(this,"Please enter email",Toast.LENGTH_LONG).show();
            return;
        }

        if(TextUtils.isEmpty(password_1)){
            Toast.makeText(this,"Please enter password",Toast.LENGTH_LONG).show();
            return;
        }
        progressDialog.setMessage("Registering Please Wait...");
        progressDialog.show();
        firebaseAuth.signInWithEmailAndPassword(username_1,password_1)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {

                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if(task.isSuccessful()){
                            Toast.makeText(Login_screen.this,"Successfully Login",Toast.LENGTH_LONG).show();

                            startActivity(new Intent(getApplicationContext(),Dashbord.class));

                            FirebaseUser currentUser= firebaseAuth.getCurrentUser();


                        }else{
                            Toast.makeText(Login_screen.this,"Login Error",Toast.LENGTH_LONG).show();



                        }
                        progressDialog.dismiss();
                    }
                });


    }

}