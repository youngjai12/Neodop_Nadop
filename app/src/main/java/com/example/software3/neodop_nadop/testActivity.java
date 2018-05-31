package com.example.software3.neodop_nadop;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class testActivity extends AppCompatActivity  {

    //firebase instances
    FirebaseAuth mAuth;
    FirebaseUser user;
    private FirebaseStorage mFirebaseStorage;
    private StorageReference mStorageReference;

    //layouts and values
    String imgUrl;
    ImageView test ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);



        //firebase instances;
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        mFirebaseStorage = FirebaseStorage.getInstance();

        //imageView
        test = (ImageView)findViewById(R.id.test_image_profile);

        if(user.getPhotoUrl() == null) {
            mStorageReference = mFirebaseStorage.getReference().child("default.png");
        }else if(user.getPhotoUrl() != null)
            mStorageReference = mFirebaseStorage.getReference().child(user.getUid()+".jpg");


        try {
            final File localFile = File.createTempFile("images", "jpg");
            mStorageReference.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
//                    Log.d("successful",localFile.getName());

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
                        exifDegree = exifOrientationToDegrees(exifOrientation);
                    } else {
                        exifDegree = 0;
                    }

                    test.setImageBitmap(rotate(bitmap, exifDegree));
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });
        }catch (IOException e){
            e.printStackTrace();
        }


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

}
