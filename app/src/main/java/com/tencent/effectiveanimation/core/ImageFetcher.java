package com.tencent.effectiveanimation.core;

import android.annotation.TargetApi;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;

import com.tencent.effectiveanimation.BuildConfig;

import java.util.List;

public class ImageFetcher {

    public static final String TAG = ImageFetcher.class.getSimpleName();

    private ImageCache mImageCache;
    private Resources mResources;

    public ImageFetcher(Resources resources) {
        mResources = resources;
        ImageCache.ImageCacheParams params = new ImageCache.ImageCacheParams();
        mImageCache = new ImageCache(params);
    }

    public void initDrawable(List<Integer> imageRes) {
        if (imageRes == null || imageRes.size() == 0) {
            return;
        }

        for (int i = 0; i < 2 && i < imageRes.size(); i++) {
            Integer data = imageRes.get(i);
            addImageCache(data);
        }
    }

    public void addImageCache(final Integer data) {
        final BitmapWorkerTask task = new BitmapWorkerTask();
        task.executeOnExecutor(AsyncTask.DUAL_THREAD_EXECUTOR, data);
    }

    public BitmapDrawable getImageCache(final Integer data) {
        return mImageCache.getBitmapFromMemCache(data);
    }

    public int getCacheSize() {
        return mImageCache.getCacheSize();
    }

    /**
     * The actual AsyncTask that will asynchronously process the image.
     */
    private class BitmapWorkerTask extends AsyncTask<Object, Void, BitmapDrawable> {
        private Object data;
        /**
         * Background processing.
         */
        @Override
        protected BitmapDrawable doInBackground(Object... params) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "doInBackground - starting work");
            }

            data = params[0];
            final Integer dataKey = (Integer) data;
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
                    mImageCache.addBitmapToCache(dataKey, drawable);
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

        }
    }

    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId, ImageCache cache) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();

        // If we're running on Honeycomb or newer, try to use inBitmap
        if (Utils.hasHoneycomb()) {
            addInBitmapOptions(options, cache);
        }
        Bitmap bitmap = BitmapFactory.decodeResource(res, resId, options);
        return bitmap;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private static void addInBitmapOptions(BitmapFactory.Options options, ImageCache cache) {
        // inBitmap only works with mutable bitmaps so force the decoder to
        // return mutable bitmaps.
        options.inMutable = true;

        if (cache != null) {
            // Try and find a bitmap to use for inBitmap
            Bitmap inBitmap = cache.getBitmapFromReusableSet(options);

            if (inBitmap != null) {
                options.inBitmap = inBitmap;
            }
        }
    }
}
