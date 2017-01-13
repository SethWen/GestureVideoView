package com.shawn.videoview.media.widget;

import android.content.Context;
import android.support.annotation.IntRange;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.TextView;

import com.shawn.videoview.R;
import com.shawn.videoview.media.util.TimeUtil;

import java.util.Formatter;
import java.util.Locale;

import tv.danmaku.ijk.media.player.AndroidMediaPlayer;
import tv.danmaku.ijk.media.player.IMediaPlayer;

/**
 * 视频控制器
 * author: Shawn
 * time  : 2017/1/4 15:23
 */

public class AbsControllerView implements IVideoController, SeekBar.OnSeekBarChangeListener, View.OnClickListener {

    protected String TAG = getClass().getSimpleName();

    private static final int PROGRESS_BAR_MAX = 100;

    protected Context context;

    protected ImageButton playButton;
    protected View fastForwardButton;
    protected TextView durationView;
    protected TextView positionView;
    protected SeekBar progressBar;
    protected StringBuilder formatBuilder;
    protected Formatter formatter;

    protected IMediaPlayer mPlayer;
    protected long position;

    private View rootView;

    protected boolean isAttachedToWindow;
    protected boolean dragging;
    private boolean isSeekBarTouching;

    public View getRootView() {
        return rootView;
    }

    public void setRootView(View rootView) {
        this.rootView = rootView;
    }


    public AbsControllerView(Context context, IMediaPlayer player) {
        this.context = context;
        this.mPlayer = player;
        initViews();

        formatBuilder = new StringBuilder();
        formatter = new Formatter(formatBuilder, Locale.getDefault());
    }

    protected void initViews() {
        rootView = LayoutInflater.from(context).inflate(R.layout.video_controller_abs, null);
        playButton = (ImageButton) rootView.findViewById(R.id.btn_play);
        positionView = (TextView) rootView.findViewById(R.id.tv_position);
        durationView = (TextView) rootView.findViewById(R.id.tv_duration);
        progressBar = (SeekBar) rootView.findViewById(R.id.seek_bar);
        progressBar.setMax(PROGRESS_BAR_MAX);
        progressBar.setOnSeekBarChangeListener(this);
        playButton.setOnClickListener(this);
    }

    /**
     * Sets the {@link } to control.
     *
     * @param mPlayer the {@code ExoPlayer} to control.
     */
    public void setPlayer(AndroidMediaPlayer mPlayer) {
        if (this.mPlayer == mPlayer) {
            return;
        }
        if (this.mPlayer != null) {
            //            this.mPlayer.removeListener(componentListener);
        }
        this.mPlayer = mPlayer;
        if (mPlayer != null) {
            //            mPlayer.addListener(componentListener);
        }
    }

    public void setPlayState() {
        if (mPlayer.isPlaying()) {
            playButton.setImageResource(R.drawable.ic_play_arrow_white_24dp);
            mPlayer.pause();
        } else {
            playButton.setImageResource(R.drawable.ic_pause_white_24dp);
            mPlayer.start();
        }
    }

    @Override
    public void setAnchorView(View view) {

    }

    @Override
    public void setEnabled(boolean enabled) {
        rootView.setEnabled(enabled);
    }

    @Override
    public void setMediaPlayer(MediaController.MediaPlayerControl player) {

    }

    @Override
    public void showOnce(View view) {

    }

    @Override
    public void setVisibility(int visibility) {
        rootView.setVisibility(visibility);
    }

    @Override
    public boolean isVisible() {
        return rootView.getVisibility() == View.VISIBLE;
    }

    public void start() {
    }

    public void pause() {
    }

    public void setDuration(long duration) {
        if (durationView != null) durationView.setText(TimeUtil.formatTime(duration));
    }

    public void setPosition(long position) {
        if (positionView != null) positionView.setText(TimeUtil.formatTime(position));
    }

    public void setProgress(@IntRange(from = 0, to = 100) long progress) {
        if (progressBar != null) progressBar.setProgress((int) progress);
    }

    public int getProgress() {
        if (progressBar != null) {
            return progressBar.getProgress();
        }
        return 0;
    }

    public boolean isSeekBarTouching() {
        return isSeekBarTouching;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        this.position = progress * mPlayer.getDuration() / seekBar.getMax();
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        isSeekBarTouching = true;
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        isSeekBarTouching = false;
        mPlayer.seekTo(position);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_play:
                if (mPlayer == null) return;
                setPlayState();
                break;
            default:

                break;
        }
    }

    public interface MediaPlayerControl {
        void start();

        void pause();

        long getDuration();

        long getCurrentPosition();

        void seekTo(long pos);

        boolean isPlaying();

        int getBufferPercentage();

        boolean canPause();

        boolean canSeekBackward();

        boolean canSeekForward();

        /**
         * Get the audio session id for the mPlayer used by this VideoView. This can be used to
         * apply audio effects to the audio track of a video.
         *
         * @return The audio session, or 0 if there was an error.
         */
        int getAudioSessionId();
    }
}
