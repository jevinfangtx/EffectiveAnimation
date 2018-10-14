package com.tencent.effectiveanimation.core;

import android.annotation.TargetApi;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.util.Log;

import com.tencent.effectiveanimation.BuildConfig;

import java.util.ArrayList;
import java.util.List;

public class ImageFetcher {

    public static final String TAG = ImageFetcher.class.getSimpleName();
    public static final int DEFAULT_LOOP_NUM = 2;

    private final Object mCacheLock = new Object();
    private ImageCache mImageCache;
    private Resources mResources;
    private Callback mCallback;
    private boolean mLooping;
    private TaskItem[] mTaskItems;
    private int mNumChildren;
    private int mCurFrame;

    public ImageFetcher(Resources resources, Callback callback) {
        mResources = resources;
        mCallback = callback;
        mTaskItems = new TaskItem[10];
        mNumChildren = 0;
        ImageCache.ImageCacheParams params = new ImageCache.ImageCacheParams();
        params.setReusableNum(DEFAULT_LOOP_NUM + 1);
        mImageCache = new ImageCache(params);
    }

    public void addCacheList(int[] drawables, int number, boolean restart) {
        synchronized (mCacheLock) {
            int total = number * 2;
            int startFrom = -1;
            boolean newRestart;
            TaskItem item;
            for (int i = 0; i < total && i < drawables.length; i = i + 2) {
                if (drawables[i] >= mTaskItems.length) {
                    growTaskItems();
                }
                newRestart = restart && i == 0;
                item = mTaskItems[drawables[i]];
                if (getImageCache(drawables[i + 1]) == null) {
                    addCache(drawables[i], drawables[i + 1], newRestart);
                    if (startFrom <= 0) {
                        startFrom = drawables[i];
                    }
                } else {
                    item.restart = restart;
                }
            }
            if (!mLooping) {
                mLooping = true;
                runCache(startFrom);
            }
        }
    }

    private TaskItem addCache(int frame, int drawable, boolean restart) {
        TaskItem item = mTaskItems[frame];
        if (item == null) {
            item = new TaskItem();
            item.resource = drawable;
            item.restart = restart;
            mTaskItems[frame] = item;
        } else {
            if (restart) {
                item.restart = restart;
            }
            if (item.resource <= 0) {
                item.resource = drawable;
            }
        }
        if ((frame + 1) > mNumChildren) {
            mNumChildren = frame + 1;
        }
        return item;
    }
    
    public void clearCache() {
        if (mImageCache != null) {
            mImageCache.clearCache();
        }
    }

    private void runCache(int frame) {
        Log.e("datata", "frame = " + frame);
        TaskItem item = mTaskItems[frame];
        if (item == null) {
            return;
        }
        mCurFrame = frame;
        if (mImageCache.getBitmap(item.resource) != null) {
            if (item.restart) {
                mCallback.callback(frame);
            }
            nextCache(frame);
        } else {
            final BitmapWorkerTask task = new BitmapWorkerTask(item);
            task.executeOnExecutor(AsyncTask.DUAL_THREAD_EXECUTOR, item.resource);
        }
    }

    private void nextCache(int frame) {
        synchronized (mCacheLock) {
            TaskItem item = mTaskItems[frame];
            if (item != null) {
                item.resource = 0;
            }
            int nextFrame = frame + 1;
            if (nextFrame >= mNumChildren) {
                nextFrame = 0;
            }
            TaskItem nextItem = mTaskItems[nextFrame];
            if (nextItem != null && nextItem.resource > 0) {
                runCache(nextFrame);
            } else {
                mLooping = false;
            }
        }
    }

    private void growTaskItems() {
        int oldSize = mTaskItems.length;
        int newSize = oldSize + 10;
        TaskItem[] taskItems = new TaskItem[newSize];
        System.arraycopy(mTaskItems, 0, taskItems, 0, oldSize);
        mTaskItems = taskItems;
    }

    public BitmapDrawable getImageCache(final Integer data) {
        return mImageCache.getBitmap(data);
    }

    public boolean removeImageCache(int frame, final Integer data) {

        boolean result = mImageCache.removeBitmap(data);
//        Log.e("datata", "remove = " + result + ", frame = " + frame);
        return result;
//        return mImageCache.removeBitmap(data);
    }

    public int getImageCacheSize() {
        return mImageCache.getCacheSize();
    }

    /**
     * The actual AsyncTask that will asynchronously process the image.
     */
    private class BitmapWorkerTask extends AsyncTask<Object, Void, BitmapDrawable> {
        private TaskItem mItem;

        public BitmapWorkerTask(TaskItem item) {
            mItem = item;
        }
        /**
         * Background processing.
         */
        @Override
        protected BitmapDrawable doInBackground(Object... params) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "doInBackground - starting work");
            }

            final Integer dataKey = (Integer) params[0];
            Bitmap bitmap = decodeSampledBitmapFromResource(mResources, dataKey, mImageCache);
            BitmapDrawable drawable = null;

            // If the bitmap was processed and the image cache is available, then add the processed
            // bitmap to the cache for future use. Note we don't check if the task was cancelled
            // here, if it was, and the thread is still running, we may as well add the processed
            // bitmap to our cache as it might be used again in the future
            if (bitmap != null) {
                // Running on Honeycomb or newer, so wrap in a standard BitmapDrawable
                drawable = new BitmapDrawable(mResources, bitmap);
                if (mImageCache != null) {
                    mImageCache.addBitmap(dataKey, drawable);
                }
            }

            if (BuildConfig.DEBUG) {
                Log.d(TAG, "doInBackground - finished work");
            }

            return drawable;
        }

        /**
         * Once the image is processed, associates it to the imageView
         */
        @Override
        protected void onPostExecute(BitmapDrawable value) {
            if (mCallback != null && mItem.restart) {
                mCallback.callback(mCurFrame);
            }
            nextCache(mCurFrame);
        }
    }

    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId, ImageCache cache) {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = 1;

        // If we're running on Honeycomb or newer, try to use inBitmap
        if (Utils.hasHoneycomb()) {
            addInBitmapOptions(options, cache);
        }
        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeResource(res, resId, options);
        return bitmap;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private static void addInBitmapOptions(BitmapFactory.Options options, ImageCache cache) {
        // inBitmap only works with mutable bitmaps so force the decoder to
        // return mutable bitmaps.
        options.inMutable = true;
        boolean find = false;
        if (cache != null) {
            // Try and find a bitmap to use for inBitmap
            Bitmap inBitmap = cache.getBitmapFromReusableSet(options);

            if (inBitmap != null) {
                options.inBitmap = inBitmap;
                find = true;
            }
        }
    }

    private static class TaskItem {
        int resource;
        boolean restart;
        public String toString() {
            return "TaskItem " + ", resource = " + resource + ", restart = " + restart;
        }
    }

    public interface Callback {
        void callback(int frame);
    }
}
