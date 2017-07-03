package com.codebase.quicklocation;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.codebase.quicklocation.database.Favorites;
import com.codebase.quicklocation.database.FavoritesData;
import com.codebase.quicklocation.database.dao.FavoritesDao;
import com.codebase.quicklocation.database.dao.FavoritesDataDao;
import com.codebase.quicklocation.utils.Reporter;
import com.codebase.quicklocation.utils.Utils;

import java.io.File;
import java.io.IOException;

public class AddFavoritesActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private FavoritesDao dao;
    private FavoritesDataDao favoritesData;
    private String cdata;
    private Favorites favorite;
    private EditText editTextComment;
    private Button takePictureButton;
    private ImageView imageView;
    private String placeDetailsJSON;
    private String targetPath = Utils.targetPath;
    /**
     * resultado de la camara cuando se toma una foto.
     */
    public int CAMERA_RESULT = 100;
    File directImge = null;
    private Reporter logger = Reporter.getInstance(AddFavoritesActivity.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_favorite);
        try {
            editTextComment = (EditText) findViewById(R.id.edit_favorite_comment);
            toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            dao = new FavoritesDao(this);
            favoritesData = new FavoritesDataDao(this);
            Bundle incomming = getIntent().getExtras();
            cdata = incomming.getString("cdata");
            placeDetailsJSON = getIntent().getExtras().getString("placeDetails");
            favorite = Utils.factoryGson().fromJson(cdata, Favorites.class);

            takePictureButton = (Button) findViewById(R.id.button_image);
            imageView = (ImageView) findViewById(R.id.imageview);

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                takePictureButton.setEnabled(false);
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
            }
            directImge = new File(targetPath);
            //directImge = directImge.getParentFile();
            if (!directImge.exists()) {
                directImge.mkdirs();
            }
        } catch (Exception e) {
            logger.error(Reporter.stringStackTrace(e));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.add_fav_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Utils.deleteImage(favorite.getPlaceId());
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Utils.deleteImage(favorite.getPlaceId());
        finish();
        super.onBackPressed();
    }

    /**
     * Guarda el registro en la base de datos
     */
    public void addFavoriteBD(MenuItem item) {
        try {
            Favorites f;
            if (favorite != null) {
                f = dao.getByPlaceId(favorite.getPlaceId());
                if (f != null) {
                    Snackbar.make(toolbar, "Favorito ya existe", Snackbar.LENGTH_SHORT).show();
                } else {
                    if (editTextComment.getText().length() > 0)
                        favorite.setComment(editTextComment.getText().toString());

                    dao.add(favorite);
                    FavoritesData favoriteData = new FavoritesData();
                    favoriteData.setPlaceId(favorite.getPlaceId());
                    favoriteData.setCdata(placeDetailsJSON);
                    favoritesData.add(favoriteData);
                    /**
                     * TODO: G
                     */
                    //Snackbar.make(toolbar, "Favorito guardado", Snackbar.LENGTH_SHORT).show();
                    Utils.showToast(this, "Favorito guardado");
                    Intent intent = new Intent();
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        } catch (Exception e) {
            //Snackbar.make(toolbar, "No puedo guardar tu favorito", Snackbar.LENGTH_SHORT).show();
            Utils.showToast(this, "No puedo guardar tu favorito");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 0) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                takePictureButton.setEnabled(true);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 100) {

            try {
                imageView.setImageBitmap(MediaStore.Images.Media.getBitmap(this.getContentResolver(), Utils.getImageUri(favorite.getPlaceId())));
            } catch (IOException e) {
                logger.error(Reporter.stringStackTrace(e));
            }
        }
    }

    public void takePicture(View view) {
        Intent intent = new Intent(AddFavoritesActivity.this,CapturePhotoActivity.class);
        intent.putExtra("placeID",favorite.getPlaceId());
        startActivityForResult(intent,CAMERA_RESULT);
    }
}
