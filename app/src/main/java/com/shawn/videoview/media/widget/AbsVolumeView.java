package com.shawn.videoview.media.widget;

import android.content.Context;
import android.support.annotation.IntRange;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.shawn.videoview.R;

/**
 * 如需单独定义音量调节视图，可以使用此类
 * author: Shawn
 * time  : 2017/1/12 10:55
 */

public class AbsVolumeView {

    protected String TAG = getClass().getSimpleName();

    private static final int PROGRESS_BAR_MAX = 100;

    protected Context context;

    private View rootView;
    private TextView tvVolume;
    private ProgressBar pbVolume;

    public AbsVolumeView(Context context) {
        this.context = context;
        rootView = LayoutInflater.from(context).inflate(R.layout.video_volume_abs, null);
        tvVolume = (TextView) rootView.findViewById(R.id.tv_volume);
        pbVolume = (ProgressBar) rootView.findViewById(R.id.pb_volume);
        pbVolume.setMax(PROGRESS_BAR_MAX);
    }

    public View getRootView() {
        return rootView;
    }

    public void setVolumeProgress(@IntRange(from = 0, to = 100) int volume) {
        volume = fixVolume(volume);
        tvVolume.setText(volume + "%");
        pbVolume.setProgress(volume);
    }

    public int getVolumeProgress() {
        return pbVolume.getProgress();
    }

    private int fixVolume(int volume) {
        if(volume < 0) return 0;
        if (volume > 100) return 100;
        return volume;
    }

    public void setVisibility(int visibility) {
        rootView.setVisibility(visibility);
    }

    public int getVisibility() {
        return rootView.getVisibility();
    }
}
