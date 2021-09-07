package com.kai.video.view;

import androidx.recyclerview.widget.RecyclerView;
import android.content.Context;
import android.util.AttributeSet;
import android.view.FocusFinder;
import android.view.KeyEvent;
import android.view.View;

/**
 * @author: njb
 * @Date: 2020/6/30 17:57
 * @desc: 解决Recyclerview焦点乱跑问题
 */
public class FocusRecyclerView extends RecyclerView {
    public FocusRecyclerView(Context context) {
        super(context);
    }

    public FocusRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            int keyCode = event.getKeyCode();
            // 这里只考虑水平移动的情况（垂直移动相同的解决方案）
            if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                View focusedView = getFocusedChild();  // 获取当前获得焦点的view
                View nextFocusView;
                try {
                    if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                        // 通过findNextFocus获取下一个需要得到焦点的view
                        nextFocusView = FocusFinder.getInstance().findNextFocus(this, focusedView, View.FOCUS_LEFT);
                    } else {
                        // 通过findNextFocus获取下一个需要得到焦点的view
                        nextFocusView = FocusFinder.getInstance().findNextFocus(this, focusedView, View.FOCUS_RIGHT);
                    }
                } catch (Exception e) {
                    nextFocusView = null;
                }
                // 如果获取失败（也就是说需要交给系统来处理焦点， 消耗掉事件，不让系统处理， 并让先前获取焦点的view获取焦点）
                if (nextFocusView == null) {
                    focusedView.requestFocus();
                    return true;
                }
            }
            if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN || keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                View focusedView = getFocusedChild();  // 获取当前获得焦点的view
                View nextFocusView;
                try {
                    if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                        // 通过findNextFocus获取下一个需要得到焦点的view
                        nextFocusView = FocusFinder.getInstance().findNextFocus(this, focusedView, View.FOCUS_DOWN);
                    } else {
                        // 通过findNextFocus获取下一个需要得到焦点的view
                        nextFocusView = FocusFinder.getInstance().findNextFocus(this, focusedView, View.FOCUS_UP);
                    }
                } catch (Exception e) {
                    nextFocusView = null;
                }
                // 如果获取失败（也就是说需要交给系统来处理焦点， 消耗掉事件，不让系统处理， 并让先前获取焦点的view获取焦点）
                if (nextFocusView == null) {
                    focusedView.requestFocus();
                    return true;
                }
            }
        }
        return super.dispatchKeyEvent(event);
    }

}