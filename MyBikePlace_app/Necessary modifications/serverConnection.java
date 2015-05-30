package com.dev.mybp.mybikeplace;


import android.os.AsyncTask;

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


public class serverConnection extends AsyncTask<String, Void, JSONObject> {

    JSONObject jsonObj = new JSONObject();

    @Override
    protected void onPostExecute(JSONObject obj) {
        super.onPostExecute(obj);

        try {
            String user_code = obj.getString("user_code");
            this.jsonObj.put("user_code", user_code);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            String pwd_code = obj.getString("pwd_code");
            this.jsonObj.put("pwd_code", pwd_code);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            String error_str = obj.getString("error_str");
            this.jsonObj.put("registraion_id", error_str);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected JSONObject doInBackground(String... params) {
        StringBuffer jsonBody = null;
        JSONObject data = null;
        JSONObject responseJSON = null;

        try {


            URL url = new URL(params[0]);
            data = new JSONObject(params[1].toString());

            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            con.setDoOutput(true);

            con.setDoInput(true);

            con.setRequestMethod("POST");


            con.setRequestProperty("Content-Type", "application/json");

            con.setRequestProperty("charset", "utf-8");

            con.setRequestProperty("Accept", "application/json");

            con.setUseCaches(false);

            OutputStream outputStreamconn = con.getOutputStream();


            OutputStreamWriter wr = new OutputStreamWriter(outputStreamconn);

            wr.write(data.toString());


            wr.flush();

            //display what returns the POST request

            StringBuilder sb = new StringBuilder();

            int HttpResult = con.getResponseCode();

            if (HttpResult == HttpURLConnection.HTTP_OK) {

                BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"));

                String line = null;
                jsonBody = new StringBuffer();
                while ((line = br.readLine()) != null) {
                    sb.append(line + "\n");
                    jsonBody.append(line);
                }

                responseJSON = new JSONObject(jsonBody.toString());
                br.close();
            }
        } catch (IOException e) {
            // print the error
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

//        try {
//            String user_code = responseJSON.getString("user_code");
//            String pwd_code = responseJSON.getString("pwd_code");
//            String error_str = responseJSON.getString("error_str");
//            setDataJson(user_code, pwd_code, error_str);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }

        return responseJSON;
    }

//    public void setDataJson ( String ... params) throws JSONException {
//
//        this.jsonObj.put("user_code", params[0]);
//        this.jsonObj.put("pwd_code", params[1]);
//        this.jsonObj.put("registraion_id", params[2]);
//    }

    public JSONObject getDataJson () {
        return this.jsonObj;
    }
}
