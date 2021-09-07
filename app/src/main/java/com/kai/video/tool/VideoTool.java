package com.kai.video.tool;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.ArrayMap;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.google.gson.JsonObject;
import com.jeffmony.downloader.VideoDownloadConfig;
import com.jeffmony.downloader.VideoDownloadManager;
import com.jeffmony.downloader.database.VideoDownloadDatabaseHelper;
import com.jeffmony.downloader.listener.IDownloadInfosCallback;
import com.jeffmony.downloader.model.Video;
import com.jeffmony.downloader.model.VideoTaskItem;
import com.jeffmony.downloader.utils.ContextUtils;
import com.kai.video.bean.DanmuTool;
import com.kai.video.bean.SniffingUtil;
import com.kai.video.sniffing.DefaultFilter;
import com.kai.video.sniffing.SniffingCallback;
import com.kai.video.sniffing.SniffingVideo;
import com.google.android.material.snackbar.Snackbar;
import com.kai.video.InfoActivity;
import com.kai.video.R;
import com.kai.video.bean.History;
import com.kai.video.view.TvPlayer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class VideoTool {
    private DanmuTool danmuTool;
    private long id;
    private Activity activity;
    private OnGetInfo onGetInfo;
    private OnGetVideo onGetVideo;
    private OnGetHistory onGetHistory;
    private JSONObject info = new JSONObject();
    private String website = "";
    //注意只能有唯一的信息获取线程和视频获取线程
    private InfoThread infoThread;
    private VideoTask videoTask;
    private InfoTask infoTask;
    private History history;
    private HistoryTask historyTask = null;
    public static VideoTool getInstance(Activity activity){
        return new VideoTool(activity);
    }
    public History getHistoryManager(){
        return history;
    }
    private VideoTool(Activity activity){
        this.activity = activity;
        danmuTool = new DanmuTool();
    }

    public DanmuTool getDanmuTool() {
        return danmuTool;
    }

    public String getWebsite() {
        return website;
    }

    public void setOnGetInfo(OnGetInfo onGetInfo){
        this.onGetInfo = onGetInfo;
    }

    public void setOnGetHistory(OnGetHistory onGetHistory) {
        this.onGetHistory = onGetHistory;
    }

    public void setOnGetVideo(OnGetVideo onGetVideo){
        this.onGetVideo = onGetVideo;
    }


    public JSONObject getInfo() {
        return info;
    }

    public void setInfo(JSONObject info) {
        this.info = info;
    }
    private class HistoryTask extends AsyncTask{
        @Override
        protected Object doInBackground(Object[] objects) {
            try {
                final JSONObject result = history.getCurrent(info.getString("current"), info.getString("url"));
                if (result.getBoolean("success"))
                    return result;
            }catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            if (o == null)
                onGetHistory.onGetFail();
            else
                onGetHistory.onGetSuccess((JSONObject)o);
            super.onPostExecute(o);
        }
    }
    public void getHistory(Context context){
        try {
            if (history == null)
                history = new History(context, info.getString("name"), info.getString("videoType"));
            if (historyTask == null || historyTask.isCancelled()) {
                historyTask = new HistoryTask();
            }
            else {
                historyTask.cancel(true);
                historyTask = new HistoryTask();
            }
            historyTask.execute();
        }catch (JSONException e){
            e.printStackTrace();
            onGetHistory.onGetFail();
        }
        new Thread(new Runnable() {
            @Override
            public void run() {


            }
        }).start();
    }
    public void getInfo(String url){
        if (infoTask == null){
            infoTask = new InfoTask(url, onGetInfo);
        }else{
            infoTask.cancel(true);
            infoTask = new InfoTask(url, onGetInfo);

        }
        infoTask.execute();
    }

    public void getVideo(String api){
        danmuTool.cancel();
        if (videoTask == null){
            videoTask = new VideoTask(info, api);
        }else{
            videoTask.cancel(true);
            videoTask = new VideoTask(info, api);

        }
        videoTask.execute();
    }
    public void destory(){
        //在活动结束时调用，避免内存泄漏
        //消除所有消息反馈及内存占用
        if (infoThread != null){
            infoThread.interrupt();
        }
        activity = null;
        id = 0;
        if (historyTask!=null)
            historyTask.cancel(true);
        if(infoTask != null)
            infoTask.cancel(true);
        if (videoTask != null)
            videoTask.cancel(true);
        danmuTool.destory();
    }
    public class VideoTask extends AsyncTask{
        private JSONObject info;
        private String api;
        private long currentTime = 0;
        private String url = "";
        private String tname = "";
        private Map<String, String> cookies;
        private int remainTime = 2;
        private Handler mainHandler = new Handler();
        //每个任务只分配一个嗅探器
        private SniffingUtil sniffingUtil;
        public VideoTask(JSONObject info, String api){
            super();
            this.info = info;
            this.api = api;
            cookies = new ArrayMap<>();
            try {
                cookies.put("web-url", URLEncoder.encode(info.getString("url")));
                cookies.put("web-current", URLEncoder.encode(info.getString("current")));
                cookies.put("web-current-text", URLEncoder.encode(info.getString("current_text")));
                cookies.put("web-name", URLEncoder.encode(info.getString("name")));
                cookies.put("web-zy", URLEncoder.encode(info.getString("zongyi")));
                cookies.put("web-pname", URLEncoder.encode(info.getString("pname")));
                cookies.put("web-season", URLDecoder.decode(info.getString("season")));
                cookies.put("web-series", URLDecoder.decode(info.getString("series")));
                cookies.put("web-mobile", "12345678901");
                cookies.put("web-tv", "true");
                url = getInfo().getString("url");
                tname = info.getString("title");
            }catch (Exception e){
                e.printStackTrace();
                cancel(true);
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (isCancelled())
                onGetVideo.onGetFail();
            else
                onGetVideo.onGetStart();
        }
        private Bundle checkLocalCache(){
            Bundle bundle = new Bundle();
            bundle.putBoolean("success", false);

            try {
                VideoDownloadDatabaseHelper mVideoDatabaseHelper = new VideoDownloadDatabaseHelper(activity);
                List<VideoTaskItem> taskItems = mVideoDatabaseHelper.getDownloadInfos();
                for (VideoTaskItem item : taskItems) {
                    if (item.getTitle().equals(tname + "|" + url) &&
                            (item.getTaskState() == 5 || item.getTaskState() == 4) &&
                            !item.isHlsType()) {
                        bundle.putBoolean("success", true);
                        bundle.putString("url", item.getFilePath());
                        bundle.putLong("time", currentTime);
                        bundle.putString("tname", tname);
                        bundle.putBoolean("localCache", true);
                        return bundle;
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            return bundle;
        }
        @Override
        protected Object doInBackground(Object[] objects) {
            try {
                currentTime = history.getTime(info.getString("url"));
            }catch (Exception e){
                e.printStackTrace();
            }
            Bundle bundle = checkLocalCache();
            if (bundle.getBoolean("success"))
                return bundle;
            while (!isCancelled() && remainTime > 0 && !activity.isDestroyed()){
                bundle = getVideo();
                if (bundle.getBoolean("success")){
                    return bundle;
                }
                if (remainTime == 1){
                    return bundle;
                }
                remainTime--;
            }
            return bundle;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            if (sniffingUtil != null) {
                sniffingUtil.releaseAll();
            }
        }

        private void sniff(String web){
            runUI(new Runnable() {
                @Override
                public void run() {
                    sniffingUtil =  SniffingUtil.get().activity(activity)
                            .autoRelease(true)
                            .callback(new SniffingCallback() {
                                @Override
                                public void onSniffingSuccess(View webView, String url,final List<SniffingVideo> videos) {
                                    onGetVideo.onGetSuccess(videos.get(0).getUrl(), currentTime, tname, false);
                                    sniffingUtil.releaseAll();
                                }

                                @Override
                                public void onSniffingError(View webView, String url, int errorCode) {
                                    onGetVideo.onGetFail();
                                    sniffingUtil.releaseAll();
                                }
                            }).connTimeOut(30 * 1000).readTimeOut(30 * 1000).setFinishedTimeOut(40*1000)
                            .filter(new DefaultFilter()).url(web);
                    sniffingUtil.start();
                }
            });

        }


        private Bundle getVideo(){
            Bundle bundle = new Bundle();
            try {
                Connection.Response response = Jsoup.connect(IPTool.getLocal() + "/videogoar")
                        .ignoreContentType(true)
                        .data("url", info.getString("url"))
                        .data("api", api)
                        .cookies(cookies)
                        .method(Connection.Method.GET)
                        .timeout(1000 * 20)
                        .execute();
                String body = response.body();
                LogUtil.d("TAG", body);
                JSONObject object = new JSONObject(body);
                JSONObject videoObj = object.getJSONArray("videos").getJSONObject(0);
                website = object.has("website")?object.getString("website"):"";
                final String video = URLDecoder.decode(videoObj.getString("url").replace("./m3u8?url=", ""));
                if ((object.has("sniff") && object.getBoolean("sniff")) || (videoObj.has("sniff") && videoObj.getBoolean("sniff"))){
                    sniff(video);
                    bundle.putBoolean("sniff", true);
                }else{
                    bundle.putBoolean("success", true);
                    bundle.putString("url", video);
                    bundle.putBoolean("localCache", false);
                }
            }catch (Exception e){
                e.printStackTrace();
                bundle.putBoolean("success", false);
            }
            return bundle;

        }
        @Override
        protected void onPostExecute(Object o) {

            Bundle bundle = (Bundle)o;
            if (bundle.containsKey("success")){
                if (bundle.getBoolean("success"))
                    onGetVideo.onGetSuccess(bundle.getString("url", ""), currentTime, tname, bundle.getBoolean("localCache"));
                else
                    onGetVideo.onGetFail();
            }else if (bundle.containsKey("sniff")){
                //do nothing but wait for sniff operation ending
            }
            super.onPostExecute(o);
        }

    }

    public class InfoTask extends AsyncTask{
        private int remainTime = 5;
        private OnGetInfo onGetInfo;
        private String url;
        public InfoTask(String url, OnGetInfo onGetInfo){
            super();
            this.url = url;
            this.onGetInfo = onGetInfo;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            onGetInfo.onGetStart();
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            try {
                while (!isCancelled() && remainTime > 0){

                    JSONObject info = getInfo();
                    if (info == null)
                        continue;
                    else if (info.getInt("current") != -1 && info.getJSONArray("selections").length() == 0){
                        continue;
                    }else{
                        VideoTool.this.info = info;
                        return info;
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }

            return null;
        }
        //递归获取视频信息
        private JSONObject getInfo(){
            LogUtil.d("TAG", "开始第" + (5 - remainTime) +"次获取信息");
            try {
                Connection.Response response = Jsoup.connect(IPTool.getLocal() + "/VideoServlet")
                        .data("url", url)
                        .ignoreContentType(true)
                        .timeout(40*1000)
                        .execute();
                 return new JSONObject(response.body());
            }catch (Exception e){
                e.printStackTrace();
                return null;
            }

        }
        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            if (o == null){
                onGetInfo.onGetFail();
            }else {
                onGetInfo.onGetSuccess((JSONObject)o);
            }
        }
    }

    private void runUI(Runnable runnable){
        if (activity == null||activity.isDestroyed()){
            return;
        }
        activity.runOnUiThread(runnable);
    }
    public class InfoThread extends Thread{
        private String url;
        private OnGetInfo onGetInfo;
        private int retryTime = 5;
        public InfoThread(String url, OnGetInfo onGetInfo){
             LogUtil.d("TAG","　从服务器开始获取视频信息");
            this.url = url;
            this.onGetInfo = onGetInfo;
        }

        @Override
        public void run() {
            getInfo();
        }

        @Override
        public boolean isInterrupted() {
            if (super.isInterrupted())
                LogUtil.d("TAG", "当前线程已废弃");
            return super.isInterrupted();
        }


    }
    public interface OnGetInfo{
        void onGetStart();
        void onGetFail();
        void onGetSuccess(JSONObject result);
    }
    public interface OnGetVideo{
        void onGetStart();
        void onGetFail();
        void onGetSuccess(String url, long Time, String tname, boolean localcache);

    }
    public interface OnGetHistory{
        void onGetSuccess(JSONObject history);
        void onGetFail();
    }
    public interface OnGetDanmu{
        void onGetSuccess(JSONArray danmuList);
        void onGetFail();
    }
}
