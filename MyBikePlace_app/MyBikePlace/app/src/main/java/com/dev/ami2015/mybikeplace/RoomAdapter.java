package com.dev.ami2015.mybikeplace;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.dev.ami2015.mybikeplace.R;
import com.dev.ami2015.mybikeplace.RoomMarker;

import java.util.ArrayList;

/**
 * Created by Zephyr on 19/08/2015.
 */
public class RoomAdapter extends ArrayAdapter<RoomMarker> {
    public RoomAdapter(Context context, ArrayList<RoomMarker> rooms) {
        super(context, 0, rooms);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        RoomMarker room = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.roommarker_row, parent, false);
        }
        // Lookup view for data population
        TextView roomName = (TextView) convertView.findViewById(R.id.roomName);
        TextView roomDescription= (TextView) convertView.findViewById(R.id.roomDescription);
        // Populate the data into the template view using the data object
        roomName.setText(room.markerName);
        roomDescription.setText(room.markerDescription);
        // Return the completed view to render on screen
        return convertView;
    }
}