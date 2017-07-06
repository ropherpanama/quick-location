package com.codebase.quicklocation.firebasedb;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by fgcanga on 6/25/17.
 */

public class Group {
    private String title;
    private String description;
    public String gruop_id;
     private Map<String, Object> members;
    private String create_by;

    public Group() {
    }

    public Group(String title, String description, String gruop_id, String create_by) {
        this.title = title;
        this.description = description;
        this.gruop_id = gruop_id;
        this.create_by = create_by;
    }

    public Group(String title, String description, String gruop_id, Map<String, Object> members, String create_by) {
        this.title = title;
        this.description = description;
        this.gruop_id = gruop_id;
        this.members = members;
        this.create_by = create_by;
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


    public String getCreate_by() {
        return create_by;
    }

    public void setCreate_by(String create_by) {
        this.create_by = create_by;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("title", title);
        result.put("description", description);
        result.put("members", members);
        result.put("gruop_id", gruop_id);
        result.put("create_by",create_by);
        return result;
    }
}
