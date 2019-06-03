package com.example.software3.neodop_nadop;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class AcceptActivity extends Activity implements View.OnClickListener {

    public static Activity mAcceptActivity;
   // AcceptActivity mAcceptactivity = (AcceptActivity)AcceptActivity.mAcceptActivity;

    FirebaseAuth mAuth;
    FirebaseUser mUser;
    Button mConfirm ,mCancel;
    String helpee_uid;
    String uid ;
    private FirebaseDatabase FDB;
    TextView tv;

    httpSendTask s;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_accept);


        FDB = FirebaseDatabase.getInstance();

        //activity 선언
        mAcceptActivity = AcceptActivity.this;

        mConfirm =(Button)findViewById(R.id.btnConfirm);
        mCancel = (Button)findViewById(R.id.btnCancel);
        mConfirm.setOnClickListener(this);
        mCancel.setOnClickListener(this);
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        Intent intent = getIntent();
        String message = intent.getStringExtra("help_info").toString();
        uid  = mUser.getUid().toString();
        helpee_uid = intent.getStringExtra("helpee_uid").toString();
        s = new httpSendTask();
       // s.execute();
        Log.d("실행됩니까?","실행됩니다.");

        //받은 도움 종류를 화면에 출력
        tv = (TextView)findViewById(R.id.accept_message);
        tv.setText(message);

        if(FDB.getReference().child(helpee_uid)!=null) {
            FDB.getReference("request").child(helpee_uid).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    UserRequest ur = dataSnapshot.getValue(UserRequest.class);
                    if(ur!=null) {
                        if ((!ur.getUid().toString().equals(uid)) && ur.isAccepted()) {
                            Log.d("다른사람이 이미 수락함","다른사람이 이미 수락함");
                            Toast.makeText(getApplicationContext(),"다른사람이 이미 수락하였습니다.",Toast.LENGTH_LONG).show();
                            if(!AcceptActivity.this.isFinishing())
                                finish();
                            UserRequest ll = new UserRequest(" ",false);
                            FDB.getReference("request").child(helpee_uid).setValue(ll);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

    }

    @Override
    public void onClick(View v) {

        switch(v.getId()){
            case R.id.btnCancel:
                if(!AcceptActivity.this.isFinishing())
                    finish();
                finish();
                break;
            case R.id.btnConfirm:
                UserRequest ur = new UserRequest(uid,true);
                FDB.getReference("request").child(helpee_uid).setValue(ur);
                //service를 종료시켜줌 나중에 확인하기!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                //잘 꺼지는듯 굿
                Intent closeintent = new Intent(this,GPSService.class);
                stopService(closeintent);
                ///////////////////////////////////////////////////////////////////////////////////////
                Intent intent = getIntent();
                String message = intent.getStringExtra("help_info");
                Log.d("정보", message);
                s = new httpSendTask();
                s.execute();
//                sendAccept();
                Intent newintent = new Intent(getApplicationContext(),ConnectedActivity.class);
                newintent.putExtra("useruid",helpee_uid);
                newintent.putExtra("message",message);
                startActivity(newintent);
            //    finish();
                Log.d("confirm 버튼누름","good");
                break;

        }
    }



    public  class httpSendTask extends AsyncTask<String, String, String> {
        private boolean cancelled = false;

        @Override
        protected String doInBackground(String... strings) {
            Intent intent = getIntent();
            String ret = "";
            Log.d("asynctask돌림",uid);

            try {
                URL url = new URL("http://neodop-nadop.iptime.org/accepthelp");
                //URL url = new URL("http://localhost:8000/accepthelp");

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                connection.setDoOutput(true);
                connection.setDoInput(true);

                connection.setRequestMethod("POST");
                DataOutputStream dos = new DataOutputStream(connection.getOutputStream());


                dos.writeBytes("&helperuid="+uid+"&helpeeuid="+helpee_uid);

                connection.connect();
                Log.e("send position to server", uid + ":" + helpee_uid);


                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    // Do whatever you want after the
                    // token is successfully stored on the server
                    ret = ""+HttpURLConnection.HTTP_OK;
                    Log.e("받음", "받음");
                    connection.disconnect();
                } else if (connection.getResponseCode() == HttpURLConnection.HTTP_BAD_REQUEST) {
//                    Toast.makeText(getApplicationContext(), "다음 기회에", Toast.LENGTH_LONG).show();
                    Log.d("이미 수락됨","이미 수락됨");
                    connection.disconnect();
                    ret = ""+HttpURLConnection.HTTP_BAD_REQUEST;
           //         finish();
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return ret;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String string) {
            super.onPostExecute(string);
        }

    }
}
