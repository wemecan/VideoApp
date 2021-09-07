package com.kai.video.bean;

public class FloatingSimul {
    static long currentTime = 0;
    public static void setCurrentTime(long currentTime){
        FloatingSimul.currentTime = currentTime;
    }

    public static long getCurrentTime() {
        return currentTime;
    }
}
