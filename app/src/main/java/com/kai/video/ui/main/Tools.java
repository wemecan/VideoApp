package com.kai.video.ui.main;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.ViewGroup;

public class Tools {

    private static int mFocusWidth;
    private static int mFoucsHeight;

    public static void focusAnimator(View v, View onFousView) {
        focusAnimator(v, onFousView, -1, 0, 0);
    }




    public static void focusAnimator(View parentView, final View focusView, int scrollY, int offSetX, int offSetY) {
        int[] fromLocation = new int[2];
        focusView.getLocationOnScreen(fromLocation);

        int fromWidth = focusView.getWidth();
        int fromHeight = focusView.getHeight();
        float fromX = fromLocation[0];
        float fromY = fromLocation[1];

        int[] toLocation = new int[2];
        parentView.getLocationOnScreen(toLocation);

        int toWidth = parentView.getWidth() + offSetX;
        int toHeight = parentView.getHeight() + offSetY;
        float toX = toLocation[0] - offSetX / 2;
        float toY = toLocation[1] - offSetY / 2;



        if (scrollY == -1) {
            if (focusView.getVisibility() == View.GONE)
                focusView.setVisibility(View.VISIBLE);
        }

        AnimatorSet animatorSet = new AnimatorSet();
        ObjectAnimator translateXAnimator = ObjectAnimator.ofFloat(focusView, "x", fromX, toX);
        translateXAnimator.addListener(new Animator.AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (focusView.getVisibility() == View.GONE) {
                    focusView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                if (focusView.getVisibility() == View.GONE)
                    focusView.setVisibility(View.VISIBLE);
            }
        });
        ObjectAnimator translateYAnimator = ObjectAnimator.ofFloat(focusView, "y", fromY, toY);
        ValueAnimator scaleWidthAnimator = ObjectAnimator.ofFloat(focusView, "width", fromWidth, toWidth);
        scaleWidthAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float width = (Float) animation.getAnimatedValue();
                mFocusWidth = (int) width;
                ViewGroup.LayoutParams layoutParams = focusView.getLayoutParams();
                layoutParams.width = mFocusWidth;
                layoutParams.height = mFoucsHeight;
                focusView.setLayoutParams(layoutParams);
            }
        });
        ValueAnimator scaleHeightAnimator = ObjectAnimator.ofFloat(focusView, "height", fromHeight, toHeight);
        scaleHeightAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float height = (Float) animation.getAnimatedValue();
                mFoucsHeight = (int) height;
                ViewGroup.LayoutParams layoutParams = focusView.getLayoutParams();
                layoutParams.width = mFocusWidth;
                layoutParams.height = mFoucsHeight;
                focusView.setLayoutParams(layoutParams);
            }
        });
        animatorSet.playTogether(translateXAnimator, translateYAnimator, scaleWidthAnimator, scaleHeightAnimator);
        animatorSet.setDuration(150);
        animatorSet.start();
    }

}


