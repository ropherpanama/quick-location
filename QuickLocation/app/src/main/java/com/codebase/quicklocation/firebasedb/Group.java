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
     private TypeGroup members;
    private Map<String, Object> salir;

    private String create_by;

    public Group() {
    }

    public Group(String title, String description, String gruop_id, String create_by) {
        this.title = title;
        this.description = description;
        this.gruop_id = gruop_id;
        this.create_by = create_by;
    }

    public Group(String title, String description, String gruop_id, TypeGroup members, String create_by) {
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

    public TypeGroup getMembers() {
        return members;
    }

    public void setMembers(TypeGroup members) {
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

    public Map<String, Object> getSalir() {
        return salir;
    }

    public void setSalir(Map<String, Object> salir) {
        this.salir = salir;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("title", title);
        result.put("description", description);
        result.put("members", members);
        result.put("salir", salir);
        result.put("gruop_id", gruop_id);
        result.put("create_by",create_by);
        return result;
    }
}
