package com.app.khoaluan.noizy.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.app.khoaluan.noizy.R;
import com.app.khoaluan.noizy.databinding.DialogWarningBinding;
import com.app.khoaluan.noizy.databinding.FragmentSettingsBinding;
import com.app.khoaluan.noizy.model.MyPrefs;
import com.app.khoaluan.noizy.ui.ActivityMain;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

public class FragmentSettings extends Fragment {
    private ActivityMain activity = (ActivityMain)getActivity();
    private FragmentSettingsBinding binding;
    private DialogWarningBinding warningBinding;

    /* Shared Preferences */
    private MyPrefs myPrefs;
    private boolean isWarn, isVibrate, isSound;
    private int warningVal;

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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_settings, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeComponents();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setEventHandler();
    }

    private void initializeComponents(){
        loadSharedPreferences();

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                binding.chkWarn.setChecked(isWarn);
                turndbWarning();
                binding.textWarnValue.setText(Integer.toString(warningVal));
                binding.swtVibrate.setChecked(isVibrate);
                binding.swtSound.setChecked(isSound);
            }
        });
    }

    //Phương thức tải dữ liệu từ Shared Preferences
    private void loadSharedPreferences(){
        myPrefs = new MyPrefs(activity);
        isWarn = myPrefs.getIsWarn();
        warningVal = myPrefs.getWarningValue();
        isVibrate = myPrefs.getIsVibrate();
        isSound = myPrefs.getIsSound();
    }

    private void setEventHandler(){
        //Do warning là view nên cần setChecked
        binding.layoutWarning.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                isWarn = !checkdbWarning();
                turndbWarning();
                turnVibrateAuto();
                binding.chkWarn.setChecked(isWarn);
                myPrefs.setIsWarn(isWarn);
                activity.loadSharedPreferences();
            }
        });

        binding.chkWarn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                isWarn = checkdbWarning();
                turndbWarning();
                turnVibrateAuto();
                myPrefs.setIsWarn(isWarn);
                activity.loadSharedPreferences();
            }
        });

        binding.layoutWarningValue.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showWarningValueDialog();
            }
        });

        //Do vibrate là view nên cần setChecked
        binding.layoutVibrate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                isVibrate = !checkVibrateOption();
                binding.swtVibrate.setChecked(isVibrate);
                myPrefs.setIsVibrate(isVibrate);
                activity.loadSharedPreferences();
            }
        });

        binding.swtVibrate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isVibrate = checkVibrateOption();
                myPrefs.setIsVibrate(isVibrate);
                activity.loadSharedPreferences();
            }
        });

        //Do sound là view nên cần setChecked
        binding.layoutSound.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                isSound = !checkSoundOption();
                binding.swtSound.setChecked(isSound);
                myPrefs.setIsSound(isSound);
                activity.loadSharedPreferences();
            }
        });

        binding.swtSound.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isSound = checkSoundOption();
                myPrefs.setIsSound(isSound);
                activity.loadSharedPreferences();
            }
        });
    }

    private boolean checkdbWarning(){
        return binding.chkWarn.isChecked();
    }

    private boolean checkVibrateOption(){
        return binding.swtVibrate.isChecked();
    }

    private boolean checkSoundOption(){
        return binding.swtSound.isChecked();
    }

    private void turndbWarning(){
        if(!isWarn){                        //nếu checkbox được không được chọn
            binding.layoutWarningValue.setVisibility(View.GONE);
            binding.layoutSound.setVisibility(View.GONE);
            binding.layoutVibrate.setVisibility(View.GONE);
        }
        else{
            binding.layoutWarningValue.setVisibility(View.VISIBLE);
            binding.layoutSound.setVisibility(View.VISIBLE);
            binding.layoutVibrate.setVisibility(View.VISIBLE);

        }
    }

    private void turnVibrateAuto(){
        if(isWarn){
            //Mỗi lần bật chức năng thì mặc định bật thông báo rung
            isVibrate = true;
            binding.swtVibrate.setChecked(isVibrate);
            myPrefs.setIsVibrate(isVibrate);
        }
    }

    private void showWarningValueDialog(){
        AlertDialog.Builder dialogBuilder =	new AlertDialog.Builder(activity);
        warningBinding = DialogWarningBinding.inflate(LayoutInflater.from(getContext()));
        dialogBuilder.setView(warningBinding.getRoot());
        final AlertDialog warningValueDialog = dialogBuilder.create();
        initializeWarningValueDialog();

        warningBinding.btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String result = warningBinding.pickerWarningValue.getDisplayedValues()[warningBinding.pickerWarningValue.getValue()];
                warningVal = Integer.parseInt(result);
                myPrefs.setWarningValue(warningVal);
                activity.loadSharedPreferences();
                Toast.makeText(getActivity(), getString(R.string.noti_warning_value_save), Toast.LENGTH_SHORT).show();
                initializeComponents();
                warningValueDialog.dismiss();
            }
        });

        warningBinding.btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                warningValueDialog.dismiss();
            }
        });

        warningValueDialog.show();
    }

    private void initializeWarningValueDialog(){
        String[] valRange = {"140","135","130","125","120","115","110","105","100",
                "95","90","85","80","75","70","65","60","55","50","45","40","35","30","25","20"};
        warningBinding.pickerWarningValue.setDisplayedValues(valRange);
        warningBinding.pickerWarningValue.setMinValue(0);
        warningBinding.pickerWarningValue.setMaxValue(valRange.length-1);
        warningBinding.pickerWarningValue.setValue(findPosWarningValue(valRange, warningVal));
        warningBinding.pickerWarningValue.setWrapSelectorWheel(false);
    }

    //Phương thức tìm vị trí của giá trị db waring trên number picker
    private int findPosWarningValue(String[] range, int value) {
        for(int i = 0; i< range.length; i++){
            if(Integer.parseInt(range[i]) == value){
                return i;
            }
        }
        return 0;
    }
}
