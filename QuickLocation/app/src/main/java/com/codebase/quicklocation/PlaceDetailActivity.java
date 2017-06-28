package com.codebase.quicklocation;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
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
import android.widget.Toast;

import com.bumptech.glide.util.Util;
import com.codebase.quicklocation.database.Favorites;
import com.codebase.quicklocation.database.dao.FavoritesDao;
import com.codebase.quicklocation.model.Geometry;
import com.codebase.quicklocation.model.LastLocation;
import com.codebase.quicklocation.model.Location;
import com.codebase.quicklocation.model.PlaceDetail;
import com.codebase.quicklocation.model.ResponseForPlaceDetails;
import com.codebase.quicklocation.model.Review;
import com.codebase.quicklocation.model.ServerReviews;
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
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

public class PlaceDetailActivity extends AppCompatActivity {
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
    private Double doubleRating;
    private StringBuilder strOpeningHours;
    private ResponseForPlaceDetails response;
    private Toolbar toolbar;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private Reporter logger = Reporter.getInstance(PlaceDetailActivity.class);
    private FavoritesDao dao;
    private View layoutReviews;
    private boolean missingInformation = false;
    private List<Review> sumReviews = new ArrayList<>();
    private Geometry serverGeometry;

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
                Snackbar.make(toolbar, "No se encontró el key de acceso al API", Snackbar.LENGTH_LONG).show();
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
                Location placeLocation = new Location();
                if (userLocation != null) {
                    if(serverGeometry != null) {
                        System.out.println("****************** COJO LA COORDENADA DEL SERVIDOR ");
                        placeLocation = serverGeometry.getLocation();
                    } else {
                        System.out.println("****************** COJO LA COORDENADA DEL GOOGLE ");
                        placeLocation = response.getResult().getGeometry().getLocation();
                    }
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
            progressDialog.dismiss();
            if (!result.contains("Error!")) {
                response = Utils.factoryGson().fromJson(result, ResponseForPlaceDetails.class);

                if ("OK".equals(response.getStatus())) {
                    PlaceDetail detail = response.getResult();

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

                    if (detail.getWebsite() == null)
                        tvWebsite.setText("Dato no disponible");
                    else
                        tvWebsite.setText(detail.getWebsite());

                    if(response.getResult().getReviews() != null && !response.getResult().getReviews().isEmpty()) {
                        sumReviews.clear();
                        sumReviews = response.getResult().getReviews();
                    }

                    getPlaceReviewaFromPlatform(strPlaceId);
                    System.out.println("Missing information " + missingInformation);
                    if(missingInformation)
                        getPlaceDataFromPlatform(strPlaceId);

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
            progressDialog = ProgressDialog.show(PlaceDetailActivity.this, "Buscando", "Por favor espere ...");
        }

        @Override
        protected void onProgressUpdate(String... text) {
        }
    }

    public void goToWebsite(View view){
        if (!tvWebsite.getText().equals("Dato no disponible")){
            String url = tvWebsite.getText().toString();
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
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
    public void guardarFavorito(View v) {
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
            String detailsResponse = Utils.objectToJson(response);
            i.putExtra("placeDetails", detailsResponse);
            i.putExtra("cdata", cdata);
            startActivity(i);
        } else {
            Snackbar.make(toolbar, "Favorito ya existe", Snackbar.LENGTH_SHORT).show();
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
            System.out.println("************ INTENTO BUSCAR INFO EN MI PLATAFORMA ... ");
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

            if(place.getGeometry() != null)
                serverGeometry = place.getGeometry();

            System.out.println("********************** AJUSTANDO LA DATA FRESCA ... " + place);
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
        } else
            System.out.println("********************* NO ENCONTRE NADA DE VALOR ...");
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
                        List<Review> freshReviews = new ArrayList<>();
                        for (DataSnapshot messageSnapshot: dataSnapshot.getChildren()) {
                            UserOpinion message = messageSnapshot.getValue(UserOpinion.class);
                            Review review = new Review();
                            review.setAuthor(message.getAuthorName());
                            review.setRating(message.getRating());
                            review.setText(message.getComment());
                            freshReviews.add(review);
                        }

                        for(Review rv : freshReviews) {
                            sumReviews.add(rv);
                            System.out.println("******************* ADDING REVIEW " + rv.getText());
                        }

                        putNewReviewsInTheScreen(sumReviews);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {}
            });
        }catch (Exception e) {
            Utils.showToast(this, "Ha ocurrido un error " + e.getMessage());
        }
    }

    private void putNewReviewsInTheScreen(List<Review> reviews) {
        System.out.println("******************** COLOCANDO REVIEWS DESDE LA PLATAFORMA PROPIA ... ");
        try {
            for(Review r : reviews){
                if(!"".equals(r.getText().trim())) {
                    System.out.println("**************** LOCAL REVIEW " + r.getText());
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
}
