package com.codebase.quicklocation;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.codebase.quicklocation.adapters.CategoryMenuItemAdapter;
import com.codebase.quicklocation.gps.GPSTrackingService;
import com.codebase.quicklocation.model.CategoryMenuItem;
import com.codebase.quicklocation.utils.Reporter;
import com.codebase.quicklocation.utils.Utils;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class WelcomeActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ArrayList<CategoryMenuItem> elements = new ArrayList();
    private LinkedHashMap<String, String> categorias = new LinkedHashMap<>();
    private Reporter logger = Reporter.getInstance(WelcomeActivity.class);;
    static final int PERMISSION_ALL = 1;
    String[] permission;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        try {
            permission= new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.CALL_PHONE
            };

            if(!hasPermissions(this, permission)){
                ActivityCompat.requestPermissions(this, permission, PERMISSION_ALL);
            } else {
                logger.write("Stating service with permissions");
                startService(new Intent(this, GPSTrackingService.class));
            }

            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
            mLayoutManager = new GridLayoutManager(this, 2);
            recyclerView.setLayoutManager(mLayoutManager);
            recyclerView.setHasFixedSize(true);
            fillElementsData();

            mAdapter = new CategoryMenuItemAdapter(elements, new CategoryMenuItemAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(CategoryMenuItem item) {
                    if (!validarEstadoGps()) {
                        showSettingsAlert();
                    } else {
                        Intent i = new Intent(WelcomeActivity.this, PlaceActivity.class);
						
                        if(!hasPermissions(WelcomeActivity.this, permission)){
                            ActivityCompat.requestPermissions(WelcomeActivity.this, permission, 100);
                        }
                        //logger.write("Selected category : " + categorias.get(item.getItemName()));
                        i.putExtra(PlaceActivity.KEY_CATEGORY, categorias.get(item.getItemName()));
                        i.putExtra(PlaceActivity.KEY_APP_CATEGORY, item.getItemName());
                        startActivity(i);
                        //No se llama a finish porque la actividad debe estar disponible
                    }
                }
            });

            recyclerView.setAdapter(mAdapter);

        } catch (Exception e) {
            logger.error(Reporter.stringStackTrace(e));
        }
    }

    private void fillElementsData() {
        String[] arrayCategorias = getResources().getStringArray(R.array.categorias);

        for (String c : arrayCategorias)
            elements.add(new CategoryMenuItem(c.toUpperCase(), Utils.getDrawableByName(this, "drawable", c)));

        //Traduccion de categorias para el API
        categorias.put("POLICIA", "police");
        categorias.put("HOSPITALES", "hospital");
        categorias.put("BOMBEROS", "fire_station");
        categorias.put("FARMACIAS", "pharmacy");
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    private void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle(this.getString(R.string.alert_gps_title));
        alertDialog.setMessage(this.getString(R.string.alert_gps_msg));
        alertDialog.setPositiveButton(this.getString(R.string.alert_gps_btn_ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });

        alertDialog.setNegativeButton(this.getString(R.string.alert_gps_btn_no), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertDialog.show();
    }

    private boolean validarEstadoGps() {
        //validacion de estado de los proveedores GPS
        LocationManager locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (isGPSEnabled) if (isNetworkEnabled) return true;

        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_ALL && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (!validarEstadoGps()){
                showSettingsAlert();
            }else{
                logger.write("Stating service without permissions");
                startService(new Intent(this, GPSTrackingService.class));
            }
        }
    }
}
