package pitt.edu.danious.myotester;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.audiofx.AutomaticGainControl;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class WorkoutActivity extends AppCompatActivity {

    MediaPlayer player = new MediaPlayer();
    private Timer timer1, timer2, timer3;
    private File file;
    // UI controls
    private ProgressBar pb;
    private TextView tv_instr, tv_count, tv_step, tv_countDown, tv_stopFlag, tv_savedCount;
    private Button btn_start, btn_stop, btn_testRun;
    // Variables
    private int sec = 0;
    private int stepIndex = 0;
    private boolean isAuto = false;
    private boolean isRecording = false;
    private int workoutCnt = 0;
    public static final String TAG = "Record Thread";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout);

        //UI control instances
        pb = (ProgressBar) findViewById(R.id.pb);
        pb.setProgress(0);
        tv_instr = (TextView) findViewById(R.id.tv_instruction);
        tv_count = (TextView) findViewById(R.id.tv_countWorkout);
        tv_savedCount = (TextView) findViewById(R.id.tv_savedWorkoutCount);
        tv_step = (TextView) findViewById(R.id.tv_step);
        tv_step.setText(this.getString(R.string.protoWait));
        tv_countDown = (TextView) findViewById(R.id.tv_countDown);
        tv_stopFlag = (TextView) findViewById(R.id.tv_stopFlag);
        btn_start = (Button) findViewById(R.id.btn_start);
        btn_start.setEnabled(true);
        btn_stop = (Button) findViewById(R.id.btn_Stop);
        btn_stop.setEnabled(false);
        btn_testRun = (Button) findViewById(R.id.btn_testRun);
        btn_testRun.setEnabled(true);

        //Initialize media player
        try {
            player.reset();
            Uri setDataSourceUri = Uri.parse("android.resource://pitt.edu.danious.myotester/" + R.raw.sequence_bpsk_4);
            player.setDataSource(this, setDataSourceUri);
            player.setAudioStreamType(AudioManager.STREAM_VOICE_CALL);
            player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    player.start();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Called when press test-run button
    public void singleProcess(View view){
        btn_start.setEnabled(false);
        btn_stop.setEnabled(false);
        btn_testRun.setEnabled(false);
        tv_instr.setText(this.getString(R.string.testRunMessage));
        steadyStep();
    }

    public void autoProcessFunc(){
        playAndRecord();
        steadyStep();
    }

    public void playAndRecord(){
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                        + "/DriveSyncFiles/" + "Name" + "_musclePCM" + dateFormat.format(date) + ".pcm");
        Log.i(TAG,"生成文件");
        Thread recordThread = new Thread(new Runnable() {
            @Override
            public void run() {
                startRecord();
            }
        });
        player.prepareAsync();
        recordThread.start();
    }

    //Called when press start button
    public void autoProcess(View view){
        isAuto = true;  //have to put it here
        Thread timer = new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        btn_start.setEnabled(false);
                        btn_stop.setEnabled(true);
                        btn_testRun.setEnabled(false);
                        tv_instr.setText(getResources().getString(R.string.textMeasure_stop));
                    }
                });
                while (isAuto) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            autoProcessFunc();
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
        if (!isAuto) {  // This section only runs after hitting the stop
            btn_start.setEnabled(true);
            btn_stop.setEnabled(false);
            btn_testRun.setEnabled(true);
            tv_instr.setText(this.getString(R.string.textMeasure));
            tv_step.setText(this.getString(R.string.protoWait));
            tv_stopFlag.setText("");
            tv_savedCount.setText(this.getString(R.string.savedWorkoutCount) + "  " + Integer.toString(workoutCnt + 1));
            workoutCnt = -1;
        }
        // This section runs every workout
        player.stop();
        isRecording = false;
        workoutCnt = workoutCnt + 1;
        tv_count.setText(Integer.toString(workoutCnt));
        tv_countDown.setText("");
        pb.setProgress(0);
        stepIndex = 0;
    }

    public void terminateTest(View view){
        if (isAuto) {
            isAuto = false;
            tv_stopFlag.setText(this.getString(R.string.stopFlagMessage));
        } else {
            isAuto = true;
            tv_stopFlag.setText("");
        }
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
        tv_step.setText(this.getString(R.string.protoSteady));
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
        tv_step.setText(this.getString(R.string.protoUp));
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
        tv_step.setText(this.getString(R.string.protoHold));
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
        tv_step.setText(this.getString(R.string.protoDown));
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
        tv_step.setText(this.getString(R.string.protoRelax));
        pb.setMax(sec*10);
        pb.setProgress(0);
        timer1 = new Timer();
        timer2 = new Timer();
        timer3 = new Timer();
        timer1.schedule(new stepBar(), 0, 100);
        timer2.schedule(new stepNumber(),1000, 1000); // update number 1s
        timer3.schedule(new stepNext(),sec*1000+100); // enter next step 5s
    }

    /* Recording Thread */
    public void startRecord(){
        int frequency = 48000;
        int channelConfiguration = AudioFormat.CHANNEL_IN_MONO;
        int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
        if(file.exists()) {
            file.delete();
            Log.i(TAG, "删除文件");
        }
        try{
            file.createNewFile();
            Log.i(TAG,"创建文件");
        }catch(IOException e){
            Log.i(TAG,"未能创建");
            throw new IllegalStateException("Create fails" + file.toString());
        }
        try{
            OutputStream os = new FileOutputStream(file);
            BufferedOutputStream bos = new BufferedOutputStream(os);
            DataOutputStream dos = new DataOutputStream(bos);
            int bufferSize = AudioRecord.getMinBufferSize(frequency, channelConfiguration, audioEncoding);
            AudioRecord audioRecord = new AudioRecord(MediaRecorder.AudioSource.UNPROCESSED, frequency, channelConfiguration, audioEncoding, bufferSize);
            if (AutomaticGainControl.isAvailable()) {
                AutomaticGainControl agc = AutomaticGainControl.create(
                        audioRecord.getAudioSessionId()
                );
                agc.setEnabled(false);
            }

            short[] buffer = new short[bufferSize];
            audioRecord.startRecording();
            Log.i(TAG, "开始录音");
            isRecording = true;
            while(isRecording){
                int bufferReadResult = audioRecord.read(buffer, 0, bufferSize);
                for (int i = 0; i < bufferReadResult; i++){
                    dos.writeShort(buffer[i]);
                }
            }
            audioRecord.stop();
            dos.close();
        }catch (Throwable t){
            Log.e(TAG, "录音失败");
        }
    }
}
