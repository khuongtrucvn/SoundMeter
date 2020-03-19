package android.dbmeter.net.ui.fragment;

import android.content.Context;
import android.dbmeter.net.R;
import android.dbmeter.net.adapter.AdapterCategory;
import android.dbmeter.net.databinding.FragmentMusicPlayerCategoriesBinding;
import android.dbmeter.net.model.Category;
import android.dbmeter.net.model.MyPrefs;
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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class FragmentMusicPlayerCategories extends Fragment{
    private ActivityMusicPlayer activity = (ActivityMusicPlayer)getActivity();
    private FragmentMusicPlayerCategoriesBinding binding;

    private boolean done = false;
    private ArrayList<Category> categories = new ArrayList<>();
    private AdapterCategory adapterCategory;

    /* Shared Preferences */
    private MyPrefs myPrefs;
    private String language;

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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_music_player_categories, container, false);
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
        loadSharedPreferences();

        adapterCategory = new AdapterCategory(activity, categories);
        binding.rvCategory.setLayoutManager(new GridLayoutManager(activity,2));
        binding.rvCategory.setAdapter(adapterCategory);

        new Thread(new Runnable() {
            @Override
            public void run() {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        binding.layoutProgress.setVisibility(View.VISIBLE);
                        binding.layoutCategory.setVisibility(View.INVISIBLE);
                        binding.layoutNotification.setVisibility(View.INVISIBLE);
                    }
                });
                loadData();
                controlLayout(categories);
            }
        }).start();
    }

    //Phương thức tải dữ liệu từ Shared Preferences
    private void loadSharedPreferences(){
        myPrefs = new MyPrefs(activity);
        language = myPrefs.getLocale();
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
                        controlLayout(categories);

                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                binding.swipeRefreshLayout.setRefreshing(false);
                            }
                        });
                    }
                }).start();
            }
        });

        adapterCategory.setOnItemClickedListener(new AdapterCategory.OnItemClickedListener() {
            @Override
            public void onItemClick(int position) {
                activity.setChosenCategory(categories.get(position));
                activity.switchFragments(2);
            }
        });
    }

    private void loadData(){
        while (!done) {
            String categoryString = getWebPage("https://soundmeterapi.herokuapp.com/api/categories");

            try{
                if(categoryString != null){
                    JSONObject obj = new JSONObject(categoryString);
                    JSONArray array = obj.getJSONArray("categories");
                    categories = getCategoryListFromJsonData(array);
                }
            }
            catch(JSONException e){
                e.printStackTrace();
            }

            if(activity != null){
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapterCategory.notify(categories);
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

    private void controlLayout(final ArrayList<Category> categories){
        if(activity != null){
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(categories.size() != 0){
                        binding.layoutCategory.setVisibility(View.VISIBLE);
                        binding.layoutNotification.setVisibility(View.INVISIBLE);
                    }
                    else{
                        binding.layoutCategory.setVisibility(View.INVISIBLE);
                        binding.textNotification.setText(R.string.noti_check_network);
                        binding.layoutNotification.setVisibility(View.VISIBLE);
                    }
                    binding.layoutProgress.setVisibility(View.INVISIBLE);
                }
            });
        }
    }

    private ArrayList<Category> getCategoryListFromJsonData(JSONArray categoryArray){
        ArrayList<Category> result = new ArrayList<>();

        for(int i=0; i < categoryArray.length(); i++){
            try{
                JSONObject categoryObject = categoryArray.getJSONObject(i);

                JSONObject titleObject = categoryObject.getJSONObject("title");
                if(titleObject.getString(language).equals("")){
                    language = "en";
                }
                Category category = new Category(categoryObject.getInt("id"), R.drawable.image_music_frame, titleObject.getString(language));
                result.add(category);
            } catch (JSONException e){
                e.printStackTrace();
            }

        }

        return result;
    }
}
