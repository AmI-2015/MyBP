package com.dev.ami2015.mybikeplace;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

public class MyBPStationMarker implements Parcelable {
    public int stationID;
    public Double stationLat;
    public Double stationLng;
    public int totalPlaces;
    public int freePlaces;

    //costruttore
    public MyBPStationMarker(int stationID, double stationLat, double stationLng, int totalPlaces, int freePlaces){
        this.stationID = stationID;
        this.stationLat = stationLat;
        this.stationLng = stationLng;
        this.totalPlaces = totalPlaces;
        this.freePlaces =freePlaces;

    }

    /**
     * Use when reconstructing User object from parcel
     * This will be used only by the 'CREATOR'
     * @param in a parcel to read this object
     */
    public MyBPStationMarker(Parcel in) {
        this.stationID = in.readInt();
        this.stationLat = in.readDouble();
        this.stationLng = in.readDouble();
        this.totalPlaces = in.readInt();
        this.freePlaces = in.readInt();
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
        dest.writeInt(stationID);
        dest.writeDouble(stationLat);
        dest.writeDouble(stationLng);
        dest.writeInt(totalPlaces);
        dest.writeInt(freePlaces);
    }

    /**
     * This field is needed for Android to be able to
     * create new objects, individually or as arrays
     *
     * If you don't do that, Android framework will through exception
     * Parcelable protocol requires a Parcelable.Creator object called CREATOR
     */
    public static final Parcelable.Creator<MyBPStationMarker> CREATOR = new Parcelable.Creator<MyBPStationMarker>() {

        public MyBPStationMarker createFromParcel(Parcel in) {
            return new MyBPStationMarker(in);
        }

        public MyBPStationMarker[] newArray(int size) {
            return new MyBPStationMarker[size];
        }
    };

    //return latlng type to create the marker position
    public LatLng GetPosition(){

        LatLng position = new LatLng(this.stationLat, this.stationLng);

        return position;
    }
}