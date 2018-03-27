package com.ag.apiaiapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class SplashActivity extends AppCompatActivity {

    Button loginBut,registerBut;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_splash);

        loginBut = (Button)findViewById(R.id.loginBut);
        registerBut = (Button)findViewById(R.id.registerBut);

        loginBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mainIntent = new Intent(getApplicationContext(),LoginActivity.class);
                startActivity(mainIntent);
                finish();
            }
        });

        registerBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mainIntent = new Intent(getApplicationContext(),RegisterActivity.class);
                startActivity(mainIntent);
                finish();
            }
        });
    }
}