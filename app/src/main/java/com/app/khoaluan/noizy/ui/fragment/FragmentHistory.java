package com.app.khoaluan.noizy.ui.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.app.khoaluan.noizy.R;
import com.app.khoaluan.noizy.adapter.AdapterHistory;
import com.app.khoaluan.noizy.adapter.AdapterNoiseChart;
import com.app.khoaluan.noizy.databinding.FragmentHistoryBinding;
import com.app.khoaluan.noizy.model.MeasureResult;
import com.app.khoaluan.noizy.ui.ActivityMain;
import com.app.khoaluan.noizy.utils.UtilsXmlFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

public class FragmentHistory extends Fragment {
    private ActivityMain activity = (ActivityMain)getActivity();
    private FragmentHistoryBinding binding;
    private AdapterHistory adapterHistory;

    /* XML file */
    private String fileName = "history.xml";
    private UtilsXmlFile xml = new UtilsXmlFile();
    private List<MeasureResult> resultList = new ArrayList<>();

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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_history, container, false);
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
        activity.changeBackgroundColor(R.color.colorPrimary);

        //Đọc file xml để lấy lịch sử đo nếu có
        resultList = xml.readXmlFile(activity);
        loadHistory(resultList);

        adapterHistory = new AdapterHistory(activity,resultList);
        binding.rvHistory.setLayoutManager(new LinearLayoutManager(activity,
                LinearLayoutManager.VERTICAL, false));
        binding.rvHistory.setAdapter(adapterHistory);
    }

    //Lấy danh sách lịch sử đo và tải lên list view
    private void loadHistory(List<MeasureResult> list) {
        File historyFile = new File(activity.getFilesDir() + "/" + fileName);

        if(historyFile.exists() && !list.isEmpty()){
            binding.textStatus.setVisibility(View.INVISIBLE);
            binding.rvHistory.setVisibility(View.VISIBLE);
        }
        else{
            binding.textStatus.setVisibility(View.VISIBLE);
            binding.rvHistory.setVisibility(View.INVISIBLE);
        }
    }

    private void setEventHandler(){
        binding.fabDelAll.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                confirmDeleteAll();
            }
        });
    }

    //Xác nhận xoá hết tất cả lịch sử
    private void confirmDeleteAll(){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(R.string.title_history_delete_all);
        builder.setMessage(R.string.activity_delete_all_confirm);
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.activity_delete_all_confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //Xoá file và thông báo
                xml.deleteXmlFile(activity);
                Toast.makeText(activity, getString(R.string.noti_all_history_delete), Toast.LENGTH_SHORT).show();
                //Load lại listview
                loadHistory(resultList);
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
