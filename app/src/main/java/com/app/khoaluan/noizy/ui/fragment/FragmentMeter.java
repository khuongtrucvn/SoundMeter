package com.app.khoaluan.noizy.ui.fragment;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.app.khoaluan.noizy.R;
import com.app.khoaluan.noizy.adapter.AdapterNoiseChart;
import com.app.khoaluan.noizy.databinding.DialogCalibrateBinding;
import com.app.khoaluan.noizy.databinding.DialogNoisechartBinding;
import com.app.khoaluan.noizy.databinding.FragmentMeterBinding;
import com.app.khoaluan.noizy.model.Global;
import com.app.khoaluan.noizy.model.NoiseLevel;
import com.app.khoaluan.noizy.ui.ActivityMain;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import static com.app.khoaluan.noizy.utils.AppConfig.MAX_CALIBRATE_VALUE;
import static com.app.khoaluan.noizy.utils.AppConfig.MIN_CALIBRATE_VALUE;
import static com.app.khoaluan.noizy.utils.AppConfig.WAITING_TIME;

public class FragmentMeter extends Fragment {
    private ActivityMain activity = (ActivityMain)getActivity();
    private FragmentMeterBinding binding;
    private DialogCalibrateBinding calibrateBinding;
    private AdapterNoiseChart adapterNoiseChart;

    private boolean isInitThreadRun = true;
    private boolean isCalibrateThreadRun = false;

    /* Recorder */
    private NoiseLevel noiseLevel = new NoiseLevel();

    /* Calibrate */
    private int calibrateValue = Global.calibrateValue;

    /* Format */
    private DecimalFormat df1;

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
        isInitThreadRun = false;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_meter, container, false);
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

    private void initializeComponents() {
        setMeasureResultFormat();

        if(!activity.isRecording){                  // Đang tạm dừng
            binding.btnPausePlay.setChecked(true);
            binding.textDuration.setBase(SystemClock.elapsedRealtime() + activity.duration);
        }
        else
            binding.textDuration.start();


        new Thread(new Runnable() {
            @Override
            public void run() {
                while (isInitThreadRun) {
                    try {
                        int level = noiseLevel.getNoiseLevel(Global.lastDb);
                        int description = noiseLevel.getNoiseLevelDescription(level);
                        noiseLevel.setSoundLevel(level, description);

                        if(activity != null){
                            //Cập nhật dữ liệu lên giao diện
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if(Global.minDb < 0)
                                        binding.textMinValue.setText(df1.format(0.0));
                                    else
                                        binding.textMinValue.setText(df1.format(Global.minDb));

                                    binding.textAverageValue.setText(df1.format(Global.avgDb));
                                    binding.textMaxValue.setText(df1.format(Global.maxDb));
                                    binding.textStatus.setText(noiseLevel.getDescription());

                                    warnRealTime(Global.lastDb);

                                    binding.speedometer.refresh();
                                }
                            });
                        }

                        Thread.sleep(WAITING_TIME);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    //Phương thức cài đặt format
    private void setMeasureResultFormat(){
        Locale currentLocale = Locale.getDefault();
        DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(currentLocale);
        otherSymbols.setDecimalSeparator('.');
        df1 = new DecimalFormat("##0.0", otherSymbols);
    }

    private void setEventHandler(){
        //Sự kiện nhấn nút Làm mới
        binding.btnRestart.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                restartRecord();
            }
        });

        //Sự kiện nhấn nút Tạm dừng/tiếp tục
        binding.btnPausePlay.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                pauseAndResumeRecord(isChecked);
            }
        });

        //Sự kiện nhấn khung thang độ ồn
        binding.layoutInfo.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showNoiseChartDialog();
            }
        });

        //Sự kiện nhấn nút Hiệu chỉnh
        binding.btnCalibrate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showCalibrateDialog();
            }
        });

        //Sự kiện nhấn nút Lưu kết quả đo độ ồn
        binding.btnSave.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

            }
        });
    }

    private void restartRecord(){
        activity.restartRecorder();
        //Restart đồng hồ về 0
        binding.textDuration.setBase(SystemClock.elapsedRealtime());

        if(binding.btnPausePlay.isChecked()) {
            binding.btnPausePlay.setChecked(false);
            binding.textDuration.setBase(SystemClock.elapsedRealtime());
            binding.textDuration.start();
        }

        Toast.makeText(getActivity(), R.string.noti_refresh, Toast.LENGTH_SHORT).show();
    }

    private void pauseAndResumeRecord(boolean isChecked){
        if (isChecked) {
            activity.pauseRecorder();

            //Lưu lại thời gian đếm và dừng đồng hồ đếm
            activity.duration = binding.textDuration.getBase() - SystemClock.elapsedRealtime();
            binding.textDuration.stop();

            Toast.makeText(getActivity(), R.string.noti_pause,Toast.LENGTH_SHORT).show();
        }
        else {
            activity.resumeRecorder();

            //Gán thời gian đếm và chạy đồng hồ đếm
            binding.textDuration.setBase(SystemClock.elapsedRealtime() + activity.duration);
            binding.textDuration.start();

            Toast.makeText(getActivity(), R.string.noti_resume,Toast.LENGTH_SHORT).show();
        }
    }

    private List<NoiseLevel> getNoiseLevelData() {
        NoiseLevel[] levels={new NoiseLevel(R.string.noise_20,R.string.noise_20_des),
                new NoiseLevel(R.string.noise_30,R.string.noise_30_des),
                new NoiseLevel(R.string.noise_40,R.string.noise_40_des),
                new NoiseLevel(R.string.noise_50,R.string.noise_50_des),
                new NoiseLevel(R.string.noise_60,R.string.noise_60_des),
                new NoiseLevel(R.string.noise_70,R.string.noise_70_des),
                new NoiseLevel(R.string.noise_80,R.string.noise_80_des),
                new NoiseLevel(R.string.noise_90,R.string.noise_90_des),
                new NoiseLevel(R.string.noise_100,R.string.noise_100_des),
                new NoiseLevel(R.string.noise_110,R.string.noise_110_des),
                new NoiseLevel(R.string.noise_120,R.string.noise_120_des),
                new NoiseLevel(R.string.noise_130,R.string.noise_130_des),
                new NoiseLevel(R.string.noise_140,R.string.noise_140_des),
        };

        return new ArrayList<>(Arrays.asList(levels));
    }

    private void showNoiseChartDialog(){
        final Dialog noiseChartDialog = new Dialog(activity);
        DialogNoisechartBinding chartBinding = DialogNoisechartBinding.inflate(LayoutInflater.from(getContext()));
        noiseChartDialog.setContentView(chartBinding.getRoot());

        adapterNoiseChart = new AdapterNoiseChart(activity,getNoiseLevelData());
        chartBinding.rvNoiseChart.setLayoutManager(new LinearLayoutManager(activity,
                LinearLayoutManager.VERTICAL, false));
        chartBinding.rvNoiseChart.setAdapter(adapterNoiseChart);

        chartBinding.btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                noiseChartDialog.dismiss();
            }
        });

        noiseChartDialog.show();
    }

    private void showCalibrateDialog(){
        AlertDialog.Builder dialogBuilder =	new AlertDialog.Builder(activity);
        calibrateBinding = DialogCalibrateBinding.inflate(LayoutInflater.from(getContext()));
        dialogBuilder.setView(calibrateBinding.getRoot());
        final AlertDialog calibrateDialog = dialogBuilder.create();
        initializeCalibrateDialog();

        calibrateBinding.btnIncrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(calibrateValue < MAX_CALIBRATE_VALUE){
                    calibrateValue++;
                    loadCalibrateValueHandler();
                }
            }
        });

        calibrateBinding.btnDecrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(calibrateValue > MIN_CALIBRATE_VALUE){
                    calibrateValue--;
                    loadCalibrateValueHandler();
                }
            }
        });

        calibrateBinding.btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Global.calibrateValue = calibrateValue;
                restartRecord();
                Toast.makeText(getActivity(), R.string.noti_calibrate_save, Toast.LENGTH_SHORT).show();
                calibrateDialog.dismiss();
                isCalibrateThreadRun = false;
            }
        });

        calibrateBinding.btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calibrateDialog.dismiss();
                isCalibrateThreadRun = false;
            }
        });

        calibrateDialog.show();
    }

    //Phương thức khởi tạo alert dialog hiệu chỉnh
    private void initializeCalibrateDialog(){
        loadCalibrateValueHandler();

        isCalibrateThreadRun = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (isCalibrateThreadRun) {
                    try {
                        if(activity != null){
                            //Cập nhật dữ liệu lên giao diện
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    float curdbVal = Global.dbCount;
                                    calibrateBinding.textCurrentValue.setText(df1.format(curdbVal));
                                }
                            });
                        }

                        Thread.sleep(WAITING_TIME);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    //Phương thức tải giá trị sai số lên alert dialog hiệu chỉnh
    private void loadCalibrateValueHandler(){
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                //tải giá trị không dấu
                calibrateBinding.textTolerance.setText(Integer.toString(Math.abs(calibrateValue)));

                //tải dấu
                if(calibrateValue < 0)
                    calibrateBinding.textSign.setText(R.string.minus);
                else
                    calibrateBinding.textSign.setText(R.string.plus);
            }
        });
    }

    //Phương thức cảnh báo thời gian thực, sử dụng synchronized vì trong thời gian thread chạy nếu đổi fragment thì activity = null => crash
    private synchronized void warnRealTime(float value){
        if(activity != null){
            int backgroundColor = noiseLevel.getWarningColor(value);                //Điều chỉnh màu nền cảnh báo người dùng
            activity.changeBackgroundColor(backgroundColor);
        }
    }
}
