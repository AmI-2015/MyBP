package com.dev.ami2015.mybikeplace.tasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.Button;

import com.dev.ami2015.mybikeplace.PersonalActivity;
import com.dev.ami2015.mybikeplace.R;
import com.google.android.gms.common.server.response.FastJsonResponse;

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
 * Created by Zephyr on 01/06/2015.
 */
public class GetUsersInfoTask extends AsyncTask<Void, Void, Void> {

    public String MYBPSERVER_GET_INFO_URL = null;
//    public static final String MYBPSERVER_GET_INFO_URL ="http://192.168.0.9:7000/myBP_server/users/get_info";
    public static final String DEBUG_TAG = "HttpExample";

    public PersonalActivity parentActivity;

    //Variable to set in Personal Activity
    String myPUStationNumber;
    String myPUStationPlace;
    Integer myPUStatus; //if status == 0 => myPUStatus false, status == 1 => myPUStatus true

    // Make GetUserInfo task able to access User Settings file
    // Shared Preference file
    SharedPreferences userSettings = null;
    // Creating editor to write inside Preference File
    SharedPreferences.Editor userSettingsEditor = null;

    //Polling flag
    boolean endPolling = false;

    //constructor receives as parameter the parent activity that started the task
    public GetUsersInfoTask(PersonalActivity activity){
        this.parentActivity = activity;
        MYBPSERVER_GET_INFO_URL = parentActivity.getResources().getString(R.string.IP_SERVER)+"/myBP_server/users/get_info";
        //Creating shared preference file
        userSettings = this.parentActivity.getSharedPreferences(this.parentActivity.getString(R.string.USER_SETTINGS), Context.MODE_PRIVATE);
        //add RegId inside preference file
        userSettingsEditor = userSettings.edit();
        //clear polling flag
        endPolling = false;

    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        //set field in personal activity with wait status
        parentActivity.ManagePollingState();
    }

    @Override
    protected Void doInBackground(Void... params) {

        try {
            this.PollingRequest();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //reset polling flag
        endPolling = false;

        return null;
    }

    @Override
    protected void onPostExecute(Void v) {
        super.onPostExecute(v);

        //set preference file with the valid data
        userSettingsEditor.putString(this.parentActivity.getString(R.string.USER_BIKE_STATION_ID), myPUStationNumber);
        userSettingsEditor.putString(this.parentActivity.getString(R.string.USER_BIKE_PLACE_ID), myPUStationPlace);
        userSettingsEditor.putInt(this.parentActivity.getString(R.string.USER_STATUS), myPUStatus);
        userSettingsEditor.commit();

//        //Debug code begin
//        userSettingsEditor = userSettings.edit();
//        userSettingsEditor.putInt(this.parentActivity.getString(R.string.USER_STATUS), 0);
////        userSettingsEditor.putString(this.parentActivity.getString(R.string.USER_BIKE_STATION_ID), "2");
//        userSettingsEditor.commit();
//        //Debug code end


        //Invoke checkStatus Method from PersonalActivity
        parentActivity.checkMyPUStatus();

        //manage NFC action when data are valid
        if(userSettings.getBoolean(parentActivity.getString(R.string.USER_NFC_ACTIVATION), false)){
            // Disable NFC activation flag
            userSettingsEditor = userSettings.edit();
            userSettingsEditor.putBoolean(parentActivity.getString(R.string.USER_NFC_ACTIVATION), false);
            userSettingsEditor.commit();

            // if nfc is detected first check user status and decide to perform sign in or lock in
            if(userSettings.getInt(parentActivity.getString(R.string.USER_STATUS), 0) == 1) {
                //user locked-in => perform lock-out
                Button lockOut = (Button) parentActivity.findViewById(R.id.buttonLockOut);
                lockOut.performClick();
            } else if (userSettings.getInt(parentActivity.getString(R.string.USER_STATUS), 0) == 0){
                //user locked-out => perform lock-in
                parentActivity.myBPStationNumber.setText(userSettings.getString(parentActivity.getString(R.string.USER_NFC_STATION_ID), null));
                parentActivity.myBPStationPlace.setText(userSettings.getString(parentActivity.getString(R.string.USER_NFC_PLACE_ID), null));

                Button lockIn= (Button) parentActivity.findViewById(R.id.buttonLockIn);
                lockIn.performClick();
            }
        }

    }


    public JSONObject MakePostRequestToMyBPServer(String myurl) throws IOException {

        InputStream is = null;
        JSONObject MyPUInfoSendedJson = null;
        JSONObject MyPUInfoReceivedJson = null;

        try {

            URL url = new URL(myurl);
            final HttpURLConnection conn = (HttpURLConnection) url.openConnection();


            //create Json object to send inside POST request
            MyPUInfoSendedJson  = new JSONObject();

            MyPUInfoSendedJson.put("user_code", userSettings.getString(this.parentActivity.getString(R.string.USER_USER_CODE), null /*default value*/));
            MyPUInfoSendedJson.put("pwd_code", userSettings.getString(this.parentActivity.getString(R.string.USER_PWD_CODE), null /*default value*/));
            MyPUInfoSendedJson.put("registration_id", userSettings.getString(this.parentActivity.getString(R.string.USER_REGID), null /*default value*/));


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
            wr.write(MyPUInfoSendedJson.toString());

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

            MyPUInfoReceivedJson = new JSONObject(response.toString());

        } catch (IOException | JSONException e){
            e.printStackTrace();
            return null;
        } finally {
            if (is != null) {
                is.close();
            }
        }

        return MyPUInfoReceivedJson;

    }

    public void ManageReceivedJson(JSONObject JsonResponse){

        try {

            JSONObject MyPUInfoReceivedJson = JsonResponse;

            //Check data-valid from server
            if(MyPUInfoReceivedJson.getString("data_valid").equals("1")){
                //the received data are valid
                //enable endPolling Flag
                endPolling = true;

                //save obtained data
                myPUStationNumber = MyPUInfoReceivedJson.getString("station_id");
                myPUStationPlace = MyPUInfoReceivedJson.getString("place_id");
                switch(MyPUInfoReceivedJson.getString("status")){
                    case "0":
                        myPUStatus = 0; //user with bike no yet locked
                        break;
                    case "1":
                        myPUStatus = 1; //user with bike already locked
                        break;
                    default:
                        myPUStatus = -1; //error -1
                        break;
                }

            }
        } catch (JSONException e) {
            e.printStackTrace();
            endPolling = true; //error
        }
    }

    public void PollingRequest() throws IOException {

        while (endPolling == false) {

            try {

                //Wait for 1 sec
                Thread.sleep(1000);

                //Make Request to server
                JSONObject JsonResponse = this.MakePostRequestToMyBPServer(MYBPSERVER_GET_INFO_URL);
                this.ManageReceivedJson(JsonResponse);

            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}