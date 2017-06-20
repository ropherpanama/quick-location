package com.codebase.quicklocation;

import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.codebase.quicklocation.database.Users;
import com.codebase.quicklocation.database.dao.UsersDao;
import com.codebase.quicklocation.model.ImprovementInformation;
import com.codebase.quicklocation.model.ImprovementRequest;
import com.codebase.quicklocation.model.LastLocation;
import com.codebase.quicklocation.model.Location;
import com.codebase.quicklocation.utils.Reporter;
import com.codebase.quicklocation.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class ImprovementActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private EditText direccion;
    private EditText telefono;
    private String locationData = "";
    private boolean shouldSendData = false;
    private String placeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_improvement);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        direccion = (EditText) findViewById(R.id.edit_new_address);
        telefono = (EditText) findViewById(R.id.edit_new_phone);
        Bundle bundle = getIntent().getExtras();
        placeId = bundle.getString("place_id");
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
        inflater.inflate(R.menu.improvement_menu, menu);
        return true;
    }

    /**
     * Realiza la revision de los datos a actualizar y la envia al servidor
     * @param item menu item
     */
    public void sendImprovementRequest(MenuItem item) {
        try {
            UsersDao dao = new UsersDao(this);
            List<Users> usuarios = dao.getAll();
            List<ImprovementInformation> informations = new ArrayList<>();
            String newDireccion = direccion.getText().toString();
            String newTelefono = telefono.getText().toString();
            ImprovementRequest request = new ImprovementRequest();
            request.setPlaceId(placeId);

            if(!usuarios.isEmpty())
                request.setAuthor(usuarios.get(0).getNickname());
            else
                request.setAuthor("anomymous");

            if(newDireccion.length() > 0) {
                ImprovementInformation i = new ImprovementInformation();
                i.setInformationContent(newDireccion);
                i.setSchedule(false);
                i.setInformationTag("address");
                informations.add(i);
                shouldSendData = true;
            }

            if(newTelefono.length() > 0) {
                ImprovementInformation i = new ImprovementInformation();
                i.setInformationContent(newTelefono);
                i.setSchedule(false);
                i.setInformationTag("telephone");
                informations.add(i);
                shouldSendData = true;
            }

            if(locationData.length() > 0) {
                ImprovementInformation i = new ImprovementInformation();
                i.setInformationContent(locationData);
                i.setSchedule(false);
                i.setInformationTag("location");
                informations.add(i);
                shouldSendData = true;
            }

            request.setInformations(informations);

            if(shouldSendData) {
                //enviar trama al servidor
                System.out.println(Utils.objectToJson(request));
            } else {
                Snackbar.make(toolbar, "No se ha ingresado nueva data", Snackbar.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Reporter.getInstance(ImprovementActivity.class).error(Reporter.stringStackTrace(e));
            Snackbar.make(toolbar, "No se puede procesar el envio", Snackbar.LENGTH_SHORT);
        }
    }

    /**
     * Ubica la coordenada actual del usuario para adjuntarla a la
     * trama de mejora de datos
     * @param v view
     */
    public void attachMyLocation(View v) {
        try {
            String lastLocation = Utils.getSavedLocation(this);
            if(!"no_location".equals(lastLocation)) {
                LastLocation userLocation = Utils.factoryGson().fromJson(lastLocation, LastLocation.class);
                locationData = userLocation.getLatitude() + ":" + userLocation.getLongitude();
                Toast.makeText(this, "Ubicación adjuntada", Toast.LENGTH_SHORT).show();
            }
        } catch(Exception e) {
            Reporter.getInstance(ImprovementActivity.class).error(Reporter.stringStackTrace(e));
        }
    }
}
