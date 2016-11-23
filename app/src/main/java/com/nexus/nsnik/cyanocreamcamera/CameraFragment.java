package com.nexus.nsnik.cyanocreamcamera;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;


public class CameraFragment extends Fragment implements View.OnClickListener, SurfaceHolder.Callback, LoaderManager.LoaderCallbacks,AsyncInterface {

    Camera mCamera;
    FloatingActionButton cam, goToFrontCamera;
    SurfaceView surfaceView;
    private SurfaceHolder mHolder;
    private static final String logTag = CameraFragment.class.getSimpleName();
    int id = 0;
    private static boolean mSwitch = false;
    public static ArrayList<Bitmap> temp = new ArrayList<>();
    ProgressBar indicatorProgress;
    TextView indicator;
    private static final int mLoaderId = 1535;
    SharedPreferences.OnSharedPreferenceChangeListener spfd = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            changeIndicator();
        }
    };
    View tempView;


    public CameraFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = LayoutInflater.from(getContext()).inflate(R.layout.cam_layout, container, false);
        initilize(id, v);
        setParm();
        setClickListener();
        tempView = v;
        return v;
    }

    private void setClickListener() {
        cam.setOnClickListener(this);
        goToFrontCamera.setOnClickListener(this);
    }

    private void setParm() {
        Camera.Parameters params = mCamera.getParameters();
        params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        mCamera.enableShutterSound(true);
        mCamera.setParameters(params);
    }

    private void initilize(int id, View v) {
        cam = (FloatingActionButton) v.findViewById(R.id.fabCamera);
        mCamera = getCamera(id);
        mCamera.setDisplayOrientation(setCameraDisplayOrientation(id));
        surfaceView = (SurfaceView)v.findViewById(R.id.camera_preview);
        mHolder = surfaceView.getHolder();
        mHolder.addCallback(this);
        goToFrontCamera = (FloatingActionButton) v.findViewById(R.id.fabGoToFrontCamera);
        indicator = (TextView) v.findViewById(R.id.gifIndicator);
        indicatorProgress = (ProgressBar) v.findViewById(R.id.gifIndicatorProgress);
        changeIndicator();
    }

    private void changeIndicator() {
        SharedPreferences spf = PreferenceManager.getDefaultSharedPreferences(getContext());
        spf.registerOnSharedPreferenceChangeListener(spfd);
        if (!spf.getBoolean(getString(R.string.savegifkey), false)) {
            indicator.setText("OFF");
            indicator.setTextColor(getResources().getColor(R.color.colorAccent));
        } else {
            indicator.setText("ON");
            indicator.setTextColor(getResources().getColor(android.R.color.holo_green_light));
        }
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
        int rotation = getActivity().getWindowManager().getDefaultDisplay().getRotation();
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
    public void onResume() {
        if (mCamera == null) {
            mCamera = getCamera(id);
            mCamera.setDisplayOrientation(setCameraDisplayOrientation(id));
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

    @Override
    public void onPause() {
        super.onPause();
        if(mCamera!=null){
            surfaceView.getHolder().removeCallback(this);
            releaseCamera();
        }

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
            case R.id.fabGoToFrontCamera:
                frontCam();
                break;
        }
    }

    private void frontCam() {
        if (!mSwitch) {
            goToFrontCamera.setImageResource(R.drawable.ic_camera_rear_white_48dp);
            releaseCamera();
            initilize(Camera.CameraInfo.CAMERA_FACING_FRONT,tempView);
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
            initilize(id,tempView);
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
        switch (id) {
            case mLoaderId:
                return new GifLoade(getActivity(), temp);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader loader, Object data) {
        switch (loader.getId()) {
            case mLoaderId:
                temp.clear();
                changeIndicator();
                indicatorProgress.setVisibility(View.GONE);
                Toast.makeText(getActivity(), "Gif Saved", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader loader) {

    }

    private void startLoader(int id) {
        if (getActivity().getSupportLoaderManager().getLoader(id) == null) {
            getActivity().getSupportLoaderManager().initLoader(id, null, this).forceLoad();
        } else {
            getActivity().getSupportLoaderManager().restartLoader(id, null, this).forceLoad();
        }
    }

    @Override
    public void sendBitmap(Bitmap b) {
        SharedPreferences spf = PreferenceManager.getDefaultSharedPreferences(getContext());
        if (spf.getBoolean(getResources().getString(R.string.savegifkey), false)) {
            temp.add(b);
            indicator.setText(temp.size() + "");
            if (temp.size() == 3) {
                indicatorProgress.setVisibility(View.VISIBLE);
                indicator.setTextColor(getResources().getColor(R.color.colorAccent));
                indicator.setText("Saving");
                startLoader(mLoaderId);
            }
        }
    }
}



