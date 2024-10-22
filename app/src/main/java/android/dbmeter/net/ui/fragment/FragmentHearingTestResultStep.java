package android.dbmeter.net.ui.fragment;

import android.dbmeter.net.database.HearingTestResultDatabase;
import android.dbmeter.net.ui.ActivityHearingTest;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.dbmeter.net.R;
import android.dbmeter.net.databinding.FragmentHearingTestResultStepBinding;
import android.dbmeter.net.model.HearingTestData;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

public class FragmentHearingTestResultStep extends Fragment {
    private ActivityHearingTest activity = (ActivityHearingTest) getActivity();
    private FragmentHearingTestResultStepBinding binding;

    private ArrayList<Integer> leftEarResult = new ArrayList<>(), rightEarResult = new ArrayList<>();

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
        HearingTestResultDatabase.delete();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_hearing_test_result_step, container, false);
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
        getResult();

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                binding.textLeftEarResult.setText(getHearingTestStandard(leftEarResult));
                binding.textRightEarResult.setText(getHearingTestStandard(rightEarResult));
            }
        });
    }

    private void setEventHandler(){
        binding.btnExitTest.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                activity.exitHearingTest();
            }
        });

        binding.btnTestAgain.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                testAgain();
            }
        });
    }

    private void testAgain(){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(R.string.title_test_again);
        builder.setMessage(R.string.activity_test_again_hearingTest);
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.activity_confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                activity.switchFragments(1);
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

    private void getResult(){
        ArrayList<HearingTestData> results = HearingTestResultDatabase.get();

        for (HearingTestData data: results) {
            if(data.getSide() == 0){
                leftEarResult.add(data.getResultSoundLevel());
            }
            else{
                rightEarResult.add(data.getResultSoundLevel());
            }
        }
    }

    private String getHearingTestStandard(ArrayList<Integer> data){
        int max = data.get(0);

        for (Integer i : data) {
            if(max < i){
                max = i;
            }
        }

        if(max > 81){
            return getString(R.string.hearing_test_result_profound);
        }
        else if(max > 61){
            return getString(R.string.hearing_test_result_severe);
        }
        else if(max > 41){
            return getString(R.string.hearing_test_result_moderate);
        }
        else if(max > 26){
            return getString(R.string.hearing_test_result_mild);
        }
        else{
            return getString(R.string.hearing_test_result_normal);
        }
    }
}