
package com.wtk.screenshot.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.wtk.screenshot.util.ShotUtil;
import com.wtk.screenshot.view.freeScreen.FreeScreen;
import com.wtk.screenshot.view.fullScreen.FullScreen;
import com.wtk.screenshot.view.localScreen.LocalScreen;
import com.wtk.screenshot.view.longScreen.LongScreen;
import com.wtk.screenshot.view.paintScreen.PaintScreen;
import com.android.systemui.R;

import android.view.animation.Animation.AnimationListener;
import android.util.Log;
//add for TEWSL-399 by liyuchong 20200109 begin
import android.os.SystemProperties;
import android.content.pm.PackageManager;
import android.os.UserHandle;
import android.content.Intent;
import android.os.Build;
import java.util.List;
import android.content.pm.ResolveInfo;
//add for TEWSL-399 by liyuchong 20200109 end

public class ScreenShotView extends FrameLayout implements View.OnClickListener {
    /* Common */
    // Default
    private static final String TAG = ShotUtil.TAG;

    //add for TEWSL-399 by liyuchong 20200109 begin
    private static final String PKG_LONGSCREENSHOT = "com.cydroid.longscreenshot";
    private static final String SERVICE_LONGSCREENSHOT="com.cydroid.longscreenshot.ScreenShotService";
    //add for TEWSL-399 by liyuchong 20200109 end

    // Util
    private Context mContext;
    private ShotUtil mShotUtil;

    //private Animation screenViewAlphaIn;
    //private Animation screenViewAlphaOut;

    // Flag
    private int screenViewAnimState = 0;

    /* View */
    private LinearLayout statusUp;
    private FrameLayout touchView;
    private ImageView slidingDown;
    private LinearLayout fullScreen;
    private LinearLayout localScreen;
    private LinearLayout paintScreen;
    private LinearLayout freeScreen;
    private LinearLayout longScreen;
    private LinearLayout statusDown;
    private Button shotOk;
    private Button shotCancel;

    private FullScreen fullTouchView;
    private LocalScreen localTouchView;
    private PaintScreen paintTouchView;
    private FreeScreen freeTouchView;
    private LongScreen longTouchView;

    public ScreenShotView(Context context) {
        this(context, null);
    }

    public ScreenShotView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        if (isInEditMode()) {
            return;
        }
    }

    public ScreenShotView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        /* Common */
        mContext = context;
        mShotUtil = ShotUtil.getInstance(mContext);
        View v = getPartView();
        this.addView(v);

        /* View */
        statusUp = (LinearLayout) v.findViewById(R.id.status_up);
        touchView = (FrameLayout) v.findViewById(R.id.touch_view);
        slidingDown = (ImageView) v.findViewById(R.id.sliding_down);
        fullScreen = (LinearLayout) v.findViewById(R.id.full_screen);
        localScreen = (LinearLayout) v.findViewById(R.id.local_screen);
        paintScreen = (LinearLayout) v.findViewById(R.id.paint_screen);
        freeScreen = (LinearLayout) v.findViewById(R.id.free_screen);
        longScreen = (LinearLayout) v.findViewById(R.id.long_screen);
        statusDown = (LinearLayout) v.findViewById(R.id.status_down);
        shotOk = (Button) v.findViewById(R.id.shot_ok);
        shotCancel = (Button) v.findViewById(R.id.shot_cancel);

        //longScreen.setVisibility(View.GONE);

        fullTouchView = (FullScreen) v.findViewById(R.id.full_touch_view);
        localTouchView = (LocalScreen) v.findViewById(R.id.local_touch_view);
        paintTouchView = (PaintScreen) v.findViewById(R.id.paint_touch_view);
        freeTouchView = (FreeScreen) v.findViewById(R.id.free_touch_view);
        longTouchView = (LongScreen) v.findViewById(R.id.long_touch_view);

        touchView.setLongClickable(true);
        touchView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (mShotUtil.getLockState() != ShotUtil.LOCK_STATE_NONE) {
                    return true;
                }
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        statusUPGone();
                        statusDownGone();
                        slidingDown.setVisibility(View.GONE);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        break;
                    case MotionEvent.ACTION_UP:
                        statusDownShow();
                        slidingDown.setVisibility(View.VISIBLE);
                        break;
                }

                switch (mShotUtil.getCurMode()) {
                    case ShotUtil.FULL_SCREEN_MODE:
                        fullTouchView.onMyTouchEvent(event);
                        break;
                    case ShotUtil.LOCAL_SCREEN_MODE:
                        localTouchView.onMyTouchEvent(event);
                        break;
                    case ShotUtil.PAINT_SCREEN_MODE:
                        paintTouchView.onMyTouchEvent(event);
                        break;
                    case ShotUtil.FREE_SCREEN_MODE:
                        freeTouchView.onMyTouchEvent(event);
                        break;
                    case ShotUtil.LONG_SCREEN_MODE:
                        longTouchView.onMyTouchEvent(event);
                        break;
                    default:
                        break;
                }
                return true;
            }
        });
        slidingDown.setOnClickListener(this);
        fullScreen.setOnClickListener(this);
        localScreen.setOnClickListener(this);
        paintScreen.setOnClickListener(this);
        freeScreen.setOnClickListener(this);
        longScreen.setOnClickListener(this);
        shotOk.setOnClickListener(this);
        shotCancel.setOnClickListener(this);
        statusUp.setClickable(true);
        statusDown.setClickable(true);

        // Default
        statusUpShow();
        statusDownShow();
    }

    public View getPartView() {
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return inflater.inflate(R.layout.screenshot_view, null);
    }

    private void statusUpShow() {
        if (statusUp.getVisibility() == View.VISIBLE) {
            return;
        }
        Animation upIn = AnimationUtils.loadAnimation(mContext,
                R.anim.select_up_in);
        statusUp.setAnimation(upIn);
        statusUp.setVisibility(View.VISIBLE);
        slidingDown.setVisibility(View.GONE);
    }

    private void statusUPGone() {
        if (statusUp.getVisibility() == View.GONE) {
            return;
        }
        Animation upOut = AnimationUtils.loadAnimation(mContext,
                R.anim.select_up_out);
        statusUp.setAnimation(upOut);
        statusUp.setVisibility(View.GONE);
    }

    private void statusDownShow() {
        if (statusDown.getVisibility() == View.VISIBLE) {
            return;
        }
        Animation downIn = AnimationUtils.loadAnimation(mContext,
                R.anim.select_down_in);
        statusDown.setAnimation(downIn);
        statusDown.setVisibility(View.VISIBLE);
        //statusDown.setClickable(true);
    }

    private void statusDownGone() {
        if (statusDown.getVisibility() == View.GONE) {
            return;
        }
        Animation downOut = AnimationUtils.loadAnimation(mContext,
                R.anim.select_down_out);
        statusDown.setAnimation(downOut);
        statusDown.setVisibility(View.GONE);
        //statusDown.setClickable(false);
    }

    @Override
    public void onClick(View view) {
        /*
        if (screenViewAnimState >0){
            return;
        }
        */
        int id = view.getId();
        if (id == R.id.shot_cancel) {
            mShotUtil.cancel();
            return;
        }
        if (mShotUtil.getLockState() != ShotUtil.LOCK_STATE_NONE) {
            return;
        }

        if (id == R.id.sliding_down) {
            statusUpShow();
        } else if (id == R.id.shot_ok) {
            saveBitmap();
        } else if (id == R.id.shot_cancel) {
        } else if (id == R.id.full_screen) {
            mShotUtil.setCurMode(ShotUtil.FULL_SCREEN_MODE);
            reset();
        } else if (id == R.id.local_screen) {
            mShotUtil.setCurMode(ShotUtil.LOCAL_SCREEN_MODE);
            reset();
        } else if (id == R.id.paint_screen) {
            mShotUtil.setCurMode(ShotUtil.PAINT_SCREEN_MODE);
            reset();
        } else if (id == R.id.free_screen) {
            mShotUtil.setCurMode(ShotUtil.FREE_SCREEN_MODE);
            reset();
        } else if (id == R.id.long_screen) {
            //modify for TEWSL-399 by liyuchong 20200109 begin
            //mShotUtil.setCurMode(ShotUtil.LONG_SCREEN_MODE);
            //reset();
            if ("yes".equals(SystemProperties.get("ro.odm_support_new_longscreenshot"))){
                mShotUtil.setLockState(ShotUtil.LOCK_STATE_ONLY_CANCEL);
                mShotUtil.cancel();
                launchLongScreenShot();
            }else {
                mShotUtil.setCurMode(ShotUtil.LONG_SCREEN_MODE);
                reset();
            }
            //modify for TEWSL-399 by liyuchong 20200109 end
        }
    }

    private void saveBitmap() {
        Log.i(TAG, "ScreenShotView;saveBitmap");
        mShotUtil.setLockState(ShotUtil.LOCK_STATE_ALL);
        switch (mShotUtil.getCurMode()) {
            case ShotUtil.FULL_SCREEN_MODE:
                fullTouchView.saveBitmap();
                break;
            case ShotUtil.LOCAL_SCREEN_MODE:
                localTouchView.saveBitmap();
                break;
            case ShotUtil.PAINT_SCREEN_MODE:
                paintTouchView.saveBitmap();
                break;
            case ShotUtil.FREE_SCREEN_MODE:
                freeTouchView.saveBitmap();
                break;
            case ShotUtil.LONG_SCREEN_MODE:
                longTouchView.saveBitmap();
                break;
            default:
                break;
        }
    }

    private void resetMode(int mode) {
        Animation screenViewAlphaIn = AnimationUtils
                .loadAnimation(mContext, R.anim.select_alpha_in);
        Animation screenViewAlphaOut = AnimationUtils.loadAnimation(mContext,
                R.anim.select_alpha_out);
        screenViewAlphaIn.setAnimationListener(animationListener);
        screenViewAlphaOut.setAnimationListener(animationListener);

        switch (mode) {
            case ShotUtil.FULL_SCREEN_MODE:
                fullScreen.setSelected(true);
                localScreen.setSelected(false);
                paintScreen.setSelected(false);
                freeScreen.setSelected(false);
                longScreen.setSelected(false);

                if (fullTouchView.getVisibility() != View.VISIBLE) {
                    fullTouchView.setAnimation(screenViewAlphaIn);
                    fullTouchView.setVisibility(View.VISIBLE);
                }
                if (localTouchView.getVisibility() != View.GONE) {
                    localTouchView.setAnimation(screenViewAlphaOut);
                    localTouchView.setVisibility(View.GONE);
                }
                if (paintTouchView.getVisibility() != View.GONE) {
                    paintTouchView.setAnimation(screenViewAlphaOut);
                    paintTouchView.setVisibility(View.GONE);
                }
                if (freeTouchView.getVisibility() != View.GONE) {
                    freeTouchView.setAnimation(screenViewAlphaOut);
                    freeTouchView.setVisibility(View.GONE);
                }
                if (longTouchView.getVisibility() != View.GONE) {
                    longTouchView.setAnimation(screenViewAlphaOut);
                    longTouchView.setVisibility(View.GONE);
                }
                break;
            case ShotUtil.LOCAL_SCREEN_MODE:
                localScreen.setSelected(true);
                fullScreen.setSelected(false);
                paintScreen.setSelected(false);
                freeScreen.setSelected(false);
                longScreen.setSelected(false);

                if (localTouchView.getVisibility() != View.VISIBLE) {
                    localTouchView.setAnimation(screenViewAlphaIn);
                    localTouchView.setVisibility(View.VISIBLE);
                }
                if (fullTouchView.getVisibility() != View.GONE) {
                    fullTouchView.setAnimation(screenViewAlphaOut);
                    fullTouchView.setVisibility(View.GONE);
                }
                if (paintTouchView.getVisibility() != View.GONE) {
                    paintTouchView.setAnimation(screenViewAlphaOut);
                    paintTouchView.setVisibility(View.GONE);
                }
                if (freeTouchView.getVisibility() != View.GONE) {
                    freeTouchView.setAnimation(screenViewAlphaOut);
                    freeTouchView.setVisibility(View.GONE);
                }
                if (longTouchView.getVisibility() != View.GONE) {
                    longTouchView.setAnimation(screenViewAlphaOut);
                    longTouchView.setVisibility(View.GONE);
                }
                break;
            case ShotUtil.PAINT_SCREEN_MODE:
                paintScreen.setSelected(true);
                fullScreen.setSelected(false);
                localScreen.setSelected(false);
                freeScreen.setSelected(false);
                longScreen.setSelected(false);

                if (paintTouchView.getVisibility() != View.VISIBLE) {
                    paintTouchView.setAnimation(screenViewAlphaIn);
                    paintTouchView.setVisibility(View.VISIBLE);
                }
                if (fullTouchView.getVisibility() != View.GONE) {
                    fullTouchView.setAnimation(screenViewAlphaOut);
                    fullTouchView.setVisibility(View.GONE);
                }
                if (localTouchView.getVisibility() != View.GONE) {
                    localTouchView.setAnimation(screenViewAlphaOut);
                    localTouchView.setVisibility(View.GONE);
                }
                if (freeTouchView.getVisibility() != View.GONE) {
                    freeTouchView.setAnimation(screenViewAlphaOut);
                    freeTouchView.setVisibility(View.GONE);
                }
                if (longTouchView.getVisibility() != View.GONE) {
                    longTouchView.setAnimation(screenViewAlphaOut);
                    longTouchView.setVisibility(View.GONE);
                }
                break;
            case ShotUtil.FREE_SCREEN_MODE:
                freeScreen.setSelected(true);
                fullScreen.setSelected(false);
                localScreen.setSelected(false);
                paintScreen.setSelected(false);
                longScreen.setSelected(false);

                if (freeTouchView.getVisibility() != View.VISIBLE) {
                    freeTouchView.setAnimation(screenViewAlphaIn);
                    freeTouchView.setVisibility(View.VISIBLE);
                }
                if (fullTouchView.getVisibility() != View.GONE) {
                    fullTouchView.setAnimation(screenViewAlphaOut);
                    fullTouchView.setVisibility(View.GONE);
                }
                if (localTouchView.getVisibility() != View.GONE) {
                    localTouchView.setAnimation(screenViewAlphaOut);
                    localTouchView.setVisibility(View.GONE);
                }
                if (paintTouchView.getVisibility() != View.GONE) {
                    paintTouchView.setAnimation(screenViewAlphaOut);
                    paintTouchView.setVisibility(View.GONE);
                }
                if (longTouchView.getVisibility() != View.GONE) {
                    longTouchView.setAnimation(screenViewAlphaOut);
                    longTouchView.setVisibility(View.GONE);
                }
                break;
            case ShotUtil.LONG_SCREEN_MODE:
                longScreen.setSelected(true);
                fullScreen.setSelected(false);
                localScreen.setSelected(false);
                paintScreen.setSelected(false);
                freeScreen.setSelected(false);

                if (longTouchView.getVisibility() != View.VISIBLE) {
                    longTouchView.setAnimation(screenViewAlphaIn);
                    longTouchView.setVisibility(View.VISIBLE);
                }
                if (fullTouchView.getVisibility() != View.GONE) {
                    fullTouchView.setAnimation(screenViewAlphaOut);
                    fullTouchView.setVisibility(View.GONE);
                }
                if (localTouchView.getVisibility() != View.GONE) {
                    localTouchView.setAnimation(screenViewAlphaOut);
                    localTouchView.setVisibility(View.GONE);
                }
                if (paintTouchView.getVisibility() != View.GONE) {
                    paintTouchView.setAnimation(screenViewAlphaOut);
                    paintTouchView.setVisibility(View.GONE);
                }
                if (freeTouchView.getVisibility() != View.GONE) {
                    freeTouchView.setAnimation(screenViewAlphaOut);
                    freeTouchView.setVisibility(View.GONE);
                }

                break;
            default:
                break;
        }
    }

    private AnimationListener animationListener = new AnimationListener() {

        @Override
        public void onAnimationStart(Animation animation) {
            //screenViewAnimState++;
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            //screenViewAnimState--;
        }
    };

    @Override
    public void onScreenStateChanged(int screenState) {
        super.onScreenStateChanged(screenState);
        if (screenState == SCREEN_STATE_OFF) {
            mShotUtil.cancel();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            mShotUtil.cancel();
            return true;
        }
        return false;
    }

    public void reset() {
        int mode = mShotUtil.getCurMode();
        switch (mode) {
            case ShotUtil.FULL_SCREEN_MODE:
                fullTouchView.reset();
                break;
            case ShotUtil.LOCAL_SCREEN_MODE:
                localTouchView.reset();
                break;
            case ShotUtil.PAINT_SCREEN_MODE:
                paintTouchView.reset();
                break;
            case ShotUtil.FREE_SCREEN_MODE:
                freeTouchView.reset();
                break;
            case ShotUtil.LONG_SCREEN_MODE:
                longTouchView.reset();
                break;
            default:
                break;
        }

        resetMode(mode);
    }

    public void cancel() {
        fullTouchView.cancel();
        localTouchView.cancel();
        paintTouchView.cancel();
        freeTouchView.cancel();
        longTouchView.cancel();
    }

    public void clear() {
        fullTouchView.clear();
        localTouchView.clear();
        paintTouchView.clear();
        freeTouchView.clear();
        longTouchView.clear();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //add by wangjian for YWSW-817 20190410 start
        //Log.e("wangjian","ScreenShotView onDraw = " + canvas.isHardwareAccelerated() + " / " + canvas.isHwBitmapsInSwModeEnabled());
        canvas.setHwBitmapsInSwModeEnabled(true);
        //add by wangjian for YWSW-817 20190410 end
        super.onDraw(canvas);
    }

    //add for TEWSL-399 by liyuchong 20200109 begin
    protected void launchLongScreenShot(){
        try {
            Intent intent = new Intent();
            intent.setClassName(PKG_LONGSCREENSHOT, SERVICE_LONGSCREENSHOT);
            if(isSafesServiceIntent(intent)){
//			    mContext.startService(intent);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    mContext.startForegroundServiceAsUser(intent,UserHandle.CURRENT);
                } else {
                    mContext.startService(intent);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "CyScreenShotTile-launchLongScreenShot() occur exception! " + e.getMessage());
        }
    }

    private boolean isSafesServiceIntent(Intent mServiceIntent){
        PackageManager packageManager = mContext.getPackageManager();
        List<ResolveInfo> services = packageManager.queryIntentServices(mServiceIntent, 0);
        return services.size() > 0;
    }
    //add for TEWSL-399 by liyuchong 20200109 end
}
