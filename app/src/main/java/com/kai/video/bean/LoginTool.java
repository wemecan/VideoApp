package com.kai.video.bean;

import android.app.Activity;

import com.kai.video.R;
import com.kai.video.tool.IPTool;

import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

public class LoginTool {

    public static void login(Activity activity, String username, String password, OnLogin onLogin){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Connection.Response response = Jsoup.connect(IPTool.getLocal() + "/login")
                            .data("action", "login")
                            .data("mobile", username)
                            .data("password", password)
                            .data("tv", "true")
                            .method(Connection.Method.GET)
                            .ignoreContentType(true)
                            .execute();
                    if (response.body().equals("right"))
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                onLogin.success();
                            }
                        });

                    else
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                onLogin.fail();
                            }
                        });

                }catch (Exception e){
                    e.printStackTrace();
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            onLogin.fail();
                        }
                    });
                }

            }
        }).start();
    }

    public interface OnLogin{
        void success();
        void fail();
    }
}
