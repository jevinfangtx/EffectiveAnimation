package com.tencent.effectiveanimation.AnimDemo;

import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;

import com.tencent.effectiveanimation.R;
import com.tencent.effectiveanimation.core.ImageFetcher;
import com.tencent.effectiveanimation.core.SmartAnimation;
import com.tencent.effectiveanimation.core.SmartAnimation1;

import java.util.ArrayList;
import java.util.List;


public class DrawableAnimActivity extends AppCompatActivity {

    private static final String TAG = "datata";
    // 显示模式，1两个都显示，2只显示AnimationDrawable，3只显示SmartAnimation
    public static final int SHOW_MODE = 3;
    public static final boolean SET_CALLBACK = true;
    private ImageView mAnimView;
    private ImageView mSmartView;
    private AnimationDrawable mDrawableAnim;
    private SmartAnimation mSmartAnim;

    private ImageFetcher mImageFetcher;
    private DrawableProvider mProvider;

    private Handler mHandler = new Handler(Looper.getMainLooper());

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawable_ainm);
        mProvider = new DrawableProvider();
        if (SHOW_MODE == 1 || SHOW_MODE == 2) {
            mAnimView = findViewById(R.id.anim_image);
            mDrawableAnim = (AnimationDrawable) mAnimView.getDrawable();
        }

        if (SHOW_MODE == 1 || SHOW_MODE == 3) {
            mSmartAnim = new SmartAnimation();
            mSmartAnim.initialize(this, mProvider.mAnimItems, null);
            mSmartView = findViewById(R.id.smart_image);
            mSmartView.setImageDrawable(mSmartAnim);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // 因为动画的资源是异步加载的，只能等资源都加载了才能显示动画，否则动画会不显示
        if (SHOW_MODE == 1 || SHOW_MODE == 2) {
            if (SET_CALLBACK) {
                mDrawableAnim.setCallback(new DrawableCallback(mDrawableAnim, mAnimView, "Drawable"));
            }
            mDrawableAnim.start();
        }

        if (SHOW_MODE == 1 || SHOW_MODE == 3) {
//            if (SET_CALLBACK) {
//                mSmartAnim.setCallback(new DrawableCallback(mSmartAnim, mSmartView, "Smart"));
//            }
            mSmartAnim.start();

//            mImageFetcher.addImageCache(R.drawable.c_anim_085, new ImageFetcher.Callback() {
//                @Override
//                public void callback(BitmapDrawable dr) {
//                    mSmartAnim.addFrame(dr, 40);
//                    mSmartView.setImageDrawable(mSmartAnim);
//                    mSmartAnim.start();
//                }
//            });
        }
    }

    private class DrawableCallback extends AnimationDrawableCallback {
        private String mTag;
        long start;
        private List<Long> times = new ArrayList<>();


        public DrawableCallback(AnimationDrawable animationDrawable, Drawable.Callback callback, String tag) {
            super(animationDrawable, callback);
            mTag = tag;
        }

        public DrawableCallback(SmartAnimation smartAnimation, Drawable.Callback callback, String tag) {
            super(smartAnimation, callback);
            mTag = tag;
        }

        @Override
        public void onAnimationStart() {
            start = SystemClock.uptimeMillis();
        }

        @Override
        public void onAnimationComplete() {
            long time = SystemClock.uptimeMillis() - start;
            times.add(time);
            long total = sum(times);
            Log.e(TAG, mTag + " current = " + time
                    + ", count = " + times.size()
                    + ", total = " + total
                    + ", average = " + total / times.size());
        }

        private long sum(List<Long> list) {
            long total = 0;
            for (long e : list) {
                total += e;
            }
            return total;
        }
    }
}
