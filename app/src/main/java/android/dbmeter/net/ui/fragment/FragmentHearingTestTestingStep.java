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
import android.dbmeter.net.adapter.AdapterHearingTest;
import android.dbmeter.net.databinding.DialogHearingTestHelpBinding;
import android.dbmeter.net.databinding.FragmentHearingTestTestingStepBinding;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

public class FragmentHearingTestTestingStep extends Fragment{
    private ActivityHearingTest activity = (ActivityHearingTest)getActivity();
    private FragmentHearingTestTestingStepBinding binding;
    private AdapterHearingTest adapterListInstructions;

    private static boolean firstTime = true;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof AppCompatActivity) {
            this.activity = (ActivityHearingTest) context;
        }
        HearingTestResultDatabase.delete();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.activity = null;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_hearing_test_testing_step, container, false);
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
        binding.toolbar.textTitle.setText(getString(R.string.title_testing));

        if(firstTime){
            showHelp();
            firstTime = false;
        }

        adapterListInstructions = new AdapterHearingTest(activity);
        binding.layoutTest.setAdapter(adapterListInstructions);

        binding.layoutTest.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(final int position) {
                super.onPageSelected(position);
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(position == 0){
                            binding.btnFirstPage.setVisibility(View.INVISIBLE);
                            binding.btnPrevPage.setVisibility(View.INVISIBLE);
                            binding.btnLastPage.setVisibility(View.VISIBLE);
                            binding.btnNextPage.setVisibility(View.VISIBLE);
                        }
                        else if (position == binding.layoutTest.getAdapter().getItemCount()-1){
                            binding.btnLastPage.setVisibility(View.INVISIBLE);
                            binding.btnNextPage.setVisibility(View.INVISIBLE);
                            binding.btnFirstPage.setVisibility(View.VISIBLE);
                            binding.btnPrevPage.setVisibility(View.VISIBLE);
                        }
                        else{
                            binding.btnLastPage.setVisibility(View.VISIBLE);
                            binding.btnNextPage.setVisibility(View.VISIBLE);
                            binding.btnFirstPage.setVisibility(View.VISIBLE);
                            binding.btnPrevPage.setVisibility(View.VISIBLE);
                        }
                    }
                });
            }
        });
    }

    private void setEventHandler() {
        binding.toolbar.btnBack.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                backToCalibrationStep();
            }
        });

        binding.layoutHelp.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showHelp();
            }
        });

        binding.btnFirstPage.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                binding.layoutTest.setCurrentItem(0, true);
            }
        });

        binding.btnPrevPage.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                binding.layoutTest.setCurrentItem(binding.layoutTest.getCurrentItem()-1, true);
            }
        });

        binding.btnNextPage.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                binding.layoutTest.setCurrentItem(binding.layoutTest.getCurrentItem()+1, true);
            }
        });

        binding.btnLastPage.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                binding.layoutTest.setCurrentItem(binding.layoutTest.getAdapter().getItemCount()-1, true);
            }
        });

        binding.btnFinish.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                proceedToResultStep();
            }
        });
    }

    private void backToCalibrationStep(){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(R.string.title_back_calibration);
        builder.setMessage(R.string.activity_back_calibration_hearingTest);
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.title_back, new DialogInterface.OnClickListener() {
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

    private void showHelp(){
        AlertDialog.Builder dialogBuilder =	new AlertDialog.Builder(activity);
        DialogHearingTestHelpBinding helpBinding = DialogHearingTestHelpBinding.inflate(LayoutInflater.from(getContext()));
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

    private void proceedToResultStep(){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(R.string.title_finish_test);
        builder.setMessage(R.string.activity_proceed_result_hearingTest);
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.title_finish, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                activity.switchFragments(3);
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
