package android.dbmeter.net.ui.fragment;

import android.content.Context;
import android.dbmeter.net.R;
import android.dbmeter.net.databinding.FragmentMusicPlayerSongBinding;
import android.dbmeter.net.model.Song;
import android.dbmeter.net.ui.ActivityMusicPlayer;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

public class FragmentMusicPlayerSong extends Fragment {
    private ActivityMusicPlayer activity = (ActivityMusicPlayer)getActivity();
    private FragmentMusicPlayerSongBinding binding;

    private Song chosenSong;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof AppCompatActivity) {
            this.activity = (ActivityMusicPlayer) context;
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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_music_player_song, container, false);
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
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding.songVideo.release();
    }

    private void initializeComponents(){
        chosenSong = activity.getChosenSong();

        getLifecycle().addObserver(binding.songVideo);

        binding.songVideo.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
            @Override
            public void onReady(@NonNull YouTubePlayer youTubePlayer) {
                super.onReady(youTubePlayer);
                youTubePlayer.loadVideo(chosenSong.getSongId(), 0);
            }
        });

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                binding.songTitle.setText(chosenSong.getSongTitle());
                binding.songDescription.setText(chosenSong.getSongDescription());
            }
        });
    }
}
