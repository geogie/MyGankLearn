package com.george.mcl.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.ClipboardManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.george.mcl.R;
import com.george.mcl.base.AppSwipeBackActivity;
import com.george.mcl.utils.ApplicationUtils;
import com.jerey.loglib.LogTools;

import java.io.File;

import butterknife.Bind;

/**
 * Created by georgeRen on 2017/7/10.
 *
 */

public class WebFragment extends BaseFragment {
    private static final String TAG = "WebFragment";
    public static final String DATA_ID = "data_id";
    public static final String DATA_TITLE = "data_title";
    public static final String DATA_URL = "data_url";
    public static final String DATA_TYPE = "data_type";
    public static final String DATA_WHO = "data_who";

    @Bind(R.id.progress_bar)
    ProgressBar mProgressBar;
    @Bind(R.id.web_view)
    android.webkit.WebView mWebView;

    Toolbar mToolBar;
    private String mTitle;
    private String mURl;

    @Override
    protected int returnLayoutID() {
        return R.layout.fragment_web;
    }

    @Override
    protected void afterCreate(Bundle savedInstanceState) {
        Log.d(TAG, "afterCreate");
        mToolBar = (Toolbar) mContainView.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(mToolBar);
        dynamicAddView(mToolBar, "background", R.color.app_main_color);
        mTitle = getArguments().getString(DATA_TITLE);
        mURl = getArguments().getString(DATA_URL);
        initWebView();
        mWebView.loadUrl(mURl);
    }

    private void initWebView() {
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(android.webkit.WebView view, String url) {
                //返回值是true的时候控制去WebView打开，为false调用系统浏览器或第三方浏览器
                view.loadUrl(url);
                return true;
            }
        });
        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(android.webkit.WebView view, int newProgress) {
                if (newProgress == 100) {
                    // 网页加载完成
                    if (mProgressBar != null) {
                        mProgressBar.setVisibility(View.GONE);
                        mProgressBar.setProgress(0);
                    }
                } else {
                    // 加载中
                    if (mProgressBar != null) {
                        mProgressBar.setVisibility(View.VISIBLE);
                        mProgressBar.setProgress(newProgress);
                    }
                }
            }
        });
        mWebView.getSettings().setJavaScriptEnabled(true);
        /**
         * 根据网络状态,设置浏览器的缓存策略
         */
        if (ApplicationUtils.isNetworkAvailable(getContext())) {
            mWebView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        } else {
            //只要本地有，无论是否过期，或者no-cache，都使用缓存中的数据。
            LogTools.w("无网络,加载缓存网页");
            showSnackbar("无网络,加载缓存网页");
            mWebView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        }
        mWebView.getSettings().setSupportZoom(true);
        mWebView.getSettings().setDisplayZoomControls(true);
        String absolutePath = getContext().getCacheDir().getAbsolutePath();// webview 默认路径

        double totalM = getWebViewCache(absolutePath);
        Log.d(TAG, "webView缓存大小totalM：" + totalM + "M");

    }

    public static double getDirSize(File file) {
        //判断文件是否存在
        if (file.exists()) {
            //如果是目录则递归计算其内容的总大小
            if (file.isDirectory()) {
                File[] children = file.listFiles();
                double size = 0;
                for (File f : children)
                    size += getDirSize(f);
                return size;
            } else {//如果是文件则直接返回其大小,以“兆”为单位
                double size = (double) file.length() / 1024 / 1024;
                return size;
            }
        } else {
            Log.d(TAG, "文件或者文件夹不存在，请检查路径是否正确！");
            return 0.0;
        }
    }

    private double getWebViewCache(String absolutePath) {
        File audioCache = new File(absolutePath);
        return getDirSize(audioCache);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_web, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_copy_the_url:
                ClipboardManager c = (ClipboardManager) getActivity().getSystemService(getActivity().CLIPBOARD_SERVICE);
                c.setText(mURl);

                break;
            case R.id.action_open_in_browser:
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mURl));
                getActivity().startActivity(intent);
                break;
            case R.id.action_share:
                Intent intent1 = new Intent(Intent.ACTION_SEND);
                intent1.setType("text/plain");
                intent1.putExtra(Intent.EXTRA_TEXT, mURl);
                intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getActivity().startActivity(Intent.createChooser(intent1, mTitle));
                break;
            case android.R.id.home:// Toolbar中的白色返回键（actionbar的最左边就是home图标和标题区域）
                LogTools.d("onOptionsItemSelected android.R.id.home");
                if (mWebView != null && mWebView.canGoBack()) {
                    mWebView.goBack();
                } else {
                    ((AppSwipeBackActivity) getActivity()).scrollToFinishActivity();
                }
                break;

        }
        return super.onOptionsItemSelected(item);
    }
}
