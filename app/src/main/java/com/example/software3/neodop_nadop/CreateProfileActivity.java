package com.example.software3.neodop_nadop;

import android.content.Intent;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;

public class CreateProfileActivity extends AppCompatActivity implements View.OnClickListener {

    EditText username , age, phonenumber,typeofDisabled;
    Switch isDisabled;
    Button submitBtn;
    RadioGroup RGsex;
    RadioButton male,female;

    private FirebaseAuth mAuth;
    private FirebaseFirestore DB;
    private FirebaseUser user;
    private FirebaseDatabase FDB;

    String sex="male";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_profile);
        username = (EditText)findViewById(R.id.profile_name);
        age = (EditText)findViewById(R.id.profile_age);
        phonenumber=(EditText)findViewById(R.id.profile_phonenumber);
        typeofDisabled=(EditText)findViewById(R.id.profile_typeofdisabled);

        isDisabled = (Switch)findViewById(R.id.profile_isdisabled);

        RGsex = (RadioGroup)findViewById(R.id.toggle);
        male = (RadioButton)findViewById(R.id.male);
        female = (RadioButton)findViewById(R.id.female);



        submitBtn = (Button)findViewById(R.id.profile_submit);

        submitBtn.setOnClickListener(this);

        isDisabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    typeofDisabled.setVisibility(View.VISIBLE);
                }else
                    typeofDisabled.setVisibility(View.INVISIBLE);
            }
        });


        RGsex.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                    switch (checkedId){
                        case R.id.male:
                            sex = "male";
                            Toast.makeText(getApplicationContext(),sex,Toast.LENGTH_SHORT).show();
                            break;
                        case R.id.female:
                            sex = "female";
                            Toast.makeText(getApplicationContext(),sex,Toast.LENGTH_SHORT).show();
                            break;
                    }
            }
        });


    }

    public void submitProfile(){
        boolean disable = false;

        if(isDisabled.isChecked()){
            disable = true;
        }

        mAuth= FirebaseAuth.getInstance();
        DB = FirebaseFirestore.getInstance();
        user = mAuth.getCurrentUser();
        String userUid = mAuth.getUid();

        Intent intent = getIntent();

        UserProfile profile = new UserProfile(sex.toString(),username.getText().toString(),
                Integer.parseInt(age.getText().toString()),phonenumber.getText().toString(),disable,
                typeofDisabled.getText().toString(),  FirebaseInstanceId.getInstance().getToken());



        DB.collection("users").document(userUid).set(profile).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d("tag", "DocumentSnapshot successfully written!");

            }
        });

        if(disable == false) {
            Intent nextActivity = new Intent(this, HelperMainActivity.class);
            startActivity(nextActivity);
            finish();
        }else{
            Intent nextActivity = new Intent(this,DisabledMainActivity.class); //여기 장애인용으로 바꾸기
            startActivity(nextActivity);
            finish();
        }



    }



    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.profile_submit:
                submitProfile();
                break;
        }
    }
}
