package com.dev.ami2015.mybikeplace;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.dev.ami2015.mybikeplace.tasks.GetRoomMarkersTask;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;


public class RoomActivity extends ActionBarActivity {

    ArrayList<RoomMarker> roomsMarkers;
//    public HashMap<RoomMarker, Marker> roomMarkersHM= new HashMap<RoomMarker, Marker>(); //HM => HashMap

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);

        //load all the room markers with an asyncTask
        new GetRoomMarkersTask(this).execute();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_room, menu);
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

    //rooms marker are received from PoliOrari API and inserted in map
    public void PutAllRoomMarkersInList(ArrayList<RoomMarker> receivedRoomsMarkers){

        roomsMarkers = receivedRoomsMarkers;

        //check downloaded data
        if(roomsMarkers == null){
            //download error
            Toast.makeText(this, "Room download error, try again.", Toast.LENGTH_LONG).show();
            //do something with the list
        } else {
            //populate the list with data
        }

//        //debug code
//        int len = roomsMarkers.size();
//        for (int i = 0; i < len; i++){
//
//            //setting room marker without making it visible
//            currentMarker = map.addMarker(new MarkerOptions().title(roomsMarkers.get(i).markerName).snippet(roomsMarkers.get(i).markerDescription).position(roomsMarkers.get(i).GetPosition()).visible(false));
//
//            roomMarkersHM.put(roomsMarkers.get(i), currentMarker);
//
//        }

    }
}
