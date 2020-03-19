package android.dbmeter.net.ui.fragment;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.dbmeter.net.R;
import android.dbmeter.net.databinding.FragmentCameraImageCaptureBinding;
import android.dbmeter.net.model.Global;
import android.dbmeter.net.model.OrientationManager;
import android.dbmeter.net.ui.ActivityCamera;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

public class FragmentCameraImageCapture extends Fragment implements OrientationManager.OrientationListener, LocationListener {
    private ActivityCamera activity = (ActivityCamera)getActivity();
    private FragmentCameraImageCaptureBinding binding;

    private static final int STATE_PREVIEW = 0;                 //Trạng thái máy ảnh hiện góc chụp thu được
    private static final int STATE_WAITING_LOCK = 1;            //Trạng thái máy ảnh chờ khoá focus
    private static final int STATE_WAITING_PRECAPTURE = 2;      //Trạng thái máy ảnh trạng thái chờ phơi sáng trước khi chụp
    private static final int STATE_WAITING_NON_PRECAPTURE = 3;  //Trạng thái máy ảnh chờ phơi sáng khác với trạng thái trước khi chụp
    private static final int STATE_PICTURE_TAKEN = 4;           //Trạng thái máy ảnh đã chụp ảnh

    private static final int MAX_PREVIEW_WIDTH = 1920;
    private static final int MAX_PREVIEW_HEIGHT = 1080;

    /* Camera */
    private String cameraId;
    private CameraDevice cameraDevice;
    private CameraCaptureSession cameraCaptureSession;
    private CaptureRequest captureRequest;
    private CaptureRequest.Builder captureRequestBuilder;

    private boolean flashSupported;

    /**
     * A {@link Semaphore} to prevent the app from exiting before closing the camera.
     */
    private Semaphore cameraOpenCloseLock = new Semaphore(1);

    /**
     * The current state of camera state for taking pictures.
     */
    private int mState = STATE_PREVIEW;

    private Size previewSize;
    private ImageReader reader;
    private String currentPhotoPath;
    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;
    private Bitmap bitmapImage;
    private int rotationDegrees = 90;

    /* Image file */
    private File imageFile;

    /* Geocoder */
    private String resultAddress = "";
    private Geocoder geocoder;

    /* Orientation */
    private OrientationManager orientationManager;

    /* Canvas position */
    //Thông số ban đầu mặc định điện thoại ở dạng portrait
    private float frameStartHeightRatio = (float)21/50;
    private float frameEndHeightRatio = (float)3/5;
    private float valueWidthRatio = (float)1/2;
    private float valueHeightRatio = (float)1/2;
    private float addressWidthRatio = (float)1/2;
    private float addressHeightRatio = (float)11/20;

    private CameraCaptureSession.CaptureCallback captureCallback = new CameraCaptureSession.CaptureCallback() {
        private void process(CaptureResult result) {
            switch (mState) {
                case STATE_PREVIEW: {
                    break;
                }
                case STATE_WAITING_LOCK: {
                    Integer afState = result.get(CaptureResult.CONTROL_AF_STATE);
                    if (afState == null) {
                        captureStillPicture();
                    } else if (CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED == afState ||
                            CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED == afState) {
                        Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                        if (aeState == null ||
                                aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED) {
                            mState = STATE_PICTURE_TAKEN;
                            captureStillPicture();
                        } else {
                            runPrecaptureSequence();
                        }
                    }
                    break;
                }
                case STATE_WAITING_PRECAPTURE: {
                    Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                    if (aeState == null ||
                            aeState == CaptureResult.CONTROL_AE_STATE_PRECAPTURE ||
                            aeState == CaptureRequest.CONTROL_AE_STATE_FLASH_REQUIRED) {
                        mState = STATE_WAITING_NON_PRECAPTURE;
                    }
                    break;
                }
                case STATE_WAITING_NON_PRECAPTURE: {
                    Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                    if (aeState == null || aeState != CaptureResult.CONTROL_AE_STATE_PRECAPTURE) {
                        mState = STATE_PICTURE_TAKEN;
                        captureStillPicture();
                    }
                    break;
                }
            }
        }

        @Override
        public void onCaptureProgressed(@NonNull CameraCaptureSession session,
                                        @NonNull CaptureRequest request,
                                        @NonNull CaptureResult partialResult) {
            process(partialResult);
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                       @NonNull CaptureRequest request,
                                       @NonNull TotalCaptureResult result) {
            process(result);
        }

    };


    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            cameraOpenCloseLock.release();
            cameraDevice = camera;
            createCameraPreview();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            cameraOpenCloseLock.release();
            cameraDevice.close();
            cameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            cameraOpenCloseLock.release();
            cameraDevice.close();
            cameraDevice = null;
            if (null != activity) {
                activity.finish();
            }
        }
    };

    private final TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {
            openCamera(width, height);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int width, int height) {
            configureTransform(width, height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture texture) {
        }

    };

    private ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            Image image = reader.acquireLatestImage();
            ByteBuffer buffer = image.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.capacity()];
            buffer.get(bytes);

            try {
                imageFile = createImageFile();
                bitmapImage = addInformation(bytes);
                save(bitmapImage);
                previewImage();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            finally {
                image.close();
            }
        }
    };

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof AppCompatActivity) {
            this.activity = (ActivityCamera) context;
            geocoder = new Geocoder(getContext(), Locale.getDefault());
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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_camera_image_capture, container, false);
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

        activity.setSupportActionBar(binding.toolbar.toolbar);
        activity.getSupportActionBar().setDisplayShowTitleEnabled(false);
        activity.getSupportActionBar().setTitle("");

        orientationManager = new OrientationManager(activity, SensorManager.SENSOR_DELAY_NORMAL, this);
        orientationManager.enable();

        getAddressFromLocation();
    }

    private void setEventHandler(){
        binding.btnSnap.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                takePicture();
            }
        });

        binding.toolbar.btnBack.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                activity.finish();
            }
        });
    }

    private void takePicture(){
        lockFocus();
    }

    private void lockFocus() {
        try {
            captureRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,
                    CameraMetadata.CONTROL_AF_TRIGGER_START);
            mState = STATE_WAITING_LOCK;
            cameraCaptureSession.capture(captureRequestBuilder.build(), captureCallback,
                    mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void unlockFocus() {
        try {
            captureRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,
                    CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);
            cameraCaptureSession.capture(captureRequestBuilder.build(), captureCallback,
                    mBackgroundHandler);
            mState = STATE_PREVIEW;
            cameraCaptureSession.setRepeatingRequest(captureRequest, captureCallback,
                    mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void captureStillPicture() {
        try {
            if (null == activity || null == cameraDevice) {
                return;
            }

            final CaptureRequest.Builder captureBuilder =
                    cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(reader.getSurface());

            captureBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);

            CameraCaptureSession.CaptureCallback CaptureCallback
                    = new CameraCaptureSession.CaptureCallback() {

                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                               @NonNull CaptureRequest request,
                                               @NonNull TotalCaptureResult result) {
                    unlockFocus();
                }
            };

            cameraCaptureSession.stopRepeating();
            cameraCaptureSession.abortCaptures();
            cameraCaptureSession.capture(captureBuilder.build(), CaptureCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void runPrecaptureSequence() {
        try {
            captureRequestBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER,
                    CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START);
            mState = STATE_WAITING_PRECAPTURE;
            cameraCaptureSession.capture(captureRequestBuilder.build(), captureCallback,
                    mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String imageFileName = "SoundMeter_" + timeStamp;
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/SoundMeter/");
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }

        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private Bitmap addInformation(byte[] data){
        Bitmap myBitmap = convertAndRotateBitmap(data, rotationDegrees);
        Canvas canvas = new Canvas(myBitmap);

        Locale currentLocale = Locale.getDefault();
        DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(currentLocale);
        otherSymbols.setDecimalSeparator('.');
        DecimalFormat df1 = new DecimalFormat("##0.0", otherSymbols);

        String value = getString(R.string.activity_avg_value) + getString(R.string.colon) + " " + df1.format(Global.avgDb)+ getString(R.string.sound_unit);
        Paint valuePaint = new Paint();
        int valueFontSize = determineMaxTextSize(value,(float)myBitmap.getWidth()/2);
        valuePaint.setTextSize(valueFontSize);
        valuePaint.setAntiAlias(true);
        valuePaint.setTextAlign(Paint.Align.CENTER);
        valuePaint.setColor(Color.WHITE);

        Paint addressPaint = new Paint();
        int addressFontSize = determineMaxTextSize(resultAddress,(float)myBitmap.getWidth()*4/5);
        addressPaint.setTextSize(addressFontSize);
        addressPaint.setAntiAlias(true);
        addressPaint.setTextAlign(Paint.Align.CENTER);
        addressPaint.setColor(Color.WHITE);

        Paint framePaint = new Paint();
        framePaint.setAntiAlias(true);
        framePaint.setColor(ContextCompat.getColor(activity,R.color.gradient_black_50));

        canvas.drawRect(0,myBitmap.getHeight()*frameStartHeightRatio,
                myBitmap.getWidth(), myBitmap.getHeight()*frameEndHeightRatio, framePaint);
        canvas.drawText(value, myBitmap.getWidth()*valueWidthRatio,myBitmap.getHeight()*valueHeightRatio, valuePaint);
        canvas.drawText(resultAddress, myBitmap.getWidth()*addressWidthRatio,myBitmap.getHeight()*addressHeightRatio, addressPaint);

        return myBitmap;
    }

    private int determineMaxTextSize(String str, float maxWidth) {
        int size = 0;
        Paint paint = new Paint();

        do {
            paint.setTextSize(++ size);
        }
        while(paint.measureText(str) < maxWidth);

        //Không để size vượt quá 160
        if(size > 160){
            size = 160;
        }

        return size;
    }

    private Bitmap convertAndRotateBitmap (byte[] data, int degrees){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inMutable = true;

        boolean done = false;
        int downsampleBy = 1;

        Bitmap myBitmap = null;
        while (!done) {
            options.inSampleSize = downsampleBy++;
            try {
                myBitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);
                myBitmap = rotate(myBitmap, degrees);
                done = true;
            } catch (OutOfMemoryError e) {
                e.printStackTrace();
            }
        }

        return myBitmap;
    }

    private Bitmap rotate(Bitmap src, float degrees) {
        Matrix matrix = new Matrix();
        matrix.preRotate(degrees);
        return Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, false);
    }

    // Lưu ảnh
    private void save(Bitmap image) throws IOException {
        FileOutputStream output = new FileOutputStream(imageFile);

        image.compress(Bitmap.CompressFormat.JPEG, 90, output);
        output.flush();
        output.close();

        getGalleryUpdate(imageFile);
    }

    private void previewImage(){
        activity.setImagePath(currentPhotoPath);
        activity.switchFragments(2);
    }

    private void openCamera(int width, int height) {
        setUpCameraOutputs(width, height);
        configureTransform(width, height);
        CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
        try {
            if (!cameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }

            if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                manager.openCamera(cameraId, stateCallback, null);
            }
        }
        catch (CameraAccessException e) {
            e.printStackTrace();
        }
        catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera opening.", e);
        }
    }

    //cài đặt Textture View
    private void configureTransform(int viewWidth, int viewHeight) {
        if (null == previewSize || null == activity) {
            return;
        }
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        RectF bufferRect = new RectF(0, 0, previewSize.getHeight(), previewSize.getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max(
                    (float) viewHeight / previewSize.getHeight(),
                    (float) viewWidth / previewSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        } else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180, centerX, centerY);
        }
        binding.textureView.setTransform(matrix);
    }

    //Cài đặt thông số của máy ảnh
    private void setUpCameraOutputs(int width, int height) {
        CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
        try {
            for (String camera : manager.getCameraIdList()) {
                CameraCharacteristics characteristics
                        = manager.getCameraCharacteristics(camera);

                // We don't use a front facing camera in this sample.
                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
                    continue;
                }

                StreamConfigurationMap map = characteristics.get(
                        CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                if (map == null) {
                    continue;
                }

                Size largest = Collections.max(
                        Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)),
                        new CompareSizesByArea());
                reader = ImageReader.newInstance(largest.getWidth(), largest.getHeight(),
                        ImageFormat.JPEG, 1);
                reader.setOnImageAvailableListener(
                        readerListener, mBackgroundHandler);

                Point displaySize = new Point();
                activity.getWindowManager().getDefaultDisplay().getSize(displaySize);
                int maxPreviewWidth = displaySize.x;
                int maxPreviewHeight = displaySize.y;

                if (maxPreviewWidth > MAX_PREVIEW_WIDTH) {
                    maxPreviewWidth = MAX_PREVIEW_WIDTH;
                }

                if (maxPreviewHeight > MAX_PREVIEW_HEIGHT) {
                    maxPreviewHeight = MAX_PREVIEW_HEIGHT;
                }


                previewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class),
                        width, height, maxPreviewWidth, maxPreviewHeight, largest);

                int orientation = getResources().getConfiguration().orientation;
                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    binding.textureView.setAspectRatio(previewSize.getWidth(), previewSize.getHeight());
                }
                else {
                    binding.textureView.setAspectRatio(previewSize.getHeight(), previewSize.getWidth());
                }

                cameraId = camera;
                return;
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    //Hàm chọn độ phân giải thích hợp
    private static Size chooseOptimalSize(Size[] choices, int textureViewWidth, int textureViewHeight, int maxWidth, int maxHeight, Size aspectRatio) {

        List<Size> bigEnough = new ArrayList<>();
        List<Size> notBigEnough = new ArrayList<>();
        int w = aspectRatio.getWidth();
        int h = aspectRatio.getHeight();
        for (Size option : choices) {
            if (option.getWidth() <= maxWidth && option.getHeight() <= maxHeight &&
                    option.getHeight() == option.getWidth() * h / w) {
                if (option.getWidth() >= textureViewWidth &&
                        option.getHeight() >= textureViewHeight) {
                    bigEnough.add(option);
                } else {
                    notBigEnough.add(option);
                }
            }
        }

        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizesByArea());
        }
        else if (notBigEnough.size() > 0) {
            return Collections.max(notBigEnough, new CompareSizesByArea());
        }
        else {
            Log.e("TAG", "Couldn't find any suitable preview size");
            return choices[0];
        }
    }

    private void createCameraPreview(){
        try {
            SurfaceTexture texture = binding.textureView.getSurfaceTexture();
            assert texture != null;

            texture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());
            Surface surface = new Surface(texture);
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);

            cameraDevice.createCaptureSession(Arrays.asList(surface, reader.getSurface()),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession session) {
                            if (null == cameraDevice) {
                                return;
                            }

                            cameraCaptureSession = session;
                            try {
                                captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                                        CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);

                                captureRequest = captureRequestBuilder.build();
                                cameraCaptureSession.setRepeatingRequest(captureRequest,
                                        captureCallback, mBackgroundHandler);
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) { }
                    },null
            );
        }
        catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        startBackgroundThread();

        // When the screen is turned off and turned back on, the SurfaceTexture is already
        // available, and "onSurfaceTextureAvailable" will not be called. In that case, we can open
        // a camera and start preview from here (otherwise, we wait until the surface is ready in
        // the SurfaceTextureListener).
        if (binding.textureView.isAvailable()) {
            openCamera(binding.textureView.getWidth(),binding.textureView.getHeight());
        }
        else {
            binding.textureView.setSurfaceTextureListener(textureListener);
        }
    }

    @Override
    public void onPause() {
        stopBackgroundThread();
        closeCamera();
        super.onPause();
    }

    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    private void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void closeCamera() {
        try {
            cameraOpenCloseLock.acquire();
            if (null != cameraCaptureSession) {
                cameraCaptureSession.close();
                cameraCaptureSession = null;
            }
            if (null != cameraDevice) {
                cameraDevice.close();
                cameraDevice = null;
            }
            if (null != reader) {
                reader.close();
                reader = null;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.", e);
        } finally {
            cameraOpenCloseLock.release();
        }
    }

    private void setAutoFlash(CaptureRequest.Builder requestBuilder) {
        if (flashSupported) {
            requestBuilder.set(CaptureRequest.CONTROL_AE_MODE,
                    CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
        }
    }

    static class CompareSizesByArea implements Comparator<Size> {

        @Override
        public int compare(Size lhs, Size rhs) {
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                    (long) rhs.getWidth() * rhs.getHeight());
        }

    }

    private void getAddressFromLocation() {
        resultAddress = getString(R.string.activity_unidentified_location);

        LocationManager locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
            }
            else if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
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

    @Override
    public void onOrientationChange(OrientationManager.ScreenOrientation screenOrientation) {
        switch(screenOrientation){
            case PORTRAIT:
                Log.w("Orientation","Portrait");
                rotationDegrees = 90;
                frameStartHeightRatio = (float)21/50;
                frameEndHeightRatio = (float)3/5;
                addressHeightRatio = (float)11/20;

                break;
            case REVERSED_PORTRAIT:
                Log.w("Orientation","Reverse portrait");
                rotationDegrees = 90;
                frameStartHeightRatio = (float)21/50;
                frameEndHeightRatio = (float)3/5;
                addressHeightRatio = (float)11/20;

                break;
            case LANDSCAPE:
                Log.w("Orientation","Landscape");
                rotationDegrees = 0;
                frameStartHeightRatio = (float)21/50;
                frameEndHeightRatio = (float)13/20;
                addressHeightRatio = (float)3/5;

                break;
            case REVERSED_LANDSCAPE:
                Log.w("Orientation","Reverse landscape");
                rotationDegrees = 180;
                frameStartHeightRatio = (float)21/50;
                frameEndHeightRatio = (float)13/20;
                addressHeightRatio = (float)3/5;

                break;
        }
    }
}
