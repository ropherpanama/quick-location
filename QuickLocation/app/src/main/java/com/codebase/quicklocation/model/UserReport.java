package com.codebase.quicklocation.model;

/**
 * Created by fgcanga on 18/06/2017.
 */

public class UserReport {
    private String username;
    private String placeId;
    private String reportContent;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public String getReportContent() {
        return reportContent;
    }

    public void setReportContent(String reportContent) {
        this.reportContent = reportContent;
    }
}
