package pitt.edu.danious.myotester;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class SetupActivity extends AppCompatActivity {

    public String folderName;
    //UI controls
    private Button btn_infoConfirm;
    private EditText et_name, et_weight;
    private Spinner sp_arm, sp_group;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        btn_infoConfirm = (Button) findViewById(R.id.btn_infoConfirm);
        et_name = (EditText) findViewById(R.id.et_name);
        et_weight = (EditText) findViewById(R.id.et_weight);
        sp_arm = (Spinner) findViewById(R.id.sp_arm);
        sp_group = (Spinner) findViewById(R.id.sp_group);

        // Check permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, 1);
        }
    }

    //check if all permissions are granted
    public boolean hasAllPermissionsGranted(@NonNull int[] grantResults){
        for (int grantResult : grantResults){
            if (grantResult == PackageManager.PERMISSION_DENIED)
                return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1){
            if(hasAllPermissionsGranted(grantResults)){
                btn_infoConfirm.setEnabled(true);// all permissions granted
            }else {
                Toast.makeText(this, "PLEASE grant all permissions of the App", Toast.LENGTH_LONG).show();// some permission are denied.
                btn_infoConfirm.setEnabled(false);
            }
        }
    }

    private boolean saveFolder(){
        final String savedPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DriveSyncFiles";
        Date date = Calendar.getInstance().getTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMddyyyy");
        String currentDate = dateFormat.format(date);
        folderName = et_name.getText().toString() + "-" + currentDate + "-" +
                (String)sp_arm.getSelectedItem() + "-G" + (String)sp_group.getSelectedItem();
        //create the folder
        File folder = new File(savedPath + '/' + folderName);
        if (!folder.exists()) {
            folder.mkdir();
            return true;
        }
        else {
            Toast.makeText(this, "Folder exists, PLEASE check the info", Toast.LENGTH_LONG).show();
            return false;
        }
    }

    //called when user confirm the information
    public void setup2Workout(View view){
        //Save info as the folder name
        if (saveFolder()) {
            Intent intent = new Intent(this, WorkoutActivity.class);
            intent.putExtra("extra_data", folderName);
            startActivity(intent);
        }
    }
}
