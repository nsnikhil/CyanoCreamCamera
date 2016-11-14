package com.nexus.nsnik.cyanocreamcamera;

import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class SaveGif {

    private static final String mFolder = "Gifs";
    private static final String logTag = SaveGif.class.getSimpleName();

    public static void makeGif(ArrayList<Bitmap> tem) {
        Log.d(logTag,tem.size()+"");
        File folder = Environment.getExternalStoragePublicDirectory(mFolder);
        if(!folder.exists()){
            if(!folder.mkdir()){
            }
        }
        File f = new File(folder,new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()));
        FileOutputStream outStream = null;
        try {
            outStream = new FileOutputStream(f);
            outStream.write(generateGIF(tem));
            Log.d("Gif Path",f+"");
            outStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static byte[] generateGIF(ArrayList<Bitmap> temp) {
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
