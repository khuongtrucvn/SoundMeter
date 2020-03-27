package android.dbmeter.net.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.dbmeter.net.R;
import android.dbmeter.net.databinding.FragmentMusicPlayerBinding;
import android.dbmeter.net.ui.ActivityMain;
import android.dbmeter.net.ui.ActivityMusicPlayer;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

public class FragmentMusicPlayer extends Fragment {
    private ActivityMain activity = (ActivityMain)getActivity();
    private FragmentMusicPlayerBinding binding;

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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_music_player, container, false);
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

    private void setEventHandler(){
        binding.btnListen.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(activity, ActivityMusicPlayer.class));
            }
        });
    }

    private void initializeComponents(){
        if (activity != null){
            activity.changeBackgroundColor(R.drawable.color_background);
        }
    }
}
