package com.example.software3.neodop_nadop;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
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

import javax.annotation.Nullable;

public class GPSService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseDatabase FDB;

    boolean bool=true;
    private static final String LOGSERVICE = "#######";

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


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(LOGSERVICE, "onDestroy - Estou sendo destruido ");
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

