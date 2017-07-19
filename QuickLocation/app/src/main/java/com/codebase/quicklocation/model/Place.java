package com.codebase.quicklocation.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by fgcanga on 22/01/2017.
 * Representa el objeto con la informacion general de un lugar devuelto por el API
 */

public class Place {
    private Geometry geometry;
    private String name;
    @SerializedName("place_id")
    private String placeId;
    private double rating;
    private String[] types;
    private String vicinity;
    private boolean showRating;

    public Geometry getGeometry() {
        return geometry;
    }

    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String[] getTypes() {
        return types;
    }

    public void setTypes(String[] types) {
        this.types = types;
    }

    public String getVicinity() {
        return vicinity;
    }

    public void setVicinity(String vicinity) {
        this.vicinity = vicinity;
    }

    public boolean isShowRating() {
        return showRating;
    }

    public void setShowRating(boolean showRating) {
        this.showRating = showRating;
    }
}
