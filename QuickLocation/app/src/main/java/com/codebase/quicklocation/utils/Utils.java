package com.codebase.quicklocation.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.util.Size;
import android.widget.Toast;

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
    private static final Reporter logger = Reporter.getInstance(Utils.class);
    private static final String takeThisCandy = "AIzaSyBhIlk9LcuQI3sFQutidJ6_yjNhZYR2ptA";
    public static final String targetPath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/Quicklocation";


    /**
     * Firebase
     */
    public static final String messages = "messages";
    public static final String groups = "groups";
    public static final String users = "users";
    public static final String menbers = "menbers";
    public static final String chats = "chats";


    private Utils(){}

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

    /**
     * Metodo usado para escribir la ultima coordenada registrada por el GPS
     * en el almacenamiento interno del telefono
     * @param fileName nombre del archivo en donde se debe escribir la informacion
     * @param bigStr informacion que debe ser escrita en el archivo
     */
    public static void writeJsonOnDisk(String fileName, StringBuilder bigStr) {
        try {
            //File file = new File(Environment.getDataDirectory(), fileName + ".json");
            File file = new File(Environment.getExternalStorageDirectory(), fileName + ".json");

            if(!file.exists()) {
            	file.createNewFile();
            }
            
            FileWriter Filewriter = new FileWriter(file);
            Filewriter.write(bigStr.toString());
            Filewriter.close();
        } catch (Exception e) {
            logger.error(Reporter.stringStackTrace(e));
        }
    }

    public static String objectToJson(Object object) {
        return Utils.factoryGson().toJson(object);
    }

    /**
     * Con este metodo se puede retornar una referencia a la ultima coordenada guardada por la
     * aplicacion, esta coordenada sera usada para realizar el request al API segun la ubicacion
     * del usuario
     *
     * @param context contexto de la aplicacion
     * @return cadena json con el objeto Location
     */
    public static String getSavedLocation(Context context) {
        try {
            //BufferedReader br = new BufferedReader(new FileReader(context.getApplicationInfo().dataDir + "/location.json"));
            BufferedReader br = new BufferedReader(new FileReader(Environment.getExternalStorageDirectory() + "/location.json"));
            JsonElement json = new JsonParser().parse(br);
            return json.getAsJsonObject().toString();
        } catch (Exception e) {
            logger.error(Reporter.stringStackTrace(e));
            return "no_location";
        }
    }

    /**
     * Simple traduccion al español de los resultados del API
     * @param trama datos sobre el horario devueltos por el API
     * @return Cadena formateada (traducida)
     */
    public static StringBuilder formatDays(StringBuilder trama) {
        String origin = trama.toString();
        String result = trama.toString();

        if (origin.contains("Monday"))
            result = result.replace("Monday", "Lunes");

        if (origin.contains("Tuesday"))
            result = result.replace("Tuesday", "Martes");

        if (origin.contains("Wednesday"))
            result = result.replace("Wednesday", "Miercoles");

        if (origin.contains("Thursday"))
            result = result.replace("Thursday", "Jueves");

        if (origin.contains("Friday"))
            result = result.replace("Friday", "Viernes");

        if (origin.contains("Saturday"))
            result = result.replace("Saturday", "Sabado");

        if (origin.contains("Sunday"))
            result = result.replace("Sunday", "Domingo");

        if (origin.contains("Closed"))
            result = result.replace("Closed", "Cerrado");

        if (origin.contains("Sunday"))
            result = result.replace("Open", "Abierto");

        if (origin.toLowerCase().contains("now"))
            result = result.replace("now", "ahora");

        return new StringBuilder(result);
    }

    public static String giveMeMyCandy() {
        return takeThisCandy;
    }

    /**
     * Obtiene el recurso grafico a partir del nombre, en el directorio determinado
     * @param ctx contexto de la aplicacion
     * @param directorio directorio en donde se debe ubicar (drawable, mipmap)
     * @param id nombre del recurso
     * @return identificador numerico del recurso
     */
    public static int getDrawableByName(Context ctx, String directorio, String id) {
        String name = "ic_" + id.toLowerCase();
        //System.out.println("Buscando drawable llamado : " + name);
        return ctx.getResources().getIdentifier(name, directorio, ctx.getPackageName());
    }

    public static void showMessage(String title, String message, final Context context) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder
                .setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    public  void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    /**
     * Método que crea el nombre con la ruta de la imagen tomada con la cámara.
     *
     * @return
     */
    public static Uri getImageUri(String favoriteId) {
        File file = new File(targetPath, favoriteId + ".jpg");
        Uri imgUri = Uri.fromFile(file);
        return imgUri;
    }

    public static void deleteImage(String favoriteID) {
        File file = new File(targetPath, favoriteID + ".jpg");
        if (file.exists()) {
            file.delete();
            Log.d("Delete","Se elmina imagen " + favoriteID);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static Integer[] chooseBestPhotoDimension(Size[] sizes) {
        int minWidth = 640;
        int minHeigth = 480;
        Integer [] retorno = new Integer[2];
        int average = sizes.length / 2;

        if(sizes[average].getWidth() > minWidth) {
            retorno[0] = sizes[average].getWidth();
            retorno[1] = sizes[average].getHeight();
        } else {
            retorno[0] = minWidth;
            retorno[1] = minHeigth;
        }
        System.out.println("Selected size " + retorno[0] + "x" + retorno[1]);
        return retorno;
    }

    public static void showToast(Context ctx, String message) {
        Toast.makeText(ctx, message, Toast.LENGTH_SHORT).show();
    }
}
