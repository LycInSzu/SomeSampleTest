package com.cydroid.note.app;


import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;
import android.util.DisplayMetrics;
import android.view.WindowManager;

public class ImageCache {

    private static final String TAG = "ImageCache";
    private LruCache<String, Bitmap> mMemoryCache;
    private static int ONE_M = 1024 * 1024;
    private static ImageCache sImageCache;
    private int mMemCacheSize;

    public static ImageCache getInstance() {
        if (null == sImageCache) {
            sImageCache = new ImageCache(NoteAppImpl.getContext());
        }
        return sImageCache;
    }

    private ImageCache(Context context) {
        DisplayMetrics metric = new DisplayMetrics();
        ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay()
                .getMetrics(metric);

        float density = metric.density;
        if (density >= 3) {
            mMemCacheSize = ONE_M * 16;
        } else if (density >= 2 && density < 3) {
            mMemCacheSize = ONE_M * 12;
        } else {
            mMemCacheSize = ONE_M * 8;
        }

        mMemoryCache = new LruCache<String, Bitmap>(mMemCacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                int bitmapSize = getBitmapSize(value);
                return bitmapSize == 0 ? 1 : bitmapSize;
            }

            @Override
            protected void entryRemoved(boolean evicted, String key,
                                        Bitmap oldValue, Bitmap newValue) {
                super.entryRemoved(evicted, key, oldValue, newValue);
            }
        };
    }

    private int getBitmapSize(Bitmap bitmap) {
        return bitmap.getRowBytes() * bitmap.getHeight();
    }

    public synchronized void addBitmapToCache(String data, Bitmap bitmap) {
        if (data == null || bitmap == null) {
            return;
        }

        if (mMemoryCache.get(data) == null) {
            mMemoryCache.put(data, bitmap);
        }
    }

    public synchronized Bitmap getBitmapFromMemCache(String data) {
        return mMemoryCache.get(data);
    }

    public int getMemCacheSize() {
        return mMemCacheSize;
    }

    void clearCache() {
        if (mMemoryCache != null) {
            mMemoryCache.evictAll();
        }
    }

}
