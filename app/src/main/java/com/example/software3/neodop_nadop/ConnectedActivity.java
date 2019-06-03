package com.example.software3.neodop_nadop;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.load.resource.bitmap.BitmapDrawableResource;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;


//서로 수락시 연결될 Activity
public class ConnectedActivity extends AppCompatActivity implements OnMapReadyCallback{
            AcceptActivity mAcceptactivity = (AcceptActivity)AcceptActivity.mAcceptActivity;

            public static AppCompatActivity mConnectedActivity;


            boolean mymet = false;
            boolean yourmet = false;
            //disabled
            static boolean is_disabled ;
            //GoogleMap 객체
            GoogleMap googleMap;
            MapFragment mapFragment;
            LocationManager locationManager;

            boolean finished = false;
            boolean first = true;
            //Firebase 객체
            FirebaseAuth mAuth;
            FirebaseUser user;
            FirebaseDatabase FDB;
            DatabaseReference DB;
            FirebaseFirestore UDB;
            private StorageReference mStorageReference;
            private FirebaseStorage mFirebaseStorage;


            //나의 위도 경도 고도
            double mLatitude;  //위도
            double mLongitude; //경도


            //위치 표시 마커
            Marker myPosition=null;
            Marker yourPosition = null;


            //취소 버튼
            Button cancel,met;
            TextView profile,typeOfProfile;
            CircleImageView userImage;
            ProgressBar progressBar;
            //value
            String uid=null;
            String message=null;
            String myUid;
            //asynctask
            httpSendTask s ;


            @Override
            protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_connected);
                locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);

                //activity 선언
                mConnectedActivity = ConnectedActivity.this;

                s = new httpSendTask();



                mAuth = FirebaseAuth.getInstance();
                user = mAuth.getCurrentUser();
                FDB = FirebaseDatabase.getInstance();
                DB = FirebaseDatabase.getInstance().getReference();
                UDB = FirebaseFirestore.getInstance();
                mFirebaseStorage = FirebaseStorage.getInstance();


                //버그 수정용
//                mAcceptactivity.finish();
//                mAcceptactivity.finish();

                //intent 값 가져오기(상대방의 uid 가져오기) 및 상대방의 정보 가져오기
                //uid를 여기서 가져옴
                Intent intent1 = getIntent();
                uid = intent1.getStringExtra("useruid");
                message = intent1.getStringExtra("message");
                userImage = (CircleImageView)findViewById(R.id.connected_image_profile);

                //ProgressBar
                progressBar = (ProgressBar)findViewById(R.id.connected_progressbar);


                AlertDialog.Builder explanation = new AlertDialog.Builder(ConnectedActivity.this);
                View dialogView = getLayoutInflater().inflate(R.layout.marker_dialog,null);

                final ImageView marker1 = dialogView.findViewById(R.id.default_marker);
                final TextView dialogText1 = dialogView.findViewById(R.id.default_marker_text);
                final ImageView marker2 = dialogView.findViewById(R.id.new_marker);
                final TextView dialogText2 = dialogView.findViewById(R.id.new_marker_text);
                final TextView explanText = dialogView.findViewById(R.id.marker_explanation);
                explanation.setView(dialogView).setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                explanation.show();

                //상대방 profile 사진 불러오기
                if(!mFirebaseStorage.getReference().child(uid+".jpg").equals(null)) {
                    mStorageReference = mFirebaseStorage.getReference().child(uid +".jpg");

                    try {
                        final File localFile = File.createTempFile("images", "jpg");
                        mStorageReference.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                Log.d("successful", localFile.getName());
                                Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                                ExifInterface exif = null;

                                try {
                                    exif = new ExifInterface(localFile.getAbsolutePath());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                int exifOrientation;
                                int exifDegree;

                                if (exif != null) {

                                    exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                                    Log.d("orientation", exifOrientation + "");
                                    exifDegree = exifOrientationToDegrees(exifOrientation);
                                } else {
                                    exifDegree = 0;
                                }

                                userImage.setImageBitmap(rotate(bitmap, exifDegree));
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }




                profile = (TextView)findViewById(R.id.connected_profile_text);
                typeOfProfile = (TextView)findViewById(R.id.connected_typeOfProfile);

                if(message==null)
                    message ="상대방이 아무 정보도 입력하지 않았습니다.";
                Log.d("메세지",message);
                //상대방 프로필 정보 가져오기
                if(uid != null) {
                    if (uid.equals("")) {
                        Toast.makeText(getApplicationContext(), "상대방의 Uid가 인식되지 않습니다.", Toast.LENGTH_SHORT).show();
                    }
                    if (!uid.equals("")) {
                        DocumentReference docRef = UDB.collection("users").document(uid);
                        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot document = task.getResult();
                                    if (document.get("disabled").toString().equals("true")) {
                                        typeOfProfile.setText(" 이름   \n 성별   \n 전화번호  \n 장애종류  \n 도움종류  \n");
                                        profile.setText(document.get("name").toString() + "\n" + document.get("sex").toString() + "\n" + document.get("phoneNumber").toString() + "\n"
                                                + document.get("typeOfDisabled").toString() + "\n" + message + "\n");
                                        is_disabled = false;
                                    } else {
                                        typeOfProfile.setText(" 이름   \n 성별   \n 전화번호  ");
                                        profile.setText(document.get("name").toString() + "\n" + document.get("sex").toString() + "\n" + document.get("phoneNumber").toString());
                                        is_disabled = true;
                                    }
                                }
                            }

                            ;
                        });
                    }
                }
                cancel = (Button)findViewById(R.id.connected_cancel);
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //여기서 취소버튼 누를시 장애인은 장애인 메인화면으로 , 비장애인은 비 장애인 메인 화면으로 돌아가게해주면됨 -> finish()해주면 될듯
                        locationManager.removeUpdates(locationListener);
                        finished = true;
                        Log.d("finished",finished+"");
                        if(!is_disabled){
                            //버그 없애기
                            startService(new Intent(getApplicationContext(),GPSService.class));
                            mAcceptactivity.finish();
                        }
                        //여기서 끝났다는 신호를 보내줌
                        s.execute();
                        finish();
                    }
                });


                met = (Button)findViewById(R.id.connected_meet);
                met.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        myUid = user.getUid().toString();
                        //만났음을 전송함
                        FDB.getReference("userposition").child(myUid).child("met").setValue(true);
                        mymet = true;
                        locationManager.removeUpdates(locationListener);

                        if(mymet && yourmet) {
                            FDB.getReference("userposition").child(myUid).child("met").setValue(false);
                            finish();
                            if(!is_disabled) {
                                mAcceptactivity.finish();
                                startService(new Intent(getApplicationContext(),GPSService.class));
                            }
                            UserStatus us = new UserStatus("0",false,uid);
                            FDB.getReference("userstatus").child(myUid).setValue(us);
                        }
                    }
                });

                //상대방도 체크했는지
                DB.child("userposition").child(uid).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        //상대의 Position data 받아옴
                        Position pos = dataSnapshot.getValue(Position.class);
                        if(pos != null)
                            yourmet = pos.getMet();
                        Log.d("상대방의 것이",yourmet+"으로 바뀜");

                        if(mymet && yourmet) {
                            FDB.getReference("userposition").child(myUid).child("met").setValue(false);
                            finish();
                            if(!is_disabled) {
                                startService(new Intent(getApplicationContext(),GPSService.class));
                                mAcceptactivity.finish();
                            }else {
                                UserStatus us = new UserStatus("0",false,uid);
                                FDB.getReference("userstatus").child(myUid).setValue(us);
                       //         DisabledMainActivity.finished = false;
                       //         startActivity(new Intent(getApplicationContext(),DisabledMainActivity.class));
                            }
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finished=true;
        s.cancel(true);
        locationManager.removeUpdates(locationListener);


    }

    @Override
    protected void onPause() {
        super.onPause();
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
                    if(!finished)
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
             //   String yourUid = "상대방의 UID를 서버로 부터 받아서 여기에 입력";
                String yourUid = uid;
                this.googleMap = googleMap;

                //지도타입 - 일반
                this.googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                this.googleMap.getUiSettings().setZoomControlsEnabled(true);   //zoom 버튼 추가

                LatLng position;

                //progressbar 없애기
                progressBar.setVisibility(View.GONE);
                //나의 위치 설정
                position = new LatLng(mLatitude, mLongitude);
                this.googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position,15));
//                Log.d("변경값 ","변경값임");
                //원래 나의 위치를 지움 새로운 marker를 설정하기 위함
                if(myPosition!=null){
                    myPosition.remove();
                }

                //나의 위치 변경될 때 마다 위치를 가져와서 지도에 표시
                myPosition = this.googleMap.addMarker(new MarkerOptions()
                        .position(position)
                        .title("현재 나의 위치"));

                Position myPos = new Position(mLatitude,mLongitude,mymet);


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
                        if(changedPos != null)
                            yourPosition = googleMap.addMarker(new MarkerOptions().position(new LatLng(
                                    changedPos.getLatitude(), changedPos.getLongitude())).title("상대방의 위치").
                                    icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)));


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }



    //이미지 회전버그수정
    private int exifOrientationToDegrees(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
            return 270;
        }
        return 0;
    }

    //사진 회전시키기
    private Bitmap rotate(Bitmap bitmap, float degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    //marker size 줄이기
    public Bitmap resizeMapIcons(String iconName,int width, int height){
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(),getResources().getIdentifier(iconName, "drawable", getPackageName()));
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, width, height, false);
        return resizedBitmap;
    }

    public  class httpSendTask extends AsyncTask<String, Void, Void> {
        private boolean cancelled = false;

        @Override
        protected Void doInBackground(String... strings) {
            Intent intent = getIntent();
            String myUid = user.getUid().toString();
            String yourUid = uid;
            Log.d("취소하는 asynctask",uid);
            if(is_disabled) {
                try {
                    URL url = new URL("http://neodop-nadop.iptime.org/cancel_request");
                    //URL url = new URL("http://localhost:8000/accepthelp");

                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                    connection.setDoOutput(true);
                    connection.setDoInput(true);

                    connection.setRequestMethod("POST");
                    DataOutputStream dos = new DataOutputStream(connection.getOutputStream());


                    dos.writeBytes("&helperuid=" +yourUid + "&helpeeuid=" + myUid);

                    connection.connect();
                    Log.e("send position to server", myUid + "본인이 장애인인 경우" + yourUid);


                    if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        // Do whatever you want after the
                        // token is successfully stored on the server
                        Log.e("받음", "받음");
                        connection.disconnect();
                    } else if (connection.getResponseCode() == HttpURLConnection.HTTP_BAD_REQUEST) {
//                    Toast.makeText(getApplicationContext(), "다음 기회에", Toast.LENGTH_LONG).show();
                        Log.d("이미 수락됨", "이미 수락됨");
                        connection.disconnect();

                 //       finish();
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else{
                try {
                    URL url = new URL("http://neodop-nadop.iptime.org/cancel_help");
                    //URL url = new URL("http://localhost:8000/accepthelp");

                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                    connection.setDoOutput(true);
                    connection.setDoInput(true);

                    connection.setRequestMethod("POST");
                    DataOutputStream dos = new DataOutputStream(connection.getOutputStream());


                    dos.writeBytes("&helperuid=" + myUid + "&helpeeuid=" + yourUid);

                    connection.connect();
                    Log.e("send position to server", myUid + "본인이 비 장애인일 경우" + yourUid);


                    if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        // Do whatever you want after the
                        // token is successfully stored on the server
                        Log.e("받음", "받음");
                        connection.disconnect();
                    } else if (connection.getResponseCode() == HttpURLConnection.HTTP_BAD_REQUEST) {
//                    Toast.makeText(getApplicationContext(), "다음 기회에", Toast.LENGTH_LONG).show();
                        Log.d("이미 수락됨", "이미 수락됨");
                        connection.disconnect();

//                        finish();
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }

    }



}


