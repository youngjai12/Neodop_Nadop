package com.example.software3.neodop_nadop;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

public class SplashActivity extends Activity {

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseFirestore DB;

    //ffdfdfd
    @Override
    protected void onCreate(Bundle savedInstanceState){
        setTheme(R.style.AppTheme_Launcher);

        super.onCreate(savedInstanceState);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {

            @Override
            public void run() {
                mAuth = FirebaseAuth.getInstance();
                user  = mAuth.getCurrentUser();
                DB = FirebaseFirestore.getInstance();
                FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                        .setTimestampsInSnapshotsEnabled(true)
                        .build();
                DB.setFirestoreSettings(settings);
                routeToPage(user);
                finish();
            }
        }, 2500);// 1000당 1초
    }
    protected void routeToPage(FirebaseUser user){




        if(user != null){
            DocumentReference docRef = DB.collection("users").document(user.getUid());
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful()){
                        DocumentSnapshot document = task.getResult();
                        if(document!=null && document.exists()){
                            if(document.get("disabled").equals(true)){
                                Intent intent = new Intent (getApplicationContext(),DisabledMainActivity.class);
                                startActivity(intent);
                                //       finish();
                            }else{
                                Intent intent = new Intent (getApplicationContext(),HelperMainActivity.class);
                                startActivity(intent);
                                //     finish();
                            }
                        }else{
                            Toast.makeText(getApplicationContext(),"No Such Data",Toast.LENGTH_LONG).show();

                            Intent intent = new Intent(getApplicationContext(),CreateProfileActivity.class);
                            startActivity(intent);
                            finish();

                        }
                    }
                }
            });

        }else{
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }
    }

}
