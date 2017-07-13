package com.george.mcl.fragment;

import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.george.mcl.R;
import com.george.mcl.View.SlideInOutRightItemAnimator;
import com.george.mcl.View.SwipeToRefreshLayout;
import com.george.mcl.adapter.MeiziAdapter;
import com.george.mcl.bean.Data;
import com.george.mcl.net.GankApi;
import com.jerey.loglib.LogTools;
import com.jerey.lruCache.DiskLruCacheManager;
import com.trello.rxlifecycle.FragmentEvent;

import java.io.IOException;

import butterknife.Bind;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by georgeRen on 2017/7/6.
 */

public class MeiziFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener{

    private static final String TAG = "MeiziFragment";

    @Bind(R.id.recycler_view)
    RecyclerView mRecyclerView;
    @Bind(R.id.swipe_ly)
    SwipeToRefreshLayout mSwipeRefreshLayout;
    @Bind(R.id.toolbar)
    Toolbar mToolBar;

    private StaggeredGridLayoutManager mStaggeredGridLayoutManager;

    //当前页数
    private int currentPager = 1;
    //是否刷新状态
    private boolean isLoadingNewData = false;
    //是否载入更多状态
    private boolean isLoadingMore = false;
    //是否已经载入去全部
    private boolean isALlLoad = false;
    MeiziAdapter mAdapter;
    private DiskLruCacheManager mDiskLruCacheManager;

    @Override
    protected int returnLayoutID() {
        return R.layout.fragment_meizi;
    }

    @Override
    protected void afterCreate(Bundle savedInstanceState) {
        ((AppCompatActivity) getActivity()).setSupportActionBar(mToolBar);
        mToolBar.setTitle("妹子");
        mToolBar.setTitleTextColor(Color.WHITE);
        dynamicAddView(mToolBar, "background", R.color.app_main_color);
        initRecyclerView(mRecyclerView);
        initSwipeRefreshLayout(mSwipeRefreshLayout);
        mAdapter = new MeiziAdapter(getActivity());
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setItemAnimator(new SlideInOutRightItemAnimator(mRecyclerView));
        mRecyclerView.addOnScrollListener(mOnScrollListener);

        try {
            Log.i(TAG, "DiskLruCacheManager 创建");
            mDiskLruCacheManager = new DiskLruCacheManager(getActivity());
            Data data = mDiskLruCacheManager.getAsSerializable(TAG);
            Log.i(TAG, "DiskLruCacheManager 读取");
            if (data != null) {
                Log.i(TAG, "获取到缓存数据");
                mAdapter.setData(data.getResults());
                mAdapter.notifyDataSetChanged();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        onRefresh();
    }
    private void initRecyclerView(RecyclerView recyclerView) {
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        mStaggeredGridLayoutManager = new StaggeredGridLayoutManager(2,
                StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(mStaggeredGridLayoutManager);
    }

    private void initSwipeRefreshLayout(SwipeRefreshLayout swipeRefreshLayout) {
        Resources resources = getResources();
        swipeRefreshLayout.setColorSchemeColors(resources.getColor(R.color.blue_dark),
                resources.getColor(R.color.red_dark),
                resources.getColor(R.color.yellow_dark),
                resources.getColor(R.color.green_dark)
        );
        swipeRefreshLayout.setOnRefreshListener(this);
    }
    @Override
    public void onRefresh() {
        LogTools.i("开始刷新");
        mSwipeRefreshLayout.setRefreshing(true);
        isLoadingMore = false;
        isLoadingNewData = true;
        loadData(1);
    }
    int[] lastPositions;
    private RecyclerView.OnScrollListener mOnScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            //但RecyclerView滑动到倒数第三个之请求加载更多
            if (lastPositions == null) {
                lastPositions = new int[mStaggeredGridLayoutManager.getSpanCount()];
            }
            int[] lastVisibleItem = mStaggeredGridLayoutManager.findLastVisibleItemPositions(lastPositions);
            int totalItemCount = mAdapter.getItemCount();
            // dy>0 表示向下滑动
            if (lastVisibleItem[0] >= totalItemCount - 4 && dy > 0 && !isLoading() && !isALlLoad) {
                requestMoreData();
            }
        }
    };

    private boolean isLoading() {
        return isLoadingMore || isLoadingNewData;
    }

    private void requestMoreData() {
        LogTools.i("加载更多");
        mSwipeRefreshLayout.setRefreshing(true);
        isLoadingMore = true;
        isLoadingNewData = false;
        loadData(++currentPager);
    }
    private void loadData(int pager) {
        GankApi.getInstance()
                .getWebService()
                .getBenefitsGoods(GankApi.LOAD_LIMIT, pager)
                .compose(this.<Data>bindUntilEvent(FragmentEvent.DESTROY_VIEW))
                .cache()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(dataObservable);
    }

    private Observer<Data> dataObservable = new Observer<Data>() {

        @Override
        public void onCompleted() {
            LogTools.i("数据onCompleted 停止刷新");
            mSwipeRefreshLayout.setRefreshing(false);
            isLoadingNewData = false;
            isLoadingMore = false;
        }

        @Override
        public void onError(Throwable e) {
            LogTools.i("onError 停止刷新");
            mSwipeRefreshLayout.setRefreshing(false);
            isLoadingNewData = false;
            isLoadingMore = false;
            showSnackbar("OnError");
        }

        @Override
        public void onNext(Data data) {
            LogTools.i("onNext " + data.toString());
            if (data != null && data.getResults() != null) {
                /**
                 * 没有更多数据
                 */
                if (data.getResults().size() < GankApi.LOAD_LIMIT) {
                    isALlLoad = true;
                    showSnackbar(R.string.no_more);
                }

                if (isLoadingMore) {
                    mAdapter.addData(data.getResults());
                } else if (isLoadingNewData) {
                    isALlLoad = false;
                    mAdapter.setData(data.getResults());
                    Log.i(TAG, "DiskLruCacheManager 写入");
                    mDiskLruCacheManager.put(TAG, data);
                }
                mAdapter.notifyDataSetChanged();
            }
        }
    };


}
