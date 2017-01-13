package com.shawn.videoview.media.callback;

import android.support.annotation.IntRange;

/**
 * author: Shawn
 * time  : 2017/1/3 16:50
 */

public interface OnVideoGestureListener {

    void onHorizontalGesture(@IntRange(from = 0, to = 100) int percent);

    void onLeftVerticalGesture(@IntRange(from = 0, to = 100) int percent);

    void onRightVerticalGesture(@IntRange(from = 0, to = 100) int percent);

    void onDoubleClickGesture();

    void onSingleClickGesture();

}
