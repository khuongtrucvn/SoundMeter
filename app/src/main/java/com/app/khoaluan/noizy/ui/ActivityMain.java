package com.app.khoaluan.noizy.ui;

import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.app.khoaluan.noizy.R;
import com.app.khoaluan.noizy.databinding.ActivityMainBinding;
import com.app.khoaluan.noizy.model.Global;
import com.app.khoaluan.noizy.model.MyMediaRecorder;
import com.app.khoaluan.noizy.model.MyPrefs;
import com.app.khoaluan.noizy.ui.fragment.FragmentHistory;
import com.app.khoaluan.noizy.ui.fragment.FragmentMeter;
import com.app.khoaluan.noizy.ui.fragment.FragmentSettings;
import com.app.khoaluan.noizy.ui.fragment.FragmentShare;
import com.app.khoaluan.noizy.utils.UtilsActivity;
import com.app.khoaluan.noizy.utils.UtilsFile;
import com.app.khoaluan.noizy.utils.UtilsFragment;
import com.google.android.material.navigation.NavigationView;

import java.io.File;

import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.databinding.DataBindingUtil;

import static com.app.khoaluan.noizy.utils.AppConfig.DELAYING_WARNING_TIME;
import static com.app.khoaluan.noizy.utils.AppConfig.WAITING_TIME;

public class ActivityMain extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    private ActivityMainBinding binding;
    private int currentFragmentId;
    private ActionBarDrawerToggle toggle;

    /* Recorder */
    private MyMediaRecorder mRecorder ;
    private boolean bListener = true;
    private boolean isThreadRun = true;
    public boolean isRecording = true;
    private Thread measureThread, warnThread;
    float volume = 10000;
    private double totalDb = 0; // tổng cộng dB
    private long numberOfDb = 0; // số lần đo độ ồn

    /* Shared Preferences */
    private MyPrefs myPrefs;
    private boolean isWarn, isVibrate, isSound;
    private int warningVal;

    /* Duration */
    public long duration;

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

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int fragmentId = item.getItemId();
        switchFragments(fragmentId);
        return true;
    }

    private void switchFragments(int fragmentId) {
        @IdRes int frameId = R.id.content_frame;

        if (fragmentId != currentFragmentId) {
            switch (fragmentId) {
                case R.id.nav_meter: {
                    binding.toolbar.textTitle.setText(R.string.title_soundmeter);
                    UtilsFragment.replace(this, frameId, new FragmentMeter());
                    break;
                }
                case R.id.nav_history: {
                    binding.toolbar.textTitle.setText(R.string.title_history);
                    UtilsFragment.replace(this, frameId, new FragmentHistory());
                    changeBackgroundColor(R.color.colorPrimary);
                    break;
                }
                case R.id.nav_share: {
                    binding.toolbar.textTitle.setText(R.string.title_share);
                    UtilsFragment.replace(this, frameId, new FragmentShare());
                    break;
                }
                case R.id.nav_settings: {
                    binding.toolbar.textTitle.setText(R.string.title_setting);
                    UtilsFragment.replace(this, frameId, new FragmentSettings());
                    break;
                }
            }
            currentFragmentId = fragmentId;
        }
        binding.drawer.closeDrawer(GravityCompat.START);
    }

    private void initializeComponents() {
        UtilsActivity.enterFullScreen(ActivityMain.this);
        @LayoutRes int layoutId = R.layout.activity_main;
        setContentView(layoutId);
        binding = DataBindingUtil.setContentView(this, layoutId);

        binding.navView.getMenu().getItem(0).setChecked(true);
        switchFragments(R.id.nav_meter);
        setSupportActionBar(binding.toolbar.toolbar);

        toggle = new ActionBarDrawerToggle(this, binding.drawer,R.string.open,R.string.close);

        binding.drawer.addDrawerListener(toggle);
        toggle.syncState();

        loadSharedPreferences();
        initRecorder();
        initdbWarning();
        setEventHandler();
    }

    private void initRecorder(){
        mRecorder = new MyMediaRecorder();
    }

    public void restartRecorder(){
        mRecorder.restartRecording();

        // Chỉnh lại thông số sau khi restart
        totalDb = 0;
        numberOfDb = 1;

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

    private void setEventHandler(){
        binding.toolbar.btnHamburger.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                binding.drawer.openDrawer(binding.navView);
            }
        });

        binding.navView.setNavigationItemSelectedListener(this);
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

    private void startListenAudio() {
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

    public void changeBackgroundColor(int color){
        binding.layoutActivity.setBackgroundColor(ContextCompat.getColor(this, color));
    }

    //Phương thức tải dữ liệu từ Shared Preferences
    public void loadSharedPreferences(){
        myPrefs = new MyPrefs(this);
        isWarn = myPrefs.getIsWarn();
        warningVal = myPrefs.getWarningValue();
        isVibrate = myPrefs.getIsVibrate();
        isSound = myPrefs.getIsSound();
        Global.calibrateValue = myPrefs.getCalibrateValue();
    }

    private void initdbWarning(){
        warnThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (isThreadRun) {
                    try {
                        if(isRecording)
                            warn(Global.lastDb);

                        Thread.sleep(DELAYING_WARNING_TIME);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        warnThread.start();
    }

    private void warn(float curVal){
        if(isWarn){
            if(curVal > warningVal){
                if(isVibrate){
                    Vibrator v = (Vibrator)this.getSystemService(Context.VIBRATOR_SERVICE);
                    if(v != null){
                        // Rung 0.5s
                        if (Build.VERSION.SDK_INT >= 26) {
                            v.vibrate(VibrationEffect.createOneShot(400, VibrationEffect.DEFAULT_AMPLITUDE));
                        } else {
                            v.vibrate(400);
                        }
                    }
                }
                if(isSound){
                    try {
                        Uri path = Uri.parse("android.resource://" + this.getPackageName()+"/raw/warning_tone");
                        RingtoneManager.setActualDefaultRingtoneUri(this, RingtoneManager.TYPE_NOTIFICATION,path);
                        Ringtone r = RingtoneManager.getRingtone(this, path);
                        r.play();
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
