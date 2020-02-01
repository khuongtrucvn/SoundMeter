package com.app.khoaluan.noizy.ui;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.app.khoaluan.noizy.R;
import com.app.khoaluan.noizy.databinding.ActivityNoiseLevelSuggestionBinding;
import com.app.khoaluan.noizy.model.Global;
import com.app.khoaluan.noizy.model.MyMediaRecorder;
import com.app.khoaluan.noizy.ui.fragment.FragmentMeter;
import com.app.khoaluan.noizy.ui.fragment.FragmentNoiseLevelSuggestionBuildingChoosingStep;
import com.app.khoaluan.noizy.ui.fragment.FragmentNoiseLevelSuggestionResultStep;
import com.app.khoaluan.noizy.ui.fragment.FragmentNoiseLevelSuggestionSectionChoosingStep;
import com.app.khoaluan.noizy.utils.UtilsActivity;
import com.app.khoaluan.noizy.utils.UtilsFile;
import com.app.khoaluan.noizy.utils.UtilsFragment;

import java.io.File;

import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import static com.app.khoaluan.noizy.utils.AppConfig.WAITING_TIME;

public class ActivityNoiseLevelSuggestion extends AppCompatActivity {
    private ActivityNoiseLevelSuggestionBinding binding;
    private int currentFragmentId;
    private static int buildingIdChosen = -1;
    private static int sectionIdChosen = -1;
    private static boolean isChoiceStartMeasure = false;

    /* Recorder */
    private MyMediaRecorder mRecorder ;
    private boolean bListener = true;
    private boolean isThreadRun = true;
    public boolean isRecording = true;
    private Thread measureThread;
    float volume = 10000;
    private double totalDb = 0; // tổng cộng dB
    private long numberOfDb = 0; // số lần đo độ ồn

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeComponents();
    }

    @Override
    public void onResume() {
        super.onResume();
        File file = UtilsFile.createFile("temp.amr");

        if (file != null)
            startMeasure(file);
        else
            Toast.makeText(this, getString(R.string.activity_recFileErr), Toast.LENGTH_LONG).show();

        bListener = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        bListener = false;
        mRecorder.delete();
        measureThread = null;
    }

    @Override
    public void onDestroy() {
        if (measureThread != null) {
            isThreadRun = false;
            measureThread = null;
        }
        mRecorder.delete();
        super.onDestroy();
    }

    public void switchFragments(int fragmentId) {
        @IdRes int frameId = R.id.content_frame;

        if (fragmentId != currentFragmentId) {
            switch (fragmentId) {
                case 1: {
                    UtilsFragment.replace(this, frameId, new FragmentNoiseLevelSuggestionBuildingChoosingStep());
                    break;
                }
                case 2: {
                    UtilsFragment.replace(this, frameId, new FragmentNoiseLevelSuggestionSectionChoosingStep());
                    break;
                }
                case 3: {
                    UtilsFragment.replace(this, frameId, new FragmentNoiseLevelSuggestionResultStep());
                    break;
                }
            }
            currentFragmentId = fragmentId;
        }
    }

    private void initializeComponents() {
        UtilsActivity.enterFullScreen(ActivityNoiseLevelSuggestion.this);
        @LayoutRes int layoutId = R.layout.activity_noise_level_suggestion;
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

    public void startMeasure(File fFile){
        try{
            mRecorder.setMyRecAudioFile(fFile);
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

    private synchronized void startListenAudio() {
        measureThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (isThreadRun) {
                    try {
                        if(bListener) {
                            numberOfDb++;
                            volume = mRecorder.getMaxAmplitude();  //Lấy áp suất âm thanh
                            if(volume > 0 && volume < 1000000) {
                                Global.setDbCount((20 * (float)(Math.log10(volume))));  //Đổi từ áp suất thành độ lớn
                                totalDb += Global.lastDb;
                                Global.avgDb = (float)(totalDb/numberOfDb);
                            }
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

    public void setBuildingIdChosen(int buildingId){
        buildingIdChosen = buildingId;
    }

    public int getBuildingIdChosen(){
        return buildingIdChosen;
    }

    public void setSectionIdChosen(int sectionId) {
        sectionIdChosen = sectionId;
    }

    public int getSectionIdChosen() {
        return sectionIdChosen;
    }

    public boolean getIsChoiceStartMeasure() {
        return isChoiceStartMeasure;
    }

    public void setIsChoiceStartMeasure(boolean isChoiceStartMeasure) {
        ActivityNoiseLevelSuggestion.isChoiceStartMeasure = isChoiceStartMeasure;
    }
}
