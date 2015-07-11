package com.dev.ami2015.mybikeplace;

import android.app.AlertDialog;
import android.app.PendingIntent;
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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.dev.ami2015.mybikeplace.tasks.stop_alarm_fromApp;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.json.JSONException;
import org.json.JSONObject;

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

    // Creating link to view elements
    TextView welcomeMessage;
    public EditText myBPStationNumber;
    public EditText myBPStationPlace;
    public static final String MYBPSERVER_URL ="http://192.168.56.1:7000/myBP_server/users/stop_alarm_fromApp";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //set the flag to not change personal activity
        String messageView = "";
        TextView textStationNumber = (TextView) findViewById(R.id.bikeStatusStationNumber);


        // get the two extras containing credentials from the intent
        Intent intent = getIntent();
        String username = intent.getStringExtra(SignInActivity.EXTRA_USERNAME);

        setContentView(R.layout.activity_personal);

        welcomeMessage = (TextView) findViewById(R.id.welcomeMessage);
        myBPStationNumber = (EditText) findViewById(R.id.bikeStatusStationNumber);
        myBPStationPlace = (EditText) findViewById(R.id.bikeStatusPlaceNumber);

/////////////////////////////// PARTE TEST PER NOTIFICA //////////////////////////////////////////
        // modify text view content
//        TextView userID = (TextView) findViewById(R.id.userID);
//        userID.setText(username);

        String message = intent.getStringExtra(GcmMessageHandler.EXTRA_MESSAGE);
        createNotification(message);

        getRegId();

        //Creating shared preference file
        userSettings = this.getSharedPreferences(getString(R.string.USER_SETTINGS), Context.MODE_PRIVATE);
        //Setting welcome message with username

        welcomeMessage.setText("Welcome, " + userSettings.getString(getString(R.string.USER_USERNAME), null));

    }

    public void createNotification(String message){
        if (Objects.equals(message, "ALARM")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(PersonalActivity.this);
            builder.setMessage("Have you dislocked the bike?")
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            Toast.makeText(PersonalActivity.this, "System Alarm is running", Toast.LENGTH_LONG).show();
                            goToPersonalActivity(1);
                            }
                        })
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            Toast.makeText(PersonalActivity.this, "Bike Disloked", Toast.LENGTH_LONG).show();
                            //#########################################//
                            JSONObject obj =new JSONObject();
                            JSONObject objResponse= new JSONObject();
                            try {
                                obj.put("station_id", "1");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            try {
                                obj.put("place_id", "2");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            try {
                                obj.put("stop_alarm", "1");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            new stop_alarm_fromApp(PersonalActivity.this).execute(MYBPSERVER_URL, obj.toString());
                            }
                        });

            builder.show();

        } else if (Objects.equals(message, "OK")) {
            Toast.makeText(PersonalActivity.this, "Bike Correctly Disloked", Toast.LENGTH_LONG).show();
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

    public void setServerResponse(JSONObject serverResponse) throws JSONException{
        int alarm = 0;
        String stop_alarm = serverResponse.getString("stop_alarm");
        if(Objects.equals(stop_alarm, "1"))
            alarm = 1;
        else {
            // sign up completed successfully
            alarm = 0;
        }
        goToPersonalActivity(alarm);
    }

    public void goToPersonalActivity(int alarm)
    {
        Intent i = new Intent(this, PersonalActivity.class);
        if(alarm == 1)
        {
            myBPStationNumber.setText("BIKE ALARM");
            myBPStationNumber.setTextColor(Color.RED);
            myBPStationNumber.setKeyListener(null);
            myBPStationPlace.setKeyListener(null);
        }
        else {
            myBPStationNumber.setText("BIKE DISLOCKED");
            myBPStationNumber.setTextColor(Color.GREEN);
            myBPStationNumber.setKeyListener(null);
            myBPStationPlace.setKeyListener(null);
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
