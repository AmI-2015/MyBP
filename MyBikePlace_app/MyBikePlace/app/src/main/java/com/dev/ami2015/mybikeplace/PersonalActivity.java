package com.dev.ami2015.mybikeplace;

import android.app.Activity;
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
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.dev.ami2015.mybikeplace.tasks.GetUsersInfoTask;
import com.dev.ami2015.mybikeplace.tasks.MakeLockInRequest;
import com.dev.ami2015.mybikeplace.tasks.MakeLockOutRequest;
import com.dev.ami2015.mybikeplace.tasks.stop_alarm_fromApp;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.plus.model.people.Person;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;


public class PersonalActivity extends ActionBarActivity {

    public final static String EXTRA_STATION_ID = "com.dev.ami2015.mybikeplace.STATION_ID";
    GoogleCloudMessaging gcm;
    String regid;
    String PROJECT_NUMBER = "80513371534";
    // Make Personal activity able to access User Settings file
    // Shared Preference file
    SharedPreferences userSettings = null;
    // Creating editor to write inside Preference File
    SharedPreferences.Editor userSettingsEditor = null;

    // Creating link to view elements
    public TextView welcomeMessage;
    public TextView myPUBikeStatus;
    public EditText myBPStationNumber;
    public EditText myBPStationPlace;
    public String MYBPSERVER_ALARM_URL = null;
//   public static final String MYBPSERVER_ALARM_URL ="http://192.168.0.9:7000/myBP_server/users/stop_alarm_fromApp";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //set the flag to not change personal activity
        String messageView = "";
        TextView textStationNumber = (TextView) findViewById(R.id.bikeStatusStationNumber);

        MYBPSERVER_ALARM_URL = getResources().getString(R.string.IP_SERVER)+"/myBP_server/users/stop_alarm_fromApp";

        // get the two extras containing credentials from the intent
        Intent intent = getIntent();
        String username = intent.getStringExtra(SignInActivity.EXTRA_USERNAME);

        setContentView(R.layout.activity_personal);

        welcomeMessage = (TextView) findViewById(R.id.welcomeMessage);
        myPUBikeStatus = (TextView) findViewById(R.id.bikeStatusStatus);
        myBPStationNumber = (EditText) findViewById(R.id.bikeStatusStationNumber);
        myBPStationPlace = (EditText) findViewById(R.id.bikeStatusPlaceNumber);

/////////////////////////////// PARTE TEST PER NOTIFICA //////////////////////////////////////////
        // modify text view content
//        TextView userID = (TextView) findViewById(R.id.userID);
//        userID.setText(username);

        String message = intent.getStringExtra(GcmMessageHandler.EXTRA_MESSAGE);
        createNotification(message);

        //Creating shared preference file
        userSettings = this.getSharedPreferences(getString(R.string.USER_SETTINGS), Context.MODE_PRIVATE);

        //Setting welcome message with username
        welcomeMessage.setText("Welcome, " + userSettings.getString(getString(R.string.USER_USERNAME), null) + "!");

        //retrieve RegId code and MyPU info through MyBP server
        getRegId();

//        //Debug code begin
//        userSettingsEditor = userSettings.edit();
//        userSettingsEditor.putString(getString(R.string.USER_BIKE_STATION_ID), "25");
//        userSettingsEditor.commit();
//        //Debug code end


        //Retrieve MyPU status from MYBPSERVER
        new GetUsersInfoTask(this).execute();

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
                                String station_ID = userSettings.getString(getString(R.string.USER_BIKE_STATION_ID), "-1");
                                obj.put("station_id", station_ID);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            try {
                                String place_ID = userSettings.getString(getString(R.string.USER_BIKE_PLACE_ID), "-1");
                                obj.put("place_id", place_ID);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            try {
                                obj.put("stop_alarm", "1");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            new stop_alarm_fromApp(PersonalActivity.this).execute(MYBPSERVER_ALARM_URL, obj.toString());
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
                userSettingsEditor.putInt(getString(R.string.USER_STATUS), -1);
                userSettingsEditor.putString(getString(R.string.USER_USER_CODE), null);
                userSettingsEditor.putString(getString(R.string.USER_PWD_CODE), null);
                userSettingsEditor.putString(getString(R.string.USER_REGID), null);
                userSettingsEditor.putString(getString(R.string.USER_BIKE_STATION_ID), "-1");
                userSettingsEditor.putString(getString(R.string.USER_BIKE_PLACE_ID), "-1");
                //need to add NFC field
                userSettingsEditor.commit();
                Intent comeBackIntent = new Intent(this, SignInActivity.class);
                startActivity(comeBackIntent);
                return true;
            case R.id.action_clear_remember_me_checkbox:
                userSettingsEditor = userSettings.edit();
                userSettingsEditor.putBoolean(getString(R.string.USER_REMEMBER_ME), false);
                userSettingsEditor.commit();
                return true;
            case R.id.action_update_status:

//                //Debug code begin
//                userSettingsEditor = userSettings.edit();
//                userSettingsEditor.putInt(getString(R.string.USER_STATUS), 1);
//                userSettingsEditor.commit();
//                //Debug code end

                new GetUsersInfoTask(this).execute();
                return true;
            case R.id.action_go_to_maps:
                Intent personalIntent = new Intent(this, MapsActivity.class);
                personalIntent.putExtra(SignInActivity.EXTRA_CALL_FROM, "noBikeOnMap");
                startActivity(personalIntent);
                return true;
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
        //Intent i = new Intent(this, PersonalActivity.class);
        if(alarm == 1)
        {
            myPUBikeStatus.setText("BIKE ALARM");
            myPUBikeStatus.setTextColor(Color.RED);
            myBPStationNumber.setTextColor(Color.RED);
            myBPStationNumber.setEnabled(false);
            myBPStationPlace.setTextColor(Color.RED);
            myBPStationPlace.setEnabled(false);
        }
        else {

            new GetUsersInfoTask(this).execute();

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

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);

                //add RegId inside preference file
                SharedPreferences.Editor taskUserSettingsEditor = PersonalActivity.this.userSettings.edit();
                taskUserSettingsEditor.putString(getString(R.string.USER_REGID), regid);
                taskUserSettingsEditor.commit();

//                //retrieve MyPU info through MyBP server
//                new GetUsersInfoTask(PersonalActivity.this).execute();


            }
        }.execute(null, null, null);
    }

    //Invoked by GetUsersInfoTask on postExecute
    public void checkMyPUStatus(){

        //Catch data from updated preference file
        Integer MyPUStatus = userSettings.getInt(getString(R.string.USER_STATUS), -2 /*default value*/);
        String MyPUStationID = userSettings.getString(getString(R.string.USER_BIKE_STATION_ID), "-2" /*default value*/);
        String MyPUPlaceID = userSettings.getString(getString(R.string.USER_BIKE_PLACE_ID), "-2" /*default value*/);

        //Check Status value
        switch(MyPUStatus){
            case -1: //Error from server!
                Toast.makeText(PersonalActivity.this, "Connection Error, try again.", Toast.LENGTH_LONG).show();
                //without break to automatically set edittext field
            case -2: //Error inside app!
                myPUBikeStatus.setText("ERROR");
                myPUBikeStatus.setTextColor(getResources().getColor(R.color.red));
                myBPStationNumber.setText("error");
                myBPStationNumber.setTextColor(getResources().getColor(R.color.black));
                myBPStationNumber.setEnabled(true);
                myBPStationPlace.setText("error");
                myBPStationPlace.setTextColor(getResources().getColor(R.color.black));
                myBPStationPlace.setEnabled(true);
                break;
            case 0: //MyPU not locked-in
                myPUBikeStatus.setText("Not Locked-in");
                myPUBikeStatus.setTextColor(getResources().getColor(R.color.orange));
                myBPStationNumber.setText("");
                myBPStationNumber.setTextColor(getResources().getColor(R.color.black));
                myBPStationNumber.setEnabled(true);
                myBPStationNumber.setHint("null");
                myBPStationNumber.setHintTextColor(getResources().getColor(R.color.hint_foreground_material_light));
                myBPStationPlace.setText("");
                myBPStationPlace.setTextColor(getResources().getColor(R.color.black));
                myBPStationPlace.setEnabled(true);
                myBPStationPlace.setHint("null");
                myBPStationPlace.setHintTextColor(getResources().getColor(R.color.hint_foreground_material_light));
                break;
            case 1: //MyPU locked-in, in this condition user cannot modify value of Station id and Place id
                myPUBikeStatus.setText("Locked-in");
                myPUBikeStatus.setTextColor(getResources().getColor(R.color.green));
                myBPStationNumber.setText(MyPUStationID);
                myBPStationNumber.setTextColor(getResources().getColor(R.color.black));
                myBPStationNumber.setEnabled(false);
                myBPStationPlace.setText(MyPUPlaceID);
                myBPStationPlace.setTextColor(getResources().getColor(R.color.black));
                myBPStationPlace.setEnabled(false);
        }

    }

    public void LockInProcedure(View view){

        //Check current MyPU status: operation done only if user is not-locked in
        if( userSettings.getInt(getString(R.string.USER_STATUS), -1) != 1) {

            //Retrieve the Station Id and Place Id inserted by user
            String stationId = myBPStationNumber.getText().toString();
            String placeId = myBPStationPlace.getText().toString();

            if(stationId.equals("") || placeId.equals("")){
                //NO bike parking inserted
                Toast.makeText(PersonalActivity.this, "Insert Parking Data!", Toast.LENGTH_LONG).show();
                // allert user
                myBPStationNumber.setHint("null");
                myBPStationNumber.setHintTextColor(getResources().getColor(R.color.red));
                myBPStationPlace.setHint("null");
                myBPStationPlace.setHintTextColor(getResources().getColor(R.color.red));
            } else {

                //Make a Lock-In request to MYBPSERVER
                new MakeLockInRequest(this, stationId, placeId).execute();

            }

        } else {
            //bike already locked
            Toast.makeText(PersonalActivity.this, "Bike already Locked", Toast.LENGTH_LONG).show();
        }
    }

    public void LockOutProcedure(View view){

        //Check current MyPU status: operation done only if user is locked in
        if( userSettings.getInt(getString(R.string.USER_STATUS), -1) == 1)
        {

            //Retrieve the Station Id and Place Id saved after lock-in procedure
            String stationId = userSettings.getString(getString(R.string.USER_BIKE_STATION_ID), "-1");
            String placeId = userSettings.getString(getString(R.string.USER_BIKE_PLACE_ID), "-1");

            //Make a Lock-Out request to MYBPSERVER
            new MakeLockOutRequest(this, stationId, placeId).execute();

        } else {
            //no bike locked
            Toast.makeText(PersonalActivity.this, "No Bike Locked", Toast.LENGTH_LONG).show();
        }
    }

    public void bikeOnMap(View view){

        //Check current MyPU status: operation done only if user is locked in
        if( userSettings.getInt(getString(R.string.USER_STATUS), -1) == 1){

            //Retrieve the Station Id
            String stationId = userSettings.getString(getString(R.string.USER_BIKE_STATION_ID), "-1");

            //Launch MapsActivity and search mystation
            Intent intent = new Intent(this, MapsActivity.class);
            intent.putExtra(EXTRA_STATION_ID, stationId);
            intent.putExtra(SignInActivity.EXTRA_CALL_FROM, "PersonalActivity");
            startActivity(intent);

        } else {
            //no bike locked
            Toast.makeText(PersonalActivity.this, "No Bike Locked", Toast.LENGTH_LONG).show();

        }

    }

    //Invoked by GetUsersInfoTask on postExecute
    public void ManagePollingState(){
        myPUBikeStatus.setText("Wait...");
        myPUBikeStatus.setTextColor(getResources().getColor(R.color.yellow));
        myBPStationNumber.setText("wait...");
        myBPStationNumber.setEnabled(false);
        myBPStationPlace.setText("wait...");
        myBPStationPlace.setEnabled(false);
    }

}
