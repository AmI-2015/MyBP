package com.dev.ami2015.mybikeplace;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Objects;


public class LoginActivity extends ActionBarActivity {

    public final static String EXTRA_USERNAME = "com.dev.ami2015.mybikeplace.USERNAME";
    public final static String EXTRA_PASSWORD = "com.dev.ami2015.mybikeplace.PASSWORD";

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

        Intent actionIntent;
        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            Intent i = new Intent(this, TestHTTPActivity.class);
//            startActivity(i);
//            return true;
//        }

        switch(id){

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

    // called when the user clicks the send button
    public void sendCredentials(View view){

//        Intent i = new Intent(this, PersonalActivity.class);

        EditText editUsername = (EditText) findViewById(R.id.usernameText);
        String username = editUsername.getText().toString();
        EditText editPassword = (EditText) findViewById(R.id.passwordText);
        String password = editPassword.getText().toString();

        if (Objects.equals(username, "")) {
            editUsername.setHint("type a valid username");
            editUsername.setHintTextColor(getResources().getColor(R.color.red));
            editPassword.setText("");
        } else {
            if (Objects.equals(password, "")) {
                editPassword.setHint("type a valid password");
                editPassword.setHintTextColor(getResources().getColor(R.color.red));
            } else {
//                //put code hear to authenticate user
//                i.putExtra(EXTRA_USERNAME, username);
//                i.putExtra(EXTRA_PASSWORD, password);
//                startActivity(i);
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

    public void GoToMaps(){

        Intent i = new Intent(this, MapsActivity.class);
        startActivity(i);
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
}

