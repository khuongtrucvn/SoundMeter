package android.dbmeter.net.ui;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.dbmeter.net.ui.fragment.FragmentHearingTestCalibrationStep;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.Toast;

import android.dbmeter.net.R;
import android.dbmeter.net.databinding.ActivityHearingTestBinding;

import android.dbmeter.net.ui.fragment.FragmentHearingTestResultStep;
import android.dbmeter.net.ui.fragment.FragmentHearingTestTestingStep;
import android.dbmeter.net.utils.UtilsActivity;
import android.dbmeter.net.utils.UtilsFragment;

import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

public class ActivityHearingTest extends AppCompatActivity {
    private ActivityHearingTestBinding binding;
    private int currentFragmentId;

    private boolean isDialogRun = false;
    private AudioManager audioManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeComponents();
    }

    private void initializeComponents() {
        UtilsActivity.enterFullScreen(ActivityHearingTest.this);
        @LayoutRes int layoutId = R.layout.activity_hearing_test;
        setContentView(layoutId);
        binding = DataBindingUtil.setContentView(this, layoutId);

        audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);

        switchFragments(1);

        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    if(!isHeadphonesPlugged() && !isDialogRun){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                remindHeadphonesPlugged();
                            }
                        });
                        isDialogRun = true;
                    }
                }
            }
        }).start();
    }

    public void switchFragments(int fragmentId){
        @IdRes int frameId = R.id.content_frame;

        if (fragmentId != currentFragmentId) {
            switch (fragmentId) {
                case 1: {
                    UtilsFragment.replace(this, frameId, new FragmentHearingTestCalibrationStep());
                    break;
                }
                case 2: {
                    UtilsFragment.replace(this, frameId, new FragmentHearingTestTestingStep());
                    break;
                }
                case 3: {
                    UtilsFragment.replace(this, frameId, new FragmentHearingTestResultStep());
                    break;
                }
            }
            currentFragmentId = fragmentId;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(currentFragmentId == 1){
            switch (keyCode) {
                case KeyEvent.KEYCODE_VOLUME_UP: {
                    audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                            AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
                    return true;
                }
                case KeyEvent.KEYCODE_VOLUME_DOWN: {
                    audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                            AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
                    return true;
                }
                default:
                    return super.onKeyDown(keyCode, event);
            }
        }
        else {
            return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if(currentFragmentId == 2){
            if (event.getKeyCode() != KeyEvent.KEYCODE_VOLUME_UP && event.getKeyCode() != KeyEvent.KEYCODE_VOLUME_DOWN){
                super.dispatchKeyEvent(event);
            }
            else{
                Toast.makeText(this,R.string.activity_test_volume_denied,Toast.LENGTH_SHORT).show();
            }
        }
        else
            super.dispatchKeyEvent(event);

        return true;
    }

    private boolean isHeadphonesPlugged(){
        AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        AudioDeviceInfo[] audioDevices = audioManager.getDevices(AudioManager.GET_DEVICES_ALL);
        for(AudioDeviceInfo deviceInfo : audioDevices){
            if(deviceInfo.getType()==AudioDeviceInfo.TYPE_WIRED_HEADPHONES
                    || deviceInfo.getType()==AudioDeviceInfo.TYPE_WIRED_HEADSET){
                return true;
            }
        }

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter != null && BluetoothProfile.STATE_CONNECTED == bluetoothAdapter.getProfileConnectionState(BluetoothProfile.HEADSET)){
            return true;
        }

        return false;
    }

    private void remindHeadphonesPlugged(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.title_headphone_disconnect);
        builder.setMessage(R.string.activity_test_headphone_remind);
        builder.setCancelable(false);
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();

        new Thread(new Runnable() {
            @Override
            public void run() {
                while(isDialogRun){
                    if(isHeadphonesPlugged()){
                        alertDialog.dismiss();
                        isDialogRun = false;
                    }
                }
            }
        }).start();
    }
}