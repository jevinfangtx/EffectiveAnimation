package com.tencent.effectiveanimation.core;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.util.Log;

import com.tencent.effectiveanimation.R;

import java.util.ArrayList;
import java.util.List;

public class SmartAnimation1 extends DrawableContainer implements Runnable, Animatable {
    private AnimationState mAnimationState;

    /** The current frame, ranging from 0 to {@link #mAnimationState#getChildCount() - 1} */
    private int mCurFrame = 0;

    /** Whether the drawable has an animation callback posted. */
    private boolean mRunning;

    /** Whether the drawable should animate when visible. */
    private boolean mAnimating;

    private boolean mMutated;

//    private static final List<List<Integer>> mAnimRes = new ArrayList<List<Integer>>();
//    {
//        mAnimRes.add(create(R.drawable.c_flash_081, 66));
//        mAnimRes.add(create(R.drawable.c_flash_082, 33));
//        mAnimRes.add(create(R.drawable.c_flash_083, 33));
//        mAnimRes.add(create(R.drawable.c_flash_084, 33));
//        mAnimRes.add(create(R.drawable.c_flash_085, 33));
//        mAnimRes.add(create(R.drawable.c_flash_086, 33));
//        mAnimRes.add(create(R.drawable.c_flash_087, 33));
//        mAnimRes.add(create(R.drawable.c_flash_088, 33));
//        mAnimRes.add(create(R.drawable.c_flash_089, 33));
//        mAnimRes.add(create(R.drawable.c_flash_090, 33));
//        mAnimRes.add(create(R.drawable.c_flash_091, 33));
//        mAnimRes.add(create(R.drawable.c_flash_092, 33));
//        mAnimRes.add(create(R.drawable.c_flash_093, 33));
//        mAnimRes.add(create(R.drawable.c_flash_094, 33));
//        mAnimRes.add(create(R.drawable.c_flash_095, 33));
//        mAnimRes.add(create(R.drawable.c_flash_096, 33));
//        mAnimRes.add(create(R.drawable.c_flash_097, 33));
//        mAnimRes.add(create(R.drawable.c_flash_098, 33));
//        mAnimRes.add(create(R.drawable.c_flash_099, 33));
//        mAnimRes.add(create(R.drawable.c_flash_100, 33));
//        mAnimRes.add(create(R.drawable.c_flash_101, 33));
//        mAnimRes.add(create(R.drawable.c_flash_102, 33));
//        mAnimRes.add(create(R.drawable.c_flash_103, 33));
//        mAnimRes.add(create(R.drawable.c_flash_104, 33));
//        mAnimRes.add(create(R.drawable.c_flash_105, 33));
//        mAnimRes.add(create(R.drawable.c_flash_106, 33));
//        mAnimRes.add(create(R.drawable.c_flash_107, 33));
//        mAnimRes.add(create(R.drawable.c_flash_108, 33));
//        mAnimRes.add(create(R.drawable.c_flash_109, 33));
//        mAnimRes.add(create(R.drawable.c_flash_081, 528));
//    }

    private static final List<List<Integer>> mAnimRes = new ArrayList<List<Integer>>();
    {
        mAnimRes.add(create(R.drawable.c_anim_065, 81));
        mAnimRes.add(create(R.drawable.c_anim_066, 27));
        mAnimRes.add(create(R.drawable.c_anim_067, 27));
        mAnimRes.add(create(R.drawable.c_anim_068, 27));
        mAnimRes.add(create(R.drawable.c_anim_069, 27));

        mAnimRes.add(create(R.drawable.c_anim_070, 27));
        mAnimRes.add(create(R.drawable.c_anim_071, 27));
        mAnimRes.add(create(R.drawable.c_anim_072, 27));
        mAnimRes.add(create(R.drawable.c_anim_073, 27));
        mAnimRes.add(create(R.drawable.c_anim_074, 27));
        mAnimRes.add(create(R.drawable.c_anim_075, 27));
        mAnimRes.add(create(R.drawable.c_anim_076, 27));
        mAnimRes.add(create(R.drawable.c_anim_077, 27));
        mAnimRes.add(create(R.drawable.c_anim_078, 27));
        mAnimRes.add(create(R.drawable.c_anim_079, 27));

        mAnimRes.add(create(R.drawable.c_anim_080, 27));
        mAnimRes.add(create(R.drawable.c_anim_081, 27));
        mAnimRes.add(create(R.drawable.c_anim_082, 27));
        mAnimRes.add(create(R.drawable.c_anim_083, 27));
        mAnimRes.add(create(R.drawable.c_anim_084, 27));
        mAnimRes.add(create(R.drawable.c_anim_085, 27));
        mAnimRes.add(create(R.drawable.c_anim_086, 27));
        mAnimRes.add(create(R.drawable.c_anim_087, 27));
        mAnimRes.add(create(R.drawable.c_anim_088, 27));
        mAnimRes.add(create(R.drawable.c_anim_089, 27));

        mAnimRes.add(create(R.drawable.c_anim_090, 27));
        mAnimRes.add(create(R.drawable.c_anim_091, 27));
        mAnimRes.add(create(R.drawable.c_anim_092, 27));
        mAnimRes.add(create(R.drawable.c_anim_093, 27));
        mAnimRes.add(create(R.drawable.c_anim_094, 27));
        mAnimRes.add(create(R.drawable.c_anim_095, 27));
        mAnimRes.add(create(R.drawable.c_anim_096, 27));
        mAnimRes.add(create(R.drawable.c_anim_097, 27));
        mAnimRes.add(create(R.drawable.c_anim_098, 27));
        mAnimRes.add(create(R.drawable.c_anim_099, 27));

        mAnimRes.add(create(R.drawable.c_anim_100, 27));
        mAnimRes.add(create(R.drawable.c_anim_101, 27));
        mAnimRes.add(create(R.drawable.c_anim_102, 27));
        mAnimRes.add(create(R.drawable.c_anim_103, 27));
        mAnimRes.add(create(R.drawable.c_anim_104, 27));
        mAnimRes.add(create(R.drawable.c_anim_105, 27));
        mAnimRes.add(create(R.drawable.c_anim_106, 27));
        mAnimRes.add(create(R.drawable.c_anim_107, 27));
        mAnimRes.add(create(R.drawable.c_anim_108, 27));
        mAnimRes.add(create(R.drawable.c_anim_109, 27));

        mAnimRes.add(create(R.drawable.c_anim_110, 27));
        mAnimRes.add(create(R.drawable.c_anim_111, 27));
        mAnimRes.add(create(R.drawable.c_anim_112, 27));
        mAnimRes.add(create(R.drawable.c_anim_113, 27));
        mAnimRes.add(create(R.drawable.c_anim_114, 27));
        mAnimRes.add(create(R.drawable.c_anim_115, 27));
        mAnimRes.add(create(R.drawable.c_anim_116, 27));
        mAnimRes.add(create(R.drawable.c_anim_117, 27));
        mAnimRes.add(create(R.drawable.c_anim_118, 27));

        mAnimRes.add(create(R.drawable.c_anim_065, 81));
    }

    private static List<Integer> create(int resId, int duration) {
        List<Integer> set = new ArrayList<>();
        set.add(resId);
        set.add(duration);
        return set;
    }

    public SmartAnimation1() {
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
            // Start from 0th frame.
            setFrame(0, false, mAnimationState.getChildCount() > 1
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
        int nextFrame = mCurFrame + 1;
        final int numFrames = mAnimationState.getChildCount();
        final boolean isLastFrame = mAnimationState.mOneShot && nextFrame >= (numFrames - 1);

        // Loop if necessary. One-shot animations should never hit this case.
        if (!mAnimationState.mOneShot && nextFrame >= numFrames) {
            nextFrame = 0;
        }

        setFrame(nextFrame, unschedule, !isLastFrame);
    }

    private void setFrame(int frame, boolean unschedule, boolean animate) {
        if (frame >= mAnimationState.getChildCount()) {
            return;
        }
        mAnimating = animate;
        mCurFrame = frame;
        selectDrawable(frame);
        if (unschedule || animate) {
            unscheduleSelf(this);
        }
        if (animate) {
            // Unscheduling may have clobbered these values; restore them
            mCurFrame = frame;
            mRunning = true;
            scheduleSelf(this, SystemClock.uptimeMillis() + mAnimationState.mDurations[frame]);
        }
    }

    public void inflate(Resources r, Context context) {
        mAnimationState.mOneShot = false;
        updateDensity(r);
        inflateChildElements(context);
        setFrame(0, true, false);
    }

    private void inflateChildElements(Context context) {
        long start = SystemClock.uptimeMillis();
        Resources resources = context.getResources();
        for (int i = 0; i < mAnimRes.size(); i++) {
            List<Integer> res = mAnimRes.get(i);
            Bitmap bitmap = BitmapFactory.decodeResource(resources, res.get(0));
            BitmapDrawable dr = new BitmapDrawable(resources, bitmap);
//            Drawable dr = ContextCompat.getDrawable(context, res.get(0));
            mAnimationState.addFrame(dr, res.get(1));
            if (dr != null) {
                dr.setCallback(this);
            }
        }
        long total = SystemClock.uptimeMillis() - start;
        Log.e("datata", "inflate time = " + total);
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

        AnimationState(AnimationState orig, SmartAnimation1 owner, Resources res) {
            super(orig, owner, res);

            if (orig != null) {
                mDurations = orig.mDurations;
                mOneShot = orig.mOneShot;
            } else {
                mDurations = new int[mAnimRes.size()];
                mOneShot = false;
            }
        }

        private void mutate() {
            mDurations = mDurations.clone();
        }

        @Override
        public Drawable newDrawable() {
            return new SmartAnimation1(this, null);
        }

        @Override
        public Drawable newDrawable(Resources res) {
            return new SmartAnimation1(this, res);
        }

        public void addFrame(Drawable dr, int dur) {
            // Do not combine the following. The array index must be evaluated before
            // the array is accessed because super.addChild(dr) has a side effect on mDurations.
            int pos = super.addChild(dr);
            mDurations[pos] = dur;
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

    private SmartAnimation1(AnimationState state, Resources res) {
        final AnimationState as = new AnimationState(state, this, res);
        setConstantState(as);
        if (state != null) {
            setFrame(0, true, false);
        }
    }
}
