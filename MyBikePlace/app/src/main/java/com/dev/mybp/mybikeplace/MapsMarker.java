package com.dev.mybp.mybikeplace;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Zephyr on 19/05/2015.
 */
public class MapsMarker {

    public String markerName;
    public String markerDescription;
    public LatLng markerLatLng;

    //costruttore
    public MapsMarker(String markerName, String markerDescription, double Latitude, double Longitude){
        this.markerName = markerName;
        this.markerDescription = markerDescription;
        this.markerLatLng = new LatLng(Latitude, Longitude);
    }

}
