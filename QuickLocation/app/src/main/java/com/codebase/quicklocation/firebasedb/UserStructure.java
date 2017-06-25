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
    private String userName;
    private String fullName;
    private TypeGroup group;

    public UserStructure() {
    }

    public UserStructure(String key_, String userName_, String fullName_) {
        this.key = key_;
        this.userName = userName_;
        this.fullName = fullName_;
        //this.group = group_;
    }

    public UserStructure(TypeGroup group) {
        this.group = group;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("key", key);
        result.put("fullName", fullName);
        result.put("userName", userName);
       // result.put("group", group);

        return result;
    }
}
