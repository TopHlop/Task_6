package com.example.task_6;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraInfoUnavailableException;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.FocusMeteringAction;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.MeteringPoint;
import androidx.camera.core.MeteringPointFactory;
import androidx.camera.core.Preview;
import androidx.camera.core.SurfaceOrientedMeteringPointFactory;
import androidx.camera.extensions.HdrImageCaptureExtender;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import android.content.res.Configuration;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.example.task_6.databinding.ActivityMainBinding;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_PERMISSIONS = 101;
    private static final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA",
            "android.permission.WRITE_EXTERNAL_STORAGE"};
    public static final String TEMP_IMAGE = "temp.jpg";

    private ProcessCameraProvider cameraProvider;
    private Camera camera;
    private ImageCapture imageCapture;

    private ActivityMainBinding binding;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        CameraSharedPreferences.loadSettings(this);

        if (allPermissionsGranted()) {
            startCamera();
            initializeViews();
            setGravityButtonDependingOnOrientation();
        } else {
            ActivityCompat.requestPermissions(this,
                    REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }

        binding.cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePicture();
            }
        });

        binding.cameraViewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (CameraSharedPreferences.isBackCamera()) {
                    CameraSharedPreferences.setIsBackCamera(false);
                    startCamera();
                    binding.cameraViewButton.setImageResource(R.mipmap.ic_front_camera);
                } else {
                    CameraSharedPreferences.setIsBackCamera(true);
                    startCamera();
                    binding.cameraViewButton.setImageResource(R.mipmap.ic_back_camera);
                }
            }
        });

        binding.flashButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (CameraSharedPreferences.isCameraFlash()) {
                    CameraSharedPreferences.setIsCameraFlash(false);
                    imageCapture.setFlashMode(ImageCapture.FLASH_MODE_OFF);
                    binding.flashButton.setImageResource(R.mipmap.ic_flash_off);
                } else {
                    CameraSharedPreferences.setIsCameraFlash(true);
                    imageCapture.setFlashMode(ImageCapture.FLASH_MODE_ON);
                    binding.flashButton.setImageResource(R.mipmap.ic_flash_on);
                }
            }
        });

        binding.previewView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN: {
                        return true;
                    }
                    case MotionEvent.ACTION_UP: {
                        return focus(event);
                    }
                    default: {return false;}
                }
            }
        });

        binding.noFocusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                camera.getCameraControl().cancelFocusAndMetering();
                binding.noFocusButton.setVisibility(View.GONE);
            }
        });
    }

    private boolean focus(MotionEvent event) {
        final float x = event.getX();
        final float y = event.getY();
        MeteringPointFactory factory = new SurfaceOrientedMeteringPointFactory(
                binding.previewView.getWidth(), binding.previewView.getHeight());
        float afPointWidth = 1.0f / 6.0f;
        float aePointWidth = afPointWidth * 1.5f;
        MeteringPoint afPoint = factory.createPoint(x, y, afPointWidth);
        MeteringPoint aePoint = factory.createPoint(x, y, aePointWidth);

        camera.getCameraControl().startFocusAndMetering(new FocusMeteringAction
                .Builder(afPoint, FocusMeteringAction.FLAG_AF)
                .addPoint(aePoint, FocusMeteringAction.FLAG_AE)
                .build());
        binding.noFocusButton.setVisibility(View.VISIBLE);
        return true;
    }

    private void takePicture() {
        File tempFile = new File(getApplication().getCacheDir(), TEMP_IMAGE);
        ImageCapture.OutputFileOptions outputFileOptions =
                new ImageCapture.OutputFileOptions.Builder(tempFile).build();
        imageCapture.takePicture(outputFileOptions, Executors.newSingleThreadExecutor(), new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                Intent intent = new Intent(MainActivity.this, SaveImageActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                exception.printStackTrace();
            }
        });
    }

    private void initializeViews() {
        binding.flashButton.setImageResource(CameraSharedPreferences.isCameraFlash() ?
                R.mipmap.ic_flash_on :
                R.mipmap.ic_flash_off);
        binding.cameraViewButton.setImageResource(CameraSharedPreferences.isBackCamera() ?
                R.mipmap.ic_back_camera :
                R.mipmap.ic_front_camera);
    }

    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), permission) !=
                    PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera();
            } else {
                Toast.makeText(this,
                        "Permissions not granted by the user.",
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void startCamera() {
        final ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    cameraProvider = cameraProviderFuture.get();
                    //Front camera check
                    if (!cameraProvider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA)) {
                        binding.cameraViewButton.setEnabled(false);
                    }
                    bindCamera(cameraProvider);
                } catch (ExecutionException | InterruptedException | CameraInfoUnavailableException e) {
                    e.printStackTrace();
                }
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindCamera(ProcessCameraProvider cameraProvider) {
        int aspectRatio = getAspectRatio();
        Preview preview = new Preview.Builder()
                .setTargetAspectRatio(aspectRatio)
                .build();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSharedPreferences.isBackCamera() ?
                        CameraSelector.LENS_FACING_BACK : CameraSelector.LENS_FACING_FRONT)
                .build();

        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setTargetAspectRatio(aspectRatio)
                .build();
        ImageCapture.Builder builder = new ImageCapture.Builder();

        HdrImageCaptureExtender hdrImageCaptureExtender = HdrImageCaptureExtender.create(builder);
        if (hdrImageCaptureExtender.isExtensionAvailable(cameraSelector)) {
            hdrImageCaptureExtender.enableExtension(cameraSelector);
        }

        imageCapture = builder
                .setTargetAspectRatio(aspectRatio)
                .setFlashMode(CameraSharedPreferences.isCameraFlash() ?
                        ImageCapture.FLASH_MODE_ON : ImageCapture.FLASH_MODE_OFF)
                .build();

        OrientationEventListener orientationEventListener = new OrientationEventListener((Context) this) {
            @Override
            public void onOrientationChanged(int orientation) {
                int rotation;

                if (orientation >= 45 && orientation < 135) {
                    rotation = Surface.ROTATION_270;
                } else if (orientation >= 135 && orientation < 225) {
                    rotation = Surface.ROTATION_180;
                } else if (orientation >= 225 && orientation < 315) {
                    rotation = Surface.ROTATION_90;
                } else {
                    rotation = Surface.ROTATION_0;
                }
                imageCapture.setTargetRotation(rotation);
            }
        };

        orientationEventListener.enable();

        cameraProvider.unbindAll();
        camera = cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector,
                preview, imageAnalysis, imageCapture);
        preview.setSurfaceProvider(binding.previewView.createSurfaceProvider());
    }

    private int getAspectRatio(){
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;
        double previewRatio = Math.max(width, height) * 1.0 / Math.min(width, height);
        if(Math.abs(previewRatio - 4.0 / 3.0) <= Math.abs(previewRatio - 16.0 / 9.0)) {
            return AspectRatio.RATIO_4_3;
        }
        else {
            return AspectRatio.RATIO_16_9;
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setGravityButtonDependingOnOrientation();
    }

    private void setGravityButtonDependingOnOrientation() {
        //changed gravity of the camera button
        switch (getWindowManager().getDefaultDisplay().getRotation()) {
            case Surface.ROTATION_0:
            case Surface.ROTATION_180: {
                FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams
                        (ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
                binding.cameraButton.setLayoutParams(layoutParams);
                break;
            }
            case Surface.ROTATION_90:
            case Surface.ROTATION_270: {
                FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams
                        (ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams.gravity = Gravity.CENTER_VERTICAL | Gravity.END;
                binding.cameraButton.setLayoutParams(layoutParams);
                break;
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            takePicture();
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        CameraSharedPreferences.saveSettings(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        CameraSharedPreferences.loadSettings(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraProvider.unbindAll();
    }

}
