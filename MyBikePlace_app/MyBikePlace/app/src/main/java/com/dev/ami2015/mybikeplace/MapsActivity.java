package com.dev.ami2015.mybikeplace;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.dev.ami2015.mybikeplace.tasks.GetRoomMarkersTask;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;

import java.util.ArrayList;


public class MapsActivity extends Activity implements OnMapReadyCallback {

    Intent mapsIntent;
    ArrayList<MapsMarker> roomsMarkers;
    ArrayList<MyBPStationMarker> myBPStationMarkers;
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

        map.setMyLocationEnabled(true);

        //set initial marker: Politecnico di Torino
        MapsMarker poliMarker = new MapsMarker("Politecnico", "Universita' di Torino", 45.062936, 7.660747);
        setMarkerInMap(map, poliMarker);

        //move camera to initial marker
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(poliMarker.GetPosition(), 13));

        map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);

        //load all the room marker with an asyncTask
        new GetRoomMarkersTask(this).execute();

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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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

    public GoogleMap getMap() {
        return googleMap;
    }
}
