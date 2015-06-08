package com.dev.ami2015.mybikeplace;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.dev.ami2015.mybikeplace.tasks.GetMyBPStationMarkersTask;
import com.dev.ami2015.mybikeplace.tasks.GetRoomMarkersTask;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;

import java.util.ArrayList;
import java.util.List;


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    Intent mapsIntent;
    ArrayList<MapsMarker> roomsMarkers;
    public ArrayList<MyBPStationMarker> myBPStationMarkers;
    GoogleMap googleMap;

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

        //map.setMyLocationEnabled(true);

        //set initial marker: Politecnico di Torino
        MapsMarker poliMarker = new MapsMarker("Politecnico", "Universita' di Torino", 45.062936, 7.660747);

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

        Intent actionIntent;
        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            Intent i = new Intent(this, TestHTTPActivity.class);
//            startActivity(i);
//            return true;
//        }

        switch(id){

            case R.id.action_settings:
                return true;
            case R.id.action_mybp_station_next_to_me:
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(NearestMyBPStationToMe().GetPosition(), 17));
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }

    }

    public void setMarkerInMap(GoogleMap map, MapsMarker mapsMarker){

        map.addMarker(new MarkerOptions().title(mapsMarker.markerName).snippet(mapsMarker.markerDescription).position(mapsMarker.GetPosition()));

    }

    public void setAllRoomMarkerInMap(GoogleMap map, ArrayList<MapsMarker> receivedRoomsMarkers){

        //rooms marker are received from PoliOrari API and inserted in map

        roomsMarkers = receivedRoomsMarkers;

        int len = roomsMarkers.size();
        for(int i = 0; i < len; i++){

            setMarkerInMap(map, roomsMarkers.get(i));
        }

    }

    public void setMyBPStationMarkerInMap(GoogleMap map, MyBPStationMarker myBPStationMarker){

        String markerName = "MyBP Station number: " + String.valueOf(myBPStationMarker.stationID);
        String markerDescription = "Free places: " + String.valueOf(myBPStationMarker.freePlaces)
                + "\n" + "Total places: " + String.valueOf(myBPStationMarker.totalPlaces);

        map.addMarker(new MarkerOptions().title(markerName).snippet(markerDescription).position(myBPStationMarker.GetPosition()));

    }

    public void setAllMyBPStationMarkerInMap(GoogleMap map, ArrayList<MyBPStationMarker> receivedMyBPStationsMarkers){

        //MyBP station markers are received from MyBPServer API and inserted in map

        myBPStationMarkers = receivedMyBPStationsMarkers;

        int len = myBPStationMarkers.size();
        for(int i = 0; i < len; i++){

            setMyBPStationMarkerInMap(map, myBPStationMarkers.get(i));
        }

    }

    // searches MyBP station with available free places
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

    // searches the nearest myBpstation to the user position only between stations with free places available
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

    public GoogleMap getMap() {
        return googleMap;
    }
}
