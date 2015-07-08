//package com.dev.ami2015.mybikeplace.tasks;
//
//import android.os.AsyncTask;
//import android.util.Log;
//
//import com.dev.ami2015.mybikeplace.MapsActivity;
//import com.dev.ami2015.mybikeplace.MyBPStationMarker;
//import com.dev.ami2015.mybikeplace.PersonalActivity;
//
//import org.json.JSONArray;
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.net.HttpURLConnection;
//import java.net.URL;
//import java.util.ArrayList;
//
///**
// * Created by Zephyr on 01/06/2015.
// */
//public class GetUsersInfoTask extends AsyncTask</*params*/ Void, /*progress not used*/ Void, /*result*/ Void> {
//
//    public static final String MYBPSERVER_URL ="http://192.168.56.1:7000/myBP_server/users/get_status";
//    public static final String DEBUG_TAG = "HttpExample";
//
//    public PersonalActivity parentActivity;
//
//    //Variable to set in Personal Activity
//    String myBPStationNumber;
//    String myBPStationPlace;
//
//    //costructor receives as parameter the parent activity that started the task
//    public GetUsersInfoTask(PersonalActivity activity){
//        this.parentActivity = activity;
//    }
//
//    @Override
//    protected Void doInBackground(Void... params) {
//
//
//        // params comes from the execute() call: params[0] is the url.
//        try {
//
//            JSONObject jsonObjectMyPUStatus = MakePostRequestToMyBPServer(MYBPSERVER_URL);
//            JSONArray jsonArrayMyBPStations = GetJsonMyBPStationList(jsonObjectMyBPStations);
//
//
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }
//
//    @Override
//    protected void onPostExecute() {
//        super.onPostExecute( );
//
//
//        //set view element content in Personal Activity
//        parentActivity.myBPStationNumber.setText(myBPStationNumber);
//        parentActivity.myBPStationPlace.setText(myBPStationPlace);
//
//    }
//
//    // Given a URL, establishes an HttpUrlConnection and retrieves
//    // the web page content as a InputStream, which it returns as
//    // a string.
//
//    public ArrayList<MyBPStationMarker> GetMyBPStationMapMarkers (JSONArray jsonMyBPStationList) throws JSONException {
//
//        ArrayList<MyBPStationMarker> myBPStationsMapMarkersList = new ArrayList<MyBPStationMarker>();
//
//        int len = jsonMyBPStationList.length();
//
//        // ciclo l'intero JSONArry per ottenere ogni json Object corrispondente ad una room
//        // e la "casto" in un map marker
//
//        for (int i = 0; i < len; i++ ){
//
//            try {
//                JSONObject singleJsonMyBPStation = jsonMyBPStationList.getJSONObject(i);
//
//                // metto il json room in un oggetto MapMarker e l'aggiungo alla lista
//
//                String stationIDString = singleJsonMyBPStation.getString("station_id");
//                String stationLatString = singleJsonMyBPStation.getString("latitude");
//                String stationLonString = singleJsonMyBPStation.getString("longitude");
//                String stationTotPlacesString = singleJsonMyBPStation.getString("tot_places");
//                String stationFreePlacesString = singleJsonMyBPStation.getString("free_places");
//
//                //convert to Integer
//                int stationID = Integer.valueOf(stationIDString);
//                int stationTotPlaces = Integer.valueOf(stationTotPlacesString);
//                int stationFreePlaces = Integer.valueOf(stationFreePlacesString);
//
//                //convert to Double
//                Double stationLat = Double.valueOf(stationLatString);
//                Double stationLon = Double.valueOf(stationLonString);
//
//                // public RoomMarker(String markerName, String markerDescription, double Latitude, double Longitude){
//                myBPStationsMapMarkersList.add(new MyBPStationMarker(stationID, stationLat, stationLon, stationTotPlaces, stationFreePlaces));
//
//
//
//            } catch (JSONException e) {
//                e.printStackTrace();
//                return null;
//            }
//        }
//
//        return myBPStationsMapMarkersList;
//    }
//
//    public JSONArray GetJsonMyBPStationList(JSONObject jsonMyBPStations){
//
//        JSONArray jsonMyBPStationsList = null;
//
//        try{
//
//            // convert unique json object to a list of json objects
//            // each item is a Room json object
//
//            jsonMyBPStationsList = jsonMyBPStations.getJSONArray("d");
//
//
//        } catch (JSONException e) {
//            e.printStackTrace();
//            return null;
//        }
//
//        return jsonMyBPStationsList;
//    }
//
//
//    public JSONObject MakePostRequestToMyBPServer(String myurl) throws IOException {
//
//        InputStream is = null;
//        JSONObject jsonMyBPStations = null;
//
//        try {
//
//            URL url = new URL(myurl);
//            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//
//            // Set request nature and parameters
//            conn.setRequestMethod("POST");
//            conn.setRequestProperty("Content-Type", "application/json");
//            conn.setDoInput(true);
//
//            // Starts the query
//            conn.connect();
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
//            jsonMyBPStations = new JSONObject(response.toString());
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
//        return jsonMyBPStations;
//
//    }
//}