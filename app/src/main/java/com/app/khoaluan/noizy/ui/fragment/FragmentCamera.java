package com.app.khoaluan.noizy.ui.fragment;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Image;
import android.media.ImageReader;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.app.khoaluan.noizy.R;
import com.app.khoaluan.noizy.databinding.FragmentCameraBinding;
import com.app.khoaluan.noizy.model.Global;
import com.app.khoaluan.noizy.ui.ActivityImagePreview;
import com.app.khoaluan.noizy.ui.ActivityMain;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

public class FragmentCamera extends Fragment implements LocationListener {
    private ActivityMain activity = (ActivityMain)getActivity();
    private FragmentCameraBinding binding;

    // kiểm tra trạng thái  ORIENTATION của ảnh đầu ra
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    private String cameraId;
    private CameraDevice cameraDevice;
    private CameraCaptureSession cameraCaptureSessions;
    private CaptureRequest.Builder captureRequestBuilder;

    private Size imageDimension;
    private ImageReader reader;
    private File imageFile;
    private String currentPhotoPath;
    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;
    private Bitmap bitmapImage;

    private String resultAddress = "";

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

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setEventHandler();
    }

    private void initializeComponents(){
        activity.restartRecorder();
        assert binding.viewPicture != null;
        binding.viewPicture.setSurfaceTextureListener(textureListener);
        assert binding.btnSnap != null;

        getAddressFromLocation();
    }

    private void setEventHandler(){
        binding.btnSnap.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    takePicture();
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void takePicture() throws CameraAccessException{
        if(cameraDevice == null) {
            return;
        }

        CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);

        CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraDevice.getId());
        Size[] jpegSizes = null;
        jpegSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(ImageFormat.JPEG);

        int width = 640;
        int height = 480;

        if (jpegSizes != null && 0 < jpegSizes.length) {
            width = jpegSizes[0].getWidth();
            height = jpegSizes[0].getHeight();
        }

        reader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1);
        List<Surface> outputSurfaces = new ArrayList<>(2);
        outputSurfaces.add(reader.getSurface());
        outputSurfaces.add(new Surface(binding.viewPicture.getSurfaceTexture()));

        final CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
        captureBuilder.addTarget(reader.getSurface());
        captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);

        // kiểm tra orientation tuỳ thuộc vào mỗi device khác nhau
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));

        try {
            imageFile = createImageFile();

            ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                    Image image = reader.acquireLatestImage();
                    ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                    byte[] bytes = new byte[buffer.capacity()];
                    buffer.get(bytes);

                    try {
                        bitmapImage = addInformation(bytes);
                        save(bitmapImage);
                        previewImage(currentPhotoPath);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (image != null) {
                            image.close();
                        }
                    }
                }
            };

            reader.setOnImageAvailableListener(readerListener, mBackgroundHandler);

            final CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session,@NonNull CaptureRequest request, TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);

                    Toast.makeText(activity, "Saved:" + imageFile, Toast.LENGTH_LONG).show();

                    try {
                        createCameraPreview();
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }
            };

            cameraDevice.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    try {
                        session.capture(captureBuilder.build(), captureListener, mBackgroundHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }
                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) { }
            }, mBackgroundHandler);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String imageFileName = "SoundMeter_" + timeStamp;
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/SoundMeter/");
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }

        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private Bitmap addInformation(byte[] data){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inMutable = true;
        Bitmap myBitmap = rotate(BitmapFactory.decodeByteArray(data, 0, data.length, options),90);
        Canvas canvas = new Canvas(myBitmap);

        Paint valuePaint = new Paint();
        valuePaint.setTextSize(150);
        valuePaint.setAntiAlias(true);
        valuePaint.setTextAlign(Paint.Align.CENTER);
        valuePaint.setColor(Color.WHITE);

        Paint addressPaint = new Paint();
        addressPaint.setTextSize(90);
        addressPaint.setAntiAlias(true);
        addressPaint.setTextAlign(Paint.Align.CENTER);
        addressPaint.setColor(Color.WHITE);

        Paint framePaint = new Paint();
        framePaint.setAntiAlias(true);
        framePaint.setColor(ContextCompat.getColor(activity,R.color.gradient_black_50));

        Locale currentLocale = Locale.getDefault();
        DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(currentLocale);
        otherSymbols.setDecimalSeparator('.');
        DecimalFormat df1 = new DecimalFormat("##0.0", otherSymbols);

        canvas.drawRect(0,myBitmap.getHeight()*21/50,myBitmap.getWidth(),myBitmap.getHeight()*3/5,framePaint);
        canvas.drawText(getString(R.string.activity_avg_value) + getString(R.string.colon) + " " + df1.format(Global.avgDb)+ getString(R.string.sound_unit), myBitmap.getWidth()/2,myBitmap.getHeight()/2, valuePaint);
        canvas.drawText(resultAddress, myBitmap.getWidth()/2,myBitmap.getHeight()*11/20, addressPaint);

        return myBitmap;
    }

    private Bitmap rotate(Bitmap src, float degrees) {
        Matrix matrix = new Matrix();
        matrix.preRotate(degrees);
        return Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true);
    }

    // Lưu ảnh
    private void save(Bitmap image) throws IOException {
        FileOutputStream output = new FileOutputStream(imageFile);

        image.compress(Bitmap.CompressFormat.JPEG, 100, output);
        output.flush();
        output.close();

        getGalleryUpdate(imageFile);
    }

    private void previewImage(String filePath){
        Intent intent = new Intent(activity, ActivityImagePreview.class);
        intent.putExtra("path", filePath);
        startActivity(intent);
    }

    private TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            try {
                openCamera();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) { }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) { }
    };

    private void openCamera() throws CameraAccessException {
        CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);

        cameraId = manager.getCameraIdList()[0];
        CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
        StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        imageDimension = map.getOutputSizes(SurfaceTexture.class)[0];

        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            manager.openCamera(cameraId, stateCallback, null);
        }
    }

    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            cameraDevice = camera;
            try {
                createCameraPreview();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            cameraDevice.close();
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            cameraDevice.close();
            cameraDevice = null;
        }
    };

    // Khởi tạo camera để preview trong textureview
    private void createCameraPreview() throws CameraAccessException{
        SurfaceTexture texture = binding.viewPicture.getSurfaceTexture();
        texture.setDefaultBufferSize(imageDimension.getWidth(), imageDimension.getHeight());
        Surface surface = new Surface(texture);
        captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
        captureRequestBuilder.addTarget(surface);
        cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback(){
            @Override
            public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                if (cameraDevice == null) {
                    return;
                }

                // When the session is ready, we start displaying the preview.
                cameraCaptureSessions = cameraCaptureSession;
                try {
                    updatePreview();
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                Toast.makeText(activity, "Configuration Changed", Toast.LENGTH_SHORT).show();
            }
        }, null);
    }

    private void updatePreview() throws CameraAccessException {
        if(cameraDevice == null) {
            return;
        }
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), null, mBackgroundHandler);
    }

    @Override
    public void onResume() {
        super.onResume();
        startBackgroundThread();
        if (binding.viewPicture.isAvailable()) {
            try {
                openCamera();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
        else {
            binding.viewPicture.setSurfaceTextureListener(textureListener);
        }
    }

    @Override
    public void onPause() {
        try {
            stopBackgroundThread();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        super.onPause();
    }

    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("Camera Background");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    private void stopBackgroundThread() throws InterruptedException{
        mBackgroundThread.quitSafely();

        mBackgroundThread.join();
        mBackgroundThread = null;
        mBackgroundHandler = null;
    }

    private void getAddressFromLocation() {
        resultAddress = getString(R.string.activity_unidentified_location);

        LocationManager locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        Geocoder geocoder = new Geocoder(activity, Locale.getDefault());
        try {
            List<Address> list = geocoder.getFromLocation(
                    location.getLatitude(), location.getLongitude(), 1);
            if (list != null && list.size() > 0) {
                Address address = list.get(0);
                resultAddress = address.getAddressLine(0);
            }
        }
        catch (IOException e) {
            Log.e("Error", "Impossible to connect to Geocoder", e);
        }
    }

    @Override
    public void onProviderDisabled(String provider) { }

    @Override
    public void onProviderEnabled(String provider) { }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) { }

    private void getGalleryUpdate(File image){
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri imageUri = FileProvider.getUriForFile(activity,"android.dbmeter.net.provider",image);
        mediaScanIntent.setData(imageUri);
        activity.sendBroadcast(mediaScanIntent);
    }
}
