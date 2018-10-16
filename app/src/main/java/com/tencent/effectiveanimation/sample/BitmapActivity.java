package com.tencent.effectiveanimation.sample;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class BitmapActivity extends AppCompatActivity {

    private DrawableProvider mProvider;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mProvider = new DrawableProvider();
        int total = 0, compress = 0;
        for (int i = 1; i < mProvider.mTouXiang.size(); i++) {
            int[] pixels1 = getBitmapPixel(mProvider.mTouXiang.get(i - 1).resource);
            int[] pixels2 = getBitmapPixel(mProvider.mTouXiang.get(i).resource);
            int count1 = countNonZeroPixel(pixels1);
            int count2 = countNonZeroPixel(pixels2);

            int[] diffPixels = getDiffPixels(pixels1, pixels2);
            int diffCount = countNonZeroPixel(diffPixels);
            total += diffPixels.length;
            compress += diffCount;
            Log.e("datata", "length = " + diffPixels.length
                    + ", count1 = " + count1
                    + ", count2 = " + count2
                    + ", diffCount = " + diffCount);
        }

        float compressRatio = 1.0f * compress / total;
        Log.e("datata", "total = " + total + ", compress = " + compress + ", ratio = " + compressRatio);
    }

    private int[] getBitmapPixel(int drawable) {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), drawable);
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int length = width * height;
        int[] pixels = new int[length];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        return pixels;
    }

    private int countNonZeroPixel(int[] pixels) {
        int count = 0;
        for (int i = 0; i < pixels.length; i++) {
            if (pixels[i] != 0) {
                count++;
            }
        }
        return count;
    }
    private int[] getDiffPixels(int [] pixels1, int[] pixels2) {
        int length = Math.min(pixels1.length, pixels2.length);
        int[] pixels = new int[length];
        for (int i = 0; i < length; i++) {
            pixels[i] = pixels1[i] - pixels2[i];
            if (pixels[i] != 0) {
//                Log.e("datata", "i = " + i + ", pix = " + pixels[i]);
            }
        }
        return pixels;
    }
}
