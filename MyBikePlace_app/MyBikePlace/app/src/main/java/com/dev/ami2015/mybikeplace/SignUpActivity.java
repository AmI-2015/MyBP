package com.dev.ami2015.mybikeplace;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;


public class SignUpActivity extends ActionBarActivity {

    public static int MIN_USERNAME_LENGHT = 8;
    public static int MAX_USERNAME_LENGHT = 16;
    public static int MIN_PASSWORD_LENGHT = 8;
    public static int MAX_PASSWORD_LENGHT = 16;

    EditText editUsername;
    EditText editPassword;

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
    public void sendSignUpForm(View view){

        boolean credentialError = checkCredential();


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
