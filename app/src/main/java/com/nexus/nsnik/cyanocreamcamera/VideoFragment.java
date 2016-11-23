package com.nexus.nsnik.cyanocreamcamera;

import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;


public class VideoFragment extends Fragment implements SurfaceHolder.Callback,View.OnClickListener {

    SurfaceHolder mHolder;
    SurfaceView mSurfaceView;
    FloatingActionButton record,goToFrontCam;
    TextView recordingIndicator;
    Camera mCamera;
    MediaRecorder mMediaRecorder;
    private static final String logTag = VideoFragment.class.getSimpleName();
    private static boolean isRecording = false;
    private static boolean mSwitch = false;
    View tempView;
    int id = 0;

    public VideoFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = LayoutInflater.from(getContext()).inflate(R.layout.vcam_layout, container, false);
        initilize(v);
        prepareVideoRecorder();
        setClickListner();
        return v;
    }

    private void initilize(View v) {
        mSurfaceView = (SurfaceView) v.findViewById(R.id.vCamSurface);
        mHolder = mSurfaceView.getHolder();
        mHolder.addCallback(this);
        record = (FloatingActionButton) v.findViewById(R.id.vCamStart);
        goToFrontCam = (FloatingActionButton) v.findViewById(R.id.vCamGoToFrontCamera);
        recordingIndicator = (TextView) v.findViewById(R.id.vCamIndicator);
        tempView = v;
    }

    private boolean prepareVideoRecorder() {
        mCamera = getCamera();
        mCamera.setDisplayOrientation(setOrientation(0));
        mMediaRecorder = new MediaRecorder();
        mCamera.unlock();
        mMediaRecorder.setCamera(mCamera);
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
        mMediaRecorder.setOutputFile(getOutputMediaFile(MEDIA_TYPE_VIDEO).toString());
        mMediaRecorder.setPreviewDisplay(mSurfaceView.getHolder().getSurface());
        try {
            mMediaRecorder.prepare();
        } catch (IllegalStateException e) {
            Log.d(logTag, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            Log.d(logTag, "IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }
        return true;
    }

    private static File getOutputMediaFile(int type) {
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
        } else if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "VID_" + timeStamp + ".mp4");
        } else {
            return null;
        }
        return mediaFile;
    }

    private void setClickListner() {
        record.setOnClickListener(this);
        goToFrontCam.setOnClickListener(this);
    }


    private Camera getCamera() {
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

    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera.lock();
            mCamera = null;
        }
    }

    private void releaseMediaRecorder() {
        if (mMediaRecorder != null) {
            mMediaRecorder.reset();
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
    }

    private int setOrientation(int cameraId) {
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
    public void onPause() {
        super.onPause();
        if(mCamera!=null){
            mSurfaceView.getHolder().removeCallback(this);
            releaseMediaRecorder();
            releaseCamera();
        }

    }

    @Override
    public void onResume() {
        if (mCamera == null) {
            mCamera = getCamera();
            mCamera.setDisplayOrientation(setOrientation(0));
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        releaseCamera();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.vCamStart:
                if (isRecording) {
                    recordingIndicator.setVisibility(View.GONE);
                    mMediaRecorder.stop();
                    releaseMediaRecorder();
                    mCamera.lock();
                    isRecording = false;
                } else {
                    if (prepareVideoRecorder()) {
                        recordingIndicator.setVisibility(View.VISIBLE);
                        mMediaRecorder.start();
                        isRecording = true;
                    } else {
                        releaseMediaRecorder();
                    }
                }
                break;
            case R.id.vCamGoToFrontCamera:
                break;
        }
    }

}
