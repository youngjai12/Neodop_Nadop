package com.example.software3.neodop_nadop;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.annotation.Nullable;

public class GPSService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener  {
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseDatabase FDB;

    boolean bool=true;
    private static final String LOGSERVICE = "#######";
    httpSendTask s = new httpSendTask();

    @Override
    public void onCreate() {
        super.onCreate();
        buildGoogleApiClient();

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        FDB = FirebaseDatabase.getInstance();

        Log.i(LOGSERVICE, "onCreate");

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(LOGSERVICE, "onStartCommand");


        //
        //Toast.makeText(getApplicationContext(),"스타트!",Toast.LENGTH_SHORT).show();

        if (!mGoogleApiClient.isConnected())
            mGoogleApiClient.connect();
        return START_STICKY;



    }


    @Override
    public void onConnected(Bundle bundle) {
        Log.i(LOGSERVICE, "onConnected" + bundle);
        Location l =null;

        //int GPScheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        //Toast.makeText(getApplicationContext(),"연결됐니?",Toast.LENGTH_SHORT).show();

        //   l = LocationServices.getFusedLocationProviderClient(this).getLastLocation().addOnCompleteListener
        //   l = LocationServices.getFusedLocationProviderClient(this).getLastLocation().getResult();
        //   Toast.makeText(getApplicationContext(),"위도"+l.getLatitude()+"  경도"+l.getLongitude(),Toast.LENGTH_SHORT);



        if (l != null) {
            Log.i(LOGSERVICE, "lat " + l.getLatitude());
            Log.i(LOGSERVICE, "lng " + l.getLongitude());

        }

        startLocationUpdate();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(LOGSERVICE, "onConnectionSuspended " + i);

    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i(LOGSERVICE, "lat " + location.getLatitude());
        Log.i(LOGSERVICE, "lng " + location.getLongitude());
        LatLng mLocation = (new LatLng(location.getLatitude(), location.getLongitude()));
        Toast.makeText(getApplicationContext(),"위도"+ mLocation.latitude + "경도"+mLocation.longitude,Toast.LENGTH_SHORT).show();

        Position pos = new Position(location.getLatitude(),location.getLongitude());

        FDB.getReference("userposition").child(user.getUid()).setValue(pos);
        String la = ""+location.getLatitude();
        String lo = ""+location.getLongitude();
        String uid = user.getUid();
        String time = "";

        SimpleDateFormat formatter = new SimpleDateFormat ("yyyy-MM-dd hh:mm:ss");
        Calendar cal = Calendar.getInstance();
        String today = null;
        today = formatter.format(cal.getTime());
        Timestamp ts = Timestamp.valueOf(today);


        Log.e("시간?",""+ts.getTime()/1000);
        time += ts.getTime()/1000;

        //
        if(s!=null) {
            s.cancel(true);
            s= null;
            httpSendTask s = new httpSendTask();
            s.execute(la, lo, uid, time);//,uid,ts.toString());

            Log.e("if문에","dddd");
        }else{

            httpSendTask s = new httpSendTask();
            s.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,la, lo, uid, time);//,uid,ts.toString());
            Log.e("else문에","dddd");

//            s.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,la, lo, uid, time);//,uid,ts.toString());

        }

    }

    public  class httpSendTask extends AsyncTask<String, Void, Void> {
            private boolean cancelled = false;
        @Override
        protected Void doInBackground(String... strings) {

                if(isCancelled()){
                    return null;
                }
                Log.e("들어오긴하니?","dd");
                try {
                    URL url = new URL("http://neodop-nadop.iptime.org/updateloc");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                    connection.setDoOutput(true);
                    connection.setDoInput(true);

                    connection.setRequestMethod("POST");
                    DataOutputStream dos = new DataOutputStream(connection.getOutputStream());


                    dos.writeBytes("lat=" + Double.parseDouble(strings[0])+"lon="+Double.parseDouble(strings[1])+"uid="+strings[2]+"timestamp="+Long.parseLong(strings[3]));

                    connection.connect();
                    Log.e("send position to server",strings[0]+" "+strings[1]+" "+strings[2]+ " "+strings[3]);

/*
                    if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        // Do whatever you want after the
                        // token is successfully stored on the server
                        connection.disconnect();
                    }
*/
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            return null;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            cancelled = true;
        }

    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(LOGSERVICE, "onDestroy - Estou sendo destruido ");
//        s.onCancelled();
        stopLocationUpdate();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(LOGSERVICE, "onConnectionFailed ");

    }

    private void initLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(3000);
        mLocationRequest.setFastestInterval(1000); //여기의 시간을 조정하면 됨 // 변경될때 찾을 수 있는 최소 interval 주로 이걸 사용될듯
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    }

    private void startLocationUpdate() {
        initLocationRequest();

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }


    private void stopLocationUpdate() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);

    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addOnConnectionFailedListener(this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build();
    }

}


