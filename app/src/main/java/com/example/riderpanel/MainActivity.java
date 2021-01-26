package com.example.riderpanel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.riderpanel.Common.Common;
import com.example.riderpanel.Model.Model;
import com.firebase.ui.auth.AuthMethodPickerLayout;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private final static int LOGIN_REQUEST_CODE = 1007; //any value you want
    private List<AuthUI.IdpConfig> providers;
    private FirebaseAuth firebaseauth;
    private FirebaseAuth.AuthStateListener listner;

         FirebaseDatabase firebaseDatabase;
         DatabaseReference databaseReference;

    @Override
    protected void onStart() {
        super.onStart();
        displayscreen();
    }

    private void displayscreen() {
        getSupportActionBar().hide();

        Timer time = new Timer();
        time.schedule(new TimerTask() {
            @Override
            public void run() {
                firebaseauth.addAuthStateListener(listner);

            }
        },3000);

    }

    @Override
    protected void onStop() {
        if(firebaseauth != null && listner != null)
            firebaseauth.removeAuthStateListener(listner);

        super.onStop();


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();

    }


    private void init() {
//        ButterKnife.bind(this);
            firebaseDatabase=FirebaseDatabase.getInstance();
            databaseReference=firebaseDatabase.getReference(Common.RIDER_INFO_REFERENCE);

        providers = Arrays.asList(
                new AuthUI.IdpConfig.PhoneBuilder().build(),
                new AuthUI.IdpConfig.EmailBuilder().build());

        firebaseauth = FirebaseAuth.getInstance();
        listner = myfirebaseAuth -> {
            FirebaseUser user = myfirebaseAuth.getCurrentUser();
//
            Toast.makeText(this, "[ERROR]" , Toast.LENGTH_SHORT ).show();
            if(user != null) {
                checkuserfromFirebase();
            }
            else {

                showLoginLayout();
            }


        };

    }

    private void showLoginLayout() {

        AuthMethodPickerLayout authMethodPickerLayout = new AuthMethodPickerLayout
                .Builder(R.layout.layout_signin)
                .setPhoneButtonId(R.id.apple_signin_button)
                .setGoogleButtonId(R.id.googleSign)
                .build();

        startActivityForResult(AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAuthMethodPickerLayout(authMethodPickerLayout)
                .setIsSmartLockEnabled(false)
                .setTheme(R.style.LoginTheme)
                .setAvailableProviders(providers)
                .build(),LOGIN_REQUEST_CODE);





    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LOGIN_REQUEST_CODE)
        {
            IdpResponse responce = IdpResponse.fromResultIntent(data);
            if (resultCode == RESULT_OK)
            {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            }
            else
            {
                Toast.makeText(this, "[ERROR]" +responce.getError().getMessage(), Toast.LENGTH_SHORT ).show();
            }
        }
    }




    private void checkuserfromFirebase() {

        databaseReference.child(FirebaseAuth.getInstance().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (snapshot.exists())
                        {
                            Model model=snapshot.getValue(Model.class);
                            goToHomeActivity(model);

                        }else{

                            showRegisterLayout();
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private void showRegisterLayout() {

        AlertDialog.Builder builder= new AlertDialog.Builder(this,R.style.dialogtheme);
        View itemView= LayoutInflater.from(this).inflate(R.layout.layout_register,null);


        TextInputEditText editText=itemView.findViewById(R.id.Firstname);

        TextInputEditText editTextl=itemView.findViewById(R.id.Lastname);

        TextInputEditText editTextm=itemView.findViewById(R.id.mobile);

        Button submit =itemView.findViewById(R.id.submit);

        if (FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber() !=null && !TextUtils.isEmpty(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber()))
                editTextm.setText(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber());

                builder.setView(itemView);
                AlertDialog dialog =builder.create();
                dialog.show();
                submit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(TextUtils.isEmpty(editText.getText().toString())){
                            Toast.makeText(getApplicationContext(),"Please enter First Name",Toast.LENGTH_SHORT).show();
                            return;

                        }else if(TextUtils.isEmpty(editTextl.getText().toString())){
                            Toast.makeText(getApplicationContext(),"Please enter Last Name",Toast.LENGTH_SHORT).show();
                            return;

                        }else if(TextUtils.isEmpty(editTextm.getText().toString())){
                            Toast.makeText(getApplicationContext(),"Please enter Mobile number",Toast.LENGTH_SHORT).show();
                            return;

                        } else{

                            final Model model=new Model();
                            model.setFirstname(editText.getText().toString());
                            model.setLastname(editTextl.getText().toString());
                            model.setPhone(editTextm.getText().toString());

                            databaseReference.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .setValue(model)
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_SHORT).show();

                                        }
                                    })
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(getApplicationContext(),"Register Successfully",Toast.LENGTH_SHORT).show();
                                            goToHomeActivity(model);

                                        }
                                    });


                        }
                    }
                });
    }

    private void goToHomeActivity(Model model) {
        Common.currentRider =model;
      startActivity(new Intent(this,Home_Screen.class));
      finish();

    }


}