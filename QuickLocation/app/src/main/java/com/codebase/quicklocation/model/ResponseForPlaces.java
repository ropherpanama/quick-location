package com.codebase.quicklocation.model;

import java.util.ArrayList;

/**
 * Created by Spanky on 22/01/2017.
 * Representa el retorno completo del Api de Google para Places (Informacion general de la busqueda)
 */

public class ResponseForPlaces {
    private ArrayList<Place> results;
    private String status;

    public ArrayList<Place> getResults() {
        return results;
    }

    public void setResults(ArrayList<Place> results) {
        this.results = results;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
