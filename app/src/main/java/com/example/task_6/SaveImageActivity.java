package com.example.task_6;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.task_6.databinding.ActivitySaveImageBinding;
import com.example.task_6.databinding.DialogSaveImageBinding;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class SaveImageActivity extends AppCompatActivity {

    private ActivitySaveImageBinding binding;
    private static final String SAVE_IMAGE_PATH = "/storage/self/primary/DCIM/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySaveImageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        String tempPath = getApplication().getCacheDir() + "/" + MainActivity.TEMP_IMAGE;
        binding.imageView.setImageURI(Uri.parse(tempPath));
        File tempImage = new File(tempPath);

        binding.saveButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("InflateParams")
            @Override
            public void onClick(View v) {
                DialogSaveImageBinding saveImageBinding = DialogSaveImageBinding.inflate(getLayoutInflater());
                AlertDialog.Builder builder = new AlertDialog.Builder(SaveImageActivity.this);
                builder.setView(saveImageBinding.getRoot())
                        .setNegativeButton(R.string.cancel_button, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                File saveImage = new File(SAVE_IMAGE_PATH +
                                        saveImageBinding.saveDialogEditText.getText().toString() +
                                        ".jpg");
                                if(saveImage.exists()) {
                                    Toast toast = Toast.makeText(SaveImageActivity.this,
                                            R.string.file_name_use_message, Toast.LENGTH_SHORT);
                                    toast.show();
                                } else {
                                    copyTempImage(saveImage, tempImage);
                                    galleryAddPic(saveImage);
                                    Toast toast = Toast.makeText(SaveImageActivity.this,
                                           R.string.saved_successfully, Toast.LENGTH_SHORT);
                                    toast.show();
                                    finishActivity(tempImage);
                                }
                            }
                        });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });

        binding.cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishActivity(tempImage);
            }
        });
    }

    private void finishActivity(File tempImage) {
        tempImage.delete();
        Intent intent = new Intent(SaveImageActivity.this,
                MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void copyTempImage(File saveImage, File tempImage) {
        try(FileChannel source = new FileInputStream(tempImage).getChannel();
            FileChannel destination = new FileOutputStream(saveImage).getChannel()) {
            destination.transferFrom(source, 0, source.size());
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    private void galleryAddPic(File saveImage) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile(saveImage);
        mediaScanIntent.setData(contentUri);
        sendBroadcast(mediaScanIntent);
    }
}
