package com.codebase.quicklocation.model;

import java.util.List;

/**
 * Created by fgcanga on 17/06/2017.
 * Objeto enviado al servidor cuando el usuario realiza
 * una sugerencia de mejora en la informacion del lugar
 */

public class ImprovementRequest {
    private String placeId;
    private String author;
    private List<ImprovementInformation> informations;
    private long date;

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public List<ImprovementInformation> getInformations() {
        return informations;
    }

    public void setInformations(List<ImprovementInformation> informations) {
        this.informations = informations;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }
}
