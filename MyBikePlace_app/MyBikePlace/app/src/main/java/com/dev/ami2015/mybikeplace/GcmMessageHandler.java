package com.dev.ami2015.mybikeplace;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.util.Objects;

/**
 * Created by root on 08/06/15.
 */
public class GcmMessageHandler extends IntentService{
    public final static String EXTRA_MESSAGE = "com.dev.ami2015.mybikeplace.EXTRA_MESSAGE";

    String mes;
    private Handler handler;
    public GcmMessageHandler() {
        super("GcmMessageHandler");
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        handler = new Handler();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        String message;

        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        mes = extras.getString("title");
        //showToast();
        Log.i("GCM", "Received : (" + messageType + ")  " + extras.getString("title"));

        GcmBroadcastReceiver.completeWakefulIntent(intent);
        message = extras.getString("the_message");

        Intent i = new Intent(this, PersonalActivity.class);

        if(Objects.equals(message, "ALARM"))
            i.putExtra(EXTRA_MESSAGE, "ALARM");
        else
            i.putExtra(EXTRA_MESSAGE, "OK");

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
        //build the notification object

        Notification notification = new Notification.Builder(this).
                setContentTitle("MyBP "+message).setAutoCancel(true).
                setSmallIcon(R.mipmap.ic_launcher).
                setContentText(message).build();


        //get the notification manager
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notification.setLatestEventInfo(this, "MyBP", "MyBP System Alarm Notification", contentIntent);
        //send the notification
        notificationManager.notify((int) System.currentTimeMillis(), notification);
    }


//    public void showToast(){
//        handler.post(new Runnable() {
//            public void run() {
//                Toast.makeText(getApplicationContext(),mes , Toast.LENGTH_LONG).show();
//            }
//        });
// }
}
