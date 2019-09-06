package pitt.edu.danious.myotester;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.view.KeyEvent;
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

import javax.net.ssl.HandshakeCompletedListener;

public class WorkoutActivity extends AppCompatActivity {

    MediaPlayer player = new MediaPlayer();
//    MediaPlayer voicePlayer = new MediaPlayer();
    MediaPlayer preCountDownPlayer = new MediaPlayer();
    MediaPlayer steadyPlayer = new MediaPlayer();
    MediaPlayer putUpPlayer = new MediaPlayer();
    MediaPlayer holdPlayer = new MediaPlayer();
    MediaPlayer putDownPlayer = new MediaPlayer();
    MediaPlayer relaxPlayer = new MediaPlayer();
    private Timer timer1, timer2, timer3, voiceTimer, preVoiceTimer;
    private File file;
    // UI controls
    private ProgressBar pb;
    private TextView tv_instr, tv_count, tv_step, tv_countDown, tv_stopFlag, tv_savedCount;
    private Button btn_start, btn_stop, btn_testRun;
    // Variables
    private int sec = 0;
    private double secVoice = 2;
    private int stepIndex = 0;
    private boolean isAuto = false;
    private boolean isSingle = false;
    private boolean isRecording = false;
    private int workoutCnt = 0;
    public static final String TAG = "Record Thread";
    public static final int PROCESS_TIME = 26;      // Protocol time + 5 * 2 (Voice Instruction Waiting Time) + 1
    public static final int PRECOUNTDOWN = 8;
    public static final int PROCESS_TIME_1ST = PROCESS_TIME + PRECOUNTDOWN;
    private String folderName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout);
        //Get save folder name
        Intent setupIntent = getIntent();
        folderName = setupIntent.getStringExtra("extra_data");

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

        //Make the volume maximum
        AudioManager amr = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int maxVoiceCall = amr.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL);
        amr.setStreamVolume(AudioManager.STREAM_VOICE_CALL, maxVoiceCall, 0);
        int maxMusic = amr.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        amr.setStreamVolume(AudioManager.STREAM_MUSIC, maxMusic, 0);

        //Initialize media player
        try {
            player.reset();
            Uri setDataSourceUri = Uri.parse("android.resource://pitt.edu.danious.myotester/" + R.raw.sequence_bpsk_4);
            player.setDataSource(this, setDataSourceUri);
            player.setAudioStreamType(AudioManager.STREAM_VOICE_CALL);
            player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
//                    player.setVolume(1.0f, 1.0f);
                    player.start();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Initialize all voice instruction players
        initVoicePlayers();
    }

    //Replace the functionality of volume up and down button
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int action = event.getAction();
        int keyCode = event.getKeyCode();
        switch (keyCode){
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (action == KeyEvent.ACTION_DOWN){
                    if (btn_start.isEnabled()) {
                        btn_start.performClick();   //need to check whether it's enabled
                    }
                }
                return true;
                case KeyEvent.KEYCODE_VOLUME_DOWN:
                    if (action == KeyEvent.ACTION_DOWN){
                        if (btn_stop.isEnabled()) {
                            btn_stop.performClick();
                        }
                    }
                    return true;
                    default:
                        return super.dispatchKeyEvent(event);
        }
    }

    //region Use prepareAsync for instruction voices
    public void initVoicePlayers(){
        try {
            preCountDownPlayer.reset();
            Uri setDataSourceUri = Uri.parse("android.resource://pitt.edu.danious.myotester/" + R.raw.countdown_voice);
            preCountDownPlayer.setDataSource(this, setDataSourceUri);
            preCountDownPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            preCountDownPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    preCountDownPlayer.start();
                }
            });
            steadyPlayer.reset();
            setDataSourceUri = Uri.parse("android.resource://pitt.edu.danious.myotester/" + R.raw.steady_voice);
            steadyPlayer.setDataSource(this, setDataSourceUri);
            steadyPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            steadyPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
//                    if (preCountDownPlayer.isPlaying()) {
//                        preCountDownPlayer.stop();
//                    }
//                    steadyPlayer.setVolume(1.0f, 1.0f);
                    steadyPlayer.start();
                }
            });
            putUpPlayer.reset();
            setDataSourceUri = Uri.parse("android.resource://pitt.edu.danious.myotester/" + R.raw.putup_voice);
            putUpPlayer.setDataSource(this, setDataSourceUri);
            putUpPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            putUpPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    if (player.isPlaying() || !isSingle) {          //Stop the ultrasonic player after the instruction player is ready
                        player.stop();
                    }
//                    putUpPlayer.setVolume(1.0f, 1.0f);
                    putUpPlayer.start();
                }
            });
            holdPlayer.reset();
            setDataSourceUri = Uri.parse("android.resource://pitt.edu.danious.myotester/" + R.raw.hold_voice);
            holdPlayer.setDataSource(this, setDataSourceUri);
            holdPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            holdPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    if (player.isPlaying() || !isSingle) {
                        player.stop();
                    }
//                    holdPlayer.setVolume(1.0f, 1.0f);
                    holdPlayer.start();
                }
            });
            putDownPlayer.reset();
            setDataSourceUri = Uri.parse("android.resource://pitt.edu.danious.myotester/" + R.raw.putdown_voice);
            putDownPlayer.setDataSource(this, setDataSourceUri);
            putDownPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            putDownPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    if (player.isPlaying() || !isSingle) {
                        player.stop();
                    }
//                    putDownPlayer.setVolume(1.0f, 1.0f);
                    putDownPlayer.start();
                }
            });
            relaxPlayer.reset();
            setDataSourceUri = Uri.parse("android.resource://pitt.edu.danious.myotester/" + R.raw.relax_voice);
            relaxPlayer.setDataSource(this, setDataSourceUri);
            relaxPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            relaxPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    if (player.isPlaying() || !isSingle) {
                        player.stop();
                    }
//                    relaxPlayer.setVolume(1.0f, 1.0f);
                    relaxPlayer.start();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //endregion

    //Called when press test-run button
    public void singleProcess(View view){
        btn_start.setEnabled(false);
        btn_stop.setEnabled(false);
        btn_testRun.setEnabled(false);
        tv_instr.setText(this.getString(R.string.testRunMessage));
        isSingle = true;
        isAuto = false;
//        steadyStep();
        stepIndex = 1;
        steadyVoice();
    }

    public void autoProcessFunc(){
//        playAndRecord();
//        steadyStep();
//        steadyVoice();
        if (workoutCnt == 0) {  //if this is doing the first workout
            preCountDownVoice();
        } else {
            steadyVoice();
        }
    }

    public void playAndRecord(String stepFolderName){
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                        + "/DriveSyncFiles/" + folderName + "/" + stepFolderName
                        + "/" + String.valueOf(workoutCnt + 1) + "_musclePCM_" + dateFormat.format(date)
                        + ".pcm");
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
        isSingle = false;
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
                        if (workoutCnt == 0) {  //if this is doing the first workout
                            Thread.sleep(PROCESS_TIME_1ST*1000);
                        } else {
                            Thread.sleep(PROCESS_TIME*1000);
                        }
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
            btn_stop.setText(this.getString(R.string.measureStopButton));
            if (!isSingle) {
                tv_savedCount.setText(this.getString(R.string.savedWorkoutCount) +
                        "  " + Integer.toString(workoutCnt + 1));
                // Finish a group, proceed to the results
                Intent intent = new Intent(this, ResultsActivity.class);
                intent.putExtra("extra_data", folderName.substring(folderName.length() - 1));
                intent.putExtra("workoutCnt", Integer.toString(workoutCnt + 1));
                startActivity(intent);
                finish();
            }
            else {
                tv_savedCount.setText("");
            }
            workoutCnt = -1;
        }
        // This section runs every workout
        if (!isSingle) {
            if (player.isPlaying()) {
                player.stop();
            }
        }
        relaxPlayer.stop();
        isRecording = false;
        workoutCnt = workoutCnt + 1;
        tv_count.setText(Integer.toString(workoutCnt));
        tv_countDown.setText("");
        pb.setProgress(0);
        if(!isSingle)
            stepIndex = 1;
        else
            stepIndex = 0;
    }

    public void terminateTest(View view){
        if (isAuto) {
            isAuto = false;
            tv_stopFlag.setText(this.getString(R.string.stopFlagMessage));
            btn_stop.setText(this.getString(R.string.measureResumeButton));
        } else {
            isAuto = true;
            tv_stopFlag.setText("");
            btn_stop.setText(this.getString(R.string.measureStopButton));
        }
    }

    private void countDown(){
        sec = sec - 1;
        tv_countDown.setText(Integer.toString(sec) + "s");
    }

    //Step voices handler
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
                    switch (msg.arg1){
                        case 1:
                            preVoiceTimer.cancel();
                            steadyVoice();
                            break;
                        case 2:
                            voiceTimer.cancel();
                            steadyStep();
                            break;
                        case 3:
                            progressBarTimerCancel();
                            putUpVoice();
                            break;
                        case 4:
                            voiceTimer.cancel();
                            putUpStep();
                            break;
                        case 5:
                            progressBarTimerCancel();
                            holdVoice();
                            break;
                        case 6:
                            voiceTimer.cancel();
                            holdStep();
                            break;
                        case 7:
                            progressBarTimerCancel();
                            putDownVoice();
                            break;
                        case 8:
                            voiceTimer.cancel();
                            putDownStep();
                            break;
                        case 9:
                            progressBarTimerCancel();
                            relaxVoice();
                            break;
                        case 10:
                            voiceTimer.cancel();
                            relaxStep();
                            break;
                        case 11:
                            progressBarTimerCancel();
                            endProcess();
                            break;
                    }
                    break;
            }
        }
    };

    // Step Timer Tasks
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

    private void progressBarTimer(int barSec){
        timer1 = new Timer();
        timer2 = new Timer();
        timer3 = new Timer();
        timer1.schedule(new stepBar(), 0, 100);
        timer2.schedule(new stepNumber(),1000, 1000); // update number 1s
        timer3.schedule(new stepNext(),sec*1000+100); // enter next step
    }

    private void progressBarTimerCancel(){
        timer1.cancel();
        timer2.cancel();
        timer3.cancel();
    }

    /* Protocol steps */
    //
    private void preCountDownVoice(){
        preCountDownPlayer.prepareAsync();
        preVoiceTimer = new Timer();
        preVoiceTimer.schedule(new stepNext(), PRECOUNTDOWN*1000);
    }

    // Step 1
    private void steadyVoice(){
//        preCountDownPlayer.stop();      //if user return from results page, a bug may happen here
        sec = 3;
        steadyPlayer.prepareAsync();
        tv_countDown.setText(Integer.toString(sec) + "s");
        tv_step.setText(this.getString(R.string.protoSteady));
        pb.setMax(sec*10);
        pb.setProgress(0);
        voiceTimer = new Timer();
        voiceTimer.schedule(new stepNext(), (int)secVoice*1000);
    }

    // Step 2
    private void steadyStep(){
//        voicePlayer = MediaPlayer.create(this, R.raw.steady_voice);
//        voicePlayer.start();
//        sec = 3;
//        tv_countDown.setText(Integer.toString(sec) + "s");
//        tv_step.setText(this.getString(R.string.protoSteady));
//        pb.setMax(sec*10);
//        pb.setProgress(0);
        steadyPlayer.stop();
        if (!isSingle) {
            playAndRecord("/1Steady");
        }
        progressBarTimer(sec);
//        steadyPlayer.prepareAsync();
    }

    // Step 3
    private void putUpVoice(){
//        if (player.isPlaying() || !isSingle) {
//            player.stop();
//        }
        isRecording = false;
        sec = 2;
        putUpPlayer.prepareAsync();
        tv_countDown.setText(Integer.toString(sec) + "s");
        tv_step.setText(this.getString(R.string.protoUp));
        pb.setMax(sec*10);
        pb.setProgress(0);
        voiceTimer = new Timer();
        voiceTimer.schedule(new stepNext(), (int)secVoice*1000);
    }

    // Step 4
    private void putUpStep(){
//        voicePlayer = MediaPlayer.create(this, R.raw.putup_voice);
//        voicePlayer.start();
//        sec = 2;
//        tv_countDown.setText(Integer.toString(sec) + "s");
//        tv_step.setText(this.getString(R.string.protoUp));
//        pb.setMax(sec*10);
//        pb.setProgress(0);
        putUpPlayer.stop();
        if (!isSingle) {
            playAndRecord("/2PutUp");
        }
        progressBarTimer(sec);
//        putUpPlayer.prepareAsync();
    }

    // Step 5
    private void holdVoice(){
//        if (player.isPlaying() || !isSingle) {
//            player.stop();
//        }
        isRecording = false;
        sec = 3;
        holdPlayer.prepareAsync();
        tv_countDown.setText(Integer.toString(sec) + "s");
        tv_step.setText(this.getString(R.string.protoHold));
        pb.setMax(sec*10);
        pb.setProgress(0);
        voiceTimer = new Timer();
        voiceTimer.schedule(new stepNext(), (int)secVoice*1000);
    }

    // Step 6
    private void holdStep(){
//        voicePlayer = MediaPlayer.create(this, R.raw.hold_voice);
//        voicePlayer.start();
//        sec = 3;
//        tv_countDown.setText(Integer.toString(sec) + "s");
//        tv_step.setText(this.getString(R.string.protoHold));
//        pb.setMax(sec*10);
//        pb.setProgress(0);
        holdPlayer.stop();
        if (!isSingle) {
            playAndRecord("/3Hold");
        }
        progressBarTimer(sec);
//        holdPlayer.prepareAsync();
    }

    // Step 7
    private void putDownVoice(){
//        if (player.isPlaying() || !isSingle) {
//            player.stop();
//        }
        isRecording = false;
        sec = 2;
        putDownPlayer.prepareAsync();
        tv_countDown.setText(Integer.toString(sec) + "s");
        tv_step.setText(this.getString(R.string.protoDown));
        pb.setMax(sec*10);
        pb.setProgress(0);
        voiceTimer = new Timer();
        voiceTimer.schedule(new stepNext(), (int)secVoice*1000);
    }

    // Step 8
    private void putDownStep(){
//        voicePlayer = MediaPlayer.create(this, R.raw.putdown_voice);
//        voicePlayer.start();
//        sec = 2;
//        tv_countDown.setText(Integer.toString(sec) + "s");
//        tv_step.setText(this.getString(R.string.protoDown));
//        pb.setMax(sec*10);
//        pb.setProgress(0);
        putDownPlayer.stop();
        if (!isSingle) {
            playAndRecord("/4PutDown");
        }
        progressBarTimer(sec);
//        putDownPlayer.prepareAsync();
    }

    // Step 9
    private void relaxVoice(){
//        if (player.isPlaying() || !isSingle) {
//            player.stop();
//        }
        isRecording = false;
        sec = 5;
        relaxPlayer.prepareAsync();
        tv_countDown.setText(Integer.toString(sec) + "s");
        tv_step.setText(this.getString(R.string.protoRelax));
        pb.setMax(sec*10);
        pb.setProgress(0);
        voiceTimer = new Timer();
        voiceTimer.schedule(new stepNext(), (int)secVoice*1000);
    }

    private void relaxStep(){
//        voicePlayer = MediaPlayer.create(this, R.raw.relax_voice);
//        voicePlayer.start();
//        sec = 5;
//        tv_countDown.setText(Integer.toString(sec) + "s");
//        tv_step.setText(this.getString(R.string.protoRelax));
//        pb.setMax(sec*10);
//        pb.setProgress(0);
            relaxPlayer.stop();
        if (!isSingle) {
            playAndRecord("/5Relax");
        }
        progressBarTimer(sec);
//        relaxPlayer.prepareAsync();
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
