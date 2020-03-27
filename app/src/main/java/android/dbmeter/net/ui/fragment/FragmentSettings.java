package android.dbmeter.net.ui.fragment;

import android.dbmeter.net.database.LocaleDescriptionDatabase;
import android.dbmeter.net.databinding.DialogLanguageBinding;
import android.dbmeter.net.ui.ActivityMain;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.LocaleList;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Toast;

import android.dbmeter.net.R;
import android.dbmeter.net.databinding.DialogWarningBinding;
import android.dbmeter.net.databinding.FragmentSettingsBinding;
import android.dbmeter.net.model.MyPrefs;
import android.dbmeter.net.model.LocaleDescription;

import java.util.ArrayList;
import java.util.Locale;

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
    private DialogLanguageBinding languageBinding;

    /* Shared Preferences */
    private MyPrefs myPrefs;
    private boolean isWarn, isVibrate, isSound;
    private int warningVal;
    private String localeCode;
    private ArrayList<LocaleDescription> mLocale = new ArrayList<>();

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
        if (activity != null){
            activity.changeBackgroundColor(R.drawable.color_background);
        }

        mLocale = LocaleDescriptionDatabase.get(activity);

        loadSharedPreferences();

        if(activity != null){
            activity.runOnUiThread(new Runnable(){
                @Override
                public void run() {
                    binding.chkWarn.setChecked(isWarn);
                    turndbWarning();
                    binding.textWarnValue.setText(Integer.toString(warningVal));
                    binding.swtVibrate.setChecked(isVibrate);
                    binding.swtSound.setChecked(isSound);

                    String localeLanguage = LocaleDescriptionDatabase.getLocaleNameFromCode(localeCode);
                    binding.textLanguage.setText(localeLanguage);
                }
            });
        }
    }

    //Phương thức tải dữ liệu từ Shared Preferences
    private void loadSharedPreferences(){
        myPrefs = new MyPrefs(activity);
        isWarn = myPrefs.getIsWarn();
        warningVal = myPrefs.getWarningValue();
        isVibrate = myPrefs.getIsVibrate();
        isSound = myPrefs.getIsSound();
        localeCode = myPrefs.getLocale();
    }

    private void setEventHandler(){
        binding.layoutLanguage.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showLanguageDialog();
            }
        });

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

    private void showLanguageDialog(){
        AlertDialog.Builder dialogBuilder =	new AlertDialog.Builder(activity);
        languageBinding = DialogLanguageBinding.inflate(LayoutInflater.from(getContext()));
        dialogBuilder.setView(languageBinding.getRoot());
        final AlertDialog languageDialog = dialogBuilder.create();
        initializeLanguageDialog();

        languageBinding.btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String localeName = languageBinding.pickerLanguage.getDisplayedValues()[languageBinding.pickerLanguage.getValue()];
                String localeCode = LocaleDescriptionDatabase.getLocaleCodeFromName(localeName);
                myPrefs.setLocale(localeCode);
                Toast.makeText(getActivity(), getString(R.string.noti_warning_value_save), Toast.LENGTH_SHORT).show();
                languageDialog.dismiss();
                activity.restartApplication();
            }
        });

        languageBinding.btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                languageDialog.dismiss();
            }
        });

        languageDialog.show();
    }

    private void initializeLanguageDialog(){

        String[] valRange = LocaleDescriptionDatabase.getLocaleNameArrayList().toArray(new String[mLocale.size()]);
        languageBinding.pickerLanguage.setDisplayedValues(valRange);
        languageBinding.pickerLanguage.setMinValue(0);
        languageBinding.pickerLanguage.setMaxValue(valRange.length-1);
        languageBinding.pickerLanguage.setValue(findPosLanguage(valRange, localeCode));
        languageBinding.pickerLanguage.setWrapSelectorWheel(false);
    }

    //Phương thức tìm vị trí của ngôn ngữ trên number picker từ localeCode
    private int findPosLanguage(String[] range, String localeCode) {
        for(int i = 0; i< range.length; i++){
            if(range[i].equals(LocaleDescriptionDatabase.getLocaleNameFromCode(localeCode))){
                return i;
            }
        }
        return 0;
    }
}
