package com.kai.video;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.UiModeManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.jeffmony.downloader.model.VideoTaskItem;
import com.kai.video.bean.Api;
import com.kai.video.bean.BaseActivity;
import com.kai.video.bean.DanmuTool;
import com.kai.video.bean.DeliverVideoTaskItem;
import com.kai.video.bean.DeviceManager;
import com.kai.video.bean.FloatingSimul;
import com.kai.video.bean.Quality;
import com.kai.video.floatUtil.FloatWindow;
import com.kai.video.sniffing.SniffingCallback;
import com.kai.video.sniffing.SniffingVideo;
import com.google.android.material.snackbar.Snackbar;
import com.kai.video.bean.History;
import com.kai.video.tool.DialogItemAdapter;
import com.kai.video.tool.IPTool;
import com.kai.video.tool.LinearTopSmoothScroller;
import com.kai.video.tool.LogUtil;
import com.kai.video.tool.SPUtils;
import com.kai.video.tool.SelectionItemAdapter;
import com.kai.video.tool.Util;
import com.kai.video.tool.VideoTool;
import com.kai.video.view.CustomDialog;
import com.kai.video.view.TvPlayer;
import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.shuyu.gsyvideoplayer.listener.GSYMediaPlayerListener;
import com.shuyu.gsyvideoplayer.listener.GSYSampleCallBack;
import com.shuyu.gsyvideoplayer.utils.OrientationUtils;
import com.shuyu.gsyvideoplayer.video.base.GSYVideoView;
import com.tencent.smtt.sdk.TbsVideo;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;


import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tv.danmaku.ijk.media.player.IjkMediaPlayer;


public class InfoActivity extends BaseActivity implements View.OnFocusChangeListener {
    private int initial_api = 0;
    private boolean forground = true;
    private LocalBroadcastManager localBroadcastManager;
    private LocalReceiver localReceiver;
    private IntentFilter intentFilter;
    private OrientationUtils orientationUtils;
    String name = "";
    private boolean direct = false;
    private TextView header;
    //url????????????????????????URL?????????
    String url = "";
    //???????????????????????????????????????????????????
    private VideoTool videoTool;
    //?????????????????????TV
    //???????????????????????????
    public TvPlayer player;
    public Button fullButton;
    private Button nextButton;
    public Button changeButton;
    private TextView description;
    private TextView peroid;
    private RecyclerView recyclerView;
    private StaggeredGridLayoutManager layoutManager;
    private SPUtils spUtils;
    SelectionItemAdapter adapter;
    @RequiresApi(api = 23)
    private void requestAlertWindowPermission() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, 1);
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {

    }

    private Handler fullHandler = new Handler();
    private Runnable fullRunnable = new Runnable() {
        @Override
        public void run() {
            if (!player.isIfCurrentIsFullscreen() && DeviceManager.isTv()) {
                player.getFullscreenButton().callOnClick();
                //player.getcurrentPlayer().getStartButton().callOnClick();
            }

        }
    };
    private void changeOtherApi(){
        Snackbar.make(player.getcurrentPlayer().getSurface(), "????????????????????????????????????????????????", Snackbar.LENGTH_LONG)
                .setAction("Action", null)
                .setBackgroundTint(getResources().getColor(R.color.colorPrimary)).show();
        int new_api = spUtils.getValue("api", 0);
        if (new_api == initial_api && new_api > 0)
            new_api = 0;
        new_api++;
        //??????????????????????????????
        if (new_api >= apiNames.size()) {
            new_api = 0;
        }
        //???????????????????????????
        if (new_api == initial_api){
            Snackbar.make(player.getcurrentPlayer().getSurface(), "???????????????????????????????????????????????????????????????????????????????????????", Snackbar.LENGTH_LONG)
                    .setAction("Action", null)
                    .setBackgroundTint(getResources().getColor(R.color.colorPrimary)).show();
            spUtils.putValue("api", new_api );
            changeButton.setText("?????????"+apiNames.get(new_api).replaceAll("\\s.*", ""));
            return;
        }
        spUtils.putValue("api", new_api);
        changeButton.setText("?????????"+apiNames.get(spUtils.getValue("api", 0)).replaceAll("\\s.*", ""));
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                getVideoFromResult();
            }
        }, 1000);

    }
    public List<String> apiNames = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppTheme);
        MyPlayerManager.loadMode(InfoActivity.this);
        FloatingSimul.setCurrentTime(0);
        apiNames = Lists.newArrayList(Api.getApis().keys());
        if (DeviceManager.getDevice() == DeviceManager.DEVICE_TV){
            requestWindowFeature(Window.FEATURE_NO_TITLE);

            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
            setContentView(R.layout.activity_info);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            LogUtil.d("TAG", "Running on a TV Device");
        }else if (DeviceManager.getDevice() == DeviceManager.DEVICE_PAD){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                InfoActivity.this.getWindow().setStatusBarColor(InfoActivity.this.getColor(R.color.colorPrimary));
            }
            setContentView(R.layout.activity_info);
        }else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                InfoActivity.this.getWindow().setStatusBarColor(InfoActivity.this.getColor(R.color.colorPrimary));
            }
            setContentView(R.layout.activity_info_none_phone);
        }

        Intent intent = getIntent();
        direct = intent.getBooleanExtra("direct", false);
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        intentFilter = new IntentFilter();
        intentFilter.addAction("com.kai.video.LOCAL_BROADCAST1");
        localReceiver = new LocalReceiver();
        localBroadcastManager.registerReceiver(localReceiver, intentFilter);
        name = intent.getStringExtra("name");
        url = intent.getStringExtra("url").replace("http://121.5.20.185/video/../player.jsp?url=","").replace("http://121.5.20.185/player.jsp?url=", "");
        spUtils = SPUtils.get(InfoActivity.this);
        SearchView searchView = (SearchView)findViewById(R.id.search_tool);
        searchView.setOnFocusChangeListener(this);
        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus && DeviceManager.isTv()) {
                    searchView.clearFocus();
                    searchView.onActionViewCollapsed();
                }
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {  // ???????????????????????????????????? ??????

                Intent intent = new Intent(InfoActivity.this, SearchActivity.class);
                intent.putExtra("wd",s);
                InfoActivity.this.startActivity(intent);
                finish();
                return true;
            }
            // ???????????????????????????
            @Override
            public boolean onQueryTextChange(String s) {
                // ???????????????????????? recyclerView ?????????????????? // ????????????????????? ???listView

                return false;
            }
        });
        header = (TextView)findViewById(R.id.title);
        MyPlayerManager.loadScreen(this);
        player = (TvPlayer) findViewById(R.id.player);
        player.setDismissControlTime(4000);
        player.initSputils(this);
        player.setIsTouchWiget(true);
        //??????????????????
        player.setRotateViewAuto(false);
        player.setAutoFullWithSize(true);
        player.setLockLand(false);
        player.setShowFullAnimation(false);
        player.setNeedLockFull(true);
        player.getFullscreenButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //????????????
                orientationUtils = new OrientationUtils(InfoActivity.this, player);
                //?????????????????????????????????
                orientationUtils.setEnable(false);
                orientationUtils.resolveByClick();
                //?????????true??????????????????actionbar????????????true??????????????????statusbar
                player.startWindowFullscreen(InfoActivity.this, true, true);
            }
        });
        player.setVideoAllCallBack(new GSYSampleCallBack() {
            @Override
            public void onPrepared(String url, Object... objects) {
                super.onPrepared(url, objects);
                fullHandler.postDelayed(fullRunnable, 1000);
                LogUtil.d("tag", player.getCurrentPlayer().getCurrentVideoHeight() + "x" + player.getCurrentPlayer().getCurrentVideoWidth());
                //????????????????????????????????????
                player.hideFailure();
                player.getcurrentPlayer().hideFailure();
                getDanmu();
                player.getcurrentPlayer().setQuality(Quality.get(player.getCurrentPlayer().getCurrentVideoWidth()));
            }

            @Override
            public void onPlayError(String url, Object... objects) {
                super.onPlayError(url, objects);
                changeOtherApi();
            }

            @Override
            public void onAutoComplete(String url, Object... objects) {
                super.onAutoComplete(url, objects);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            videoTool.getHistoryManager().updateTime(videoTool.getInfo().getString("url"), 0);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }).start();
                switchNext();
            }

            @Override
            public void onClickStartError(String url, Object... objects) {
                super.onClickStartError(url, objects);

            }

            @Override
            public void onEnterFullscreen(String url, Object... objects) {
                super.onEnterFullscreen(url, objects);
                fullButton.clearFocus();
                full(true);
                if (player.getCurrentState() == GSYVideoView.CURRENT_STATE_PAUSE && DeviceManager.isTv())
                    player.getCurrentPlayer().getStartButton().callOnClick();
            }

            @Override
            public void onQuitFullscreen(String url, Object... objects) {
                super.onQuitFullscreen(url, objects);
                orientationUtils.releaseListener();
                fullButton.requestFocus();
                full(false);
                if (player.isRelease())
                    player.showLoading();

                scrollItemToTop(adapter.getCurrent());
            }

        });

        description = (TextView) findViewById(R.id.description);
        peroid = (TextView) findViewById(R.id.period);
        nextButton = (Button) findViewById(R.id.next);
        nextButton.setOnFocusChangeListener(this);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (player.isIfCurrentIsFullscreen()){
                    player.getCurrentPlayer().getStartButton().callOnClick();
                    return;
                }
                switchNext();
            }
        });
        fullButton = (Button) findViewById(R.id.full);
        fullButton.setOnFocusChangeListener(this);
        fullButton.requestFocus();
        changeButton = (Button) findViewById(R.id.change_api);
        changeButton.setOnFocusChangeListener(this);
        initial_api = spUtils.getValue("api", 0);
        changeButton.setText("?????????"+apiNames.get(initial_api).replaceAll("\\s.*", ""));
        changeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (player.isIfCurrentIsFullscreen()){
                    player.getCurrentPlayer().getStartButton().callOnClick();
                    return;
                }
                player.changeApi();
            }
        });
        recyclerView = (RecyclerView) findViewById(R.id.selection);
        recyclerView.setOnFocusChangeListener(this);
        fullButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (player.isIfCurrentIsFullscreen()){
                    player.getCurrentPlayer().getStartButton().callOnClick();
                    return;
                }
                orientationUtils = new OrientationUtils(InfoActivity.this, player);
                //?????????????????????????????????
                orientationUtils.setEnable(false);
                orientationUtils.resolveByClick();
                //?????????true??????????????????actionbar????????????true??????????????????statusbar
                player.startWindowFullscreen(InfoActivity.this, false, false);

            }
        });
        player.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                Toast.makeText(InfoActivity.this, keyCode + "", Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        initVideoTool();
    }

    public void updateTime(long time){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    videoTool.getHistoryManager().updateTime(videoTool.getInfo().getString("url"), time);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public String getUrl(){
        try {
            return videoTool.getInfo().getString("url");
        }catch (Exception e){
            return "";
        }

    }


    private void full(boolean enable) {
        if (DeviceManager.isTv()) {
            //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            return;
        }
        if (enable) {
            WindowManager.LayoutParams lp =  getWindow().getAttributes();
            lp.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
            getWindow().setAttributes(lp);
            //getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        } else {
            WindowManager.LayoutParams attr = getWindow().getAttributes();
            attr.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().setAttributes(attr);
            //getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
    }
    private void getDanmu() {
        //??????demo????????????
        videoTool.getDanmuTool().setOnLoadingListener(new DanmuTool.OnLoadingListener() {
            @Override
            public void onSuccess(JSONArray danmuList, String url) {
                try {
                    if (url.equals(videoTool.getInfo().getString("url"))){
                        Toast.makeText(InfoActivity.this, "??????????????????", Toast.LENGTH_SHORT).show();
                        player.getcurrentPlayer().loadDanmu(danmuList);
                    }


                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onFail() {

            }
        });
        try {
            videoTool.getDanmuTool().getDanmu(videoTool.getInfo().getString("url"));
        }catch (Exception e){
            e.printStackTrace();
        }

    }
    private void initVideoTool(){
        videoTool = VideoTool.getInstance(this);
        videoTool.setOnGetHistory(new VideoTool.OnGetHistory() {
            @Override
            public void onGetSuccess(JSONObject history) {
                try {
                    String c =history.getString("current");
                    if (c.equals("???-1???"))
                        return;
                    else if (c.equals("???-2???")){
                        return;
                    }
                    if (direct) {
                        switchVideo(url);
                        return;
                    }
                    switchVideo(history.getString("url"));
                    Snackbar.make(player.getcurrentPlayer().getSurface(), "???????????????" + videoTool.getInfo().getString("name") +"    " +c, Snackbar.LENGTH_LONG)
                            .setAction("Action", null)
                            .setBackgroundTint(getResources().getColor(R.color.colorPrimary)).show();
                }catch (Exception e){
                    e.printStackTrace();
                }

            }

            @Override
            public void onGetFail() {
                try {
                    LogUtil.d("tag", "history none");
                    JSONArray array = videoTool.getInfo().getJSONArray("selections");
                    if (array.length()>0) {
                        switchVideo(videoTool.getInfo().getJSONArray("selections").getJSONObject(0), 0);
                        scrollItemToTop(0);
                        adapter.change(0);
                    }
                    else
                        getVideoFromResult();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
        videoTool.setOnGetInfo(new VideoTool.OnGetInfo() {
            @Override
            public void onGetStart() {
                fullHandler.removeCallbacks(fullRunnable);
                player.hideFailure();
                player.getcurrentPlayer().hideFailure();
                player.showLoading();
                player.getcurrentPlayer().showLoading();
                fullButton.setClickable(false);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (videoTool.getInfo().has("current"))
                                videoTool.getHistoryManager().updateCurrent(videoTool.getInfo().getString("current"), videoTool.getInfo().getString("url"));
                        }catch (Exception e){
                            e.printStackTrace();
                        }

                    }
                }).start();
            }

            @Override
            public void onGetFail() {

            }

            @Override
            public void onGetSuccess(JSONObject info) {
                try {
                    header.setText(info.has("name")?info.getString("name"):"");
                    player.setCoverUrl(info.has("url")?info.getString("url"):"");
                    description.setText(info.has("description")?info.getString("description"):"");
                    peroid.setText(!info.has("period")?"":info.getString("period"));
                    layoutManager = new StaggeredGridLayoutManager(isScreenOriatationPortrait(InfoActivity.this)?5:10, StaggeredGridLayoutManager.VERTICAL);
                    recyclerView.setLayoutManager(layoutManager);
                    JSONArray array = info.getJSONArray("selections");
                    adapter = new SelectionItemAdapter(array);
                    adapter.setOnListener(new SelectionItemAdapter.onListener() {
                        @Override
                        public void onEnsure(int currentPosition) {
                            scrollItemToTop(currentPosition);
                        }

                        @Override
                        public void onClick(int position) {
                            try {
                                updateTime(player.getCurrentPositionWhenPlaying());
                                JSONObject obj = array.getJSONObject(position);
                                switchVideo(obj, position);
                                fullButton.requestFocus();
                            }catch (Exception e){
                                e.printStackTrace();
                            }

                        }
                    });
                    recyclerView.setAdapter(adapter);
                    videoTool.getHistory(InfoActivity.this);
                    //?????????
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        });
        videoTool.setOnGetVideo(new VideoTool.OnGetVideo() {
            @Override
            public void onGetStart() {
                fullHandler.removeCallbacks(fullRunnable);
                player.setQuality("");
                player.getcurrentPlayer().setQuality("");
                player.setLocalCache(false);
                player.hideFailure();
                player.getcurrentPlayer().hideFailure();
                player.showLoading();
                player.getcurrentPlayer().showLoading();
                fullButton.setClickable(false);
                player.getGSYVideoManager().pause();
                player.getGSYVideoManager().releaseMediaPlayer();
                player.setRelease(true);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            videoTool.getHistoryManager().updateCurrent(videoTool.getInfo().getString("current"), videoTool.getInfo().getString("url"));
                        }catch (Exception e){
                            e.printStackTrace();
                        }

                    }
                }).start();
            }

            @Override
            public void onGetFail() {
                if (!player.isRelease())
                    return;
                player.showFailure();
                player.getcurrentPlayer().showFailure();
                changeOtherApi();
            }

            @Override
            public void onGetSuccess(String url, long currentTime, String tname, boolean localCache) {
                url = toUtf8String(url);

                if (!player.isRelease())
                    return;
                player.setRelease(false);
                if (localCache)
                    Snackbar.make(player.getcurrentPlayer().getSurface(), "???????????????", Snackbar.LENGTH_LONG).setAction("Action", null).setBackgroundTint(getResources().getColor(R.color.colorPrimary)).show();
                fullButton.setClickable(true);
                player.setLocalCache(localCache);
                boolean cache;
                Log.d("tag", url);
                if (url.contains("subaibai"))
                    url = IPTool.getLocal() + "/m3u8?url=" + URLEncoder.encode(url);
                if (url.contains("sf1-ttcdn"))
                    url = url.replaceAll("\\?.*", "");
                if (url.contains("hls") || url.contains("m3u8") || url.contains("bigmao") || url.contains("csssss"))
                    cache = false;
                else
                    cache = true;
                player.getCurrentPlayer().setUp(url, cache, tname);
                player.getCurrentPlayer().setSeekOnStart(currentTime);

                if (forground){
                    player.getCurrentPlayer().startPlayLogic();
                }
                //???????????????????????????????????????????????????????????????
                new Handler().postDelayed(timeOUtRunnable, 20 * 1000);

            }
        });
        url = url.replaceAll("\\?.*", "");
        videoTool.getInfo(url);
    }
    Runnable timeOUtRunnable = new Runnable() {
        @Override
        public void run() {
            int currentState = player.getCurrentState();
            if (currentState == player.CURRENT_STATE_PLAYING || currentState == GSYVideoView.CURRENT_STATE_NORMAL || currentState == GSYVideoView.CURRENT_STATE_PLAYING_BUFFERING_START || currentState == GSYVideoView.CURRENT_STATE_PAUSE)
                return;
            player.getcurrentPlayer().release();
            changeOtherApi();

        }
    };
    /**
     * ??????url????????????
     * @param s
     * @return
     */
    public static String toUtf8String(String s) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c >= 0 && c <= 255) {
                sb.append(c);
            } else {
                byte[] b;
                try {
                    b = String.valueOf(c).getBytes("utf-8");
                } catch (Exception ex) {
                    System.out.println(ex);
                    b = new byte[0];
                }
                for (int j = 0; j < b.length; j++) {
                    int k = b[j];
                    if (k < 0)
                        k += 256;
                    sb.append("%" + Integer.toHexString(k).toUpperCase());
                }
            }
        }
        return sb.toString();
    }
    public void deleteLog(){
        try {
            videoTool.getHistoryManager().deleteLog(videoTool.getWebsite(), videoTool.getInfo().getString("name"));
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        fullHandler.removeCallbacks(fullRunnable);
        //????????????????????????????????????????????????
        if (player != null && player.isIfCurrentIsFullscreen()){
            if (keyCode == KeyEvent.KEYCODE_ENTER){
                player.getCurrentPlayer().getStartButton().callOnClick();
            }
            else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT){
                player.getcurrentPlayer().setSeekAdd(1);
            }
            else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT){
                player.getcurrentPlayer().setSeekAdd(-1);
            }
            else if (keyCode == KeyEvent.KEYCODE_BACK){
                player.hideAllDialog();
                player.getCurrentPlayer().getFullscreenButton().callOnClick();
            }
            else if (keyCode == KeyEvent.KEYCODE_DPAD_UP){
                player.getcurrentPlayer().showDamakuSetting();
            }
            else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN){
                player.getcurrentPlayer().changeApi();
            }
            else if (keyCode == KeyEvent.KEYCODE_MENU || keyCode == KeyEvent.KEYCODE_SETTINGS){
                player.getcurrentPlayer().showSetting();
            }
            return true;
        }
        //cancelFullscreenTimer();
        if (keyCode == KeyEvent.KEYCODE_BACK && recyclerView.hasFocus()){
            fullButton.requestFocus();
            return false;
        }
        if (keyCode == KeyEvent.KEYCODE_MENU){
            player.getcurrentPlayer().showSetting();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }
    public static boolean isScreenOriatationPortrait(Context context) {
        return context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
    }

    void scrollItemToTop(int position){
        LinearSmoothScroller smoothScroller = new LinearTopSmoothScroller(this);
        smoothScroller.setTargetPosition(position);
        layoutManager.startSmoothScroll(smoothScroller);
    }

    public void getVideoFromResult(){
        try {
            final String url = videoTool.getInfo().getString("url");
            player.setCoverUrl(url);
            videoTool.getVideo(Api.getApis().getString(apiNames.get(spUtils.getValue("api", 0))));
        }catch (Exception e){
            e.printStackTrace();
        }


    }
    public void switchVideo(JSONObject object, int i){
        try {
            JSONObject newInfo = videoTool.getInfo();
            newInfo.put("url", object.getString("url"));
            newInfo.put("title", object.getString("videoTitle"));
            String c = object.getString("title");
            newInfo.put("current", c);
            //??????????????????????????????
            if (newInfo.getBoolean("zongyi")){
                newInfo.put("current", (i+1) + "");
                newInfo.put("pname", c);
            }
            try {
                Integer.parseInt(c);
            }catch (Exception e){
                newInfo.put("current", "-2");
                newInfo.put("current_text", c.replaceAll("\\s", ""));
            }

            videoTool.setInfo(newInfo);
            getVideoFromResult();
        }catch (Exception e){
            e.printStackTrace();
        }


    }
    public void switchVideo(String newUrl){
        try {
            JSONObject newInfo = videoTool.getInfo();
            JSONArray array1 = videoTool.getInfo().getJSONArray("selections");
            for(int i = 0; i < array1.length(); i++){
                JSONObject object = array1.getJSONObject(i);
                if (object.getString("url").equals(newUrl)){
                    scrollItemToTop(i);
                    adapter.change(i);
                    newInfo.put("title", object.getString("videoTitle"));
                    newInfo.put("url", object.getString("url"));
                    String c = object.getString("title");
                    newInfo.put("current", c);
                    //??????????????????????????????
                    if (newInfo.getBoolean("zongyi")){
                        newInfo.put("current", (i+1) + "");
                        newInfo.put("pname", c);
                    }
                        try {
                            Integer.parseInt(c);

                        }catch (Exception e){
                            newInfo.put("current", "-2");
                            newInfo.put("current_text", c.replaceAll("\\s", ""));
                        }

                    videoTool.setInfo(newInfo);
                    getVideoFromResult();
                    break;

                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }

    }
    public void switchNext(){
        try {
            int c = adapter.getCurrent();
            if (c == adapter.getItemCount()-1){
                Snackbar.make(player.getcurrentPlayer().getSurface(), "??????????????????", Snackbar.LENGTH_LONG).setAction("Action", null).setBackgroundTint(getResources().getColor(R.color.colorPrimary)).show();
                return;
            }
            int index = c;
            JSONObject obj = new JSONObject();
            do {
                obj = adapter.getArray().getJSONObject(++index);
            }
            while (obj.getInt("type") == 1);
            adapter.change(index);
            switchVideo(obj, index);
            Snackbar.make(player.getcurrentPlayer().getSurface(), "????????????????????????????????????", Snackbar.LENGTH_LONG).setAction("Action", null).setBackgroundTint(getResources().getColor(R.color.colorPrimary)).show();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        videoTool.getHistoryManager().updateTime(videoTool.getInfo().getString("url"), 0);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }).start();


        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    protected void onPause() {
        forground = false;
        player.getGSYVideoManager().pause();
        player.getCurrentPlayer().onVideoPause();
        super.onPause();
    }
    @Override
    public void onBackPressed() {
        //????????????
        if (player.isIfCurrentIsFullscreen()){
            //Toast.makeText(InfoActivity.this, "shsh", Toast.LENGTH_SHORT).show();
            player.getFullWindowPlayer().getBackButton().callOnClick();
            return;
        }
        player.setRelease(true);
        player.getGSYVideoManager().pause();
        player.getGSYVideoManager().releaseMediaPlayer();
        player.setVideoAllCallBack(null);
        GSYVideoManager.releaseAllVideos();
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        forground = false;
        if (player != null) {
            player.setRelease(true);
            player.getCurrentPlayer().getGSYVideoManager().pause();
            GSYVideoManager.releaseAllVideos();
            player.getCurrentPlayer().getGSYVideoManager().releaseMediaPlayer();
        }
        if (videoTool != null)
            videoTool.destory();
        player.destory();
        if (FloatWindow.get() != null)
            FloatWindow.destroy();
        localBroadcastManager.unregisterReceiver(localReceiver);
        super.onDestroy();
    }
    @Override
    protected void onResume() {
        forground = true;
        //localBroadcastManager.registerReceiver(localReceiver, intentFilter);
        //???????????????????????????????????????????????????????????????
        if (FloatWindow.get() != null){
            FloatWindow.destroy();
            //player.getcurrentPlayer().resume(player.getCurrentPositionWhenPlaying());
        }

        if (FloatingSimul.getCurrentTime() > 0){
            player.getcurrentPlayer().resume(player.getCurrentPositionWhenPlaying());
            FloatingSimul.setCurrentTime(0);
        }else if (videoTaskItem != null){
            Snackbar.make(player.getcurrentPlayer().getSurface(), "??????????????????", Snackbar.LENGTH_LONG).setAction("Action", null).setBackgroundTint(getResources().getColor(R.color.colorPrimary)).show();
            player.getcurrentPlayer().resume(player.getCurrentPositionWhenPlaying(), videoTaskItem);
            videoTaskItem = null;
        }
        //player.getcurrentPlayer().resume(player.getCurrentPositionWhenPlaying());
        super.onResume();
    }
    private VideoTaskItem videoTaskItem = null;
    public class LocalReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                DeliverVideoTaskItem deliverVideoTaskItem = (DeliverVideoTaskItem) intent.getSerializableExtra("item");
                VideoTaskItem item = DeliverVideoTaskItem.unpack(deliverVideoTaskItem);
                //????????????????????????????????????????????????
                if ((item.getTaskState() == 4 || item.getTaskState() == 5) && item.getTitle().equals(videoTool.getInfo().getString("title") + "|" + videoTool.getInfo().getString("url"))){
                    player.getcurrentPlayer().setLocalCache(true);
                    if (forground){
                        player.getcurrentPlayer().resume(player.getCurrentPositionWhenPlaying(), item);
                    }else
                        videoTaskItem = item;
                }
            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }



}
