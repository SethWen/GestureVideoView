package com.shawn.videoview.media.widget;

import android.content.Context;
import android.support.annotation.IntRange;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.shawn.videoview.R;

/**
 * 如需单独定义亮度调节视图，可以使用此类
 * author: Shawn
 * time  : 2017/1/12 10:54
 */

public class AbsBrightnessView {

    protected String TAG = getClass().getSimpleName();

    private static final int PROGRESS_BAR_MAX = 100;

    protected Context context;

    private View rootView;
    private TextView tvBrightness;
    private ProgressBar pbBrightness;

    public AbsBrightnessView(Context context) {
        this.context = context;
        rootView = LayoutInflater.from(context).inflate(R.layout.video_brightness_abs, null);
        tvBrightness = (TextView) rootView.findViewById(R.id.tv_brightness);
        pbBrightness = (ProgressBar) rootView.findViewById(R.id.pb_brightness);
        pbBrightness.setMax(PROGRESS_BAR_MAX);
    }

    public View getRootView() {
        return rootView;
    }

    public void setBrightnessProgress(@IntRange(from = 0, to = 100) int brightness) {
        brightness = fixBrightness(brightness);
        tvBrightness.setText(brightness + "%");
        pbBrightness.setProgress(brightness);
    }

    public int getBrightnessProgress() {
        return pbBrightness.getProgress();
    }

    private int fixBrightness(int brightness) {
        if (brightness < 0) return 0;
        if (brightness > 100) return 100;
        return brightness;
    }

    public void setVisibility(int visibility) {
        rootView.setVisibility(visibility);
    }

    public int getVisibility() {
        return rootView.getVisibility();
    }
}
