package android.dbmeter.net.ui.fragment;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.dbmeter.net.R;
import android.dbmeter.net.databinding.FragmentCameraBinding;
import android.dbmeter.net.ui.ActivityCamera;
import android.dbmeter.net.ui.ActivityMain;
import android.dbmeter.net.utils.AppConfig;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

public class FragmentCamera extends Fragment {
    private static boolean isFirstTime = true;

    private ActivityMain activity = (ActivityMain)getActivity();
    private FragmentCameraBinding binding;
    private static String[] PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
    };

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof AppCompatActivity) {
            this.activity = (ActivityMain) context;
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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_camera, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeComponents();
    }

    private void initializeComponents() {
        if (activity != null){
            activity.changeBackgroundColor(R.drawable.color_background);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setEventHandler();
    }

    private void setEventHandler(){
        binding.btnSnap.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                verifyPermissions();
            }
        });
    }

    private void verifyPermissions() {
        int camera_per = ActivityCompat.checkSelfPermission(activity, Manifest.permission.CAMERA);
        int loc_fine_per = ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION);
        int loc_coarse_per = ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION);

        // Check for permission with device running Android 6 M and later
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (camera_per != PackageManager.PERMISSION_GRANTED || loc_fine_per != PackageManager.PERMISSION_GRANTED || loc_coarse_per != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(PERMISSIONS, AppConfig.REQUEST_CODE);
            }
            else {
                locationStatusCheck();
            }
        }
        else {
            locationStatusCheck();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == AppConfig.REQUEST_CODE) {
            if(checkPermissionsGranted(permissions, grantResults)) {
                locationStatusCheck();
            }
            else{
                Toast.makeText(activity, R.string.activity_cameraStartErr, Toast.LENGTH_LONG).show();
            }
        }
    }

    private boolean checkPermissionsGranted(@NonNull String[] permissions, @NonNull int[] grantResults){
        if(grantResults.length > 0){
            for (int i = 0; i < permissions.length; i++) {
                int grantResult = grantResults[i];

                if (grantResult != PackageManager.PERMISSION_GRANTED){
                    return false;
                }
            }
        }
        else {
            return false;
        }

        return true;
    }

    private void locationStatusCheck() {
        if (isFirstTime){
            isFirstTime = false;

            LocationManager manager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
            if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                enableLocationService();
            }
            else{
                startActivity(new Intent(activity, ActivityCamera.class));
            }
        }
        else{
            startActivity(new Intent(activity, ActivityCamera.class));
        }
    }

    private void enableLocationService() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage(R.string.activity_snap_location_on);
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.activity_confirm, new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialogInterface, int id) {
                startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        });
        builder.setNegativeButton(R.string.activity_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int id) {
                dialogInterface.dismiss();
                startActivity(new Intent(activity, ActivityCamera.class));
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
