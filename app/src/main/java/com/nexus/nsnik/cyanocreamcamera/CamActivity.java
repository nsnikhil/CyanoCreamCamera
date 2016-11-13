package com.nexus.nsnik.cyanocreamcamera;

import android.Manifest;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;


public class CamActivity extends AppCompatActivity implements View.OnClickListener, SurfaceHolder.Callback {

    Camera mCamera;
    FloatingActionButton cam, goToVidCam, goToFrontCamera;
    private static final int pRequestCode = 5002;
    private static final String[] mPermissions = {Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    SurfaceView surfaceView;
    private SurfaceHolder mHolder;
    private static final String logTag = CamActivity.class.getSimpleName();
    int id = 0;
    private static boolean mSwitch = false;
    public int count = 0;
    public ArrayList<Bitmap> temp = new ArrayList<>();
    private static final String mFolder = "Gifs";
    ImageView goToList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cam_layout);
        checkCameraHardware();
        checkPermissions();
        initilize(id);
        setParm();
        cam.setOnClickListener(this);
        goToVidCam.setOnClickListener(this);
        goToFrontCamera.setOnClickListener(this);
        goToList.setOnClickListener(this);
    }

    private void setParm() {
        Camera.Parameters params = mCamera.getParameters();
        params.setFlashMode(Camera.Parameters.FOCUS_MODE_AUTO);
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
        goToList = (ImageView)findViewById(R.id.goToList);
    }


    private Camera getCamera(int id) {
        Camera c = null;

        try {
            if(id==Camera.CameraInfo.CAMERA_FACING_FRONT){
                c = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
            }else {
                c = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
            }
        } catch (RuntimeException e) {
            Log.e(logTag, "Camera failed to open: " + e.getLocalizedMessage());
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
        if(mCamera==null){
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


    Camera.PictureCallback pictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            Bitmap bittemp = BitmapFactory.decodeByteArray(data,0,data.length);
            temp.add(bittemp);
            Log.d(logTag,"adeedToList");
            File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
            if (pictureFile == null) {
                return;
            }
            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
            } catch (FileNotFoundException e) {
            } catch (IOException e) {
            }
            mCamera.startPreview();
        }
    };


    private File getOutputMediaFile(int type) {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "MyCameraApp");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
            Toast.makeText(this,mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg",Toast.LENGTH_SHORT).show();
        } else if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "VID_" + timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
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
                Log.d(logTag,count+"");
                mCamera.takePicture(null, null, pictureCallback);
                count++;
                if(count>=3){
                    makeGif();
                    count=0;
                    temp.clear();
                }
                break;
            case R.id.fabGoToVidCamera:
                Toast.makeText(this, "Not Ready", Toast.LENGTH_SHORT).show();
                break;
            case R.id.fabGoToFrontCamera:
                if(!mSwitch){
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
                }else {
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
                break;
            case R.id.goToList:
                startActivity(new Intent(CamActivity.this,ListActivity.class));
                break;
        }
    }

    private void makeGif() {
        File folder = Environment.getExternalStoragePublicDirectory(mFolder);
        if(!folder.exists()){
            if(!folder.mkdir()){

            }
        }
        File f = new File(folder,new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()));
        FileOutputStream outStream = null;
        try {
            outStream = new FileOutputStream(f);
            outStream.write(generateGIF());
            Log.d("GifSaved",folder+"/test.gif");
            outStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private byte[] generateGIF() {
        ArrayList<Bitmap> bitmaps = temp;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        AnimatedGifEncoder encoder = new AnimatedGifEncoder();
        encoder.start(bos);
        for (Bitmap bitmap : bitmaps) {
            encoder.addFrame(bitmap);
        }
        encoder.finish();
        return bos.toByteArray();
    }

}

