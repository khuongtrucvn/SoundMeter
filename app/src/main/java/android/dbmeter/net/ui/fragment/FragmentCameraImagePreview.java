package android.dbmeter.net.ui.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.dbmeter.net.R;
import android.dbmeter.net.databinding.FragmentCameraImagePreviewBinding;
import android.dbmeter.net.ui.ActivityCamera;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.io.File;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

public class FragmentCameraImagePreview extends Fragment {
    private ActivityCamera activity = (ActivityCamera)getActivity();
    private FragmentCameraImagePreviewBinding binding;

    private File image;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof AppCompatActivity) {
            this.activity = (ActivityCamera) context;
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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_camera_image_preview, container, false);
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
        activity.setSupportActionBar(binding.toolbar.toolbar);
        activity.getSupportActionBar().setDisplayShowTitleEnabled(false);
        activity.getSupportActionBar().setTitle("");

        loadImage();
    }

    private void setEventHandler(){
        binding.toolbar.btnBack.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                activity.switchFragments(1);
            }
        });

        binding.btnShare.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                shareImage();
            }
        });

        binding.btnDelete.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                deleteImage();
            }
        });
    }

    private void loadImage() {
        /*Intent intent = activity.getIntent();
        imagePath = intent.getStringExtra("path");*/

        String imagePath = activity.getImagePath();
        image = new File(imagePath);
        binding.viewImage.setImageURI(Uri.fromFile(image));
    }

    private void deleteImage(){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(R.string.title_image_delete);
        builder.setMessage(R.string.activity_delete_image_confirm);
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.activity_delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (image.exists()) {
                    image.delete();
                    Toast.makeText(activity, R.string.noti_image_delete, Toast.LENGTH_SHORT).show();
                    activity.switchFragments(1);
                }
            }
        });
        builder.setNegativeButton(R.string.activity_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void shareImage(){
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        Uri imageUri = FileProvider.getUriForFile(activity,"android.dbmeter.net.provider",image);
        shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
        shareIntent.setType("image/*");
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        Intent chooser = Intent.createChooser(shareIntent, "#SoundMeter");

        List<ResolveInfo> resInfoList = activity.getPackageManager().queryIntentActivities(chooser, PackageManager.MATCH_DEFAULT_ONLY);

        for (ResolveInfo resolveInfo : resInfoList) {
            String packageName = resolveInfo.activityInfo.packageName;
            activity.grantUriPermission(packageName, imageUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }

        startActivity(chooser);
    }
}
