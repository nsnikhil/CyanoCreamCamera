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

public class CamActivity extends AppCompatActivity implements SurfaceHolder.Callback{

    Camera mCamera;
    SurfaceView surfaceView;
    private SurfaceHolder mHolder;
    MediaRecorder mMediaRecorder;
    FloatingActionButton cam,gotoVcam;
    private static final int pRequestCode = 5002;
    private static final String[] mPermissions = {Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE};


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cam_layout);
        checkCameraHardware();
        checkPermissions();


        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        surfaceView = (SurfaceView) findViewById(R.id.camera_preview);
        mHolder = surfaceView.getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        cam = (FloatingActionButton) findViewById(R.id.fabCamera);
        gotoVcam = (FloatingActionButton) findViewById(R.id.gotoVcam);
        cam.setOnClickListener(new View.OnClickListener() {
                                   @Override
                                   public void onClick(View v) {
                                       mCamera.takePicture(null, null, mPicture);
                                   }
                               }
        );
        gotoVcam.setOnClickListener(new View.OnClickListener() {
                                   @Override
                                   public void onClick(View v) {
                                       Intent go = new Intent(CamActivity.this,VcamActivity.class);
                                       startActivity(go);
                                       Toast.makeText(getApplication(),"Code is written but not working",Toast.LENGTH_LONG).show();
                                   }
                               }
        );
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case pRequestCode: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this,"Camera Allowed",Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this,"Camera Denied",Toast.LENGTH_SHORT).show();
                }
                if (grantResults.length > 0 && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this,"Location Allowed",Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this,"Location Denied",Toast.LENGTH_SHORT).show();
                }
                if (grantResults.length > 0 && grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this,"Microphone Allowed",Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this,"Microphone Denied",Toast.LENGTH_SHORT).show();
                }
                if (grantResults.length > 0 && grantResults[3] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this,"Storage Allowed",Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this,"Storage Denied",Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        int numberOfCameras = Camera.getNumberOfCameras();
        Camera.CameraInfo info = new Camera.CameraInfo();
        mCamera = null;
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.getCameraInfo(i, info);
            // Open a back camera
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                try {
                    mCamera = Camera.open(i);
                    break;
                } catch (RuntimeException e) {
                }
            }
        }
        if (mCamera == null) {
        }

        Camera.Parameters parameters = mCamera.getParameters();

        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360; //@menu for mirro
        } else {
            result = (info.orientation - degrees + 360) % 360;
        }
        mCamera.setDisplayOrientation(result);

        Camera.Size size = parameters.getPreviewSize();

        mHolder.setFixedSize(size.width,size.height);

        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (mHolder.getSurface() == null){
            return;
        }
        try {
            mCamera.stopPreview();
        } catch (Exception e){
        }

        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
    }

    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
            if (pictureFile == null){
                Log.d("", "Error creating media file, check storage permissions: " );
                return;
            }
            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
            } catch (FileNotFoundException e) {
                Log.d("", "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d("", "Error accessing file: " + e.getMessage());
            }
        }
    };

    private static File getOutputMediaFile(int type){
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory( Environment.DIRECTORY_PICTURES), "MyCameraApp");
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_"+ timeStamp + ".jpg");
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "VID_"+ timeStamp + ".mp4");
        } else {
            return null;
        }
        return mediaFile;
    }
}
