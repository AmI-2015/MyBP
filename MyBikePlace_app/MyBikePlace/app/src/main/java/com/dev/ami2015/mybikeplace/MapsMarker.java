package com.dev.ami2015.mybikeplace;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

public class MapsMarker implements Parcelable {
    public String markerName;
    public String markerDescription;
    public Double markerLat;
    public Double markerLng;

    //costruttore
    public MapsMarker(String markerName, String markerDescription, double Latitude, double Longitude){
        this.markerName = markerName;
        this.markerDescription = markerDescription;
        this.markerLat = Latitude;
        this.markerLng = Longitude;
    }

    /**
     * Use when reconstructing User object from parcel
     * This will be used only by the 'CREATOR'
     * @param in a parcel to read this object
     */
    public MapsMarker(Parcel in) {
        this.markerName = in.readString();
        this.markerDescription = in.readString();
        this.markerLat = in.readDouble();
        this.markerLng = in.readDouble();
    }

    /**
     * Define the kind of object that you gonna parcel,
     * You can use hashCode() here
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Actual object serialization happens here, Write object content
     * to parcel one by one, reading should be done according to this write order
     * @param dest parcel
     * @param flags Additional flags about how the object should be written
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(markerName);
        dest.writeString(markerDescription);
        dest.writeDouble(markerLat);
        dest.writeDouble(markerLng);
    }

    /**
     * This field is needed for Android to be able to
     * create new objects, individually or as arrays
     *
     * If you don't do that, Android framework will through exception
     * Parcelable protocol requires a Parcelable.Creator object called CREATOR
     */
    public static final Parcelable.Creator<MapsMarker> CREATOR = new Parcelable.Creator<MapsMarker>() {

        public MapsMarker createFromParcel(Parcel in) {
            return new MapsMarker(in);
        }

        public MapsMarker[] newArray(int size) {
            return new MapsMarker[size];
        }
    };

    //return latlng type to create the marker position
    public LatLng GetPosition(){

        LatLng position = new LatLng(this.markerLat, this.markerLng);

        return position;
    }
}