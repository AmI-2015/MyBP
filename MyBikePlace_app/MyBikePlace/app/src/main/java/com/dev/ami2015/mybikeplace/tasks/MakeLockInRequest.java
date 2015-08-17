package com.dev.ami2015.mybikeplace.tasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.dev.ami2015.mybikeplace.PersonalActivity;
import com.dev.ami2015.mybikeplace.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Zephyr on 13/07/2015.
 */
public class MakeLockInRequest extends AsyncTask<Void, Void, Void>  {

    public String MYBPSERVER_LOCK_IN_URL = null;
//    public static final String MYBPSERVER_LOCK_IN_URL ="http://192.168.0.9:7000/myBP_server/users/lock_app";
    public static final String DEBUG_TAG = "HttpExample";

    public PersonalActivity parentActivity;

    public String LockInResultStr = null;

    //Variable to set in Personal Activity
    String lockInStationID;
//    String lockInStationIDResult;
    String lockInPlaceID;
//    String lockInPlaceIDResult;

    // Make MakeLockIntask able to access User Settings file
    // Shared Preference file
    SharedPreferences userSettings = null;
//    // Creating editor to write inside Preference File
//    SharedPreferences.Editor userSettingsEditor = null;


    //constructor receives as parameter the parent activity that started the task
    public MakeLockInRequest(PersonalActivity activity, String stationID, String placeID){
        this.parentActivity = activity;
        MYBPSERVER_LOCK_IN_URL = parentActivity.getResources().getString(R.string.IP_SERVER)+"/myBP_server/users/lock_app";
        //Creating shared preference file
        userSettings = this.parentActivity.getSharedPreferences(this.parentActivity.getString(R.string.USER_SETTINGS), Context.MODE_PRIVATE);

        //Save station id and place id inserted by user
        lockInStationID = stationID;
        lockInPlaceID = placeID;

    }

    @Override
    protected Void doInBackground(Void... params) {
        // params comes from the execute() call: params[0] is the url.
        try {

            JSONObject LockInResultJson = MakePostRequestToMyBPServer(MYBPSERVER_LOCK_IN_URL);
            if(LockInResultJson != null) {
                //no connection error
                LockInResultStr = LockInResultJson.getString("end");
            } else {
                //connection error
                LockInResultStr = "error";
            }

        } catch (IOException e) {
            return null;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void v) {
        super.onPostExecute(v);

        if(LockInResultStr.equals("1")){
            //everything fine in starting lock in request
            //Invoke GetInfoTask
            new GetUsersInfoTask(parentActivity).execute();
        } else {
            //Stop operation and "toast" the error
            Toast.makeText(parentActivity, "Connection Error, try again.", Toast.LENGTH_LONG).show();
        }
    }

    public JSONObject MakePostRequestToMyBPServer(String myurl) throws IOException {

        InputStream is = null;
        JSONObject lockInRequestJson = null;
        JSONObject lockInResultJson = null;

        try {

            URL url = new URL(myurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();


            //create Json object to send inside POST request
            lockInRequestJson  = new JSONObject();

            lockInRequestJson.put("station_id", lockInStationID);
            lockInRequestJson.put("place_id", lockInPlaceID);
            lockInRequestJson.put("security_key", userSettings.getString(this.parentActivity.getString(R.string.USER_USER_CODE), null /*default value*/) +
                    userSettings.getString(this.parentActivity.getString(R.string.USER_PWD_CODE), null /*default value*/));
            lockInRequestJson.put("registration_id", userSettings.getString(this.parentActivity.getString(R.string.USER_REGID), null /*default value*/));
            lockInRequestJson.put("lock_flag", "1"); //indico operazione di lockin

            // Set request nature and parameters
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setInstanceFollowRedirects(false);
            conn.setUseCaches(false);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            conn.setConnectTimeout(2000);

            //write json inside request
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(lockInRequestJson.toString());

            wr.flush();


            // Get the HTTP response
            int responseCode = conn.getResponseCode();
            Log.d(DEBUG_TAG, "The response is: " + responseCode);
            is = conn.getInputStream();

            // Convert the HTTP response (InputStream) into a string
            BufferedReader in = new BufferedReader(new InputStreamReader(is));

            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }

            //close the stream
            is.close();
            in.close();

            // Convert the string response into a JSONObject

            lockInResultJson = new JSONObject(response.toString());

        } catch (IOException | JSONException e){
            e.printStackTrace();
            return null;
        } finally {
            if (is != null) {
                is.close();
            }
        }

        return lockInResultJson;

    }
}
