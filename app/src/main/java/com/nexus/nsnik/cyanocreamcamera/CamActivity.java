package com.nexus.nsnik.cyanocreamcamera;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;


public class CamActivity extends AppCompatActivity implements View.OnClickListener, SurfaceHolder.Callback, LoaderManager.LoaderCallbacks,AsyncInterface{

    Camera mCamera;
    FloatingActionButton cam, goToVidCam, goToFrontCamera;
    private static final int pRequestCode = 5002;
    private static final String[] mPermissions = {Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    SurfaceView surfaceView;
    private SurfaceHolder mHolder;
    private static final String logTag = CamActivity.class.getSimpleName();
    int id = 0;
    private static boolean mSwitch = false;
    public static ArrayList<Bitmap> temp = new ArrayList<>();
    ImageView goToList, settings, flash;
    private static final int mLoaderId = 1535;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cam_layout);
        checkCameraHardware();
        checkPermissions();
        initilize(id);
        setParm();
        setClickListener();
    }

    private void setClickListener() {
        cam.setOnClickListener(this);
        goToVidCam.setOnClickListener(this);
        goToFrontCamera.setOnClickListener(this);
        goToList.setOnClickListener(this);
        settings.setOnClickListener(this);
    }

    private void setParm() {
        Camera.Parameters params = mCamera.getParameters();
        params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        mCamera.enableShutterSound(true);
        mCamera.setParameters(params);
    }

    private void initilize(int id) {
        mCamera = getCamera(id);
        mCamera.setDisplayOrientation(setCameraDisplayOrientation(0));
        surfaceView = (SurfaceView) findViewById(R.id.camera_preview);
        mHolder = surfaceView.getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        cam = (FloatingActionButton) findViewById(R.id.fabCamera);
        goToFrontCamera = (FloatingActionButton) findViewById(R.id.fabGoToFrontCamera);
        goToVidCam = (FloatingActionButton) findViewById(R.id.fabGoToVidCamera);
        goToList = (ImageView) findViewById(R.id.goToList);
        settings = (ImageView) findViewById(R.id.cameraSettings);
        flash = (ImageView) findViewById(R.id.camerFlashLight);
    }


    private Camera getCamera(int id) {
        Camera c = null;
        try {
            if (id == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                c = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
            } else {
                c = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
            }
        } catch (RuntimeException e) {
        }
        return c;
    }

    public int setCameraDisplayOrientation(int cameraId) {
        android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = this.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;
        } else {
            result = (info.orientation - degrees + 360) % 360;
        }
        return result;
    }

    @Override
    protected void onResume() {
        if (mCamera == null) {
            mCamera = getCamera(id);
        }
        super.onResume();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (mHolder.getSurface() == null) {
            return;
        }
        try {
            mCamera.stopPreview();
        } catch (Exception e) {
        }
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
        } catch (Exception e) {
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        releaseCamera();
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
                || ContextCompat.checkSelfPermission(this, mPermissions[1]) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, mPermissions[2]) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, mPermissions[3]) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, mPermissions, pRequestCode);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();
    }

    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fabCamera:
                SaveAsync save = new SaveAsync();
                save.asyncInterface = this;
                save.execute(mCamera);
                break;
            case R.id.fabGoToVidCamera:
                Toast.makeText(this, "Not Ready", Toast.LENGTH_SHORT).show();
                break;
            case R.id.fabGoToFrontCamera:
                frontCam();
                break;
            case R.id.goToList:
                startActivity(new Intent(CamActivity.this, ListActivity.class));
                break;
            case R.id.cameraSettings:
                startActivity(new Intent(CamActivity.this, Prefrences.class));
                break;
        }
    }

    private void frontCam(){
        if (!mSwitch) {
            goToFrontCamera.setImageResource(R.drawable.ic_camera_rear_white_48dp);
            releaseCamera();
            initilize(Camera.CameraInfo.CAMERA_FACING_FRONT);
            try {
                mCamera.setPreviewDisplay(mHolder);
                mCamera.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mSwitch = true;
        } else {
            goToFrontCamera.setImageResource(R.drawable.ic_camera_front_white_48dp);
            releaseCamera();
            initilize(id);
            try {
                mCamera.setPreviewDisplay(mHolder);
                mCamera.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mSwitch = false;
        }
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        switch (id){
            case mLoaderId:
                return new GifLoade(this, temp);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader loader, Object data) {
        switch (loader.getId()){
            case mLoaderId:
                temp.clear();
                Toast.makeText(this,"Gif Saved",Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader loader) {

    }

    private void startLoader(int id) {
        if (getSupportLoaderManager().getLoader(id) == null) {
            getSupportLoaderManager().initLoader(id, null, this).forceLoad();
        } else {
            getSupportLoaderManager().restartLoader(id, null, this).forceLoad();
        }
    }

    @Override
    public void sendBitmap(Bitmap b) {
        temp.add(b);
        if(temp.size()==3){
            startLoader(mLoaderId);
        }
    }
}

