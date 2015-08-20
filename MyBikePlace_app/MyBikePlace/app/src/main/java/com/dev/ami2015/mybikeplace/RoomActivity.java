package com.dev.ami2015.mybikeplace;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.dev.ami2015.mybikeplace.tasks.GetRoomMarkersTask;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;


public class  RoomActivity extends ActionBarActivity {

    ArrayList<RoomMarker> roomsMarkers;
//    public HashMap<RoomMarker, Marker> roomMarkersHM= new HashMap<RoomMarker, Marker>(); //HM => HashMap
    RoomActivity context;
    Intent roomActivityIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);

        //load all the room markers with an asyncTask
        new GetRoomMarkersTask(this).execute();
        context = this;

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
        if (id == R.id.action_update_rooms) {
            new GetRoomMarkersTask(this).execute();
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
            Toast.makeText(this, "Room download error, try update rooms.", Toast.LENGTH_LONG).show();
            RoomMarker errorMarker = new RoomMarker("download error","no data", -1.0,-1.0);
            roomsMarkers = new ArrayList<RoomMarker>();
            roomsMarkers.add(errorMarker);
            //do something with the list
        }

        //populate the list with data
        // Create the adapter to convert the array to views
        final RoomAdapter adapter = new RoomAdapter(this, roomsMarkers);
        // Attach the adapter to a ListView
        ListView listView = (ListView) findViewById(R.id.roomList);
        listView.setAdapter(adapter);

        //enable click on item of the list
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final RoomMarker selectedRoom = (RoomMarker) parent.getItemAtPosition(position);
//                Toast.makeText(context, (CharSequence) selectedRoom.markerName, Toast.LENGTH_LONG).show();
                //when a room is selected maps activity is opened and the correct station is displayed
                roomActivityIntent = new Intent(context, MapsActivity.class);
                roomActivityIntent.putExtra("Name", selectedRoom.markerName);
                roomActivityIntent.putExtra("Description", selectedRoom.markerDescription);
                roomActivityIntent.putExtra("Latitude", selectedRoom.markerLat);
                roomActivityIntent.putExtra("Longitude", selectedRoom.markerLng);
                roomActivityIntent.putExtra(SignInActivity.EXTRA_CALL_FROM, "RoomActivity");
                startActivity(roomActivityIntent);

            }
        });

        //enables filtering for the contents of the given ListView
        listView.setTextFilterEnabled(true);

        EditText roomFilter = (EditText) findViewById(R.id.roomFilter);
        roomFilter.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.getFilter().filter(s.toString());
            }
        });
    }
}
