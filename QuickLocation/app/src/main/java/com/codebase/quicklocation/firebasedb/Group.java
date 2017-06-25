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
   // public Map<String, Boolean> stars = new HashMap<>();
     private TypeGroup members;

    public Group() {
    }

    public Group(String titleGroup, String detailGroup, TypeGroup members_) {
        this.title = titleGroup;
        this.description = detailGroup;
        this.members = members_;
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

    public TypeGroup getMembers() {
        return members;
    }

    public void setMembers(TypeGroup members) {
        this.members = members;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("title", title);
        result.put("description", description);
        result.put("members", members);
        // result.put("group", group);
        return result;
    }
}
