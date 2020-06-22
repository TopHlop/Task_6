package com.example.task_6;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

public class CameraViewModel extends AndroidViewModel {

    private boolean isBackCamera;
    private boolean isCameraFlash;

    public CameraViewModel(@NonNull Application application) {
        super(application);
        isBackCamera = CameraSharedPreferences.isBackCamera(application);
        isCameraFlash = CameraSharedPreferences.isCameraFlash(application);
    }

    public boolean isBackCamera() {
        return isBackCamera;
    }

    public void setBackCamera(boolean backCamera) {
        isBackCamera = backCamera;
    }

    public boolean isCameraFlash() {
        return isCameraFlash;
    }

    public void setCameraFlash(boolean cameraFlash) {
        isCameraFlash = cameraFlash;
    }

    public void saveSettings() {
        CameraSharedPreferences.saveSettings(getApplication(), isBackCamera, isCameraFlash);
    }
}
