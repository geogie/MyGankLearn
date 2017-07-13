package com.george.mcl.fragment;

import android.animation.ValueAnimator;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.george.mcl.R;
import com.george.mcl.View.SlideInOutRightItemAnimator;
import com.george.mcl.adapter.DayFragmentAdapter;
import com.george.mcl.bean.GankDay;
import com.george.mcl.net.GankApi;
import com.jerey.loglib.LogTools;
import com.jerey.lruCache.DiskLruCacheManager;
import com.trello.rxlifecycle.FragmentEvent;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import butterknife.Bind;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by georgeRen on 2017/7/6.
 */

public class TodayFragment extends BaseFragment implements DatePickerDialog.OnDateSetListener {
    private static final String TAG = "TodayFragment";
    @Bind(R.id.toolbar)
    Toolbar mToolbar;
    @Bind(R.id.toolbar_layout)
    CollapsingToolbarLayout mToolbarLayout;
    @Bind(R.id.app_bar)
    AppBarLayout mAppBar;
    @Bind(R.id.day_recycleview)
    RecyclerView mRecyclerView;
    @Bind(R.id.story_img)
    ImageView mImageView;
    @Bind(R.id.float_action_button)
    FloatingActionButton mButton;

    private LinearLayoutManager mLinearLayoutManager;
    private DayFragmentAdapter mAdapter;
    int year;
    int month;
    int day;
    private DiskLruCacheManager mDiskLruCacheManager;

    @Override
    protected int returnLayoutID() {
        return R.layout.fragment_day;
    }

    @Override
    protected void afterCreate(Bundle savedInstanceState) {
        ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);
        initUI();
        dynamicAddView(mToolbarLayout, "ContentScrimColor", R.color.app_main_color);
        mAdapter = new DayFragmentAdapter(getActivity());
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.setTime(new Date());
        year = calendar.get(java.util.Calendar.YEAR);
        month = calendar.get(java.util.Calendar.MONTH) + 1;
        day = calendar.get(java.util.Calendar.DAY_OF_MONTH);
        //加载前一天的先
        load(year, month, day - 1);
        //再加载今天的
        load(year, month, day);
        try {
            mDiskLruCacheManager = new DiskLruCacheManager(getActivity(), "xiamin");
            Log.i(TAG, "DiskLruCacheManager 创建");
            GankDay gankDay = mDiskLruCacheManager.getAsSerializable(TAG);
            Log.i(TAG, "DiskLruCacheManager 读取数据 " + gankDay);
            if (gankDay != null) {
                loadUIByGankday(gankDay);
            }
        } catch (IOException e) {
            Log.e(TAG, "DiskLruCacheManager 创建失败");
            e.printStackTrace();
        }
    }

    private void initUI() {
        mToolbarLayout.setTitle("今日热点");
        mImageView.setImageResource(R.drawable.jay);
        //设置渐显动画，替换状态栏颜色
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int statusBarColor = ((AppCompatActivity) getActivity()).getWindow().getStatusBarColor();
            mToolbarLayout.setContentScrimColor(statusBarColor);
            if (statusBarColor != ((AppCompatActivity) getActivity()).getWindow().getStatusBarColor()) {
                ValueAnimator statusBarColorAnim = ValueAnimator.ofArgb(
                        ((AppCompatActivity) getActivity()).getWindow().getStatusBarColor(), statusBarColor);
                statusBarColorAnim.addUpdateListener(new ValueAnimator
                        .AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            ((AppCompatActivity) getActivity()).getWindow().setStatusBarColor((int) animation.getAnimatedValue());
                        }
                    }
                });
                //设置转换颜色的动画时间
                statusBarColorAnim.setDuration(1000L);
                statusBarColorAnim.setInterpolator(
                        new AccelerateInterpolator());
                statusBarColorAnim.start();
            }
        }
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mLinearLayoutManager = new LinearLayoutManager(mRecyclerView.getContext());
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar now = Calendar.getInstance();
                DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(
                        TodayFragment.this,
                        now.get(Calendar.YEAR),
                        now.get(Calendar.MONTH),
                        now.get(Calendar.DAY_OF_MONTH)
                );
                datePickerDialog.setVersion(DatePickerDialog.Version.VERSION_2);
                datePickerDialog.show(getActivity().getFragmentManager(), "Datepickerdialog");
            }
        });
    }

    private void load(final int year, final int month, final int day) {

        GankApi.getInstance()
                .getWebService()
                .getGoodsByDay(year, month, day)
                .compose(this.<GankDay>bindUntilEvent(FragmentEvent.DESTROY_VIEW))
                .cache()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<GankDay>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        showSnackbar(R.string.error);
                    }

                    @Override
                    public void onNext(GankDay gankDay) {
                        LogTools.d(gankDay.toString());
                        if (gankDay != null && gankDay.results != null && gankDay.results.Android != null) {
                            mToolbarLayout.setTitle(year + "年" + month + "月" + day + "日");
                            mDiskLruCacheManager.put(TAG, gankDay);
                            loadUIByGankday(gankDay);
                        } else {
                            showSnackbar("该日可能没有更新哦");
                        }
                    }
                });
    }

    private void loadUIByGankday(GankDay gankDay) {
        LogTools.d(gankDay.results.福利.get(0).getUrl());
        Glide.with(TodayFragment.this)
                .load(gankDay.results.福利.get(0).getUrl())
                .centerCrop()
                .crossFade()
                .error(R.drawable.jay)
                .into(mImageView);
        mAdapter.setData(gankDay.results);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setItemAnimator(new SlideInOutRightItemAnimator(mRecyclerView));
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        LogTools.d("DatePickerDialog" + year + "-" + monthOfYear + "-" + dayOfMonth);
        load(year, monthOfYear + 1, dayOfMonth);
    }
}
