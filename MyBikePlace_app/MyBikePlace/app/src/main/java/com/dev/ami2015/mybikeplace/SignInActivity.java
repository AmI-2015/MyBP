package com.dev.ami2015.mybikeplace;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import com.dev.ami2015.mybikeplace.tasks.signInConnection;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;


public class SignInActivity extends ActionBarActivity {

    public final static String EXTRA_USERNAME = "com.dev.ami2015.mybikeplace.USERNAME";
    public final static String EXTRA_PASSWORD = "com.dev.ami2015.mybikeplace.PASSWORD";
    public static String userID = null;
    public static final String MYBPSERVER_URL ="http://192.168.56.1:7000/myBP_server/users/sign_in";
    Intent logInIntent;

    // view elements from activty
    EditText editUsername = null;
    EditText editPassword = null;
    CheckBox checkRememberMe = null;
    CheckBox checkSkip = null;

    // Objects to manage GCM
    GoogleCloudMessaging gcm;
    String regid;
    String PROJECT_NUMBER = "80513371534";

    // Shared Preference file
    SharedPreferences userSettings = null;
    // Creating editor to write inside Preference File
    SharedPreferences.Editor userSettingsEditor = null;

    // Username and Password to copy inside Preference File
    String prefUsername = null;
    String prefPassword = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // get the two extras containg credentials from the intent
        Intent intent = getIntent();
        String username = intent.getStringExtra(SignInActivity.EXTRA_USERNAME);

        setContentView(R.layout.activity_signin);

        // Acquiring view elements from activty
        editUsername = (EditText) findViewById(R.id.usernameText);
        editPassword = (EditText) findViewById(R.id.passwordText);
        checkRememberMe = (CheckBox) findViewById(R.id.rememberMeCheckBox);
        checkSkip = (CheckBox) findViewById(R.id.skipCheckBox);

        if(username != null) {
            // modify text view content
            editUsername.setText(username);
        }

        getRegId();

        // Creating shared preference file
        userSettings = this.getSharedPreferences(getString(R.string.USER_SETTINGS), Context.MODE_PRIVATE);

        // Check shared preference file
        SetUsernamePasswordByPreferenceFile();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch(id){

            case R.id.actionSignUp:
                SignUp();
                return true;
            case R.id.actionTestConnection:
                TestConnection();
                return true;
            case R.id.actionGoToMap:
                GoToMaps();
                return true;
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

    // called when the user clicks the send button
    public void sendCredentials(View view) throws UnsupportedEncodingException, JSONException, NoSuchAlgorithmException {

        editUsername = (EditText) findViewById(R.id.usernameText);
        editPassword = (EditText) findViewById(R.id.passwordText);
        checkRememberMe = (CheckBox) findViewById(R.id.rememberMeCheckBox);
        checkSkip = (CheckBox) findViewById(R.id.skipCheckBox);

        // Manage Username TextView
        String username = editUsername.getText().toString();
        // copying username to save it into preference file
        prefUsername = username;
        SignInActivity.userID = username;

        // Manage Password TextView
        String password = editPassword.getText().toString();
        // copying password to save it into preference file
        prefPassword = password;

        // Save Remember Me and Skip CheckBox status inside user settings preference file
        userSettingsEditor = userSettings.edit();
        userSettingsEditor.putBoolean(getString(R.string.USER_REMEMBER_ME), checkRememberMe.isChecked());
        userSettingsEditor.putBoolean(getString(R.string.USER_SKIP), checkSkip.isChecked());
        userSettingsEditor.commit();


        byte[] bytesOfUser = username.getBytes("UTF-8");
        byte[] bytesOfPwd = password.getBytes("UTF-8");
        MessageDigest mdUser = null;
        MessageDigest mdPwd = null;

        if (Objects.equals(username, "")) {
            editUsername.setHint("type a valid username");
            editUsername.setHintTextColor(getResources().getColor(R.color.red));
            editPassword.setText("");
        } else {
            if (Objects.equals(password, "")) {
                editPassword.setHint("type a valid password");
                editPassword.setHintTextColor(getResources().getColor(R.color.red));
            } else {

                //Encryption of user and pwd
                mdUser = MessageDigest.getInstance("MD5");
                mdPwd = MessageDigest.getInstance("MD5");
                byte[] digestUser = mdUser.digest(bytesOfUser);
                byte[] digestPwd = mdPwd.digest(bytesOfPwd);
                username = android.util.Base64.encodeToString(digestUser, android.util.Base64.DEFAULT);
                password = android.util.Base64.encodeToString(digestPwd, android.util.Base64.DEFAULT);
                //#########################################//
                JSONObject obj =new JSONObject();
                JSONObject objResponse= new JSONObject();
                obj.put("pwd_code",  password);
                obj.put("user_code", username);
                obj.put("registration_id", regid);

                new signInConnection(this).execute(MYBPSERVER_URL, obj.toString());

            }
        }
    }


    // called when user click on clear button
    public void clearCredentials(View view){

        editUsername.setText("");
        editUsername.setHint(R.string.username_hint);
        editUsername.setHintTextColor(getResources().getColor(R.color.hint_foreground_material_light));

        editPassword.setText("");
        editPassword.setHint(R.string.password_hint);
        editPassword.setHintTextColor(getResources().getColor(R.color.hint_foreground_material_light));

    }

    public void setServerResponse(JSONObject serverResponse) throws JSONException {

        int errorSIGNIN = 0;
        String errStr = serverResponse.getString("error_str");
        if (Objects.equals(errStr, "ERROR_SIGNIN"))
            errorSIGNIN = 1;
        else{
            // Sign in was successful
            errorSIGNIN = 0;
            // Open editor to write inside Preference File ONLY IF REMEMBER ME IS CHECKED
            if (userSettings.getBoolean(getString(R.string.USER_REMEMBER_ME), false)) {
                userSettingsEditor = userSettings.edit();
                // save username and password inside user settings file
                userSettingsEditor.putString(getString(R.string.USER_USERNAME), prefUsername);
                userSettingsEditor.putString(getString(R.string.USER_PASSWORD), prefPassword);
                // COMMIT MODIFICATION!!!
                userSettingsEditor.commit();
            }
        }

        goToPersonalActivity(userID,errorSIGNIN);
    }

    public void GoToMaps(){

        logInIntent = new Intent(this, MapsActivity.class);
        startActivity(logInIntent);
    }

    public void goToPersonalActivity(String userID, int error)
    {
        Intent i = new Intent(this, PersonalActivity.class);
        Intent err_i = new Intent(this, SignInActivity.class);
        if(error == 1)
        {
            //SOLO PER DEBUG
            err_i.putExtra(EXTRA_USERNAME, "USER_ID o PWD ERRATE\n");
            startActivity(err_i);
        }
        else {
            i.putExtra(EXTRA_USERNAME, userID);
            startActivity(i);
        }
    }

    public void TestConnection() {

        TextView connectionStatus = (TextView) findViewById(R.id.connectionStatusText);

        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            connectionStatus.setText("Internet connection is stable");
            connectionStatus.setTextColor(getResources().getColor(R.color.green));
        } else {
            connectionStatus.setText("No internet Connection");
            connectionStatus.setTextColor(getResources().getColor(R.color.red));
        }
    }

    //called when Sign Up in action bar is pressed
    public void SignUp(){
        SignUpActivity.regid = regid;
        logInIntent = new Intent(this, SignUpActivity.class);
        startActivity(logInIntent);

    }

    //checks if preference file contains valid username and password to automatically set inside
    //editUsername and editPassword view elements
    public void SetUsernamePasswordByPreferenceFile(){

        //Retrieving data from User Settings file
        boolean skip = userSettings.getBoolean(getString(R.string.USER_SKIP), false /*default value*/);
        boolean rememberMe = userSettings.getBoolean(getString(R.string.USER_REMEMBER_ME), false /*default value*/);
        String username = userSettings.getString(getString(R.string.USER_USERNAME), null /*default value*/);
        String password = userSettings.getString(getString(R.string.USER_PASSWORD), null /*default value*/);

        //Populates/Skip sign in depending on retrieved data
        if(skip){ //skip check box is checked

            // Restore user setting about: skip
            checkSkip.setChecked(true);

            if(rememberMe){ //rememberMe check box is checked

                // Restore user setting about: remember me
                checkRememberMe.setChecked(true);

                if(username != null && password != null){ //username and password data are presents inside user settings file

                    //Compile sign in form
                    editUsername.setText(username);
                    editPassword.setText(password);
                    //Execute sign in
                    Button continueButton = (Button) findViewById(R.id.continueButton);
                    continueButton.performClick();

                } else { //username and password data aren't presents inside user settings file: AUTO SIGN IN NOT POSSIBLE

                    //Reset default value for check box
                    checkSkip.setChecked(false);
                    checkRememberMe.setChecked(false);

                }
            } else { //rememberMe check box is unchecked

                //Go automatically to map without sign in procedure
                this.GoToMaps();

            }

        } else { //skip check box is unchecked

            if(rememberMe){ //rememberMe check box is checked

                // Restore user setting about: remember me
                checkRememberMe.setChecked(true);

                if(username != null && password != null){ //username and password data are presents inside user settings file

                    //Compile sign in form
                    editUsername.setText(username);
                    editPassword.setText(password);
                    //Wait user action

                } else { //username and password data aren't presents inside user settings file: AUTO SIGN IN NOT POSSIBLE

                    //Reset default value for remember me check box
                    checkRememberMe.setChecked(false);

                }

            } else { //rememberMe check box is unchecked

                //Do Nothing wait user action
            }
        }
    }

}

