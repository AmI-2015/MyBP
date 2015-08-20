package com.dev.ami2015.mybikeplace;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.dev.ami2015.mybikeplace.R;
import com.dev.ami2015.mybikeplace.RoomMarker;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Zephyr on 19/08/2015.
 */

public class RoomAdapter extends ArrayAdapter<RoomMarker> implements Filterable{

    private ArrayList<RoomMarker> originalData = null;
    private ArrayList<RoomMarker> filteredData = null;

    public RoomAdapter(Context context, ArrayList<RoomMarker> rooms) {
        super(context, 0, rooms);
        originalData = rooms;
        filteredData = rooms;
    }

    //For this helper method, return based on filteredData
    public int getCount()
    {
        return filteredData.size();
    }

    //This should return a data object, not an int
    public RoomMarker getItem(int position)
    {
        return filteredData.get(position);
    }

    public long getItemId(int position)
    {
        return position;
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

    @Override
    public Filter getFilter() {
        Filter roomFilter = new Filter(){

            @Override
            protected FilterResults performFiltering(CharSequence charSequence)
            {
                FilterResults results = new FilterResults();

                //If there's nothing to filter on, return the original data for your list
                if(charSequence == null || charSequence.length() == 0)
                {
                    results.values = originalData;
                    results.count = originalData.size();
                }
                else
                {
                    ArrayList<RoomMarker> filterResultsData = new ArrayList<RoomMarker>();

                    for(RoomMarker data : originalData)
                    {
                        //In this loop, you'll filter through originalData and compare each item to charSequence.
                        //If you find a match, add it to your new ArrayList
                        //I'm not sure how you're going to do comparison, so you'll need to fill out this conditional
                        if( data.markerName.toLowerCase().startsWith(charSequence.toString().toLowerCase()))
                        {
                            filterResultsData.add(data);
                        }
                    }

                    results.values = filterResultsData;
                    results.count = filterResultsData.size();
                }

                return results;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults)
            {
                filteredData = (ArrayList<RoomMarker>)filterResults.values;
                notifyDataSetChanged();
            }
        };

        return roomFilter;
    }
}