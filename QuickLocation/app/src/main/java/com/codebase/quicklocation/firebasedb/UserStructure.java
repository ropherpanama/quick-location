package com.codebase.quicklocation.firebasedb;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by AUrriola on 6/25/17.
 */
@IgnoreExtraProperties
public class UserStructure {
    private String key;
    private String username;
    private String fullname;
    private TypeGroup groups;

    public UserStructure() {
    }

    public UserStructure(String key_, String userName_, String fullName_) {
        this.key = key_;
        this.username = userName_;
        this.fullname = fullName_;
        //this.groups = group_;
    }

    public UserStructure(TypeGroup group) {
        this.groups = group;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("key", key);
        result.put("fullname", fullname);
        result.put("username", username);
       // result.put("groups", groups);

        return result;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public TypeGroup getGroups() {
        return groups;
    }

    public void setGroups(TypeGroup groups) {
        this.groups = groups;
    }
}
