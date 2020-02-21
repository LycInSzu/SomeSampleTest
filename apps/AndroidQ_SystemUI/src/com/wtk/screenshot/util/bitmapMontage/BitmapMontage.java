package com.wtk.screenshot.util.bitmapMontage;

import java.util.ArrayList;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wtk.screenshot.util.ShotUtil;
import com.android.systemui.R;

import android.view.ViewGroup;
import android.graphics.PixelFormat;

import com.wtk.screenshot.view.longScreen.LongSetDialog;
import com.wtk.screenshot.util.SharePref;

import java.io.ByteArrayOutputStream;

public class BitmapMontage {
    /* Common */
    // Default
    private static final String TAG = ShotUtil.TAG;
    private static int DEFAULT_TOLERANCE_VALUE = 50;

    // Util
    private Context mContext;
    private BitmapMontageInterface mInterface;
    private Dialog mProgressDialog;
    private ProgressAsyncTask mAsyncTask;
    private ShotUtil mShotUtil;
    private SharePref mSharePref;

    // Flag
    private ArrayList<Bitmap> bitmaps = new ArrayList<Bitmap>();
    private Bitmap mMontageBitmap;
    private int statusBarHeight;
    private int titleBarHeight;
    private int subBarHeight;
    private int fullHeight = 100;

    private boolean toleranceAuto = false;
    private int toleranceValue = 0;
    private boolean upperStateAuto = false;
    private int upperStateValue = 0;
    private boolean downStateAuto = false;
    private int downStateValue = 0;
    // add BUG_ID:TQQB-127 liuzhijun 20181018 (start)
    private int mDefUpDownValue = 0;
    // add BUG_ID:TQQB-127 liuzhijun 20181018 (end)

    // View
    private TextView tipTextView;

    public BitmapMontage(Context context, BitmapMontageInterface mInterface) {
        super();
        this.mContext = context;
        this.mInterface = mInterface;
        mShotUtil = ShotUtil.getInstance(mContext);
        mSharePref = SharePref.getInstance(mContext, LongSetDialog.SHARE_TITLE);

        DEFAULT_TOLERANCE_VALUE = mContext.getResources().getInteger(R.integer.fun_screenshot_default_tolerance);
		// add BUG_ID:TQQB-127 liuzhijun 20181018 (start)
		mDefUpDownValue = context.getResources().getInteger(R.integer.def_up_down_value);
		// add BUG_ID:TQQB-127 liuzhijun 20181018 (end)
    }

    public void montageBitmap(ArrayList<Bitmap> maps) {
        if (maps == null || maps.size() == 0) {
            return;
        }

        bitmaps = maps;
        mAsyncTask = new ProgressAsyncTask();
        mAsyncTask.execute();
    }

    public void cancel() {
        if (mProgressDialog != null) {
            mProgressDialog.cancel();
        }

        if (mAsyncTask != null) {
            mAsyncTask.cancel(true);
            mAsyncTask = null;
            mShotUtil.setLockState(ShotUtil.LOCK_STATE_ALL);
        }
    }

    public void clear() {
        if (mMontageBitmap != null && !mMontageBitmap.isRecycled()) {
            mMontageBitmap.recycle();
            mMontageBitmap = null;
        }
    }

    public interface BitmapMontageInterface {
        public void onMontgeBitmap(Bitmap bitmap);
    }

    private Dialog createLoadingDialog(Context context, String msg) {

        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.bitmapmotage_loading, null);
        LinearLayout layout = (LinearLayout) v.findViewById(R.id.dialog_view);

        ImageView spaceshipImage = (ImageView) v.findViewById(R.id.img);
        tipTextView = (TextView) v.findViewById(R.id.tipTextView);

        Animation hyperspaceJumpAnimation = AnimationUtils.loadAnimation(
                context, R.anim.loading_animation);

        spaceshipImage.startAnimation(hyperspaceJumpAnimation);
        tipTextView.setText(msg);

        Dialog loadingDialog = new Dialog(context, R.style.loading_dialog);

        loadingDialog.setCancelable(false);
        loadingDialog.setContentView(layout, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.FILL_PARENT,
                LinearLayout.LayoutParams.FILL_PARENT));
        loadingDialog.getWindow().setWindowAnimations(R.style.loaddialogAnim);
        loadingDialog.getWindow().setType(
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        return loadingDialog;
    }

    private class ProgressAsyncTask extends AsyncTask<Void, Integer, Bitmap> {
        @Override
        protected void onPreExecute() {
            mProgressDialog = createLoadingDialog(mContext, mContext
                    .getResources().getString(R.string.bitmap_montage_loading));
            mProgressDialog.getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            mProgressDialog.getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            mProgressDialog.getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            toleranceAuto = mSharePref.getBoolean(
                    LongSetDialog.SHARE_TOLERANCE_AUTO, true);
            toleranceValue = mSharePref.getInt(
                    LongSetDialog.SHARE_TOLERANCE_VALUES, 0);
			// add BUG_ID:TQQB-127 liuzhijun 20181018 (start)
			upperStateAuto = mSharePref.getBoolean(
					LongSetDialog.SHARE_UPPER_STATUS_AUTO, false);
			upperStateValue = mSharePref.getInt(
					LongSetDialog.SHARE_UPPER_STATUS_VALUES, mDefUpDownValue);
			downStateAuto = mSharePref.getBoolean(
					LongSetDialog.SHARE_DOWN_STATUS_AUTO, false);
			downStateValue = mSharePref.getInt(
					LongSetDialog.SHARE_DOWN_STATUS_VALUES, mDefUpDownValue);
			// add BUG_ID:TQQB-127 liuzhijun 20181018 (end)

            mShotUtil.setLockState(ShotUtil.LOCK_STATE_ONLY_CANCEL);
            mProgressDialog.show();
            Log.i(TAG,
                    "BitmapMontage,onPreExecute;bitmaps.size()="
                            + bitmaps.size());
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            statusBarHeight = getStatusBarHeight();
            Bitmap fullBitmap = mShotUtil.getFullScreenBitmap();
            Bitmap mNewBitmap = null;
            if (fullBitmap != null) {
                fullHeight = fullBitmap.getHeight() - statusBarHeight;
            }
            titleBarHeight = (upperStateAuto ? getMaxTitleBarHeight() : (upperStateValue * fullHeight / 100));
            if (downStateAuto || (downStateValue + titleBarHeight) > fullHeight) {
                subBarHeight = getMaxSubBarHeight();
            } else {
                subBarHeight = downStateValue * fullHeight / 100;
            }

            Log.i(TAG, "BitmapMontage;doInBackground;statusBarHeight="
                    + statusBarHeight + ",titleBarHeight=" + titleBarHeight
                    + ",subBarHeight=" + subBarHeight);

            for (int i = bitmaps.size() - 1; i >= 0; i--) {
                Log.i(TAG, "BitmapMontage;doInBackground;montageID=" + i);
                publishProgress(bitmaps.size() - i);
                Bitmap bitmap = montageSingleBitmap(mNewBitmap, bitmaps.get(i));
                if (bitmap == null) {
                    break;
                }
                mNewBitmap = bitmap;
            }
            return mNewBitmap;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            String page = String.format(
                    mContext.getResources().getString(
                            R.string.bitmap_montage_loading_detail), values[0] + "");
            if (tipTextView != null) {
                tipTextView.setText(page);
            }
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            mProgressDialog.cancel();
            mAsyncTask = null;
            mShotUtil.setLockState(ShotUtil.LOCK_STATE_ALL);
            mMontageBitmap = result;
            if (mInterface != null) {
                mInterface.onMontgeBitmap(mMontageBitmap);
            }
        }
    }

    private synchronized Bitmap montageSingleBitmap(Bitmap newBitmap, Bitmap bitmap_1) {
        if (bitmap_1 == null) {
            return null;
        }
        if (newBitmap == null) {
            newBitmap = bitmap_1;
            return newBitmap;
        }

        int width = bitmap_1.getWidth();
        int width_2 = newBitmap.getWidth();
        if (width != width_2) {
            Log.e(TAG, "BitmapMontage;newBitmap;width_1 != width__2");
            return null;
        }
        int height_1 = bitmap_1.getHeight();
        int height_2 = newBitmap.getHeight();

        int[] pixel_1 = new int[width * height_1];
        bitmap_1.getPixels(pixel_1, 0, width, 0, 0, width, height_1);

        int[] pixel_2 = new int[width * height_2];
        newBitmap.getPixels(pixel_2, 0, width, 0, 0, width, height_2);

        // transparent statusBar and titleBar
        for (int y = 0; y < statusBarHeight + titleBarHeight; y++) {
            for (int x = 0; x < width; x++) {
                pixel_2[y * width + x] = 0x00ffffff;
            }
        }

        Bitmap transBitmap = null;
        try {
            transBitmap = Bitmap.createBitmap(pixel_2, 0, width, width,
				height_2, Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (transBitmap == null) {
            return null;
        }

        // Compare translate height
        int stepX = width / 100;
        int offsetY = 0;
        boolean isBreak = false;
        for (int i = 0; i <= (height_1 - statusBarHeight - titleBarHeight - subBarHeight); i++) {
            isBreak = false;
            loop:
            for (int y = (statusBarHeight + titleBarHeight + i); y < (height_1 - subBarHeight); y = y + 1) {
                for (int x = width * 1 / 5; x < width * 4 / 5; x = x + stepX) {
                    if (Math.abs(pixel_1[y * width + x]
                            - pixel_2[(y - i) * width + x]) > (toleranceAuto ? (DEFAULT_TOLERANCE_VALUE * LongSetDialog.TOLERANCE_MAX_VALUE / 100.0)
                            : (toleranceValue * LongSetDialog.TOLERANCE_MAX_VALUE / 100.0)) * 1000000) {
                        isBreak = true;
                        break loop;
                    }
                }
            }
            if (!isBreak) {
                offsetY = i;
                break;
            }
        }
        Log.i(TAG, "BitmapMontage;newBitmap;offsetY=" + offsetY + ",width=" + width + ",height_1=" + height_1 + ",height_2=" + height_2
                + ",stepX=" + stepX);

        // montage bitmap
        int newHeight = height_2 + offsetY;
        Bitmap monBitmap = null;
        try {
            monBitmap = Bitmap.createBitmap(width, newHeight,
                    Bitmap.Config.RGB_565);
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (monBitmap == null) {
            return null;
        }
        newBitmap.recycle();
        newBitmap = null;
        newBitmap = monBitmap;
        Canvas canvas = new Canvas(newBitmap);
        canvas.drawBitmap(bitmap_1, 0, 0, null);
        canvas.drawBitmap(transBitmap, 0, offsetY, null);
        transBitmap.recycle();
        transBitmap = null;
        //System.gc();
        return newBitmap;
    }

    public byte[] BitmapToBytes(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    private int getStatusBarHeight() {
        int result = 0;
        int resourceId = mContext.getResources().getIdentifier(
                "status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = mContext.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    private int getMaxTitleBarHeight() {
        int barHeight;
        if (bitmaps.size() < 2) {
            return 0;
        }
        int[] heights = new int[bitmaps.size() - 1];
        for (int i = 0; i < bitmaps.size() - 1; i++) {
            heights[i] = getItemTitleBarHeight(bitmaps.get(0),
                    bitmaps.get(i + 1));
        }
        barHeight = heights[0];
        for (int i = 1; i < heights.length; i++) {
            if (barHeight < heights[i]) {
                barHeight = heights[i];
            }
        }
        return barHeight;
    }

    private int getItemTitleBarHeight(Bitmap bp1, Bitmap bp2) {
        int itemTitleHeight = 0;

        int width = bp1.getWidth();
        int width_2 = bp2.getWidth();
        int height = bp1.getHeight();
        int height_2 = bp2.getHeight();
        if (width != width_2) {
            Log.e(TAG,
                    "BitmapMontage;getItemTitleBarHeight;width_1 != width__2");
            return 0;
        }
        if (height != height_2) {
            Log.e(TAG,
                    "BitmapMontage;getItemTitleBarHeight;height_1 != height_2");
            return 0;
        }

        int[] pixel_1 = new int[width * height];
        bp1.getPixels(pixel_1, 0, width, 0, 0, width, height);

        int[] pixel_2 = new int[width * height];
        bp2.getPixels(pixel_2, 0, width, 0, 0, width, height);

        loop:
        for (int y = statusBarHeight; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (pixel_1[y * width + x] != pixel_2[y * width + x]) {
                    itemTitleHeight = y - statusBarHeight;
                    break loop;
                }
            }

        }
        return itemTitleHeight;
    }

    private int getMaxSubBarHeight() {
        int barHeight;
        if (bitmaps.size() < 2) {
            return 0;
        }
        int[] heights = new int[bitmaps.size() - 1];
        for (int i = 0; i < bitmaps.size() - 1; i++) {
            heights[i] = getItemSubBarHeight(bitmaps.get(0),
                    bitmaps.get(i + 1));
        }
        barHeight = heights[0];
        for (int i = 1; i < heights.length; i++) {
            if (barHeight < heights[i]) {
                barHeight = heights[i];
            }
        }
        return barHeight;
    }

    private int getItemSubBarHeight(Bitmap bp1, Bitmap bp2) {
        int itemTitleHeight = 0;

        int width = bp1.getWidth();
        int width_2 = bp2.getWidth();
        int height = bp1.getHeight();
        int height_2 = bp2.getHeight();
        if (width != width_2) {
            Log.e(TAG,
                    "BitmapMontage;getItemSubBarHeight;width_1 != width__2");
            return 0;
        }
        if (height != height_2) {
            Log.e(TAG,
                    "BitmapMontage;getItemSubBarHeight;height_1 != height_2");
            return 0;
        }

        int[] pixel_1 = new int[width * height];
        bp1.getPixels(pixel_1, 0, width, 0, 0, width, height);

        int[] pixel_2 = new int[width * height];
        bp2.getPixels(pixel_2, 0, width, 0, 0, width, height);

        loop:
        for (int y = height - 1; y >= statusBarHeight; y--) {
            for (int x = 0; x < width; x++) {
                if (pixel_1[y * width + x] != pixel_2[y * width + x]) {
                    itemTitleHeight = height - y - 1;
                    break loop;
                }
            }

        }
        return itemTitleHeight;
    }

}
