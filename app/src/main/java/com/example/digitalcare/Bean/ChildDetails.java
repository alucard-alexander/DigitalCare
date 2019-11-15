package com.example.digitalcare.Bean;

import com.google.firebase.firestore.GeoPoint;

public class ChildDetails {
    private String name;
    private String p_id;
    private GeoPoint geoPoint;

    public String getName() {
        return name;
    }

    public ChildDetails(String name, String p_id, GeoPoint geoPoint) {
        this.name = name;
        this.p_id = p_id;
        this.geoPoint = geoPoint;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getP_id() {
        return p_id;
    }

    public void setP_id(String p_id) {
        this.p_id = p_id;
    }

    public GeoPoint getGeoPoint() {
        return geoPoint;
    }

    public void setGeoPoint(GeoPoint geoPoint) {
        this.geoPoint = geoPoint;
    }
}
