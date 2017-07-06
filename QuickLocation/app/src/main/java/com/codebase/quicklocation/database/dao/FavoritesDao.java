package com.codebase.quicklocation.database.dao;

/**
 * Created by fgcanga on 05/06/2017.
 */

import android.content.Context;
import android.util.Log;

import com.codebase.quicklocation.database.DBHelper;
import com.codebase.quicklocation.database.Favorites;
import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

public class FavoritesDao {
    private DBHelper helper;
    private Dao<Favorites, String> dao;

    public FavoritesDao(Context ctx) {
        try {
            helper = new DBHelper(ctx);
            dao = helper.getFavoritesDao();
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
    }

    public void add(Favorites favorite) {
        try {
            dao.create(favorite);
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
    }

    public int update(Favorites favorite) {
        try {
            return dao.update(favorite);
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int deleteAll() {
        try {
            return dao.delete(dao.queryForAll());
        }catch (Exception e){
            e.printStackTrace();
            return -1;
        }
    }

    public int delete(Favorites favorite) {
        try {
            return dao.delete(favorite);
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public Favorites getByPlaceId(String placeId){
        try {
            List<Favorites> retorno = dao.queryForEq("placeId", placeId);

            if(retorno.isEmpty())
                return null;
            else
                return retorno.get(0);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<Favorites> getAll() {
        try {
            return dao.queryForAll();
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}

