package com.codebase.quicklocation;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
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
import com.codebase.quicklocation.database.dao.FavoritesDao;
import com.codebase.quicklocation.model.Geometry;
import com.codebase.quicklocation.model.LastLocation;
import com.codebase.quicklocation.model.Location;
import com.codebase.quicklocation.model.PlaceDetail;
import com.codebase.quicklocation.model.ResponseForPlaceDetails;
import com.codebase.quicklocation.model.Review;
import com.codebase.quicklocation.model.UserOpinion;
import com.codebase.quicklocation.utils.HTTPTasks;
import com.codebase.quicklocation.utils.Reporter;
import com.codebase.quicklocation.utils.Utils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

public class PlaceDetailActivity extends AppCompatActivity {
    private static final int FROM_FAVORITE = 6;

    private ImageView ivPlacePhoto;
    private TextView tvPlaceDirection;
    private TextView tvWebsite;
    private TextView tvPlacePhone;
    private TextView tvPlaceOpeningHours;
    private Button callButton;
    private String strPlaceName;
    private String strPlaceDirection;
    private String strPlacePhone;
    private String strPlaceId;
    private String strCategory;
    private String strWebsite;
    private Double doubleRating;
    private StringBuilder strOpeningHours;
    private ResponseForPlaceDetails response;
    private Toolbar toolbar;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private Reporter logger = Reporter.getInstance(PlaceDetailActivity.class);
    private FavoritesDao dao;
    private View layoutReviews;
    private boolean missingInformation = false;
    private List<Review> togetherReviews = new ArrayList<>();
    private Geometry serverGeometry;
    private PlaceDetail placeFavoriteType = new PlaceDetail();
    private boolean from_favorito = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_detail);
        dao = new FavoritesDao(this);
        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing);
        collapsingToolbarLayout.setTitleEnabled(false);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);


        if (getIntent().hasExtra("from_favorito")) {
            from_favorito = getIntent().getExtras().getBoolean("from_favorito");
        }

        ivPlacePhoto = (ImageView) findViewById(R.id.iv_place_photo);
        tvPlacePhone = (TextView) findViewById(R.id.tv_phone_number);
        tvPlaceDirection = (TextView) findViewById(R.id.tv_place_direction);
        tvPlaceOpeningHours = (TextView) findViewById(R.id.tv_opening_hours);
        tvWebsite = (TextView) findViewById(R.id.tv_website);
        callButton = (Button) findViewById(R.id.call_action_button);
        layoutReviews = findViewById(R.id.layout_reviews);

        try {
            Bundle bundle = getIntent().getExtras();

            strPlaceName = bundle.getString(PlaceActivity.KEY_PLACE_NAME);
            setTitle(strPlaceName);

            strPlaceId = bundle.getString(PlaceActivity.KEY_PLACE_ID);
            strCategory = bundle.getString(PlaceActivity.KEY_APP_CATEGORY);
            doubleRating = bundle.getDouble(PlaceActivity.KEY_PLACE_RATING);

            Button button = (Button) findViewById(R.id.button_add_favorite);
            ImageView imageView = (ImageView) findViewById(R.id.image_add_favorite);
            Favorites f = dao.getByPlaceId(strPlaceId);

            if(f != null) {
                button.setVisibility(View.GONE);
                imageView.setVisibility(View.GONE);
            }

            String key = Utils.giveMeMyCandy();

            if (key != null) {
                String url = getString(R.string.google_api_place_details_url) + "placeid=" + strPlaceId + "&key=" + key;
                DownloadDetailOfPlace downloader = new DownloadDetailOfPlace();
                downloader.execute(url);
            } else
                Utils.showToast(this, "No se encontró el key de acceso al API");
        } catch (Exception e) {
            logger.error(Reporter.stringStackTrace(e));
        }
        File directImge = new File(Utils.targetPath);
        if (!directImge.exists()) {
            directImge.mkdirs();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (from_favorito) {
            Intent intent = new Intent();
            setResult(FROM_FAVORITE, intent);
            finish();
            //intent.putExtra()
        }
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
            String lastLocation = Utils.getSavedLocation(PlaceDetailActivity.this);

            if (!"no_location".equals(lastLocation)) {
                LastLocation userLocation = Utils.factoryGson().fromJson(lastLocation, LastLocation.class);
                Date date = new Date(userLocation.getTime());
                Log.e("GPSTrackingService", date.toString());
                Location placeLocation;
                if (userLocation != null) {
                    if(serverGeometry != null) {
                        logger.write("****************** COJO LA COORDENADA DEL SERVIDOR ");
                        placeLocation = serverGeometry.getLocation();
                        placeFavoriteType.setGeometry(serverGeometry);
                    } else {
                        logger.write("****************** COJO LA COORDENADA DEL GOOGLE ");
                        placeLocation = response.getResult().getGeometry().getLocation();
                    }
                    String queryParams = placeLocation.getLat() + "," + placeLocation.getLng();
                    Uri gmmIntentUri = Uri.parse("google.navigation:q=" + queryParams);
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    startActivity(mapIntent);
                }
            } else {
                Utils.showToast(this, "No puedo ubicarte en este momento.");
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
            progressDialog.dismiss();
            if (!result.contains("Error!")) {
                response = Utils.factoryGson().fromJson(result, ResponseForPlaceDetails.class);

                if ("OK".equals(response.getStatus())) {
                    PlaceDetail detail = response.getResult();
                    placeFavoriteType = response.getResult();//se asigna el resultado al favorito si ha de guardarse, esto ira cambiando

                    if (detail.getPhotos() != null && detail.getPhotos().size() > 0) {
                        String key = Utils.giveMeMyCandy();

                        if (key != null) {
                            String photoUrl = getString(R.string.google_api_place_photo_url) +
                                    "maxwidth=12040&photoreference=" + detail.getPhotos().get(0).getPhotoReference() +
                                    "&key=" + Utils.giveMeMyCandy();

                            DisplayMetrics metrics = new DisplayMetrics();
                            getWindowManager().getDefaultDisplay().getMetrics(metrics);

                            int height = (int) (metrics.heightPixels * 0.50);

                            Picasso.with(PlaceDetailActivity.this)
                                    .load(photoUrl)
                                    .error(R.drawable.default_img)
                                    .resize(metrics.widthPixels, height)
                                    .into(ivPlacePhoto);
                        } else {
                            verificarBitmapLocal();

                        }
                    } else {
                        if(dao.getByPlaceId(strPlaceId) != null)
                            verificarBitmapLocal();
                    }

                    if (detail.getFormattedPhoneNumber() != null) {
                        strPlacePhone = detail.getFormattedPhoneNumber();
                        if ("".equals(strPlacePhone)) {
                            strPlacePhone = "Dato no disponible";
                            callButton.setEnabled(false);
                            missingInformation = true;
                        }
                    } else {
                        callButton.setEnabled(false);
                        strPlacePhone = "Dato no disponible";
                        missingInformation = true;
                    }

                    if (detail.getFormattedAddress() != null)
                        strPlaceDirection = detail.getFormattedAddress();
                    else {
                        missingInformation = true;
                    }

                    if (detail.getOpeningHours() != null) {
                        if (detail.getOpeningHours().getWeekdayText() != null && detail.getOpeningHours().getWeekdayText().size() > 0) {
                            strOpeningHours = new StringBuilder();

                            for (String str : detail.getOpeningHours().getWeekdayText())
                                strOpeningHours.append(str).append("\n");
                        }

                        if (strOpeningHours.equals(""))
                            tvPlaceOpeningHours.setText("Dato no disponible");
                        else
                            tvPlaceOpeningHours.setText(Utils.formatDays(strOpeningHours));
                    } else {
                        tvPlaceOpeningHours.setText("Dato no disponible");
                        missingInformation = true;
                    }

                    tvPlacePhone.setText(strPlacePhone);
                    tvPlaceDirection.setText(strPlaceDirection);

                    if (detail.getWebsite() == null) {
                        strWebsite = "Dato no disponible";
                        tvWebsite.setText(strWebsite);
                    } else {
                        tvWebsite.setText(detail.getWebsite());
                        strWebsite = detail.getWebsite();
                    }

                    //Buscar reviews, de Google primero, luego del server
                    if(response.getResult().getReviews() != null && !response.getResult().getReviews().isEmpty()) {
                        togetherReviews.clear();
                        togetherReviews.addAll(response.getResult().getReviews());
                    }

                    getPlaceReviewaFromPlatform(strPlaceId);
                    //fin de busqueda de reviews

                    logger.write("Missing information " + missingInformation);

                    //if(missingInformation)
                    getPlaceDataFromPlatform(strPlaceId);

                } else if ("ZERO_RESULTS".equals(response.getStatus())) {
                    Utils.showToast(PlaceDetailActivity.this, "Tu busqueda no arrojo resultados");
                } else {
                    Utils.showToast(PlaceDetailActivity.this, response.getStatus());
                }
            }
        }

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(PlaceDetailActivity.this, "Buscando", "Por favor espere ...");
        }

        @Override
        protected void onProgressUpdate(String... text) {
        }
    }

    public void goToWebsite(View view){
        if (!"Dato no disponible".equals(strWebsite)){
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(strWebsite));
            startActivity(i);
        }
    }

    private void verificarBitmapLocal() {
        Bitmap opcImagePlace = null;
        try {
            opcImagePlace = MediaStore.Images.Media.getBitmap(PlaceDetailActivity.this.getContentResolver(), Utils.getImageUri(strPlaceId));
        } catch (IOException e) {
            logger.error(Reporter.stringStackTrace(e));
        }
        if (opcImagePlace != null) {
            //ivPlacePhoto.setImageBitmap(opcImagePlace);

            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);

            int height = (int) (metrics.heightPixels * 0.158);

            Picasso.with(PlaceDetailActivity.this)
                    .load(Utils.getImageUri(strPlaceId))
                    .error(R.drawable.default_img)
                    .resize(metrics.widthPixels, height)
                    .into(ivPlacePhoto);

        } else {
            ivPlacePhoto.setImageResource(R.drawable.default_img);
        }
    }

    /**
     * Guarda la seleccion en la tabla de favoritos
     *
     * @param v parametro de vista
     */
    public void addFavorite(View v) {
        dao = new FavoritesDao(this);
        Favorites f = dao.getByPlaceId(strPlaceId);
        if (f == null) {
            Favorites favorite = new Favorites();
            favorite.setLocalName(strPlaceName);
            favorite.setRating(doubleRating);
            favorite.setCategory(strCategory);
            favorite.setAddedFrom(new Date());
            favorite.setPlaceId(strPlaceId);
            Intent i = new Intent(PlaceDetailActivity.this, AddFavoritesActivity.class);
            String cdata = Utils.objectToJson(favorite);
            //String detailsResponse = Utils.objectToJson(response);
            String detailsResponse = Utils.objectToJson(placeFavoriteType);
            i.putExtra("placeDetails", detailsResponse);
            i.putExtra("cdata", cdata);
            startActivity(i);
        } else {
            Utils.showToast(this, "Favorito ya existe");
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        //verificarBitmapLocal();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.place_detail_menu, menu);
        return true;
    }

    /**
     * Este metodo despliega la ventana de Mejora de Informacion
     * @param item menu item que debe ser clickeado
     */
    public void showImprovementScreen(MenuItem item) {
        Intent i = new Intent(PlaceDetailActivity.this, ImprovementActivity.class);
        i.putExtra("place_id", strPlaceId);
        i.putExtra("api_response", Utils.objectToJson(response));
        startActivity(i);
    }

    /**
     * Este metodo responde ante el evento click sobre el item de menu
     * Quejas o Sugerencias, despliega la ventana de Reportes
     * @param item menu item que debe ser clickeado
     */
    public void showCommentsScreen(MenuItem item) {
        Intent i = new Intent(PlaceDetailActivity.this, ReportActivity.class);
        i.putExtra("place_id", strPlaceId);
        i.putExtra("api_response", Utils.objectToJson(response));
        startActivity(i);
    }

    private void getPlaceDataFromPlatform(String placeId) {
        try {
            logger.write("************ INTENTO BUSCAR INFO EN MI PLATAFORMA ... ");
            final FirebaseDatabase database = FirebaseDatabase.getInstance();
            database.getReference().child("places/data").child(placeId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    setScreenForNewData(dataSnapshot.getValue(PlaceDetail.class));
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {}
            });
        }catch (Exception e) {
            Utils.showToast(this, "Ha ocurrido un error " + e.getMessage());
        }
    }

    /**
     * Coloca en pantalla la data retornada desde el Firebase por ausencia en Google
     * @param place lugar retornado por el API
     */
    private void setScreenForNewData(PlaceDetail place) {

        if(place != null) {
            placeFavoriteType = place;//seteo lo nuevo al favorito

            if(place.getGeometry() != null) {
                serverGeometry = place.getGeometry();
            }

            logger.write("********************** AJUSTANDO LA DATA FRESCA ... " + place);
            if(place.getFormattedAddress() != null && place.getFormattedAddress().length() > 0)
                tvPlaceDirection.setText(place.getFormattedAddress());
            if(place.getFormattedPhoneNumber() != null && place.getFormattedPhoneNumber().length() > 0)
                tvPlacePhone.setText(place.getFormattedPhoneNumber());

            if(place.getOpeningHours() != null && place.getOpeningHours().getWeekdayText() != null && !place.getOpeningHours().getWeekdayText().isEmpty()) {
                StringBuilder builder = new StringBuilder();
                for (String str : place.getOpeningHours().getWeekdayText())
                    builder.append(str).append("\n");
                tvPlaceOpeningHours.setText(Utils.formatDays(builder));
            }

            if(place.getFormattedPhoneNumber() != null && place.getFormattedPhoneNumber().length() > 0) {
                strPlacePhone = place.getFormattedPhoneNumber();
                tvPlacePhone.setText(strPlacePhone);
            }

            if(place.getWebsite() != null && place.getWebsite().length() > 0) {
                strWebsite = place.getWebsite();
                tvWebsite.setText(strWebsite);
            }
        } else
            logger.write("********************* NO ENCONTRE NADA DE VALOR ...");
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
                    placeFavoriteType.setReviews(togetherReviews);// seteo al favorito todos los reviews recolectados
                    putNewReviewsInTheScreen(togetherReviews);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {}
            });
        }catch (Exception e) {
            Utils.showToast(this, "Ha ocurrido un error " + e.getMessage());
        }
    }

    private void putNewReviewsInTheScreen(List<Review> reviews) {
        logger.write("******************** COLOCANDO REVIEWS DESDE LA PLATAFORMA PROPIA ... ");
        try {
            for(Review r : reviews){
                if(!"".equals(r.getText().trim())) {
                    logger.write("**************** LOCAL REVIEW " + r.getText());
                    TextView authorTextView = new TextView(PlaceDetailActivity.this);
                    TextView commentTextView = new TextView(PlaceDetailActivity.this);
                    TextView ratingView = new TextView(PlaceDetailActivity.this);
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
                    View separator = new View(PlaceDetailActivity.this);
                    separator.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1));
                    separator.setBackgroundColor(getResources().getColor(R.color.accent));
                    ((LinearLayout) layoutReviews).addView(separator);
                }
            }
        }catch(Exception e) {
            Utils.showToast(this, "Ha ocurrido un error " + e.getMessage());
        }
    }

    @Override
    public void onBackPressed() {
        if (from_favorito) {
            Intent intent = new Intent();
            setResult(FROM_FAVORITE, intent);
            finish();
        } else {
            finish();
        }
    }
}
