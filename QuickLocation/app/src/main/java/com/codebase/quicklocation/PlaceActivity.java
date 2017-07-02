package com.codebase.quicklocation;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Canvas;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.codebase.quicklocation.adapters.PlaceItemAdapter;
import com.codebase.quicklocation.database.Users;
import com.codebase.quicklocation.database.dao.UsersDao;
import com.codebase.quicklocation.model.LastLocation;
import com.codebase.quicklocation.model.Place;
import com.codebase.quicklocation.model.ResponseForPlaces;
import com.codebase.quicklocation.model.UserUseStatistic;
import com.codebase.quicklocation.sorters.RatingSorter;
import com.codebase.quicklocation.utils.HTTPTasks;
import com.codebase.quicklocation.utils.Reporter;
import com.codebase.quicklocation.utils.Utils;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

public class PlaceActivity extends AppCompatActivity {
    static final String KEY_CATEGORY = "categoria";
    static final String KEY_APP_CATEGORY = "app_categoria";
    static final String KEY_PLACE_ID = "placeId";
    static final String KEY_PLACE_NAME = "placeName";
    static final String KEY_PLACE_RATING = "placeRating";
    private String key;
    private String categoria;
    private String appCategoria;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private Toolbar toolbar;
    private Reporter logger = Reporter.getInstance(PlaceActivity.class);
    private List<Place> places = new ArrayList<>();
    private List<Place> fakePlaces = new ArrayList<>();//Lista original para efectos de ordenamiento
    private UsersDao usersDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place);
        Bundle bundle = getIntent().getExtras();
        //data contiene la categoria seleccionada por el usuario en la pantalla anterior
        categoria = bundle.getString(KEY_CATEGORY);
        appCategoria = bundle.getString(KEY_APP_CATEGORY);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        try {
            usersDao = new UsersDao(this);
            //descargar data
            String lastLocation = Utils.getSavedLocation(PlaceActivity.this);
            if (!"no_location".equals(lastLocation)) {
                LastLocation userLocation = Utils.factoryGson().fromJson(lastLocation, LastLocation.class);
                key = Utils.giveMeMyCandy();
                if(key != null) {
                    String url = getString(R.string.google_api_nearby_search_url) + "location=" + userLocation.getLatitude() + "," + userLocation.getLongitude() + "&rankby=distance" + "&type=" + categoria + "&key=" + key;
                    DownloadListOfPlaces downloader = new DownloadListOfPlaces();
                    downloader.execute(url);
                } else
                    Snackbar.make(toolbar, "No se encontró el key de acceso al API", Snackbar.LENGTH_LONG).show();
            } else {
                Utils.showMessage("Problemas", "No fue posible determinar tu ubicacion actual. Intentalo mas tarde.", this);
            }
        } catch (Exception e) {
            logger.error(Reporter.stringStackTrace(e));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private class DownloadListOfPlaces extends AsyncTask<String, String, String> {
        private String apiResponse;
        ProgressDialog progressDialog;

        @Override
        protected String doInBackground(String... params) {
            try {
                //logger.write("Calling places API ...");
                InputStream streamResponse = HTTPTasks.getJsonFromServer(params[0]);
                return new Scanner(streamResponse).useDelimiter("\\A").next();
            } catch (Exception e) {
                apiResponse = "Error! : " + e.getMessage();
                logger.error(Reporter.stringStackTrace(e));
                Toast.makeText(PlaceActivity.this, "Error de conexion", Toast.LENGTH_SHORT).show();
            }
            return apiResponse;
        }


        @Override
        protected void onPostExecute(String result) {
            progressDialog.dismiss();
            if (!result.contains("Error!")) {
                ResponseForPlaces response = Utils.factoryGson().fromJson(result, ResponseForPlaces.class);
                if ("OK".equals(response.getStatus())) {
                    //List<Place> places = response.getResults();
                    places.clear();
                    places = response.getResults();

                    for (Place p : places)
                        fakePlaces.add(p);

                    //Ajustar la data para mostrar en la lista de resultados
                    recyclerView = (RecyclerView) findViewById(R.id.list_of_places);
                    DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL);
                    recyclerView.addItemDecoration(dividerItemDecoration);
                    recyclerView.setLayoutManager(new LinearLayoutManager(PlaceActivity.this));
                    recyclerView.setHasFixedSize(true);
                    recyclerView.addItemDecoration(dividerItemDecoration);

                    mAdapter = new PlaceItemAdapter(places, appCategoria, new PlaceItemAdapter.OnItemClickListener() {
                        @Override
                        public void onItemClick(Place item) {
                            //guardar cada acceso del usuario en firebase, para reporte web
                            List<Users> usuarios = usersDao.getAll();

                            if(!usuarios.isEmpty()) {
                                final FirebaseDatabase database = FirebaseDatabase.getInstance();
                                Users u = usuarios.get(0);
                                UserUseStatistic statistic = new UserUseStatistic();
                                statistic.setLoginDate(new Date());
                                statistic.setNickname(u.getNickname());
                                statistic.setPlaceId(item.getPlaceId());
                                statistic.setCategory(categoria);
                                database.getReference().child("places/statistics").child(u.getNickname()).push().setValue(statistic);
                                System.out.println("****************** ESTADISTICA ENVIADA " + Utils.objectToJson(statistic));
                            }

                            Intent i = new Intent(PlaceActivity.this, PlaceDetailActivity.class);
                            i.putExtra(KEY_PLACE_ID, item.getPlaceId());
                            i.putExtra(KEY_PLACE_NAME, item.getName());
                            i.putExtra(KEY_PLACE_RATING, item.getRating());
                            i.putExtra(KEY_APP_CATEGORY, appCategoria);
                            startActivity(i);
                        }
                    });

                    recyclerView.setAdapter(mAdapter);
                } else if ("ZERO_RESULTS".equals(response.getStatus())) {
                    //TODO: proveer la informacion necesaria, de ser posible realizar en este punto una busqueda mas amplia
                    Snackbar.make(toolbar, "Tu busqueda no arrojo resultados", Snackbar.LENGTH_SHORT).show();
                } else {
                    Utils.showMessage("Conexion", "En estos momentos no podemos ubicar tu informacion, intentalo mas tarde", PlaceActivity.this);
                }
            }
        }

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(PlaceActivity.this, "Buscando", "Por favor espere ...");
            progressDialog.setCancelable(true);
        }


        @Override
        protected void onProgressUpdate(String... text) {
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.place_menu, menu);
        return true;
    }

    public void ordenarPorRating(MenuItem item) {
        Collections.sort(places, new RatingSorter());
        mAdapter.notifyDataSetChanged();
    }

    /**
     * Como no tengo manera de ordenar por distancia,
     * se restaura el array original que en teoria viene ordenado por distancia desde el API
     * @param item menu item que activa el evento
     */
    public void ordenarPorDistancia(MenuItem item){
        places.clear();

        for(Place p: fakePlaces)
            places.add(p);

        mAdapter.notifyDataSetChanged();
    }
}
