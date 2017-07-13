package com.george.mcl.View;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;

/**
 * Created by georgeRen on 2017/7/10.
 *
 * 如果view还在测量中：则刷新不可调用。
 */

public class SwipeToRefreshLayout extends SwipeRefreshLayout {

    public SwipeToRefreshLayout(Context context) {
        super(context);
    }

    public SwipeToRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    private boolean mMeasured = false;
    private boolean mPreMeasureRefreshing = false;

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (!mMeasured) {
            mMeasured = true;
            setRefreshing(mPreMeasureRefreshing);
        }
    }


    @Override
    public void setRefreshing(boolean refreshing) {
        if (mMeasured) {
            super.setRefreshing(refreshing);
        } else {
            mPreMeasureRefreshing = refreshing;
        }
    }
}
