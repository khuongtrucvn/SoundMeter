package com.app.khoaluan.noizy.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.app.khoaluan.noizy.R;
import com.app.khoaluan.noizy.databinding.FragmentHearingTestBinding;
import com.app.khoaluan.noizy.ui.ActivityHearingTest;
import com.app.khoaluan.noizy.ui.ActivityMain;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

public class FragmentHearingTest extends Fragment {
    private ActivityMain activity = (ActivityMain)getActivity();
    private FragmentHearingTestBinding binding;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof AppCompatActivity) {
            this.activity = (ActivityMain) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.activity = null;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_hearing_test, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setEventHandler();
    }

    private void setEventHandler(){
        binding.btnStartTest.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(isHeadphonesPlugged()){
                    startActivity(new Intent(activity, ActivityHearingTest.class));
                }
                else{
                    Toast.makeText(activity, R.string.activity_hearingTestStartErr, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private boolean isHeadphonesPlugged(){
        AudioManager audioManager = (AudioManager)activity.getSystemService(Context.AUDIO_SERVICE);
        AudioDeviceInfo[] audioDevices = audioManager.getDevices(AudioManager.GET_DEVICES_ALL);
        for(AudioDeviceInfo deviceInfo : audioDevices){
            if(deviceInfo.getType()==AudioDeviceInfo.TYPE_WIRED_HEADPHONES
                    || deviceInfo.getType()==AudioDeviceInfo.TYPE_WIRED_HEADSET){
                return true;
            }
        }
        return false;
    }
}