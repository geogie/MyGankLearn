package com.george.mcl;

import com.jerey.themelib.SkinConfig;
import com.jerey.themelib.base.SkinBaseApplication;

/**
 * Created by georgeRen on 2017/7/6.
 */

public class GankApp extends SkinBaseApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        SkinConfig.setCanChangeStatusColor(true);
        SkinConfig.setCanChangeFont(true);
        SkinConfig.setDebug(false);
    }
}
