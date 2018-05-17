package com.example.software3.neodop_nadop;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ConnectedActivity extends AppCompatActivity implements OnMapReadyCallback{

            //GoogleMap 객체
            GoogleMap googleMap;
            MapFragment mapFragment;
            LocationManager locationManager;


            //Firebase 객체
            FirebaseAuth mAuth;
            FirebaseUser user;
            FirebaseDatabase FDB;
            DatabaseReference DB;


            //나의 위도 경도 고도
            double mLatitude;  //위도
            double mLongitude; //경도


            //위치 표시 마커
            Marker myPosition=null;
            Marker yourPosition = null;


            //취소 버튼
            Button cancel;


            @Override
            protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_disabled_main);
                locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);

                mAuth = FirebaseAuth.getInstance();
                user = mAuth.getCurrentUser();
                FDB = FirebaseDatabase.getInstance();
                DB = FirebaseDatabase.getInstance().getReference();

                cancel = (Button)findViewById(R.id.connected_cancel);

                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //여기서 취소버튼 누를시 장애인은 장애인 메인화면으로 , 비장애인은 비 장애인 메인 화면으로 돌아가게해주면됨
                    }
                });


                //GPS가 켜져있는지 체크
                if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                    //GPS 설정화면으로 이동
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    intent.addCategory(Intent.CATEGORY_DEFAULT);
                    startActivity(intent);
                    finish();
                }

                //마시멜로 이상이면 권한 요청하기
                if(Build.VERSION.SDK_INT >= 23){
                    //권한이 없는 경우
                    if(ContextCompat.checkSelfPermission(ConnectedActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                            ContextCompat.checkSelfPermission(ConnectedActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                        ActivityCompat.requestPermissions(ConnectedActivity.this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION , android.Manifest.permission.ACCESS_FINE_LOCATION} , 1);
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
            }

            //권한 요청후 응답 콜백
            @Override
            public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
                //ACCESS_COARSE_LOCATION 권한
                if(requestCode==1){
                    //권한받음
                    if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                        requestMyLocation();
                    }
                    //권한못받음
                    else{
                        Toast.makeText(this, "권한없음", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
            }

            //나의 위치 요청
            public void requestMyLocation(){
                if(ContextCompat.checkSelfPermission(ConnectedActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(ConnectedActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                    return;
                }
                //요청
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 5, locationListener);
            }

            //위치정보 구하기 리스너
            LocationListener locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    if(ContextCompat.checkSelfPermission(ConnectedActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                            ContextCompat.checkSelfPermission(ConnectedActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                        return;
                    }
                    //나의 위치를 한번만 가져오기 위해    //여기 지우면 계속 바뀌네 바꿔줄때마다
                    //   locationManager.removeUpdates(locationListener);

                    //위도 경도
                    mLatitude = location.getLatitude();   //위도
                    mLongitude = location.getLongitude(); //경도

                    //맵생성
                    SupportMapFragment mapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
                    //콜백클래스 설정
                    mapFragment.getMapAsync(ConnectedActivity.this);

                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) { Log.d("gps", "onStatusChanged"); }

                @Override
                public void onProviderEnabled(String provider) { }

                @Override
                public void onProviderDisabled(String provider) { }
            };

            //구글맵 생성 콜백
            @Override
            public void onMapReady(final GoogleMap googleMap) {
                //상대방의 위치 초기화
                final Position yourPos = null;




                //이 부분에서 상대방의 userUid를 가져와서 이 String 변수에 넣어주면 됩니다.
                String myUid = user.getUid();
                String yourUid = "상대방의 UID를 서버로 부터 받아서 여기에 입력";

                this.googleMap = googleMap;

                //지도타입 - 일반
                this.googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

                //나의 위치 설정
                LatLng position = new LatLng(mLatitude , mLongitude);


                //화면중앙의 위치 (나의 위치) 와 카메라 줌비율
                this.googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 15));

                Position myPos = new Position(mLatitude,mLongitude);


                //원래 나의 위치를 지움 새로운 marker를 설정하기 위함
                if(myPosition!=null){
                    myPosition.remove();
                }

                //나의 위치 변경될 때 마다 위치를 가져와서 지도에 표시
                myPosition = this.googleMap.addMarker(new MarkerOptions()
                        .position(position)
                        .title("현재 나의 위치"));

                //내 위치를 실시간 Database에 업데이트
                FDB.getReference("userposition").child(myUid).setValue(myPos);



                //상대방의 위치가 변경될 때 마다 위치를 가져와서 지도에 표시
                DB.child("userposition").child(yourUid).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        //상대의 Position data 받아옴
                        Position changedPos = dataSnapshot.getValue(Position.class);

                        //기존의 위치 삭제 -> 안해줄시 marker가 여러개 찍힘
                        if(yourPosition!=null)
                            yourPosition.remove();

                        //변경됨 위치 추가
                        yourPosition = googleMap.addMarker(new MarkerOptions().position(new LatLng(changedPos.getLatitude(),changedPos.getLongitude())).title("상대방의 위치"));
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


            }
}


