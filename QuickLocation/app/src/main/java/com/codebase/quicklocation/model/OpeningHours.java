package com.codebase.quicklocation.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Spanky on 23/01/2017.
 */

public class OpeningHours {
    @SerializedName("weekday_text")
    private String[] weekdayText;

    public String[] getWeekdayText() {
        return weekdayText;
    }

    public void setWeekdayText(String[] weekdayText) {
        this.weekdayText = weekdayText;
    }
}