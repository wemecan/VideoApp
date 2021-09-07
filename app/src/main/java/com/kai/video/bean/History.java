package com.kai.video.bean;
import android.content.Context;
import com.kai.video.tool.IPTool;
import com.kai.video.tool.SPUtils;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

public class History {
    private String username = "";
    private String videoType;
    private String name;
    public History(Context context, String name, String videoType){
        this.name = name;
        this.videoType = videoType;
        username = SPUtils.get(context).getValue("username", "");
    }
    public void updateCurrent(String current, String url){
        if (username.isEmpty())
            return;
        try {
            Connection.Response response = Jsoup.connect(IPTool.getLocal() + "/history")
                    .data("action", "updateCurrent")
                    .data("name", name)
                    .data("username", username)
                    .data("videoType", videoType)
                    .data("current", current)
                    .data("url", url)
                    .timeout(8 * 1000)
                    .method(Connection.Method.GET)
                    .ignoreContentType(true)
                    .execute();
            response.body();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public JSONObject getCurrent(String current, String url){
        if (username.isEmpty())
            return new JSONObject();
        try {
            Connection.Response response = Jsoup.connect(IPTool.getLocal() + "/history")
                    .data("action", "current")
                    .data("name", name)
                    .data("username", username)
                    .data("videoType", videoType)
                    .data("current", current)
                    .data("url", url)
                    .method(Connection.Method.GET)
                    .ignoreContentType(true)
                    .execute();
            return new JSONObject(response.body());
        }catch (Exception e){
            e.printStackTrace();
            return new JSONObject();
        }
    }
    public long getTime(String url){
        if (username.isEmpty())
            return 0;
        try {
            Connection.Response response = Jsoup.connect(IPTool.getLocal() + "/history")
                    .data("action", "time")
                    .data("username", username)
                    .data("url", url)
                    .method(Connection.Method.GET)
                    .ignoreContentType(true)
                    .execute();
            JSONObject object = new JSONObject(response.body());
            if (object.getBoolean("success")){
                return object.getLong("time");
            }else {
                createTime(url);
            }

        }catch (Exception e){
            e.printStackTrace();

        }
        return 0;
    }
    private void createTime(String url){
        if (username.isEmpty())
            return;
        try {
            Jsoup.connect(IPTool.getLocal() + "/history")
                    .data("action", "createTime")
                    .data("username", username)
                    .data("url", url)
                    .timeout(5 * 1000)
                    .method(Connection.Method.GET)
                    .ignoreContentType(true)
                    .execute();

        }catch (Exception e){
            e.printStackTrace();

        }
    }
    public void deleteLog(String website, String name){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Jsoup.connect(IPTool.getLocal() + "/history")
                            .data("action", "deleteLog")
                            .data("website", website)
                            .data("username", username)
                            .data("name", name)
                            .timeout(3 * 1000)
                            .method(Connection.Method.GET)
                            .ignoreContentType(true)
                            .execute();

                }catch (Exception e){
                    e.printStackTrace();

                }
            }
        }).start();
    }
    public void updateTime(String url, long time){
        if (username.isEmpty())
            return;
        try {
            Connection.Response response= Jsoup.connect(IPTool.getLocal() + "/history")
                    .data("action", "updateTime")
                    .data("username", username)
                    .data("url", url)
                    .data("time", String.valueOf(time))
                    .timeout(10 * 1000)
                    .method(Connection.Method.GET)
                    .ignoreContentType(true)
                    .execute();
            response.body();

        }catch (Exception e){
            e.printStackTrace();

        }
    }
    public static JSONObject getAll(Context context){
        try {
            Connection.Response response = Jsoup.connect(IPTool.getLocal() + "/history")
                    .data("action", "get")
                    .data("username", SPUtils.get(context).getValue("username", ""))
                    .method(Connection.Method.GET)
                    .ignoreContentType(true)
                    .execute();
            return  new JSONObject(response.body());
        }catch (Exception e){
            e.printStackTrace();
            return new JSONObject();
        }
    }
}
