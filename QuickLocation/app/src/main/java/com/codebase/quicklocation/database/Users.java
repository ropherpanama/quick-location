package com.codebase.quicklocation.database;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

/**
 * Created by Rosendo on 05/06/2017.
 */

@DatabaseTable
public class Users {
    @DatabaseField(generatedId = true, columnName = "_id")
    private int id;
    @DatabaseField
    private String nickname;
    @DatabaseField
    private String name;
    @DatabaseField
    private String lastname;
    @DatabaseField
    private String country;
    @DatabaseField
    private String password;
    @DatabaseField
    private Date signup;
    @DatabaseField
    private String deviceImei;

    public Users() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Date getSignup() {
        return signup;
    }

    public void setSignup(Date signup) {
        this.signup = signup;
    }

    public String getDeviceImei() {
        return deviceImei;
    }

    public void setDeviceImei(String deviceImei) {
        this.deviceImei = deviceImei;
    }
}
