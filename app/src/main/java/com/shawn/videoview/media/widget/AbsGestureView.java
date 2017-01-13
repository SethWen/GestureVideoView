package com.shawn.videoview.media.widget;

import android.content.Context;
import android.support.annotation.IntRange;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.shawn.videoview.R;

/**
 * 手势操作时显示的视图
 * author: Shawn
 * time  : 2017/1/12 15:06
 */

public class AbsGestureView {

    private Context context;

    private View rootView;
    private LinearLayout llSeek, llVolume, llBrightness;
    private ImageView ivSeek, ivVolume, ivBrightness;
    private ProgressBar pbVolume, pbBrightness;
    private TextView tvSeek;

    public AbsGestureView(Context context) {
        this.context = context;
        rootView = LayoutInflater.from(context).inflate(R.layout.video_gesture_abs, null);
        llSeek = (LinearLayout) rootView.findViewById(R.id.ll_seek);
        llVolume = (LinearLayout) rootView.findViewById(R.id.ll_volume);
        llBrightness = (LinearLayout) rootView.findViewById(R.id.ll_brightness);
        ivSeek = (ImageView) rootView.findViewById(R.id.iv_seek);
        ivVolume = (ImageView) rootView.findViewById(R.id.iv_volume);
        ivBrightness = (ImageView) rootView.findViewById(R.id.iv_brightness);
        pbVolume = (ProgressBar) rootView.findViewById(R.id.pb_volume);
        pbBrightness = (ProgressBar) rootView.findViewById(R.id.pb_brightness);
        tvSeek = (TextView) rootView.findViewById(R.id.tv_seek);
    }

    public View getRootView() {
        return rootView;
    }

    public void setRootView(View rootView) {
        this.rootView = rootView;
    }

    public void setVisibility(int visibility) {
        rootView.setVisibility(visibility);
    }

    public void setSeekText(String text, boolean isForward) {
        if (isForward) {
            ivSeek.setImageResource(R.drawable.ic_fast_forward_white_24dp);
        } else {
            ivSeek.setImageResource(R.drawable.ic_fast_rewind_white_24dp);
        }
        tvSeek.setText(text);
    }

    public void setSeekViewVisibility(int visibility) {
        rootView.setVisibility(visibility);
        llSeek.setVisibility(visibility);
    }

    public void setVolumeViewVisibility(int visibility) {
        rootView.setVisibility(visibility);
        llVolume.setVisibility(visibility);
    }

    public void setBrightnessViewVisibility(int visibility) {
        rootView.setVisibility(visibility);
        llBrightness.setVisibility(visibility);
    }

    public void setVolumeProgress(@IntRange(from = 0, to = 100) int volume) {
        volume = fixVolume(volume);
        pbVolume.setProgress(volume);
    }

    public int getVolumeProgress() {
        return pbVolume.getProgress();
    }

    public void setBrightnessProgress(@IntRange(from = 0, to = 100) int brightness) {
        brightness = fixBrightness(brightness);
        pbBrightness.setProgress(brightness);
    }

    public int getBrightnessProgress() {
        return pbBrightness.getProgress();
    }

    private int fixVolume(int volume) {
        if (volume < 0) return 0;
        if (volume > 100) return 100;
        return volume;
    }

    private int fixBrightness(int brightness) {
        if (brightness < 0) return 0;
        if (brightness > 100) return 100;
        return brightness;
    }
}
