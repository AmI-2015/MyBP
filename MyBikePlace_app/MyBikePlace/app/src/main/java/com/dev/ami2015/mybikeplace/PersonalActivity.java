package com.dev.ami2015.mybikeplace;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;


public class PersonalActivity extends ActionBarActivity {

    //Make personale activity able to access User Settings file
    // Shared Preference file
    SharedPreferences userSettings = null;
    // Creating editor to write inside Preference File
    SharedPreferences.Editor userSettingsEditor = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // get the two extras containg credentials from the intent
        Intent intent = getIntent();
        String username = intent.getStringExtra(SignInActivity.EXTRA_USERNAME);

        setContentView(R.layout.activity_personal);

        // modify text view content
        TextView userID = (TextView) findViewById(R.id.userID);
        userID.setText(username);

        // Creating shared preference file
        userSettings = this.getSharedPreferences(getString(R.string.USER_SETTINGS), Context.MODE_PRIVATE);

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
                return true;
            case R.id.action_clear_remember_me_checkbox:
                userSettingsEditor = userSettings.edit();
                userSettingsEditor.putBoolean(getString(R.string.USER_REMEMBER_ME), false);
                userSettingsEditor.commit();
                return true;
            case R.id.action_clear_skip_checkbox:
                userSettingsEditor = userSettings.edit();
                userSettingsEditor.putBoolean(getString(R.string.USER_SKIP), false);
                userSettingsEditor.commit();
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }
}
