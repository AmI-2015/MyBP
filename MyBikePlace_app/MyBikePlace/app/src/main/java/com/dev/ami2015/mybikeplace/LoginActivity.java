package com.dev.ami2015.mybikeplace;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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


public class LoginActivity extends ActionBarActivity {

    public final static String EXTRA_USERNAME = "com.dev.ami2015.mybikeplace.USERNAME";
    public final static String EXTRA_PASSWORD = "com.dev.ami2015.mybikeplace.PASSWORD";
    public static String userID = null;
    public static final String MYBPSERVER_URL ="http://192.168.56.1:7000/myBP_server/users/sign_in";
    Intent logInIntent;

    GoogleCloudMessaging gcm;
    String regid;
    String PROJECT_NUMBER = "80513371534";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // get the two extras containg credentials from the intent
        Intent intent = getIntent();
        String username = intent.getStringExtra(LoginActivity.EXTRA_USERNAME);

        setContentView(R.layout.activity_login);
        if(username != null) {
            // modify text view content
            TextView userID = (TextView) findViewById(R.id.usernameText);
            userID.setText(username);
        }

        getRegId();

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

        Intent actionIntent;
        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            Intent i = new Intent(this, TestHTTPActivity.class);
//            startActivity(i);
//            return true;
//        }

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

        EditText editUsername = (EditText) findViewById(R.id.usernameText);
        String username = editUsername.getText().toString();
        LoginActivity.userID = username;
        EditText editPassword = (EditText) findViewById(R.id.passwordText);
        String password = editPassword.getText().toString();

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

        EditText editUsername = (EditText) findViewById(R.id.usernameText);
        editUsername.setText("");
        editUsername.setHint(R.string.username_hint);
        editUsername.setHintTextColor(getResources().getColor(R.color.hint_foreground_material_light));

        EditText editPassword = (EditText) findViewById(R.id.passwordText);
        editPassword.setText("");
        editPassword.setHint(R.string.password_hint);
        editPassword.setHintTextColor(getResources().getColor(R.color.hint_foreground_material_light));

    }

    public void setServerResponse(JSONObject serverResponse) throws JSONException {

        int errorSIGNIN = 0;
        String errStr = serverResponse.getString("error_str");
        if(Objects.equals(errStr, "ERROR_SIGNIN"))
            errorSIGNIN = 1;
        else
            errorSIGNIN = 0;

        goToPersonalActivity(userID,errorSIGNIN);
    }

    public void GoToMaps(){

        logInIntent = new Intent(this, MapsActivity.class);
        startActivity(logInIntent);
    }

    public void goToPersonalActivity(String userID, int error)
    {
        Intent i = new Intent(this, PersonalActivity.class);
        Intent err_i = new Intent(this, LoginActivity.class);
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
}

