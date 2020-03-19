package android.dbmeter.net.ui;

import android.dbmeter.net.R;
import android.dbmeter.net.databinding.ActivityMusicPlayerBinding;
import android.dbmeter.net.model.Category;
import android.dbmeter.net.model.Song;
import android.dbmeter.net.ui.fragment.FragmentMusicPlayerCategories;
import android.dbmeter.net.ui.fragment.FragmentMusicPlayerSong;
import android.dbmeter.net.ui.fragment.FragmentMusicPlayerSongList;
import android.dbmeter.net.utils.UtilsActivity;
import android.dbmeter.net.utils.UtilsFragment;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;

import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

public class ActivityMusicPlayer extends AppCompatActivity {
    private ActivityMusicPlayerBinding binding;
    private int currentFragmentId;

    private static Category chosenCategory;
    private static Song chosenSong;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeComponents();
        setEventHandler();
    }

    private void initializeComponents() {
        UtilsActivity.enterFullScreen(ActivityMusicPlayer.this);
        @LayoutRes int layoutId = R.layout.activity_music_player;
        setContentView(layoutId);
        binding = DataBindingUtil.setContentView(this, layoutId);
        setSupportActionBar(binding.toolbar.toolbar);

        switchFragments(1);
    }

    public void switchFragments(int fragmentId){
        @IdRes int frameId = R.id.content_frame;

        if (fragmentId != currentFragmentId) {
            switch (fragmentId) {
                case 1: {
                    binding.toolbar.textTitle.setText(R.string.title_category);
                    UtilsFragment.replace(this, frameId, new FragmentMusicPlayerCategories());
                    break;
                }
                case 2: {
                    binding.toolbar.textTitle.setText(chosenCategory.getCategoryTitle());
                    UtilsFragment.replace(this, frameId, new FragmentMusicPlayerSongList());
                    break;
                }
                case 3: {
                    binding.toolbar.textTitle.setText(chosenSong.getSongTitle());
                    UtilsFragment.replace(this, frameId, new FragmentMusicPlayerSong());
                    break;
                }
            }
            currentFragmentId = fragmentId;
        }
    }

    private void handleBackButton(){
        if(currentFragmentId == 1){
            finish();
        }
        else if(currentFragmentId == 2){
            switchFragments(1);
        }
        else if(currentFragmentId == 3){
            switchFragments(2);
        }
    }

    private void setEventHandler(){
        binding.toolbar.btnBack.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                handleBackButton();
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {

            handleBackButton();

            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    public Category getChosenCategory() {
        return chosenCategory;
    }

    public void setChosenCategory(Category chosenCategory) {
        ActivityMusicPlayer.chosenCategory = chosenCategory;
    }

    public Song getChosenSong() {
        return chosenSong;
    }

    public void setChosenSong(Song chosenSong) {
        ActivityMusicPlayer.chosenSong = chosenSong;
    }
}
