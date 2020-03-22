package android.dbmeter.net.ui;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.dbmeter.net.R;
import android.dbmeter.net.database.LocaleDescriptionDatabase;
import android.dbmeter.net.databinding.ActivityMainBinding;
import android.dbmeter.net.model.Global;
import android.dbmeter.net.model.LocaleDescription;
import android.dbmeter.net.model.MyMediaRecorder;
import android.dbmeter.net.model.MyPrefs;
import android.dbmeter.net.ui.fragment.FragmentCamera;
import android.dbmeter.net.ui.fragment.FragmentHearingTest;
import android.dbmeter.net.ui.fragment.FragmentHistory;
import android.dbmeter.net.ui.fragment.FragmentMeter;
import android.dbmeter.net.ui.fragment.FragmentMusicPlayer;
import android.dbmeter.net.ui.fragment.FragmentNoiseLevelSuggestion;
import android.dbmeter.net.ui.fragment.FragmentSettings;
import android.dbmeter.net.utils.UtilsActivity;
import android.dbmeter.net.utils.UtilsFragment;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.LocaleList;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.Locale;

import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.databinding.DataBindingUtil;

import static android.dbmeter.net.utils.AppConfig.DELAYING_WARNING_TIME;
import static android.dbmeter.net.utils.AppConfig.VIBRATION_TIME;
import static android.dbmeter.net.utils.AppConfig.WAITING_TIME;

public class ActivityMain extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    private ActivityMainBinding binding;
    private int currentFragmentId;
    private ActionBarDrawerToggle toggle;

    /* Recorder */
    private MyMediaRecorder mRecorder ;
    private boolean isThreadRun = true;
    private boolean isPlaying = true;                      //Trạng thái của nút pause/play
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
    private boolean isRingtoneFree = true;
    private CountDownTimer vibratorCountDownCounter;
    private boolean isVibratorFree = true;

    private String localeCode;

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
        initdbWarning();
    }

    @Override
    public void onPause() {
        super.onPause();
        isThreadRun = false;
        mRecorder.stopRecorder();
        measureThread = null;
        warnThread = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isThreadRun = false;
        mRecorder.stopRecorder();

        if (measureThread != null) {
            measureThread = null;
        }

        if(warnThread != null){
            warnThread = null;
        }

        releaseWarningRingtone(ringtone);
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
                case R.id.nav_camera: {
                    binding.toolbar.textTitle.setText(R.string.title_share);
                    UtilsFragment.replace(this, frameId, new FragmentCamera());
                    changeBackgroundColor(backgroundColor);
                    break;
                }
                case R.id.nav_focusing_music: {
                    binding.toolbar.textTitle.setText(R.string.title_focusing_music);
                    UtilsFragment.replace(this, frameId, new FragmentMusicPlayer());
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

        //Cài đặt ngôn ngữ cho ứng dụng
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
        localeCode =  myPrefs.getLocale();
    }

    private void initdbWarning(){
        v = (Vibrator)this.getSystemService(Context.VIBRATOR_SERVICE);
        vibratorCountDownCounter = new CountDownTimer(1000,400) {
            @Override
            public void onTick(long l) { }

            @Override
            public void onFinish() {
                isVibratorFree = true;
            }
        };

        //Khi ringtone chạy hết mới mở tiếp
        ringtone = MediaPlayer.create(this, R.raw.warning_tone);
        ringtone.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                isRingtoneFree = true;
            }
        });

        warnThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (isThreadRun) {
                    try {
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

    private synchronized void warn(float curVal){
        //Nếu recorder đang chạy
        if(isPlaying){
            if(isWarn){
                if(curVal > warningVal){
                    if(isVibrate){
                        if(v.hasVibrator()){
                            if(isVibratorFree){
                                if (Build.VERSION.SDK_INT >= 26) {
                                    v.vibrate(VibrationEffect.createOneShot(VIBRATION_TIME, VibrationEffect.DEFAULT_AMPLITUDE));
                                } else {
                                    v.vibrate(VIBRATION_TIME);
                                }
                                isVibratorFree = false;
                                vibratorCountDownCounter.start();
                            }
                        }
                    }

                    if(isSound){
                        if(isRingtoneFree){
                            ringtone.start();
                            isRingtoneFree = false;
                        }
                    }
                }
            }
        }
    }

    private void setAppLocale(){
        Resources resources = getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        Configuration config = resources.getConfiguration();
        config.setLocale(new Locale(localeCode));
        resources.updateConfiguration(config, dm);
    }

    public void restartApplication(){
        startActivity(new Intent(this, ActivityMain.class));
        finishAffinity();
    }

    private void releaseWarningRingtone(MediaPlayer ringtone) {
        try {
            if (ringtone != null) {
                if (ringtone.isPlaying()){
                    ringtone.stop();
                }
                ringtone.release();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean getIsPlayingStatus(){
        return isPlaying;
    }
}
