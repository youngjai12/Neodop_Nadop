package com.example.software3.neodop_nadop;

import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import static android.support.constraint.Constraints.TAG;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    AcceptActivity mAcceptactivity = (AcceptActivity)AcceptActivity.mAcceptActivity;
    ConnectedActivity mConnectedActivity = (ConnectedActivity)ConnectedActivity.mConnectedActivity;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // ...

        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            //helper_uid,help_info
   //         String requestname = remoteMessage.getData().get("type");
   //         Log.d("type",requestname);
            if(remoteMessage.getData().get("type").equals("request")) {

                Intent intent = new Intent(getApplicationContext(), AcceptActivity.class);
                String uid = remoteMessage.getData().get("helpee_uid");
                String helpinfo = remoteMessage.getData().get("help_info");

                Log.d("string help", uid);
                Log.d("string helpinfo", helpinfo);

                intent.putExtra("help_info", helpinfo);
                intent.putExtra("helpee_uid", uid);
                startActivity(intent);
            }else if(remoteMessage.getData().get("type").equals("match_success")){
                String uid = remoteMessage.getData().get("helper_uid");
                Intent intent = new Intent(getApplicationContext(),ConnectedActivity.class);
//                Log.d("match_success",uid);
                intent.putExtra("useruid",uid);
                intent.putExtra("message","nothing");
                startActivity(intent);
            }else if(remoteMessage.getData().get("type").equals("canceledby_disabled")){
                    //상대방이 취소했습니다.
//                    Toast.makeText(getApplicationContext(),"상대방이 취소하셨습니다.",Toast.LENGTH_SHORT).show();

                    //켜져 있던 상태에서 받으므로 다시 켜야함
                    startService(new Intent(this,GPSService.class));
                    if(!mConnectedActivity.isFinishing())
                         mConnectedActivity.finish();
                    if(!mAcceptactivity.isFinishing())
                        mAcceptactivity.finish();
                    startActivity(new Intent(this,CancelNotificationActivity.class));


            }else if(remoteMessage.getData().get("type").equals("canceledby_helper")){
                //상대방이 취소했습니다.
//                    Toast.makeText(getApplicationContext(),"상대방이 취소하셨습니다.",Toast.LENGTH_SHORT).show();
                    if(!mConnectedActivity.isFinishing())
                     mConnectedActivity.finish();

                     startActivity(new Intent(this,CancelNotificationActivity.class));
            }


        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }
}
