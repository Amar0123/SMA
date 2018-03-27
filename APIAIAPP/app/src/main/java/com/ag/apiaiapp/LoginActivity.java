package com.ag.apiaiapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity {

    Button loginBut,resetPWBut;
    ProgressDialog progressDialog;
    private FirebaseAuth mAuth;
    TextInputLayout username, password;

    public static final int PER_LOGIN = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);

        loginBut = (Button) findViewById(R.id.loginBut);
        resetPWBut = (Button) findViewById(R.id.resetPWBut);
        username = (TextInputLayout) findViewById(R.id.username);
        password = (TextInputLayout) findViewById(R.id.password);

        loginBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String usernameStr = username.getEditText().getText().toString();
                String passwordStr = password.getEditText().getText().toString();

                if (TextUtils.isEmpty(usernameStr) || TextUtils.isEmpty(passwordStr)) {
                } else {
                    progressDialog.setTitle("Logging In");
                    progressDialog.setMessage("Please wait while we check your credentials");
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.show();

                    loginUser(usernameStr, passwordStr);
                }
            }
        });

        resetPWBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetPW(username.getEditText().getText().toString());
            }
        });
    }

    public void resetPW(String email){
        if(username.getEditText().getText().toString().isEmpty()){
            Toast.makeText(this, "Enter your email", Toast.LENGTH_SHORT).show();
        }else{
            FirebaseAuth.getInstance().sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(LoginActivity.this, "Reset password email sent", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }else{
                        Toast.makeText(LoginActivity.this, "Reset password email not sent", Toast.LENGTH_SHORT).show();
                        progressDialog.hide();
                    }
                }
            });
        }
    }

    public void loginUser(final String u, String p) {
        mAuth.signInWithEmailAndPassword(u, p).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    try {
                        if (mAuth.getCurrentUser().isEmailVerified()) {
                            progressDialog.dismiss();

                            //sets a device token for the user
                            String currentUserID = mAuth.getCurrentUser().getUid();
                            String deviceToken = FirebaseInstanceId.getInstance().getToken();

                            FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID).child("device_token")
                                    .setValue(deviceToken).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                    finish();
                                }
                            });

                        } else {
                            Toast.makeText(LoginActivity.this, "Email not Verified", Toast.LENGTH_SHORT).show();
                            progressDialog.hide();
                            mAuth.signOut();
                        }
                    } catch (NullPointerException e) {
                    }
                } else {
                    progressDialog.hide();
                    Toast.makeText(LoginActivity.this, "Cannot sign in. Please try again later.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(getApplicationContext(),SplashActivity.class);
        finish();
        startActivity(i);
        super.onBackPressed();
    }
}