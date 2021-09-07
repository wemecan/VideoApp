package com.kai.video.tool;

import android.content.Context;

import androidx.recyclerview.widget.LinearSmoothScroller;

public class LinearTopSmoothScroller extends LinearSmoothScroller {

    public LinearTopSmoothScroller(Context context) {
        super(context);
    }

    @Override
    protected int getVerticalSnapPreference() {
        return SNAP_TO_START;
    }
}