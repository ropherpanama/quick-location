package com.codebase.quicklocation;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;

import com.codebase.quicklocation.database.Users;
import com.codebase.quicklocation.database.dao.UsersDao;
import com.codebase.quicklocation.model.ImprovementInformation;
import com.codebase.quicklocation.model.ImprovementRequest;
import com.codebase.quicklocation.model.PlaceDetail;
import com.codebase.quicklocation.model.ResponseForPlaceDetails;
import com.codebase.quicklocation.model.Review;
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

public class ReportActivity extends AppCompatActivity {
    private EditText reportContent;
    private RatingBar ratingBar;
    private String placeID;
    private Toolbar toolbar;
    private Reporter logger = Reporter.getInstance(ReportActivity.class);
    private String firebasePlaceRecord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        reportContent = (EditText) findViewById(R.id.report_edit_text);
        ratingBar = (RatingBar) findViewById(R.id.ratingBar);
        Bundle bundle = getIntent().getExtras();
        placeID = bundle.getString("place_id");
        firebasePlaceRecord = bundle.getString("api_response");
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        final Button sendButton = (Button) findViewById(R.id.report_button_send);

        reportContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if(reportContent.getText().length() > 120) {
                    reportContent.setError("Máximo 120 caracteres");
                    sendButton.setEnabled(false);
                } else {
                    reportContent.setError(null);
                    sendButton.setEnabled(true);
                }
            }
        });
    }

    public void sendReport(View view) {
        try {
            UsersDao dao = new UsersDao(this);
            ImprovementRequest request = new ImprovementRequest();
            request.setDate(System.currentTimeMillis());
            String content = reportContent.getText().toString();

            if(content.length() > 0) {
                ImprovementInformation comment = new ImprovementInformation();
                comment.setSchedule(false);
                comment.setInformationTag("comment");
                comment.setInformationContent(content);

                ImprovementInformation rating = new ImprovementInformation();
                rating.setSchedule(false);
                rating.setInformationTag("rating");
                rating.setInformationContent(String.valueOf(ratingBar.getRating()));

                List<ImprovementInformation> informations = new ArrayList<>();
                informations.add(comment);
                informations.add(rating);

                List<Users> users = dao.getAll();
                if(!users.isEmpty())
                    request.setAuthor(users.get(0).getNickname());
                else
                    request.setAuthor("anonymous");

                request.setPlaceId(placeID);
                request.setInformations(informations);
                final ImprovementRequest sendToServer = request;
                //enviar trama al servidor
                final FirebaseDatabase database = FirebaseDatabase.getInstance();
                database.getReference().child("places/data").child(placeID).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(!dataSnapshot.exists()) {
                            if(firebasePlaceRecord != null) {
                                logger.write("************ NO TENGO EL PLACE PADRE, LO MANDO" + firebasePlaceRecord);
                                ResponseForPlaceDetails responseForPlaceDetails = Utils.factoryGson().fromJson(firebasePlaceRecord, ResponseForPlaceDetails.class);
                                PlaceDetail placeDetail = responseForPlaceDetails.getResult();
                                placeDetail.setReviews(Collections.<Review>emptyList());

                                database.getReference().child("places/new/data").child(placeID).setValue(placeDetail).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        logger.write("************ MANDO EL REVIEW HIJO AL COMPLETARSE EL PADRE");
                                        database.getReference().child("places/new/reviews").child(placeID).push().setValue(sendToServer);
                                        logger.write("************ HIJO GUARDADO EN SU CASA");
                                    }
                                });
                            }
                        } else {
                            logger.write("************ MANDO EL REVIEW HIJO YA TENIA PADRE");
                            database.getReference().child("places/new/reviews").child(placeID).push().setValue(sendToServer);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {}
                });
                //logger.write("************ MANDO EL REVIEW HIJO YA TENIA PADRE");
                //database.getReference().child("places/new/reviews").child(placeID).push().setValue(request);
                Utils.showToast(ReportActivity.this, "Tu opinión ha sido enviada");
                finish();
            } else {
                reportContent.setError("Debe introducir un comentario");
            }
        }catch (Exception e) {
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
}
