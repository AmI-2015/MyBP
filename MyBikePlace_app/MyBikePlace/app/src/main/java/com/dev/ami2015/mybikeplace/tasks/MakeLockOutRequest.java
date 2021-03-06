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
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Zephyr on 14/07/2015.
 */
public class MakeLockOutRequest extends AsyncTask<Void, Void, Void> {

    public String MYBPSERVER_LOCK_OUT_URL = null;
//    public static final String MYBPSERVER_LOCK_OUT_URL ="http://192.168.0.9:7000/myBP_server/users/lock_app";
    public static final String DEBUG_TAG = "HttpExample";

    public PersonalActivity parentActivity;

    public String LockOutResultStr = null;

    //Resutl from MYBPSERVER
    String lockOutStationID;
    String lockOutStationIDResult;
    String lockOutPlaceID;
    String lockOutPlaceIDResult;

    // Shared Preference file
    SharedPreferences userSettings = null;
//    // Creating editor to write inside Preference File
//    SharedPreferences.Editor userSettingsEditor = null;


    //constructor receives as parameter the parent activity that started the task
    public MakeLockOutRequest(PersonalActivity activity, String stationId, String placeId){
        this.parentActivity = activity;
        MYBPSERVER_LOCK_OUT_URL = parentActivity.getResources().getString(R.string.IP_SERVER)+"/myBP_server/users/lock_app";
        //Creating shared preference file
        userSettings = this.parentActivity.getSharedPreferences(this.parentActivity.getString(R.string.USER_SETTINGS), Context.MODE_PRIVATE);

        //Settling statioId and placeID
        lockOutStationID = stationId;
        lockOutPlaceID = placeId;

    }

    @Override
    protected Void doInBackground(Void... params) {
        // params comes from the execute() call: params[0] is the url.
        try {

            JSONObject LockOutResultJson = MakePostRequestToMyBPServer(MYBPSERVER_LOCK_OUT_URL);
            if(LockOutResultJson != null) {
                //no connection error
                LockOutResultStr = LockOutResultJson.getString("end");
            } else {
                //connection error
                LockOutResultStr = "error";
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

        if(LockOutResultStr.equals("1")){
            //everything fine in starting lock out request
            //Invoke GetInfoTask
            new GetUsersInfoTask(parentActivity).execute();
        } else if (LockOutResultStr.equals("0")){
            //no free place when lock in request started
            Toast.makeText(parentActivity, "Free place, lock-out stopped.", Toast.LENGTH_LONG).show();
        } else {
            //Stop operation and "toast" the error
            Toast.makeText(parentActivity, "Connection Error, try again.", Toast.LENGTH_LONG).show();
        }
    }


    public JSONObject MakePostRequestToMyBPServer(String myurl) throws IOException {

        InputStream is = null;
        JSONObject lockOutRequestJson = null;
        JSONObject lockOutResultJson = null;

        try {

            URL url = new URL(myurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();


            //create Json object to send inside POST request
            lockOutRequestJson  = new JSONObject();

            lockOutRequestJson.put("station_id", lockOutStationID);
            lockOutRequestJson.put("place_id", lockOutPlaceID);
            lockOutRequestJson.put("security_key", userSettings.getString(this.parentActivity.getString(R.string.USER_USER_CODE), null /*default value*/) +
                    userSettings.getString(this.parentActivity.getString(R.string.USER_PWD_CODE), null /*default value*/));
            lockOutRequestJson.put("registration_id", userSettings.getString(this.parentActivity.getString(R.string.USER_REGID), null /*default value*/));
            lockOutRequestJson.put("lock_flag", "-1");

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
            wr.write(lockOutRequestJson.toString());

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

            lockOutResultJson = new JSONObject(response.toString());

        } catch (IOException | JSONException e){
            e.printStackTrace();
            return null;
        } finally {
            if (is != null) {
                is.close();
            }
        }

        return lockOutResultJson;

    }
}

