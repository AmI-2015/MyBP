package com.dev.ami2015.mybikeplace.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.dev.ami2015.mybikeplace.RoomMarker;
import com.dev.ami2015.mybikeplace.MapsActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Zephyr on 25/05/2015.
 */
// Uses AsyncTask to create a task away from the main UI thread. This task takes a
// URL string and uses it to create an HttpUrlConnection. Once the connection
// has been established, the AsyncTask downloads the contents of the webpage as
// an InputStream. Finally, the InputStream is converted into a string, which is
// displayed in the UI by the AsyncTask's onPostExecute method.

public class GetRoomMarkersTask extends AsyncTask</*params*/ Void, /*progress not used*/ Void, /*result*/ ArrayList<RoomMarker>> {

    public static final String POLIORARI_URL ="http://www.swas.polito.it/dotnet/orari_lezione_pub/mobile/ws_orari_mobile.asmx/get_elenco_aule";
    public static final String DEBUG_TAG = "HttpExample";

    public MapsActivity parentActivity;


    //costructor receives as parameter the parent activity that started the task
    public GetRoomMarkersTask(MapsActivity activity){
        this.parentActivity = activity;
    }

    @Override
    protected ArrayList<RoomMarker> doInBackground(Void... params) {

        ArrayList<RoomMarker> roomMarker = null;

        // params comes from the execute() call: params[0] is the url.
        try {

            JSONObject jsonObjectRooms = MakePostRequestToPoliOrari(POLIORARI_URL);
            JSONArray jsonArrayRooms = GetJsonRoomsList(jsonObjectRooms);

            roomMarker = GetRoomsMapMarkers(jsonArrayRooms);

            return roomMarker;

        } catch (IOException e) {
            return null;
        } catch (JSONException e) {
            e.printStackTrace();
            return roomMarker;
        }
    }

    @Override
    protected void onPostExecute(ArrayList<RoomMarker> roomMarkers) {
        super.onPostExecute(roomMarkers);

        //we don't need to load rooms marker directly inside maps
        parentActivity.setAllRoomMarkerInMap(parentActivity.getMap(), roomMarkers);

    }

    // Given a URL, establishes an HttpUrlConnection and retrieves
    // the web page content as a InputStream, which it returns as
    // a string.

    public ArrayList<RoomMarker> GetRoomsMapMarkers (JSONArray jsonRoomsList) throws JSONException {

        ArrayList<RoomMarker> roomsMapMarkersList = new ArrayList<RoomMarker>();

        int len = jsonRoomsList.length();

        // ciclo l'intero JSONArry per ottenere ogni json Object corrispondente ad una room
        // e la "casto" in un map marker

        for (int i = 0; i < len; i++ ){

            try {
                JSONObject singleJsonRoom = jsonRoomsList.getJSONObject(i);

                // metto il json room in un oggetto MapMarker e l'aggiungo alla lista

                String roomName = singleJsonRoom.getString("aula");
                if (Character.isDigit(roomName.charAt(0)) == true) {
                    //the room is a proper room, need to add "Aula "
                    roomName = "Aula "+roomName;
                }
                String roomLocation = "Sito: "+singleJsonRoom.getString("sito");
                String roomLatString = singleJsonRoom.getString("lat");
                String roomLonString = singleJsonRoom.getString("lon");

                //adapt to double format
                roomLatString = roomLatString.replace(",",".");
                roomLonString = roomLonString.replace(",",".");

                //convert to Double
                Double roomLat = Double.valueOf(roomLatString);
                Double roomLon = Double.valueOf(roomLonString);

                // public RoomMarker(String markerName, String markerDescription, double Latitude, double Longitude){
                roomsMapMarkersList.add(new RoomMarker(roomName, roomLocation, roomLat, roomLon));



            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        }

        return roomsMapMarkersList;
    }

    public JSONArray GetJsonRoomsList(JSONObject jsonRooms){

        JSONArray jsonRoomsList = null;

        try{

            // convert unique json object to a list of json objects
            // each item is a Room json object

            jsonRoomsList = jsonRooms.getJSONArray("d");


        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        return jsonRoomsList;
    }


    public JSONObject MakePostRequestToPoliOrari(String myurl) throws IOException {

        InputStream is = null;
        JSONObject jsonRooms = null;

        try {

            URL url = new URL(myurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            // Set request nature and parameters
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoInput(true);

            // Starts the query
            conn.connect();

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

            jsonRooms = new JSONObject(response.toString());

        } catch (IOException | JSONException e){
            e.printStackTrace();
            return null;
        } finally {
            if (is != null) {
                is.close();
            }
        }

        return jsonRooms;

    }
}
