package com.nexus.nsnik.cyanocreamcamera;

import android.content.Intent;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;

public class VcamActivity extends AppCompatActivity implements View.OnClickListener{

    Camera vc;
    FloatingActionButton vcam,goToCam;
    CamSurface cams;
    private MediaRecorder mMediaRecorder;
    FrameLayout surfaceView;
    private boolean isRecording = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vcam_layout);
        prepareVideoRecorder();
        inititlize();
        vcam.setOnClickListener(this);
    }

    private boolean prepareVideoRecorder(){

        vc = getCamera();
        cams = new CamSurface(this, vc);
        mMediaRecorder = new MediaRecorder();
        vc.unlock();
        mMediaRecorder.setCamera(vc);
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
        mMediaRecorder.setOutputFile(getOutputMediaFile(MEDIA_TYPE_VIDEO).toString());
        mMediaRecorder.setPreviewDisplay(cams.getHolder().getSurface());
        try {
            mMediaRecorder.prepare();
        } catch (IllegalStateException e) {
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            releaseMediaRecorder();
            return false;
        }
        return true;
    }
    private void inititlize() {
        surfaceView = (FrameLayout) findViewById(R.id.VidCaamera_preview);
        surfaceView.addView(cams);
        vcam = (FloatingActionButton)findViewById(R.id.fabVidCamera);
        goToCam = (FloatingActionButton)findViewById(R.id.goToCamera);
    }

    private Camera getCamera() {
        Camera c = null;
        try {
            c = Camera.open();
        } catch (Exception e) {

        }
        return c;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.fabVidCamera:
                if (isRecording) {
                    mMediaRecorder.stop();
                    releaseMediaRecorder();
                    vc.lock();
                    isRecording = false;
                } else {
                    if (prepareVideoRecorder()) {
                        mMediaRecorder.start();
                        isRecording = true;
                    } else {
                        releaseMediaRecorder();
                    }
                }
                break;
            case R.id.goToCamera:
                startActivity(new Intent(VcamActivity.this,CamActivity.class));
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseMediaRecorder();
        releaseCamera();
    }

    private void releaseMediaRecorder(){
        if (mMediaRecorder != null) {
            mMediaRecorder.reset();
            mMediaRecorder.release();
            mMediaRecorder = null;
            vc.lock();
        }
    }

    private void releaseCamera(){
        if (vc != null){
            vc = null;
        }
    }

    private File getOutputMediaFile(int type) {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "MyCameraApp");
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +"IMG_"+ timeStamp + ".jpg");
            Log.d("Path",mediaStorageDir.getPath() + File.separator +"IMG_"+ timeStamp + ".jpg");
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "VID_"+ timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }
}
