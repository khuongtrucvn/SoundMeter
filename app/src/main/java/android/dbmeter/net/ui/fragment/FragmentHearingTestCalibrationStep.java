package android.dbmeter.net.ui.fragment;

import android.content.Context;
import android.dbmeter.net.R;
import android.dbmeter.net.database.HearingTestResultDatabase;
import android.dbmeter.net.databinding.FragmentHearingTestCalibrationStepBinding;
import android.dbmeter.net.ui.ActivityHearingTest;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

public class FragmentHearingTestCalibrationStep extends Fragment {
    private ActivityHearingTest activity = (ActivityHearingTest)getActivity();
    private FragmentHearingTestCalibrationStepBinding binding;

    private MediaPlayer mediaPlayer;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof AppCompatActivity) {
            this.activity = (ActivityHearingTest) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.activity = null;
        if(mediaPlayer != null){
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        HearingTestResultDatabase.delete();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_hearing_test_calibration_step, container, false);
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
        binding.btnPlay.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mediaPlayer = MediaPlayer.create(activity, R.raw.calibration_file);
                mediaPlayer.start();
            }
        });

        binding.btnStartTestingStep.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                activity.switchFragments(2);
            }
        });
    }
}