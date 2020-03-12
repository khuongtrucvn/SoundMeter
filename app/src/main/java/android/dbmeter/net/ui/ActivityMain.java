package android.dbmeter.net.ui;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.dbmeter.net.R;
import android.dbmeter.net.databinding.ActivityMainBinding;
import android.dbmeter.net.model.Global;
import android.dbmeter.net.model.MyMediaRecorder;
import android.dbmeter.net.model.MyPrefs;
import android.dbmeter.net.ui.fragment.FragmentCamera;
import android.dbmeter.net.ui.fragment.FragmentHearingTest;
import android.dbmeter.net.ui.fragment.FragmentHistory;
import android.dbmeter.net.ui.fragment.FragmentMeter;
import android.dbmeter.net.ui.fragment.FragmentNoiseLevelSuggestion;
import android.dbmeter.net.ui.fragment.FragmentSettings;
import android.dbmeter.net.utils.UtilsActivity;
import android.dbmeter.net.utils.UtilsFragment;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

import java.util.Locale;

import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.databinding.DataBindingUtil;

import static android.dbmeter.net.utils.AppConfig.DELAYING_WARNING_TIME;
import static android.dbmeter.net.utils.AppConfig.WAITING_TIME;

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
    private int volume;
    private double totalDb = 0; // tổng cộng dB
    private long numberOfDb = 0; // số lần đo độ ồn

    /* Shared Preferences */
    private MyPrefs myPrefs;
    private boolean isWarn, isVibrate, isSound;
    private int warningVal;

    /* Duration */
    public long duration;

    /* Warning */
    private MediaPlayer ringtone;
    private Vibrator v;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeComponents();
    }

    @Override
    public void onResume() {
        super.onResume();

        if(isRecording){
            startMeasure();
            initdbWarning();
        }
        bListener = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        bListener = false;
        mRecorder.stopRecorder();

        measureThread = null;
        warnThread = null;

        //stopWarningRingtone(ringtone);
        //stopVibration(v);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mRecorder.stopRecorder();

        if (measureThread != null) {
            isThreadRun = false;
            measureThread = null;
        }

        if(warnThread != null){
            warnThread = null;
        }

        //stopWarningRingtone(ringtone);
        //stopVibration(v);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int fragmentId = item.getItemId();
        switchFragments(fragmentId);
        return true;
    }

    public void switchFragments(int fragmentId) {
        @IdRes int frameId = R.id.content_frame;
        int backgroundColor = R.drawable.color_background;

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
                    changeBackgroundColor(backgroundColor);
                    break;
                }
                case R.id.nav_hearing_test: {
                    binding.toolbar.textTitle.setText(R.string.title_hearing_test);
                    UtilsFragment.replace(this, frameId, new FragmentHearingTest());
                    changeBackgroundColor(backgroundColor);
                    break;
                }
                case R.id.nav_noise_level_suggest: {
                    binding.toolbar.textTitle.setText(R.string.title_noise_level_suggest);
                    UtilsFragment.replace(this, frameId, new FragmentNoiseLevelSuggestion());
                    changeBackgroundColor(backgroundColor);
                    break;
                }
                case R.id.nav_share: {
                    binding.toolbar.textTitle.setText(R.string.title_share);
                    UtilsFragment.replace(this, frameId, new FragmentCamera());
                    changeBackgroundColor(backgroundColor);
                    break;
                }
                case R.id.nav_settings: {
                    binding.toolbar.textTitle.setText(R.string.title_setting);
                    UtilsFragment.replace(this, frameId, new FragmentSettings());
                    changeBackgroundColor(backgroundColor);
                    break;
                }
            }
            currentFragmentId = fragmentId;
        }
        binding.drawer.closeDrawer(GravityCompat.START);
    }

    private void initializeComponents() {
        loadSharedPreferences();
        setAppLocale();
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

        initRecorder();
        initdbWarning();
        setEventHandler();
    }

    private void initRecorder(){
        mRecorder = new MyMediaRecorder();
    }

    public void startRecorder(){
        isRecording = true;
        bListener = true;
        mRecorder.startRecorder();
    }

    public void stopRecorder(){
        isRecording = false;
        bListener = false;
        mRecorder.stopRecorder();
    }

    public synchronized void restartRecorder(){
        bListener = false;
        mRecorder.restartRecorder();
        bListener = true;

        // Chỉnh lại thông số sau khi restart
        totalDb = 0;
        numberOfDb = 0;

        Global.minDb = 140;
        Global.avgDb = 0;
        Global.maxDb = 0;
    }

    private void setEventHandler(){
        binding.toolbar.btnHamburger.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                binding.drawer.openDrawer(binding.navView);
            }
        });

        binding.navView.setNavigationItemSelectedListener(this);
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
                        if(bListener) {
                            getSoundPowerLevel();
                        }
                        Thread.sleep(WAITING_TIME);
                    }
                    catch (Exception e) {
                        Log.e("TAG","Đã vào đây");
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
        if(volume > 0) {
            float dbCurrent = 20 * (float)(Math.log10(volume));
            Global.setDbCount(dbCurrent);  //Đổi từ áp suất thành độ lớn
            numberOfDb++;
            totalDb += Global.lastDb;
            Global.avgDb = (float)(totalDb/numberOfDb);
        }
    }

    public void changeBackgroundColor(int color){
        binding.layoutActivity.setBackgroundResource(color);
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
                        if(isRecording) {
                            warn(Global.lastDb);
                        }

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

    private synchronized void warn(float curVal){
        if(isWarn){
            if(curVal > warningVal){
                Log.w("WARNING","Warning");
                if(isVibrate){
                    v = (Vibrator)this.getSystemService(Context.VIBRATOR_SERVICE);
                    if(v != null){
                        // Rung 0.4s
                        if (Build.VERSION.SDK_INT >= 26) {
                            v.vibrate(VibrationEffect.createOneShot(400, VibrationEffect.DEFAULT_AMPLITUDE));
                        } else {
                            v.vibrate(400);
                        }
                    }
                }
                if(isSound){
                    ringtone = MediaPlayer.create(this, R.raw.warning_tone);
                    ringtone.start();
                }
            }
        }
    }

    private void setAppLocale(){
        Resources resources = getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        Configuration config = resources.getConfiguration();

        if(!myPrefs.getLocale().equals("")){
            config.setLocale(new Locale(myPrefs.getLocale().toLowerCase()));
            resources.updateConfiguration(config, dm);
        }
    }

    public void restartApplication(){
        startActivity(new Intent(this, ActivityMain.class));
        finishAffinity();
    }

    /*private void stopWarningRingtone(MediaPlayer ringtone) {
        try {
            if (ringtone != null) {
                if (ringtone.isPlaying())
                    ringtone.stop();
                ringtone.release();
                ringtone = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/

    /*private void stopVibration(Vibrator v) {
        if (v != null) {
            v.cancel();
        }
    }*/
}
