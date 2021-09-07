package com.kai.video.bean;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.JsonArray;
import com.kai.video.tool.IPTool;
import com.kai.video.tool.LogUtil;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import master.flame.danmaku.controller.IDanmakuView;

public class DanmuTool {
    private String url = "";
    private JSONArray danmuList;
    private OnLoadingListener onLoadingListener;
    private DanmuTask danmuTask = null;

    public void setOnLoadingListener(OnLoadingListener onLoadingListener) {
        this.onLoadingListener = onLoadingListener;
    }
    public void destory(){
        if (danmuTask != null)
            danmuTask.cancel(true);

    }

    public String getUrl() {
        return url;
    }

    public DanmuTool(){
    }

    public JSONArray getDanmuList() {
        return danmuList;
    }
    public class DanmuTask extends AsyncTask{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            danmuList = new JSONArray();
        }

        @Override
        protected void onProgressUpdate(Object[] values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            try {
                Connection.Response response = Jsoup.connect(IPTool.getLocal() + "/danmu")
                        .data("url", url)
                        .method(Connection.Method.GET)
                        .ignoreContentType(true)
                        .execute();
                JSONArray array = new JSONObject(response.body()).getJSONArray("data");
                danmuList = array;
                return  array;

            }catch (Exception e){
                return null;
            }
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            if ( isCancelled())
                return;
            if (o == null)
                onLoadingListener.onFail();
            else
                onLoadingListener.onSuccess((JSONArray) danmuList, url);
        }
    }
    public void cancel(){
        if (danmuTask != null){
            danmuTask.cancel(true);
        }
    }
    public void getDanmu(String url){
        if (url.equals(this.url) && danmuList != null && danmuList.length() > 0){
            LogUtil.d("TAG", "视频地址不变，直接获取缓存");
            onLoadingListener.onSuccess(danmuList, url);
        }else {
            this.url = url.replaceAll("\\?.*", "");
            danmuList = new JSONArray();
            if (danmuTask == null || danmuTask.isCancelled()){
                danmuTask = new DanmuTask();
            }else {
                danmuTask.cancel(true);
                danmuTask = new DanmuTask();
            }
            danmuTask.execute();
        }

    }
    public interface OnLoadingListener{
        void onSuccess(JSONArray danmuList, String url);
        void onFail();
    }
}
