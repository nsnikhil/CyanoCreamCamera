package com.nexus.nsnik.cyanocreamcamera;

import android.Manifest;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;

/**
 * Created by nsnik on 29-Oct-16.
 */

public class CamActivity extends AppCompatActivity{

    Camera c;
    FloatingActionButton cam;
    private static final int pRequestCode = 5002;
    private static final String[] mPermissions = {Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    CamSurface cams;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cam_layout);
        checkCameraHardware();
        checkPermissions();
        c = getCamera();
        cams = new CamSurface(this,c);
        FrameLayout surfaceView = (FrameLayout)findViewById(R.id.camera_preview);
        surfaceView.addView(cams);
    }

    private Camera getCamera() {
        Camera c = null;
        try {
            c = Camera.open();
        }catch (Exception e){

        }
        return c;
    }

    private boolean checkCameraHardware() {
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            return true;
        } else {
            return false;
        }
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, mPermissions[0]) != PackageManager.PERMISSION_GRANTED
                ||ContextCompat.checkSelfPermission(this, mPermissions[1]) != PackageManager.PERMISSION_GRANTED
                ||ContextCompat.checkSelfPermission(this, mPermissions[2]) != PackageManager.PERMISSION_GRANTED
                ||ContextCompat.checkSelfPermission(this, mPermissions[3]) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, mPermissions, pRequestCode);
        }
    }
}
