package com.codebase.quicklocation;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.codebase.quicklocation.database.Users;
import com.codebase.quicklocation.database.dao.UsersDao;
import com.codebase.quicklocation.model.ImprovementInformation;
import com.codebase.quicklocation.model.ImprovementRequest;
import com.codebase.quicklocation.model.LastLocation;
import com.codebase.quicklocation.model.PlaceDetail;
import com.codebase.quicklocation.model.ResponseForPlaceDetails;
import com.codebase.quicklocation.model.Review;
import com.codebase.quicklocation.model.Schedule;
import com.codebase.quicklocation.utils.Reporter;
import com.codebase.quicklocation.utils.Utils;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ImprovementActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private EditText direccion;
    private EditText telefono;
    private String locationData = "";
    private boolean shouldSendData = false;
    private boolean shouldSendSchedule = false;
    private String placeId;
    private Spinner spinnerFrom;
    private Spinner spinnerTo;
    private Spinner spinnerSatFrom;
    private Spinner spinnerSatTo;
    private Spinner spinnerSunFrom;
    private Spinner spinnerSunTo;
    private CheckBox closedSunday;
    private CheckBox closedWeekend;
    private CheckBox enableScheduleForm;
    private String [] weekdays = {"Lunes", "Martes", "Miercoles", "Jueves", "Viernes"};
    private String firebasePlaceRecord;
    private Reporter logger = Reporter.getInstance(ImprovementActivity.class);

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
        firebasePlaceRecord = bundle.getString("api_response");
        closedSunday  = (CheckBox) findViewById(R.id.checkboxSundayClosed);
        closedWeekend = (CheckBox) findViewById(R.id.checkboxWeekendClosed);
        enableScheduleForm = (CheckBox) findViewById(R.id.checkbox_schedule_form);

        //Adapter para todos los spinners
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.schedule_hours, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFrom = (Spinner) findViewById(R.id.spinner_from);
        spinnerFrom.setAdapter(adapter);
        spinnerTo = (Spinner) findViewById(R.id.spinner_to);
        spinnerTo.setAdapter(adapter);
        spinnerSatFrom = (Spinner) findViewById(R.id.spinner_from_sat);
        spinnerSatFrom.setAdapter(adapter);
        spinnerSatTo = (Spinner) findViewById(R.id.spinner_to_sat);
        spinnerSatTo.setAdapter(adapter);
        spinnerSunFrom = (Spinner) findViewById(R.id.spinner_from_sun);
        spinnerSunFrom.setAdapter(adapter);
        spinnerSunTo = (Spinner) findViewById(R.id.spinner_to_sun);
        spinnerSunTo.setAdapter(adapter);
        //el formulario de horarios debe estar deshabilitado por default
        scheduleFormStatus(false);

        closedSunday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((CheckBox) v).isChecked()) {
                    spinnerSunFrom.setEnabled(false);
                    spinnerSunTo.setEnabled(false);
                } else {
                    spinnerSunFrom.setEnabled(true);
                    spinnerSunTo.setEnabled(true);
                }
            }
        });

        closedWeekend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(((CheckBox) v).isChecked()) {
                    spinnerSatFrom.setEnabled(false);
                    spinnerSatTo.setEnabled(false);
                    spinnerSunFrom.setEnabled(false);
                    spinnerSunTo.setEnabled(false);
                    closedSunday.setEnabled(false);
                } else {
                    spinnerSatFrom.setEnabled(true);
                    spinnerSatTo.setEnabled(true);
                    spinnerSunFrom.setEnabled(true);
                    spinnerSunTo.setEnabled(true);
                    closedSunday.setEnabled(true);
                }
            }
        });

        enableScheduleForm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(((CheckBox) v).isChecked()) {
                    scheduleFormStatus(true);
                    shouldSendSchedule = true;
                } else {
                    scheduleFormStatus(false);
                    shouldSendSchedule = false;
                }
            }
        });
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

            //proceso informacion de horarios si el formulario esta habilitado
            if(shouldSendSchedule) {
                ImprovementInformation i = new ImprovementInformation();
                i.setInformationTag("schedule");
                i.setSchedule(true);

                List<Schedule> schedules = new ArrayList<>();
                //Se agrega lunes a viernes
                String hourFrom = spinnerFrom.getSelectedItem().toString();
                String hourTo = spinnerTo.getSelectedItem().toString();

                for(String s : weekdays) {
                    schedules.add(new Schedule(s, hourFrom, hourTo, true));
                }
                //se agregan los fines de semana
                if(closedWeekend.isChecked()) {
                    schedules.add(new Schedule("Sabado", "", "", false));
                } else {
                    schedules.add(new Schedule("Sabado", spinnerSatFrom.getSelectedItem().toString(), spinnerSatTo.getSelectedItem().toString(), true));
                }

                if(!closedSunday.isChecked() && !closedWeekend.isChecked()) {
                    schedules.add(new Schedule("Domingo", spinnerSunFrom.getSelectedItem().toString(), spinnerSunTo.getSelectedItem().toString(), true));
                } else {
                    schedules.add(new Schedule("Domingo", "", "", false));
                }

                i.setSchedules(schedules);
                informations.add(i);
                shouldSendData = true;
            }

            request.setInformations(informations);

            if(shouldSendData) {
                final ImprovementRequest sendToServer = request;
                //enviar trama al servidor
                final FirebaseDatabase database = FirebaseDatabase.getInstance();
                database.getReference().child("places/data").child(placeId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(!dataSnapshot.exists()) {
                            if(firebasePlaceRecord != null) {
                                logger.write("************ NO TENGO EL PLACE PADRE, LO MANDO" + firebasePlaceRecord);
                                ResponseForPlaceDetails responseForPlaceDetails = Utils.factoryGson().fromJson(firebasePlaceRecord, ResponseForPlaceDetails.class);
                                PlaceDetail placeDetail = responseForPlaceDetails.getResult();
                                placeDetail.setReviews(Collections.<Review>emptyList());
                                database.getReference().child("places/new/data").child(placeId).setValue(placeDetail).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        logger.write("************ MANDO EL REVIEW HIJO AL COMPLETARSE EL PADRE");
                                        database.getReference().child("places/new/report-issue").child(placeId).push().setValue(sendToServer);
                                        logger.write("************ HIJO GUARDADO EN SU CASA");
                                    }
                                });
                            }
                        } else {
                            logger.write("************ MANDO EL REVIEW HIJO YA TENIA PADRE");
                            database.getReference().child("places/new/report-issue").child(placeId).push().setValue(sendToServer);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {}
                });

                Utils.showToast(this, "Tu información ha sido recibida");
                finish();
            } else {
                Utils.showToast(this, "No se ha ingresado nueva data");
            }
        } catch (Exception e) {
            Reporter.getInstance(ImprovementActivity.class).error(Reporter.stringStackTrace(e));
            Utils.showToast(this, "No se puede procesar el envio");
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

    private void scheduleFormStatus(boolean status) {
        spinnerFrom.setEnabled(status);
        spinnerTo.setEnabled(status);
        spinnerSatFrom.setEnabled(status);
        spinnerSatTo.setEnabled(status);
        spinnerSunFrom.setEnabled(status);
        spinnerSunTo.setEnabled(status);
        spinnerSunFrom.setEnabled(status);
        spinnerSunTo.setEnabled(status);
        closedSunday.setEnabled(status);
        closedWeekend.setEnabled(status);
    }
}
