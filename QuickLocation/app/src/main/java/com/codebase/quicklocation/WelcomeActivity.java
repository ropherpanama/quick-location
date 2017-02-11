package com.codebase.quicklocation;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.codebase.quicklocation.adapters.CategoryMenuItemAdapter;
import com.codebase.quicklocation.gps.GPSTrackingService;
import com.codebase.quicklocation.model.CategoryMenuItem;
import com.codebase.quicklocation.utils.Reporter;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class WelcomeActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ArrayList<CategoryMenuItem> elements = new ArrayList();
    private SearchView mSearchView;
    private MenuItem searchMenuItem;
    private LinkedHashMap<String, String> categorias = new LinkedHashMap<>();
    private Reporter logger = Reporter.getInstance(WelcomeActivity.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        try {
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            startService(new Intent(this, GPSTrackingService.class));
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
                        logger.write("Selected category : " + categorias.get(item.getItemName()));
                        i.putExtra(PlaceActivity.KEY_CATEGORY, categorias.get(item.getItemName()));
                        startActivity(i);
                        //No se llama a finish porque la actividad debe estar disponible
                    }
                }
            });

            recyclerView.setAdapter(mAdapter);

            if (!validarEstadoGps())
                showSettingsAlert();

        } catch (Exception e) {
            logger.error(Reporter.stringStackTrace(e));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        searchMenuItem = menu.findItem(R.id.search);
        mSearchView = (SearchView) searchMenuItem.getActionView();
        mSearchView.setOnQueryTextListener(listener);
        return true;
    }

    SearchView.OnQueryTextListener listener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String query) {
            return false;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            Toast.makeText(getApplicationContext(), newText, Toast.LENGTH_LONG).show();
            return false;
        }
    };

    private void fillElementsData() {
        elements.add(new CategoryMenuItem("POLICIA", 1));
        elements.add(new CategoryMenuItem("HOSPITALES", 2));
        elements.add(new CategoryMenuItem("BOMBEROS", 3));
        elements.add(new CategoryMenuItem("FARMACIAS", 4));
        //Traduccion de categorias para el API
        categorias.put("POLICIA", "police");
        categorias.put("HOSPITALES", "hospital");
        categorias.put("BOMBEROS", "fire_station");
        categorias.put("FARMACIAS", "pharmacy");
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
}
