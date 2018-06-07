package com.example.software3.neodop_nadop;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class AcceptActivity extends Activity implements View.OnClickListener {

    FirebaseAuth mAuth;
    FirebaseUser mUser;
    Button mConfirm ,mCancel;
    String helpee_uid;
    String uid ;

    httpSendTask s;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_accept);

        mConfirm =(Button)findViewById(R.id.btnConfirm);
        mCancel = (Button)findViewById(R.id.btnCancel);
        mConfirm.setOnClickListener(this);
        mCancel.setOnClickListener(this);
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        Intent intent = getIntent();
        uid  = mUser.getUid().toString();
        helpee_uid = intent.getStringExtra("helpee_uid").toString();
        s = new httpSendTask();
       // s.execute();
        Log.d("실행됩니까?","실행됩니다.");
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.btnCancel:
                finish();
                break;
            case R.id.btnConfirm:
                Intent intent = getIntent();
                String message = intent.getStringExtra("help_info");
                Log.d("정보", message);
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



    public  class httpSendTask extends AsyncTask<String, Void, Void> {
        private boolean cancelled = false;

        @Override
        protected Void doInBackground(String... strings) {
            Intent intent = getIntent();

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
                    Log.e("받음", "받음");
                    connection.disconnect();
                } else if (connection.getResponseCode() == HttpURLConnection.HTTP_BAD_REQUEST) {
//                    Toast.makeText(getApplicationContext(), "다음 기회에", Toast.LENGTH_LONG).show();
                    connection.disconnect();
                    finish();
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }

    }
}
