package com.codebase.quicklocation.database;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

/**
 * Created by Rosendo on 05/06/2017.
 */

@DatabaseTable
public class Favorites {
    @DatabaseField(id = true)
    private String placeId;
    @DatabaseField
    private String localName;
    @DatabaseField
    private Date addedFrom;
    @DatabaseField
    private double rating;
    @DatabaseField
    private String category;
    @DatabaseField
    private String comment;

    public Favorites(){}

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public String getLocalName() {
        return localName;
    }

    public void setLocalName(String localName) {
        this.localName = localName;
    }

    public Date getAddedFrom() {
        return addedFrom;
    }

    public void setAddedFrom(Date addedFrom) {
        this.addedFrom = addedFrom;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public String toString() {
        return "Favorites{" +
                "placeId = '" + placeId + '\'' +
                ", localName = '" + localName + '\'' +
                ", addedFrom = " + addedFrom +
                ", rating = " + rating +
                ", category = '" + category + '\'' +
                ", comment = '" + comment + '\'' +
                '}';
    }
}
