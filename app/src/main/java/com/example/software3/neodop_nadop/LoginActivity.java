package com.example.software3.neodop_nadop;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.oob.SignUp;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;

import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */


    // UI references.
    private EditText mEmail;
    private EditText mPassword;
    private View mProgressView;
    private View mLoginFormView;
    private Button SignInBtn;
    private Button SignUpBtn;

    //파이어 베이스 변수
    FirebaseFirestore DB;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        /* 파이어베이스 코드 */
        FirebaseMessaging.getInstance().subscribeToTopic("news");
        FirebaseInstanceId.getInstance().getToken();



       // startActivity(new Intent(getApplicationContext(),SplashActivity.class));
        // Splash Activity 실행

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_main);


        // Set up the login form.
        mEmail = (EditText)findViewById(R.id.email);
        mPassword = (EditText)findViewById(R.id.password);
        mProgressView = (ProgressBar)findViewById(R.id.login_progress);
        SignInBtn = (Button)findViewById(R.id.sign_in_button);
        SignUpBtn = (Button)findViewById(R.id.sign_up_button);
        SignInBtn.setOnClickListener(this);
        SignUpBtn.setOnClickListener(this);


    }


    public void SignIn(){

        final String username = mEmail.getText().toString().trim();
        String password = mPassword.getText().toString().trim();

        mAuth = FirebaseAuth.getInstance();



  //      Log.e("email:  ",username);
  //      Log.e("Password:  ",password);
        if(username.isEmpty()){
            mEmail.setError("Email is required");
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(username).matches() && username.contains("@skku.edu")){
            mEmail.setError("Please enter a vaild email");
            mEmail.requestFocus();
            return;
        }


        if(password.isEmpty()){
            mPassword.setError("Password is required");
            mPassword.requestFocus();
            return;
        }

        if(password.length()<6){
            mPassword.setError("Minimum length of password should be 6");
            mPassword.requestFocus();
            return;

        }


        mAuth.signInWithEmailAndPassword(username,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                FirebaseUser user = mAuth.getCurrentUser();
                if(task.isSuccessful() && mAuth.getCurrentUser().isEmailVerified()){

                    DatabaseReference FDB = FirebaseDatabase.getInstance().getReference();
                    DB = FirebaseFirestore.getInstance();
                    final String userUid = mAuth.getUid();

                    DocumentReference docRef = DB.collection("users").document(userUid);

                    docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                               DocumentSnapshot userProfile = task.getResult();
                               if(task.isSuccessful() && userProfile.exists()){
                                   if(userProfile.get("disabled").equals("true")){
                                       //장애인 전용 메뉴 activity 실행
                                       Intent intent = new Intent(getApplicationContext(), DisabledMainActivity.class);
                                       startActivity(intent);
                                       finish();

                                   }else{
                                       //도우미 전용 메뉴 activity 실행
                                       Intent intent = new Intent(getApplicationContext(), HelperMainActivity.class);
                                       startActivity(intent);
                                       finish();
                                   }


                               }else{
                                   Toast.makeText(getApplicationContext(),"No Such Data",Toast.LENGTH_LONG).show();

                                   Intent intent = new Intent(getApplicationContext(),CreateProfileActivity.class);
                                   startActivity(intent);
                                   finish();

                                  }

                        }

                    });

                }else{
                    if(user == null){
                        Toast.makeText(getApplicationContext(),"등록되지 않은 유저입니다. 회원가입을 먼저 해 주세요",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if(!user.isEmailVerified()){
                        Toast.makeText(getApplicationContext(),"이메일을 보냈습니다. 이메일 인증을 먼저 해 주세요",
                                Toast.LENGTH_LONG).show();
                        FirebaseUser curUser = mAuth.getCurrentUser();
                        curUser.sendEmailVerification();
                    }

                }
            }
        });




    }





    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.sign_in_button:
                SignIn();
                finish();
                break;
            case R.id.sign_up_button:
                Intent intent = new Intent(getApplicationContext(),SignUpActivity.class);
                startActivity(intent);
                break;

        }
    }
}

