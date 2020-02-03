package com.app.khoaluan.noizy.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.app.khoaluan.noizy.R;
import com.app.khoaluan.noizy.databinding.DialogNoiseLevelSuggestHelpBinding;
import com.app.khoaluan.noizy.databinding.FragmentNoiseLevelSuggestBuildingChoosingStepBinding;
import com.app.khoaluan.noizy.model.BuildingStandard;
import com.app.khoaluan.noizy.database.BuildingStandardDatabase;
import com.app.khoaluan.noizy.ui.ActivityNoiseLevelSuggestion;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

public class FragmentNoiseLevelSuggestionBuildingChoosingStep extends Fragment {
    private ActivityNoiseLevelSuggestion activity = (ActivityNoiseLevelSuggestion)getActivity();
    private FragmentNoiseLevelSuggestBuildingChoosingStepBinding binding;

    private ArrayList<BuildingStandard> mBuildings = new ArrayList<>();

    private static boolean firstTime = true;

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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_noise_level_suggest_building_choosing_step, container, false);
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
        activity.setSupportActionBar(binding.toolbar.toolbar);
        binding.toolbar.textTitle.setText(getString(R.string.title_buildings));

        mBuildings = BuildingStandardDatabase.get(activity);
        initializeBuildingPicker();

        if(firstTime){
            showHelp();
            firstTime = false;
        }
    }

    private void setEventHandler() {
        binding.toolbar.btnBack.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                activity.finish();
            }
        });

        binding.layoutHelp.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showHelp();
            }
        });

        binding.btnContinue.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int buildingId = BuildingStandardDatabase.searchBuildingId(binding.pickerBuilding.getDisplayedValues()[binding.pickerBuilding.getValue()]);
                activity.setBuildingIdChosen(buildingId);
                activity.switchFragments(2);
            }
        });
    }

    private void showHelp(){
        AlertDialog.Builder dialogBuilder =	new AlertDialog.Builder(activity);
        DialogNoiseLevelSuggestHelpBinding helpBinding = DialogNoiseLevelSuggestHelpBinding.inflate(LayoutInflater.from(getContext()));
        dialogBuilder.setView(helpBinding.getRoot());
        final AlertDialog helpDialog = dialogBuilder.create();

        helpBinding.btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                helpDialog.dismiss();
            }
        });

        helpDialog.show();
    }

    private void initializeBuildingPicker(){
        ArrayList<String> buildings = new ArrayList<>();
        for (BuildingStandard bs: mBuildings) {
            buildings.add(bs.getBuildingName());
        }

        String[] building_array = buildings.toArray(new String[mBuildings.size()]);
        binding.pickerBuilding.setDisplayedValues(building_array);
        binding.pickerBuilding.setMinValue(0);
        binding.pickerBuilding.setMaxValue(building_array.length-1);
        binding.pickerBuilding.setWrapSelectorWheel(true);
    }
}