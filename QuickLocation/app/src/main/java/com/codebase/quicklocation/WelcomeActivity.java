package com.codebase.quicklocation;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
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
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;

import com.codebase.quicklocation.adapters.CategoryMenuItemAdapter;
import com.codebase.quicklocation.database.Users;
import com.codebase.quicklocation.database.dao.UsersDao;
import com.codebase.quicklocation.gps.GPSTrackingService;
import com.codebase.quicklocation.model.CategoryMenuItem;
import com.codebase.quicklocation.model.LastLocation;
import com.codebase.quicklocation.utils.Reporter;
import com.codebase.quicklocation.utils.Utils;
import com.crashlytics.android.Crashlytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import io.fabric.sdk.android.Fabric;

public class WelcomeActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ArrayList<CategoryMenuItem> elements = new ArrayList<>();
    private LinkedHashMap<String, String> categorias = new LinkedHashMap<>();
    private Reporter logger = Reporter.getInstance(WelcomeActivity.class);;
    static final int PERMISSION_ALL = 1;

    String[] permission;
    private FirebaseAuth mAuth;
    private Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser()!=null) {
            setContentView(R.layout.activity_welcome);
            context = this;
            logUser();
            try {
                permission = new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.CALL_PHONE
                };

                if (!hasPermissions(this, permission)) {
                    ActivityCompat.requestPermissions(this, permission, PERMISSION_ALL);
                } else {
                    logger.write("Stating service with permissions");
                    System.out.println("***************** Arrancando servicio desde Menu 2 ...");
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

                            if (!hasPermissions(WelcomeActivity.this, permission)) {
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

            geneararTokenFCM();
            String tokenFcm = FirebaseInstanceId.getInstance().getToken();

            if (tokenFcm!=null) {
                Log.e("onCreate"," token fcm " + tokenFcm);
                DatabaseReference root = FirebaseDatabase.getInstance().getReference().child(Utils.users);
                FirebaseUser task = FirebaseAuth.getInstance().getCurrentUser();
                assert task != null;
                DatabaseReference user_referemce = root.child(task.getUid()).child(Utils.token_fcm);
                user_referemce.setValue(tokenFcm);
            } else {
                Log.e("MainActivity","Token fcm sin crear");
            }
        } else {
            Intent intent = new Intent(this, LoginActivity.class);
            System.out.println("******************** Parando servicio desde Menu 2");
            stopService(new Intent(this, GPSTrackingService.class));
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivity(intent);
            finish();
        }
    }

    private void geneararTokenFCM() {
        String lastLocation = Utils.getSavedLocation(this);
        LastLocation userLocation = Utils.factoryGson().fromJson(lastLocation, LastLocation.class);
        UsersDao usersDao = new UsersDao(this);
        SharedPreferences preferences = context.getSharedPreferences("registration_id", Context.MODE_PRIVATE);
        String token_fcm =  preferences.getString("registration_id", "");

        if (token_fcm.equals("")) {
            String tokenFcm = FirebaseInstanceId.getInstance().getToken();
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("registration_id", tokenFcm);
        }

        DatabaseReference root = FirebaseDatabase.getInstance().getReference().child(Utils.users);
        FirebaseUser task = FirebaseAuth.getInstance().getCurrentUser();
        assert task != null;
        System.out.println("********************* Email para filtrar usuario activo " + task.getEmail());
        Users usuario = usersDao.getByEmail(task.getEmail());
        if(usuario != null) {
            DatabaseReference user_referemce = root.child(task.getUid()).child(Utils.token_fcm);
            user_referemce.setValue(token_fcm);
            user_referemce = root.child(task.getUid()).child("fullname");
            user_referemce.setValue(usuario.getFullname());
            user_referemce = root.child(task.getUid()).child("latitude");
            user_referemce.setValue(userLocation.getLatitude());
            user_referemce = root.child(task.getUid()).child("longitude");
            user_referemce.setValue(userLocation.getLongitude());
            user_referemce = root.child(task.getUid()).child("username");
            user_referemce.setValue(usuario.getNickname());
            user_referemce = root.child(task.getUid()).child("key");
            user_referemce.setValue(task.getUid());
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
                System.out.println("***************** Arrancando servicio desde Menu 1 ...");
                startService(new Intent(this, GPSTrackingService.class));
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    /**
     * Muestra la pantalla de favoritos al tocar el menu Favoritos
     * @param item menu item que activa el evento
     */
    public void showFavorites(MenuItem item) {
        Intent i = new Intent(WelcomeActivity.this, FavoritesActivity.class);
        startActivity(i);
    }


    public void listChats(MenuItem item) {
        Intent intent = new Intent(WelcomeActivity.this, ChatsListActivity.class);
        startActivity(intent);
    }

    /**
     * Método para finalizar la sesión con firebase.
     * @param item
     */
    public void logout(MenuItem item){
        dialogLogout();
    }

    private void dialogLogout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.warning));
        builder.setMessage(context.getString(R.string.salir));
        builder.setCancelable(false);
        builder.setPositiveButton(context.getString(R.string.aceptar), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                mAuth.signOut();
                Intent intent = new Intent(context, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(intent);
                System.out.println("******************** Parando servicio desde Menu 1");
                stopService(new Intent(WelcomeActivity.this, GPSTrackingService.class));
                finish();
            }
        }).setNegativeButton(context.getString(R.string.cancelar),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();

                    }
                });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        Button pbtn = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        pbtn.setTextColor(Color.parseColor("#da1919"));
        Button nbtn = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        nbtn.setTextColor(Color.parseColor("#da1919"));
    }

    /**
     * Registra al usuario en la plataforma  Crashlytics de Fabric.
     */
    private void logUser() {
        Crashlytics.setUserIdentifier(mAuth.getCurrentUser().getEmail());
    }
}
