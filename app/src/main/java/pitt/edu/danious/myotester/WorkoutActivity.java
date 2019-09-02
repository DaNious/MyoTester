package pitt.edu.danious.myotester;

import androidx.appcompat.app.AppCompatActivity;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.sql.Time;
import java.util.Timer;
import java.util.TimerTask;

public class WorkoutActivity extends AppCompatActivity {

    MediaPlayer player = new MediaPlayer();
    private Timer timer1, timer2, timer3;
    // UI controls
    private ProgressBar pb;
    private TextView tv_instr, tv_count, tv_step, tv_countDown;
    private Button btn_start, btn_stop;
    // Variables
    private int sec = 0;
    private int stepIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout);

        //UI control instances
        pb = (ProgressBar) findViewById(R.id.pb);
        pb.setProgress(0);
        tv_instr = (TextView) findViewById(R.id.tv_instruction);
        tv_count = (TextView) findViewById(R.id.tv_countWorkout);
        tv_step = (TextView) findViewById(R.id.tv_step);
        tv_countDown = (TextView) findViewById(R.id.tv_countDown);
        btn_start = (Button) findViewById(R.id.btn_Start);
        btn_start.setEnabled(true);
        btn_stop = (Button) findViewById(R.id.btn_Stop);
        btn_stop.setEnabled(false);

        //Initialize media player
        player.reset();
//        player = MediaPlayer.create(this, );
        player.setAudioStreamType(AudioManager.STREAM_VOICE_CALL);
    }

    public void singleProcess(View view){
        btn_start.setEnabled(false);
        btn_stop.setEnabled(true);
        tv_instr.setText(this.getString(R.string.textMeasure_stop));
        steadyStep();
    }

    public void autoProcess(View view){
        Thread timer = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            btn_start.performClick();
                        }
                    });
                    try {
                        Thread.sleep(16000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        timer.start();
    }

    public void endProcess(){
        btn_start.setEnabled(true);
        btn_stop.setEnabled(false);
        tv_instr.setText(this.getString(R.string.textMeasure));
        stepIndex = 0;
    }

    private void countDown(){
        sec = sec - 1;
        tv_countDown.setText(Integer.toString(sec) + "s");
    }

    private Handler stepHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 1:
                    pb.incrementProgressBy(1);
                    break;
                case 2:
                    countDown();
                    break;
                case 3:
                    timer1.cancel();
                    timer2.cancel();
                    timer3.cancel();
                    switch (msg.arg1){
                        case 1:
                            putUpStep();
                            break;
                        case 2:
                            holdStep();
                            break;
                        case 3:
                            putDownStep();
                            break;
                        case 4:
                            relaxStep();
                            break;
                        case 5:
                            endProcess();
                            break;
                    }
                    break;
            }
        }
    };

    // Tasks
    private class stepBar extends TimerTask{
        @Override
        public void run() {
            Message msg = new Message();
            msg.what = 1;
            stepHandler.sendMessage(msg);
        }
    }
    private class stepNumber extends TimerTask{
        @Override
        public void run() {
            Message msg = new Message();
            msg.what = 2;
            stepHandler.sendMessage(msg);
        }
    }
    private class stepNext extends TimerTask{
        @Override
        public void run() {
            Message msg = new Message();
            msg.what = 3;
            stepIndex = stepIndex + 1;
            msg.arg1 = stepIndex;
            stepHandler.sendMessage(msg);
        }
    }

    /* Protocol steps */
    private void steadyStep(){
        sec = 3;
        tv_countDown.setText(Integer.toString(sec) + "s");
        pb.setMax(sec*10);
        pb.setProgress(0);
//        startMeasure();
        timer1 = new Timer();
        timer2 = new Timer();
        timer3 = new Timer();
        timer1.schedule(new stepBar(), 0, 100);
        timer2.schedule(new stepNumber(),1000, 1000); // update number 1s
        timer3.schedule(new stepNext(),sec*1000+100); // enter next step 5s
    }

    private void putUpStep(){

        sec = 2;
        tv_countDown.setText(Integer.toString(sec) + "s");
        pb.setMax(sec*10);
        pb.setProgress(0);
        timer1 = new Timer();
        timer2 = new Timer();
        timer3 = new Timer();
        timer1.schedule(new stepBar(), 0, 100);
        timer2.schedule(new stepNumber(),1000, 1000); // update number 1s
        timer3.schedule(new stepNext(),sec*1000+100); // enter next step 5s
    }

    private void holdStep(){
        sec = 3;
        tv_countDown.setText(Integer.toString(sec) + "s");
        pb.setMax(sec*10);
        pb.setProgress(0);
        timer1 = new Timer();
        timer2 = new Timer();
        timer3 = new Timer();
        timer1.schedule(new stepBar(), 0, 100);
        timer2.schedule(new stepNumber(),1000, 1000); // update number 1s
        timer3.schedule(new stepNext(),sec*1000+100); // enter next step 5s
    }

    private void putDownStep(){
        sec = 2;
        tv_countDown.setText(Integer.toString(sec) + "s");
        pb.setMax(sec*10);
        pb.setProgress(0);
        timer1 = new Timer();
        timer2 = new Timer();
        timer3 = new Timer();
        timer1.schedule(new stepBar(), 0, 100);
        timer2.schedule(new stepNumber(),1000, 1000); // update number 1s
        timer3.schedule(new stepNext(),sec*1000+100); // enter next step 5s
    }

    private void relaxStep(){
        sec = 5;
        tv_countDown.setText(Integer.toString(sec) + "s");
        pb.setMax(sec*10);
        pb.setProgress(0);
        timer1 = new Timer();
        timer2 = new Timer();
        timer3 = new Timer();
        timer1.schedule(new stepBar(), 0, 100);
        timer2.schedule(new stepNumber(),1000, 1000); // update number 1s
        timer3.schedule(new stepNext(),sec*1000+100); // enter next step 5s
    }
}
