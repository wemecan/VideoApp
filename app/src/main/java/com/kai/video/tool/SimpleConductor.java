package com.kai.video.tool;

import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import com.kai.video.Commend;
import com.kai.video.bean.DeviceManager;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;


public class SimpleConductor {
    private String action = "tv";
    private List<Element> list = new ArrayList<>();
    private List<FutureTask<Integer>> tagTasks = new ArrayList<>();
    private List<String> types = new ArrayList<>();

    public List<String> getTypes() {
        return types;
    }

    public SimpleConductor(String action){
        this.action = action;
        if (action.equals("tv")){
            types.add("tencent");
            types.add("iqiyi");
            types.add("mgtv");
            types.add("bilibili");
        }else if (action.equals("film")){
            types.add("mgtv");
            types.add("bilibili");
            types.add("tencent");
            types.add("iqiyi");

        }else if (action.equals("cartoon")){
            types.add("tencent");
            types.add("bilibili");
            types.add("bilibili1");
            types.add("iqiyi");


        }else if (action.equals("zy")){
            types.add("tencent");
            types.add("mgtv");
            types.add("bilibili");
            types.add("iqiyi");

        }
    }
    public SimpleConductor(){

    }
    public void search(final String key,final OnSearchListener onSearchListener){
         new Thread(new Runnable() {
             @Override
             public void run() {
                 try {
                     Connection.Response response = Jsoup.connect(IPTool.getLocal() + "/SearchServlet")
                             .data("wd", key)
                             .method(Connection.Method.GET)
                             .execute();
                     onSearchListener.onSearch(new JSONObject(response.body()).getJSONArray("data"));
                 }catch (Exception e){
                     e.printStackTrace();
                 }


             }
         }).start();
    }

    private void run(final String action, final String type){

        FutureTask<Integer> task = new FutureTask<Integer>(new Runnable() {
            @Override
            public void run() {
                try {

                    Document document = Jsoup.connect(IPTool.getLocal() + "/hotTool")
                            .data("action", action)
                            .data("type", type)
                            .get();
                    List<Element> elements = document.getElementsByTag("li");
                    if (elements.size() == 0)
                        return;
                    List<Element> additions = new ArrayList<>();
                    Commend commend = new Commend(type, action);
                    additions.add(commend.getDocument());
                    additions.addAll(elements);
                    int ems = DeviceManager.isTv()?7:4;
                    int rem = ems - elements.size()%ems;
                    for(int i = 0; i < rem; i++){
                        additions.add(null);
                    }
                    while (!type.equals("tencent") && types.size() == 0)
                        Thread.sleep(500);
                    list.addAll(additions);
                    if (action.equals("japaneseList"))
                        types.add("bilibili1");
                    else
                        types.add(type);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        },1);
        tagTasks.add(task);
        new Thread(task).start();
    }

    public void get(final OnGetListener onGetListener){
        list.clear();
        tagTasks.clear();
        LogUtil.d("TAG", "从服务器获取视频汇总");
        for(String type: types){
            if (action.equals("cartoon") && type.equals("bilibili")){
                run("ChineseList", "bilibili");
                continue;
            }
            if (action.equals("cartoon") && type.equals("bilibili1")){
                run("japaneseList", "bilibili");
                continue;
            }
            run(action, type);
        }
        types.clear();
        for (FutureTask<Integer> task:tagTasks) {
            try {
                task.get();
            }catch (Exception e){
                e.printStackTrace();
            }

        }
        onGetListener.onFinish(list);


    }
    public interface OnGetListener{
        void onFinish(List<Element> list);
    }
    public interface OnSearchListener{
        void onSearch(JSONArray list);
    }
}
