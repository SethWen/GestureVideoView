/*
 * Copyright (C) 2015 Zhang Rui <bbcallen@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.shawn.videoview.media;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TableLayout;

import com.shawn.videoview.media.callback.OnVideoGestureListener;
import com.shawn.videoview.media.callback.OnVideoLifeCycleListener;
import com.shawn.videoview.media.util.TimeUtil;
import com.shawn.videoview.media.widget.AbsControllerView;
import com.shawn.videoview.media.widget.AbsErrorView;
import com.shawn.videoview.media.widget.AbsGestureView;
import com.shawn.videoview.media.widget.AbsPreparingView;
import com.shawn.videoview.media.widget.IRenderView;
import com.shawn.videoview.media.widget.IVideoController;
import com.shawn.videoview.media.widget.SurfaceRenderView;
import com.shawn.videoview.media.widget.TextureRenderView;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;

import tv.danmaku.ijk.media.player.AndroidMediaPlayer;
import tv.danmaku.ijk.media.player.IMediaPlayer;

public class GestureVideoView extends FrameLayout implements AbsControllerView.MediaPlayerControl,
        GestureDetector.OnDoubleTapListener, GestureDetector.OnGestureListener, IMediaPlayer.OnCompletionListener,
        IMediaPlayer.OnBufferingUpdateListener, IMediaPlayer.OnVideoSizeChangedListener, IMediaPlayer.OnErrorListener,
        IMediaPlayer.OnInfoListener, IMediaPlayer.OnPreparedListener, IMediaPlayer.OnSeekCompleteListener {

    private String TAG = "IjkVideoView";
    // settable by the client
    private Uri mUri;
    private Map<String, String> mHeaders;

    // all possible internal states
    private static final int STATE_ERROR = -1;
    private static final int STATE_IDLE = 0;
    private static final int STATE_PREPARING = 1;
    private static final int STATE_PREPARED = 2;
    private static final int STATE_PLAYING = 3;
    private static final int STATE_PAUSED = 4;
    private static final int STATE_PLAYBACK_COMPLETED = 5;
    /**
     * mCurrentState is a VideoView object's current state.
     * mTargetState is the state that a method caller intends to reach.
     * For instance, regardless the VideoView object's current state, calling pause() intends to bring the object to a
     * target state of STATE_PAUSED.
     */
    private int mCurrentState = STATE_IDLE;
    private int mTargetState = STATE_IDLE;

    /**
     * 视频渲染 View 类型
     */
    public static final int RENDER_NONE = 0;
    public static final int RENDER_SURFACE_VIEW = 1;
    public static final int RENDER_TEXTURE_VIEW = 2;
    private int mRenderMode = RENDER_TEXTURE_VIEW;

    /**
     * 更新视频进度事件
     */
    public static final int UPDATE_VIDEO_POSITION = 100;
    /**
     * 刷新字幕事件
     */
    public static final int UPDATE_VIDEO_SUBTITLE = 200;

    /**
     * 视频比例
     */
    private int mCurrentAspectRatio = IRenderView.AR_16_9_FIT_PARENT;

    /**
     * 播放器内核，此处的使用 IjkPlayer 的 Android 实现类
     */
    private AndroidMediaPlayer mPlayer = null;
    private IRenderView.ISurfaceHolder mSurfaceHolder = null;
    private InfoHudViewHolder mHudViewHolder;

    /**
     * Subtitle rendering widget overlaid on top of the video.
     */
    // private RenderingWidget mSubtitleWidget;


    private Context mAppContext;
    private AudioManager audioManager;
    /**
     * 最大系统音量
     */
    private int maxVolume;
    /**
     * 手势检测器
     */
    private GestureDetector mGestureDetector;

    /**
     * 视频渲染 View
     */
    private IRenderView mRenderView;
    /**
     * 视频控制条
     */
    private AbsControllerView mController;
    /**
     * 视频准备 View
     */
    private AbsPreparingView mPreparingView;
    /**
     * 视频发生错误 View todo 发生错误后的刷新操作
     */
    private AbsErrorView mErrorView;
    //    /**
    //     * 屏幕亮度调节 View
    //     */
    //    private AbsBrightnessView mBrightnessView;
    //    /**
    //     * 媒体音量调节 View
    //     */
    //    private AbsVolumeView mVolumeView;
    /**
     * 手势操作显示的 View
     */
    private AbsGestureView mGestureView;

    /**
     * 设备屏幕宽，高
     */
    private int screenWidth, screenHeight;
    /**
     * 视频宽高
     */
    private int mVideoWidth, mVideoHeight;
    /**
     * 渲染 View 宽高
     */
    private int mSurfaceWidth, mSurfaceHeight;
    /**
     * todo 视频像素比例？？？不甚理解！！！
     */
    private int mVideoSarNum, mVideoSarDen;

    /**
     * 正在做横向滑动手势
     */
    private boolean isHorScrolling;
    /**
     * 正在做左侧纵向滑动手势
     */
    private boolean isLeftVerScrolling;
    /**
     * 正在做右侧纵向滑动手势
     */
    private boolean isRightVerScrolling;

    private boolean mCanPause = true;
    private boolean mCanSeekBack = true;
    private boolean mCanSeekForward = true;

    /**
     * 横向手势累积进度
     */
    private int cumuHorProgress;
    /**
     * 横向手势之前的进度
     */
    private int preSeekProgress;

    private int mVideoRotationDegree;
    private int mCurrentBufferPercentage;
    private long mSeekWhenPrepared;  // recording the seek position while preparing

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_VIDEO_POSITION:
                    if (mController != null) {
                        mController.setPosition(getCurrentPosition());
                        mController.setProgress(position2Progress(getCurrentPosition()));
                        mHandler.sendEmptyMessageDelayed(UPDATE_VIDEO_POSITION, 500);
                    }
                    break;
                case UPDATE_VIDEO_SUBTITLE:
                    // TODO: 2017/1/12 刷字幕
                    break;
                default:
                    break;
            }
        }
    };

    public GestureVideoView(Context context) {
        super(context);
        init(context);
    }

    public GestureVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public GestureVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public GestureVideoView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        initAssistantField();
        initRender(mRenderMode);

        initController();
        initPreparingView();
        initErrorView();
        //        initBrightnessView();
        //        initVolumeView();
        initGestureView();

        initVolume(context);
        initBrightness(context);

        // FIXME: 2017/1/12 在获取视频信息前无法得到视频的尺寸，所以 IjkVideoView 处于全屏状态，修复之
        mVideoWidth = 0;
        mVideoHeight = 0;

        setFocusable(true);
        setFocusableInTouchMode(true);
        requestFocus();

        mCurrentState = STATE_IDLE;
        mTargetState = STATE_IDLE;
    }

    /**
     * 初始化手势操作时显示的 View
     */
    private void initGestureView() {
        mGestureView = new AbsGestureView(getContext());

        LayoutParams params = new LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER);

        View gestureRootView = mGestureView.getRootView();

        gestureRootView.setLayoutParams(params);

        this.addView(gestureRootView);
    }

    /**
     * 初始化 辅助字段
     */
    private void initAssistantField() {
        mAppContext = getContext().getApplicationContext();

        // 获取屏幕宽高
        DisplayMetrics dm = getResources().getDisplayMetrics();
        screenWidth = dm.widthPixels;
        screenHeight = dm.heightPixels;
        Log.i(TAG, "initAssistantField: " + "screenWidth = " + screenWidth + ", screenHeight = " + screenHeight);

        // 创建手势识别器
        mGestureDetector = new GestureDetector(getContext(), this);
        mGestureDetector.setIsLongpressEnabled(true);
        mGestureDetector.setOnDoubleTapListener(this);
    }

    /**
     * 初始化系统音量
     */
    private void initVolume(Context context) {
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        // 系统音量范围 [0, 15]
        maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        Log.i(TAG, "initVolume: " + "maxVolume = " + maxVolume + ", currentVolume = " + currentVolume);

        // 设置音量进度
        int curVolumePer = (int) (100.0F / maxVolume * currentVolume);
        mGestureView.setVolumeProgress(curVolumePer);
    }

    /**
     * 初始化屏幕亮度
     *
     * @param context
     */
    private void initBrightness(Context context) {
        Window window = ((Activity) context).getWindow();
        float curBrightness = window.getAttributes().screenBrightness;  // 该值跟随系统的话，数值为(-∞, 0)，否则为 [0,1]
        if (curBrightness <= 0.00f) curBrightness = 0.50f;  // 系统默认情况更改为一半亮度

        // 设置起始屏幕亮度
        WindowManager.LayoutParams params = window.getAttributes();
        params.screenBrightness = curBrightness;
        window.setAttributes(params);

        int curBrightnessPer = (int) (curBrightness * 100.0F);
        Log.i(TAG, "initBrightness: curBrightness = " + curBrightness + ", curBrightnessPer = " + curBrightnessPer);

        // 设置亮度进度条
        mGestureView.setBrightnessProgress(curBrightnessPer);
    }

    /**
     * 初始化 控制器
     */
    private void initController() {
        mController = new AbsControllerView(getContext(), mPlayer);

        LayoutParams params = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM);

        View controllerRootView = mController.getRootView();

        controllerRootView.setLayoutParams(params);

        this.addView(controllerRootView);
    }

    //    /**
    //     * 初始化音量调节 View
    //     */
    //    private void initVolumeView() {
    //        mVolumeView = new AbsVolumeView(getContext());
    //
    //        LayoutParams params = new LayoutParams(
    //                ViewGroup.LayoutParams.WRAP_CONTENT,
    //                ViewGroup.LayoutParams.WRAP_CONTENT,
    //                Gravity.END | Gravity.CENTER_VERTICAL);
    //
    //        View volumeRootView = mVolumeView.getRootView();
    //
    //        volumeRootView.setLayoutParams(params);
    //
    //        this.addView(volumeRootView);
    //    }

    //    /**
    //     * 初始化亮度调节 View
    //     */
    //    private void initBrightnessView() {
    //        mBrightnessView = new AbsBrightnessView(getContext());
    //
    //        LayoutParams params = new LayoutParams(
    //                ViewGroup.LayoutParams.WRAP_CONTENT,
    //                ViewGroup.LayoutParams.WRAP_CONTENT,
    //                Gravity.START | Gravity.CENTER_VERTICAL);
    //
    //        View brightnessRootView = mBrightnessView.getRootView();
    //
    //        brightnessRootView.setLayoutParams(params);
    //
    //        this.addView(brightnessRootView);
    //    }

    /**
     * 初始化 ErrorView
     */
    private void initErrorView() {
        // TODO: 2017/1/9 等待具体实例化  Abs
        mErrorView = new AbsErrorView(getContext());

        LayoutParams params = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                Gravity.CENTER);

        View errorRootView = mErrorView.getRootView();
        errorRootView.setLayoutParams(params);
        
        mErrorView.setOnReloadClickListener(() -> {
            Log.i(TAG, "onReload: ");
            openVideo();
        });

        this.addView(errorRootView);
    }

    /**
     * 初始化 PreparingView
     */
    private void initPreparingView() {
        mPreparingView = new AbsPreparingView(getContext());

        LayoutParams params = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                Gravity.CENTER);

        View preparingRootView = mPreparingView.getRootView();

        preparingRootView.setLayoutParams(params);

        this.addView(preparingRootView);
    }

    /**
     * 设置视频渲染 view
     *
     * @param renderView
     */
    public void setRenderView(IRenderView renderView) {
        if (mRenderView != null) {
            if (mPlayer != null) mPlayer.setDisplay(null);

            View renderUIView = mRenderView.getView();
            mRenderView.removeRenderCallback(mSHCallback);
            mRenderView = null;
            removeView(renderUIView);
        }

        if (renderView == null) return;

        mRenderView = renderView;
        renderView.setAspectRatio(mCurrentAspectRatio);
        if (mVideoWidth > 0 && mVideoHeight > 0)
            renderView.setVideoSize(mVideoWidth, mVideoHeight);
        if (mVideoSarNum > 0 && mVideoSarDen > 0)
            renderView.setVideoSampleAspectRatio(mVideoSarNum, mVideoSarDen);

        View renderUIView = mRenderView.getView();
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER);
        renderUIView.setLayoutParams(params);
        addView(renderUIView);

        mRenderView.addRenderCallback(mSHCallback);
        mRenderView.setVideoRotation(mVideoRotationDegree);
    }

    public void initRender(int renderMode) {
        switch (renderMode) {
            case RENDER_NONE:
                setRenderView(null);
                break;
            case RENDER_TEXTURE_VIEW: {
                TextureRenderView renderView = new TextureRenderView(getContext());
                if (mPlayer != null) {
                    renderView.getSurfaceHolder().bindToMediaPlayer(mPlayer);
                    renderView.setVideoSize(mPlayer.getVideoWidth(), mPlayer.getVideoHeight());
                    renderView.setVideoSampleAspectRatio(mPlayer.getVideoSarNum(), mPlayer.getVideoSarDen());
                    renderView.setAspectRatio(mCurrentAspectRatio);
                }
                setRenderView(renderView);
                break;
            }
            case RENDER_SURFACE_VIEW: {
                SurfaceRenderView renderView = new SurfaceRenderView(getContext());
                setRenderView(renderView);
                break;
            }
            default:
                Log.e(TAG, String.format(Locale.getDefault(), "invalid render %d\n", renderMode));
                break;
        }
    }

    public void setHudView(TableLayout tableLayout) {
        mHudViewHolder = new InfoHudViewHolder(getContext(), tableLayout);
    }

    /**
     * Sets video path.
     *
     * @param path the path of the video.
     */
    public void setVideoPath(String path) {
        setVideoURI(Uri.parse(path));
    }

    /**
     * Sets video URI.
     *
     * @param uri the URI of the video.
     */
    public void setVideoURI(Uri uri) {
        setVideoURI(uri, null);
    }

    /**
     * Sets video URI using specific headers.
     *
     * @param uri     the URI of the video.
     * @param headers the headers for the URI request.
     *                Note that the cross domain redirection is allowed by default, but that can be
     *                changed with key/value pairs through the headers parameter with
     *                "android-allow-cross-domain-redirect" as the key and "0" or "1" as the value
     *                to disallow or allow cross domain redirection.
     */
    private void setVideoURI(Uri uri, Map<String, String> headers) {
        mUri = uri;
        mHeaders = headers;
        mSeekWhenPrepared = 0;
        openVideo();
        requestLayout();
        invalidate();
    }

    public void stopPlayback() {
        if (mPlayer != null) {
            if (mOnVideoLifeCycleListener != null) mOnVideoLifeCycleListener.onStop(mPlayer);

            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
            if (mHudViewHolder != null) mHudViewHolder.setMediaPlayer(null);
            mCurrentState = STATE_IDLE;
            mTargetState = STATE_IDLE;
            AudioManager am = (AudioManager) mAppContext.getSystemService(Context.AUDIO_SERVICE);
            am.abandonAudioFocus(null);

        }
    }

    //    @TargetApi(Build.VERSION_CODES.M)
    private void openVideo() {
        if (mUri == null || mSurfaceHolder == null) {
            // not ready for playback just yet, will try again later
            return;
        }
        // we shouldn't clear the target state, because somebody might have called start() previously
        release(false);

        audioManager.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

        try {
            if (mPlayer == null) mPlayer = new AndroidMediaPlayer();

            if (mOnVideoLifeCycleListener != null) mOnVideoLifeCycleListener.onPreparing(mPlayer);
            if (mPreparingView != null) mPreparingView.setVisibility(VISIBLE);

            mPlayer.setOnPreparedListener(this);
            mPlayer.setOnVideoSizeChangedListener(this);
            mPlayer.setOnCompletionListener(this);
            mPlayer.setOnErrorListener(this);
            mPlayer.setOnInfoListener(this);
            mPlayer.setOnBufferingUpdateListener(this);

            mCurrentBufferPercentage = 0;
            if (mUri.getScheme() != null && (mUri.getScheme().equals("http") || mUri.getScheme().equals("https"))) {
                // 调用网络视频
                Log.i(TAG, "Loading web URI: " + mUri.toString());
            } else {
                // 调用本地视频
                Log.i(TAG, "Loading local URI: " + mUri.toString());
            }
            mPlayer.setDataSource(mAppContext, mUri, mHeaders);

            bindSurfaceHolder(mPlayer, mSurfaceHolder);
            mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mPlayer.setScreenOnWhilePlaying(true);
            mPlayer.prepareAsync();

            if (mHudViewHolder != null) mHudViewHolder.setMediaPlayer(mPlayer);

            // we don't set the target state here either, but preserve the target state that was there before.
            mCurrentState = STATE_PREPARING;
        } catch (IOException | IllegalArgumentException ex) {
            Log.w(TAG, "Unable to open content: " + mUri, ex);
            mCurrentState = STATE_ERROR;
            mTargetState = STATE_ERROR;
            mOnVideoLifeCycleListener.onError(mPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
        }
    }

    public void setMediaController(IVideoController controller) {
        //        if (mMediaController != null) {
        //            mMediaController.hide();
        //        }
        //        mMediaController = controller;
        //                attachMediaController();
    }

    // REMOVED: mSHCallback
    private void bindSurfaceHolder(IMediaPlayer mp, IRenderView.ISurfaceHolder holder) {
        if (mp == null)
            return;

        if (holder == null) {
            mp.setDisplay(null);
            return;
        }

        holder.bindToMediaPlayer(mp);
    }

    IRenderView.IRenderCallback mSHCallback = new IRenderView.IRenderCallback() {
        @Override
        public void onSurfaceChanged(@NonNull IRenderView.ISurfaceHolder holder, int format, int w, int h) {
            if (holder.getRenderView() != mRenderView) {
                Log.e(TAG, "onSurfaceChanged: unmatched render callback\n");
                return;
            }

            mSurfaceWidth = w;
            mSurfaceHeight = h;

            boolean isValidState = (mTargetState == STATE_PLAYING);
            boolean hasValidSize = !mRenderView.shouldWaitForResize() || (mVideoWidth == w && mVideoHeight == h);
            if (mPlayer != null && isValidState && hasValidSize) {
                if (mSeekWhenPrepared != 0) {
                    seekTo(mSeekWhenPrepared);
                }
                start();
            }
        }

        @Override
        public void onSurfaceCreated(@NonNull IRenderView.ISurfaceHolder holder, int width, int height) {
            if (holder.getRenderView() != mRenderView) {
                Log.e(TAG, "onSurfaceCreated: unmatched render callback\n");
                return;
            }

            mSurfaceHolder = holder;
            if (mPlayer != null) {
                bindSurfaceHolder(mPlayer, holder);
            } else {
                openVideo();
            }
        }

        @Override
        public void onSurfaceDestroyed(@NonNull IRenderView.ISurfaceHolder holder) {
            if (holder.getRenderView() != mRenderView) {
                Log.e(TAG, "onSurfaceDestroyed: unmatched render callback\n");
                return;
            }

            // after we return from this we can't use the surface any more
            mSurfaceHolder = null;
            // REMOVED: if (mMediaController != null) mMediaController.hide();
            // REMOVED: release(true);
            releaseWithoutStop();
        }
    };

    public void releaseWithoutStop() {
        if (mPlayer != null)
            mPlayer.setDisplay(null);
    }

    /*
     * release the media mPlayer in any state
     */
    public void release(boolean cleartargetstate) {
        if (mPlayer != null) {
            mPlayer.reset();
            mPlayer.release();
            mPlayer = null;
            // REMOVED: mPendingSubtitleTracks.clear();
            mCurrentState = STATE_IDLE;
            if (cleartargetstate) {
                mTargetState = STATE_IDLE;
            }
            AudioManager am = (AudioManager) mAppContext.getSystemService(Context.AUDIO_SERVICE);
            am.abandonAudioFocus(null);
            // TODO: 2017/1/11
            if (mHandler != null) {
                mHandler.removeCallbacksAndMessages(this);
                mHandler = null;
            }
        }
    }

    @Override
    public boolean onTrackballEvent(MotionEvent ev) {
        if (isInPlaybackState() && mController != null) {
            //            toggleMediaControlsVisiblity();
        }
        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean isKeyCodeSupported = keyCode != KeyEvent.KEYCODE_BACK &&
                keyCode != KeyEvent.KEYCODE_VOLUME_UP &&
                keyCode != KeyEvent.KEYCODE_VOLUME_DOWN &&
                keyCode != KeyEvent.KEYCODE_VOLUME_MUTE &&
                keyCode != KeyEvent.KEYCODE_MENU &&
                keyCode != KeyEvent.KEYCODE_CALL &&
                keyCode != KeyEvent.KEYCODE_ENDCALL;
        if (isInPlaybackState() && isKeyCodeSupported && mController != null) {
            if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK ||
                    keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) {
                if (isPlaying()) {
                    pause();
                    //                    mController.setVisibility(VISIBLE);
                } else {
                    start();
                    //                    mController.setVisibility(GONE);
                }
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY) {
                if (!isPlaying()) {
                    start();
                    //                    mController.setVisibility(GONE);
                }
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP
                    || keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE) {
                if (isPlaying()) {
                    pause();
                    //                    mController.setVisibility(VISIBLE);
                }
                return true;
            } else {
                //                toggleMediaControlsVisiblity();
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    private void toggleMediaControlsVisiblity() {
        if (mController.isVisible()) {
            mController.setVisibility(GONE);
        } else {
            mController.setVisibility(VISIBLE);
        }
    }

    @Override
    public void start() {
        if (isInPlaybackState()) {
            if (mOnVideoLifeCycleListener != null) mOnVideoLifeCycleListener.onStart(mPlayer);

            mPlayer.start();
            mCurrentState = STATE_PLAYING;


            // TODO: 2017/1/11 开启 Handler 消息
        }
        mTargetState = STATE_PLAYING;
    }

    @Override
    public void pause() {
        if (isInPlaybackState()) {
            if (mPlayer.isPlaying()) {
                if (mOnVideoLifeCycleListener != null) mOnVideoLifeCycleListener.onPause(mPlayer);

                mPlayer.pause();
                mCurrentState = STATE_PAUSED;

                // TODO: 2017/1/11 移除 Handler 消息
            }
        }
        mTargetState = STATE_PAUSED;
    }

    public void suspend() {
        release(false);
    }

    public void resume() {
        openVideo();
    }

    @Override
    public long getDuration() {
        if (isInPlaybackState()) {
            return mPlayer.getDuration();
        }
        return -1L;
    }

    @Override
    public long getCurrentPosition() {
        if (isInPlaybackState()) {
            return mPlayer.getCurrentPosition();
        }
        return 0L;
    }

    @Override
    public void seekTo(long msec) {
        if (isInPlaybackState()) {
            mPlayer.seekTo(msec);
            mSeekWhenPrepared = 0;
        } else {
            mSeekWhenPrepared = msec;
        }
    }

    @Override
    public boolean isPlaying() {
        return isInPlaybackState() && mPlayer.isPlaying();
    }

    @Override
    public int getBufferPercentage() {
        if (mPlayer != null) {
            return mCurrentBufferPercentage;
        }
        return 0;
    }

    private boolean isInPlaybackState() {
        return (mPlayer != null &&
                mCurrentState != STATE_ERROR &&
                mCurrentState != STATE_IDLE &&
                mCurrentState != STATE_PREPARING);
    }

    @Override
    public boolean canPause() {
        return mCanPause;
    }

    @Override
    public boolean canSeekBackward() {
        return mCanSeekBack;
    }

    @Override
    public boolean canSeekForward() {
        return mCanSeekForward;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }

    //    private void initRender() {
    //        initRender(RENDER_TEXTURE_VIEW);
    //    }

    /**
     * 进度百分比 -> 视频 Position
     *
     * @param percent
     * @return
     */
    private long progress2Position(int percent) {
        long position = (long) (percent / 100F * getDuration());
        position = fixPosition(position);
        return position;
    }

    /**
     * 修正视频位置数值
     *
     * @param position
     * @return
     */
    private long fixPosition(long position) {
        if (position < 0) position = 0;
        if (position > getDuration()) position = getDuration();
        return position;
    }

    /**
     * 视频 Position -> 进度百分比
     *
     * @param position
     * @return
     */
    private int position2Progress(long position) {
        int progress = (int) (position * 1.0F / getDuration() * 100);
        progress = fixProgress(progress);
        return progress;
    }

    /**
     * 修正进度数值
     *
     * @param progress
     * @return
     */
    private int fixProgress(int progress) {
        if (progress < 0) progress = 0;
        if (progress > 100) progress = 100;
        return progress;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isInPlaybackState() && mController != null) {
            //            toggleMediaControlsVisiblity();
        }
        if (event.getAction() == MotionEvent.ACTION_UP && isHorScrolling) { // 横向滑动手势抬起之后
            long targetPosition = progress2Position(cumuHorProgress + preSeekProgress);
            Log.i(TAG, "onTouchEvent: targetPosition = " + targetPosition);
            mPlayer.seekTo(targetPosition);
            cumuHorProgress = 0;
            isHorScrolling = false;
            mGestureView.setSeekViewVisibility(GONE);
            mHandler.sendEmptyMessageDelayed(UPDATE_VIDEO_POSITION, 500);
        } else if (event.getAction() == MotionEvent.ACTION_UP && isLeftVerScrolling) {  // 左侧纵向滑动手势抬起之后
            isLeftVerScrolling = false;
            mGestureView.setVolumeViewVisibility(GONE);
        } else if (event.getAction() == MotionEvent.ACTION_UP && isRightVerScrolling) {  // 右侧纵向滑动手势抬起之后
            isRightVerScrolling = false;
            mGestureView.setBrightnessViewVisibility(GONE);
        } else {
            return mGestureDetector.onTouchEvent(event);
        }
        return true;
    }

    private OnVideoGestureListener mOnVideoGesturelistener;

    public void setOnVideoGestureListener(OnVideoGestureListener onVideoGestureListener) {
        this.mOnVideoGesturelistener = onVideoGestureListener;
    }

    @Override
    public boolean onDown(MotionEvent motionEvent) {
        Log.i(TAG, "onDown: " + motionEvent.getAction());
        preSeekProgress = mController.getProgress();
        return true;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {
        Log.i(TAG, "onShowPress: " + motionEvent.getAction());
    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        Log.i(TAG, "onSingleTapUp: " + motionEvent.getAction());
        if (mOnVideoGesturelistener != null) mOnVideoGesturelistener.onSingleClickGesture();
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent downEvent, MotionEvent moveEvent, float distanceX, float distanceY) {
        float absX = Math.abs(distanceX);
        float absY = Math.abs(distanceY);
        float curX = downEvent.getX();

        int leftVerPercent;
        int rightVerPercent;
        int horPercent;

        if (absX > 10 && Math.abs(absY) < 10 && !isLeftVerScrolling && !isRightVerScrolling) {
            isHorScrolling = true;
            mGestureView.setSeekViewVisibility(VISIBLE);
            mHandler.removeMessages(UPDATE_VIDEO_POSITION);
            // 横向滑动
            horPercent = (int) (distanceX / mSurfaceWidth * 100 * -1);
            cumuHorProgress += horPercent;
            if (mOnVideoGesturelistener != null) mOnVideoGesturelistener.onHorizontalGesture(horPercent);
            Log.i(TAG, "onScroll: onHorizontalGesture --> "
                    + "horPercent = " + horPercent + ", cumuHorProgress = " + cumuHorProgress);
            updateVideoProgress(fixProgress(horPercent + mController.getProgress()));
        } else if (absX < 10 && absY > 10 && curX < screenWidth / 2 && !isHorScrolling && !isRightVerScrolling) {
            isLeftVerScrolling = true;
            mGestureView.setVolumeViewVisibility(VISIBLE);
            // 左侧纵向滑动
            leftVerPercent = (int) (distanceY / mSurfaceHeight * 100);
            if (mOnVideoGesturelistener != null) mOnVideoGesturelistener.onLeftVerticalGesture(leftVerPercent);
            Log.i(TAG, "onScroll: onLeftVerticalGesture" + leftVerPercent);
            updateSystemVolume(leftVerPercent);
        } else if (absX < 10 && absY > 10 && curX > screenWidth / 2 && !isHorScrolling && !isLeftVerScrolling) {
            isRightVerScrolling = true;
            mGestureView.setBrightnessViewVisibility(VISIBLE);
            // 右侧纵向滑动
            rightVerPercent = (int) (distanceY / mSurfaceHeight * 100);
            if (mOnVideoGesturelistener != null) mOnVideoGesturelistener.onRightVerticalGesture(rightVerPercent);
            Log.i(TAG, "onScroll: onRightVerticalGesture = " + rightVerPercent);
            updateSystemBrightness(rightVerPercent);
        }
        return true;
    }

    /**
     * 更新 视频进度
     *
     * @param progress
     */
    private void updateVideoProgress(int progress) {
        mController.setProgress(progress);
        if (preSeekProgress < progress) {
            mGestureView.setSeekText(TimeUtil.formatTime(progress2Position(progress)), true);
        } else {
            mGestureView.setSeekText(TimeUtil.formatTime(progress2Position(progress)), false);
        }
    }

    /**
     * 调节当前应用中屏幕亮度
     *
     * @param brightnessPer
     */
    private void updateSystemBrightness(int brightnessPer) {
        int curBrightnessPer = brightnessPer + mGestureView.getBrightnessProgress();

        Window window = ((Activity) getContext()).getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.screenBrightness = curBrightnessPer * 0.01F;
        if (params.screenBrightness > 1.0f) params.screenBrightness = 1.0f;
        if (params.screenBrightness < 0.01f) params.screenBrightness = 0.01f;
        window.setAttributes(params);

        mGestureView.setBrightnessProgress(curBrightnessPer);

        Log.i(TAG, "updateSystemBrightness: curBrightnessPer = " + curBrightnessPer
                + ", screenBrightness = " + params.screenBrightness);
    }

    /**
     * 调节系统媒体音量
     *
     * @param volumePer
     */
    private void updateSystemVolume(int volumePer) {
        int targetVolumePer = volumePer + mGestureView.getVolumeProgress();
        mGestureView.setVolumeProgress(targetVolumePer);

        int targetVolume = (int) (targetVolumePer * 1.0F / 100 * maxVolume);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, targetVolume, 0);

        Log.i(TAG, "updateSystemVolume: targetVolumePer = " + targetVolumePer + ", targetVolume = " + targetVolume);
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {
        Log.i(TAG, "onLongPress: " + motionEvent.getAction());
    }

    @Override
    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        Log.i(TAG, "onFling: " + motionEvent.getAction());
        return true;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
        Log.i(TAG, "onSingleTapConfirmed: " + motionEvent.getAction());
        setControllerVisibility();
        if (mOnVideoGesturelistener != null) mOnVideoGesturelistener.onSingleClickGesture();
        return true;
    }

    /**
     * 设置视频控制器可见性
     */
    private void setControllerVisibility() {
        if (mController != null) {
            mController.setVisibility(mController.isVisible() ? GONE : VISIBLE);
        }
    }

    @Override
    public boolean onDoubleTap(MotionEvent motionEvent) {
        Log.i(TAG, "onDoubleTap: " + motionEvent.getAction());
        if (mOnVideoGesturelistener != null) mOnVideoGesturelistener.onDoubleClickGesture();

        // 暂停或开始
        if (mPlayer != null) {
            mController.setPlayState();
        }
        return true;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent motionEvent) {
        Log.i(TAG, "onDoubleTapEvent: " + motionEvent.getAction());
        return true;
    }

    @Override
    public void onBufferingUpdate(IMediaPlayer iMediaPlayer, int percent) {
        mCurrentBufferPercentage = percent;
    }

    @Override
    public void onCompletion(IMediaPlayer iMediaPlayer) {
        if (mOnVideoLifeCycleListener != null) mOnVideoLifeCycleListener.onCompletion(mPlayer);

        mCurrentState = STATE_PLAYBACK_COMPLETED;
        mTargetState = STATE_PLAYBACK_COMPLETED;
        if (mController != null) {
            //            mController.setVisibility(GONE);
        }
    }

    @Override
    public boolean onError(IMediaPlayer iMediaPlayer, int what, int extra) {
        Log.i(TAG, "Error: " + "[" + what + ", " + extra + "]");
        mCurrentState = STATE_ERROR;
        mTargetState = STATE_ERROR;

        if (mController != null) mController.setVisibility(GONE);
        if (mErrorView != null) mErrorView.setVisibility(VISIBLE);

        /**
         *  If an error handler has been supplied, use it and finish.
         */
        if (mOnVideoLifeCycleListener != null) {
            if (mOnVideoLifeCycleListener.onError(mPlayer, what, extra)) {
                return true;
            }
        }
        return true;
    }

    @Override
    public boolean onInfo(IMediaPlayer iMediaPlayer, int what, int extra) {
        if (mOnVideoLifeCycleListener != null) mOnVideoLifeCycleListener.onInfo(iMediaPlayer);

        switch (what) {
            case IMediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING:
                Log.d(TAG, "MEDIA_INFO_VIDEO_TRACK_LAGGING:");
                break;
            case IMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
                Log.d(TAG, "MEDIA_INFO_VIDEO_RENDERING_START:");
                break;
            case IMediaPlayer.MEDIA_INFO_BUFFERING_START:
                Log.d(TAG, "MEDIA_INFO_BUFFERING_START:");
                break;
            case IMediaPlayer.MEDIA_INFO_BUFFERING_END:
                Log.d(TAG, "MEDIA_INFO_BUFFERING_END:");
                break;
            case IMediaPlayer.MEDIA_INFO_NETWORK_BANDWIDTH:
                Log.d(TAG, "MEDIA_INFO_NETWORK_BANDWIDTH: " + extra);
                break;
            case IMediaPlayer.MEDIA_INFO_BAD_INTERLEAVING:
                Log.d(TAG, "MEDIA_INFO_BAD_INTERLEAVING:");
                break;
            case IMediaPlayer.MEDIA_INFO_NOT_SEEKABLE:
                Log.d(TAG, "MEDIA_INFO_NOT_SEEKABLE:");
                break;
            case IMediaPlayer.MEDIA_INFO_METADATA_UPDATE:
                Log.d(TAG, "MEDIA_INFO_METADATA_UPDATE:");
                break;
            case IMediaPlayer.MEDIA_INFO_UNSUPPORTED_SUBTITLE:
                Log.d(TAG, "MEDIA_INFO_UNSUPPORTED_SUBTITLE:");
                break;
            case IMediaPlayer.MEDIA_INFO_SUBTITLE_TIMED_OUT:
                Log.d(TAG, "MEDIA_INFO_SUBTITLE_TIMED_OUT:");
                break;
            case IMediaPlayer.MEDIA_INFO_VIDEO_ROTATION_CHANGED:
                mVideoRotationDegree = extra;
                Log.d(TAG, "MEDIA_INFO_VIDEO_ROTATION_CHANGED: " + extra);
                if (mRenderView != null)
                    mRenderView.setVideoRotation(extra);
                break;
            case IMediaPlayer.MEDIA_INFO_AUDIO_RENDERING_START:
                Log.d(TAG, "MEDIA_INFO_AUDIO_RENDERING_START:");
                break;
        }
        return true;
    }

    @Override
    public void onPrepared(IMediaPlayer iMediaPlayer) {
        mCurrentState = STATE_PREPARED;

        // Get the capabilities of the mPlayer for this stream
        // REMOVED: Metadata

        if (mPreparingView != null) mPreparingView.setVisibility(GONE);

        if (mOnVideoLifeCycleListener != null) mOnVideoLifeCycleListener.onPrepared(mPlayer);

        if (mController != null) {
            mController.setEnabled(true);
            mController.setPlayer(mPlayer);
            mController.setDuration(mPlayer.getDuration());
            mHandler.sendEmptyMessage(UPDATE_VIDEO_POSITION);
        }
        mVideoWidth = iMediaPlayer.getVideoWidth();
        mVideoHeight = iMediaPlayer.getVideoHeight();

        long seekToPosition = mSeekWhenPrepared;  // mSeekWhenPrepared may be changed after seekTo() call
        if (seekToPosition != 0) {
            seekTo(seekToPosition);
        }
        if (mVideoWidth != 0 && mVideoHeight != 0) {
            // REMOVED: getHolder().setFixedSize(mVideoWidth, mVideoHeight);
            if (mRenderView != null) {
                mRenderView.setVideoSize(mVideoWidth, mVideoHeight);
                mRenderView.setVideoSampleAspectRatio(mVideoSarNum, mVideoSarDen);
                if (!mRenderView.shouldWaitForResize()
                        || mSurfaceWidth == mVideoWidth && mSurfaceHeight == mVideoHeight) {
                    // We didn't actually change the size (it was already at the size we need), so we won't get a
                    // "surface changed" callback, so start the video here instead of in the callback.
                    if (mTargetState == STATE_PLAYING) {
                        start();
                        if (mController != null) {
                            mController.setVisibility(VISIBLE);
                        }
                    } else if (!isPlaying() && (seekToPosition != 0 || getCurrentPosition() > 0)) {
                        if (mController != null) {
                            // Show the media controls when we're paused into a video and make 'em stick.
                            mController.setVisibility(VISIBLE);
                        }
                    }
                }
            }
        } else {
            // We don't know the video size yet, but should start anyway.
            // The video size might be reported to us later.
            if (mTargetState == STATE_PLAYING) {
                start();
            }
        }
    }

    @Override
    public void onSeekComplete(IMediaPlayer iMediaPlayer) {

    }

    @Override
    public void onVideoSizeChanged(IMediaPlayer iMediaPlayer, int i, int i1, int i2, int i3) {
        mVideoWidth = iMediaPlayer.getVideoWidth();
        mVideoHeight = iMediaPlayer.getVideoHeight();
        mVideoSarNum = iMediaPlayer.getVideoSarNum();
        mVideoSarDen = iMediaPlayer.getVideoSarDen();
        if (mVideoWidth != 0 && mVideoHeight != 0) {
            if (mRenderView != null) {
                mRenderView.setVideoSize(mVideoWidth, mVideoHeight);
                mRenderView.setVideoSampleAspectRatio(mVideoSarNum, mVideoSarDen);
            }
            // REMOVED: getHolder().setFixedSize(mVideoWidth, mVideoHeight);
            requestLayout();
        }
    }

    private OnVideoLifeCycleListener mOnVideoLifeCycleListener;

    public void setmOnVideoLifeCycleListener(OnVideoLifeCycleListener onVideoLifeCycleListener) {
        this.mOnVideoLifeCycleListener = onVideoLifeCycleListener;
    }
}
