package com.codebase.quicklocation;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

import com.codebase.quicklocation.model.ResponseForPlaceDetails;
import com.codebase.quicklocation.utils.Utils;

public class PlaceDetailActivity extends AppCompatActivity {

    private TextView data;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_detail);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        data = (TextView) findViewById(R.id.data);

        //load temporal data
        StringBuilder json = Utils.getJsonFromDisk(PlaceDetailActivity.this, "response_for_detail");
        ResponseForPlaceDetails response = Utils.factoryGson().fromJson(json.toString(), ResponseForPlaceDetails.class);
        StringBuilder info = new StringBuilder();
        info.append("Direccion");
        info.append("\n");
        info.append(response.getResult().getFormattedAddress());
        info.append("\n");
        info.append("\n");
        info.append("Web");
        info.append("\n");
        info.append(response.getResult().getWebsite());
        info.append("\n");
        info.append("\n");
        info.append("Telefono");
        info.append("\n");
        info.append(response.getResult().getFormattedPhoneNumber());
        info.append("\n");
        info.append("\n");
        info.append("Horarios");
        info.append("\n");

        for(String str : response.getResult().getOpeningHours().getWeekdayText())
            info.append(str).append("\n");

        data.setText(info);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
