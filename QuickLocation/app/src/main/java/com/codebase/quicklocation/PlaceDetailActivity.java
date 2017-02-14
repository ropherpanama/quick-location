package com.codebase.quicklocation;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.codebase.quicklocation.adapters.PlaceItemAdapter;
import com.codebase.quicklocation.model.LastLocation;
import com.codebase.quicklocation.model.Location;
import com.codebase.quicklocation.model.Place;
import com.codebase.quicklocation.model.PlaceDetail;
import com.codebase.quicklocation.model.ResponseForPlaceDetails;
import com.codebase.quicklocation.model.ResponseForPlaces;
import com.codebase.quicklocation.utils.HTTPTasks;
import com.codebase.quicklocation.utils.Reporter;
import com.codebase.quicklocation.utils.Utils;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.w3c.dom.Text;

import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

public class PlaceDetailActivity extends AppCompatActivity {

    private TextView tvPlaceName;
    private TextView tvPlaceDirection;
    private TextView tvPlacePhone;
    private TextView tvPlaceOpeningHours;
    private TextView tvOpeningStatus;
    private String strPlaceName;
    private String strPlaceDirection;
    private String strPlacePhone;
    private String strOpeningStatus;
    private StringBuilder strOpeningHours;
    private ResponseForPlaceDetails response;
    private Toolbar toolbar;
    private Reporter logger = Reporter.getInstance(PlaceDetailActivity.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_detail);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        tvPlaceName = (TextView) findViewById(R.id.tv_place_name);
        tvPlacePhone = (TextView) findViewById(R.id.tv_phone_number);
        tvPlaceDirection = (TextView) findViewById(R.id.tv_place_direction);
        tvPlaceOpeningHours = (TextView) findViewById(R.id.tv_opening_hours);
        tvOpeningStatus = (TextView) findViewById(R.id.tv_opening_status);

        try {
            Bundle bundle = getIntent().getExtras();
            strPlaceName = bundle.getString(PlaceActivity.KEY_PLACE_NAME);
            String strPlaceId = bundle.getString(PlaceActivity.KEY_PLACE_ID);

            String key = Utils.getApplicationKey(this);
            String url = getString(R.string.google_api_place_details_url) + "placeid=" + strPlaceId + "&key=" + key;

            DownloadDetailOfPlace downloader = new DownloadDetailOfPlace();
            downloader.execute(url);
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

    /**
     * Este metodo realiza la llamada al local
     * al presionar el boton Llamar
     *
     * @param v vista padre del boton (no se utiliza pero es necesaria para el
     *          funcionamiento del click handler)
     */
    public void makeACall(View v) {
        try {
            Intent i = new Intent(Intent.ACTION_CALL);
            i.setData(Uri.parse("tel:" + strPlacePhone));
            startActivity(i);
        } catch (SecurityException se) {
            se.printStackTrace();
        }
    }

    /**
     * Este metodo realiza lo siguiente:
     * Obtiene la coordenada actual del usuario
     * Obtiene la coordenada del lugar escogido
     *
     * @param v vista padre del boton (no se utiliza pero es necesaria para el
     *          funcionamiento del click handler)
     */
    public void goToThePlace(View v) {
        try {
            String lastLocation = Utils.getSavedLocation(PlaceDetailActivity.this);

            if (!"no_location".equals(lastLocation)) {
                LastLocation userLocation = Utils.factoryGson().fromJson(lastLocation, LastLocation.class);
                Date date = new Date(userLocation.getTime());
                Log.e("GPSTrackingService", date.toString());

                if (userLocation != null) {
                    Location placeLocation = response.getResult().getGeometry().getLocation();
                    String queryParams = placeLocation.getLat() + "," + placeLocation.getLng();
                    Uri gmmIntentUri = Uri.parse("google.navigation:q=" + queryParams);
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    startActivity(mapIntent);
                }
            } else {
                //TODO proveer alternativa para cuando no se encuentra archivo de coordenada en disco
                Snackbar.make(findViewById(android.R.id.content), "Had a snack at Snackbar", Snackbar.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            logger.error(Reporter.stringStackTrace(e));
        }
    }

    private class DownloadDetailOfPlace extends AsyncTask<String, String, String> {
        private String apiResponse;
        ProgressDialog progressDialog;

        @Override
        protected String doInBackground(String... params) {
            logger.write("Calling place details API ...");
            try {
                InputStream streamResponse = HTTPTasks.getJsonFromServer(params[0]);
                return new Scanner(streamResponse).useDelimiter("\\A").next();
            } catch (Exception e) {
                apiResponse = "Error! : " + e.getMessage();
                logger.error(Reporter.stringStackTrace(e));
            }
            return apiResponse;
        }

        @Override
        protected void onPostExecute(String result) {
            logger.write(result);
            progressDialog.dismiss();
            if (!result.contains("Error!")) {
                response = Utils.factoryGson().fromJson(result, ResponseForPlaceDetails.class);

                if ("OK".equals(response.getStatus())) {
                    PlaceDetail detail = response.getResult();

                    if(detail.getFormattedPhoneNumber() != null)
                        strPlacePhone = detail.getFormattedPhoneNumber();

                    if(detail.getFormattedAddress() != null)
                        strPlaceDirection = detail.getFormattedAddress();

                    if(detail.getOpeningHours() != null) {
                        if (detail.getOpeningHours().getWeekdayText() != null && detail.getOpeningHours().getWeekdayText().length > 0) {
                            strOpeningHours = new StringBuilder();

                            for (String str : detail.getOpeningHours().getWeekdayText())
                                strOpeningHours.append(str).append("\n");
                        }

                        tvPlaceOpeningHours.setText(Utils.formatDays(strOpeningHours));

                        if (detail.getOpeningHours().isOpenNow()) {
                            tvOpeningStatus.setText("Abierto en este momento");
                            tvOpeningStatus.setTextColor(ContextCompat.getColor(PlaceDetailActivity.this, R.color.accent));
                        } else {
                            tvOpeningStatus.setText("Cerrado en este momento");
                            tvOpeningStatus.setTextColor(ContextCompat.getColor(PlaceDetailActivity.this, android.R.color.holo_red_dark));
                        }
                    }

                    tvPlaceName.setText(strPlaceName);
                    tvPlacePhone.setText(strPlacePhone);
                    tvPlaceDirection.setText(strPlaceDirection);
                } else if ("ZERO_RESULTS".equals(response.getStatus())) {
                    //TODO: proveer la informacion necesaria, de ser posible realizar en este punto una busqueda mas amplia
                    Snackbar.make(toolbar, "Tu busqueda no arrojo resultados", Snackbar.LENGTH_SHORT).show();
                } else {
                    //TODO: Caso probado colocar pantalla de informacion
                    Snackbar.make(toolbar, response.getStatus(), Snackbar.LENGTH_SHORT).show();
                }
            }
        }

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(PlaceDetailActivity.this, "ProgressDialog", "Descargando datos");
        }

        @Override
        protected void onProgressUpdate(String... text) {
        }
    }
}
