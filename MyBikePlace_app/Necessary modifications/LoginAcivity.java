package com.dev.mybp.mybikeplace;
import com.dev.mybp.mybikeplace.serverConnection;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
//import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.util.Log;

import java.lang.String;
import org.apache.http.params.*;
import org.apache.http.client.ClientProtocolException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import org.apache.http.HttpEntity;
import java.net.HttpURLConnection;
import java.net.URL;

import java.io.*;
import java.io.UnsupportedEncodingException;
import java.security.*;

public class LoginActivity extends ActionBarActivity {

    public final static String EXTRA_USERNAME = "com.example.zephyr.tutorialhtml.USERNAME";
    public final static String EXTRA_PASSWORD = "com.example.zephyr.tutorialhtml.PASSWORD";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // called when the user clicks the send button
    public void sendCredentials(View view) throws UnsupportedEncodingException {

        Intent i = new Intent(this, PersonalActivity.class);
        String userRsp=null, pwdRsp = null, redIdRsp = null;

        EditText editUsername = (EditText) findViewById(R.id.usernameText);
        String username = editUsername.getText().toString();
        EditText editPassword = (EditText) findViewById(R.id.passwordText);
        String password = editPassword.getText().toString();

        if (username.isEmpty()) {
            editUsername.setHint("type a valid username");
            editUsername.setHintTextColor(getResources().getColor(R.color.red));
            editPassword.setText("");
        } else {
            if (password.isEmpty()) {
                editPassword.setHint("type a valid password");
                editPassword.setHintTextColor(getResources().getColor(R.color.red));
            } else {
                byte[] bytesOfUser = username.getBytes("UTF-8");
                byte[] bytesOfPwd = password.getBytes("UTF-8");
                MessageDigest mdUser = null;
                MessageDigest mdPwd = null;
                try {
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
                    obj.put("pwd_code", password);
                    obj.put("user_code", username);
                    obj.put("registration_id", "2351");
                    //############## TALE URL E' QUELLO SOLO PER IL SIGN IN AL SERVER #################//
                    String url = "http://10.13.13.156:7000/myBP_server/users/sign_in";
                    serverConnection con = new serverConnection();
                    con.execute(url, obj.toString());
                    // MIKY IL PROBLEMA QUI E' CHE QUESTA getDataJson() NON POSSO FARLA QUI, PERCHE' CONN
                    //ESEGUE QUEL TASK IN MODO ASINCRONO; BISOGNA QUINDI RISOLVERE IL PROBLEMA DI GESTIRE
                    //IL PACCHETTO CHE RITORNA IL SERVER E POTER ACCEDERE A QUELLE INFOs DA QUESTA CLASSE
//                    objResponse= con.getDataJson();
//                    userRsp= objResponse.getString("user_code");
//                    pwdRsp = objResponse.getString("pwd_code");
//                    redIdRsp = objResponse.getString("registration_id");
                } catch (NoSuchAlgorithmException e) {
                    username = "ERROR_ENCRYPTION";
                    password = "ERROR_ENCRYPTION";
                    //e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

//                if (Integer.parseInt(redIdRsp)==-1) {
//                    i.putExtra(EXTRA_USERNAME, username);
//                    i.putExtra(EXTRA_PASSWORD, password);
//                    //Check function to sign in
//                    startActivity(i);
//                } else
                if (redIdRsp==null) {
                    i.putExtra(EXTRA_USERNAME, username);
                    i.putExtra(EXTRA_PASSWORD, password);
                    //Check function to sign in
                    startActivity(i);
                } else {
                    i.putExtra(EXTRA_USERNAME, userRsp);
                    i.putExtra(EXTRA_PASSWORD, pwdRsp);
                    //Check function to sign in
                    startActivity(i);
                }
            }
        }
    }


    // called when user click on clear button
    public void clearCredentials(View view) {

        EditText editUsername = (EditText) findViewById(R.id.usernameText);
        editUsername.setText("");
        editUsername.setHint(R.string.usernameHint);
        editUsername.setHintTextColor(getResources().getColor(R.color.hint_foreground_material_light));

        EditText editPassword = (EditText) findViewById(R.id.passwordText);
        editPassword.setText("");
        editPassword.setHint(R.string.passwordHint);
        editPassword.setHintTextColor(getResources().getColor(R.color.hint_foreground_material_light));

    }

    public void goToMaps(View view) {

        Intent i = new Intent(this, MapsActivity.class);
        startActivity(i);
    }
}
