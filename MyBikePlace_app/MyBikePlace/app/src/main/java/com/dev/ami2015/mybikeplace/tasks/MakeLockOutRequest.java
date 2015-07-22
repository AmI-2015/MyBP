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

            MakePostRequestToMyBPServer(MYBPSERVER_LOCK_OUT_URL);

//            JSONObject LockOutResultJson = MakePostRequestToMyBPServer(MYBPSERVER_LOCK_OUT_URL);

//            //save obtained data from MYBPSERVER
//            lockOutStationIDResult = LockOutResultJson.getString("station_id");
//            lockOutPlaceIDResult = LockOutResultJson.getString("place_id");
//
//            if (!lockOutStationIDResult.equals("-1") || !lockOutPlaceIDResult.equals("-1")){
//                //error from server, MyPU not unlocked
//                userSettingsEditor = userSettings.edit();
//                userSettingsEditor.putInt(parentActivity.getString(R.string.USER_STATUS), -1);
//                userSettingsEditor.commit();
//            } else if (lockOutStationIDResult.equals("-1") && lockOutPlaceIDResult.equals("-1")){
//                //lock-out procedure successful
//                userSettingsEditor = userSettings.edit();
//                userSettingsEditor.putInt(parentActivity.getString(R.string.USER_STATUS), 0);
//                userSettingsEditor.putString(parentActivity.getString(R.string.USER_BIKE_STATION_ID), "-1");
//                userSettingsEditor.putString(parentActivity.getString(R.string.USER_BIKE_PLACE_ID), "-1");
//                userSettingsEditor.commit();
//            }
//

        } catch (IOException e) {
            return null;
        }
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
        return null;
    }

    @Override
    protected void onPostExecute(Void v) {
        super.onPostExecute(v);

//        //Debug code begin
//        userSettingsEditor = userSettings.edit();
//        userSettingsEditor.putInt(this.parentActivity.getString(R.string.USER_STATUS), 1);
//        userSettingsEditor.commit();
//        //Debug code end

        //Invoke GetUsersInfoTask
        new GetUsersInfoTask(parentActivity).execute();

    }


    public void MakePostRequestToMyBPServer(String myurl) throws IOException {

        JSONObject lockOutRequestJson = null;

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
            lockOutRequestJson.put("lock", "-1");

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
            wr.write(lockOutRequestJson.toString());

            wr.flush();

            //Get the HTTP response
            int responseCode = conn.getResponseCode();
            Log.d(DEBUG_TAG, "The response is: " + responseCode);


        } catch (IOException | JSONException e){
            e.printStackTrace();
        }
    }

//    public JSONObject MakePostRequestToMyBPServer(String myurl) throws IOException {
//
//        InputStream is = null;
//        JSONObject lockOutRequestJson = null;
//        JSONObject lockOutResultJson = null;
//
//        try {
//
//            URL url = new URL(myurl);
//            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//
//
//            //create Json object to send inside POST request
//            lockOutRequestJson  = new JSONObject();
//
//            lockOutRequestJson.put("station_id", lockOutStationID);
//            lockOutRequestJson.put("place_id", lockOutPlaceID);
//            lockOutRequestJson.put("security_key", userSettings.getString(this.parentActivity.getString(R.string.USER_USER_CODE), null /*default value*/) +
//                    userSettings.getString(this.parentActivity.getString(R.string.USER_PWD_CODE), null /*default value*/));
//            lockOutRequestJson.put("registration_id", userSettings.getString(this.parentActivity.getString(R.string.USER_REGID), null /*default value*/));
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
//            wr.write(lockOutRequestJson.toString());
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
//            lockOutResultJson = new JSONObject(response.toString());
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
//        return lockOutResultJson;
//
//    }
}

