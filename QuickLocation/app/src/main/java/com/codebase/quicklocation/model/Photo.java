package com.codebase.quicklocation.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by fgcanga on 28/02/2017.
 */

public class Photo {
    @SerializedName("photo_reference")
    private String photoReference;

    public String getPhotoReference() {
        return photoReference;
    }

    public void setPhotoReference(String photoReference) {
        this.photoReference = photoReference;
    }
}
