package com.codebase.quicklocation;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RatingBar;

import com.codebase.quicklocation.database.Users;
import com.codebase.quicklocation.database.dao.UsersDao;
import com.codebase.quicklocation.model.ImprovementInformation;
import com.codebase.quicklocation.model.ImprovementRequest;
import com.codebase.quicklocation.model.UserReport;
import com.codebase.quicklocation.utils.Reporter;
import com.codebase.quicklocation.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class ReportActivity extends AppCompatActivity {
    private EditText reportContent;
    private RatingBar ratingBar;
    private String placeID;
    private Reporter logger = Reporter.getInstance(ReportActivity.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        reportContent = (EditText) findViewById(R.id.report_edit_text);
        ratingBar = (RatingBar) findViewById(R.id.ratingBar);
        Bundle bundle = getIntent().getExtras();
        placeID = bundle.getString("place_id");
    }

    public void sendReport(View view) {
        try {
            UsersDao dao = new UsersDao(this);
            ImprovementRequest request = new ImprovementRequest();
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
                //enviar trama al servidor
                System.out.println(Utils.objectToJson(request));
            }
        }catch (Exception e) {
            logger.error(Reporter.stringStackTrace(e));
        }
    }
}
