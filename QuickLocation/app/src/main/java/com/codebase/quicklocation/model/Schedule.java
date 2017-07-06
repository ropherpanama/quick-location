package com.codebase.quicklocation.model;

/**
 * Created by fgcanga on 17/06/2017.
 */

public class Schedule {
    private String dayName;
    private String openFrom;
    private String closedFrom;
    private boolean isOpen;

    public Schedule(String dayName, String openFrom, String closedFrom, boolean isOpen) {
        this.dayName = dayName;
        this.openFrom = openFrom;
        this.closedFrom = closedFrom;
        this.isOpen = isOpen;
    }

    public String getDayName() {
        return dayName;
    }

    public void setDayName(String dayName) {
        this.dayName = dayName;
    }

    public String getOpenFrom() {
        return openFrom;
    }

    public void setOpenFrom(String openFrom) {
        this.openFrom = openFrom;
    }

    public String getClosedFrom() {
        return closedFrom;
    }

    public void setClosedFrom(String closedFrom) {
        this.closedFrom = closedFrom;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void setOpen(boolean open) {
        isOpen = open;
    }
}
