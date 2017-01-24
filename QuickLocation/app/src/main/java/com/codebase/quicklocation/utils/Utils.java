package com.codebase.quicklocation.utils;

import android.content.Context;
import android.os.Environment;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

/**
 * Created by Spanky on 22/01/2017.
 */

public class Utils {
    private static Gson gson;
    private static final String DEFAULT_PATTERN_DATE = "yyyy-MM-dd";
    public static final String DATA_NOT_FOUND = "Data Not Found";

    public static Gson factoryGson(final String pattern) {
        return builderGson(pattern);
    }

    public static Gson factoryGson() {
        return builderGson(DEFAULT_PATTERN_DATE);
    }

    private static Gson builderGson(final String pattern) {
        if (gson == null) {
            gson = new GsonBuilder().serializeNulls().setDateFormat(pattern)
                    .setPrettyPrinting().setVersion(1.0).create();
        }
        return gson;
    }

    public static StringBuilder getJsonFromDisk(Context context, String jsonFile) {
        try {
            File sdcard = Environment.getExternalStorageDirectory();
            File file = new File(sdcard, jsonFile + ".json");
            //BufferedReader br = new BufferedReader(new FileReader(context.getApplicationInfo().dataDir + "/" + jsonFile + ".json"));
            BufferedReader br = new BufferedReader(new FileReader(file));
            JsonElement json = new JsonParser().parse(br);
            StringBuilder data = null;

            if (json.isJsonArray())
                data = new StringBuilder(json.getAsJsonArray().toString());
            else if (json.isJsonObject())
                data = new StringBuilder(json.getAsJsonObject().toString());

            return data;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
