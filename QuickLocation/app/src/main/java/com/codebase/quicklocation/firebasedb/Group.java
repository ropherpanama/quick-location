package com.codebase.quicklocation.firebasedb;

import com.google.firebase.database.Exclude;

import java.sql.Date;
import java.util.Comparator;
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
    private long timestamp;
    private String lastmessage;

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

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getLastmessage() {
        return lastmessage;
    }

    public void setLastmessage(String lastmessage) {
        this.lastmessage = lastmessage;
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


    /**
     * Compara las fechas en tipo Date, luego las ordenas descendento o ascenentes.
     */
    public static class CompDate implements Comparator<Group> {
        private int mod = 1;
        public CompDate(boolean desc) {
            if (desc) mod =-1;
        }
        @Override
        public int compare(Group arg0, Group arg1) {
            Date date0 = new Date(arg0.getTimestamp()*1000L);
            Date date1 = new Date(arg1.getTimestamp()*1000L);
            return mod*date0.compareTo(date1);
        }
    }


}
