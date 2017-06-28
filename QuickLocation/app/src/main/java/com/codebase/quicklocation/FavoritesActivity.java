package com.codebase.quicklocation;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.codebase.quicklocation.adapters.FavoritesItemAdapter;
import com.codebase.quicklocation.database.Favorites;
import com.codebase.quicklocation.database.dao.FavoritesDao;
import com.codebase.quicklocation.utils.Reporter;
import com.codebase.quicklocation.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FavoritesActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private Toolbar toolbar;
    private Reporter logger = Reporter.getInstance(PlaceActivity.class);
    private List<Favorites> favorites = new ArrayList<>();
    private FavoritesDao dao = new FavoritesDao(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);
        try {
            toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            recyclerView = (RecyclerView) findViewById(R.id.list_of_favorites);
            DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL);
            recyclerView.addItemDecoration(dividerItemDecoration);
            recyclerView.setLayoutManager(new LinearLayoutManager(FavoritesActivity.this));
            recyclerView.setHasFixedSize(true);
            recyclerView.addItemDecoration(dividerItemDecoration);
            //se carga la lista de favoritos guardados por el usuario
            favorites = getFavoritesFromDb();

            if(favorites.isEmpty()) {
                Utils.showToast(this, "No hay favoritos para mostrar");
            } else {
                mAdapter = new FavoritesItemAdapter(favorites, new FavoritesItemAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(Favorites item) {
                        String cdata = Utils.objectToJson(item);
                        Intent itentfavoritoDetails = new Intent(FavoritesActivity.this, FavoriteDetailsActivity.class);
                        itentfavoritoDetails.putExtra("favorito",cdata);
                        startActivity(itentfavoritoDetails);
                    }
                });

                recyclerView.setAdapter(mAdapter);
            }
        } catch (Exception e) {
            logger.error(Reporter.stringStackTrace(e));
        }
    }

    private List<Favorites> getFavoritesFromDb() {
        try {
            return dao.getAll();
        }catch (Exception e) {
            logger.error(Reporter.stringStackTrace(e));
            return Collections.emptyList();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.favorites_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        } else if (item.getItemId() == R.id.delete_all) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder
                    .setTitle("Eliminar todo")
                    .setMessage("¿Desea eliminar todos los registros?")
                    .setCancelable(false)
                    .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                        @Override
                        public  void onClick(DialogInterface dialog, int which) {
                            if(dao.deleteAll() > 0) {
                                favorites.clear();//limpio la lista actual
                                mAdapter.notifyDataSetChanged();
                                Utils.showToast(FavoritesActivity.this, "Registros eliminados");
                            } else {
                                Utils.showToast(FavoritesActivity.this, "No puedo borrar los registros");
                            }
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .show();
        }
        return super.onOptionsItemSelected(item);
    }
}
