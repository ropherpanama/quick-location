package com.codebase.quicklocation;

import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import com.codebase.quicklocation.adapters.AccessItemAdapter;
import com.codebase.quicklocation.model.AccessItem;
import com.codebase.quicklocation.utils.Utils;

import java.util.ArrayList;

public class WelcomeActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ArrayList<AccessItem> elements = new ArrayList();
    private SearchView mSearchView;
    private MenuItem searchMenuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mLayoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setHasFixedSize(true);
        fillElementsData();

        mAdapter = new AccessItemAdapter(elements, new AccessItemAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(AccessItem item) {
                Intent i = new Intent(WelcomeActivity.this, PlaceActivity.class);
                StringBuilder jsonData = Utils.getJsonFromDisk(WelcomeActivity.this, "response_api");

                if(jsonData != null)
                    i.putExtra(PlaceActivity.KEY_DATA, jsonData.toString());
                else
                    i.putExtra(PlaceActivity.KEY_DATA, Utils.DATA_NOT_FOUND);

                startActivity(i);
                //No se llama a finish porque la actividad debe estar disponible
            }
        });

        recyclerView.setAdapter(mAdapter);
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

    private void fillElementsData(){
        elements.add(new AccessItem("POLICIA", 1));
        elements.add(new AccessItem("HOSPITALES", 2));
        elements.add(new AccessItem("BOMBEROS", 3));
        elements.add(new AccessItem("FARMACIAS", 4));
    }
}
