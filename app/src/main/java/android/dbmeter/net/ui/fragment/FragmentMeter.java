package android.dbmeter.net.ui.fragment;

import android.app.Dialog;
import android.dbmeter.net.ui.ActivityMain;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Toast;

import android.dbmeter.net.R;
import android.dbmeter.net.adapter.AdapterNoiseChart;
import android.dbmeter.net.databinding.DialogCalibrateBinding;
import android.dbmeter.net.databinding.DialogNoisechartBinding;
import android.dbmeter.net.databinding.FragmentMeterBinding;
import android.dbmeter.net.model.Global;
import android.dbmeter.net.model.MeasureResult;
import android.dbmeter.net.model.MyPrefs;
import android.dbmeter.net.model.NoiseLevel;
import android.dbmeter.net.utils.UtilsXmlFile;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import static android.dbmeter.net.utils.AppConfig.MAX_CALIBRATE_VALUE;
import static android.dbmeter.net.utils.AppConfig.MIN_CALIBRATE_VALUE;
import static android.dbmeter.net.utils.AppConfig.WAITING_TIME;

public class FragmentMeter extends Fragment {
    private ActivityMain activity = (ActivityMain)getActivity();
    private FragmentMeterBinding binding;
    private DialogCalibrateBinding calibrateBinding;
    private AdapterNoiseChart adapterNoiseChart;

    private boolean isInitThreadRun = true;
    private boolean isCalibrateThreadRun = false;

    List<MeasureResult> resultList;
    MeasureResult ms;

    /* XML file */
    private UtilsXmlFile xml = new UtilsXmlFile();

    /* Shared Preferences */
    private MyPrefs myPrefs;

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
        myPrefs = new MyPrefs(activity);
        calibrateValue = myPrefs.getCalibrateValue();

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
                ms = getMeasureResult();

                confirmSaveHistory();
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
            activity.stopRecorder();

            //Lưu lại thời gian đếm và dừng đồng hồ đếm
            activity.duration = binding.textDuration.getBase() - SystemClock.elapsedRealtime();
            binding.textDuration.stop();

            Toast.makeText(getActivity(), R.string.noti_pause,Toast.LENGTH_SHORT).show();
        }
        else {
            activity.startRecorder();

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
                myPrefs.setCalibrateValue(calibrateValue);
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

    private MeasureResult getMeasureResult(){
        MeasureResult result = new MeasureResult();

        result.setCurValue(Float.parseFloat(df1.format(Global.lastDb)));
        result.setMinValue(Float.parseFloat(binding.textMinValue.getText().toString()));
        result.setAvgValue(Float.parseFloat(binding.textAverageValue.getText().toString()));
        result.setMaxValue(Float.parseFloat(binding.textMaxValue.getText().toString()));
        String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        result.setDate(currentDate);
        String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
        result.setTime(currentTime);
        result.setDuration(binding.textDuration.getText().toString());

        return result;
    }

    private void confirmSaveHistory(){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(R.string.title_history_save);
        builder.setMessage(R.string.activity_save_confirm);
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.activity_save, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //save history to file
                resultList = new ArrayList<>();
                resultList.add(ms);
                xml.writeXmlFile(activity, resultList);

                //restart
                restartRecord();
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
}
