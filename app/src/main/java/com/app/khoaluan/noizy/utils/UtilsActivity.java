package com.app.khoaluan.noizy.utils;

import android.graphics.Color;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

/**
 * The UtilsActivity class provides method to enter full screen activity.
 */
public class UtilsActivity {
    /**
     * Hide Android status bar for fullscreen activity
     *
     * @param activity Activity
     */
    public static void enterFullScreen(@NonNull AppCompatActivity activity) {
        activity.getWindow().getDecorView();
        activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }
}
