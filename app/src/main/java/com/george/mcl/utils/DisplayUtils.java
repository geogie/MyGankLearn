package com.george.mcl.utils;

import android.content.Context;

/**
 * Created by georgeRen on 2017/7/12.
 */

public class DisplayUtils {
    public static int dpToPx(Context context, float dip) {
        final float SCALE = context.getResources().getDisplayMetrics().density;
        float valueDips = dip;
        int valuePixels = (int) (valueDips * SCALE + 0.5f);
        return valuePixels;
    }
}
