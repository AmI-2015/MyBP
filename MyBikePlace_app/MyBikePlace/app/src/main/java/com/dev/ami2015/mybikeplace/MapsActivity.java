package com.dev.ami2015.mybikeplace;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.dev.ami2015.mybikeplace.tasks.GetMyBPStationMarkersTask;
import com.dev.ami2015.mybikeplace.tasks.GetRoomMarkersTask;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;

import java.util.ArrayList;
import java.util.HashMap;


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    Intent mapsIntent;
    ArrayList<RoomMarker> roomsMarkers;
    GoogleMap googleMap;

    public ArrayList<MyBPStationMarker> myBPStationMarkers;
    public HashMap<MyBPStationMarker, Marker> myBPStationMarkersHM = new HashMap<MyBPStationMarker, Marker>(); //HM => HashMap
    public HashMap<RoomMarker, Marker> roomMarkersHM= new HashMap<RoomMarker, Marker>(); //HM => HashMap

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    @Override
    public void onMapReady(GoogleMap map) {

        //catch the map object
        this.googleMap = map;

        map.setMyLocationEnabled(true);

//        // Setting the MyBPStationInfoWindowAdapter to add the right infoWindow
//        googleMap.setInfoWindowAdapter(new MyBPStationInfoWindowAdapter());


        //set initial marker: Politecnico di Torino
        RoomMarker poliMarker = new RoomMarker("Politecnico", "Universita' di Torino", 45.062936, 7.660747);

        //don't show politecnico marker
        //setMarkerInMap(map, poliMarker);

        //move camera to initial marker
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(poliMarker.GetPosition(), 13));

        map.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        //load all the room markers with an asyncTask
        new GetRoomMarkersTask(this).execute();

        //load all the MyBP Stations markers with an asyncTask
        new GetMyBPStationMarkersTask(this).execute();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_maps, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch(id){

            case R.id.action_settings:
                return true;
            case R.id.action_mybp_station_next_to_me:
                ShowNearestMyBPStationToMe();
                return true;
            case R.id.action_sign_in:
                //Opening shared preference file
                SharedPreferences userSettings = this.getSharedPreferences(getString(R.string.USER_SETTINGS), Context.MODE_PRIVATE);
                SharedPreferences.Editor userSettingsEditor = userSettings.edit();
                //Clear skip sign-in checkbox
                userSettingsEditor.putBoolean(getString(R.string.USER_SKIP), false);
                userSettingsEditor.commit();
                //Start SignIn Activity
                mapsIntent = new Intent(this, SignInActivity.class);
                startActivity(mapsIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }

    }

    //rooms marker are received from PoliOrari API and inserted in map
    public void setAllRoomMarkerInMap(GoogleMap map, ArrayList<RoomMarker> receivedRoomsMarkers){

        Marker currentMarker;

        roomsMarkers = receivedRoomsMarkers;

        int len = roomsMarkers.size();
        for (int i = 0; i < len; i++){

            //setting room marker without making it visible
            currentMarker = map.addMarker(new MarkerOptions().title(roomsMarkers.get(i).markerName).snippet(roomsMarkers.get(i).markerDescription).position(roomsMarkers.get(i).GetPosition()).visible(false));

            roomMarkersHM.put(roomsMarkers.get(i), currentMarker);

        }

    }


    //MyBP station markers are received from MyBPServer API and inserted in map
    public void setAllMyBPStationMarkerInMap(GoogleMap map, ArrayList<MyBPStationMarker> receivedMyBPStationsMarkers) {

        Marker currentMarker;

        int len = receivedMyBPStationsMarkers.size();
        for (int i = 0; i < len; i++) {

            String markerName = String.valueOf(receivedMyBPStationsMarkers.get(i).stationID);
            String markerDescription = String.valueOf(receivedMyBPStationsMarkers.get(i).freePlaces) + " / " +
                    String.valueOf(receivedMyBPStationsMarkers.get(i).totalPlaces);

            // Setting the MyBPStationInfoWindowAdapter to add the right infoWindow
            googleMap.setInfoWindowAdapter(new MyBPStationInfoWindowAdapter());

            currentMarker = map.addMarker(new MarkerOptions().title(markerName).snippet(markerDescription).position(receivedMyBPStationsMarkers.get(i).GetPosition()));

            myBPStationMarkersHM.put(receivedMyBPStationsMarkers.get(i), currentMarker);

        }
    }

    // Shows the nearest MyBPStation to the user with available places
    public void ShowNearestMyBPStationToMe(){

        MyBPStationMarker nearestMyBPStationToMe = NearestMyBPStationToMe();
        Marker marker = myBPStationMarkersHM.get(nearestMyBPStationToMe);

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 17));

        marker.showInfoWindow();

    }

    // Searches the nearest myBpstation to the user position only between stations with free places available
    public MyBPStationMarker NearestMyBPStationToMe(){

        // save my location
        Location myLocation = googleMap.getMyLocation();

//        //Debug my Location
//        Location myLocation = new Location("");
//        myLocation.setLatitude(45.062936);
//        myLocation.setLongitude(7.660747);

        Location testedLocation = new Location("");
        MyBPStationMarker nearestMyBPStation = null;

        // finds all stations with available free places
        ArrayList<MyBPStationMarker> myBPStationWithFreePlaces = FindFreeStations();

        int len = myBPStationWithFreePlaces.size();

        // nearestMyBPStation initialization

        testedLocation.setLatitude(myBPStationWithFreePlaces.get(0).stationLat);
        testedLocation.setLongitude(myBPStationWithFreePlaces.get(0).stationLng);
        nearestMyBPStation = myBPStationWithFreePlaces.get(0);

        double distance = myLocation.distanceTo(testedLocation);

        for(int i = 1; i < len; i++){

            //update testedLocation
            testedLocation.setLatitude(myBPStationWithFreePlaces.get(i).stationLat);
            testedLocation.setLongitude(myBPStationWithFreePlaces.get(i).stationLng);

            if(myLocation.distanceTo(testedLocation) < distance){

                distance = myLocation.distanceTo(testedLocation);
                nearestMyBPStation = myBPStationWithFreePlaces.get(i);

            }
        }

        return nearestMyBPStation;
    }

    // searches MyBP stations with available free places
    public ArrayList<MyBPStationMarker> FindFreeStations(){

        ArrayList<MyBPStationMarker> freeStation = new ArrayList<MyBPStationMarker>();
        MyBPStationMarker tmpMyBPStation;

        int len = myBPStationMarkers.size();
        for(int i = 0; i < len; i++){

            tmpMyBPStation = myBPStationMarkers.get(i);

            //add to the new MyBP Station list only station with free places available
            if(tmpMyBPStation.freePlaces > 0 ){
                freeStation.add(tmpMyBPStation);
            }
        }

        return freeStation;
    }

    public class MyBPStationInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

        // Constructor
        public MyBPStationInfoWindowAdapter()
        {
        }

        // use custom frame
        @Override
        public View getInfoWindow(Marker marker) {
            return null;
        }

        // Defines the contents of the InfoWindow
        @Override
        public View getInfoContents(Marker marker) {

            // Getting view from the layout file info_window_layout
            View infoWindow = getLayoutInflater().inflate(R.layout.markerwindow_layout, null);

            // Getting reference to the TextView to set station number
            TextView stationNumber = (TextView) infoWindow.findViewById(R.id.markerwindow_mybp_station_number);

            // Getting reference to the TextView to set free places
            TextView freePlaces = (TextView) infoWindow.findViewById(R.id.markerwindow_free_places);

            // Getting reference to the TextView to set total places
            TextView totPlaces = (TextView) infoWindow.findViewById(R.id.markerwindow_tot_places);

            // Setting station number text
            stationNumber.setText("MyBP Station n.: " + marker.getTitle());
            stationNumber.setAllCaps(true);

            // Extracting free places string from snipper string

            String snippetStr = marker.getSnippet();
            String freePlacesStr = "";
            String totPlacesStr = "";
            char tmpChr;
            boolean snippetSecondHalf = false;

            for(int i = 0; i < snippetStr.length(); i++){

                tmpChr = snippetStr.charAt(i);
                if((tmpChr == '/') && (snippetSecondHalf == false)){
                    snippetSecondHalf = true;
                }

                if((snippetSecondHalf == false) && (tmpChr != '/')){
                    freePlacesStr += Character.toString(tmpChr);
                }

                if((snippetSecondHalf == true) && (tmpChr != '/')){
                    totPlacesStr += Character.toString(tmpChr);
                }
            }

            // Setting free places text
            freePlaces.setText("Free places: " + freePlacesStr);

            // Setting totale places text
            totPlaces.setText("Total places: " + totPlacesStr);

            // Returning the view containing InfoWindow contents
            return infoWindow;

        }

    }

    public GoogleMap getMap() {
        return googleMap;
    }


}
