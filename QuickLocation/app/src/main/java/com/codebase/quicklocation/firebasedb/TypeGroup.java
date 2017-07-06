package com.codebase.quicklocation.firebasedb;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by fgcanga on 6/25/17.
 */

public class TypeGroup {
    private String keyName;
    private boolean status;
    public TypeGroup(){}

    public TypeGroup(String keyName, boolean status) {
        this.keyName = keyName;
        this.status = status;
    }
    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put(keyName,status);
        return result;
    }

    public String getKeyName() {
        return keyName;
    }

    public void setKeyName(String keyName) {
        this.keyName = keyName;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }
}
