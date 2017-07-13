package com.george.mcl.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.george.mcl.R;
import com.george.mcl.adapter.HomeFragmentPagerAdapter;
import com.george.mcl.net.Config;
import com.jerey.loglib.LogTools;

import butterknife.Bind;

/**
 * Created by georgeRen on 2017/7/6.
 */

public class HomeFragment extends BaseFragment{
    @Bind(R.id.viewpager)
    ViewPager mViewPager;
    @Bind(R.id.toolbar)
    Toolbar mToolbar;
    @Bind(R.id.tabLayout)
    TabLayout mTabLayout;
    private HomeFragmentPagerAdapter mFragmentAdapter;

    @Override
    protected int returnLayoutID() {
        return R.layout.fragment_home;
    }

    @Override
    protected void afterCreate(Bundle savedInstanceState) {
        LogTools.i("afterCreate");
        ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);
        dynamicAddView(mToolbar, "background", R.color.app_main_color);
        /**
         * 注: 在该Fragment设置mToolbar的onOptionsItemSelected是无效的
         */
        mFragmentAdapter = new HomeFragmentPagerAdapter(getChildFragmentManager());

        mViewPager.setAdapter(mFragmentAdapter);

        //根据类型添加fragment
        for (String type : Config.TYPES) {
            mFragmentAdapter.addFragment(ListFragment.getListFragment(type), type);
        }
        mFragmentAdapter.notifyDataSetChanged();
        //所有子fragment均不销毁
        mViewPager.setOffscreenPageLimit(mFragmentAdapter.getCount());
        mTabLayout.setupWithViewPager(mViewPager);
        mViewPager.setCurrentItem(0);
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        LogTools.i("onAttach");
    }

}
