package com.ag.apiaiapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ShopsActivity extends AppCompatActivity {
    private ProgressDialog mRegProgress;
    List<Data> datas;
    ListView lv;
    DatabaseReference ref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shops);

        mRegProgress = new ProgressDialog(this);

        datas = new ArrayList<>();

        lv = (ListView)findViewById(R.id.lv);

        final String productType = getIntent().getStringExtra("productType").toLowerCase();
        String mallName = getIntent().getStringExtra("mallName").toLowerCase();

        // Get a reference to our posts
        FirebaseApp.initializeApp(this);
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        ref = database.getReference("SMA/"+ mallName +"/Shops/");

        mRegProgress.setTitle("Retrieving Shops");
        mRegProgress.setCanceledOnTouchOutside(false);
        mRegProgress.show();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                mRegProgress.dismiss();

                ref.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        datas.clear();

                        for (DataSnapshot ss : dataSnapshot.getChildren()){
                            if(ss.child("tag").getValue().toString().toLowerCase().contains(productType.toString())){
                                Data d = ss.getValue(Data.class);

                                datas.add(d);
                            }
                        }

                        if(datas.size() == 0){
                            Toast.makeText(ShopsActivity.this, "No shops found", Toast.LENGTH_SHORT).show();
                        }else{
                        }

                        ShopCustomAdapter artistAdapter = new ShopCustomAdapter(ShopsActivity.this, datas);
                        //attaching adapter to the listview
                        lv.setAdapter(artistAdapter);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        System.out.println("The read failed: " + databaseError.getCode());
                    }
                });
            }
        }, 2000);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(datas.get(position).getShoplink()));
                startActivity(browserIntent);
            }
        });
    }
}
