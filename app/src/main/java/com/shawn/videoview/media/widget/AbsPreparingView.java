package com.shawn.videoview.media.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import com.shawn.videoview.R;

/**
 * 视频准备视图
 * author: Shawn
 * time  : 2017/1/9 17:21
 */

public class AbsPreparingView {

    private Context context;
    private View rootView;

    public AbsPreparingView(Context context) {
        this.context = context;
        rootView = LayoutInflater.from(context).inflate(R.layout.video_preparing_abs, null);
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
}
