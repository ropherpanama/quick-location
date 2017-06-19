package com.codebase.quicklocation.database.dao;

import android.content.Context;

import com.codebase.quicklocation.database.DBHelper;
import com.codebase.quicklocation.database.FavoritesData;
import com.codebase.quicklocation.database.Users;
import com.j256.ormlite.dao.Dao;

import java.util.Collections;
import java.util.List;

/**
 * Created by Rosendo on 18/06/2017.
 */

public class FavoritesDataDao {
    private DBHelper helper;
    private Dao<FavoritesData, String> dao;

    public FavoritesDataDao(Context ctx) {
        try {
            helper = new DBHelper(ctx);
            dao = helper.getFavoritesDataDao();
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
    }

    public void add(FavoritesData cdata) {
        try {
            dao.create(cdata);
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
    }

    public void update(FavoritesData cdata) {
        try {
            dao.update(cdata);
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
    }

    public void delete(FavoritesData cdata) {
        try {
            dao.delete(cdata);
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
    }

    public List<FavoritesData> getAll() {
        try {
            return dao.queryForAll();
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}
