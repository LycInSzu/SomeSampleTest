
package com.wtk.screenshot.util;

import java.io.InputStream;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.wtk.screenshot.ScreenShotEntrance;
import com.android.systemui.R;
import com.android.systemui.screenshot.GlobalScreenshot;

import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;

import android.os.Handler;

public class ShotUtil {
    /* Common */
    // Default
    public static final String TAG = "WTK_FUNNY_SCREEN_SHOT";
    public static final String SCREENSHOT_START_ACTION = "com.wtk.screenshot.start2";
    public static final String SHARE_PRE_TITILE = "wtk_screen_shot";
    public static final String SHARE_PRE_SHOT_MODE = "wtk_shot_mode";
    public static final int FULL_SCREEN_MODE = 0;
    public static final int LOCAL_SCREEN_MODE = 1;
    public static final int PAINT_SCREEN_MODE = 2;
    public static final int FREE_SCREEN_MODE = 3;
    public static final int LONG_SCREEN_MODE = 4;
    public static final boolean IS_TEST = false;
    public static final int LOCK_STATE_NONE = 0;
    public static final int LOCK_STATE_ALL = 1;
    public static final int LOCK_STATE_ONLY_CANCEL = 2;

    // Util
    private Context mContext;
    private static ShotUtil instance = null;
    private SharedPreferences sharedPreferences;
    private GlobalScreenshot mScreenshot;

    private Runnable finishRunnable;

    private Handler mHandler = new Handler();

    // Flag
    private Bitmap fullScreenBitmap;
    private ScreenShotEntrance screenShotEntrance;
    private boolean isScreenShotShow;
    private int lockState = LOCK_STATE_NONE;

    public synchronized static ShotUtil getInstance(Context context) {
        if (context == null) {
            return instance;
        }
        if (instance == null) {
            instance = new ShotUtil(context);
        }
        return instance;
    }

    private void clearInstance() {
        if (screenShotEntrance != null) {
            screenShotEntrance.clear();
            screenShotEntrance = null;
        }
        /*
        if (null != fullScreenBitmap && !fullScreenBitmap.isRecycled()) {
            fullScreenBitmap.recycle();
            fullScreenBitmap = null;
        }
        */
        finishRunnable = null;
        //instance = null;
    }

    public synchronized void clear() {
        Log.i(TAG, "ShotUtil;clear");
        clearInstance();
        setLockState(LOCK_STATE_NONE);
    }

    public synchronized void cancel() {
        Log.i(TAG, "ShotUtil;cancel;lockState=" + lockState);
        if (lockState != LOCK_STATE_NONE && lockState != LOCK_STATE_ONLY_CANCEL) {
            return;
        }
        if (screenShotEntrance != null && isScreenShotShow) {
            setLockState(LOCK_STATE_ALL);
            screenShotEntrance.cancel();
            runFinishRunnable();
        }
    }

    private synchronized void runFinishRunnable() {
        Log.i(TAG, "ShotUtil;runFinishRunnable;finishRunnable=" + (finishRunnable != null));
        if (finishRunnable != null) {
            finishRunnable.run();
        } else {
            clear();
        }
    }

    private ShotUtil(Context context) {
        mContext = context;
        sharedPreferences = mContext.getSharedPreferences(SHARE_PRE_TITILE, 0);
    }

    public Bitmap takeScreenshot() {
        try {
            //fullScreenBitmap = BitmapFactory.decodeResource(
            //        mContext.getResources(), R.drawable.test);
            if (mScreenshot == null) {
                return null;
            }
            if (null != fullScreenBitmap && !fullScreenBitmap.isRecycled()) {
                fullScreenBitmap.recycle();
                fullScreenBitmap = null;
            }
            fullScreenBitmap = mScreenshot.takeWtkScreenshot(finishRunnable, true, true);
            if (fullScreenBitmap == null || fullScreenBitmap.isRecycled()) {
                fullScreenBitmap = null;
            }
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fullScreenBitmap;
    }

    public void setFinishRunnable(Runnable finishRun) {
        finishRunnable = finishRun;
    }

    public void setGlobalScreenshot(GlobalScreenshot shot) {
        mScreenshot = shot;
    }

    /*Save*/
    public void saveBitMap(Bitmap bitmap) {
        if (bitmap != null) {
            Log.i(TAG, "ShotUtil;saveBitMap;bitmap=" + !bitmap.isRecycled());
        } else {
            Log.i(TAG, "ShotUtil;saveBitMap_2;bitmap=null");
        }
        setLockState(LOCK_STATE_ALL);
        if (screenShotEntrance != null && isScreenShotShow) {
            screenShotEntrance.cancel();
        }
        if (bitmap == null || bitmap.isRecycled() || mScreenshot == null) {
            runFinishRunnable();
            return;
        }
        mScreenshot.saveBitMap(bitmap, finishRunnable);
    }

    public Bitmap getFullScreenBitmap() {
        if (fullScreenBitmap == null || fullScreenBitmap.isRecycled()) {
            return null;
        }
        return fullScreenBitmap;
    }

    /* Set/Get ScreenShotEntrance */
    public void setScreenShotEntrance(ScreenShotEntrance entrance) {
        screenShotEntrance = entrance;
    }

    public ScreenShotEntrance getScreenShotEntrance() {
        return screenShotEntrance;
    }

    /* Set/Get isScreenShotShow */
    public void setScreenShotShowState(boolean state) {
        isScreenShotShow = state;
    }

    public boolean getScreenShotShowState() {
        return isScreenShotShow;
    }

    public int getCurMode() {
        return sharedPreferences.getInt(SHARE_PRE_SHOT_MODE, 0);
    }

    public void setCurMode(int mode) {
        if (mode < 0 || mode > 4) {
            return;
        }
        Log.i(TAG, "ShotUtil;setCurMode,mode=" + mode);
        sharedPreferences.edit().putInt(SHARE_PRE_SHOT_MODE, mode).commit();
    }

    public static Bitmap readBitMap(Context context, int resId) {
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inPreferredConfig = Bitmap.Config.RGB_565;
        opt.inPurgeable = true;
        opt.inInputShareable = true;
        InputStream is = context.getResources().openRawResource(resId);
        return BitmapFactory.decodeStream(is, null, opt);
    }

    public static Bitmap compQualityBitmap(Bitmap image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        int options = 100;
        while (baos.toByteArray().length / 1024 > 100) {
            baos.reset();
            options -= 10;
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);

        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);
        return bitmap;
    }

    public static Bitmap compSizeBitmap(Bitmap image, float width, float height) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        int options = 100;
        while (baos.toByteArray().length / 1024 > 512) {
            baos.reset();
            options -= 10;
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);

        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        float ww = width;
        float hh = height;
        int be = 1;
        if (w > h && w > ww) {
            be = (int) (newOpts.outWidth / ww);
        } else if (w < h && h > hh) {
            be = (int) (newOpts.outHeight / hh);
        }
        if (be <= 0)
            be = 1;
        newOpts.inSampleSize = be;
        newOpts.inPreferredConfig = Bitmap.Config.RGB_565;

        isBm = new ByteArrayInputStream(baos.toByteArray());
        bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
        return compQualityBitmap(bitmap);
    }

    public void setLockState(int state) {
        lockState = state;
    }

    public int getLockState() {
        return lockState;
    }

}
