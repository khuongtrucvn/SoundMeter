package android.dbmeter.net.utils;

import android.graphics.Color;
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
        activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        activity.getWindow().setStatusBarColor(Color.TRANSPARENT);
        activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }
}
