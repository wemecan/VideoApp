package com.kai.video.view;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.shuyu.gsyvideoplayer.GSYVideoManager;

import java.util.Timer;
import java.util.TimerTask;

/**
 * 适配了悬浮窗的view
 * Created by guoshuyu on 2017/12/25.
 */

public class FloatPlayerView extends FrameLayout {

    FloatingVideo videoPlayer;
    String url;
    long currentTime;
    boolean cache;


    public FloatPlayerView(Context context, String url, long currentTime, boolean cache) {
        super(context);
        this.url = url;
        this.currentTime = currentTime;
        this.cache = cache;
        init();
    }

    public FloatPlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FloatPlayerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        videoPlayer = new FloatingVideo(getContext());
        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.gravity = Gravity.CENTER;

        addView(videoPlayer, layoutParams);


        videoPlayer.setUp(url, cache, "小窗");
        videoPlayer.setSeekOnStart(currentTime);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                videoPlayer.getStartButton().callOnClick();
            }
        },1000);


        //增加封面
        /*ImageView imageView = new ImageView(getContext());
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setImageResource(R.mipmap.xxx1);
        videoPlayer.setThumbImageView(imageView);*/

        //是否可以滑动调整
        videoPlayer.setIsTouchWiget(false);

    }


    public void onPause() {
        videoPlayer.getCurrentPlayer().onVideoPause();
    }

    public void onResume() {
        videoPlayer.getCurrentPlayer().onVideoResume();
    }

}
