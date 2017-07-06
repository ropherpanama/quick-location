package com.codebase.quicklocation.utils;

/**
 * Created by fgcanga on 11/02/2017.
 */

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class HTTPTasks {

    private static Reporter logger = Reporter.getInstance(HTTPTasks.class);
    /**
     * Este metodo busca en el contenido json en el servidor (ejecuta un http request)
     *
     * @param direccion direccion web del servicio a consumir
     * @return json con la trama devuelta por el servidor
     */
    public static InputStream getJsonFromServer(String direccion) {
        try {
            URL url = new URL(direccion);
            URLConnection urlConnection = url.openConnection();
            return urlConnection.getInputStream();
        } catch (Exception e) {
            logger.error(Reporter.stringStackTrace(e));
            return null;
        }
    }
}
