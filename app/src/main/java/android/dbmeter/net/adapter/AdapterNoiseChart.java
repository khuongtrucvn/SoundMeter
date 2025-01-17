package android.dbmeter.net.adapter;

import android.dbmeter.net.model.NoiseLevel;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import android.dbmeter.net.R;
import android.dbmeter.net.databinding.ItemDialogNoisechartBinding;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

public class AdapterNoiseChart extends RecyclerView.Adapter<AdapterNoiseChart.NoiseChartViewHolder>{
    private List<NoiseLevel> listData;
    private Context context;
    private LayoutInflater inflater;
    private AdapterNoiseChart.OnItemClickedListener onItemClickedListener;

    class NoiseChartViewHolder extends RecyclerView.ViewHolder {
        private ItemDialogNoisechartBinding binding;

        NoiseChartViewHolder(ItemDialogNoisechartBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public interface OnItemClickedListener {
        void onItemClick(int index);
    }

    public AdapterNoiseChart(Context context, List<NoiseLevel> list){
        this.context = context;
        this.listData = list;
    }

    @NonNull
    @Override
    public AdapterNoiseChart.NoiseChartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (inflater == null)
            inflater = LayoutInflater.from(parent.getContext());
        ItemDialogNoisechartBinding binding = DataBindingUtil.inflate(inflater,
                R.layout.item_dialog_noisechart, parent,false);
        return new AdapterNoiseChart.NoiseChartViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull final  AdapterNoiseChart.NoiseChartViewHolder holder, final int position) {
        NoiseLevel noiseLevel = listData.get(position);

        holder.binding.textSoundValue.setText(noiseLevel.getLevel());
        holder.binding.textSoundDescription.setText(noiseLevel.getDescription());
    }

    @Override
    public int getItemCount() {
        return listData.size();
    }

    public void setOnItemClickedListener(AdapterNoiseChart.OnItemClickedListener onItemClickedListener) {
        this.onItemClickedListener = onItemClickedListener;
    }
}
