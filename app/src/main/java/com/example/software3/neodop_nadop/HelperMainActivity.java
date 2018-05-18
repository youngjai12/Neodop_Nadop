package com.example.software3.neodop_nadop;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class HelperMainActivity extends AppCompatActivity  {
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private DatabaseReference mDatabase;
    Switch swc;

    TextView textView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_helper_main);

        swc = (Switch)findViewById(R.id.help_enable);


        mAuth = FirebaseAuth.getInstance();
        textView = (TextView)findViewById(R.id.textView2);
        String isDisable = "TRUE";
        mDatabase = FirebaseDatabase.getInstance().getReference();
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        if(Build.VERSION.SDK_INT >= 23){
            //권한이 없는 경우
            if(ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(HelperMainActivity.this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION , android.Manifest.permission.ACCESS_FINE_LOCATION} , 1);
            }
            //권한이 있는 경우
            else{

            }
        }
        //마시멜로 아래
        else{
        }


        final String userUid = mAuth.getUid();


        swc.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    Intent intent = new Intent(HelperMainActivity.this,GPSService.class);
                    startService(intent);
                }else{
                    Intent intent = new Intent(HelperMainActivity.this,GPSService.class);
                    stopService(intent);

                    //앱을 완전히 껐다가 켰을때 꺼진거 처럼 보이게 초기화가 되어있어서 불편함
                    //나중에 수정하기
                }
            }
        });


    }

}
