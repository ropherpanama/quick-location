package com.codebase.quicklocation.model;

/**
 * Created by fgcanga on 22/01/2017.
 * Representa el objeto Geometry devuelto por el Api de Google
 */

public class Geometry {
    private Location location;

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }
}
