package com.george.mcl.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.jerey.themelib.loader.SkinManager;

/**
 * Created by georgeRen on 2017/7/10.
 */

public class AnimationHelper {
    public static final int MINI_RADIUS = 0;
    public static final int DEFAULT_DURIATION = 500;
    @SuppressLint("NewApi")
    public static void startActivityForResult(
            final Activity thisActivity, final Intent intent, final Integer requestCode,
            final Bundle bundle, final View view,
            int colorOrImageRes, final long durationMills) {
        // SDK 低于LOLLIPOP不做处理,直接跳转
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            if (requestCode == null) {
                thisActivity.startActivity(intent);
            } else if (bundle == null) {
                thisActivity.startActivityForResult(intent, requestCode);
            } else {
                thisActivity.startActivityForResult(intent, requestCode, bundle);
            }
            return;
        }
        int[] location = new int[2];
        view.getLocationInWindow(location);
        final int xCenter = location[0] + view.getWidth() / 2;
        final int yCenter = location[1] + view.getHeight() / 2;
        final ImageView imageView = new ImageView(thisActivity);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setBackgroundColor(SkinManager.getInstance().getColor(colorOrImageRes));

        final ViewGroup decorView = (ViewGroup) thisActivity.getWindow().getDecorView();
        int w = decorView.getWidth();
        int h = decorView.getHeight();
        decorView.addView(imageView, w, h);

        // 计算中心点至view边界的最大距离
        int maxW = Math.max(xCenter, w - xCenter);
        int maxH = Math.max(yCenter, h - yCenter);
        final int finalRadius = (int) Math.sqrt(maxW * maxW + maxH * maxH) + 1;

        Animator anim = ViewAnimationUtils.createCircularReveal(imageView, xCenter, yCenter, 0, finalRadius);
        int maxRadius = (int) Math.sqrt(w * w + h * h) + 1;
        long finalDuration = durationMills;
        /**
         * 计算时间
         */
        if (finalDuration == DEFAULT_DURIATION) {
            // 算出实际边距与最大边距的比率
            double rate = 1d * finalRadius / maxRadius;
            // 水波扩散的距离与扩散时间成正比
            finalDuration = (long) (DEFAULT_DURIATION * rate);
        }
        anim.setDuration(finalDuration);
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (requestCode == null) {
                    thisActivity.startActivity(intent);
                } else if (bundle == null) {
                    thisActivity.startActivityForResult(intent, requestCode);
                } else {
                    thisActivity.startActivityForResult(intent, requestCode, bundle);
                }

                // 默认渐隐过渡动画.
                thisActivity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                // 默认显示返回至当前Activity的动画.
                view.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Animator anim =
                                ViewAnimationUtils.createCircularReveal(imageView, xCenter, yCenter, finalRadius, 0);
                        anim.setDuration(durationMills);
                        anim.addListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                try {
                                    decorView.removeView(imageView);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        anim.start();
                    }
                }, 1000);
            }
        });
        anim.start();
    }
    public static void startActivity(
            Activity thisActivity, Intent intent, View triggerView, int colorOrImageRes, long durationMills) {
        startActivityForResult(thisActivity, intent, null, null, triggerView, colorOrImageRes, durationMills);
    }

    public static void startActivity(
            Activity thisActivity, Intent intent, View triggerView, int colorOrImageRes) {
        startActivity(thisActivity, intent, triggerView, colorOrImageRes, DEFAULT_DURIATION);
    }
}
