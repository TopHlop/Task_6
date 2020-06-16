package com.example.task_6;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class CameraSharedPreferences {

    private static final String CAMERA_SHARED_PREFERENCES = "cameraSettings";
    private static final String CAMERA_SELECTOR_SHARED_PREFERENCE = "cameraSelector";
    private static final String CAMERA_FLASH_PREFERENCES = "cameraFlash";
    private static SharedPreferences storageSettings;
    //true - back camera, false - front camera
    private static boolean isBackCamera;
    private static boolean isCameraFlash;

    public static void saveSettings(Context context) {
        storageSettings = context.getSharedPreferences(CAMERA_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor ed = storageSettings.edit();
        ed.putBoolean(CAMERA_SELECTOR_SHARED_PREFERENCE, isBackCamera);
        ed.putBoolean(CAMERA_FLASH_PREFERENCES, isCameraFlash);
        ed.apply();
    }

    public static void loadSettings(Context context) {
        storageSettings = context.getSharedPreferences(CAMERA_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        isBackCamera = storageSettings.getBoolean(CAMERA_SELECTOR_SHARED_PREFERENCE, true);
        isCameraFlash = storageSettings.getBoolean(CAMERA_FLASH_PREFERENCES, false);
    }

    public static boolean isBackCamera() {
        return isBackCamera;
    }

    public static void setIsBackCamera(boolean isBackCamera) {
        CameraSharedPreferences.isBackCamera = isBackCamera;
    }

    public static boolean isCameraFlash() {
        return isCameraFlash;
    }

    public static void setIsCameraFlash(boolean isCameraFlash) {
        CameraSharedPreferences.isCameraFlash = isCameraFlash;
    }
}
