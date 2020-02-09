package android.dbmeter.net.ui;

import android.dbmeter.net.utils.UtilsActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import android.dbmeter.net.R;
import android.dbmeter.net.databinding.ActivityImagePreviewBinding;

import java.io.File;
import java.util.List;

import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.databinding.DataBindingUtil;

public class ActivityImagePreview extends AppCompatActivity {
    private ActivityImagePreviewBinding binding;

    String imagePath = "";
    File image;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeComponents();
    }

    private void initializeComponents(){
        UtilsActivity.enterFullScreen(ActivityImagePreview.this);
        @LayoutRes int layoutId = R.layout.activity_image_preview;
        setContentView(layoutId);
        binding = DataBindingUtil.setContentView(this, layoutId);

        setSupportActionBar(binding.toolbar.toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setTitle("");

        loadImage();

        setEventHandler();
    }

    private void loadImage() {
        Intent intent = getIntent();
        imagePath = intent.getStringExtra("path");
        image = new File(imagePath);
        binding.viewImage.setImageURI(Uri.fromFile(image));
    }

    private void setEventHandler() {
        binding.toolbar.btnBack.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
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

    private void deleteImage(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.title_image_delete);
        builder.setMessage(R.string.activity_delete_image_confirm);
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.activity_delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (image.exists()) {
                    image.delete();
                    Toast.makeText(getBaseContext(), R.string.noti_image_delete, Toast.LENGTH_SHORT).show();
                    finish();
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
        Uri imageUri = FileProvider.getUriForFile(ActivityImagePreview.this,"android.dbmeter.net.provider",image);
        shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
        shareIntent.setType("image/*");
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        Intent chooser = Intent.createChooser(shareIntent, "#SoundMeter");

        List<ResolveInfo> resInfoList = this.getPackageManager().queryIntentActivities(chooser, PackageManager.MATCH_DEFAULT_ONLY);

        for (ResolveInfo resolveInfo : resInfoList) {
            String packageName = resolveInfo.activityInfo.packageName;
            this.grantUriPermission(packageName, imageUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }

        startActivity(chooser);
    }
}
