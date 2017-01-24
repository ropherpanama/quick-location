package com.codebase.quicklocation.model;

/**
 * Created by Spanky on 22/01/2017.
 * Representa el objeto Location devuelto por el Api de Google
 */

public class Location {
    private double lat;
    private double lng;

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }
}
