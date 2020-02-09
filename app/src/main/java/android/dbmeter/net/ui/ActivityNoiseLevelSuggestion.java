package android.dbmeter.net.ui;

import android.dbmeter.net.model.MyMediaRecorder;
import android.dbmeter.net.ui.fragment.FragmentNoiseLevelSuggestionResultStep;
import android.dbmeter.net.ui.fragment.FragmentNoiseLevelSuggestionSectionChoosingStep;
import android.os.Bundle;
import android.widget.Toast;

import android.dbmeter.net.R;
import android.dbmeter.net.databinding.ActivityNoiseLevelSuggestionBinding;
import android.dbmeter.net.model.Global;
import android.dbmeter.net.ui.fragment.FragmentNoiseLevelSuggestionBuildingChoosingStep;
import android.dbmeter.net.utils.UtilsActivity;
import android.dbmeter.net.utils.UtilsFragment;

import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import static android.dbmeter.net.utils.AppConfig.WAITING_TIME;

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
