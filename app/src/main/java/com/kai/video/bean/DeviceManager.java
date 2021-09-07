package com.kai.video.bean;

import android.app.Activity;
import android.app.UiModeManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Configuration;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Window;
import android.view.WindowManager;

import com.kai.video.MainActivity;
import com.kai.video.R;

public class DeviceManager {
    public static int DEVICE_TV = 0;
    public static int DEVICE_PAD = 1;
    public static int DEVICE_PHONE = 2;
    private static int DEVICE = 0;

    public static void setDevice(int device) {
        DeviceManager.DEVICE = device;
    }

    public static boolean tv = false;

    public static boolean isTv() {
        return tv;
    }
    public static boolean isPhone(){
        return DEVICE == DEVICE_PHONE;
    }

    public static int getDevice() {
        return DEVICE;
    }

    public static void init(Context context){
        UiModeManager uiModeManager = (UiModeManager) context.getSystemService(ContextWrapper.UI_MODE_SERVICE);
        if (uiModeManager.getCurrentModeType() == Configuration.UI_MODE_TYPE_TELEVISION) {
            DeviceManager.tv = true;
            DeviceManager.setDevice(DEVICE_TV);
        }
        else {
            DeviceManager.tv = false;
            TelephonyManager telephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            int type = telephony.getPhoneType();
            if (type == TelephonyManager.PHONE_TYPE_NONE) {
                DeviceManager.setDevice(DEVICE_PAD);
                Log.d("TAG", "is Tablet!");
            } else {
                DeviceManager.setDevice(DEVICE_PHONE);
                Log.d("TAG", "is phone!");

            }
        }
    }
}
