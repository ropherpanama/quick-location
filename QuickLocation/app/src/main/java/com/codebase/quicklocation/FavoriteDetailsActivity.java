package com.codebase.quicklocation;

import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.codebase.quicklocation.database.Favorites;
import com.codebase.quicklocation.database.FavoritesData;
import com.codebase.quicklocation.database.dao.FavoritesDataDao;
import com.codebase.quicklocation.model.LastLocation;
import com.codebase.quicklocation.model.Location;
import com.codebase.quicklocation.model.PlaceDetail;
import com.codebase.quicklocation.model.ResponseForPlaceDetails;
import com.codebase.quicklocation.model.Review;
import com.codebase.quicklocation.model.UserOpinion;
import com.codebase.quicklocation.utils.Reporter;
import com.codebase.quicklocation.utils.Utils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by fgcanga on 6/18/17.
 */

public class FavoriteDetailsActivity extends AppCompatActivity {

    LinearLayout llayoutFavorite;
    private ImageView ivPlacePhoto;
    private Button callButton;
    private String strPlaceDirection;
    private String strPlacePhone;
    private TextView tvPlaceDirection;
    private TextView tvPlacePhone;
    private TextView tvPlaceOpeningHours;
    private TextView tvWebsite;
    private String targetPath = Utils.targetPath;
    private FavoritesDataDao favoritesDataDao;
    private FavoritesData favoritesData;
    private String cdata;
    private Favorites favorite;
    private PlaceDetail placeDetail;
    private StringBuilder strOpeningHours;
    private Toolbar toolbar;
    private Reporter logger = Reporter.getInstance(FavoriteDetailsActivity.class);
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private View layoutReviews;
    private String strPlaceId;
    private List<Review> togetherReviews = new ArrayList<>();
    //private PlaceDetail placeFavoriteType = new PlaceDetail();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_detail);

        layoutReviews = findViewById(R.id.layout_reviews);
        llayoutFavorite = (LinearLayout) findViewById(R.id.llayoutFavorite);
        llayoutFavorite.setVisibility(View.GONE);
        ivPlacePhoto = (ImageView) findViewById(R.id.iv_place_photo);
        Bundle incomming = getIntent().getExtras();
        cdata = incomming.getString("favorito");
        favoritesDataDao = new FavoritesDataDao(this);
        favorite = Utils.factoryGson().fromJson(cdata, Favorites.class);

        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing);
        collapsingToolbarLayout.setTitleEnabled(false);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        tvPlacePhone = (TextView) findViewById(R.id.tv_phone_number);
        tvPlaceDirection = (TextView) findViewById(R.id.tv_place_direction);
        tvPlaceOpeningHours = (TextView) findViewById(R.id.tv_opening_hours);
        callButton = (Button) findViewById(R.id.call_action_button);
        tvWebsite = (TextView) findViewById(R.id.tv_website);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        int height = (int) (metrics.heightPixels * 0.158);

        Picasso.with(FavoriteDetailsActivity.this)
                .load(getImageUri())
                .error(R.drawable.default_img)
                .resize(metrics.widthPixels, height)
                .into(ivPlacePhoto);

        tvPlacePhone = (TextView) findViewById(R.id.tv_phone_number);
        tvPlaceDirection = (TextView) findViewById(R.id.tv_place_direction);
        tvPlaceOpeningHours = (TextView) findViewById(R.id.tv_opening_hours);
        callButton = (Button) findViewById(R.id.call_action_button);

        favoritesData = favoritesDataDao.getByPlaceId(favorite.getPlaceId());
        placeDetail = Utils.factoryGson().fromJson(favoritesData.getCdata(), PlaceDetail.class);
        strPlaceId = favorite.getPlaceId();
        setTitle(favorite.getLocalName());
        showViewDetails();
    }

    private void showViewDetails() {
        if (placeDetail.getFormattedPhoneNumber() != null) {
            strPlacePhone = placeDetail.getFormattedPhoneNumber();
            if ("".equals(strPlacePhone)) {
                strPlacePhone = "Dato no disponible";
                callButton.setEnabled(false);
            }
        } else {
            callButton.setEnabled(false);
            strPlacePhone = "Dato no disponible";
        }

        if (placeDetail.getFormattedAddress() != null)
            strPlaceDirection = placeDetail.getFormattedAddress();

        if (placeDetail.getOpeningHours() != null) {
            if (placeDetail.getOpeningHours().getWeekdayText() != null && placeDetail.getOpeningHours().getWeekdayText().size() > 0) {
                strOpeningHours = new StringBuilder();

                for (String str : placeDetail.getOpeningHours().getWeekdayText())
                    strOpeningHours.append(str).append("\n");
            }

            if (strOpeningHours.equals(""))
                tvPlaceOpeningHours.setText("Dato no disponible");
            else
                tvPlaceOpeningHours.setText(Utils.formatDays(strOpeningHours));
        } else
            tvPlaceOpeningHours.setText("Dato no disponible");

        tvPlacePhone.setText(strPlacePhone);
        tvPlaceDirection.setText(strPlaceDirection);

        if (placeDetail.getWebsite() == null)
            tvWebsite.setText("Dato no disponible");
        else
            tvWebsite.setText(placeDetail.getWebsite());

        if(placeDetail.getReviews() != null) {
            togetherReviews.clear();
            togetherReviews.addAll(placeDetail.getReviews());
        }

        getPlaceReviewaFromPlatform(strPlaceId);
    }

    public void goToWebsite(View view) {
        if (!tvWebsite.getText().equals("Dato no disponible")) {
            String url = tvWebsite.getText().toString();
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    /**
     * Método que crea el nombre con la ruta de la imagen tomada con la cámara.
     *
     * @return
     */
    private Uri getImageUri() {
        File file = new File(targetPath, favorite.getPlaceId() + ".jpg");
        Uri imgUri = Uri.fromFile(file);
        return imgUri;
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
            logger.error(Reporter.stringStackTrace(se));
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
            String lastLocation = Utils.getSavedLocation(FavoriteDetailsActivity.this);

            if (!"no_location".equals(lastLocation)) {
                LastLocation userLocation = Utils.factoryGson().fromJson(lastLocation, LastLocation.class);
                Date date = new Date(userLocation.getTime());
                Log.e("GPSTrackingService", date.toString());

                if (userLocation != null) {
                    Location placeLocation = placeDetail.getGeometry().getLocation();//TODO checkear esto
                    String queryParams = placeLocation.getLat() + "," + placeLocation.getLng();
                    Uri gmmIntentUri = Uri.parse("google.navigation:q=" + queryParams);
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    startActivity(mapIntent);
                }
            } else {
                Utils.showToast(this, "No puedo ubicarte ...");
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.fav_details_menu, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private void putNewReviewsInTheScreen(List<Review> reviews) {
        logger.write("******************** COLOCANDO REVIEWS DESDE LA PLATAFORMA PROPIA ... ");
        try {
            for(Review r : reviews){
                if(!"".equals(r.getText().trim())) {
                    logger.write("**************** LOCAL REVIEW " + r.getText());
                    TextView authorTextView = new TextView(FavoriteDetailsActivity.this);
                    TextView commentTextView = new TextView(FavoriteDetailsActivity.this);
                    TextView ratingView = new TextView(FavoriteDetailsActivity.this);
                    commentTextView.setText(r.getText());
                    authorTextView.setText(r.getAuthor());
                    ratingView.setText(String.valueOf(r.getRating()));
                    ratingView.setGravity(Gravity.RIGHT);
                    ratingView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_star_review, 0);
                    commentTextView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                    commentTextView.setPadding(0, 5, 0, 5);
                    authorTextView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                    authorTextView.setTypeface(null, Typeface.BOLD);
                    ratingView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                    ((LinearLayout) layoutReviews).addView(authorTextView);
                    ((LinearLayout) layoutReviews).addView(commentTextView);
                    ((LinearLayout) layoutReviews).addView(ratingView);
                    View separator = new View(FavoriteDetailsActivity.this);
                    separator.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1));
                    separator.setBackgroundColor(getResources().getColor(R.color.accent));
                    ((LinearLayout) layoutReviews).addView(separator);
                }
            }
        }catch(Exception e) {
            Utils.showToast(this, "Ha ocurrido un error " + e.getMessage());
        }
    }

    /**
     * Este metodo despliega la ventana de Mejora de Informacion
     * @param item menu item que debe ser clickeado
     */
    public void showImprovementScreenFromFav(MenuItem item) {
        ResponseForPlaceDetails response = new ResponseForPlaceDetails();
        response.setResult(placeDetail);
        Intent i = new Intent(FavoriteDetailsActivity.this, ImprovementActivity.class);
        i.putExtra("place_id", strPlaceId);
        i.putExtra("api_response", Utils.objectToJson(response));
        startActivity(i);
    }

    /**
     * Este metodo responde ante el evento click sobre el item de menu
     * Quejas o Sugerencias, despliega la ventana de Reportes
     * @param item menu item que debe ser clickeado
     */
    public void showCommentsScreenFromFav(MenuItem item) {
        ResponseForPlaceDetails response = new ResponseForPlaceDetails();
        response.setResult(placeDetail);
        Intent i = new Intent(FavoriteDetailsActivity.this, ReportActivity.class);
        i.putExtra("place_id", strPlaceId);
        i.putExtra("api_response", Utils.objectToJson(response));
        startActivity(i);
    }

    /**
     * Busca los reviews de la plataforma si Google no los tiene
     * @param placeId
     */
    private void getPlaceReviewaFromPlatform(final String placeId) {
        try {
            final FirebaseDatabase database = FirebaseDatabase.getInstance();
            database.getReference().child("places/reviews").child(placeId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.getValue() != null) {
                        List<Review> serverReviews = new ArrayList<>();
                        for (DataSnapshot messageSnapshot: dataSnapshot.getChildren()) {
                            UserOpinion message = messageSnapshot.getValue(UserOpinion.class);
                            Review review = new Review();
                            review.setAuthor(message.getAuthorName());
                            review.setRating(message.getRating());
                            review.setText(message.getComment());
                            serverReviews.add(review);
                        }

                        //agrego las reviews que vienen del server si las hay
                        if(serverReviews != null)
                            togetherReviews.addAll(serverReviews);
                    }
                    //Envio los reviews recolectados a pintar en pantalla
                    putNewReviewsInTheScreen(togetherReviews);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {}
            });
        }catch (Exception e) {
            Utils.showToast(this, "Ha ocurrido un error " + e.getMessage());
        }
    }

}
