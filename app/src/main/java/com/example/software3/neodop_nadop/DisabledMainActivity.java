package com.example.software3.neodop_nadop;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.media.Rating;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;

public class DisabledMainActivity extends AppCompatActivity {


    public static boolean finished = true;

    //constant
    private static final int PICK_FROM_CAMERA = 0;
    private static final int PICK_FROM_ALBUM = 1;


    LocationManager locationManager;

    //Firebase fields
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private DatabaseReference mDatabase;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseStorage mFirebaseStorage;
    private StorageReference mStorageReference;
    private FirebaseFirestore mFireStore;


    //layouts
    private String imageFilePath;
    private Uri photoUri;
    TextView textView;
    Button callbtn;

    // ImageView userImage;
    CircleImageView userImage;
    Button userChangeImage,test;
    Bitmap bitmap;

    //value
    double mLatitude;
    double mLongitude;

    //asynctask
    httpSendTasks s;


    @Override
    protected void onDestroy() {
        super.onDestroy();
        locationManager.removeUpdates(locationListener);
        // service 연결 해제
    }




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_disabled_main);


        //layouts
     //   textView = (TextView)findViewById(R.id.helper_lower_text);
        userImage = (CircleImageView)findViewById(R.id.disabled_image_profile);
        userChangeImage = (Button)findViewById(R.id.disabled_image_profile_change);
        test = (Button)findViewById(R.id.disabled_profile_change);
        callbtn = (Button)findViewById(R.id.disabled_call);
        textView = (TextView)findViewById(R.id.disabled_lower_text);


        //firebase user
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseStorage = FirebaseStorage.getInstance();
        mFireStore = FirebaseFirestore.getInstance();
        Log.d("token", FirebaseInstanceId.getInstance().getToken().toString());
        mFireStore.collection("users").document(user.getUid().toString()).update("token",FirebaseInstanceId.getInstance().getToken().toString());
      //  startService(new Intent(this,GPSServiceDisabled.class));

        s = new httpSendTasks();

        if(mFirebaseDatabase.getReference("userstatus")!=null) {
            mFirebaseDatabase.getReference("userstatus").child(user.getUid().toString()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                       //Rating 받는 dialog
                        final UserStatus us = dataSnapshot.getValue(UserStatus.class);
                        if (us != null) {
                            if (!us.getFinished()) {
                                final AlertDialog.Builder ratingbar = new AlertDialog.Builder(DisabledMainActivity.this);
                                View dialogView = getLayoutInflater().inflate(R.layout.ratingbar_dialog, null);
                                final TextView dialogText = dialogView.findViewById(R.id.dialogEt);
                                final RatingBar dialogRb = dialogView.findViewById(R.id.dialogRb);
                                ratingbar.setView(dialogView).setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
//                                        Toast.makeText(getParent(), dialogRb.getRating() + dialogText.getText().toString() + "", Toast.LENGTH_SHORT).show();
                                        s = new httpSendTasks();
                                        s.execute(dialogRb.getRating()+"/"+us.getYourUid().toString());


                                        //여기서 rating 추가
                                        DocumentReference docRef = mFireStore.collection("users").document(us.getYourUid().toString());
                                        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                if(task.isSuccessful()){
                                                    DocumentSnapshot ds = task.getResult();
                                                    double preRating = ds.getDouble("rating");
                                                    long numOfRaters = ds.getLong("numOfRaters");
                                                    numOfRaters++;
                                                    mFireStore.collection("users").document(us.getYourUid()).update("numOfRaters",numOfRaters);

                                                    double Rating = (preRating + dialogRb.getRating())/(double)numOfRaters;
                                                    mFireStore.collection("users").document(us.getYourUid()).update("rating",Rating);
                                                    Log.d("numOfRators and rating",numOfRaters+" and "+Rating);
                                                }
                                            }
                                        });
                                        mFireStore.collection("users").document(us.getYourUid().toString());


                                        mFirebaseDatabase.getReference("userstatus").child(user.getUid().toString()).child("finished").setValue(true);
                                        dialog.cancel();
                                    }
                                }).setNegativeButton("아직 안끝남", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Handler hd = new Handler();

                                        hd.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                String newString = Calendar.getInstance().getTime().toString();
                                                mFirebaseDatabase.getReference("userstatus").child(user.getUid().toString()).child("changeValue").setValue(newString);

                                            }
                                        } ,10000);
                                        //끝났는지 몇분뒤 다시 물어볼지를 여기서 결정

                                        dialog.cancel();
                                        //여기서 핸들러 써서
                                    }
                                });
                                Handler hd = new Handler();

                                hd.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {

                                        ratingbar.show();

                                    }
                                } ,2000);

                            }
                        }

                }


                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }



        locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);

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
            if(ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                    ){
                ActivityCompat.requestPermissions(DisabledMainActivity.this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION , android.Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.READ_EXTERNAL_STORAGE
                        ,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.CAMERA  } , 1);
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



        List<String> strings = new ArrayList<String>();
        strings.add("자신에게 한계를 설정하지 마라. 당신이 할 수 있다고 \n생각하는 만큼 당신은 할 수 있다. 기억해라. 당신이 \n할 수 있다고 믿는다면 당신은 반드시 이룰 것이다.\n\n\n" +
                "- Mary Kay Ash - ");
        strings.add("다른 사람들이 당신은 할 수 없다고 하는 것을 \n한 가지만 해봐라. 그러면 앞으로 \n당신은 그들이 설정한 한계를 신경쓰지 않게 될 것이다.\n" +
                "\n\n- Jame Cook -");
        strings.add("희망은 절대 당신을 버리지 않는다.\n 당신이 희망을 버릴 뿐이다. \n" +
                "\n\n- Richard Brickner -");
        strings.add("자기가 하는 일에 신념을 갖지 않으면 안 된다. \n그리고 누구나 자기가 하는 일이 좋다고 굳게 믿으면 \n힘이 생기는 법이다. \n" +
                "\n\n- Goethe -  \n");

        String[] arr =new String[strings.size()];
        arr = strings.toArray(arr);
        int a= (int)(Math.random()*4);
        textView.setText(arr[a]);


        callbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
     //           Toast.makeText(getApplicationContext(),"여기다가 action 추가해야",Toast.LENGTH_SHORT).show();

                Log.d("myuid",user.getUid().toString());
                Log.d("position",mLatitude+"::"+mLongitude);

                AlertDialog.Builder dialog = new AlertDialog.Builder(DisabledMainActivity.this);

                dialog.setTitle("호출하기");
                dialog.setMessage("어디서(실내라면 몇층인지도 알려주세요)\n어떤 도움이 필요한지 간단히 적어주세요\n상호 동의 후 \n상대방에게 당신의 기본 정보와 위치, \n도움의 종류가 전달됩니다.");

                final EditText input = new EditText(DisabledMainActivity.this);
                //input.setLayoutParams(lp);
                dialog.setView(input);

                dialog.setPositiveButton("호출하기", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String message = input.getText().toString();
                        Log.d("message 전달",message);
                       // Intent intent = new Intent(getApplicationContext(),ConnectedActivity.class);
                        Intent intent = new Intent(getApplicationContext(),WaitActivity.class);
                        String uid ="";
                        intent.putExtra("useruid",uid);
                        intent.putExtra("message",message);
                        double[] pos = {mLatitude,mLongitude};
                        intent.putExtra("position",pos);
                        startActivity(intent);

                    }
                }).setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                dialog.show();


            }
        });
        test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),CreateProfileActivity.class);
                startActivity(intent);
               // startActivity(new Intent(getApplicationContext(),AcceptActivity.class));

            }
        });


        //실행 권한 주기
        if(Build.VERSION.SDK_INT >= 23){
            //권한이 없는 경우(위치, 쓰기 , 읽기, 카메라 권한)
            if(ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                    ){
                ActivityCompat.requestPermissions(DisabledMainActivity.this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION , android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.READ_EXTERNAL_STORAGE
                        , android.Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA  } , 1);
            }
            //권한이 있는 경우
            else{

            }
        }
        //기존의 profile 불러오기
        if(user.getPhotoUrl() == null) {
            mStorageReference = mFirebaseStorage.getReference().child("default.png");
        }else if(user.getPhotoUrl() != null)
            mStorageReference = mFirebaseStorage.getReference().child(user.getUid()+".jpg");



        try {
            final File localFile = File.createTempFile("images", "jpg");
            mStorageReference.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    Log.d("successful",localFile.getName());
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
                        Log.d("orientation",exifOrientation+"");
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
        }catch (IOException e){
            e.printStackTrace();
        }



        //profile change를 위한 버튼
        userChangeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogInterface.OnClickListener cameralistener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        takePhotoAction();
                    }
                };
                DialogInterface.OnClickListener albumlistener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getAlbumAction();
                    }
                };
                DialogInterface.OnClickListener cancelListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                };
                new AlertDialog.Builder(DisabledMainActivity.this).setTitle("업로드할 이미지 선택").setPositiveButton("사진촬영",cameralistener).
                        setNeutralButton("취소",cancelListener).setNegativeButton("앨범선택",albumlistener).show();
            }
        });

    }




    //사진 촬영하여 profile update
    public void takePhotoAction(){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);


        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }

            if (photoFile != null) {
                photoUri = FileProvider.getUriForFile(this, getPackageName(), photoFile);

                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);

                startActivityForResult(takePictureIntent, PICK_FROM_CAMERA);
            }
        }

//        }

    }


    //사진을 앨범에서 가져와서 profile update
    public void getAlbumAction(){

        Intent intent = new Intent(Intent.ACTION_PICK);
        //앨범호출
        intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent,PICK_FROM_ALBUM);
    }



    //각각의 intent 마다 결과
    @Override
    public void onActivityResult(int requestCode,int resultCode,Intent data){
        super.onActivityResult(requestCode,resultCode,data);

        if(resultCode != RESULT_OK)
            return;

        switch (requestCode){

            case PICK_FROM_ALBUM: {
                sendPicture(data.getData());
                break;
            }

            case PICK_FROM_CAMERA:{
                //원하는 uri 로 변경후
                Uri uri = getImageUri(imageFilePath);
                //firebase profile storage 로 보냄
                sendToFirebase(uri);

                Bitmap bitmap = BitmapFactory.decodeFile(imageFilePath);
                ExifInterface exif = null;

                try {
                    exif = new ExifInterface(imageFilePath);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                int exifOrientation;
                int exifDegree;

                if (exif != null) {
                    exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                    exifDegree = exifOrientationToDegrees(exifOrientation);
                } else {
                    exifDegree = 0;
                }

                userImage.setImageBitmap(rotate(bitmap, exifDegree));
                break;
            }


        }


    }

    //사진을 앨범에서 가져와서 화면에 띄워주기
    public void sendPicture(Uri imgUri){

        String imgPath = getRealPathFromURI(imgUri);
        ExifInterface exif = null;
        try{
            exif = new ExifInterface(imgPath);
        }catch (IOException e){
            e.printStackTrace();
        }

        Log.d("album uri",imgUri.toString());
        //Firebase에 전송
        sendToFirebase(imgUri);

        Bitmap bitmap  = BitmapFactory.decodeFile(imgPath);
        int exifOrientation;
        int exifDegree;

        if (exif != null) {
            exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            exifDegree = exifOrientationToDegrees(exifOrientation);
        } else {
            exifDegree = 0;
        }

        userImage.setImageBitmap(rotate(bitmap, exifDegree));


    }


    //사진의 절대경로 가져오기
    private String getRealPathFromURI(Uri contentUri) {
        int column_index=0;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        if(cursor.moveToFirst()){
            column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        }

        return cursor.getString(column_index);
    }


    //이미지 파일 생성
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "TEST_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,      /* prefix */
                ".jpg",         /* suffix */
                storageDir          /* directory */
        );

        imageFilePath = image.getAbsolutePath();

        return image;
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


    //Firebase 에 프로필 사진과 profile 데이터 보내기
    private void sendToFirebase(Uri imgUri){
        String uid = user.getUid();
        StorageReference storageRef = mFirebaseStorage.getReference();

        if(user.getPhotoUrl()!=null)
            storageRef.child(uid+".jpg").delete();

        StorageReference profileRef = storageRef.child(uid+".jpg");

        UploadTask upload = profileRef.putFile(imgUri);
        upload.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
        upload.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.d("successfully sent","good!!!");
            }
        });


        //프로필 파일의 파일 주소를 user profile에 추가
        UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder()
                .setDisplayName(user.getUid())
                .setPhotoUri(Uri.parse("gs://neodop-nadop.appspot.com/"+uid+".jpg"))
                .build();

        user.updateProfile(profileUpdate).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Log.d("successful","good");
                }
            }
        });
    }

    // absolutePath를 Uri형식으로 바꾸기
    private Uri getImageUri(String m_imagePath){
        Uri m_imgUri = null;
        File m_file;
        try {
            m_file = new File(m_imagePath);
            m_imgUri = FileProvider.getUriForFile(getApplicationContext(),getPackageName(),m_file);
        } catch (Exception p_e) {
        }
        return m_imgUri;
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
        if(ContextCompat.checkSelfPermission(DisabledMainActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(DisabledMainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            return;
        }
        //요청
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 5, locationListener);
    }

    //위치정보 구하기 리스너
    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if(ContextCompat.checkSelfPermission(DisabledMainActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(DisabledMainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
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


    public  class httpSendTasks extends AsyncTask<String, Void, Void> {
        private boolean cancelled = false;

        @Override
        protected Void doInBackground(String... strings) {
            //Intent intent = getIntent();
           // String myUid = user.getUid().toString();
            String[] ratnuid = strings[0].split("/");
            String rating = ratnuid[0];
            String yourUid = ratnuid[1];


            try {
                URL url = new URL("http://neodop-nadop.iptime.org/finishhelp");
                //URL url = new URL("http://localhost:8000/accepthelp");

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                connection.setDoOutput(true);
                connection.setDoInput(true);

                connection.setRequestMethod("POST");
                DataOutputStream dos = new DataOutputStream(connection.getOutputStream());


                dos.writeBytes("&helperuid=" + yourUid + "&rating="+rating +"&helpeeuid="+user.getUid().toString());

                connection.connect();
                Log.e("send position to server", rating + "앞: rating,  뒤: 상대 uid:" + yourUid);


                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    // Do whatever you want after the
                    // token is successfully stored on the server
                    Log.e("받음", "받음");
                    connection.disconnect();
                } else if (connection.getResponseCode() == HttpURLConnection.HTTP_BAD_REQUEST) {
//                    Toast.makeText(getApplicationContext(), "다음 기회에", Toast.LENGTH_LONG).show();
                    Log.d("오류", "bad_request");
                    connection.disconnect();

                    //       finish();
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

}
