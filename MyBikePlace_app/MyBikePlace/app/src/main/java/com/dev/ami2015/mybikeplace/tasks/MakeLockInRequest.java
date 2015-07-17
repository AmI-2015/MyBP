package com.dev.ami2015.mybikeplace.tasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

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

    public static final String MYBPSERVER_LOCK_IN_URL ="http://192.168.56.1:7000/myBP_server/users/lock_app";
    public static final String DEBUG_TAG = "HttpExample";

    public PersonalActivity parentActivity;

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

            MakePostRequestToMyBPServer(MYBPSERVER_LOCK_IN_URL);

//            JSONObject LockInResultJson = MakePostRequestToMyBPServer(MYBPSERVER_LOCK_IN_URL);
//
//            //theoretically useless, the LockIn request just has to start lock-in procedure, GetUsersInfoTask determinate the real status
//
//            //save obtained data from MYBPSERVER
//            lockInStationIDResult = LockInResultJson.getString("station_id");
//            lockInPlaceIDResult = LockInResultJson.getString("place_id");
//
//            if (lockInStationIDResult.equals("-1") || lockInPlaceIDResult.equals("-1") ||
//                    !lockInStationIDResult.equals(lockInStationID) || !lockInPlaceIDResult.equals(lockInPlaceID)){
//                //error from server, MyPU not locked
//                userSettingsEditor = userSettings.edit();
//                userSettingsEditor.putInt(parentActivity.getString(R.string.USER_STATUS), -1);
//                userSettingsEditor.commit();
//            } else if (lockInStationIDResult.equals(lockInStationID) && lockInPlaceIDResult.equals(lockInPlaceID)){
//                //lock-in procedure successful
//                userSettingsEditor = userSettings.edit();
//                userSettingsEditor.putInt(parentActivity.getString(R.string.USER_STATUS), +1);
//                userSettingsEditor.putString(parentActivity.getString(R.string.USER_BIKE_STATION_ID), lockInStationIDResult);
//                userSettingsEditor.putString(parentActivity.getString(R.string.USER_BIKE_PLACE_ID), lockInPlaceIDResult);
//                userSettingsEditor.commit();
//            }
//
//
        } catch (IOException e) {
            return null;
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void v) {
        super.onPostExecute(v);

        //Invoke GetInfoTask
        new GetUsersInfoTask(parentActivity).execute();

    }

    public void MakePostRequestToMyBPServer(String myurl) throws IOException {

        JSONObject lockInRequestJson = null;

        try {

            URL url = new URL(myurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();


            //create Json object to send inside POST request
            lockInRequestJson = new JSONObject();

            lockInRequestJson.put("station_id", lockInStationID);
            lockInRequestJson.put("place_id", lockInPlaceID);
            lockInRequestJson.put("security_key", userSettings.getString(this.parentActivity.getString(R.string.USER_USER_CODE), null /*default value*/) +
                    userSettings.getString(this.parentActivity.getString(R.string.USER_PWD_CODE), null /*default value*/));
            lockInRequestJson.put("registration_id", userSettings.getString(this.parentActivity.getString(R.string.USER_REGID), null /*default value*/));


            // Set request nature and parameters
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setInstanceFollowRedirects(false);
            conn.setUseCaches(false);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");

            //write json inside request
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(lockInRequestJson.toString());

            wr.flush();

            //Get the HTTP response
            int responseCode = conn.getResponseCode();
            Log.d(DEBUG_TAG, "The response is: " + responseCode);


        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }


//    public JSONObject MakePostRequestToMyBPServer(String myurl) throws IOException {
//
//        InputStream is = null;
//        JSONObject lockInRequestJson = null;
//        JSONObject lockInResultJson = null;
//
//        try {
//
//            URL url = new URL(myurl);
//            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//
//
//            //create Json object to send inside POST request
//            lockInRequestJson  = new JSONObject();
//
//            lockInRequestJson.put("station_id", lockInStationID);
//            lockInRequestJson.put("place_id", lockInPlaceID);
//            lockInRequestJson.put("security_key", userSettings.getString(this.parentActivity.getString(R.string.USER_USER_CODE), null /*default value*/) +
//                    userSettings.getString(this.parentActivity.getString(R.string.USER_PWD_CODE), null /*default value*/));
//            lockInRequestJson.put("registration_id", userSettings.getString(this.parentActivity.getString(R.string.USER_REGID), null /*default value*/));
//
//
//            // Set request nature and parameters
//            conn.setRequestMethod("POST");
//            conn.setDoOutput(true);
//            conn.setDoInput(true);
//            conn.setInstanceFollowRedirects(false);
//            conn.setUseCaches(false);
//            conn.setRequestProperty("Content-Type", "application/json");
//            conn.setRequestProperty("Accept", "application/json");
//
//            //write json inside request
//            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
//            wr.write(lockInRequestJson.toString());
//
//            wr.flush();
//
//
//            // Get the HTTP response
//            int responseCode = conn.getResponseCode();
//            Log.d(DEBUG_TAG, "The response is: " + responseCode);
//            is = conn.getInputStream();
//
//            // Convert the HTTP response (InputStream) into a string
//            BufferedReader in = new BufferedReader(new InputStreamReader(is));
//
//            String inputLine;
//            StringBuffer response = new StringBuffer();
//
//            while ((inputLine = in.readLine()) != null) {
//                response.append(inputLine);
//            }
//
//            //close the stream
//            is.close();
//            in.close();
//
//            // Convert the string response into a JSONObject
//
//            lockInResultJson = new JSONObject(response.toString());
//
//        } catch (IOException | JSONException e){
//            e.printStackTrace();
//            return null;
//        } finally {
//            if (is != null) {
//                is.close();
//            }
//        }
//
//        return lockInResultJson;
//
//    }
}
