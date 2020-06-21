package com.example.task_6;

import android.content.Context;
import android.content.SharedPreferences;

public class CameraSharedPreferences {

    private static final String CAMERA_SHARED_PREFERENCES = "cameraSettings";
    private static final String CAMERA_SELECTOR_SHARED_PREFERENCE = "cameraSelector";
    private static final String CAMERA_FLASH_PREFERENCES = "cameraFlash";
    private static SharedPreferences storageSettings;

    public static void saveSettings(Context context, boolean isBackCamera, boolean isCameraFlash) {
        storageSettings = context.getSharedPreferences(CAMERA_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor ed = storageSettings.edit();
        ed.putBoolean(CAMERA_SELECTOR_SHARED_PREFERENCE, isBackCamera);
        ed.putBoolean(CAMERA_FLASH_PREFERENCES, isCameraFlash);
        ed.apply();
    }


    public static boolean isBackCamera(Context context) {
        storageSettings = context.getSharedPreferences(CAMERA_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        return storageSettings.getBoolean(CAMERA_SELECTOR_SHARED_PREFERENCE, true);
    }


    public static boolean isCameraFlash(Context context) {
        storageSettings = context.getSharedPreferences(CAMERA_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        return storageSettings.getBoolean(CAMERA_FLASH_PREFERENCES, false);
    }

}
