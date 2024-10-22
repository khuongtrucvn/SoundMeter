package android.dbmeter.net.ui.fragment;

import android.content.Context;
import android.dbmeter.net.R;
import android.dbmeter.net.databinding.FragmentNoiseLevelSuggestResultStepBinding;
import android.dbmeter.net.model.BuildingSectionStandard;
import android.dbmeter.net.model.BuildingStandard;
import android.dbmeter.net.model.Global;
import android.dbmeter.net.ui.ActivityNoiseLevelSuggestion;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

public class FragmentNoiseLevelSuggestionResultStep extends Fragment {
    private ActivityNoiseLevelSuggestion activity = (ActivityNoiseLevelSuggestion)getActivity();
    private FragmentNoiseLevelSuggestResultStepBinding binding;

    private boolean isChoiceStartMeasure;
    private BuildingSectionStandard chosenSection;
    private BuildingStandard chosenBuilding;

    private int progressStatus = 0;

    private DecimalFormat df1;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof AppCompatActivity) {
            this.activity = (ActivityNoiseLevelSuggestion) context;
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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_noise_level_suggest_result_step, container, false);
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
        isChoiceStartMeasure = activity.getIsChoiceStartMeasure();
        chosenBuilding = activity.getChosenBuilding();
        chosenSection = activity.getChosenSection();

        final String location = chosenBuilding.getBuildingName() + " " + activity.getString(R.string.minus) + " " + chosenSection.getSectionName();

        setMeasureResultFormat();

        binding.textLocation.setText(location);
        binding.textStandardValue.setText(String.valueOf(chosenSection.getSectionNoiseLevel()));

        if (!isChoiceStartMeasure){
            binding.layoutProgress.setVisibility(View.INVISIBLE);
            binding.layoutResult.setVisibility(View.VISIBLE);
            binding.btnMeasureAgain.setVisibility(View.INVISIBLE);
            binding.layoutActualNoise.setVisibility(View.INVISIBLE);
            binding.textConclusion.setVisibility(View.INVISIBLE);
        }
        else{
            activity.restartRecorder();
            binding.layoutProgress.setVisibility(View.VISIBLE);
            binding.layoutResult.setVisibility(View.INVISIBLE);

            new Thread(new Runnable() {
                public void run() {
                    while (progressStatus < 100) {
                        progressStatus += 1;
                        binding.progressBar.setProgress(progressStatus);
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    final float minValue = Global.minDb, maxValue = Global.maxDb, avgValue = Global.avgDb;

                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            binding.layoutProgress.setVisibility(View.INVISIBLE);
                            binding.layoutResult.setVisibility(View.VISIBLE);

                            binding.textMinValue.setText(df1.format(minValue));
                            binding.textMaxValue.setText(df1.format(maxValue));
                            binding.textAverageValue.setText(df1.format(avgValue));

                            if(avgValue <= chosenSection.getSectionNoiseLevel()){
                                binding.textConclusion.setTextColor(ContextCompat.getColor(activity,R.color.green));
                                binding.textConclusion.setText(R.string.noti_noise_suggestion_conclusion_true);
                            }
                            else{
                                binding.textConclusion.setTextColor(ContextCompat.getColor(activity,R.color.red));
                                binding.textConclusion.setText(R.string.noti_noise_suggestion_conclusion_false);
                            }
                        }
                    });
                }
            }).start();
        }
    }

    private void setEventHandler(){
        binding.btnMeasureAgain.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                progressStatus = 0;
                initializeComponents();
            }
        });

        binding.btnCheckOther.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                activity.switchFragments(1);
            }
        });

        binding.btnExitTest.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                activity.exitNoiseLevelSuggestion();
            }
        });
    }

    private void setMeasureResultFormat(){
        Locale currentLocale = Locale.getDefault();
        DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(currentLocale);
        otherSymbols.setDecimalSeparator('.');
        df1 = new DecimalFormat("##0.0", otherSymbols);
    }
}