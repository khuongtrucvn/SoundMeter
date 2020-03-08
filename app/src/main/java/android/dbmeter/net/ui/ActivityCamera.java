package android.dbmeter.net.ui;

import android.content.pm.ActivityInfo;
import android.dbmeter.net.R;
import android.dbmeter.net.databinding.ActivityCameraBinding;
import android.dbmeter.net.model.Global;
import android.dbmeter.net.model.MyMediaRecorder;
import android.dbmeter.net.ui.fragment.FragmentCameraImageCapture;
import android.dbmeter.net.ui.fragment.FragmentCameraImagePreview;
import android.dbmeter.net.utils.UtilsActivity;
import android.dbmeter.net.utils.UtilsFragment;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import static android.dbmeter.net.utils.AppConfig.WAITING_TIME;

public class ActivityCamera extends AppCompatActivity {
    private ActivityCameraBinding binding;
    private int currentFragmentId;

    /* Recorder */
    private MyMediaRecorder mRecorder ;
    private boolean bListener = true;
    private boolean isThreadRun = true;
    public boolean isRecording = true;
    private Thread measureThread;
    float volume = 10000;
    private double totalDb = 0; // tổng cộng dB
    private long numberOfDb = 0; // số lần đo độ ồn

    private String imagePath = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeComponents();
    }

    @Override
    public void onResume() {
        super.onResume();

        startMeasure();
        bListener = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        bListener = false;
        mRecorder.stopRecording();
        measureThread = null;
    }

    @Override
    public void onDestroy() {
        if (measureThread != null) {
            isThreadRun = false;
            measureThread = null;
        }
        mRecorder.stopRecording();
        super.onDestroy();
    }

    public void switchFragments(int fragmentId) {
        @IdRes int frameId = R.id.content_frame;

        if (fragmentId != currentFragmentId) {
            switch (fragmentId) {
                case 1: {
                    UtilsFragment.replace(this, frameId, new FragmentCameraImageCapture());
                    break;
                }
                case 2: {
                    UtilsFragment.replace(this, frameId, new FragmentCameraImagePreview());
                    break;
                }
            }
            currentFragmentId = fragmentId;
        }
    }

    private void initializeComponents() {
        UtilsActivity.enterFullScreen(ActivityCamera.this);
        @LayoutRes int layoutId = R.layout.activity_camera;
        setContentView(layoutId);
        binding = DataBindingUtil.setContentView(this, layoutId);

        switchFragments(1);
        initRecorder();
    }

    private void initRecorder(){
        mRecorder = new MyMediaRecorder();
    }

    public void restartRecorder(){
        mRecorder.restartRecording();

        // Chỉnh lại thông số sau khi restart
        totalDb = 0;
        numberOfDb = 0;

        Global.minDb = 140;
        Global.avgDb = 0;
        Global.maxDb = 0;
    }

    public void resumeRecorder(){
        mRecorder.resumeRecording();
        isRecording = true;
    }

    public void pauseRecorder(){
        mRecorder.pauseRecording();
        isRecording = false;
    }

    public void startMeasure(){
        try{
            if (mRecorder.startRecorder()) {
                startListenAudio();
            }
            else{
                Toast.makeText(this, getString(R.string.activity_recStartErr), Toast.LENGTH_SHORT).show();
            }
        }
        catch(Exception e){
            Toast.makeText(this, getString(R.string.activity_recBusyErr), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void startListenAudio() {
        measureThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (isThreadRun) {
                    try {
                        if(bListener) {
                            getSoundPowerLevel();
                        }
                        Thread.sleep(WAITING_TIME);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                        bListener = false;
                    }
                }
            }
        });
        measureThread.start();
    }

    private synchronized void getSoundPowerLevel(){
        volume = mRecorder.getMaxAmplitude();  //Lấy áp suất âm thanh
        //Log.e("Thread", "Thread : " + threadId + ", volume: " + volume);
        if(volume > 0) {
            float dbCurrent = 20 * (float)(Math.log10(volume));
            Global.setDbCount(dbCurrent);  //Đổi từ áp suất thành độ lớn
            numberOfDb++;
            totalDb += Global.lastDb;
            Global.avgDb = (float)(totalDb/numberOfDb);
            //Log.e("Measureeeeee", "Thread : " + threadId + ", volume: " + volume + ", times: " + numberOfDb + ", average: " + Global.avgDb +", min: " + Global.minDb);
        }
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
}