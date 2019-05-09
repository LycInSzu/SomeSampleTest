package com.pri.factorytest.Brightness;

import android.os.Bundle;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;

import com.pri.factorytest.PrizeBaseActivity;
import com.pri.factorytest.R;
import com.pri.factorytest.util.Utils;

public class Brightness extends PrizeBaseActivity implements OnClickListener {
    private boolean mBrightnessDown;
    private boolean mBrightnessUp;
    //private WindowManager.LayoutParams lp;
    private static final int MIN_BRIGHTNESS_LEVEL = 10;
    private int mDefualtSystemBrightLevel = MIN_BRIGHTNESS_LEVEL;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.brightness);
        //lp = getWindow().getAttributes();
        try {
            mDefualtSystemBrightLevel = Settings.System.getInt(getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        Button display_lcd_on = (Button) findViewById(R.id.brightness_strong);
        display_lcd_on.setOnClickListener(this);
        Button display_lcd_off = (Button) findViewById(R.id.brightness_slow);
        display_lcd_off.setOnClickListener(this);
        confirmButtonNonEnable();
        Utils.paddingLayout(findViewById(R.id.passButton), 0, ACTIVITY_TOP_PADDING, 0, 0);

    }

    @Override
    public void onClick(View arg0) {
        switch (arg0.getId()) {
            case R.id.brightness_strong:
                /*lp.screenBrightness = 1.0f;
                getWindow().setAttributes(lp);*/
                Settings.System.putInt(getContentResolver(),
                        Settings.System.SCREEN_BRIGHTNESS, 255);
                mBrightnessUp = true;
                if ((mBrightnessUp) && (mBrightnessDown)) {
                    mButtonPass.setEnabled(true);
                }
                break;
            case R.id.brightness_slow:
                /*lp.screenBrightness = 0.2f;
                getWindow().setAttributes(lp);*/
                Settings.System.putInt(getContentResolver(),
                        Settings.System.SCREEN_BRIGHTNESS, 40);
                mBrightnessDown = true;
                if ((mBrightnessUp) && (mBrightnessDown)) {
                    mButtonPass.setEnabled(true);
                }
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Settings.System.putInt(getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS, mDefualtSystemBrightLevel);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Settings.System.putInt(getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS, mDefualtSystemBrightLevel);
    }

    @Override
    public void finish() {
        super.finish();
        Settings.System.putInt(getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS, mDefualtSystemBrightLevel);
    }
}
