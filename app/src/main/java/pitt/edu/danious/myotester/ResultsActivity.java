package pitt.edu.danious.myotester;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class ResultsActivity extends AppCompatActivity {

    // UI controls
    private TextView tv_resultsLabel, tv_reminder, tv_remindTime;

    // Variables
    private String groupDone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        Intent setupIntent = getIntent();
        groupDone = setupIntent.getStringExtra("extra_data");

        tv_resultsLabel = (TextView) findViewById(R.id.tv_resultsLabel);
        tv_reminder = (TextView) findViewById(R.id.tv_reminderLabel);
        tv_remindTime = (TextView) findViewById(R.id.tv_remindTime);

        if (groupDone.equals("3")) {
            tv_resultsLabel.setText(R.string.textResultsDone);
            tv_reminder.setVisibility(View.GONE);
            tv_remindTime.setText("");
        } else {
            tv_resultsLabel.setText(R.string.textResults);
            tv_reminder.setVisibility(View.VISIBLE);
            // Time reminder
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.HOUR, 1);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
            String currentTime = simpleDateFormat.format(calendar.getTime());
            tv_remindTime.setText(currentTime);
            //TODO: add setting the system alarm function
        }
    }
}
