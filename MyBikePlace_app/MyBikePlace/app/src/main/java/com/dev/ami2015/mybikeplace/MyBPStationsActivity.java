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

import java.util.ArrayList;
import java.util.Collections;


public class MyBPStationsActivity extends ActionBarActivity {

    ArrayList<MyBPStationMarker> stationMarkers;
    MyBPStationsActivity context;
    Intent stationIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_bpstations);

        context = this;

        //save all the stations to display them in the list
        Intent intent = getIntent();
        stationMarkers = intent.getParcelableArrayListExtra("MyBPStations");

        //check downloaded data
        if(stationMarkers == null){
            //download error
            Toast.makeText(this, "Stations download error, try update.", Toast.LENGTH_LONG).show();
            MyBPStationMarker errorMarker = new MyBPStationMarker(-1, -1.0, -1.0, -1, -1);
            stationMarkers = new ArrayList<MyBPStationMarker>();
            stationMarkers.add(errorMarker);
            //do something with the list
        }


//        Collections.reverse(stationMarkers);

//        //debug stations
//        stationMarkers.add(new MyBPStationMarker(4, 5.5, 6.6, 20, 0));
//        stationMarkers.add(new MyBPStationMarker(-1, 5.5, 6.6, 15, -1));

        PutAllMyBPStationInList();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_my_bpstations, menu);
        return true;
    }


    //station marker are received from MapsActivity
    public void PutAllMyBPStationInList(){

        //populate the list with data
        // Create the adapter to convert the array to views
        final MyBPStationAdapter adapter = new MyBPStationAdapter(this, stationMarkers);
        // Attach the adapter to a ListView
        ListView listView = (ListView) findViewById(R.id.stationList);
        listView.setAdapter(adapter);

        //enable click on item of the list
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final MyBPStationMarker selectedStation = (MyBPStationMarker) parent.getItemAtPosition(position);
//                Toast.makeText(context, (CharSequence) Integer.toString(selectedStation.stationID), Toast.LENGTH_LONG).show();
                //when an existing station is selected maps activity is opened and the station is displayed
                if(selectedStation.stationID != -1) {
                    stationIntent = new Intent(context, MapsActivity.class);
                    stationIntent.putExtra("selectedStation", selectedStation);
                    stationIntent.putExtra(SignInActivity.EXTRA_CALL_FROM, "StationActivity");
                    startActivity(stationIntent);
                } else {
                    Toast.makeText(context, "The selected station doesn't exist, try again.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
