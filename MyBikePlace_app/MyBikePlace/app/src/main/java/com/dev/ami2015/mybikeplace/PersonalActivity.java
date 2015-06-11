package com.dev.ami2015.mybikeplace;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;
import java.util.Objects;


public class PersonalActivity extends ActionBarActivity {

    GoogleCloudMessaging gcm;
    String regid;
    String PROJECT_NUMBER = "80513371534";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // get the two extras containg credentials from the intent
        Intent intent = getIntent();
        String username = intent.getStringExtra(LoginActivity.EXTRA_USERNAME);

        setContentView(R.layout.activity_personal);

        // modify text view content
        TextView userID = (TextView) findViewById(R.id.userID);
        userID.setText(username);

        Intent intentPerAct = getIntent();
        String message = intentPerAct.getStringExtra(GcmMessageHandler.EXTRA_MESSAGE);
        String messageView = "";
        if(Objects.equals(message, "ALARM"))
        {
            messageView = "YOUR BIKE HAS BEEN MOVED";
            setContentView(R.layout.activity_personal);

            // modify text view content
            TextView textAlarm = (TextView) findViewById(R.id.textAlarm);
            textAlarm.setText(messageView);
            textAlarm.setTextColor(Color.RED);
        }
        else
        {
            messageView = "YOUR BIKE HAS BEEN SUCCESSFULLY DISLOCKED";
            setContentView(R.layout.activity_personal);

            // modify text view content
            TextView textAlarm = (TextView) findViewById(R.id.textAlarm);
            textAlarm.setText(message);
            textAlarm.setTextColor(Color.GREEN);
        }
        getRegId();
    }

    @Override
    protected void onResume() {
        super.onResume();

        String message = getIntent().getStringExtra(GcmMessageHandler.EXTRA_MESSAGE);
        String messageView = "";
        if(Objects.equals(message, "ALARM"))
        {
            setContentView(R.layout.activity_personal);
            messageView = "YOUR BIKE HAS BEEN MOVED";
            // modify text view content
            TextView textAlarm = (TextView) findViewById(R.id.textAlarm);
            textAlarm.setText(messageView);
            textAlarm.setTextColor(Color.RED);
        }
        else
        {
            messageView = "YOUR BIKE HAS BEEN SUCCESSFULLY DISLOCKED";
            setContentView(R.layout.activity_personal);

            // modify text view content
            TextView textAlarm = (TextView) findViewById(R.id.textAlarm);
            textAlarm.setText(message);
            textAlarm.setTextColor(Color.GREEN);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_personal, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //GCM REG ID REQUEST
    public void getRegId(){
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
                    }
                    regid = gcm.register(PROJECT_NUMBER);
                    msg = "Device registered, registration ID=" + regid;
                    Log.i("GCM", msg);


                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();

                }
                return msg;
            }

        }.execute(null, null, null);
    }
}
