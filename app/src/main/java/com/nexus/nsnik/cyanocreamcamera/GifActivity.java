package com.nexus.nsnik.cyanocreamcamera;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;

import java.io.IOException;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;
import pl.droidsonroids.gif.GifTextureView;

public class GifActivity extends AppCompatActivity {

    GifImageView gifView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gif);
        gifView = (GifImageView) findViewById(R.id.gifViewer);
        String url = getIntent().getExtras().getString(getResources().getString(R.string.gifurl));
        try {
            final GifDrawable gifFromAssets = new GifDrawable(url);
            gifView.setImageDrawable(gifFromAssets);
            gifFromAssets.setLoopCount(5);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
