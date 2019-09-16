package com.cydroid.ota.ui.widget;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.LruCache;
import android.view.View;

import com.cydroid.ota.Log;
import com.cydroid.ota.SettingUpdateApplication;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import com.cydroid.ota.R;
//Chenyee <CY_REQ> <xuyongji> <20171222> modify for SW17W16A-2705 begin
import android.os.Build;
//Chenyee <CY_REQ> <xuyongji> <20171222> modify for SW17W16A-2705 end
/**
 * @author borney
 *         Created by borney on 6/5/15.
 */
public class AnimationBgDrawable extends Drawable implements Runnable, ITheme {
    private static final String TAG = "AnimationBgDrawable";
    private static final int REFRESH_TIME = 16;
    private Context mContext;
    private int mColor;
    private int mSimilarColor;
    private View mTarget;
    private boolean isStart = false;
    private AnimationState mAnimationState;
    private Paint mBgPaint;
    private Paint mColorPaint;
    private Shader mShader;
    private static LruCache<String, Bitmap> MEMORY_CACHE;
    private static int mCacheSize = 0;
    private static Set<Integer> LOADING_BITMAP;

    AnimationBgDrawable(final View view, Context context) {
        mTarget = view;
        mContext = context;
        Resources res = context.getResources();
        mBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        mBgPaint.setAlpha(150);
        mColorPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        mColor = ((SettingUpdateApplication) context.getApplicationContext()).getSystemTheme().getChameleon().AccentColor_G1;
        mSimilarColor = SimilarColor.getSimilarColor(mColor);

        int[] durations = res.getIntArray(R.array.gn_su_main_bganim_durations);
        mAnimationState = new AnimationState(durations);
		//Chenyee <CY_Bug> <xuyongji> <20180210> modify for CSW1705A-1489 begin
		if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O) {
	        mAnimationState.addFrame(R.drawable.gn_su_anim_bg_diwen0);
	        mAnimationState.addFrame(R.drawable.gn_su_anim_bg_diwen1);
	        mAnimationState.addFrame(R.drawable.gn_su_anim_bg_diwen2);
	        mAnimationState.addFrame(R.drawable.gn_su_anim_bg_diwen3);
		}
		//Chenyee <CY_Bug> <xuyongji> <20180210> modify for CSW1705A-1489 end
    }

    static{

        LOADING_BITMAP = new HashSet<>();

        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        // Use 1/8th of the available memory for this memory cache.
        final int cacheSize = maxMemory / 8;

        MEMORY_CACHE = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return bitmap.getByteCount() / 1024;
            }

            @Override
            protected void entryRemoved(boolean evicted, String key, Bitmap oldValue, Bitmap newValue) {
                recycleBitmap(oldValue);
            }
        };
    }
    public void start() {
        isStart = true;
        mTarget.post(this);
    }

    @Override
    public void onChameleonChanged(Chameleon chameleon) {
        mColor = ((SettingUpdateApplication) mContext.getApplicationContext()).getSystemTheme().getChameleon().AppbarColor_A1;
        mSimilarColor = SimilarColor.getSimilarColor(mColor);
        mShader = null;
        mTarget.invalidate();
    }

    @Override
    public void draw(Canvas canvas) {
        if (!isStart) {
            return;
        }
        if (mShader == null) {
            mShader = new LinearGradient(0, 0, 0, canvas.getHeight(), new int[]{
                    mColor, mSimilarColor}, null,
                    Shader.TileMode.CLAMP);
            mColorPaint.setShader(mShader);
        }
        canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), mColorPaint);
		//Chenyee <CY_REQ> <xuyongji> <20171222> modify for SW17W16A-2705 begin
		if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O) {
	        Bitmap bgBitmap = loadBitmap(R.drawable.gn_su_anim_bg_diwen);
	        if (bgBitmap != null) {
	            canvas.drawBitmap(bgBitmap, 0, 0, mBgPaint);
	        }
	        mAnimationState.draw(canvas);
		}
		//Chenyee <CY_REQ> <xuyongji> <20171222> modify for SW17W16A-2705 end
    }

    @Override
    public void setAlpha(int alpha) {

    }

    @Override
    public void setColorFilter(ColorFilter cf) {

    }

    @Override
    public int getOpacity() {
        return 0;
    }

    public void onDestory() {
        Log.d(TAG, "onDestory");
        Iterator<Map.Entry<String, Bitmap>> iterator = MEMORY_CACHE.snapshot().entrySet().iterator();
        try {
            while (iterator.hasNext()) {
                Bitmap bitmap = iterator.next().getValue();
                recycleBitmap(bitmap);
                iterator.remove();
            }
        } catch (NoSuchElementException e) {
        }
        mCacheSize = 0;
    }

    @Override
    public void run() {
        long now = System.currentTimeMillis();
        if (mAnimationState.getStartOffSet() == 0) {
            mAnimationState.setStartOffSet(now);
            mTarget.removeCallbacks(this);
            mTarget.postDelayed(this, REFRESH_TIME);
            return;
        }
        float intput = mAnimationState.update(now);
        if (intput < 0) {
            mAnimationState.setStartOffSet(now);
            mTarget.removeCallbacks(this);
            mTarget.postDelayed(this, REFRESH_TIME);
            return;
        }
        mTarget.invalidate();
        mTarget.removeCallbacks(this);
        mTarget.postDelayed(this, REFRESH_TIME);
    }

    private static void recycleBitmap(Bitmap bitmap) {
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
        }
    }

    private Bitmap loadBitmap(int resId) {
        final String imageKey = String.valueOf(resId);

        final Bitmap bitmap = getBitmapFromMemCache(imageKey);
        if (bitmap != null && !bitmap.isRecycled()) {
            return bitmap;
        } else {
            if (!LOADING_BITMAP.contains(resId)) {
                BitmapWorkerTask task = new BitmapWorkerTask(mContext, resId);
                task.execute(resId);
            }
        }
        return null;
    }

    private static void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            synchronized (MEMORY_CACHE) {
                int bit = bitmap.getWidth() * bitmap.getHeight() * getBytesPerPixel(bitmap.getConfig()) / 1024;
                mCacheSize += bit;
                MEMORY_CACHE.resize(Math.max(mCacheSize, MEMORY_CACHE.maxSize()));
                MEMORY_CACHE.put(key, bitmap);
            }
        }
    }

    private static Bitmap getBitmapFromMemCache(String key) {
        synchronized (MEMORY_CACHE) {
            return MEMORY_CACHE.get(key);
        }
    }

    private static int getBytesPerPixel(Config config) {
        if (config == Config.ARGB_8888) {
            return 4;
        } else if (config == Config.RGB_565) {
            return 2;
        } else if (config == Config.ARGB_4444) {
            return 2;
        } else if (config == Config.ALPHA_8) {
            return 1;
        }
        return 1;
    }

    private final class AnimationState {
        int[] bitmaps;
        Paint[] paints;
        int[] durations;
        int dur_index = 0;
        int length = 0;
        long startOffSet = 0;

        public AnimationState(int[] durations) {
            this.durations = durations;
        }

        void addFrame(int id) {
            if (bitmaps == null) {
                growArray(length, length + 10);
            }
            bitmaps[length] = id;
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
            paint.setAlpha(0);
            paints[length] = paint;
            length++;
        }

        void setStartOffSet(long startOffSet) {
            this.startOffSet = startOffSet;
            dur_index = 0;
        }

        long getStartOffSet() {
            return this.startOffSet;
        }

        float update(long now) {
            float normalizedTime;
            float diff = (float) (now - startOffSet);
            long sum = 0;
            for (int i = 0; i <= dur_index; i++) {
                sum += durations[i];
            }
            if (diff > sum) {
                dur_index++;
            }
            if (dur_index >= durations.length) {
                dur_index = 0;
                return -1.0f;
            }
            sum = 0;
            for (int i = 0; i <= dur_index - 1; i++) {
                sum += durations[i];
            }
            normalizedTime = (diff - (dur_index == 1 ? 0 : sum)) / (float) durations[dur_index];
            float input = normalizedTime;
            for (int i = 0; i < length; i++) {
                int diff_index = dur_index - i;
                if (diff_index == 2) {
                    paints[i].setAlpha((int) (input * 255));
                } else if (diff_index == 4) {
                    paints[i].setAlpha((int) ((1 - input) * 255));
                } else if (diff_index == 3) {
                    paints[i].setAlpha(255);
                } else {
                    paints[i].setAlpha(0);
                }
            }
            return input;
        }

        void draw(Canvas canvas) {
            for (int i = 0; i < length; i++) {
                Bitmap bitmap = loadBitmap(bitmaps[i]);
                Paint paint = paints[i];
                if (bitmap != null && paint.getAlpha() != 0) {
                    canvas.drawBitmap(bitmap, 0, 0, paint);
                }
            }
        }

        private void growArray(int oldSize, int newSize) {
            int[] newBitmaps = new int[newSize];
            if (bitmaps != null) {
                System.arraycopy(bitmaps, 0, newBitmaps, 0, oldSize);
            }
            bitmaps = newBitmaps;

            Paint[] newPaints = new Paint[newSize];
            if (paints != null) {
                System.arraycopy(paints, 0, newPaints, 0, oldSize);
            }
            paints = newPaints;
        }
    }

    private static class BitmapWorkerTask extends AsyncTask<Integer, Void, Bundle> {
        private WeakReference<Context> weakContext;
        private int resId;

        BitmapWorkerTask(Context context, int resId) {
            weakContext = new WeakReference<Context>(context);
            this.resId = resId;
        }

        @Override
        protected void onPreExecute() {
            LOADING_BITMAP.add(resId);
        }

        @Override
        protected Bundle doInBackground(Integer... params) {
            Context context = weakContext.get();
            if (context == null) {
                return null;
            }
            Resources res = context.getResources();
            final String imageKey = String.valueOf(params[0]);
            DisplayMetrics displayMetrics = res.getDisplayMetrics();
            final Bitmap bitmap = decodeSampledBitmapFromResource(
                    res, params[0], displayMetrics.widthPixels, displayMetrics.heightPixels);
            Bundle bundle = new Bundle();
            bundle.putString("bitmapKey", imageKey);
            bundle.putParcelable("bitmap", bitmap);
            return bundle;
        }

        @Override
        protected void onPostExecute(Bundle bundle) {
            LOADING_BITMAP.remove(resId);
            addBitmapToMemoryCache(bundle.getString("bitmapKey"), (Bitmap) bundle.getParcelable("bitmap"));
            // mTarget.invalidate();
        }

        private Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
                                                       int reqWidth, int reqHeight) {

            // First decode with inJustDecodeBounds=true to check dimensions
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeResource(res, resId, options);

            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            return BitmapFactory.decodeResource(res, resId, options);
        }

        private int calculateInSampleSize(
                BitmapFactory.Options options, int reqWidth, int reqHeight) {
            // Raw height and width of image
            final int height = options.outHeight;
            final int width = options.outWidth;
            int inSampleSize = 1;

            if (height > reqHeight || width > reqWidth) {

                final int halfHeight = height / 2;
                final int halfWidth = width / 2;

                // Calculate the largest inSampleSize value that is a power of 2 and keeps both
                // height and width larger than the requested height and width.
                while ((halfHeight / inSampleSize) > reqHeight
                        && (halfWidth / inSampleSize) > reqWidth) {
                    inSampleSize *= 2;
                }
            }

            return inSampleSize;
        }
    }
}
