package com.shawn.videoview.media.callback;

import tv.danmaku.ijk.media.player.IMediaPlayer;

/**
 * author: Shawn
 * time  : 2017/1/10 11:10
 */

public interface OnVideoLifeCycleListener {

    void onPreparing(IMediaPlayer iMediaPlayer);

    void onPrepared(IMediaPlayer iMediaPlayer);

    void onStart(IMediaPlayer iMediaPlayer);

    void onPause(IMediaPlayer iMediaPlayer);

    void onStop(IMediaPlayer iMediaPlayer);

    void onInfo(IMediaPlayer iMediaPlayer);

    void onCompletion(IMediaPlayer iMediaPlayer);

    boolean onError(IMediaPlayer iMediaPlayer, int what, int extra);

}
