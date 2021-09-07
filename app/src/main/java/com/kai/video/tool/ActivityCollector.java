package com.kai.video.tool;

import android.app.Activity;
import android.util.Log;

import com.kai.video.MainActivity;

import java.util.ArrayList;
import java.util.List;

public class ActivityCollector {
    public static List<Activity> activities = new ArrayList<>();
    public static void addActivity(Activity activity){
        activities.add(activity);
    }
    public static void removeActivity(Activity activity){
        activities.remove(activity);
    }
    public static void finishAll(){
        for (Activity activity: activities){
            try {
                activity.finish();
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        System.exit(0);
    }
    public static void clearTop(Activity activity){
        for (int i = 0; i < activities.size(); i++) {
            Activity a = activities.get(i);
            if (a.getLocalClassName().equals(activity.getLocalClassName())){
                activities.set(i, activity);
                activities = activities.subList(0, i+1);
                break;
            }
        }
    }
}
