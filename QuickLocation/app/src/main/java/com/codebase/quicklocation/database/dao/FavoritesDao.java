package com.codebase.quicklocation.database.dao;

/**
 * Created by Rosendo on 05/06/2017.
 */

import android.content.Context;

import com.codebase.quicklocation.database.DBHelper;
import com.codebase.quicklocation.database.Favorites;
import com.j256.ormlite.dao.Dao;

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

    public void update(Favorites favorite) {
        try {
            dao.update(favorite);
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
    }

    public void delete(Favorites favorite) {
        try {
            dao.delete(favorite);
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
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

