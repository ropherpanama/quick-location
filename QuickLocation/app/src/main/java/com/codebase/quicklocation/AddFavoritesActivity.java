package com.codebase.quicklocation;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.codebase.quicklocation.database.Favorites;
import com.codebase.quicklocation.database.dao.FavoritesDao;
import com.codebase.quicklocation.model.ResponseForPlaceDetails;
import com.codebase.quicklocation.utils.Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AddFavoritesActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private FavoritesDao dao;
    private String cdata;
    private Favorites favorite;
    private EditText editTextComment;
    private Button takePictureButton;
    private ImageView imageView;
    private String placeDetailsJSON;
    private ResponseForPlaceDetails placeDetails;
    private String targetPath = Utils.targetPath;
    /**
     * resultado de la camar cuando se toma una foto.
     */
    public int CAMERA_RESULT = 100;
    File directImge = null;

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
            Bundle incomming = getIntent().getExtras();
            cdata = incomming.getString("cdata");
            placeDetailsJSON = getIntent().getExtras().getString("PlaceDetails");
            favorite = Utils.factoryGson().fromJson(cdata, Favorites.class);
            placeDetails = Utils.factoryGson().fromJson(placeDetailsJSON,ResponseForPlaceDetails.class);

            takePictureButton = (Button) findViewById(R.id.button_image);
            imageView = (ImageView) findViewById(R.id.imageview);

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                takePictureButton.setEnabled(false);
                ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE }, 0);
            }
            directImge = new File(targetPath);
            //directImge = directImge.getParentFile();
            if (!directImge.exists()) {
                directImge.mkdirs();
            }

        } catch (Exception e) {
            e.printStackTrace();
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
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Guarda el registro en la base de datos
     */
    public void guardarFavoritoBD(MenuItem item) {
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

                    Snackbar.make(toolbar, "Favorito guardado", Snackbar.LENGTH_SHORT).show();
                    finish();
                }
            }
        } catch (Exception e) {
            Snackbar.make(toolbar, "No puedo guardar tu favorito", Snackbar.LENGTH_SHORT).show();
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
            if (resultCode == RESULT_OK) {
                if (directImge != null) {
                    if(data != null) {
                        Bundle extras = data.getExtras();
                        if (extras != null) {
                            Bitmap photo = extras.getParcelable("data");
                            OutputStream outStream = null;
                            File file = new File(targetPath, favorite.getPlaceId() + ".jpg");
                            try {
                                Log.d("guardando bitmap", "inciando a guardar la foto " + file.getName());
                                outStream = new FileOutputStream(file);
                                if (photo != null) {
                                    photo.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
                                }
                                outStream.flush();
                                outStream.close();
                                Log.d("guardando bitmap", "finalizando " + file.getName());
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            imageView.setImageBitmap(photo);

                        }
                    }else
                    {
                        try {
                            imageView.setImageBitmap(MediaStore.Images.Media.getBitmap(this.getContentResolver(), getImageUri()));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }



    public void takePicture(View view) {
        Intent camaraItent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        camaraItent.putExtra(MediaStore.EXTRA_OUTPUT, getImageUri());
        this.startActivityForResult(camaraItent, CAMERA_RESULT);

    }

    private static File getOutputMediaFile(){
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "CameraDemo");

        if (!mediaStorageDir.exists()){
            if (!mediaStorageDir.mkdirs()){
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        return new File(mediaStorageDir.getPath() + File.separator +
                "IMG_"+ timeStamp + ".jpg");
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
}
