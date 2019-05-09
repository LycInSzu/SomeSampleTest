package com.pri.factorytest.LCD;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.pri.factorytest.PrizeBaseActivity;
import com.pri.factorytest.R;
import com.pri.factorytest.util.Utils;

import java.io.InputStream;
import java.util.stream.Stream;

public class LCD extends PrizeBaseActivity {

    private Handler mHandler;
    private int imgSeq = 0;
    private WindowManager.LayoutParams mLayoutParams;
    private PowerManager.WakeLock mWakeLock;
    private PowerManager mPowerManager;
    private LinearLayout mLinearLayout;
    private long currentTimeMils = 0;
    private final static int DELAY_SHOW = 3000;

    private static final int[] mTestImg = {R.drawable.lcm_red, R.drawable.lcm_green,
            R.drawable.lcm_blue, R.drawable.lcm_black,
            R.drawable.lcm_black_white_lump, R.drawable.lcm_girl_01,
            R.drawable.lcm_girl_02/*, R.drawable.lcm_girl_08*/};

    private static final int[] mOLEDImg = {R.drawable.lcm_red, R.drawable.lcm_green,
            R.drawable.lcm_blue, R.drawable.lcm_black, R.drawable.gray_32, R.drawable.gray_48,
            R.drawable.gray_64, R.drawable.chessboard_1, R.drawable.gray_127, R.drawable.object_4,
            R.drawable.person_4, R.drawable.lcm_girl_01, R.drawable.lcm_girl_02};

    private int[] mImgArr = Stream.of("k6309").anyMatch(x -> Build.DISPLAY.toLowerCase().startsWith(x)) ? mOLEDImg : mTestImg;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setBottomUIMenuVisibility(false);
        setContentView(R.layout.lcd);
        mLayoutParams = getWindow().getAttributes();
        mLayoutParams.screenBrightness = 1;
        mLayoutParams.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        getWindow().setAttributes(mLayoutParams);

        mLinearLayout = (LinearLayout) findViewById(R.id.myLinearLayout1);
        mPowerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = mPowerManager.newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "BackLight");
        initHandler();
        //showFirstPic();
    }

    private void setBottomUIMenuVisibility(boolean show) {
        View decorView = getWindow().getDecorView();
        int uiHideOption = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        int uiShowOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(show ? uiShowOptions : uiHideOption);
    }

    private void showFirstPic() {
        Message message = new Message();
        message.what = imgSeq = 0;
        mHandler.sendMessage(message);
    }

    public boolean onTouchEvent(MotionEvent event) {
        boolean turnBack = (imgSeq == 0 || imgSeq == 4) && System.currentTimeMillis() - currentTimeMils <= DELAY_SHOW;
        if (turnBack) {
            return super.onTouchEvent(event);
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (imgSeq <= mImgArr.length) {
                    Message message = new Message();
                    message.what = imgSeq;
                    mHandler.sendMessage(message);
                } else {
                    imgSeq = mImgArr.length;
                }
                break;
            default:
                break;
        }
        return super.onTouchEvent(event);
    }

    private void initHandler() {
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                final float brightness = 1.0f;
                try {
                    BitmapFactory.Options option = new BitmapFactory.Options();
                    option.inPreferredConfig = Config.ARGB_8888;
                    option.inPurgeable = true;
                    option.inInputShareable = true;
                    InputStream mInputSream = getResources()
                            .openRawResource(mImgArr[imgSeq]);
                    Bitmap bitmap = BitmapFactory.decodeStream(mInputSream,
                            null, option);
                    BitmapDrawable bitmapDrawable = new BitmapDrawable(
                            bitmap);
                    mLinearLayout.setBackgroundDrawable(bitmapDrawable);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                imgSeq++;
                currentTimeMils = System.currentTimeMillis();
                mLayoutParams = getWindow().getAttributes();
                mLayoutParams.screenBrightness = brightness;
                getWindow().setAttributes(mLayoutParams);
                if (imgSeq > mImgArr.length) {
                    imgSeq = 0;
                    confirmButton();
                }

                super.handleMessage(msg);
            }
        };
    }

    public void confirmButton() {
        setBottomUIMenuVisibility(true);
        setContentView(R.layout.lcd_confirm);
        super.confirmButton();
        Utils.paddingLayout(findViewById(R.id.passButton), 0, ACTIVITY_TOP_PADDING, 0, 0);
    }

    @Override
    protected void onResume() {
        wakeLock(true);
        super.onResume();
        currentTimeMils = System.currentTimeMillis();
    }

    @Override
    protected void onPause() {
        wakeLock(false);
        super.onPause();
    }

    private void wakeLock(boolean isLock){
        if (isLock) {
            mWakeLock.acquire();
        }else {
            mWakeLock.release();
        }
    }
}
