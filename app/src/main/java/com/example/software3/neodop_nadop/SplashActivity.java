package com.example.software3.neodop_nadop;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;

public class SplashActivity extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState){
        setContentView(R.layout.activity_splash);
        super.onCreate(savedInstanceState);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {

            @Override
            public void run() {
                finish();
            }
        }, 2500);// 3 초
    }


}
