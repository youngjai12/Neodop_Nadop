package com.example.software3.neodop_nadop;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.media.ExifInterface;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public class HelperMainActivity extends AppCompatActivity  {

    //constant
    private static final int PICK_FROM_CAMERA = 0;
    private static final int PICK_FROM_ALBUM = 1;
    private static final int CROP_FROM_IMAGE = 2;



    //Firebase fields
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private DatabaseReference mDatabase;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseStorage mFirebaseStorage;
    private StorageReference mStorageReference;
    //layouts

    private String imageFilePath;
    private Uri photoUri;
    //private URI picUri;

    Switch swc;
    TextView textView;
   // ImageView userImage;
    CircleImageView userImage;
    Button userChangeImage,test;
    Bitmap bitmap;



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_helper_main);

        //layouts
      //  textView = (TextView)findViewById(R.id.textView2);
        swc = (Switch)findViewById(R.id.help_enable);
        userImage = (CircleImageView)findViewById(R.id.helper_image_profile);
        userChangeImage = (Button)findViewById(R.id.helper_image_profile_change);
        test = (Button)findViewById(R.id.helper_profile_change);

        //사진을 원형으로 표시
//        userImage.setBackground(new ShapeDrawable(new OvalShape()));
      //  userImage.setClipToOutline(true);


        //firebase user
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseStorage = FirebaseStorage.getInstance();


        test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),testActivity.class);
                startActivity(intent);

            }
        });


        //실행 권한 주기
        if(Build.VERSION.SDK_INT >= 23){
            //권한이 없는 경우(위치, 쓰기 , 읽기, 카메라 권한)
            if(ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                    ){
                ActivityCompat.requestPermissions(HelperMainActivity.this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION , android.Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.READ_EXTERNAL_STORAGE
              ,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.CAMERA  } , 1);
            }
            //권한이 있는 경우
            else{

            }
        }
        //마시멜로 아래
        else{
        }



        //버그 수정
        if(isServiceRunningCheck()){
            swc.setChecked(true);
        }


        //스위치 on/off시 위치 정보를 보내기/보내지 않기
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
                new AlertDialog.Builder(HelperMainActivity.this).setTitle("업로드할 이미지 선택").setPositiveButton("사진촬영",cameralistener).
                        setNeutralButton("취소",cancelListener).setNegativeButton("앨범선택",albumlistener).show();
            }
        });






    }

    //switch on 후 어플 종료후 다시 시작 할 때 필요
    public boolean isServiceRunningCheck() {
        ActivityManager manager = (ActivityManager) this.getSystemService(Activity.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if ("com.example.software3.neodop_nadop.GPSService".equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
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
}
