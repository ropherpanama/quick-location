package com.codebase.quicklocation.database;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by fgcanga on 18/06/2017.
 */

@DatabaseTable
public class FavoritesData {
    @DatabaseField(id = true)
    private String placeId;
    @DatabaseField
    private String cdata;

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public String getCdata() {
        return cdata;
    }

    public void setCdata(String cdata) {
        this.cdata = cdata;
    }
}
