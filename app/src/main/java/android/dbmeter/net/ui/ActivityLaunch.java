package android.dbmeter.net.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.dbmeter.net.R;
import android.dbmeter.net.database.LocaleDescriptionDatabase;
import android.dbmeter.net.model.Global;
import android.dbmeter.net.model.MyPrefs;
import android.dbmeter.net.utils.AppConfig;
import android.os.Build;
import android.os.Bundle;
import android.os.LocaleList;
import android.util.Log;

import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class ActivityLaunch extends AppCompatActivity {
    private static String[] PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO
    };

    private MyPrefs myPrefs;
    private String localeCode;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        verifyPermissions();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == AppConfig.REQUEST_CODE) {
            if(checkPermissionsGranted(permissions, grantResults))
                initializeComponents();
            else
                finish();
        }
    }

    private void initializeComponents() {
        loadSharedPreferences();

        //Nếu ứng dụng lần đầu sử dụng (không có lưu Ngôn ngữ trong Shared Pref)
        if(localeCode.equals("")){
            saveAppLocale();
        }

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
                ActivityCompat.requestPermissions(this, PERMISSIONS, AppConfig.REQUEST_CODE);
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

    //Phương thức tải dữ liệu từ Shared Preferences
    public void loadSharedPreferences(){
        myPrefs = new MyPrefs(this);
        localeCode =  myPrefs.getLocale();
    }

    private String getDeviceLocale(){
        String currentLocale;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            currentLocale = LocaleList.getDefault().get(0).getLanguage();
        } else{
            currentLocale =  Locale.getDefault().getLanguage();
        }

        return currentLocale;
    }

    private void saveAppLocale(){
        //Khởi tạo data locale
        LocaleDescriptionDatabase.get(this);

        //Nếu ứng dụng không hỗ trợ ngôn ngữ của thiết bị đang sử dụng thì ứng dụng sẽ sử dụng ngôn ngữ mặc định là tiếng anh
        if(LocaleDescriptionDatabase.getLocaleNameFromCode(getDeviceLocale()).equals("")){
            localeCode = getString(R.string.locale_en);
        }
        else{
            localeCode = getDeviceLocale();
        }

        myPrefs.setLocale(localeCode);
    }
}
