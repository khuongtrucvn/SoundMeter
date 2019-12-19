package com.app.khoaluan.noizy.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.app.khoaluan.noizy.R;
import com.app.khoaluan.noizy.databinding.ItemFragmentHistoryBinding;
import com.app.khoaluan.noizy.model.MeasureResult;
import com.app.khoaluan.noizy.model.NoiseLevel;
import com.app.khoaluan.noizy.utils.UtilsXmlFile;

import java.text.DecimalFormat;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

public class AdapterHistory extends RecyclerView.Adapter<AdapterHistory.HistoryViewHolder>{
    private List<MeasureResult> listData;
    private Context context;
    private LayoutInflater inflater;
    private AdapterHistory.OnItemClickedListener onItemClickedListener;


    class HistoryViewHolder extends RecyclerView.ViewHolder {
        private ItemFragmentHistoryBinding binding;

        HistoryViewHolder(ItemFragmentHistoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public interface OnItemClickedListener {
        void onItemClick(int index);
    }

    public AdapterHistory(Context context, List<MeasureResult> list){
        this.context = context;
        this.listData = list;
    }

    @NonNull
    @Override
    public AdapterHistory.HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (inflater == null)
            inflater = LayoutInflater.from(parent.getContext());
        ItemFragmentHistoryBinding binding = DataBindingUtil.inflate(inflater,
                R.layout.item_fragment_history, parent,false);
        return new AdapterHistory.HistoryViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull final  AdapterHistory.HistoryViewHolder holder, final int position) {
        MeasureResult measureResult = listData.get(position);
        DecimalFormat df1 = new DecimalFormat("####.0");
        holder.binding.textMin.setText(df1.format(measureResult.getMinValue()));
        holder.binding.textAvg.setText(df1.format(measureResult.getAvgValue()));
        holder.binding.textMax.setText(df1.format(measureResult.getMaxValue()));
        holder.binding.textDate.setText(measureResult.getDate());
        holder.binding.textTime.setText(measureResult.getTime());
        holder.binding.textDuration.setText(measureResult.getDuration());
        NoiseLevel nl = new NoiseLevel();
        int status = nl.getNoiseLevelDescription(nl.getNoiseLevel(measureResult.getCurValue()));
        holder.binding.textStatus.setText(status);

        holder.binding.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteNode(v.getContext(),position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listData.size();
    }

    public void setOnItemClickedListener(AdapterHistory.OnItemClickedListener onItemClickedListener) {
        this.onItemClickedListener = onItemClickedListener;
    }

    private void deleteNode(final Context context, final int position) {
        final UtilsXmlFile xml = new UtilsXmlFile();

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.title_confirm);
        builder.setMessage(R.string.activity_delete_confirm);
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.activity_confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                List<MeasureResult> ms = xml.getNodeDeleteList(context,position);
                xml.deleteNodeXmlFile(context,ms);
                AdapterHistory.this.notify(ms);
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

    private void notify(List<MeasureResult> list){
        listData.clear();
        listData.addAll(list);
        notifyDataSetChanged();
    }
}
