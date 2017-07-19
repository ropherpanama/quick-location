package com.codebase.quicklocation.model;

import com.codebase.quicklocation.database.Favorites;

/**
 * Created by fgcanga on 6/26/17.
 */

public class ChatModel {

    private String id = "";
    private UserModel userModel;
    private String message = "";
    private String timeStamp = "";
    private Favorites favorites;
    private MapModel mapModel;

    public ChatModel() {
    }

    public ChatModel(String id, UserModel userModel, String message, String timeStamp, Favorites favorites) {
        this.id = id;
        this.userModel = userModel;
        this.message = message;
        this.timeStamp = timeStamp;
        this.favorites = favorites;
    }

    public ChatModel(UserModel userModel, String message, String timeStamp, Favorites favorites) {
        this.userModel = userModel;
        this.message = message;
        this.timeStamp = timeStamp;
        this.favorites = favorites;
    }

    public ChatModel(UserModel userModel, String timeStamp, MapModel mapModel) {
        this.userModel = userModel;
        this.timeStamp = timeStamp;
        this.mapModel = mapModel;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public UserModel getUserModel() {
        return userModel;
    }

    public void setUserModel(UserModel userModel) {
        this.userModel = userModel;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public Favorites getFavorites() {
        return favorites;
    }

    public void setFavorites(Favorites favorites) {
        this.favorites = favorites;
    }

    public MapModel getMapModel() {
        return mapModel;
    }

    public void setMapModel(MapModel mapModel) {
        this.mapModel = mapModel;
    }

    @Override
    public String toString() {
        return "ChatModel{" +
                "mapModel=" + mapModel +
                ", favorites=" + favorites +
                ", timeStamp='" + timeStamp + '\'' +
                ", message='" + message + '\'' +
                ", userModel=" + userModel +
                '}';
    }
}
