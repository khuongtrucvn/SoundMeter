package com.app.khoaluan.noizy.ui;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.app.khoaluan.noizy.R;
import com.app.khoaluan.noizy.databinding.ActivityMainBinding;
import com.app.khoaluan.noizy.ui.fragment.FragmentHistory;
import com.app.khoaluan.noizy.ui.fragment.FragmentMeter;
import com.app.khoaluan.noizy.ui.fragment.FragmentSettings;
import com.app.khoaluan.noizy.ui.fragment.FragmentShare;
import com.app.khoaluan.noizy.utils.UtilsActivity;
import com.app.khoaluan.noizy.utils.UtilsFragment;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.databinding.DataBindingUtil;

public class ActivityMain extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private static final int REQUEST_CODE = 1;
    private static String[] PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO
    };

    private ActivityMainBinding binding;
    private int currentFragmentId;
    private ActionBarDrawerToggle toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UtilsActivity.enterFullScreen(ActivityMain.this);

        @LayoutRes int layoutId = R.layout.activity_main;
        setContentView(layoutId);
        binding = DataBindingUtil.setContentView(this, layoutId);
        binding.navView.getMenu().getItem(0).setChecked(true);
        switchFragments(R.id.nav_meter);
        setEventHandler();
    }

    private void setEventHandler() {
        setSupportActionBar(binding.toolbar.toolbar);

        binding.toolbar.btnHamburger.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                binding.drawer.openDrawer(binding.navView);
            }
        });

        toggle = new ActionBarDrawerToggle(this, binding.drawer,R.string.open,R.string.close);

        binding.drawer.addDrawerListener(toggle);
        toggle.syncState();

        binding.navView.setNavigationItemSelectedListener(this);
    }

    private void switchFragments(int fragmentId) {
        @IdRes int frameId = R.id.content_frame;

        if (fragmentId != currentFragmentId) {
            switch (fragmentId) {
                case R.id.nav_meter: {
                    binding.toolbar.textTitle.setText(R.string.title_soundmeter);
                    UtilsFragment.replace(this, frameId, new FragmentMeter());
                    break;
                }
                case R.id.nav_history: {
                    binding.toolbar.textTitle.setText(R.string.title_history);
                    UtilsFragment.replace(this, frameId, new FragmentHistory());
                    break;
                }
                case R.id.nav_share: {
                    binding.toolbar.textTitle.setText(R.string.title_share);
                    UtilsFragment.replace(this, frameId, new FragmentShare());
                    break;
                }
                case R.id.nav_settings: {
                    binding.toolbar.textTitle.setText(R.string.title_setting);
                    UtilsFragment.replace(this, frameId, new FragmentSettings());
                    break;
                }
            }
            currentFragmentId = fragmentId;
        }
        binding.drawer.closeDrawer(GravityCompat.START);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int fragmentId = item.getItemId();
        switchFragments(fragmentId);
        return true;
    }

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int storage_per = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int audio_per = ActivityCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO);


        if (storage_per != PackageManager.PERMISSION_GRANTED && audio_per != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS,
                    REQUEST_CODE
            );
        }
    }
}
