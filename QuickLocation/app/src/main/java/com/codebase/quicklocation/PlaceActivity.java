package com.codebase.quicklocation;

import android.content.Intent;
import android.graphics.Canvas;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

import com.codebase.quicklocation.adapters.AccessItemAdapter;
import com.codebase.quicklocation.adapters.PlaceItemAdapter;
import com.codebase.quicklocation.model.AccessItem;
import com.codebase.quicklocation.model.Geometry;
import com.codebase.quicklocation.model.Place;
import com.codebase.quicklocation.model.PlaceItem;
import com.codebase.quicklocation.model.ResponseForPlaces;
import com.codebase.quicklocation.utils.Utils;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class PlaceActivity extends AppCompatActivity {
    static final String KEY_DATA = "data";
    private String data;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place);
        Bundle bundle = getIntent().getExtras();
        data = bundle.getString(KEY_DATA);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        try {
            if(Utils.DATA_NOT_FOUND.equals(data))
                System.out.println(data);
            else {
                ResponseForPlaces response = Utils.factoryGson().fromJson(data, ResponseForPlaces.class);
                List<Place>  places = response.getResults();

                //Ajustar la data para mostrar en la lista de resultados
                recyclerView = (RecyclerView) findViewById(R.id.list_of_places);
                recyclerView.setLayoutManager(new LinearLayoutManager(this));
                recyclerView.setHasFixedSize(true);
                recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
                    @Override
                    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
                        super.onDraw(c, parent, state);
                    }
                });

                mAdapter = new PlaceItemAdapter(places, new PlaceItemAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(Place item) {
                        //Snackbar snack = Snackbar.make(toolbar, item.getName() + " " + item.getPlaceId(), Snackbar.LENGTH_SHORT);
                        //snack.show();
                        Intent i = new Intent(PlaceActivity.this, PlaceDetailActivity.class);
                        startActivity(i);
                    }
                });

                recyclerView.setAdapter(mAdapter);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
