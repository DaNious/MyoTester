package pitt.edu.danious.myotester;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class SetupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
    }

    //called when user confirm the information
    public void setup2Workout(View view){
        Intent intent = new Intent(this, WorkoutActivity.class);
        startActivity(intent);
    }
}
