package com.codebase.quicklocation.utils;

import java.io.File;
import java.io.PrintStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import android.annotation.SuppressLint;
import android.os.Environment;
import android.os.SystemClock;

/**
 * Esta clase generara un archivo de logs en el raiz de la SD Card. Se genera un
 * archivo por fecha (automatico)
 *
 * @author rospena
 */
public class Reporter {
    private PrintStream printStr;
    private static String fileName = "quicklocation_app";// Default File Name
    private String programName = "";
    private String message = "";
    private static Reporter log;
    private GregorianCalendar g = new GregorianCalendar();
    private Date date = new Date();

    public static Reporter getInstance(Class caller) {
        if (log == null)
            log = new Reporter(caller);
        return log;
    }

    public static Reporter getInstance(Class caller, String fileName) {
        if (log == null)
            log = new Reporter(caller);
        log.setFileName(fileName);
        return log;
    }

    private Reporter(Class caller) {
        this.setProgramName(caller.getClass().getCanonicalName());
    }



    private PrintStream createLogFile() {
        Calendar calendar = Calendar.getInstance();
        g.setTime(date);
        String name = fileName + "_" + calendar.get(Calendar.YEAR)
                + (calendar.get(Calendar.MONTH) + 1 < 10 ? "0" : "")
                + (calendar.get(Calendar.MONTH) + 1)
                + (calendar.get(Calendar.DATE) < 10 ? "0" : "")
                + calendar.get(Calendar.DATE) + ".txt";
        File file = new File(Environment.getExternalStorageDirectory(), name);
        FileOutputStream out;
        PrintStream ps = null;

        try {
            out = new FileOutputStream(file, true);
            ps = new PrintStream(out);
            printStr = ps;
        } catch (Exception x) {
            System.out.println(fileName + " --> Error al crear Archivo: "
                    + name + "\n" + x);
        }
        return ps;
    }

    /**
     * Use este metodo para imprimir informacion o anotaciones similares a debug
     *
     * @param text Mensaje que desea imprimir
     * @return
     */
    public String write(String text) {
        message = text;

        if (printStr == null)
            printStr = createLogFile();
        printStr.println("Hora: " + fechahoy("yyyy-MM-dd HH:mm:ss")
                + " " + SystemClock.currentThreadTimeMillis() + " -- "
                + programName + " -- [MSG] : " + message);
        printStr.flush();
        return message;
    }

    /**
     * Use este metodo para imprimir salidas de error, excepciones o cualquier
     * salida que represente un mal funcionamiento en su proceso
     *
     * @param text Mensaje de error que desee imprimir
     * @return
     */
    public String error(String text) {
        message = text;

        if (printStr == null)
            printStr = createLogFile();
        printStr.println("Hora: " + fechahoy("yyyy-MM-dd HH:mm:ss")
                + " " + SystemClock.currentThreadTimeMillis() + " -- "
                + programName + " -- [ERR] : " + message);
        printStr.flush();
        return message;
    }

    public static String getFileName() {
        return fileName;
    }

    /**
     * Use este metodo si desea modificar el nombre del archivo que se generara,
     * por defecto el archivo se llama Log.log No requiere colocar la extension
     * .log, solo escriba el nombre del archivo
     *
     * @param fileName Nombre del archivo de salida
     */
    public void setFileName(String fileName) {
        Reporter.fileName = fileName;
    }

    public String getProgramName() {
        return programName;
    }

    /**
     * Use este metodo para permitirle saber al escritor del archivo cual es la
     * clase que esta generando la salida, esto le ayudara a ubicar de mejor
     * forma el origen de los eventos en sus procesos.
     *
     * @param programName Nombre de la clase que genera la salida (ejm.
     *                    setProgramName("Calculos.java"))
     */
    public void setProgramName(String programName) {
        this.programName = programName;
    }

    /**
     * Este metodillo te permite capturar la salida del printStackTrace a un
     * String :)
     *
     * @param exception Exception que se quiera capturar en el catch
     * @return String con todo el trace
     */
    public static String stringStackTrace(Exception exception) {
        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        exception.printStackTrace(printWriter);
        System.out.println(writer.toString());
        return writer.toString();
    }

    @SuppressLint("SimpleDateFormat")
    private static String fechahoy(String formato) {
        Date date = new Date();
        StringBuilder fechahoy;
        SimpleDateFormat YYYYMMDD = new SimpleDateFormat(formato);
        fechahoy = new StringBuilder(YYYYMMDD.format(date));
        return fechahoy.toString();
    }
}

