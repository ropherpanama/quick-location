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
import java.io.FileWriter;

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

    public static void writeJsonOnDisk(Context context, String fileName, StringBuilder bigStr) {
        try {
            FileWriter Filewriter = new FileWriter(context.getApplicationInfo().dataDir + "/" + fileName + ".json");
            Filewriter.write(bigStr.toString());
            Filewriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String objectToJson(Object object) {
        return Utils.factoryGson().toJson(object);
    }

    /**
     * Con este metodo se puede retornar una referencia a la ultima coordenada guardada por la
     * aplicacion, esta coordenada sera usada para realizar el request al API segun la ubicacion
     * del usuario
     * @param context contexto de la aplicacion
     * @return cadena json con el objeto Location
     */
    public static String getSavedLocation(Context context) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(context.getApplicationInfo().dataDir + "/location.json"));
            JsonElement json = new JsonParser().parse(br);
            return json.getAsJsonObject().toString();
        }catch (Exception e){
            return "no_location";
        }
    }

    public static StringBuilder formatDays(StringBuilder trama) {
        String origin = trama.toString();
        String result = trama.toString();

        if(origin.contains("Monday"))
            result = result.replace("Monday", "Lunes");

        if(origin.contains("Tuesday"))
            result = result.replace("Tuesday", "Martes");

        if(origin.contains("Wednesday"))
            result = result.replace("Wednesday", "Miercoles");

        if(origin.contains("Thursday"))
            result = result.replace("Thursday", "Jueves");

        if(origin.contains("Friday"))
            result = result.replace("Friday", "Viernes");

        if(origin.contains("Saturday"))
            result = result.replace("Saturday", "Sabado");

        if(origin.contains("Sunday"))
            result = result.replace("Sunday", "Domingo");

        if(origin.contains("Closed"))
            result = result.replace("Closed", "Cerrado");

        if(origin.contains("Sunday"))
            result = result.replace("Open", "Abierto");

        if(origin.toLowerCase().contains("now"))
            result = result.replace("now", "ahora");

        return new StringBuilder(result);
    }
}
