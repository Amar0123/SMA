package com.ag.apiaiapp;

import android.content.Context;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MyProfileActivity extends AppCompatActivity {
    String mCurrentUserId;
    private FirebaseAuth mAuth;

    TextInputLayout fname,lname,dname,email;
    Spinner ageSpin,genderSpin;

    Button updateBut;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);

        fname = (TextInputLayout)findViewById(R.id.firstName);
        lname = (TextInputLayout)findViewById(R.id.lastName);
        dname= (TextInputLayout)findViewById(R.id.displayName);
        email = (TextInputLayout)findViewById(R.id.email);
        ageSpin = (Spinner)findViewById(R.id.ageSpin);
        genderSpin= (Spinner)findViewById(R.id.genderSpin);
        updateBut= (Button) findViewById(R.id.updateBut);

        List age = new ArrayList<Integer>();
        for (int i = 6; i <= 100; i++) {
            age.add(Integer.toString(i) + " years old");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, age);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ageSpin.setAdapter(adapter);
        ageSpin.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                return false;
            }
        });

        ArrayAdapter<CharSequence> genderAdapter = ArrayAdapter.createFromResource(this,R.array.genderArray,android
                .R.layout.simple_spinner_item);
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genderSpin.setAdapter(genderAdapter );
        genderSpin.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                return false;
            }
        });

        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();
        FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Profile p = dataSnapshot.getValue(Profile.class);
                dname.getEditText().setText(p.getDname());
                fname.getEditText().setText(p.getFname());
                lname.getEditText().setText(p.getLname());
                email.getEditText().setText(p.getEmail());

                if(p.getGender().matches("")){
                    genderSpin.setSelection(0);
                }else if(p.getGender().matches("Male")){
                    genderSpin.setSelection(1);
                }else{
                    genderSpin.setSelection(2);
                }

                for (int i = 0; i < 11; i++){
                    if(ageSpin.getItemAtPosition(i).toString().matches(p.getAge())){
                        ageSpin.setSelection(i);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        updateBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(genderSpin.getSelectedItemPosition() == 0){
                    Toast.makeText(MyProfileActivity.this, "Enter your gender", Toast.LENGTH_SHORT).show();
                }else{
                    FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUserId).child("age").setValue(ageSpin.getSelectedItem().toString());
                    FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUserId).child("dname").setValue(dname.getEditText().getText().toString());
                    FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUserId).child("lname").setValue(lname.getEditText().getText().toString());
                    FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUserId).child("fname").setValue(fname.getEditText().getText().toString());
                    FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUserId).child("gender").setValue(genderSpin.getSelectedItem().toString());
                }
                Toast.makeText(MyProfileActivity.this, "Profile Updated", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
}
