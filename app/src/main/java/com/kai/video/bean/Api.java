package com.kai.video.bean;

import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Api {
    static JSONObject apis = null;
    public static String referer = "https://jhpc.manduhu.com/?url=";
    public static void loadApis(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Connection.Response response = Jsoup.connect("http://121.5.20.185/video/src/tv.json")
                            .ignoreContentType(true)
                            .ignoreHttpErrors(true)
                            .method(Connection.Method.GET)
                            .execute();
                    apis = new JSONObject(response.body());
                    getReferer();
                }catch (Exception e){
                    e.printStackTrace();
                    apis = null;
                }


            }
        }).start();

    }

    public static JSONObject getApis(){
        if (apis != null)
            return apis;
        List<FutureTask<Integer>> futureTasks = new ArrayList<>();
        futureTasks.add(new FutureTask<Integer>(new Runnable() {
            @Override
            public void run() {
                try {
                    Connection.Response response = Jsoup.connect("http://121.5.20.185/video/src/tv.json")
                            .method(Connection.Method.GET)
                            .ignoreHttpErrors(true)
                            .ignoreContentType(true)
                            .execute();
                    apis = new JSONObject(response.body());
                }catch (Exception e){
                    e.printStackTrace();
                    apis = null;
                }


            }
        }, 1));
        for (Future<Integer> future : futureTasks){
            try {
                future.get();
            }catch (Exception e){

            }
        }
        if (apis != null)
            return apis;
        else
            return new JSONObject();
    }
    public static void getReferer(){
        new Thread(new Runnable() {
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
                        referer = object.getJSONObject("fuckapp").getString("parse");
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        }).start();
    }
}
