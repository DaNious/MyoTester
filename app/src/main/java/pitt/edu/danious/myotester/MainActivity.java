package pitt.edu.danious.myotester;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    //called when user proceed to next
    public void start2Test(View view){
        Intent intent = new Intent(this, WorkoutActivity.class);
        startActivity(intent);
    }
}
