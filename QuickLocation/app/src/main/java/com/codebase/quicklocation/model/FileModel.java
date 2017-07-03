package com.codebase.quicklocation.model;

/**
 * Created by Alessandro Barreto on 22/06/2016.
 */
public class FileModel {

    private String type;
    private String favorito_categoria; //categoria
    private String nombre_favorito; //detalle
    private String size_file;

    public FileModel() {
    }

    public FileModel(String type, String favorito_categoria, String name_file, String size_file) {
        this.type = type;
        this.favorito_categoria = favorito_categoria;
        this.nombre_favorito = name_file;
        this.size_file = size_file;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFavorito_categoria() {
        return favorito_categoria;
    }

    public void setFavorito_categoria(String favorito_categoria) {
        this.favorito_categoria = favorito_categoria;
    }

    public String getNombre_favorito() {
        return nombre_favorito;
    }

    public void setNombre_favorito(String nombre_favorito) {
        this.nombre_favorito = nombre_favorito;
    }

    public String getSize_file() {
        return size_file;
    }

    public void setSize_file(String size_file) {
        this.size_file = size_file;
    }
}
