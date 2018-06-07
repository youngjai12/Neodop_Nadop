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
import com.google.firebase.iid.FirebaseInstanceId;
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

public class HelperMainActivity extends AppCompatActivity implements View.OnClickListener {

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
    Switch swc;
    TextView textView;
    Button btn1,btn2,btn3,btn4;
   // ImageView userImage;
    CircleImageView userImage;
    Button userChangeImage,test;
    Bitmap bitmap;




    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_helper_main);

        //layouts
        textView = (TextView)findViewById(R.id.helper_lower_text);
        swc = (Switch)findViewById(R.id.help_enable);
        userImage = (CircleImageView)findViewById(R.id.helper_image_profile);
        userChangeImage = (Button)findViewById(R.id.helper_image_profile_change);
        test = (Button)findViewById(R.id.helper_profile_change);

        btn1 =(Button)findViewById(R.id.helper_lower_btn1);
        btn2 =(Button)findViewById(R.id.helper_lower_btn2);
        btn3 =(Button)findViewById(R.id.helper_lower_btn3);
        btn4 =(Button)findViewById(R.id.helper_lower_btn4);

        btn1.setOnClickListener(this);
        btn2.setOnClickListener(this);
        btn3.setOnClickListener(this);
        btn4.setOnClickListener(this);

        //사진을 원형으로 표시
//        userImage.setBackground(new ShapeDrawable(new OvalShape()));
      //  userImage.setClipToOutline(true);



        //firebase user
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseStorage = FirebaseStorage.getInstance();
        Log.d("token", FirebaseInstanceId.getInstance().getToken().toString());


        test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),CreateProfileActivity.class);
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

    @Override
    public void onClick(View v) {
            switch (v.getId()){
                case R.id.helper_lower_btn1:
                    btn1.setBackgroundResource(R.drawable.ic_btn_pressed);
                    btn2.setBackgroundResource(R.drawable.ic_btn);
                    btn3.setBackgroundResource(R.drawable.ic_btn);
                    btn4.setBackgroundResource(R.drawable.ic_btn);
                    String text = "시각장애인 도움팁 \n\n 1. 길을 안내할 때에는 숫자를 사용해서 \n정확하게 설명합니다. \n\n2.도로의 위험요소 같은 것들을 \n상세히 잘 설명해줍니다.\n\n" +
                            "3. 안내할 때 장애인이 수월하게 걸을 수 있도록 팔을 내주는것은 문제가 없으나 \n 시각장애인의 팔을 잡거나 끄는 행위는 실례되는 행위이므로 주의합시다\n\n" +
                            "4. 길 안내를 할 때에는 저기, 여기 와 같은 \n애매한 표현은 삼가도록 주의합니다.";
                    textView.setText(text);
                    break;
                case R.id.helper_lower_btn2:
                    btn2.setBackgroundResource(R.drawable.ic_btn_pressed);
                    btn1.setBackgroundResource(R.drawable.ic_btn);
                    btn3.setBackgroundResource(R.drawable.ic_btn);
                    btn4.setBackgroundResource(R.drawable.ic_btn);
                    String text1="청각장애인 도움팁 \n\n 1. 청각 장애인분들은 입모양을 보고 알아들을 수 있기 때문에 듣지 못 할 것이라 생각하고 , 함부로 말하지 않고, 언행상의 예의를 지켜주세요." +
                            "\n\n 2. 청각장애인에게 몸짓, 표정, 입모양은 매우 중요하므로, 마주보며 입모양을 뚜렷하게 말하면 대부분 알아들을 수 있습니다.\n\n 3. 그렇지만 과장된 얼굴표현과 몸동작을 할 필요는 없습니다.";
                    textView.setText(text1);
                    break;
                case R.id.helper_lower_btn3:
                    btn3.setBackgroundResource(R.drawable.ic_btn_pressed);
                    btn1.setBackgroundResource(R.drawable.ic_btn);
                    btn2.setBackgroundResource(R.drawable.ic_btn);
                    btn4.setBackgroundResource(R.drawable.ic_btn);

                    String text2 ="지체 장애인 도움 팁\n\n1. 휠체어를 탄 장애인이 계단을 오르려 할 경우, 앞으로 내려오는 것이 편한지,\n 뒤로 내려오는 것이 편한지를 먼저 물어보고 도울 수 있도록 주의합니다. " +
                            "\n\n2. 휠체어를 사용하지는 않는 보행장애인의 경우, 계단을 오르내릴 때, 단순히 팔을 잡는 것은 도움이 되지 않고, 허리를 부축하고 계단을 오르내릴 수 있도록 주의합니다. ";
                    textView.setText(text2);
                    break;
                case R.id.helper_lower_btn4:
                    btn4.setBackgroundResource(R.drawable.ic_btn_pressed);
                    btn1.setBackgroundResource(R.drawable.ic_btn);
                    btn2.setBackgroundResource(R.drawable.ic_btn);
                    btn3.setBackgroundResource(R.drawable.ic_btn);
                    String text3 ="발달 장애인 도움 팁\n\n1. 발달 장애인은 말의 발음이 불명확하고, 단어선택이 미숙한 경향이 있으므로, 주의 깊게 들어서 의사를 정확히 판단할 수 있도록 주의합니다." +
                            "\n\n2. 비장애인이 의사를 표현할 때, 발음을 분명히 하고, 쉬운 단어를 선택하며, 몸짓 등의 행동을 덧붙여 이해를 돕습니다." +
                            "\n\n3. 그리고 말을 할 때에 눈을 맞추며 얘기할 수 있도록 하고, 지능이 부족하지만, 생활연령에 맞게 존칭어를 사용합니다. ";
                    textView.setText(text3);

                    break;
            }
    }
}
