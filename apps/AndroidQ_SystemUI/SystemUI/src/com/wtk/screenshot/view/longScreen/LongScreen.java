
package com.wtk.screenshot.view.longScreen;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.wtk.screenshot.util.ShotUtil;
import com.android.systemui.R;

import android.os.Handler;

import com.wtk.screenshot.util.shell.ShellExe;

import java.io.IOException;

import android.view.WindowManager;

import java.util.ArrayList;

import com.android.systemui.screenshot.GlobalScreenshot;

import android.widget.Toast;

import com.wtk.screenshot.util.bitmapMontage.BitmapMontage;
import com.wtk.screenshot.util.bitmapMontage.BitmapMontage.BitmapMontageInterface;

import android.content.res.Configuration;
import android.app.KeyguardManager;
import android.provider.Settings;
//import android.database.ContentObserver;

public class LongScreen extends FrameLayout implements View.OnClickListener,
        BitmapMontageInterface {
    /* Common */
    // Default
    private static final String TAG = ShotUtil.TAG;
    private static final int LONG_RIGHT_UP = 1;
    private static final int LONG_RIGHT_DOWN = 2;
    private static final int MAX_BITMAP_SIZE = 10;
    private static final String WTK_LIST_OBSERVER = "wtk_list_observer";
    // Util
    private Context mContext;
    private ShotUtil mShotUtil;
    private Handler mHandler = new Handler();
    private ArrayList<Bitmap> bitmaps = new ArrayList<Bitmap>();
    //private GlobalScreenshot mScreenshot;
    private BitmapMontage mBitmapMontage;
    private WindowManager wm;
    private KeyguardManager mKeyguardManager;

    // Flag
    private int screenWidth;
    private int screenHeight;
    private String[] cmdx = new String[3];
    private int curMode;
    private Bitmap curBitmap;

    /* View */
    private FrameLayout instruction;
    private LinearLayout longRight;
    private ImageView longRightUp;
    private ImageView longRightDown;
    private ImageView test;
    private ImageView longSet;

    public LongScreen(Context context) {
        this(context, null);
    }

    public LongScreen(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        if (isInEditMode()) {
            return;
        }
    }

    public LongScreen(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        mShotUtil = ShotUtil.getInstance(mContext);

        View v = getPartView();
        this.addView(v);

        instruction = (FrameLayout) v.findViewById(R.id.instruction);
        longRight = (LinearLayout) v.findViewById(R.id.long_right);
        longRightUp = (ImageView) v.findViewById(R.id.long_rightup);
        longRightDown = (ImageView) v.findViewById(R.id.long_rightdown);
        test = (ImageView) v.findViewById(R.id.test);
        longSet = (ImageView) v.findViewById(R.id.long_set);

        longRight.setClickable(true);
        longRightUp.setOnClickListener(this);
        longRightDown.setOnClickListener(this);
        longSet.setOnClickListener(this);

        wm = (WindowManager) mContext
                .getSystemService(Context.WINDOW_SERVICE);
        screenWidth = wm.getDefaultDisplay().getWidth();
        screenHeight = wm.getDefaultDisplay().getHeight();

        mKeyguardManager = (KeyguardManager) mContext.getSystemService(Context.KEYGUARD_SERVICE);

        //mScreenshot = new GlobalScreenshot(mContext);
        mBitmapMontage = new BitmapMontage(mContext, this);

        // Default
        //modify by wangjian for YWSW-858 20190413 start
        //instruction.setVisibility(View.VISIBLE);
        instruction.setVisibility(View.GONE);
        //modify by wangjian for YWSW-858 20190413 end
        longRightShow();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }


    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (visibility == View.GONE) {
        } else if (visibility == View.VISIBLE) {
        } else if (visibility == View.INVISIBLE) {
        }
    }


    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    public View getPartView() {
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return inflater.inflate(R.layout.long_screen, null);
    }

    private void longRightShow() {
        if (longRight.getVisibility() == View.VISIBLE) {
            return;
        }
        Animation rightIn = AnimationUtils.loadAnimation(mContext,
                R.anim.select_right_in);
        longRight.setAnimation(rightIn);
        longRight.setVisibility(View.VISIBLE);
    }

    private void longRightGone() {
        if (longRight.getVisibility() == View.GONE) {
            return;
        }
        Animation rightOut = AnimationUtils.loadAnimation(mContext,
                R.anim.select_right_out);
        longRight.setAnimation(rightOut);
        longRight.setVisibility(View.GONE);
    }

    private void longSetShow() {
        if (longSet.getVisibility() == View.VISIBLE) {
            return;
        }
        Animation rightIn = AnimationUtils.loadAnimation(mContext,
                R.anim.select_right_in);
        longSet.setAnimation(rightIn);
        longSet.setVisibility(View.VISIBLE);
    }

    private void longSetGone() {
        if (longSet.getVisibility() == View.GONE) {
            return;
        }
        Animation rightOut = AnimationUtils.loadAnimation(mContext,
                R.anim.select_right_out);
        longSet.setAnimation(rightOut);
        longSet.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View v) {
        if (mShotUtil.getLockState() != ShotUtil.LOCK_STATE_NONE) {
            return;
        }
        int id = v.getId();
        if (id == R.id.long_rightup) {
            instruction.setVisibility(View.GONE);
            longRightUpOrDown(LONG_RIGHT_UP);
        } else if (id == R.id.long_rightdown) {
            instruction.setVisibility(View.GONE);
            longRightUpOrDown(LONG_RIGHT_DOWN);
        } else if (id == R.id.long_set) {
            new com.wtk.screenshot.view.longScreen.LongSetDialog(mContext).showDialog();
        }
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        screenWidth = wm.getDefaultDisplay().getWidth();
        screenHeight = wm.getDefaultDisplay().getHeight();
        Log.i(TAG, "LongScreen;onConfigurationChanged;screenWidth="
                + screenWidth + ",screenHeight=" + screenHeight);
    }

    private void longRightUpOrDown(int mode) {
        if (mKeyguardManager.inKeyguardRestrictedInputMode()) {
            return;
        }
        curMode = mode;
        if (mShotUtil.getScreenShotEntrance() == null || mShotUtil.getScreenShotEntrance().getShotView() == null) {
            return;
        }
        mShotUtil.getScreenShotEntrance().getShotView()
                .setVisibility(View.GONE);
        cmdx[0] = "/system/bin/sh";
        cmdx[1] = "-c";
        switch (mode) {
            case LONG_RIGHT_UP:
                cmdx[2] = "input swipe " + screenWidth / 2 + " " + screenHeight / 4
                        + " " + screenWidth / 2 + " " + screenHeight * 3 / 4;
                break;
            case LONG_RIGHT_DOWN:
                cmdx[2] = "input swipe " + screenWidth / 2 + " " + screenHeight * 3 / 4
                        + " " + screenWidth / 2 + " " + screenHeight / 4;
                break;
            default:
                break;
        }

        mHandler.post(moveRunnable);
        /*
        if ((bitmaps.size() >1 && bitmaps.size() < MAX_BITMAP_SIZE)
                ||(bitmaps.size() == 1 && curMode == LONG_RIGHT_DOWN)
                ||(bitmaps.size() == MAX_BITMAP_SIZE && curMode == LONG_RIGHT_UP)){
            Settings.System.putInt(mContext.getContentResolver(), WTK_LIST_OBSERVER,mode);
        }
        mHandler.postDelayed(viewShowRunnable, 500);
        */
    }

    private Runnable moveRunnable = new Runnable() {

        @Override
        public void run() {
            try {
                if ((bitmaps.size() > 1 && bitmaps.size() < MAX_BITMAP_SIZE)
                        || (bitmaps.size() == 1 && curMode == LONG_RIGHT_DOWN)
                        || (bitmaps.size() == MAX_BITMAP_SIZE && curMode == LONG_RIGHT_UP)) {
                    int result = ShellExe.execCommand(cmdx);
                    Log.i(TAG, "LongScreen;moveUpRunnable;result=" + result);
                }
                mHandler.postDelayed(viewShowRunnable, 500);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    private Runnable viewShowRunnable = new Runnable() {

        @Override
        public void run() {
            //Settings.System.putInt(mContext.getContentResolver(), WTK_LIST_OBSERVER,0);
            switch (curMode) {
                case LONG_RIGHT_UP:
                    if (bitmaps.size() > 1) {
                        bitmaps.remove(bitmaps.size() - 1);
                    } else {
                        Toast.makeText(mContext, R.string.long_screen_on_top, Toast.LENGTH_SHORT)
                                .show();
                    }
                    break;
                case LONG_RIGHT_DOWN:
                    if (bitmaps.size() < MAX_BITMAP_SIZE) {
                    /*
                    curBitmap = mScreenshot.takeWtkScreenshot(new Runnable() {
                                    @Override 
                        public void run() {
                                    }
                                    }, true, true);
                                */
                        Bitmap bitmap = mShotUtil.takeScreenshot();
                        if (bitmap != null) {
                            curBitmap = bitmap.copy(Bitmap.Config.RGB_565, true);
                            bitmaps.add(curBitmap);
                        }
                    } else {
                        String maxSize = String.format(
                                mContext.getResources().getString(R.string.long_screen_bitmap_full),
                                MAX_BITMAP_SIZE + "");
                        Toast.makeText(mContext, maxSize, Toast.LENGTH_SHORT)
                                .show();
                    }
                    break;
                default:
                    break;
            }
            if (mShotUtil.getScreenShotEntrance() != null && mShotUtil.getScreenShotEntrance().getShotView() != null) {
                mShotUtil.getScreenShotEntrance().getShotView()
                        .setVisibility(View.VISIBLE);
            }
        }
    };

    public void reset() {
        test.setVisibility(View.GONE);
        bitmaps.clear();
        Bitmap bitmap = mShotUtil.getFullScreenBitmap().copy(Bitmap.Config.RGB_565, true);
        if (bitmap != null) {
            Bitmap newBitmap = bitmap.copy(bitmap.getConfig(), true);
            bitmaps.add(newBitmap);
        }
    }

    public void cancel() {
        mBitmapMontage.cancel();
    }

    public void clear() {
        mBitmapMontage.clear();
        for (Bitmap bitmap : bitmaps) {
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
                bitmap = null;
            }
        }
    }

    private void test(Bitmap bitmap) {
        test.setVisibility(View.VISIBLE);
        test.setImageBitmap(bitmap);

    }

    public void saveBitmap() {
        mBitmapMontage.montageBitmap(bitmaps);
    }

    @Override
    public void onMontgeBitmap(Bitmap bitmap) {
        if (ShotUtil.IS_TEST) {
            test(bitmap);
        } else {
            mShotUtil.saveBitMap(bitmap);
        }
    }

    public boolean onMyTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                instruction.setVisibility(View.GONE);
                longRightGone();
                longSetGone();
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                longRightShow();
                longSetShow();
                break;
        }
        return false;
    }
}
