package com.example.digitalcare.Bean;

import com.google.firebase.firestore.GeoPoint;

public class MarkerDetails {
    String name;
    GeoPoint geoPoint;

    public MarkerDetails(String name, GeoPoint geoPoint) {
        this.name = name;
        this.geoPoint = geoPoint;
    }

    public MarkerDetails(GeoPoint point){
        this.geoPoint = point;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public GeoPoint getGeoPoint() {
        return geoPoint;
    }

    public void setGeoPoint(GeoPoint geoPoint) {
        this.geoPoint = geoPoint;
    }
}
