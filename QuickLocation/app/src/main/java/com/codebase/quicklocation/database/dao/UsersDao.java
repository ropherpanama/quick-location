package com.codebase.quicklocation.database.dao;

/**
 * Created by Rosendo on 05/06/2017.
 */

import android.content.Context;

import com.codebase.quicklocation.database.DBHelper;
import com.codebase.quicklocation.database.Users;
import com.j256.ormlite.dao.Dao;

import java.util.Collections;
import java.util.List;

public class UsersDao {
    private DBHelper helper;
    private Dao<Users, Integer> dao;

    public UsersDao(Context ctx) {
        try {
            helper = new DBHelper(ctx);
            dao = helper.getUsersDao();
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
    }

    public void add(Users user) {
        try {
            dao.create(user);
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
    }

    public void update(Users user) {
        try {
            dao.update(user);
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
    }

    public void delete(Users user) {
        try {
            dao.delete(user);
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Users> getAll() {
        try {
            return dao.queryForAll();
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}

