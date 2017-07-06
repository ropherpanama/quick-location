package com.codebase.quicklocation.sorters;

import com.codebase.quicklocation.model.Place;

import java.util.Comparator;

/**
 * Created by fgcanga on 01/05/2017.
 * Sorter para ordenar los sitios encontrados de acuerdo al rating
 */

public class RatingSorter implements Comparator<Place> {
    @Override
    public int compare(Place a, Place b) {
        int returnVal = 0;

        if (a.getRating() > b.getRating()) {
            returnVal = -1;
        } else if (a.getRating() < b.getRating()) {
            returnVal = 1;
        } else if (a.getRating() == b.getRating()) {
            returnVal = 0;
        }
        return returnVal;
    }
}
