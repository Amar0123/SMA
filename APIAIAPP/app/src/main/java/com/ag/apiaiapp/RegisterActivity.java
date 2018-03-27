package com.ag.apiaiapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    Button registerBut;
    TextInputLayout displayName, username, password;
    ProgressDialog progressDialog;
    private FirebaseAuth mAuth;
    DatabaseReference database;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();

        registerBut = (Button)findViewById(R.id.registerBut);
        displayName = (TextInputLayout)findViewById(R.id.displayName);
        username= (TextInputLayout)findViewById(R.id.username);
        password = (TextInputLayout)findViewById(R.id.password);

        progressDialog = new ProgressDialog(this);

        registerBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String displayStr = displayName.getEditText().getText().toString();
                String usernameStr = username.getEditText().getText().toString();
                String passwordStr = password.getEditText().getText().toString();

                if(TextUtils.isEmpty(displayStr) || TextUtils.isEmpty(usernameStr) || TextUtils.isEmpty(passwordStr)){
                    Toast.makeText(RegisterActivity.this, "Empty field", Toast.LENGTH_SHORT).show();
                }else{
                    progressDialog.setTitle("Registering User");
                    progressDialog.setMessage("Please wait while we create your account");
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.show();

                    registerUser(displayStr,usernameStr,passwordStr);
                }
            }
        });
    }

    private void registerUser(final String d, final String u, String p) {
        mAuth.createUserWithEmailAndPassword(u,p).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful() ){
                    sendVeri();//send verification email to user

                    //create connection to firebase realtime database for storing user data
                    database = FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid().toString());
                    HashMap<String, String> userMap = new HashMap();
                    userMap.put("dname", d);//displayname
                    userMap.put("fname", "");//firstname
                    userMap.put("lname", "");//lastname
                    userMap.put("email", u);
                    userMap.put("gender", "");
                    userMap.put("age", "");

                    database.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            mAuth.signOut();

                            Intent intent = new Intent(RegisterActivity.this,SplashActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        }
                    });
                }else{
                    progressDialog.hide();
                    Toast.makeText(RegisterActivity.this, "Cannot sign in. Please try again later.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void sendVeri(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null){
            user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(RegisterActivity.this, "Verification email sent", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }else{
                        Toast.makeText(RegisterActivity.this, "Verification email not sent", Toast.LENGTH_SHORT).show();
                        progressDialog.hide();
                    }
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(getApplicationContext(),SplashActivity.class);
        finish();
        startActivity(i);
        super.onBackPressed();
    }
}
