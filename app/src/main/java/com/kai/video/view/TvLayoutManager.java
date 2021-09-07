package com.kai.video.view;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;

public class TvLayoutManager extends GridLayoutManager {
    public TvLayoutManager(Context context, int spanCount, int orientation, boolean reverseLayout) {
        super(context, spanCount, orientation, reverseLayout);
    }

    @Nullable
    @Override
    public View onInterceptFocusSearch(@NonNull View focused, int direction) {
        int currentPosition= getPosition(getFocusedChild());//这里要用这个方法
        int count = getItemCount();
        return super.onInterceptFocusSearch(focused, direction);

    }
}
