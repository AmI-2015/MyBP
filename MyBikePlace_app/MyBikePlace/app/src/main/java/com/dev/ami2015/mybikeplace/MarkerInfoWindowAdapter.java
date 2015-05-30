package com.dev.ami2015.mybikeplace;

import android.view.View;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

/**
 * Created by Zephyr on 30/05/2015.
 */
public class MarkerInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    private final View mymarkerview;

    MarkerInfoWindowAdapter() {
        mymarkerview = getLayoutInflater().inflate(R.layout.markerwindow_layout, null);
    }

    public View getInfoWindow(Marker marker) {
        render(marker, mymarkerview);
        return mymarkerview;
    }

    public View getInfoContents(Marker marker) {
        return null;
    }

    private void render(Marker marker, View view) {
        // Add the code to set the required values
        // for each element in your custominfowindow layout file
    }

}
