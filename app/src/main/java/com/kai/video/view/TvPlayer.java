package com.kai.video.view;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.JsonObject;
import com.jeffmony.downloader.VideoDownloadManager;
import com.jeffmony.downloader.listener.IDownloadInfosCallback;
import com.jeffmony.downloader.model.VideoTaskItem;
import com.kai.video.DownloadActivity;
import com.kai.video.InfoActivity;
import com.kai.video.MyPlayerManager;
import com.kai.video.R;
import com.kai.video.bean.AndroidMediaPlayer;
import com.kai.video.bean.DeviceManager;
import com.kai.video.bean.FloatingSimul;
import com.kai.video.bean.Quality;
import com.kai.video.floatUtil.FloatWindow;
import com.kai.video.floatUtil.IFloatWindow;
import com.kai.video.floatUtil.MoveType;
import com.kai.video.floatUtil.Screen;
import com.kai.video.mediacodec.MediaCodecRenderView;
import com.kai.video.tool.DanamakuAdapter;
import com.kai.video.tool.DialogItemAdapter;
import com.kai.video.tool.LogUtil;
import com.kai.video.tool.MyDanmakuParser;

import com.kai.video.tool.SPUtils;
import com.ksyun.media.player.KSYTextureView;
import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.shuyu.gsyvideoplayer.listener.VideoAllCallBack;
import com.shuyu.gsyvideoplayer.utils.Debuger;
import com.shuyu.gsyvideoplayer.utils.GSYVideoType;
import com.shuyu.gsyvideoplayer.video.NormalGSYVideoPlayer;
import com.shuyu.gsyvideoplayer.video.base.GSYBaseVideoPlayer;
import com.shuyu.gsyvideoplayer.video.base.GSYVideoPlayer;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import master.flame.danmaku.controller.IDanmakuView;
import master.flame.danmaku.danmaku.loader.ILoader;
import master.flame.danmaku.danmaku.loader.IllegalDataException;
import master.flame.danmaku.danmaku.loader.android.DanmakuLoaderFactory;
import master.flame.danmaku.danmaku.model.BaseDanmaku;
import master.flame.danmaku.danmaku.model.DanmakuTimer;
import master.flame.danmaku.danmaku.model.IDisplayer;
import master.flame.danmaku.danmaku.model.android.DanmakuContext;
import master.flame.danmaku.danmaku.model.android.Danmakus;
import master.flame.danmaku.danmaku.model.android.SpannedCacheStuffer;
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser;
import master.flame.danmaku.danmaku.parser.IDataSource;
import master.flame.danmaku.ui.widget.DanmakuView;

public class TvPlayer extends NormalGSYVideoPlayer {
    private static float FLOAT_WIDTH = DeviceManager.isTv()?0.4f:0.8f;
    private static float FLOAT_HEIGHT =  FLOAT_WIDTH/16.0f*9.0f;
    private static float FLOAT_X = DeviceManager.isTv()?0.9f:0.8f;
    private static float FLOAT_Y = DeviceManager.isTv()?1.0f:0.6f;
    private static int ems_normal = DeviceManager.isTv()?11:7;
    private static int ems_full = DeviceManager.isTv()?35:30;
    private static float mdanmakuSpeed = 1.2f;
    private TextView qualityView;
    private ImageView failure;
    private String quality = "";
    private boolean localCache;
    private IFloatWindow iFloatWindow = null;
    private String url;
    private String title;
    private boolean cachewithPlay;
    private RelativeLayout mDamakuBar;
    private RelativeLayout surface;
    private ImageView setting;
    boolean mDanmaKuShow = true;
    private BaseDanmakuParser mParser;//解析器对象
    private IDanmakuView mDanmakuView;//弹幕view
    private DanmakuContext mDanmakuContext;
    private JSONArray danmuList;
    private boolean release;
    private long mDanmakuStartSeekPosition = -1;

    public boolean isLocalCache() {
        return localCache;
    }

    public void setLocalCache(boolean localCache) {
        this.localCache = localCache;
    }

    public boolean isRelease() {
        return release;
    }

    @Override
    public void setVideoAllCallBack(VideoAllCallBack mVideoAllCallBack) {

        super.setVideoAllCallBack(mVideoAllCallBack);
    }
    public boolean getCache(){
        return mCache;
    }
    public void setRelease(boolean release) {
        if (release)
            ((TvPlayer)getCurrentPlayer()).getDanmakuView().clearDanmakusOnScreen();
        this.release = release;
    }

    @Override
    public int getLayoutId() {
        return R.layout.layout_tv_player;
    }

    public TvPlayer(Context context) {
        super(context);
    }

    public TvPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }



    public void setDanmakuContext(DanmakuContext mDanmakuContext) {
        this.mDanmakuContext = mDanmakuContext;
    }
    public void showLoading(){
        mLoadingProgressBar.setVisibility(VISIBLE);
    }
    public void hideLoading(){
        mLoadingProgressBar.setVisibility(INVISIBLE);
    }
    public boolean isLoading(){
        return mLoadingProgressBar.getVisibility() == VISIBLE;
    }
    private Handler bufferHandler = new Handler();
    private Runnable bufferRunnable = new Runnable() {
        @Override
        public void run() {
            if (spUtils.getValue("danmu", 4) != 4 && getDanmakuView()!=null && getDanmakuView().isPrepared())
                resolveDanmakuSeek(TvPlayer.this, getCurrentPositionWhenPlaying());
        }
    };
    boolean buffering = false;
    @Override
    protected void updateStartImage() {
        super.updateStartImage();
        if (getCurrentState() == CURRENT_STATE_PLAYING && buffering) {
            LogUtil.d("tag", "buffered");
            danmakuOnResume();
            buffering = false;
        }
    }

    @Override
    protected void setViewShowState(View view, int visibility) {
        super.setViewShowState(view, visibility);
        if (visibility == VISIBLE && DeviceManager.isTv()) {
            findViewById(R.id.clock).setVisibility(INVISIBLE);
        }
    }

    @Override
    protected void hideAllWidget() {
        super.hideAllWidget();
        if (DeviceManager.isTv()) {
            findViewById(R.id.clock).setVisibility(VISIBLE);
        }

    }


    @Override
    protected void changeUiToPreparingShow() {
        super.changeUiToPreparingShow();
        buffering = true;
    }

    @Override
    protected void changeUiToPlayingBufferingShow() {
        //处于加载时停止弹幕的滚动
        super.changeUiToPlayingBufferingShow();
        bufferHandler.removeCallbacks(bufferRunnable);
        danmakuOnPause();
        LogUtil.d("tag", "buffering");
        buffering = true;
        danmakuOnPause();
    }

    @Override
    protected void changeUiToError() {
        //播放失败时停止弹幕的滚动
        super.changeUiToError();
        danmakuOnPause();
    }

    private void initDanmaku(Context context) {
        LogUtil.d("tag", "初始化弹幕上下文");
        // 设置是否禁止重叠
        HashMap<Integer, Boolean> overlappingEnablePair = new HashMap<Integer, Boolean>();
        overlappingEnablePair.put(BaseDanmaku.TYPE_SCROLL_RL, true);
        overlappingEnablePair.put(BaseDanmaku.TYPE_FIX_TOP, true);
        DanamakuAdapter danamakuAdapter = new DanamakuAdapter(mDanmakuView);
        mDanmakuContext = DanmakuContext.create();
        mDanmakuContext.setDanmakuStyle(IDisplayer.DANMAKU_STYLE_STROKEN, 3)
                .setDuplicateMergingEnabled(DeviceManager.isTv())
                .setScrollSpeedFactor(DeviceManager.isTv()?1.65f:1.2f)
                .setScaleTextSize(1.9f)
                .setCacheStuffer(new SpannedCacheStuffer(), danamakuAdapter) // 图文混排使用SpannedCacheStuffer
                .preventOverlapping(overlappingEnablePair);
        if (!isIfCurrentIsFullscreen()){
            loadDanmuSetting(4);
        }else
            loadDanmuSetting(spUtils.getValue("danmu", 4));
        if (mDanmakuView != null) {
            if (danmuList != null) {
                mParser = getParser();
            }

            mDanmakuView.setCallback(new master.flame.danmaku.controller.DrawHandler.Callback() {
                @Override
                public void updateTimer(DanmakuTimer timer) {

                }

                @Override
                public void drawingFinished() {

                }

                @Override
                public void danmakuShown(BaseDanmaku danmaku) {
                }

                @Override
                public void prepared() {
                    if (getDanmakuView() != null && getDanmakuContext() !=null) {
                        resolveDanmakuShow();
                        //Toast.makeText(getActivityContext(), "弹幕加载完毕" , Toast.LENGTH_SHORT).show();
                        if (getCurrentState() == CURRENT_STATE_PAUSE || buffering || failure.getVisibility() == VISIBLE) {
                            setDanmakuStartSeekPosition(getCurrentPositionWhenPlaying());
                            resolveDanmakuSeek(TvPlayer.this, getDanmakuStartSeekPosition());
                            danmakuOnPause();
                            setDanmakuStartSeekPosition(-1);
                        }
                        else {
                            setDanmakuStartSeekPosition(getCurrentPositionWhenPlaying());
                            resolveDanmakuSeek(TvPlayer.this, getDanmakuStartSeekPosition());
                            setDanmakuStartSeekPosition(-1);
                        }
                    }
                }
            });
            mDanmakuView.enableDanmakuDrawingCache(!DeviceManager.isTv());
        }


    }

    @Override
    public void onError(int what, int extra) {
        super.onError(what, extra);

    }
    public InfoActivity getActivity(){
        return ((InfoActivity)mContext);
    }
    private boolean showAlert = true;

    @Override
    public GSYBaseVideoPlayer startWindowFullscreen(Context context, boolean actionBar, boolean statusBar) {

        GSYBaseVideoPlayer gsyBaseVideoPlayer = super.startWindowFullscreen(context, actionBar, statusBar);
        if (gsyBaseVideoPlayer != null) {
            //如果是TV设备先死按键说明
            gsyBaseVideoPlayer.getVideoSarDen();
            TvPlayer gsyVideoPlayer = (TvPlayer) gsyBaseVideoPlayer;
            ((TvPlayer) gsyBaseVideoPlayer).initSputils(getActivity());
            gsyVideoPlayer.setQuality(quality);
            gsyVideoPlayer.mTitleTextView.setMaxEms(ems_full);
            gsyVideoPlayer.loadDanmuSetting(SPUtils.get(context).getValue("danmu", 4));
            if (DeviceManager.isTv()){
                if (showAlert){
                    showAlert = false;
                    gsyVideoPlayer.tvAlert.setVisibility(VISIBLE);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            gsyVideoPlayer.tvAlert.setVisibility(GONE);
                        }
                    }, 7000);
                }

            }
            //否则显示弹幕设置条
            else {
                getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                ((TvPlayer)gsyBaseVideoPlayer).initDanmuSetting(context);
                gsyBaseVideoPlayer.setRotateViewAuto(true);
                gsyBaseVideoPlayer.setOnlyRotateLand(true);
                gsyVideoPlayer.setting.setVisibility(VISIBLE);

            }
            gsyVideoPlayer.setDanmakuStartSeekPosition(getCurrentPositionWhenPlaying());
            gsyVideoPlayer.setDanmaKuShow(getDanmaKuShow());
            onPrepareDanmaku(gsyVideoPlayer);

        }
        return gsyBaseVideoPlayer;

    }


    @Override
    protected void resolveNormalVideoShow(View oldF, ViewGroup vp, GSYVideoPlayer gsyVideoPlayer) {
        super.resolveNormalVideoShow(oldF, vp, gsyVideoPlayer);
        if (gsyVideoPlayer != null) {
            TvPlayer gsyDanmaVideoPlayer = (TvPlayer) gsyVideoPlayer;
            setDanmaKuShow(gsyDanmaVideoPlayer.getDanmaKuShow());
            if (failure.getVisibility() == VISIBLE)
                gsyDanmaVideoPlayer.showFailure();
            if (isLoading())
                gsyDanmaVideoPlayer.showLoading();
            if (DeviceManager.isTv())
                onVideoPause();
            if (gsyDanmaVideoPlayer.getDanmakuView() != null &&
                    gsyDanmaVideoPlayer.getDanmakuView().isPrepared()) {
                resolveDanmakuSeek(this, gsyDanmaVideoPlayer.getCurrentPositionWhenPlaying());
                resolveDanmakuShow();
                releaseDanmaku(gsyDanmaVideoPlayer);
            }
        }
    }


    @Override
    protected void cloneParams(GSYBaseVideoPlayer from, GSYBaseVideoPlayer to) {
        LogUtil.d("tag",  " 传递数据");
        try {
            ((TvPlayer) to).danmuList = ((TvPlayer) from).danmuList;
            ((TvPlayer) to).title = ((TvPlayer) from).title;
            ((TvPlayer) to).url = ((TvPlayer) from).url;
            ((TvPlayer) to).cachewithPlay = ((TvPlayer) from).cachewithPlay;
            ((TvPlayer) to).coverUrl = ((TvPlayer) from).coverUrl;
            ((TvPlayer) to).quality = ((TvPlayer) from).quality;
            ((TvPlayer) to).localCache = ((TvPlayer) from).localCache;
            ((TvPlayer) to).release = ((TvPlayer) from).release;
        }catch (ClassCastException e){
            LogUtil.d("tag", "小窗模式");
        }
        finally {
            super.cloneParams(from, to);
        }

    }
    public void loadDanmuSetting(int i){
        float percent = 1f;
        int maxTop = DeviceManager.isTv()?5:5;
        int maxRL = DeviceManager.isTv()?20:15;
        switch (i){
            case 0:percent = 1.0f;maxTop = 5;break;
            case 1:percent = 0.5f;maxTop = 4;break;
            case 2:percent = 0.25f;maxTop = 3;break;
            case 3:percent = 0.15f;maxTop= 2;break;
            case 4:percent = 0f;maxTop = 0;break;
        }
        if (i == 4 && mDanmaKuShow){
            mDanmaKuShow = false;
            resolveDanmakuShow();
        }else if (i != 4 && !mDanmaKuShow){
            mDanmaKuShow = true;
            resolveDanmakuShow();
        }
        maxRL = (int) (percent * maxRL);
        HashMap<Integer, Integer> maxLinesPair = new HashMap<>();
        maxLinesPair.put(BaseDanmaku.TYPE_FIX_TOP, maxTop);
        maxLinesPair.put(BaseDanmaku.TYPE_SCROLL_RL, maxRL); // 滚动弹幕最大显示5行
        mDanmakuContext.setMaximumLines(maxLinesPair);
    }
    public void showDanmuState(){
        List<String> items = new ArrayList<>();
        items.add("弹幕状态：" + String.valueOf(danmuList == null?"未加载":"已加载"));
        items.add("弹幕数量：" + String.valueOf(danmuList == null?0:danmuList.length()));
        new CustomDialog.Builder(mContext)
                .setTitle("弹幕信息")
                .setList(items, null, -1)
                .setOnItemClickListener(new DialogItemAdapter.OnItemClickListener() {
                    @Override
                    public void onClick(String item, Object o, int position, CustomDialog dialog) {
                        dialog.dismiss();
                    }
                })
                .create()
                .show();
    }
    public void showVideoState(){
        String[] its = MyPlayerManager.getInfo(mContext);
        List<String> messages = new ArrayList<>();
        messages.add("视频分辨率：" + getCurrentVideoWidth() + " x " + getCurrentVideoHeight());
        messages.add("视频清晰度：" + Quality.get(getCurrentVideoWidth()));
        messages.add("视频来源站：" + getActivity().getUrl());
        messages.add("视频源地址：" + mUrl);
        messages.add("视频总时长：" + translateLong((long)getDuration()));
        messages.add("视频资源站：" + getActivity().apiNames.get(spUtils.getValue("api", 0)).replaceAll("\\s.*", ""));
        messages.add("视频解码器：" + its[1]);
        messages.add("视频解码器(EN)：" + its[2]);
        messages.add("播放器内核：" + its[0]);


        new CustomDialog.Builder(mContext)
                .setTitle("视频信息")
                .setList(messages, null, -1)
                .setOnItemClickListener(new DialogItemAdapter.OnItemClickListener() {
                    @Override
                    public void onClick(String item, Object o, int position, CustomDialog dialog) {
                        Toast.makeText(mContext, item, Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                    }
                })
                .create()
                .show();
    }
    public void showErrorAlert(){
        showsetting = true;
        AlertDialog dialog = new AlertDialog.Builder(mContext)
                .setTitle("视频报错")
                .setMessage("如果视频内容与描述不符或者无法播放，请点击立即反馈，管理系统将后续修正视频。")
                .setNegativeButton("暂时忽略", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        showsetting = false;
                    }
                })
                .setPositiveButton("立即反馈", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getActivity().deleteLog();
                        Snackbar.make(getSurface(), "反馈已经提交，请暂时切换其他线路，视频后续会修复", Snackbar.LENGTH_LONG).setAction("Action", null).setBackgroundTint(getResources().getColor(R.color.colorPrimary)).show();
                    }
                }).create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorPrimary));
        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.colorPrimary));
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).requestFocus();
    }
    private CustomDialog changeDialog;
    public void changeApi(){
        if (showsetting)
            return;
        if (changeDialog != null && changeDialog.isShowing())
            return;
        if (changeDialog == null){
            changeDialog = new CustomDialog.Builder(mContext)
                    .setTitle("线路设置")
                    .setMessage("请选择画质合适的线路观看")
                    .setList(getActivity().apiNames, null, spUtils.getValue("api", 0))
                    .setOnItemClickListener(new DialogItemAdapter.OnItemClickListener() {
                        @Override
                        public void onClick(String item, Object o, int i, CustomDialog dialog) {
                            spUtils.putValue("api", i);
                            getActivity().fullButton.requestFocus();
                            getActivity().changeButton.setText("当前："+ getActivity().apiNames.get(i).replaceAll("\\s.*", ""));
                            getActivity().getVideoFromResult();
                            dialog.dismiss();
                        }
                    })
                    .create();
            changeDialog.show();
        }else {
            changeDialog.setCurrent(spUtils.getValue("api", 0));
            changeDialog.resume();
        }

    }
    private void startWindowPlayer(){
        if (FloatWindow.get() != null) {
            return;
        }
        getStartButton().callOnClick();
        if (isIfCurrentIsFullscreen() && !DeviceManager.isTv())
            getFullscreenButton().callOnClick();
        FloatPlayerView floatPlayerView = new FloatPlayerView(mContext.getApplicationContext(), mUrl,
                getCurrentPositionWhenPlaying(), cachewithPlay);
        FloatWindow
                .with(mContext.getApplicationContext())
                .setView(floatPlayerView)
                .setWidth(Screen.width, FLOAT_WIDTH)
                .setHeight(Screen.width, FLOAT_HEIGHT)
                .setX(Screen.width, FLOAT_X)
                .setY(Screen.height, FLOAT_Y)
                .setMoveType(MoveType.slide)
                .setFilter(false)
                .setMoveStyle(500, new BounceInterpolator())
                .setOnDestroyListener(new FloatWindow.OnDestroyListener() {
                    @Override
                    public void onDestory(long time) {
                        FloatingSimul.setCurrentTime(time);
                    }
                })
                .build();
        iFloatWindow= FloatWindow.get();
        iFloatWindow.show();
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        mContext.startActivity(intent);
    }
    private static String[] settingArray = new String[]{"线路设置", "内核设置", "缓存设置", "倍速设置", "画面设置", "弹幕信息", "视频信息", "视频报错", "小窗模式[测试]"};
    private CustomDialog settingDialog;
    private boolean showsetting = false;
    public void showSetting(){
        if (showsetting)
            return;
        if (settingDialog == null)
            settingDialog = new CustomDialog.Builder(mContext)
                    .setMessage("播放设置页")
                    .setTitle("设置")
                    .setOnDismissListener(new CustomDialog.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            showsetting = false;
                        }
                    })
                    .setList(Arrays.asList(TvPlayer.settingArray), null, -1)
                    .setOnItemClickListener(new DialogItemAdapter.OnItemClickListener() {
                        @Override
                        public void onClick(String item, Object o, int which, CustomDialog dialog) {
                            LogUtil.d("tag", item);
                            showsetting = false;
                            if (which == 0){
                                changeApi();
                            }
                            else if (which == 1){
                                showVideoSetting();
                            }
                            else if (which == 2){
                                showDownloadSetting();
                            }
                            else if (which == 3){
                                showSpeedSetting();
                            }
                            else if (which == 4){
                                showScreenSetting();
                            }
                            else if (which == 5){
                                showDanmuState();
                            }
                            else if (which == 6){
                                showVideoState();
                            }
                            else if (which == 7){
                                showErrorAlert();
                            }
                            else if (which == 8){
                                startWindowPlayer();
                            }

                            dialog.hide();

                        }
                    })
                    .create();
        showsetting = true;
        settingDialog.show();
    }
    private static String[] cacheArray = new String[]{"立即缓存", "删除缓存", "缓存管理"};
    private void showDownloadSetting(){
        LogUtil.d("tag", showsetting + "sss");
        if (showsetting)
            return;
        showsetting = true;
        new CustomDialog.Builder(mContext)
                .setTitle("缓存设置")
                .setOnDismissListener(new CustomDialog.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        showsetting = false;
                    }
                })
                .setMessage("当前状态：" + (localCache?"已缓存":"未缓存"))
                .setList(Arrays.asList(TvPlayer.cacheArray), null, -1)
                .setOnItemClickListener(new DialogItemAdapter.OnItemClickListener() {
                    @Override
                    public void onClick(String item, Object o, int which, CustomDialog dialog) {
                        showsetting = false;
                        dialog.dismiss();
                        if (which == 0){
                            VideoDownloadManager downloadManager = VideoDownloadManager.getInstance();
                            downloadManager.fetchDownloadItems(new IDownloadInfosCallback() {
                                @Override
                                public void onDownloadInfos(List<VideoTaskItem> items) {
                                    for(VideoTaskItem item:items){
                                        if (item.getTitle().equals(mTitle + "|" + coverUrl)){
                                            final int state = item.getTaskState();
                                            ((Activity)mContext).runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Snackbar.make(getSurface(), state == 5?"视频已缓存":"视频正在缓存", Snackbar.LENGTH_LONG).setAction("Action", null).setBackgroundTint(getResources().getColor(R.color.colorPrimary)).show();
                                                }
                                            });
                                            downloadManager.removeDownloadInfosCallback(this);
                                            return;
                                        }
                                    }
                                    Snackbar.make(getSurface(), "视频开始缓存", Snackbar.LENGTH_LONG).setAction("Action", null).setBackgroundTint(getResources().getColor(R.color.colorPrimary)).show();
                                    VideoTaskItem item = new VideoTaskItem(mUrl, "", mTitle + "|" + coverUrl, "");
                                    downloadManager.startDownload(item);
                                    downloadManager.removeDownloadInfosCallback(this);
                                }
                            });


                        }else if (which == 1){
                            release();
                            Snackbar.make(getSurface(), "缓存删除成功", Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).setBackgroundTint(getResources().getColor(R.color.colorPrimary)).show();
                            VideoDownloadManager downloadManager = VideoDownloadManager.getInstance();
                            downloadManager.fetchDownloadItems(new IDownloadInfosCallback() {
                                @Override
                                public void onDownloadInfos(List<VideoTaskItem> items) {
                                    for(VideoTaskItem item : items){
                                        if (item.getTitle().equals(mTitle + "|" + coverUrl)){
                                            downloadManager.deleteVideoTask(item.getUrl(), true);
                                            break;
                                        }
                                    }
                                    downloadManager.removeDownloadInfosCallback(this);
                                    ((Activity)mContext).runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            getActivity().getVideoFromResult();
                                        }
                                    });
                                }
                            });

                        }else if (which == 2){
                            Intent intent = new Intent(mContext, DownloadActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            mContext.startActivity(intent);
                        }
                    }
                })
                .create().show();
    }
    private static String[] screenArray = new String[]{"默认", "16:9", "4:3", "全屏裁剪", "全屏拉伸"};
    public void showScreenSetting(){
        showsetting = true;
        int current = spUtils.getValue("screen", 0);
        if (current == GSYVideoType.SCREEN_MATCH_FULL)
            current = 3;
        else if (current == GSYVideoType.SCREEN_TYPE_FULL)
            current = 4;

        new CustomDialog.Builder(mContext)
                .setTitle("画面设置")
                .setMessage("设置播放器的画面比例")
                .setOnDismissListener(new CustomDialog.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        showsetting = false;
                    }
                })
                .setList(Arrays.asList(TvPlayer.screenArray), null, current)
                .setOnItemClickListener(new DialogItemAdapter.OnItemClickListener(){
                    @Override
                    public void onClick(String item, Object o, int which, CustomDialog dialog) {
                        showsetting = false;
                        if (which == 0){
                            MyPlayerManager.changeScreen(mContext, GSYVideoType.SCREEN_TYPE_DEFAULT);
                        }else if (which == 1){
                            MyPlayerManager.changeScreen(mContext, GSYVideoType.SCREEN_TYPE_16_9);
                        }else if (which == 2){
                            MyPlayerManager.changeScreen(mContext, GSYVideoType.SCREEN_TYPE_4_3);
                        }else if (which == 3){
                            MyPlayerManager.changeScreen(mContext, GSYVideoType.SCREEN_MATCH_FULL);
                        }else if (which == 4){
                            MyPlayerManager.changeScreen(mContext, GSYVideoType.SCREEN_TYPE_FULL);
                        }
                        resume(getCurrentPositionWhenPlaying());
                        dialog.dismiss();
                    }
                })
                .create()
                .show();

    }
    private CustomDialog danmuDialog;
    private static String danmuArray[] = new String[]{"全屏","半屏","顶部","条状","屏蔽"};
    public void showDamakuSetting(){
        if (showsetting)
            return;
        showsetting = true;
        if (danmuDialog == null) {
            danmuDialog = new CustomDialog.Builder(mContext)
                    .setTitle("弹幕设置")
                    .setMessage("设置弹幕覆盖的范围")
                    .setOnDismissListener(new CustomDialog.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            showsetting = false;
                        }
                    })
                    .setList(Arrays.asList(TvPlayer.danmuArray), null, SPUtils.get(mContext).getValue("danmu", 4))
                    .setOnItemClickListener(new DialogItemAdapter.OnItemClickListener() {
                        @Override
                        public void onClick(String item, Object o, int position, CustomDialog dialog) {
                            showsetting = false;
                            spUtils.putValue("danmu", position);
                            loadDanmuSetting(position);
                            dialog.hide();
                        }
                    })
                    .create();
            danmuDialog.show();
        }
        else{
            danmuDialog.resume();
        }


    }
    private static String[] speedArray = new String[]{"x 1.0", "x 1.25", "x 1.5", "x 1.75", "x 2.0"};
    public void showSpeedSetting(){
        showsetting = false;
        float currentSpeed =  getSpeed();
        int current = 0;

        if (currentSpeed == 1)
            current = 0;
        else if (currentSpeed == 1.25)
            current = 1;
        else if (currentSpeed == 1.5)
            current = 2;
        else if (currentSpeed == 1.75)
            current = 3;
        else if (currentSpeed == 2.0)
            current = 4;
        new CustomDialog.Builder(mContext)
                .setTitle("倍速设置")
                .setMessage("设置播放器倍速")
                .setOnDismissListener(new CustomDialog.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        showsetting = false;
                    }
                })
                .setList(Arrays.asList(TvPlayer.speedArray), null, current)
                .setOnItemClickListener(new DialogItemAdapter.OnItemClickListener() {
                    @Override
                    public void onClick(String item, Object o, int position, CustomDialog dialog) {
                        double s = 1 + position * 0.25;
                        setSpeed((float) s);
                        mDanmakuContext.setScrollSpeedFactor((float)( mdanmakuSpeed/ s));
                        showsetting = false;
                        dialog.dismiss();
                    }
                })
                .create()
                .show();
    }
    private CustomDialog mediaDialog;
    public static String[] mediasArray = new String[]{"IJK内核 [软解]", "IJK内核 [硬解]", "EXO内核 [软解]", "KSY内核 [软解]", "KSY内核 [硬解]"};
    public void showVideoSetting(){
        if (mediaDialog != null && mediaDialog.isShowing())
            return;
        if (mediaDialog == null){
            mediaDialog = new CustomDialog.Builder(mContext)
                    .setTitle("内核设置")
                    .setOnDismissListener(new CustomDialog.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            showsetting = false;
                        }
                    })
                    .setMessage("选择内核和解码器，硬解：适合电视，可启动画质引擎；软解：适合移动设备，流畅省电")
                    .setList(Arrays.asList(TvPlayer.mediasArray), null, MyPlayerManager.getCurrentKernel(mContext))
                    .setOnItemClickListener(new DialogItemAdapter.OnItemClickListener() {
                        @Override
                        public void onClick(String item, Object o, int i, CustomDialog dialog) {
                            onVideoPause();
                            MyPlayerManager.changeMode(mContext, i);
                            spUtils.putValue("media", i);
                            resume(getCurrentPositionWhenPlaying());
                            showsetting = false;
                            dialog.dismiss();
                        }
                    })
                    .create();
            mediaDialog.show();
        }
        else {
            mediaDialog.resume();
        }
    }

    public RelativeLayout getSurface() {
        return surface;
    }
    private EditText editText;
    public void initDanmuSetting(final Context context){
        mDamakuBar.setVisibility(VISIBLE);
        ImageButton danmuSetting = (ImageButton) findViewById(R.id.setting1);
        danmuSetting.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showDamakuSetting();
            }
        });
        editText = (EditText)findViewById(R.id.danmu_editer);


        editText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {

                if (hasFocus){
                    getStartButton().callOnClick();
                    cancelDismissControlViewTimer();
                }else {

                }
            }
        });
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && KeyEvent.KEYCODE_ENTER == event.getKeyCode() && KeyEvent.ACTION_DOWN == event.getAction())){
                    if (!editText.getText().toString().isEmpty()) {
                        addDanmaku(editText.getText().toString());
                        editText.setText("");
                        InputMethodManager manager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                        if (manager != null)
                            manager.hideSoftInputFromWindow(editText.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                        getStartButton().callOnClick();
                        startDismissControlViewTimer();
                        return true;
                    }
                }
                return false;
            }
        });
        Button send = (Button) findViewById(R.id.send);
        send.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editText.getText().toString().isEmpty())
                    return;
                addDanmaku(editText.getText().toString());
                editText.setText("");
                InputMethodManager manager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                if (manager != null)
                    manager.hideSoftInputFromWindow(editText.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                getStartButton().callOnClick();
                startDismissControlViewTimer();
            }
        });
    }
    private void setDialogProgress(int progress){
        if (mDialogSeekTime != null)
            mDialogSeekTime.setText(translateLong((long)getDuration() * progress / 100));
        if (mDialogProgressBar != null)
            mDialogProgressBar.setProgress(progress);

    }
    public class SeekTask extends AsyncTask{
        private boolean started = false;
        private int progress = 0;
        private int remainTime = 2;
        private Handler handler = new Handler();
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            started = true;
            showProgressDialog((float) 1, translateLong(getGSYVideoManager().getCurrentPosition()), getGSYVideoManager().getBufferedPercentage(), translateLong((long)getDuration()), 100);
            progress = (int) ((float)getCurrentPositionWhenPlaying()/(float) getDuration()*100);
            LogUtil.d("tag", "ss" + progress);
            mDialogProgressBar.setProgress(progress);
        }

        public boolean isStarted() {
            return started;
        }

        public void activateDelay() {
            this.remainTime = 1;
        }
        public void add(int a){
            if (a > 0)
                add();
            else
                sub();
        }
        public void add(){
            progress++;
            if (progress > 100)
                progress = 100;
            activateDelay();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    setDialogProgress(progress);
                }
            });
        }
        public void sub(){
            progress--;
            if (progress < 0)
                progress = 0;
            activateDelay();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    setDialogProgress(progress);
                }
            });
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            Log.d("tag", remainTime + "s");
            while (remainTime >= 0){
                try {
                    Thread.sleep(500);
                    remainTime--;
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            dismissProgressDialog();
            if (progress == 0)
                seekTo(1000);
            else if (progress == 100)
                seekTo(getDuration() - 1000);
            else
                seekTo(getDuration() * progress /100);
            cancel(true);
        }
    }
    private SeekTask seekTask;
    public void setSeekAdd(int add){
        if (seekTask.isCancelled()){
            seekTask = new SeekTask();
            seekTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }else if (!seekTask.isStarted()){
            seekTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
        seekTask.add(add);
    }



    public static String translateLong(Long time) {
        try{
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            format.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));
            String dateTime = format.format(time);
            Date date = format.parse(dateTime);

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);

            Calendar orgin = Calendar.getInstance();
            orgin.setTimeInMillis(0);

            int hour = calendar.get(Calendar.HOUR_OF_DAY) - orgin.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE) - orgin.get(Calendar.MINUTE);
            int second = calendar.get(Calendar.SECOND) - orgin.get(Calendar.SECOND);

            StringBuilder builder = new StringBuilder();
            if(hour!=0){
                if (hour < 10)
                    builder.append(0 + "");
                builder.append(hour).append(":");
            }
            if(minute!=0){
                if (minute < 10)
                    builder.append(0 + "");
                builder.append(minute).append(":");
            }else {
                builder.append("00:");
            }
            if(second!=0){
                if (second < 10)
                    builder.append(0 + "");
                builder.append(second);
            }else {
                builder.append("00");
            }

            return builder.toString();
        }catch (Exception e){
            e.printStackTrace();
            return "";
        }
    }


    public void resume(long currentTime, VideoTaskItem item){
        release();
        setUp(item.getFilePath(), cachewithPlay, mTitle);
        setSeekOnStart(currentTime);
        startPlayLogic();
    }
    public void resume(long currentTime){
        release();
        setUp(mUrl, cachewithPlay, mTitle);
        setSeekOnStart(currentTime);
        startPlayLogic();
    }

    public void showFailure(){
        mStartButton.setVisibility(INVISIBLE);
        mLoadingProgressBar.setVisibility(INVISIBLE);
        failure.setVisibility(VISIBLE);
    }
    public void hideFailure(){
        failure.setVisibility(INVISIBLE);
    }

    public void setQuality(String text) {
        int color = R.color.color_low;
        switch (text){
            case "4K 蓝光HDR":color = R.color.color_4k;break;
            case "1080P 原画":color = R.color.color_1080;break;
            case "720P 超清":color = R.color.color_720;break;
            default:break;
        }
        this.quality = text;
        qualityView.setText(quality);
        if (!quality.isEmpty()){
            qualityView.setVisibility(VISIBLE);
        }else
            qualityView.setVisibility(INVISIBLE);
        qualityView.getBackground().setColorFilter(mContext.getResources().getColor(color), PorterDuff.Mode.SRC);


    }
    public String getQuality() {
        return quality;
    }
    private SPUtils spUtils = null;
    private RelativeLayout tvAlert;
    public void initSputils(Activity activity){
        spUtils = SPUtils.get(activity);
    }
    @Override
    protected void init(final Context context) {
        super.init(context);
        mTitleTextView.setMaxEms(ems_normal);
        initSputils(getActivity());
        seekTask = new SeekTask();
        ImageButton pip;
        ImageButton download;
        failure = (ImageView) findViewById(R.id.failure);
        tvAlert = (RelativeLayout) findViewById(R.id.tv_alert);
        qualityView = (TextView) findViewById(R.id.quality);
        qualityView.setText(quality);
        pip = (ImageButton) findViewById(R.id.pip);
        download = (ImageButton) findViewById(R.id.download);
        download.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar.make(getSurface(), "视频开始缓存，可在通知栏或缓存管理中查看", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).setBackgroundTint(getResources().getColor(R.color.colorPrimary)).show();
                VideoTaskItem item = new VideoTaskItem(mUrl, "", mTitle + "|" + coverUrl, "");
                VideoDownloadManager.getInstance().startDownload(item);
            }
        });
        pip.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (FloatWindow.get() != null) {
                    return;
                }
                getStartButton().callOnClick();
                getFullscreenButton().callOnClick();
                FloatPlayerView floatPlayerView = new FloatPlayerView(context.getApplicationContext(), mUrl,
                        getCurrentPositionWhenPlaying(), cachewithPlay);
                FloatWindow
                        .with(context.getApplicationContext())
                        .setView(floatPlayerView)
                        .setWidth(Screen.width, 0.8f)
                        .setHeight(Screen.width, 0.45f)
                        .setX(Screen.width, 0.8f)
                        .setY(Screen.height, 0.6f)
                        .setMoveType(MoveType.slide)
                        .setFilter(false)
                        .setMoveStyle(500, new BounceInterpolator())
                        .build();
                iFloatWindow= FloatWindow.get();
                iFloatWindow.show();
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                context.startActivity(intent);

            }
        });
        mDanmakuView = (DanmakuView) findViewById(R.id.danmaku_view);
        mDamakuBar = (RelativeLayout) findViewById(R.id.danmu_bar);
        surface = (RelativeLayout) findViewById(R.id.surface_container);
        setting = (ImageView) findViewById(R.id.setting);
        if (DeviceManager.isTv())
            setting.setVisibility(INVISIBLE);
        setting.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showSetting();
            }
        });
        initDanmaku(context);

    }

    public IDanmakuView getDanmakuView() {
        return mDanmakuView;
    }

    public DanmakuContext getDanmakuContext() {
        return mDanmakuContext;
    }
    public MyDanmakuParser createParser(InputStream stream) {
        if (stream == null) {
            LogUtil.i("tag", "弹幕信息缺失，生成空白弹幕控件");
            return new MyDanmakuParser() {

                @Override
                protected Danmakus parse() {
                    return new Danmakus();
                }
            };
        }
        ILoader loader = DanmakuLoaderFactory.create(DanmakuLoaderFactory.TAG_ACFUN);

        try {
            loader.load(stream);
        } catch (IllegalDataException e) {
            e.printStackTrace();
        }
        MyDanmakuParser parser = new MyDanmakuParser();
        IDataSource<?> dataSource = loader.getDataSource();
        parser.load(dataSource);
        LogUtil.i("tag", "弹幕流加载完毕");
        return parser;

    }

    public void destory(){
        releaseDanmaku(this);
        mDanmakuContext = null;
        seekTask.cancel(true);
        seekTask = null;
        if (changeDialog != null)
            changeDialog.dismiss();
        if (danmuDialog != null)
            danmuDialog.dismiss();
        if (settingDialog != null)
            settingDialog.dismiss();
    }

    /**
     释放弹幕控件
     */
    private void releaseDanmaku(TvPlayer danmakuVideoPlayer) {
        if (danmakuVideoPlayer != null && danmakuVideoPlayer.getDanmakuView() != null && danmakuVideoPlayer.getDanmakuView().isShown()) {
            Debuger.printfError("release Danmaku!");
            danmakuVideoPlayer.getDanmakuView().release();
        }
    }

    /**
     模拟添加弹幕数据
     */
    private void addDanmaku(String text) {
        BaseDanmaku danmaku = mDanmakuContext.mDanmakuFactory.createDanmaku(BaseDanmaku.TYPE_SCROLL_RL);
        if (danmaku == null || mDanmakuView == null) {
            return;
        }
        danmaku.text = text;
        danmaku.padding = 5;
        danmaku.priority = 8;  // 可能会被各种过滤器过滤并隐藏显示，所以提高等级
        danmaku.isLive = true;
        danmaku.setTime(mDanmakuView.getCurrentTime() + 500);
        danmaku.textSize = 27f;
        danmaku.textColor = Color.RED;
        //danmaku.textShadowColor = Color.GRAY;
        danmaku.borderColor = Color.WHITE;
        mDanmakuView.addDanmaku(danmaku);

    }
    private void resolveDanmakuShow() {
        post(new Runnable() {
            @Override
            public void run() {
                if (mDanmaKuShow) {
                    if (!getDanmakuView().isShown())
                        getDanmakuView().show();
                    //mToogleDanmaku.setText("弹幕关");
                } else {
                    if (getDanmakuView().isShown()) {
                        getDanmakuView().hide();
                    }
                    //mToogleDanmaku.setText("弹幕开");
                }
            }
        });
    }
    public TvPlayer getcurrentPlayer(){
        return (TvPlayer) getCurrentPlayer();
    }

    public void loadDanmu(JSONArray danmuList) {
        mParser = null;
        this.danmuList = danmuList;
        onPrepareDanmaku((TvPlayer) getCurrentPlayer());
        setDanmakuStartSeekPosition(getCurrentPositionWhenPlaying());
    }



    private String coverUrl = "";

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public String getReferer(){
        StringBuilder builder = new StringBuilder();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    Connection.Response response = Jsoup.connect("https://555dy.fun/static/js/playerconfig.js?t=20210906")
                            .ignoreContentType(true)
                            .execute();
                    Pattern pattern = Pattern.compile("MacPlayerConfig.player_list=(\\{.*\\})\\,"
                            + "MacPlayerConfig.downer_list=");
                    Matcher matcher = pattern.matcher(response.body());
                    if (matcher.find()) {
                        JSONObject object = new JSONObject(matcher.group(1));
                        builder.append(object.getJSONObject("fuckapp").getString("parse"));
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        };
        FutureTask<Integer> futureTask = new FutureTask<Integer>(runnable, 1);
        try {
            futureTask.get();
        }catch (Exception e){
            e.printStackTrace();
        }
        return builder.toString();
    }
    @Override
    public boolean setUp(String url, boolean cacheWithPlay, String title) {
        //VideoType.get(url);
        this.url = url;
        this.title = title;
        this.cachewithPlay = cacheWithPlay;
        mParser = null;
        danmuList = null;

        if (isRelease())
            return false;
        setRelease(false);
        if (url.contains("zy.acampt.com") || url.contains("qie2.suipq.com")){

            LogUtil.d("tag", "特殊切片");
            Map<String, String> map = new HashMap<>(2);
            map.put("Origin", "https://jhpc.manduhu.com");
            map.put("Referer", "https://jhpc.manduhu.com/jianghu.php?url=" + url);
            return setUp(url, cacheWithPlay, null, map, title);
        }else if (url.contains("wy.bigmao.top") && url.endsWith("mp4")){
            LogUtil.d("tag", "特殊请求");
            Map<String, String> map = new HashMap<>(2);
            map.put("Rederer", "https://www.nfmovies.com/js/player/m3u8.html?" + System.currentTimeMillis());
            map.put("Range", "bytes=0-1");
            return setUp(url, cacheWithPlay, null, map, title);
        }
        return super.setUp(url, cacheWithPlay, title);
    }

    @Override
    public void onPrepared() {
        super.onPrepared();
        onPrepareDanmaku(this);
    }
    private void syncDanmu(){
        long mCurrentPosition = getCurrentPositionWhenPlaying();
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (Math.abs(getDanmakuView().getCurrentTime() - mCurrentPosition) > 1500)
                    bufferHandler.post(bufferRunnable);
            }
        }).start();
    }
    @Override
    public void onVideoPause() {
        super.onVideoPause();
        danmakuOnPause();
        getActivity().updateTime(getCurrentPositionWhenPlaying());
    }

    @Override
    public void onVideoResume() {
        super.onVideoResume();
        danmakuOnResume();
        syncDanmu();
        getActivity().updateTime(getCurrentPositionWhenPlaying());
    }


    @Override
    public void onVideoResume(boolean isResume) {
        super.onVideoResume(isResume);
        syncDanmu();
        danmakuOnResume();
    }

    @Override
    protected void clickStartIcon() {
        super.clickStartIcon();
        if (mCurrentState == CURRENT_STATE_PLAYING) {
            danmakuOnResume();
            //loadDanmuSetting(SPUtils.get(mContext).getValue("danmu", 4));
            syncDanmu();
        } else if (mCurrentState == CURRENT_STATE_PAUSE) {
            danmakuOnPause();
            //loadDanmuSetting(4);
        }
    }

    @Override
    public void onCompletion() {
        releaseDanmaku(this);
    }

    @Override
    public void onSeekComplete() {
        super.onSeekComplete();
        //如果已经初始化过的，直接seek到对于位置
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mHadPlay && getDanmakuView() != null && getDanmakuView().isPrepared()) {
                    int time = getCurrentPositionWhenPlaying();
                    if (time == 0){
                        //出现回弹现象
                        time = getCurrentPositionWhenPlaying();
                    }
                    resolveDanmakuSeek(TvPlayer.this, time);
                } else if (mHadPlay && getDanmakuView() != null && !getDanmakuView().isPrepared()) {
                    //如果没有初始化过的，记录位置等待
                    setDanmakuStartSeekPosition(getCurrentPositionWhenPlaying());
                }
                if (getCurrentState() == CURRENT_STATE_PAUSE)
                    getcurrentPlayer().onVideoResume();
            }
        }, 500);

        LogUtil.d("tag", "state:" + getCurrentState());
        getActivity().updateTime(getCurrentPositionWhenPlaying());
    }
    protected void danmakuOnPause() {
        getActivity().updateTime(getCurrentPositionWhenPlaying());
        if (mDanmakuView != null && mDanmakuView.isPrepared()) {
            mDanmakuView.pause();
        }
    }

    protected void danmakuOnResume() {
        if (mDanmakuView != null && mDanmakuView.isPrepared() && mDanmakuView.isPaused()) {
            mDanmakuView.resume();
        }
    }

    public void hideAllDialog(){
        if (mediaDialog != null)
            mediaDialog.hide();
        if (danmuDialog != null)
            danmuDialog.hide();
        if (settingDialog != null)
            settingDialog.hide();
        if (changeDialog != null)
            changeDialog.hide();
    }
    /**
     开始播放弹幕
     */
    private void onPrepareDanmaku(TvPlayer gsyVideoPlayer) {
        if (gsyVideoPlayer.getDanmakuView() != null && !gsyVideoPlayer.getDanmakuView().isPrepared() && gsyVideoPlayer.getParser() != null) {
            gsyVideoPlayer.getDanmakuView().prepare(gsyVideoPlayer.getParser(),
                    gsyVideoPlayer.getDanmakuContext());
        }
    }

    /**
     弹幕偏移
     */
    private void resolveDanmakuSeek(TvPlayer gsyVideoPlayer, long time) {
        if (mHadPlay && gsyVideoPlayer.getDanmakuView() != null && gsyVideoPlayer.getDanmakuView().isPrepared()) {
            gsyVideoPlayer.getDanmakuView().seekTo(time);
        }
    }


    public BaseDanmakuParser getParser() {
        if (mParser == null) {
            if (danmuList != null) {
                mParser = createParser(new ByteArrayInputStream(danmuList.toString().getBytes()));
            }
        }
        return mParser;
    }


    public long getDanmakuStartSeekPosition() {
        return mDanmakuStartSeekPosition;
    }

    public void setDanmakuStartSeekPosition(long danmakuStartSeekPosition) {
        this.mDanmakuStartSeekPosition = danmakuStartSeekPosition;
    }

    public void setDanmaKuShow(boolean danmaKuShow) {
        mDanmaKuShow = danmaKuShow;
    }

    public boolean getDanmaKuShow() {
        return mDanmaKuShow;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        super.onProgressChanged(seekBar, progress, fromUser);
        //每百分之五进行一次弹幕矫正
        if (progress % 2 == 0){
            syncDanmu();
        }
        getActivity().updateTime(getCurrentPositionWhenPlaying());
    }

}