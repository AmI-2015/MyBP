package com.dev.ami2015.mybikeplace.tasks;

import android.os.AsyncTask;

import com.dev.ami2015.mybikeplace.SignUpActivity;

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

/*
 * Created by root on 06/06/15.
 */

public class signUpConnection extends AsyncTask<String , Void , JSONObject>  {

    public static final String DEBUG_TAG = "HttpExample";

    public SignUpActivity parentActivity;

    //constructor receives as parameter the parent activity that started the task
    public signUpConnection(SignUpActivity activity){
        this.parentActivity = activity;
    }

    @Override
    protected void onPostExecute(JSONObject serverResponse) {
        super.onPostExecute(serverResponse);

        try {
            parentActivity.setServerResponse(serverResponse);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //new GetMyBPStationMarkersTask(this.parentActivity).execute();

    }
    @Override
    protected JSONObject doInBackground(String... params) {
        JSONObject serverResponse= null;

        // params comes from the execute() call: params[0] is the url.
        try {

            serverResponse = MakePostRequestToServer(params[0],params[1]);;

            return serverResponse;

        } catch (IOException e) {
            return null;
        }

    }

    public JSONObject MakePostRequestToServer(String myurl, String headerRqst) throws IOException {

        InputStream is = null;
        JSONObject jsonResponse = null;
        JSONObject data = null;

        try {

            URL url = new URL(myurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            // Set request nature and parameters
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setInstanceFollowRedirects(false);
            conn.setUseCaches(false);
            conn.setRequestProperty("Content-Type", "application/json");//charset=utf-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setConnectTimeout(2000);

            data = new JSONObject(headerRqst);

            // Starts the query
            //conn.connect();

            // Get the HTTP response
            //int responseCode = conn.getResponseCode();
            //Log.d(DEBUG_TAG, "The response is: " + responseCode);

            OutputStream outputStreamconn = conn.getOutputStream();


            OutputStreamWriter wr = new OutputStreamWriter(outputStreamconn);

            wr.write(data.toString());


            wr.flush();

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

            jsonResponse = new JSONObject(response.toString());

        } catch (IOException | JSONException e){
            e.printStackTrace();
            return null;
        } finally {
            if (is != null) {
                is.close();
            }
        }

        return jsonResponse;

    }

}
