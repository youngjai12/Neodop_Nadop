package com.example.software3.neodop_nadop;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.Toast;

import com.google.android.gms.maps.SupportMapFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class WaitActivity extends Activity {

    FirebaseAuth mAuth;
    FirebaseUser user;
    LocationManager locationManager;
    //나의 위도 경도 고도
    double mLatitude;  //위도
    double mLongitude; //경도
    httpSendTask s ;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_wait);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        s = new httpSendTask();


        //GPS가 켜져있는지 체크
        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            //GPS 설정화면으로 이동
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            startActivity(intent);
           // finish();
        }

        //마시멜로 이상이면 권한 요청하기
        if(Build.VERSION.SDK_INT >= 23){
            //권한이 없는 경우
            if(ContextCompat.checkSelfPermission(WaitActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(WaitActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(WaitActivity.this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION , android.Manifest.permission.ACCESS_FINE_LOCATION} , 1);
            }
            //권한이 있는 경우
            else{
                requestMyLocation();
            }
        }
        //마시멜로 아래
        else{
            requestMyLocation();
        }
        s.execute();



    }

    @Override
    protected void onStop() {
        super.onStop();
        s.onCancelled();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        s.onCancelled();
    }
    @Override
    protected void onResume(){
        super.onResume();

        //execute가아니라 다른거 실행해야 할 수도 있음
//        s.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }

    //위치정보 구하기 리스너
    LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if(ContextCompat.checkSelfPermission(WaitActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(WaitActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                return;
            }
            //나의 위치를 한번만 가져오기 위해    //여기 지우면 계속 바뀌네 바꿔줄때마다
            //   locationManager.removeUpdates(locationListener);

            //위도 경도
            mLatitude = location.getLatitude();   //위도
            mLongitude = location.getLongitude(); //경도

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) { Log.d("gps", "onStatusChanged"); }

        @Override
        public void onProviderEnabled(String provider) { }

        @Override
        public void onProviderDisabled(String provider) { }
    };

    //나의 위치 요청
    public void requestMyLocation(){
        if(ContextCompat.checkSelfPermission(WaitActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(WaitActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            return;
        }
        //요청
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 5, mLocationListener);
    }




    public  class httpSendTask extends AsyncTask<String, Void, Void> {
        private boolean cancelled = false;
        @Override
        protected Void doInBackground(String... strings) {
            String uid = user.getUid();
            Intent intent = getIntent();
            String message=intent.getStringExtra("message");


            try {
                URL url = new URL("http://neodop-nadop.iptime.org/requesthelp");
                //URL url = new URL("localhost:8000/requesthelp");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                connection.setDoOutput(true);
                connection.setDoInput(true);

                connection.setRequestMethod("POST");
                DataOutputStream dos = new DataOutputStream(connection.getOutputStream());
                String latitude =""+mLatitude;
                String longitude=""+mLongitude;
                dos.writeBytes("&lat=" + Double.parseDouble(latitude)+"&lon="+Double.parseDouble(longitude)+"&uid="+uid+"&info="+"hello");
                Log.d("latitude",latitude);
                Log.d("longitude",longitude);
                connection.connect();


                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    // Do whatever you want after the
                    // token is successfully stored on the server
                    connection.disconnect();
//                    Toast.makeText(getApplicationContext(),"잠시만 기다려 주세요",Toast.LENGTH_LONG).show();
//                    locationManager.removeUpdates(mLocationListener);
                    finish();
                }else if(connection.getResponseCode() == HttpURLConnection.HTTP_BAD_REQUEST){
//                    Toast.makeText(getApplicationContext(),"주변에 도우미가 없습니다.",Toast.LENGTH_LONG).show();
                    connection.disconnect();
 //                   locationManager.removeUpdates(mLocationListener);
                    finish();

                }
            }catch(MalformedURLException e){
                e.printStackTrace();
            }catch (IOException e){
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

    }


}
