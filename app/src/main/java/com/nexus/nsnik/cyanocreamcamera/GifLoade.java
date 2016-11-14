package com.nexus.nsnik.cyanocreamcamera;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import java.util.ArrayList;


public class GifLoade extends AsyncTaskLoader{

    ArrayList<Bitmap> gif;
    private static final String logTag = GifLoade.class.getSimpleName();

    public GifLoade(Context context,ArrayList<Bitmap> send) {
        super(context);
        gif = send;
    }

    @Override
    public Object loadInBackground() {
        SaveGif.makeGif(gif);
        return null;
    }
}
