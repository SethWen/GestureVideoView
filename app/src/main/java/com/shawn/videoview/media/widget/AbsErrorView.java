package com.shawn.videoview.media.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import com.shawn.videoview.R;

/**
 * 视频出现 Error
 * author: Shawn
 * time  : 2017/1/9 17:21
 */

public class AbsErrorView {

    private Context context;

    private View rootView;
    private Button btnReload;

    public AbsErrorView(Context context) {
        this.context = context;
        rootView = LayoutInflater.from(context).inflate(R.layout.video_error_abs, null);
        btnReload = (Button) rootView.findViewById(R.id.btn_reload);
        btnReload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onReloadClickListener.onReload();
            }
        });
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

    public void setOnReloadClickListener(OnReloadClickListener onReloadClickListener) {
        this.onReloadClickListener = onReloadClickListener;
    }

    private OnReloadClickListener onReloadClickListener;

    public interface OnReloadClickListener {
        void onReload();
    }
}
