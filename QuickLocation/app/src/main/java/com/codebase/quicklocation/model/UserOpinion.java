package com.codebase.quicklocation.model;

/**
 * Created by fgcanga on 27/06/2017.
 */

public class UserOpinion {
    private String comment;
    private double rating;
    private String authorName;

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }
}
