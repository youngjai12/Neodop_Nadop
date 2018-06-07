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
    double preLatitude;
    double preLongitude;
    httpSendTask s ;
    double position[];



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_wait);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        s = new httpSendTask();
        Intent intent = getIntent();
        position = intent.getDoubleArrayExtra("position");
        Log.d("가져온 position",position[0]+"::"+position[1]);
        s.execute();
    }

    @Override
    protected void onStop() {
        super.onStop();
        s.cancel(true);

        //    s.onCancelled();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        s.cancel(true);
     //   s.onCancelled();
    }
    @Override
    protected void onResume(){
        super.onResume();

        //execute가아니라 다른거 실행해야 할 수도 있음
   //     s = new httpSendTask();
  //      s.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }


    public class httpSendTask extends AsyncTask<String, Void, Void> {
        private boolean cancelled = false;
        @Override
        protected Void doInBackground(String... strings) {
            String uid = user.getUid();
            Intent intent = getIntent();
            String message=""+intent.getStringExtra("message");


            try {
                URL url = new URL("http://neodop-nadop.iptime.org/requesthelp");
              //  URL url = new URL("http://localhost:8000/requesthelp");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                connection.setDoOutput(true);
                connection.setDoInput(true);

                connection.setRequestMethod("POST");
                DataOutputStream dos = new DataOutputStream(connection.getOutputStream());


                String latitude =""+position[0];
                String longitude=""+position[1];
                dos.writeBytes("&lat=" + Double.parseDouble(latitude) + "&lon=" + Double.parseDouble(longitude) + "&uid=" + uid + "&info=" + message);
                Log.d("보내는 position",position[0]+"::"+position[1]);

                connection.connect();


                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    // Do whatever you want after the
                    // token is successfully stored on the server
                    connection.disconnect();
//                    Toast.makeText(getApplicationContext(),"잠시만 기다려 주세요",Toast.LENGTH_LONG).show();
//                    locationManager.removeUpdates(mLocationListener);
                    Log.d("성공적으로 보냄","보냄");
                    finish();
                }else if(connection.getResponseCode() == HttpURLConnection.HTTP_BAD_REQUEST){
//                    Toast.makeText(getApplicationContext(),"주변에 도우미가 없습니다.",Toast.LENGTH_LONG).show();
                    Log.d("보내는데 실패 ","실패");
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
