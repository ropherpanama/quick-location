package com.codebase.quicklocation;

import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.codebase.quicklocation.model.LastLocation;
import com.codebase.quicklocation.model.Location;
import com.codebase.quicklocation.model.PlaceDetail;
import com.codebase.quicklocation.model.ResponseForPlaceDetails;
import com.codebase.quicklocation.utils.Utils;

import org.w3c.dom.Text;

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
    private ResponseForPlaceDetails response = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_detail);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //load temporal data
        StringBuilder json = Utils.getJsonFromDisk(PlaceDetailActivity.this, "response_for_detail");
        response = Utils.factoryGson().fromJson(json.toString(), ResponseForPlaceDetails.class);
        PlaceDetail detail = response.getResult();

        Bundle bundle = getIntent().getExtras();
        tvPlaceName = (TextView) findViewById(R.id.tv_place_name);
        tvPlacePhone = (TextView) findViewById(R.id.tv_phone_number);
        tvPlaceDirection = (TextView) findViewById(R.id.tv_place_direction);
        tvPlaceOpeningHours = (TextView) findViewById(R.id.tv_opening_hours);
        tvOpeningStatus = (TextView) findViewById(R.id.tv_opening_status);

        strPlaceName = bundle.getString(PlaceActivity.KEY_PLACE_NAME);
        strPlacePhone = detail.getFormattedPhoneNumber();
        strPlaceDirection = detail.getFormattedAddress();

        if(detail.getOpeningHours().getWeekdayText().length > 0) {
            strOpeningHours = new StringBuilder();

            for(String str : detail.getOpeningHours().getWeekdayText())
                strOpeningHours.append(str).append("\n");
        }

        tvPlaceName.setText(strPlaceName);
        tvPlacePhone.setText(strPlacePhone);
        tvPlaceDirection.setText(strPlaceDirection);

        if(detail.getOpeningHours().isOpenNow()) {
            tvOpeningStatus.setText("Abierto en este momento");
            tvOpeningStatus.setTextColor(ContextCompat.getColor(this, R.color.accent));
        } else {
            tvOpeningStatus.setText("Cerrado en este momento");
            tvOpeningStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
        }

        tvPlaceOpeningHours.setText(Utils.formatDays(strOpeningHours));
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
     * @param v vista padre del boton (no se utiliza pero es necesaria para el
     *          funcionamiento del click handler)
     */
    public void makeACall(View v) {
        try {
            Intent i = new Intent(Intent.ACTION_CALL);
            i.setData(Uri.parse("tel:" + strPlacePhone));
            startActivity(i);
        }catch (SecurityException se) {
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

            if(!"no_location".equals(lastLocation)) {
                LastLocation userLocation = Utils.factoryGson().fromJson(lastLocation, LastLocation.class);

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
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
