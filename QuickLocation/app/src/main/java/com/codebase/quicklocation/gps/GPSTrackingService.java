package com.codebase.quicklocation.gps;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;

import com.codebase.quicklocation.model.LastLocation;
import com.codebase.quicklocation.utils.Reporter;
import com.codebase.quicklocation.utils.Utils;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

/**
 * Created by fgcanga on 06/02/2017.
 */

public class GPSTrackingService extends Service {
    private Reporter logger = Reporter.getInstance(GPSTrackingService.class);
    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 300000; //5 minutos
    private static final float LOCATION_DISTANCE = 0;
    DatabaseReference root;
    FirebaseUser userFirebase;
    private class LocationListener implements android.location.LocationListener {
        Location mLastLocation;

        public LocationListener(String provider) {
            //logger.write("LocationListener " + provider);
            mLastLocation = new Location(provider);


        }

        @Override
        public void onLocationChanged(Location location) {
            LastLocation last = new LastLocation();
            last.setAccuracy(location.getAccuracy());
            last.setLatitude(location.getLatitude());
            last.setLongitude(location.getLongitude());
            last.setProvider(location.getProvider());
            last.setTime(System.currentTimeMillis());
            Utils.writeJsonOnDisk("location", new StringBuilder(Utils.objectToJson(last)));
            //logger.write(Utils.objectToJson(last));
            logger.write(Utils.objectToJson(last));
            setGeoFire(location.getLatitude(),location.getLongitude());
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
    }

    /**
     * Envia las coordenadas con GeoFire;
     * @param latitude
     * @param longitude
     */
    private void setGeoFire(double latitude, double longitude) {

        if (userFirebase != null)
        {
            DatabaseReference ref = root.child(userFirebase.getUid());
            HashMap<String, Object> result = new HashMap<>();
            result.put("latitude", latitude);
            result.put("longitude", longitude);
            ref.updateChildren(result);
           // GeoFire geoFire = new GeoFire(ref);
           // geoFire.setLocation(Utils.location, new GeoLocation(latitude, longitude));

        }
        /*Map<String, Boolean> mParent = new HashMap<>();
        mParent.put(tokenFcm, true);
        user_referemce.setValue(mParent);*/
    }

    LocationListener[] mLocationListeners = new LocationListener[]{
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER),
            new LocationListener(LocationManager.PASSIVE_PROVIDER)
    };

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //logger.write("onStartCommand");
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        initializeLocationManager();
        try {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE, mLocationListeners[0]);
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE, mLocationListeners[1]);
            mLocationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE, mLocationListeners[2]);
            FirebaseApp.initializeApp(this);
            root  = FirebaseDatabase.getInstance().getReference().child(Utils.users);
            userFirebase = FirebaseAuth.getInstance().getCurrentUser();

        } catch (SecurityException | IllegalArgumentException| DatabaseException ex) {
            logger.error(Reporter.stringStackTrace(ex));
        }

    }

    @Override
    public void onDestroy() {
        //logger.write("onDestroy");
        super.onDestroy();
        if (mLocationManager != null) {
            for (int i = 0; i < mLocationListeners.length; i++) {
                try {
                    if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    mLocationManager.removeUpdates(mLocationListeners[i]);
                } catch (Exception ex) {
                    logger.error(Reporter.stringStackTrace(ex));
                }
            }
        }
    }

    private void initializeLocationManager() {
        //logger.write("initializeLocationManager - LOCATION_INTERVAL: " + LOCATION_INTERVAL + " LOCATION_DISTANCE: " + LOCATION_DISTANCE);
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }
}

