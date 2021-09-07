package com.kai.video.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatTextView;

import com.kai.video.R;
import com.kai.video.tool.LogUtil;

import java.util.Timer;
import java.util.TimerTask;

public class ScrollTextView extends AppCompatTextView {

    private static final String TAG = "ScrollTextView";
    private String mText = "";
    private int mOffsetX = 0;
    private Rect mRect;
    private Timer mTimer;
    private TimerTask mTimerTask;
    /**
     * 速度，负数左移，正数右移。
     */
    private int mSpeed = -8;
    private static final int PFS = 24;
    private int gravity;

    public ScrollTextView(Context context) {
        this(context, null);
    }
    public ScrollTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mRect = new Rect();
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.ScrollTextView);
        gravity = ta.getInteger(R.styleable.ScrollTextView_gravity, 0);
    }

    private class MyTimerTask extends TimerTask {
        @Override
        public void run() {
            //如果View能容下所有文字，直接返回
            if (mRect.right < getWidth()){
                return;
            }
            if (mOffsetX < - mRect.right - getPaddingEnd()){
                //左移时的情况
                mOffsetX = getPaddingStart() + mRect.right;
            } else if (mOffsetX > getPaddingStart() + mRect.right){
                //右移时的情况
                mOffsetX = - mRect.right -getPaddingEnd();
            }
            mOffsetX += mSpeed;
            postInvalidate();
        }
    }
    /*
    手动控制绘制的开始
     */
    boolean hasStart = false;
    public void startScroll(){
        hasStart = true;
        mOffsetX = mRect.right/2;
        mTimer = new Timer();
        mTimerTask = new MyTimerTask();
        mTimer.schedule(mTimerTask, 0, 1000 / 15);
    }
    /*
    手动结束绘制
     */
    public void stopScroll(){
        hasStart = false;
        mOffsetX = mRect.right/2;
        postInvalidate();
        LogUtil.e(TAG, "killTimer");
        if (mTimerTask != null){
            mTimerTask.cancel();
            mTimerTask = null;
        }
        if (mTimer != null){
            mTimer.cancel();
            mTimer = null;
        }
    }
    @Override
    protected void onDraw(Canvas canvas) {
        mText = getText().toString();
        TextPaint textPaint = getPaint();
        textPaint.setColor(getCurrentTextColor());
        textPaint.setTextSize(getTextSize());

        float textWidth = textPaint.measureText(mText);
        //获取文本区域大小，保存在mRect中。
        textPaint.getTextBounds(mText, 0, mText.length(), mRect);
        float mTextCenterVerticalToBaseLine =
            ( - textPaint.ascent() + textPaint.descent()) / 2 - textPaint.descent();
        if (!hasStart){
            if (gravity == 0){
                textPaint.setTextAlign(Paint.Align.CENTER);
                if (getWidth() < textWidth){
                    mText =  TextUtils.ellipsize(mText,new TextPaint(textPaint),getWidth(),TextUtils.TruncateAt.END).toString();
                    canvas.drawText(mText, getWidth()/2, getHeight() / 2 + mTextCenterVerticalToBaseLine, textPaint);
                }else {

                    canvas.drawText(mText, getWidth()/2, getHeight() / 2 + mTextCenterVerticalToBaseLine, textPaint);
                }
            }else {
                if (getWidth() < textWidth){
                    mText =  TextUtils.ellipsize(mText,new TextPaint(textPaint),getWidth(),TextUtils.TruncateAt.END).toString();
                    canvas.drawText(mText, 0, getHeight() / 2 + mTextCenterVerticalToBaseLine, textPaint);
                }else {

                    canvas.drawText(mText, 0, getHeight() / 2 + mTextCenterVerticalToBaseLine, textPaint);
                }
            }

            return;

        }
        if (mRect.right < getWidth()){
            if (gravity == 0){
                textPaint.setTextAlign(Paint.Align.CENTER);
                canvas.drawText(mText, getWidth()/2, getHeight() / 2 + mTextCenterVerticalToBaseLine, textPaint);
            }else {
                canvas.drawText(mText, 0, getHeight() / 2 + mTextCenterVerticalToBaseLine, textPaint);
            }

        }else {
            canvas.translate(mOffsetX, 0);
            canvas.drawText(mText, 0, getHeight() / 2 + mTextCenterVerticalToBaseLine, textPaint);
        }


    }


}