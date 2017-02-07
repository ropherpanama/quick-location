package com.codebase.quicklocation.gps;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.codebase.quicklocation.R;
import com.codebase.quicklocation.model.LastLocation;
import com.codebase.quicklocation.utils.Utils;

public class GPSTracker extends Service implements LocationListener {
    private final Context mContext;
    private boolean isGPSEnabled = false;
    private boolean isNetworkEnabled = false;
    private boolean canGetLocation = false;
    private Location location;
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 metros
    private static final long MIN_TIME_BW_UPDATES = 3000 * 60; // 3 minutos
    protected LocationManager locationManager;

    public GPSTracker(Context context) {
        Log.d(this.getClass().getCanonicalName(), "Servicio creado");
        this.mContext = context;
        getLocation();
    }

    public Location getLocation() {
        try {
            locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                Log.d(this.getClass().getCanonicalName(), "Any provider enabled");
            } else {
                canGetLocation = true;

                if (isNetworkEnabled) {
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

                    Log.d(this.getClass().getCanonicalName(), "Provider Network");
                    if (locationManager != null) {
                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        Log.d(this.getClass().getCanonicalName(), "Intentando buscar la ultima ubicacion conocida");
                    }
                }

                if (isGPSEnabled) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

                    Log.d(this.getClass().getCanonicalName(), "Provider GPS");
                    if (locationManager != null) {
                        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        Log.d(this.getClass().getCanonicalName(), "Probando conectar con el GPS la ultima coordenada");
                    }
                }
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        return location;
    }

    public void stopUsingGPS() {
        if (Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (locationManager != null) {
                locationManager.removeUpdates(GPSTracker.this);
            }
            return;
        }
    }

    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
        alertDialog.setTitle(mContext.getString(R.string.alert_gps_title));
        alertDialog.setMessage(mContext.getString(R.string.alert_gps_msg));
        alertDialog.setPositiveButton(mContext.getString(R.string.alert_gps_btn_ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mContext.startActivity(intent);
            }
        });

        alertDialog.setNegativeButton(mContext.getString(R.string.alert_gps_btn_no), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertDialog.show();
    }

    public boolean isCanGetLocation() {
        return canGetLocation;
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(this.getClass().getCanonicalName(), "La coordenada ha cambiado!");
        LastLocation last = new LastLocation();
        last.setAccuracy(location.getAccuracy());
        last.setLatitude(location.getLatitude());
        last.setLongitude(location.getLongitude());
        last.setProvider(location.getProvider());
        last.setTime(System.currentTimeMillis());
        Log.d(this.getClass().getCanonicalName(), Utils.objectToJson(last));
        Utils.writeJsonOnDisk(mContext, "location", new StringBuilder(Utils.objectToJson(last)));
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }
}