package com.codebase.quicklocation.model;

/**
 * Created by Spanky on 23/01/2017.
 */

public class ResponseForPlaceDetails {
    private PlaceDetail result;
    private String status;

    public PlaceDetail getResult() {
        return result;
    }

    public void setResult(PlaceDetail result) {
        this.result = result;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
