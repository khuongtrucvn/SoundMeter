package android.dbmeter.net.ui.fragment;

import android.content.Context;
import android.dbmeter.net.R;
import android.dbmeter.net.adapter.AdapterSongList;
import android.dbmeter.net.databinding.FragmentMusicPlayerSongListBinding;
import android.dbmeter.net.model.Category;
import android.dbmeter.net.model.Song;
import android.dbmeter.net.ui.ActivityMusicPlayer;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class FragmentMusicPlayerSongList extends Fragment {
    private ActivityMusicPlayer activity = (ActivityMusicPlayer)getActivity();
    private FragmentMusicPlayerSongListBinding binding;

    private Category chosenCategory;
    private boolean done = false;
    private ArrayList<Song> songList = new ArrayList<>();
    private AdapterSongList adapterSongList;

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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_music_player_song_list, container, false);
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
        chosenCategory = activity.getChosenCategory();

        adapterSongList = new AdapterSongList(activity, songList);
        binding.rvSongList.setLayoutManager(new LinearLayoutManager(activity,
                LinearLayoutManager.VERTICAL, false));
        binding.rvSongList.setAdapter(adapterSongList);

        new Thread(new Runnable() {
            @Override
            public void run() {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        binding.layoutProgress.setVisibility(View.VISIBLE);
                        binding.layoutSongList.setVisibility(View.INVISIBLE);
                        binding.layoutNotification.setVisibility(View.INVISIBLE);
                    }
                });
                loadData();
                controlLayout(songList);
            }
        }).start();
    }

    private void setEventHandler() {
        binding.swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                done = false;

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        loadData();
                        controlLayout(songList);

                        if(activity != null){
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    binding.swipeRefreshLayout.setRefreshing(false);
                                }
                            });
                        }
                    }
                }).start();
            }
        });

        adapterSongList.setOnItemClickedListener(new AdapterSongList.OnItemClickedListener() {
            @Override
            public void onItemClick(int position) {
                activity.setChosenSong(songList.get(position));
                activity.switchFragments(3);
            }
        });
    }

    private void loadData(){
        while (!done) {
            String address = "https://soundmeterapi.herokuapp.com/api/category/" + chosenCategory.getCategoryId();
            String songListString = getWebPage(address);

            try{
                if(songListString != null){
                    JSONObject obj = new JSONObject(songListString);
                    JSONArray array = obj.getJSONArray("musics");
                    songList = getSongListFromJsonData(array);
                }
            }
            catch(JSONException e){
                e.printStackTrace();
            }

            if(activity != null){
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapterSongList.notify(songList);
                    }
                });
            }

            done = true;
        }
    }

    private String getWebPage(String address) {
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        String result = null;

        try {
            URL url = new URL(address);

            connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            InputStream stream = connection.getInputStream();

            reader = new BufferedReader(new InputStreamReader(stream));

            StringBuilder buffer = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                buffer.append(line).append("\n");
            }

            result = buffer.toString();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    private void controlLayout(final ArrayList<Song> songList){
        if(activity != null){
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(songList.size() != 0){
                        binding.layoutSongList.setVisibility(View.VISIBLE);
                        binding.layoutNotification.setVisibility(View.INVISIBLE);
                    }
                    else{
                        binding.layoutSongList.setVisibility(View.INVISIBLE);
                        binding.textNotification.setText(R.string.noti_check_network);
                        binding.layoutNotification.setVisibility(View.VISIBLE);
                    }
                    binding.layoutProgress.setVisibility(View.INVISIBLE);
                }
            });
        }
    }

    private ArrayList<Song> getSongListFromJsonData(JSONArray songArray){
        ArrayList<Song> result = new ArrayList<>();

        for(int i=0; i < songArray.length(); i++){
            try{
                JSONObject songObject = songArray.getJSONObject(i);
                JSONObject thumbnailObject = songObject.getJSONObject("thumbnails");
                JSONObject thumbnailQualityObject = thumbnailObject.getJSONObject("high");

                Song song = new Song(songObject.getString("id"), songObject.getString("title"),
                        songObject.getString("description"), thumbnailQualityObject.getString("url"),
                        songObject.getString("url"));
                result.add(song);
            } catch (JSONException e){
                e.printStackTrace();
            }
        }

        return result;
    }
}
