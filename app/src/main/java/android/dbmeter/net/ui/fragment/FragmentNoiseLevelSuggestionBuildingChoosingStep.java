package android.dbmeter.net.ui.fragment;

import android.dbmeter.net.ui.ActivityNoiseLevelSuggestion;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.dbmeter.net.R;
import android.dbmeter.net.databinding.DialogNoiseLevelSuggestHelpBinding;
import android.dbmeter.net.databinding.FragmentNoiseLevelSuggestBuildingChoosingStepBinding;
import android.dbmeter.net.model.BuildingStandard;
import android.dbmeter.net.database.BuildingStandardDatabase;

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
        mBuildings = BuildingStandardDatabase.get(activity);
        initializeBuildingPicker();

        if(firstTime){
            showHelp();
            firstTime = false;
        }
    }

    private void setEventHandler() {
        binding.layoutHelp.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showHelp();
            }
        });

        binding.btnContinue.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                BuildingStandard building = BuildingStandardDatabase.searchBuildingFromName(binding.pickerBuilding.getDisplayedValues()[binding.pickerBuilding.getValue()]);
                activity.setChosenBuilding(building);
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