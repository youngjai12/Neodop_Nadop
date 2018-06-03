package com.example.software3.neodop_nadop;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.Rating;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
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

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;

public class DisabledMainActivity extends AppCompatActivity {

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
    TextView textView;
    Button callbtn;

    // ImageView userImage;
    CircleImageView userImage;
    Button userChangeImage,test;
    Bitmap bitmap;


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

              //  String uid ;
                //테스트용 uid 전달


                //필요한 도움의 종류 edittext로 받기
                AlertDialog.Builder dialog = new AlertDialog.Builder(DisabledMainActivity.this);

                dialog.setTitle("호출하기");
                dialog.setMessage("어떤 도움이 필요한지 간단히 적어주세요\n상호 동의 후 \n상대방에게 당신의 기본 정보와 위치, \n도움의 종류가 전달됩니다.");
//                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
//                        LinearLayout.LayoutParams.MATCH_PARENT,
//                        LinearLayout.LayoutParams.MATCH_PARENT);
                final EditText input = new EditText(DisabledMainActivity.this);
                //input.setLayoutParams(lp);
                dialog.setView(input);

                dialog.setPositiveButton("호출하기", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String message = input.getText().toString();
                        Log.d("message 전달",message);
                        Intent intent = new Intent(getApplicationContext(),ConnectedActivity.class);
                        String uid ="";


                        //테스트용 uid 전달 tkdgur5273@skku.edu, ray5273@naver.com
                        if(user.getUid().toString().equals("DFlLOW1GSVhtuSd6dO6tAn9n99B3")){
                            uid = "OC1sKS2ghKUp2wtns90uverlfQ22";
                        }else if(user.getUid().toString().equals("OC1sKS2ghKUp2wtns90uverlfQ22")){
                            uid = "DFlLOW1GSVhtuSd6dO6tAn9n99B3";
                        }
                        intent.putExtra("useruid",uid);
                        startActivity(intent);
                    }
                }).setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                dialog.show();

//                //Rating 받는 dialog
//                AlertDialog.Builder ratingbar = new AlertDialog.Builder(DisabledMainActivity.this);
//                View dialogView = getLayoutInflater().inflate(R.layout.ratingbar_dialog,null);
//                final TextView dialogText = dialogView.findViewById(R.id.dialogEt);
//                final RatingBar dialogRb = dialogView.findViewById(R.id.dialogRb);
//
//                ratingbar.setView(dialogView).setPositiveButton("확인", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        Toast.makeText(getApplicationContext(), dialogRb.getRating()+dialogText.getText().toString()+"",Toast.LENGTH_SHORT).show();
//                        dialog.dismiss();
//                    }
//                });
//                ratingbar.show();


            }
        });
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

}
