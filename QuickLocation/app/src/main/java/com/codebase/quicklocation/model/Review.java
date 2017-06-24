package com.codebase.quicklocation.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Rosendo on 23/06/2017.
 */

public class Review {
    @SerializedName("author_name")
    private String author;
    private double rating;
    private String text;

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
