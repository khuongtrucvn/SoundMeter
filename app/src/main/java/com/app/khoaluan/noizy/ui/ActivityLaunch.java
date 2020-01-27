package com.app.khoaluan.noizy.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import com.app.khoaluan.noizy.utils.AppConfig;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import static com.app.khoaluan.noizy.utils.AppConfig.REQUEST_CODE;

public class ActivityLaunch extends AppCompatActivity {
    private static String[] PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        verifyPermissions();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if(checkPermissionsGranted(permissions, grantResults))
                initializeComponents();
            else
                finish();
        }
    }

    private void initializeComponents() {
        try {
            Thread.sleep(AppConfig.SPLASH_TIME);
            startActivity(new Intent(this, ActivityMain.class));
            finish();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void verifyPermissions() {
        int write_per = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int read_per = ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        int audio_per = ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);

        // Check for permission with device running Android 6 M and later
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (read_per != PackageManager.PERMISSION_GRANTED || write_per != PackageManager.PERMISSION_GRANTED || audio_per != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_CODE);
            }
            else {
                initializeComponents();
            }
        }
        else {
            initializeComponents();
        }
    }

    private boolean checkPermissionsGranted(@NonNull String[] permissions, @NonNull int[] grantResults){
        if(grantResults.length > 0){
            for (int i = 0; i < permissions.length; i++) {
                int grantResult = grantResults[i];

                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        else{
            return false;
        }

        return true;
    }
}
