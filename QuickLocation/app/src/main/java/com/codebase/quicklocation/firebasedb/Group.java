package com.codebase.quicklocation.firebasedb;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by AUrriola on 6/25/17.
 */

public class Group {
    private String title;
    private String description;
    public String gruop_id;
     private Map<String, Object> members;

    public Group() {
    }

    public Group(String title, String description, String id_gruop, Map<String, Object> members) {
        this.title = title;
        this.description = description;
        this.gruop_id = id_gruop;
        this.members = members;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, Object> getMembers() {
        return members;
    }

    public void setMembers(Map<String, Object> members) {
        this.members = members;
    }

    public String getGruop_id() {
        return gruop_id;
    }

    public void setGruop_id(String gruop_id) {
        this.gruop_id = gruop_id;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("title", title);
        result.put("description", description);
        result.put("members", members);
        result.put("gruop_id", gruop_id);
        return result;
    }
}
