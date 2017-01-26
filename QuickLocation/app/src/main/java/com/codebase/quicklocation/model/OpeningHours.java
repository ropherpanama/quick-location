package com.codebase.quicklocation.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Spanky on 23/01/2017.
 */

public class OpeningHours {
    @SerializedName("weekday_text")
    private String[] weekdayText;
    @SerializedName("open_now")
    private boolean openNow;

    public String[] getWeekdayText() {
        return weekdayText;
    }

    public void setWeekdayText(String[] weekdayText) {
        this.weekdayText = weekdayText;
    }

    public boolean isOpenNow() {
        return openNow;
    }

    public void setOpenNow(boolean openNow) {
        this.openNow = openNow;
    }
}