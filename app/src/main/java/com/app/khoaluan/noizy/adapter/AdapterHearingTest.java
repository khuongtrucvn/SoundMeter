package com.app.khoaluan.noizy.adapter;

import android.content.Context;
import android.media.MediaPlayer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.app.khoaluan.noizy.R;
import com.app.khoaluan.noizy.databinding.ItemFragmentHearingTestTestingStepBinding;
import com.app.khoaluan.noizy.model.HearingTestData;
import com.app.khoaluan.noizy.database.HearingTestResultDatabase;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import static android.media.ToneGenerator.MAX_VOLUME;

public class AdapterHearingTest extends RecyclerView.Adapter<AdapterHearingTest.HearingTestViewHolder> {
    private ArrayList<HearingTestData> mResults;
    private LayoutInflater inflater;
    private AdapterHearingTest.OnItemClickedListener onItemClickedListener;
    private Context context;
    private MediaPlayer mediaPlayer;

    private double[] volumes = {0, 0.3, 0.7, 1, 5, 10, 45, 70, 99};

    class HearingTestViewHolder extends RecyclerView.ViewHolder {
        private ItemFragmentHearingTestTestingStepBinding binding;

        HearingTestViewHolder(ItemFragmentHearingTestTestingStepBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public AdapterHearingTest(Context context) {
        this.context = context;
        mResults = HearingTestResultDatabase.get();
    }

    public interface OnItemClickedListener {
        void onItemClick(int index);
    }

    @NonNull
    @Override
    public AdapterHearingTest.HearingTestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (inflater == null)
            inflater = LayoutInflater.from(parent.getContext());
        ItemFragmentHearingTestTestingStepBinding binding = DataBindingUtil.inflate(inflater,
                R.layout.item_fragment_hearing_test_testing_step, parent, false);
        return new AdapterHearingTest.HearingTestViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(final @NonNull AdapterHearingTest.HearingTestViewHolder holder, final int position) {
        final HearingTestData result = mResults.get(position);

        if(result.getSide() == 0){
            holder.binding.textEarSide.setText(R.string.hearing_test_left);
        }
        else{
            holder.binding.textEarSide.setText(R.string.hearing_test_right);
        }

        String frequency = result.getFrequency() + " " + context.getString(R.string.sound_frequency_unit);
        holder.binding.textFrequency.setText(frequency);

        String soundLevel = result.getSoundLevel() + " " + context.getString(R.string.sound_testing_unit);
        holder.binding.textSoundLevel.setText(soundLevel);

        holder.binding.btnPlay.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mediaPlayer = MediaPlayer.create(context, result.getSoundFile());
                if(result.getSide() == 0){
                    mediaPlayer.setVolume(getVolume(result.getVolumeLevel()), (float)0.0);
                }
                else{
                    mediaPlayer.setVolume((float)0.0, getVolume(result.getVolumeLevel()));
                }
                mediaPlayer.start();
            }
        });

        holder.binding.btnHear.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(result.getSoundLevel()-10 >= 0){
                    mResults = HearingTestResultDatabase.updateSoundLevel(position, result.getSoundLevel()-10, result.getVolumeLevel()-1, result.getResultSoundLevel()-10);
                    refreshAdapter();
                }

                mediaPlayer = MediaPlayer.create(context, result.getSoundFile());
                if(result.getSide() == 0){
                    mediaPlayer.setVolume(getVolume(result.getVolumeLevel()), (float)0.0);
                }
                else{
                    mediaPlayer.setVolume((float)0.0, getVolume(result.getVolumeLevel()));
                }
                mediaPlayer.start();
            }
        });

        holder.binding.btnCantHear.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(result.getSoundLevel()+10 <=80){
                    mResults = HearingTestResultDatabase.updateSoundLevel(position, result.getSoundLevel()+10, result.getVolumeLevel()+1, result.getResultSoundLevel()+10);
                    refreshAdapter();
                }
                else{
                    mResults = HearingTestResultDatabase.updateSoundLevel(position, result.getSoundLevel(), result.getVolumeLevel(), 90);
                }

                mediaPlayer = MediaPlayer.create(context, result.getSoundFile());
                if(result.getSide() == 0){
                    mediaPlayer.setVolume(getVolume(result.getVolumeLevel()), (float)0.0);
                }
                else{
                    mediaPlayer.setVolume((float)0.0, getVolume(result.getVolumeLevel()));
                }
                mediaPlayer.start();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mResults.size();
    }

    public void setOnItemClickedListener(AdapterHearingTest.OnItemClickedListener onItemClickedListener) {
        this.onItemClickedListener = onItemClickedListener;
    }

    private void refreshAdapter(){
        mResults = HearingTestResultDatabase.get();
        notifyDataSetChanged();
    }

    private float getVolume(int soundLevelPosition){
        double desiredVolume = volumes[soundLevelPosition];
        return (float) (1 - (Math.log(MAX_VOLUME - desiredVolume) / Math.log(MAX_VOLUME)));
    }
}
