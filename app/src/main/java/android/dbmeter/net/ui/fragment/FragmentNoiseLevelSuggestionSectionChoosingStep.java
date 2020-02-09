package android.dbmeter.net.ui.fragment;

import android.dbmeter.net.database.BuildingSectionStandardDatabase;
import android.dbmeter.net.database.BuildingStandardDatabase;
import android.dbmeter.net.model.BuildingSectionStandard;
import android.dbmeter.net.model.BuildingStandard;
import android.dbmeter.net.ui.ActivityNoiseLevelSuggestion;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.dbmeter.net.R;
import android.dbmeter.net.databinding.DialogNoiseLevelSuggestHelpBinding;
import android.dbmeter.net.databinding.FragmentNoiseLevelSuggestSectionChoosingStepBinding;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

public class FragmentNoiseLevelSuggestionSectionChoosingStep extends Fragment {
    private ActivityNoiseLevelSuggestion activity = (ActivityNoiseLevelSuggestion)getActivity();
    private FragmentNoiseLevelSuggestSectionChoosingStepBinding binding;

    private BuildingStandard chosenBuilding;
    private ArrayList<BuildingSectionStandard> mSection = new ArrayList<>();

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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_noise_level_suggest_section_choosing_step, container, false);
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
        mSection = BuildingSectionStandardDatabase.get(activity);
        mSection = BuildingSectionStandardDatabase.searchSectionFromBuilding(activity.getBuildingIdChosen());
        chosenBuilding = BuildingStandardDatabase.searchBuilding(activity.getBuildingIdChosen());

        activity.setSupportActionBar(binding.toolbar.toolbar);
        binding.toolbar.textTitle.setText(chosenBuilding.getBuildingName());

        String question = activity.getString(R.string.noti_noise_suggestion_choose_section_1) + " " + chosenBuilding.getBuildingName() + " " + activity.getString(R.string.noti_noise_suggestion_choose_section_2);
        binding.textAsk.setText(question);

        initializeSectionPicker();
    }

    private void setEventHandler() {
        binding.toolbar.btnBack.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                activity.switchFragments(1);
            }
        });

        binding.layoutHelp.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showHelp();
            }
        });

        binding.btnCheckValue.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int sectionId = BuildingSectionStandardDatabase.searchSectionIdOfBuilding(chosenBuilding.getBuildingId(),binding.pickerSection.getDisplayedValues()[binding.pickerSection.getValue()]);
                activity.setSectionIdChosen(sectionId);
                activity.setIsChoiceStartMeasure(false);
                activity.switchFragments(3);
            }
        });

        binding.btnMeasure.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int sectionId = BuildingSectionStandardDatabase.searchSectionIdOfBuilding(chosenBuilding.getBuildingId(),binding.pickerSection.getDisplayedValues()[binding.pickerSection.getValue()]);
                activity.setSectionIdChosen(sectionId);
                activity.setIsChoiceStartMeasure(true);
                activity.switchFragments(3);
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

    private void initializeSectionPicker(){
        ArrayList<String> sections = new ArrayList<>();
        for (BuildingSectionStandard bss: mSection) {
            sections.add(bss.getSectionName());
        }

        String[] section_array = sections.toArray(new String[mSection.size()]);
        binding.pickerSection.setDisplayedValues(section_array);
        binding.pickerSection.setMinValue(0);
        binding.pickerSection.setMaxValue(section_array.length-1);
        binding.pickerSection.setWrapSelectorWheel(true);
    }
}
