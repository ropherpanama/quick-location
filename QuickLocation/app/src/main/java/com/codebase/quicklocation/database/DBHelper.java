package com.codebase.quicklocation.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;

/**
 * Created by Rosendo on 05/06/2017.
 */

public class DBHelper extends OrmLiteSqliteOpenHelper {
    private static final String DATABASE_NAME = "quicklocation.db";
    private static final int DATABASE_VERSION = 1;

    private Dao<Users, Integer> usersDao;
    private Dao<Favorites, String> favoritesDao;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
        try {
            TableUtils.createTable(connectionSource, Users.class);
            TableUtils.createTable(connectionSource, Favorites.class);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        onCreate(db, connectionSource);
    }

    public Dao<Users, Integer> getUsersDao() throws SQLException {
        if (usersDao == null) {
            usersDao = getDao(Users.class);
        }
        return usersDao;
    }

    public Dao<Favorites, String> getFavoritesDao() throws SQLException {
        if(favoritesDao == null)
            favoritesDao = getDao(Favorites.class);
        return favoritesDao;
    }

    @Override
    public void close() {
        super.close();
        usersDao = null;
        favoritesDao = null;
    }
}
