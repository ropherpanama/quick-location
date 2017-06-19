package com.codebase.quicklocation;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.codebase.quicklocation.database.Users;
import com.codebase.quicklocation.database.dao.UsersDao;
import com.codebase.quicklocation.model.UserReport;
import com.codebase.quicklocation.utils.Reporter;
import com.codebase.quicklocation.utils.Utils;

import java.util.List;

public class ReportActivity extends AppCompatActivity {
    private EditText reportContent;
    private String placeID;
    private Reporter logger = Reporter.getInstance(ReportActivity.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        reportContent = (EditText) findViewById(R.id.report_edit_text);
        Bundle bundle = getIntent().getExtras();
        placeID = bundle.getString("place_id");
    }

    public void sendReport(View view) {
        try {
            UsersDao dao = new UsersDao(this);
            String content = reportContent.getText().toString();
            if(content.length() > 0) {
                UserReport report = new UserReport();
                report.setPlaceId(placeID);
                report.setReportContent(content);

                List<Users> users = dao.getAll();
                if(!users.isEmpty())
                    report.setUsername(users.get(0).getNickname());
                else
                    report.setUsername("anonymous");

                //enviar trama al servidor
                System.out.println(Utils.objectToJson(report));
            }
        }catch (Exception e) {
            logger.error(Reporter.stringStackTrace(e));
        }
    }
}
