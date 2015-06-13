package com.dev.ami2015.mybikeplace;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.dev.ami2015.mybikeplace.tasks.signUpConnection;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;


public class SignUpActivity extends ActionBarActivity {

    public static int MIN_USERNAME_LENGHT = 8;
    public static int MAX_USERNAME_LENGHT = 16;
    public static int MIN_PASSWORD_LENGHT = 8;
    public static int MAX_PASSWORD_LENGHT = 16;
    public static String regid;
    public final static String EXTRA_USERNAME = "com.dev.ami2015.mybikeplace.USERNAME";
    public final static String EXTRA_PASSWORD = "com.dev.ami2015.mybikeplace.PASSWORD";
    public static final String MYBPSERVER_URL ="http://192.168.56.1:7000/myBP_server/users/sign_up";

    // editText view elements
    EditText editUsername;
    EditText editPassword;

    // credentials inserted by user
    public String username = null;
    public String password = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        editUsername = (EditText) findViewById(R.id.usernameEditSignUpActivity);
        editPassword = (EditText) findViewById(R.id.passwordEditSignUpActivity);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_sign_up, menu);
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

    // called when user click on Sign Up button
    public void sendSignUpForm(View view) throws UnsupportedEncodingException, NoSuchAlgorithmException, JSONException {

        boolean credentialError = checkCredential();

        if (credentialError == true ) {
        }
        else
        {
            // Taking credentials by User Interface
            username = editUsername.getText().toString();
            password = editPassword.getText().toString();

            // Preparing Encryption
            byte[] bytesOfUser    = username.getBytes("UTF-8");
            byte[] bytesOfPwd     = password.getBytes("UTF-8");
            MessageDigest mdUser  = null;
            MessageDigest mdPwd   = null;

            //Encryption of user and pwd
            mdUser = MessageDigest.getInstance("MD5");
            mdPwd = MessageDigest.getInstance("MD5");
            byte[] digestUser = mdUser.digest(bytesOfUser);
            byte[] digestPwd  = mdPwd.digest(bytesOfPwd);
            String cryptedUsername = android.util.Base64.encodeToString(digestUser, android.util.Base64.DEFAULT);
            String cryptedPassword = android.util.Base64.encodeToString(digestPwd, android.util.Base64.DEFAULT);

            //#########################################//
            JSONObject obj =new JSONObject();
            JSONObject objResponse= new JSONObject();
            obj.put("pwd_code",  cryptedPassword);
            obj.put("user_code", cryptedUsername);
            obj.put("registration_id", regid);

            new signUpConnection(this).execute(MYBPSERVER_URL, obj.toString());
        }
    }

    // called when user click on Clear button
    public void clearSignUpForm(View view){

        clearUsernameField();
        clearPasswordField();

    }

    // check if the credential format is correct
    public boolean checkCredential(){

        boolean usernameError = false;
        boolean passwordError = false;

        String username = editUsername.getText().toString();
        String password = editPassword.getText().toString();

        if(username.length() < MIN_USERNAME_LENGHT | username.length() > MAX_USERNAME_LENGHT /* | checkalphanumeric */ ){
            usernameError = true;
            editUsername.setText("");
            editUsername.setHint(getResources().getString(R.string.username_input_error_hint));
            editUsername.setHintTextColor(getResources().getColor(R.color.red));
            // if username is wrong the password field is automatically deleted
            clearPasswordField();
        }

        if(password.length() < MIN_PASSWORD_LENGHT | password.length() > MAX_PASSWORD_LENGHT /* | checkalphanumeric */){
            usernameError = true;
            editPassword.setText("");
            editPassword.setHint(getResources().getString(R.string.password_input_error_hint));
            editPassword.setHintTextColor(getResources().getColor(R.color.red));
        }

        if (usernameError == true | passwordError == true){
            // sign up form is correct
            return true;
        } else {
            // sign up form is wrong
            return false;
        }

    }


    public void setServerResponse(JSONObject serverResponse) throws JSONException {

        int errorSIGNUP = 0;
        String errStr = serverResponse.getString("error_str");
        if(Objects.equals(errStr, "ERROR_SIGNUP"))
            errorSIGNUP = 1;
        else {
            // sign up completed successfully
            errorSIGNUP = 0;
        }
        goToPersonalActivity(username,errorSIGNUP);
    }

    public void goToPersonalActivity(String userID, int error)
    {
        Intent i = new Intent(this, PersonalActivity.class);
        if(error == 1)
        {
            editUsername.setText("ERROR SIGN UP! THIS USER ALREADY EXISTS");
            editPassword.setText("");
        }
        else {
            i.putExtra(EXTRA_USERNAME, userID);
            startActivity(i);
        }
    }

    // function tu easy manage activity view
    public void clearUsernameField(){

        editUsername.setText("");
        editUsername.setHint(R.string.username_rule_hint);
        editUsername.setHintTextColor(getResources().getColor(R.color.hint_foreground_material_light));

    }

    public void clearPasswordField(){

        editPassword.setText("");
        editPassword.setHint(R.string.password_rule_hint);
        editPassword.setHintTextColor(getResources().getColor(R.color.hint_foreground_material_light));

    }
}
