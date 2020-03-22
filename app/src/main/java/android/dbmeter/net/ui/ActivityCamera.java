package android.dbmeter.net.ui;

import android.dbmeter.net.R;
import android.dbmeter.net.databinding.ActivityCameraBinding;
import android.dbmeter.net.model.Global;
import android.dbmeter.net.model.MyMediaRecorder;
import android.dbmeter.net.ui.fragment.FragmentCameraImageCapture;
import android.dbmeter.net.ui.fragment.FragmentCameraImagePreview;
import android.dbmeter.net.utils.UtilsActivity;
import android.dbmeter.net.utils.UtilsFragment;
import android.os.Bundle;
import android.util.Log;
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
    private boolean isThreadRun = true;
    public boolean isPlaying = true;
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
        isThreadRun = true;
        startMeasure();
    }

    @Override
    public void onPause() {
        super.onPause();
        isThreadRun = false;
        mRecorder.stopRecorder();
        measureThread = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isThreadRun = false;
        mRecorder.stopRecorder();
        if (measureThread != null) {
            measureThread = null;
        }
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

    public void startRecorder(){
        isPlaying = true;
        mRecorder.startRecorder();
    }

    public void stopRecorder(){
        isPlaying = false;
        mRecorder.stopRecorder();
    }

    public void restartRecorder(){
        // Chỉnh lại thông số sau khi restart
        totalDb = 0;
        numberOfDb = 0;

        Global.minDb = 140;
        Global.avgDb = 0;
        Global.maxDb = 0;
    }

    private void startMeasure(){
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
                        getSoundPowerLevel();
                        Thread.sleep(WAITING_TIME);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                        isPlaying = false;
                    }
                }
            }
        });
        measureThread.start();
    }

    private synchronized void getSoundPowerLevel(){
        if(isPlaying) {
            volume = mRecorder.getMaxAmplitude();  //Lấy áp suất âm thanh

            //Kiểm tra nếu số lần đo vượt quá 1 triệu lần thì reset
            if(numberOfDb > 1000000){
                numberOfDb = 1000;
                totalDb = Global.avgDb*1000;
            }

            if(volume > 0) {
                //Đổi từ áp suất thành độ lớn
                float dbCurrent = 20 * (float)(Math.log10(volume));
                Global.setDbCount(dbCurrent);
                numberOfDb++;
                totalDb += Global.lastDb;
                Global.avgDb = (float)(totalDb/numberOfDb);
            }
        }
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
}
