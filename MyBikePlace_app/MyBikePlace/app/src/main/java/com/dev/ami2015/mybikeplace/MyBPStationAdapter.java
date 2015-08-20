package com.dev.ami2015.mybikeplace;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Zephyr on 20/08/2015.
 */
public class MyBPStationAdapter extends ArrayAdapter<MyBPStationMarker> {

    public MyBPStationAdapter(Context context, ArrayList<MyBPStationMarker> stations) {
        super(context, 0, stations);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        MyBPStationMarker station = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.station_row, parent, false);
        }

        // Lookup view for data population
        TextView stationID = (TextView) convertView.findViewById(R.id.stationNumber);
        TextView stationFreePlaces= (TextView) convertView.findViewById(R.id.stationFreePlaces);
        TextView stationTotPlaces= (TextView) convertView.findViewById(R.id.stationTotPlaces);

        if(station.stationID == -1){
            // download error
            stationID.setText("Download error");
            stationFreePlaces.setText("null");
            stationTotPlaces.setText("null");
        } else {
            // Populate the data into the template view using the data object
            stationID.setText("Station  " + Integer.toString(station.stationID));
            stationFreePlaces.setText("Free places: " + Integer.toString(station.freePlaces));
            stationTotPlaces.setText("Total places: " + Integer.toString(station.totalPlaces));
        }

        // Set Row background color depending on Free/Tot places
        if (station.freePlaces > 0){
            convertView.setBackgroundColor(getContext().getResources().getColor(R.color.green));
        } else {
            convertView.setBackgroundColor(getContext().getResources().getColor(R.color.red));
        }

        // Return the completed view to render on screen
        return convertView;
    }
}