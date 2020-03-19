package android.dbmeter.net.utils;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

public class UtilsFragment {
    public static void replace(@NonNull AppCompatActivity activity, @IdRes int containerId, @NonNull Fragment fragment) {
        activity.getSupportFragmentManager()
                .beginTransaction()
                .replace(containerId, fragment)
                .commit();
    }
}
