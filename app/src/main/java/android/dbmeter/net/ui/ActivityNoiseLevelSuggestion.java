package android.dbmeter.net.ui;

import android.content.DialogInterface;
import android.dbmeter.net.model.BuildingSectionStandard;
import android.dbmeter.net.model.BuildingStandard;
import android.dbmeter.net.model.MyMediaRecorder;
import android.dbmeter.net.ui.fragment.FragmentNoiseLevelSuggestionResultStep;
import android.dbmeter.net.ui.fragment.FragmentNoiseLevelSuggestionSectionChoosingStep;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import android.dbmeter.net.R;
import android.dbmeter.net.databinding.ActivityNoiseLevelSuggestionBinding;
import android.dbmeter.net.model.Global;
import android.dbmeter.net.ui.fragment.FragmentNoiseLevelSuggestionBuildingChoosingStep;
import android.dbmeter.net.utils.UtilsActivity;
import android.dbmeter.net.utils.UtilsFragment;

import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import static android.dbmeter.net.utils.AppConfig.WAITING_TIME;

public class ActivityNoiseLevelSuggestion extends AppCompatActivity {
    private ActivityNoiseLevelSuggestionBinding binding;
    private int currentFragmentId;
    private static BuildingStandard chosenBuilding;
    private static BuildingSectionStandard chosenSection;
    private static boolean isChoiceStartMeasure = false;

    /* Recorder */
    private MyMediaRecorder mRecorder ;
    private boolean isThreadRun = true;
    public boolean isPlaying = true;
    private Thread measureThread;
    float volume = 10000;
    private double totalDb = 0; // tổng cộng dB
    private long numberOfDb = 0; // số lần đo độ ồn

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeComponents();
        setEventHandler();
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
                    binding.toolbar.textTitle.setText(R.string.title_buildings);
                    UtilsFragment.replace(this, frameId, new FragmentNoiseLevelSuggestionBuildingChoosingStep());
                    break;
                }
                case 2: {
                    binding.toolbar.textTitle.setText(chosenBuilding.getBuildingName());
                    UtilsFragment.replace(this, frameId, new FragmentNoiseLevelSuggestionSectionChoosingStep());
                    break;
                }
                case 3: {
                    binding.toolbar.textTitle.setText(R.string.title_result);
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
        setSupportActionBar(binding.toolbar.toolbar);

        switchFragments(1);
        initRecorder();
    }

    private void setEventHandler(){
        binding.toolbar.btnBack.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                handleBackButton();
            }
        });
    }

    private void handleBackButton(){
        if(currentFragmentId == 1){
            exitNoiseLevelSuggestion();
        }
        else if(currentFragmentId == 2){
            switchFragments(1);
        }
        else if(currentFragmentId == 3){
            switchFragments(2);
        }
    }

    public void exitNoiseLevelSuggestion(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.title_exit);
        builder.setMessage(R.string.activity_exit_noiseSuggest);
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.title_exit, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });
        builder.setNegativeButton(R.string.activity_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            handleBackButton();

            return true;
        }

        return super.onKeyDown(keyCode, event);
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

    public BuildingStandard getChosenBuilding() {
        return chosenBuilding;
    }

    public void setChosenBuilding(BuildingStandard chosenBuilding) {
        ActivityNoiseLevelSuggestion.chosenBuilding = chosenBuilding;
    }

    public BuildingSectionStandard getChosenSection() {
        return chosenSection;
    }

    public void setChosenSection(BuildingSectionStandard chosenSection) {
        ActivityNoiseLevelSuggestion.chosenSection = chosenSection;
    }

    public boolean getIsChoiceStartMeasure() {
        return isChoiceStartMeasure;
    }

    public void setIsChoiceStartMeasure(boolean isChoiceStartMeasure) {
        ActivityNoiseLevelSuggestion.isChoiceStartMeasure = isChoiceStartMeasure;
    }
}
