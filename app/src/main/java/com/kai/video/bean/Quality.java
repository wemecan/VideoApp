package com.kai.video.bean;

import android.view.View;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.kai.video.R;

public class Quality {
    public static String get(int width){
        if (width > 1920){
            return "4K 蓝光HDR";
        }else if (width > 1280){
            return "1080P 原画";
        }else if (width > 900){
            return "720P 超清";
        }else if (width > 640){
            return "640P 高清";
        }else if (width > 480){
            return "480P 标清";
        }else if (width > 320){
            return "320P 流畅";
        }else if (width >0){
            return "270P 极速";
        }else {
            return "";
        }
    }
}
