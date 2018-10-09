package com.tencent.effectiveanimation.core;

import android.annotation.TargetApi;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;

import com.tencent.effectiveanimation.BuildConfig;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ImageFetcher {

    public static final String TAG = ImageFetcher.class.getSimpleName();
    public static final int DEFAULT_LOOP_NUM = 2;

    private final Object mCacheLock = new Object();
    private ImageCache mImageCache;
    private Resources mResources;
    private List<TaskItem> mTaskItems;
    private Callback mCallback;
    private boolean mLooping;
    private long start;

    public ImageFetcher(Resources resources, Callback callback) {
        mResources = resources;
        mCallback = callback;
        mTaskItems = new ArrayList<TaskItem>();
        ImageCache.ImageCacheParams params = new ImageCache.ImageCacheParams();
        params.setReusableNum(DEFAULT_LOOP_NUM + 1);
        mImageCache = new ImageCache(params);
    }

    /**
     * 增加缓存任务
     * @param drawables 任务信息
     * @param restart 第一个任务是否需要恢复动画队列
     */
    public void addCache(LinkedHashMap<Integer, Integer> drawables, boolean restart) {
        synchronized (mCacheLock) {
            Iterator<Map.Entry<Integer, Integer>> iterator = drawables.entrySet().iterator();
            boolean find;
            boolean newRestart;
            TaskItem item;
            int index = 0;
            while (iterator.hasNext()) {
                Map.Entry<Integer, Integer> entry = iterator.next();
                newRestart = restart && index == 0;
                find = false;
                for (int i = 0; i < mTaskItems.size(); i++) {
                    item = mTaskItems.get(i);
                    if (item.frame == entry.getKey()) {
                        if (newRestart) {
                            item.restart = newRestart;
                        }
                        find = true;
                        break;
                    }
                }
                if (!find) {
                    TaskItem newItem = new TaskItem();
                    newItem.frame = entry.getKey();
                    newItem.resource = entry.getValue();
                    newItem.restart = newRestart;
                    mTaskItems.add(newItem);
                }
                index++;
            }
            if (!mLooping) {
                mLooping = true;
                runCache(mTaskItems.get(0));
            }
        }
    }

    private void runCache(TaskItem item) {
        if (mImageCache.getBitmap(item.resource) != null) {
            if (item.restart) {
                mCallback.callback(item.frame);
            }
            nextCache(item.frame);
        } else {
            start = SystemClock.uptimeMillis();
            final BitmapWorkerTask task = new BitmapWorkerTask(item);
            task.executeOnExecutor(AsyncTask.DUAL_THREAD_EXECUTOR, item.resource);
        }
    }

    private void nextCache(int frame) {
        synchronized (mCacheLock) {
            int index = -1;
            for (int i = 0; i < mTaskItems.size(); i++) {
                TaskItem item = mTaskItems.get(i);
                if (item.frame == frame) {
                    index = i;
                    break;
                }
            }
            TaskItem item;
            if (index < 0) {
                item = mTaskItems.size() > 0 ? mTaskItems.get(0) : null;
            } else {
                int nextIndex = index + 1;
                item = nextIndex < mTaskItems.size() ? mTaskItems.get(nextIndex) : null;
            }
            removeCache(frame);
            if (item != null) {
                runCache(item);
            } else {
                mLooping = false;
            }
        }
    }

    private void removeCache(int frame) {
        synchronized (mCacheLock) {
            Iterator<TaskItem> iterator = mTaskItems.iterator();
            while (iterator.hasNext()) {
                TaskItem item = iterator.next();
                if (item.frame == frame) {
                    iterator.remove();
                }
            }
        }
    }

    public void clearCache() {
        if (mImageCache != null) {
            mImageCache.clearCache();
        }
    }

    public BitmapDrawable getImageCache(final Integer data) {
        return mImageCache.getBitmap(data);
    }

    public boolean removeImageCache(final Integer data) {
        return mImageCache.removeBitmap(data);
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
                mCallback.callback(mItem.frame);
            }
//            Log.e("datata", "frame = " + mItem.frame + ", time = " + (SystemClock.uptimeMillis() - start));
            nextCache(mItem.frame);
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
        Log.e("datata", "find = " + find);

    }

    private static class TaskItem {
        int frame;
        int resource;
        boolean restart;
        public String toString() {
            return "TaskItem frame = " + frame + ", resource = " + resource + ", restart = " + restart;
        }
    }

    public interface Callback {
        void callback(int frame);
    }
}
