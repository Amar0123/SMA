package com.ag.apiaiapp;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class IndiShopActivity extends AppCompatActivity {

    String shoplink;
    WebView web;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_indi_shop);

        web= (WebView) findViewById(R.id.webView);
        shoplink = getIntent().getStringExtra("shoplink").toString().replace("https","http");

        Toast.makeText(this, shoplink, Toast.LENGTH_SHORT).show();

        web.setWebViewClient(new WebViewClient());
        web.getSettings().setJavaScriptEnabled(true);
        web.getSettings().setBuiltInZoomControls(true);
        web.setHorizontalScrollBarEnabled(false);
        web.setVerticalScrollBarEnabled(true);
        web.loadUrl("https://www.jurongpoint.com.sg/store/304/black-hammer");
        web.setWebChromeClient(new WebChromeClient() );
    }
}
