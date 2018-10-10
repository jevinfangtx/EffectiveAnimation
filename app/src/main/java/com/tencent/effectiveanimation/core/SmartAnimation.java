package com.tencent.effectiveanimation.core;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.tencent.effectiveanimation.R;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SmartAnimation extends DrawableContainer implements Runnable, Animatable {
    // default cache image number
    private static final int DEFAULT_CACHE_NUM = 2;

    private AnimationState mAnimationState;
    private ImageFetcher mImageFetcher;
    private List<DrawableItem> mDrawableItems;
    private int[] mCacheDrawable;

    /** The current frame, ranging from 0 to {@link #mAnimationState#getChildCount() - 1} */
    private int mCurFrame = 0;

    /** Whether the drawable has an animation callback posted. */
    private boolean mRunning;

    /** Whether the drawable should animate when visible. */
    private boolean mAnimating;

    private boolean mMutated;

    private boolean mCaching;
//    private List<Long> mTimes = new ArrayList<>();
    private long start;

    public SmartAnimation() {
        this(null, null);
    }

    /**
     * Sets whether this AnimationDrawable is visible.
     * <p>
     * When the drawable becomes invisible, it will pause its animation. A subsequent change to
     * visible with <code>restart</code> set to true will restart the animation from the
     * first frame. If <code>restart</code> is false, the drawable will resume from the most recent
     * frame. If the drawable has already reached the last frame, it will then loop back to the
     * first frame, unless it's a one shot drawable (set through {@link #setOneShot(boolean)}),
     * in which case, it will stay on the last frame.
     *
     * @param visible true if visible, false otherwise
     * @param restart when visible, true to force the animation to restart
     *                from the first frame
     * @return true if the new visibility is different than its previous state
     */
    @Override
    public boolean setVisible(boolean visible, boolean restart) {
        final boolean changed = super.setVisible(visible, restart);
        if (visible) {
            if (restart || changed) {
                boolean startFromZero = restart || (!mRunning && !mAnimationState.mOneShot) ||
                        mCurFrame >= mAnimationState.getChildCount();
                setFrame(startFromZero ? 0 : mCurFrame, true, mAnimating);
            }
        } else {
            unscheduleSelf(this);
        }
        return changed;
    }

    /**
     * Starts the animation from the first frame, looping if necessary. This method has no effect
     * if the animation is running.
     * <p>
     * <strong>Note:</strong> Do not call this in the
     * {@link android.app.Activity#onCreate} method of your activity, because
     * the {@link AnimationDrawable} is not yet fully attached to the window.
     * If you want to play the animation immediately without requiring
     * interaction, then you might want to call it from the
     * {@link android.app.Activity#onWindowFocusChanged} method in your
     * activity, which will get called when Android brings your window into
     * focus.
     *
     * @see #isRunning()
     * @see #stop()
     */
    @Override
    public void start() {
        mAnimating = true;

        if (!isRunning()) {
            // initialize cache frame
            cacheFrame(0, true);
            // Start from 0th frame.
            setFrame(0, false, mDrawableItems.size() > 1
                    || !mAnimationState.mOneShot);
        }
    }

    /**
     * Stops the animation at the current frame. This method has no effect if the animation is not
     * running.
     *
     * @see #isRunning()
     * @see #start()
     */
    @Override
    public void stop() {
        mAnimating = false;
        if (isRunning()) {
            mCurFrame = 0;
            unscheduleSelf(this);
        }
        if (mImageFetcher != null) {
            mImageFetcher.clearCache();
        }
    }

    /**
     * Indicates whether the animation is currently running or not.
     *
     * @return true if the animation is running, false otherwise
     */
    @Override
    public boolean isRunning() {
        return mRunning;
    }

    /**
     * This method exists for implementation purpose only and should not be
     * called directly. Invoke {@link #start()} instead.
     *
     * @see #start()
     */
    @Override
    public void run() {
        nextFrame(false);
    }

    @Override
    public void unscheduleSelf(Runnable what) {
        mRunning = false;
        super.unscheduleSelf(what);
    }

    /**
     * @return The number of frames in the animation
     */
    public int getNumberOfFrames() {
        return mAnimationState.getChildCount();
    }

    /**
     * @return The Drawable at the specified frame index
     */
    public Drawable getFrame(int index) {
        return mAnimationState.getChild(index);
    }

    /**
     * @return The duration in milliseconds of the frame at the
     *         specified index
     */
    public int getDuration(int i) {
        return mAnimationState.mDurations[i];
    }

    /**
     * @return True of the animation will play once, false otherwise
     */
    public boolean isOneShot() {
        return mAnimationState.mOneShot;
    }

    /**
     * Sets whether the animation should play once or repeat.
     *
     * @param oneShot Pass true if the animation should only play once
     */
    public void setOneShot(boolean oneShot) {
        mAnimationState.mOneShot = oneShot;
    }

    /**
     * Adds a frame to the animation
     *
     * @param frame The frame to add
     * @param duration How long in milliseconds the frame should appear
     */
    public void addFrame(@NonNull Drawable frame, int duration) {
        mAnimationState.addFrame(frame, duration);
        if (!mRunning) {
            setFrame(0, true, false);
        }
    }

    private void nextFrame(boolean unschedule) {
        int curFrame = mCurFrame;
        int nextFrame = mCurFrame + 1;
        final int numFrames = mDrawableItems.size();
        final boolean isLastFrame = mAnimationState.mOneShot && nextFrame >= (numFrames - 1);

        // Loop if necessary. One-shot animations should never hit this case.
        if (!mAnimationState.mOneShot && nextFrame >= numFrames) {
            nextFrame = 0;
        }
//        addTime();
//        showTimes();

        setFrame(nextFrame, unschedule, !isLastFrame);

        // set and then remove current
        removeFrame(curFrame);
    }

    private void setFrame(int frame, boolean unschedule, boolean animate) {
//        mTimes.clear();
//        start = SystemClock.uptimeMillis();
        if (frame >= mDrawableItems.size()) {
            return;
        }

        mAnimating = animate;
        mCurFrame = frame;
        DrawableItem item = mDrawableItems.get(mCurFrame);
        BitmapDrawable drawable = mImageFetcher.getImageCache(item.resource);
        if (drawable != null) {
            if (mCurFrame < mAnimationState.getChildCount()) {
                mAnimationState.setFrame(drawable, item.duration, mCurFrame);
            } else {
                mAnimationState.addFrame(drawable, item.duration);
            }
            drawable.setCallback(this);

            selectDrawable(frame);

            // cache next frame image
            cacheFrame(frame, false);

            if (unschedule || animate) {
                unscheduleSelf(this);
            }
            if (animate) {
                // Unscheduling may have clobbered these values; restore them
                mCurFrame = frame;
                mRunning = true;
                scheduleSelf(this, SystemClock.uptimeMillis() + mAnimationState.mDurations[frame]);
            }
//            addTime();
        } else {
            cacheFrame(frame, true);
        }
    }

    private void removeFrame(int frame) {
        mAnimationState.removeFrame(frame);
        DrawableItem item = mDrawableItems.get(frame);
        if (item != null) {
            mImageFetcher.removeImageCache(item.resource);
        }
    }

    private void cacheFrame(int frame, boolean current) {
        if (!current) {
            frame = frame + 1;
        }

        if (mCacheDrawable == null) {
            mCacheDrawable = new int[DEFAULT_CACHE_NUM * 2];
        }
        for (int i = 0; i < DEFAULT_CACHE_NUM; i++) {
            frame = frame + i;
            if (frame >= mDrawableItems.size()) {
                frame = 0;
            }
            mCacheDrawable[i] = frame;
            mCacheDrawable[i+1] = mDrawableItems.get(frame).resource;
        }
        mCaching = current;
        mImageFetcher.addCacheList(mCacheDrawable, DEFAULT_CACHE_NUM, current);
    }

    private final ImageFetcher.Callback mFetcherCallback = new ImageFetcher.Callback() {
        @Override
        public void callback(int frame) {
            if (mCurFrame == frame && mCaching) {
                mCaching = false;
                setFrame(frame, false, mDrawableItems.size() > 1
                        || !mAnimationState.mOneShot);
            }
        }
    };

    public void initialize(Context context, List<DrawableItem> drawableItems, Configuration configuration) {
        if (configuration == null) {
            // default configuration
            configuration = new Configuration();
        }
        if (mImageFetcher == null) {
            mImageFetcher = new ImageFetcher(context.getResources(), mFetcherCallback);
        }
        // todo: set visible
        mAnimationState.mVariablePadding = configuration.mVariablePadding;
        mAnimationState.mOneShot = configuration.mOneShot;
        mDrawableItems = drawableItems;
        updateDensity(context.getResources());
        setFrame(0, true, false);
    }

    public void inflate(Context context) {
        // todo: inflate xml
    }

    @Override
    @NonNull
    public Drawable mutate() {
        if (!mMutated && super.mutate() == this) {
            mAnimationState.mutate();
            mMutated = true;
        }
        return this;
    }

    @Override
    AnimationState cloneConstantState() {
        return new AnimationState(mAnimationState, this, null);
    }

    /**
     * @hide
     */
    public void clearMutated() {
        super.clearMutated();
        mMutated = false;
    }

    private final static class AnimationState extends DrawableContainerState {
        private int[] mDurations;
        private boolean mOneShot = false;

        AnimationState(AnimationState orig, SmartAnimation owner, Resources res) {
            super(orig, owner, res);

            if (orig != null) {
                mDurations = orig.mDurations;
                mOneShot = orig.mOneShot;
            } else {
                mDurations = new int[getCapacity()];
                mOneShot = false;
            }
        }

        private void mutate() {
            mDurations = mDurations.clone();
        }

        @Override
        public Drawable newDrawable() {
            return new SmartAnimation(this, null);
        }

        @Override
        public Drawable newDrawable(Resources res) {
            return new SmartAnimation(this, res);
        }

        public void addFrame(Drawable dr, int dur) {
            // Do not combine the following. The array index must be evaluated before
            // the array is accessed because super.addChild(dr) has a side effect on mDurations.
            int pos = super.addChild(dr);
            mDurations[pos] = dur;
        }

        public void setFrame(Drawable dr, int dur, int index) {
            super.setChild(dr, index);
            mDurations[index] = dur;
        }

        public void removeFrame(int index) {
            super.removeChild(index);
            mDurations[index] = 0;
        }

        @Override
        public void growArray(int oldSize, int newSize) {
            super.growArray(oldSize, newSize);
            int[] newDurations = new int[newSize];
            System.arraycopy(mDurations, 0, newDurations, 0, oldSize);
            mDurations = newDurations;
        }
    }

    @Override
    protected void setConstantState(@NonNull DrawableContainerState state) {
        super.setConstantState(state);

        if (state instanceof AnimationState) {
            mAnimationState = (AnimationState) state;
        }
    }

    private SmartAnimation(AnimationState state, Resources res) {
        final AnimationState as = new AnimationState(state, this, res);
        setConstantState(as);
        if (state != null) {
            setFrame(0, true, false);
        }
    }

    public static class Configuration {
        public boolean mVisible = true;
        public boolean mVariablePadding = false;
        private boolean mOneShot = false;

        public Configuration setVisible(boolean visible) {
            this.mVisible = visible;
            return this;
        }

        public Configuration setVariablePadding(boolean variablePadding) {
            this.mVariablePadding = variablePadding;
            return this;
        }

        public Configuration setOneShot(boolean oneShot) {
            this.mOneShot = oneShot;
            return this;
        }
    }
    public static class TimeItem {
        int frame;
        int time;

        @Override
        public String toString() {
            return "frame = " + frame
                    + ", time = " + time;
        }
    }
}
