package com.codebase.quicklocation;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.bumptech.glide.util.Util;
import com.codebase.quicklocation.adapters.FavoritesItemAdapter;
import com.codebase.quicklocation.database.Favorites;
import com.codebase.quicklocation.database.dao.FavoritesDao;
import com.codebase.quicklocation.database.dao.FavoritesDataDao;
import com.codebase.quicklocation.model.Place;
import com.codebase.quicklocation.model.PlaceDetail;
import com.codebase.quicklocation.utils.Reporter;
import com.codebase.quicklocation.utils.Utils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class FavoritesActivity extends AppCompatActivity {
    private static final int ADD_ACTIVITY_FAVORITE = 5;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private Toolbar toolbar;
    private Reporter logger = Reporter.getInstance(PlaceActivity.class);
    private List<Favorites> favorites = new ArrayList<>();
    private List<Favorites> favoritesTemp = new ArrayList<>();
    private FavoritesDao dao = new FavoritesDao(this);
    private FavoritesDataDao dataDao = new FavoritesDataDao(this);
    private boolean add_favorite = false;
    Timer timer = new Timer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);
        if (getIntent().hasExtra("add_favorite")) {
            add_favorite = getIntent().getExtras().getBoolean("add_favorite");
        }
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

            final FirebaseDatabase database = FirebaseDatabase.getInstance();

            for (final Favorites p : favorites) {
                database.getReference().child("places/data").child(p.getPlaceId()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            System.out.println("Processing ... " + dataSnapshot.getValue().toString());
                            PlaceDetail placeDetail = dataSnapshot.getValue(PlaceDetail.class);
                            System.out.println(placeDetail.getName() + ", rating: " + placeDetail.getRating());
                            double a = placeDetail.getRating() * placeDetail.getReviewsCount();
                            double b = p.getRating() * Utils.googleMult;
                            int total = placeDetail.getReviewsCount() + Utils.googleMult;
                            double finalRating = 0;

                            if (a > 0 && b > 0)
                                finalRating = (a + b) / total;
                            else if (a > 0 && b <= 0)
                                finalRating = a / placeDetail.getReviewsCount();
                            else if (a <= 0 && b > 0)
                                finalRating = b / Utils.googleMult;

                            p.setRating(Double.parseDouble(Utils.df.format(finalRating)));
                            favoritesTemp.add(p);
                        } else {
                            favoritesTemp.add(p);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
            }

            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    if (favoritesTemp.size() == favorites.size()) {
                        timer.cancel();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (favoritesTemp.isEmpty()) {
                                    Utils.showToast(FavoritesActivity.this, "No hay favoritos para mostrar");
                                } else {
                                    mAdapter = new FavoritesItemAdapter(favoritesTemp, new FavoritesItemAdapter.OnItemClickListener() {
                                        @Override
                                        public void onItemClick(Favorites item) {
                                            String cdata = Utils.objectToJson(item);
                                            if (add_favorite) {
                                                Intent intent = new Intent();
                                                intent.putExtra("cdata", cdata);
                                                setResult(ADD_ACTIVITY_FAVORITE, intent);
                                                finish();
                                            } else {
                                                Intent i = new Intent(FavoritesActivity.this, PlaceDetailActivity.class);
                                                i.putExtra(PlaceActivity.KEY_PLACE_NAME, item.getLocalName());
                                                i.putExtra(PlaceActivity.KEY_PLACE_ID, item.getPlaceId());
                                                startActivity(i);
                                            }
                                        }
                                    });

                                    recyclerView.setAdapter(mAdapter);
                                }
                            }
                        });
                    } else {
                        System.out.println("Aun no ... ");
                    }
                }
            };

            timer.schedule(timerTask, 0, 100);
        } catch (Exception e) {
            logger.error(Reporter.stringStackTrace(e));
        }
    }

    private List<Favorites> getFavoritesFromDb() {
        try {
            return dao.getAll();
        } catch (Exception e) {
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
                        public void onClick(DialogInterface dialog, int which) {
                            if (dao.deleteAll() > 0) {
                                favoritesTemp.clear();//limpio la lista actual
                                dataDao.deleteAll();
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

    @Override
    protected void onStop() {
        super.onStop();
        timer.cancel();
    }
}
