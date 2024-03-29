package com.example.software3.neodop_nadop;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener{

    private EditText mEmail,mPassword;
    private Switch isDisabled;
    private Button submit;
    private FirebaseFirestore DB;
    private FirebaseUser user;
    private FirebaseAuth mAuth;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mEmail = (EditText)findViewById(R.id.signup_email);
        mPassword = (EditText)findViewById(R.id.signup_password);

        submit = (Button)findViewById(R.id.signup_submit);
        submit.setOnClickListener(this);

    }



    public void SignUp(){

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

        mAuth.createUserWithEmailAndPassword(username,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                     if(task.isSuccessful()){
                         FirebaseUser user = mAuth.getCurrentUser();
                         user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>(){
                             @Override
                             public void onComplete(@NonNull Task<Void> task) {
                                 Toast.makeText(getApplicationContext(),"메일 인증을 해주세요", Toast.LENGTH_LONG).show();
                             }
                         });
                     }else{
                         Toast.makeText(getApplicationContext(),"인증 중에 에러가 발생했습니다.", Toast.LENGTH_LONG).show();
                     }

            }
        });



    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.signup_submit:
                SignUp();
                break;
        }
    }
}
