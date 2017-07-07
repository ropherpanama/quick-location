package com.codebase.quicklocation.model;

import java.util.Date;

/**
 * Created by fgcanga on 27/06/2017.
 */

public class UserUseStatistic {
    private String nickname;
    private long date;
    private String placeId;
    private String category;

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }
}
