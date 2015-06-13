package com.dev.ami2015.mybikeplace;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;

import android.content.SharedPreferences;
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
    // Make personale activity able to access User Settings file
    // Shared Preference file
    SharedPreferences userSettings = null;
    // Creating editor to write inside Preference File
    SharedPreferences.Editor userSettingsEditor = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // get the string extra containing username from the intent
        Intent intent = getIntent();
        String username = intent.getStringExtra(SignInActivity.EXTRA_USERNAME);

        setContentView(R.layout.activity_personal);

        // modify text view content
        TextView userID = (TextView) findViewById(R.id.userID);
        userID.setText(username);

        Intent intentPerAct = getIntent();
        String message = intentPerAct.getStringExtra(GcmMessageHandler.EXTRA_MESSAGE);
        if(Objects.equals(message, "ALARM"))
        {
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String messageView = "";
                    TextView textAlarm = (TextView) findViewById(R.id.textAlarm);
                    switch (which){
                        case DialogInterface.BUTTON_POSITIVE:
                            messageView = "YOUR BIKE HAS BEEN MOVED";
                            setContentView(R.layout.activity_personal);

                            // modify text view content
                            textAlarm.setText(messageView);
                            textAlarm.setTextColor(Color.RED);
                            break;

                        case DialogInterface.BUTTON_NEGATIVE:
                            messageView = "YOUR BIKE HAS BEEN SUCCESSFULLY DISLOCKED";
                            setContentView(R.layout.activity_personal);

                            // modify text view content
                            textAlarm.setText(messageView);
                            textAlarm.setTextColor(Color.GREEN);
                            break;
                    }
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Have you dislocked the bike?").setNegativeButton("No", dialogClickListener)
                    .setPositiveButton("Yes", dialogClickListener).show();

        }
        else
        {
            String messageView = "";
            messageView = "YOUR BIKE HAS BEEN SUCCESSFULLY DISLOCKED";
            setContentView(R.layout.activity_personal);

            // modify text view content
            TextView textAlarm = (TextView) findViewById(R.id.textAlarm);
            textAlarm.setText(message);
            textAlarm.setTextColor(Color.GREEN);
        }
        getRegId();

        // Accessing to shared preference file
        userSettings = this.getSharedPreferences(getString(R.string.USER_SETTINGS), Context.MODE_PRIVATE);
    }

//
//    @Override
//    protected void onResume() {
//        super.onResume();
//
//        String message = getIntent().getStringExtra(GcmMessageHandler.EXTRA_MESSAGE);
//        String messageView = "";
//        if(Objects.equals(message, "ALARM"))
//        {
//            setContentView(R.layout.activity_personal);
//            messageView = "YOUR BIKE HAS BEEN MOVED";
//            // modify text view content
//            TextView textAlarm = (TextView) findViewById(R.id.textAlarm);
//            textAlarm.setText(messageView);
//            textAlarm.setTextColor(Color.RED);
//        }
//        else
//        {
//            messageView = "YOUR BIKE HAS BEEN SUCCESSFULLY DISLOCKED";
//            setContentView(R.layout.activity_personal);
//
//            // modify text view content
//            TextView textAlarm = (TextView) findViewById(R.id.textAlarm);
//            textAlarm.setText(message);
//            textAlarm.setTextColor(Color.GREEN);
//        }
//
//
//    }

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

        switch(id){

            case R.id.action_restore_default_settings:
                // restore default settings preference file
                userSettingsEditor = userSettings.edit();
                userSettingsEditor.putString(getString(R.string.USER_USERNAME), null);
                userSettingsEditor.putString(getString(R.string.USER_PASSWORD), null);
                userSettingsEditor.putBoolean(getString(R.string.USER_REMEMBER_ME), false);
                userSettingsEditor.putBoolean(getString(R.string.USER_SKIP), false);
                userSettingsEditor.commit();
                Intent comeBackIntent = new Intent(this, SignInActivity.class);
                startActivity(comeBackIntent);
                return true;
            case R.id.action_clear_remember_me_checkbox:
                userSettingsEditor = userSettings.edit();
                userSettingsEditor.putBoolean(getString(R.string.USER_REMEMBER_ME), false);
                userSettingsEditor.commit();
                return true;
            case R.id.action_go_to_maps:
                Intent personalIntent = new Intent(this, MapsActivity.class);
                startActivity(personalIntent);
//            case R.id.action_clear_skip_checkbox:
//                userSettingsEditor = userSettings.edit();
//                userSettingsEditor.putBoolean(getString(R.string.USER_SKIP), false);
//                userSettingsEditor.commit();
//                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
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
