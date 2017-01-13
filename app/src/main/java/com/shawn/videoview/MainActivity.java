package com.shawn.videoview;

import android.graphics.PixelFormat;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.shawn.videoview.media.GestureVideoView;
import com.shawn.videoview.media.callback.OnVideoLifeCycleListener;

import tv.danmaku.ijk.media.player.IMediaPlayer;


public class MainActivity extends AppCompatActivity implements OnVideoLifeCycleListener {

    private static final String TAG = "MainActivity";

    private GestureVideoView gestureVideoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gestureVideoView = (GestureVideoView) findViewById(R.id.ijk_view);
        //        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/test1.mp4";
        String path = "http://192.168.1.169:8080/video/test1.mp4";
        Log.e(TAG, "onCreate: " + path);

        gestureVideoView.setVideoPath(path);


        gestureVideoView.setmOnVideoLifeCycleListener(this);

        findViewById(R.id.btn_go).setOnClickListener(v -> {
            //                long seekTo = ijkVideoView.getCurrentPosition() + 10000;
            //                ijkVideoView.seekTo(seekTo);
        });
        Log.d(TAG, "onCreate: " + "test");

    }

    @Override
    public void onPreparing(IMediaPlayer iMediaPlayer) {
        Log.i(TAG, "onPreparing: ");
    }

    @Override
    public void onPrepared(IMediaPlayer iMediaPlayer) {
        Log.i(TAG, "onPrepared: ");
        gestureVideoView.start();
    }

    @Override
    public void onStart(IMediaPlayer iMediaPlayer) {
        Log.i(TAG, "onStart: ");
    }

    @Override
    public void onPause(IMediaPlayer iMediaPlayer) {
        Log.i(TAG, "onPause: ");
    }

    @Override
    public void onStop(IMediaPlayer iMediaPlayer) {
        Log.i(TAG, "onStop: ");
    }

    @Override
    public void onInfo(IMediaPlayer iMediaPlayer) {
        Log.i(TAG, "onInfo: ");
    }

    @Override
    public void onCompletion(IMediaPlayer iMediaPlayer) {
        Log.i(TAG, "onCompletion: ");
    }

    @Override
    public boolean onError(IMediaPlayer iMediaPlayer, int what, int extra) {
        Log.i(TAG, "onError: ");
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (gestureVideoView != null && !gestureVideoView.isPlaying()) {
            gestureVideoView.start();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (gestureVideoView != null && gestureVideoView.isPlaying()) {
            gestureVideoView.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (gestureVideoView != null) {
            gestureVideoView.stopPlayback();
            gestureVideoView.release(true);
        }
    }
}
