package com.shawn.videoview.media.widget;

import android.view.View;
import android.widget.MediaController;

/**
 * author: Shawn
 * time  : 2017/1/4 14:55
 */

public interface IVideoController {

    void setAnchorView(View view);

    void setEnabled(boolean enabled);

    void setMediaPlayer(MediaController.MediaPlayerControl player);

    void showOnce(View view);

    void setVisibility(int visibility);

    boolean isVisible();

}
