package com.george.mcl.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.george.mcl.R;
import com.george.mcl.View.PinchImageView;
import com.george.mcl.base.AppSwipeBackActivity;
import com.george.mcl.utils.AnimationHelper;
import com.george.mcl.utils.ImageSave;
import com.jerey.loglib.LogTools;

import butterknife.Bind;
import butterknife.ButterKnife;

public class PhotoActivity extends AppSwipeBackActivity implements View.OnClickListener{
    private static final String TAG = "PhotoActivity";
    private static final String URL = "URL";

    @Bind(R.id.meizi_image)
    PinchImageView pinchImageView;
    @Bind(R.id.btn_back)
    ImageView mBtnBack;
    @Bind(R.id.btn_save)
    ImageView mBtnSave;

    private String mUrl;
    private Bitmap mBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // 透明状态栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            // 透明导航栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
        setContentView(R.layout.activity_photo);
        ButterKnife.bind(this);
        mBtnBack.setOnClickListener(this);
        mBtnSave.setOnClickListener(this);
        Intent intent = getIntent();
        mUrl = intent.getStringExtra(URL);
        LogTools.d(TAG, "url: " + mUrl);
        Glide.with(this)
                .load(mUrl)
                .asBitmap()
                .error(R.drawable.bg_cyan)
                .placeholder(R.drawable.bg_cyan)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                        pinchImageView.setImageBitmap(resource);
                        mBitmap = resource;
                    }
                });
    }
    public static void startActivity(AppCompatActivity activity, String url, View transitionView) {
        Intent intent = new Intent(activity, PhotoActivity.class);
        intent.putExtra(URL, url);

        // 这里指定了共享的视图元素
//        ActivityOptionsCompat options = ActivityOptionsCompat
//                .makeSceneTransitionAnimation(activity, transitionView, "image");
//
//        ActivityCompat.startActivity(activity, intent, options.toBundle());
        // 使用覆盖动画, 体验更好
        AnimationHelper.startActivity(activity
                , intent
                , transitionView
                , R.color.app_main_color);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_back:
                LogTools.d("点击back,结束Activity");
                finish();
                overridePendingTransition(R.anim.out_to_bottom, 0);
                break;
            case R.id.btn_save:
                LogTools.d("点击保存,保存图片");
                Toast.makeText(this, "保存图片", Toast.LENGTH_SHORT).show();
                ImageSave.with(getApplicationContext())
                        .save(mBitmap)
                        .setImageSaveListener(new ImageSave.ImageSaveListener() {
                            @Override
                            public void onSuccess() {
                                Toast.makeText(PhotoActivity.this, "保存成功", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onError() {
                                Toast.makeText(PhotoActivity.this, "保存失败", Toast.LENGTH_SHORT).show();
                            }
                        });
                break;
        }
    }
}
