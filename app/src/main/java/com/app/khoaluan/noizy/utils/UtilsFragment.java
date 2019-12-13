package com.app.khoaluan.noizy.utils;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class UtilsFragment {
    /**
     * Put a fragment to FrameLayout
     *
     * @param activity    Activity
     * @param containerId ID of the FrameLayout
     * @param fragment    Fragment to replace
     */
    public static void replace(@NonNull AppCompatActivity activity, @IdRes int containerId, @NonNull Fragment fragment) {
        activity.getSupportFragmentManager()
                .beginTransaction()
                .replace(containerId, fragment)
                .commit();
    }
}
