package com.pri.factorytest.TouchPanelEdge;

import android.content.Context;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemProperties;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pri.factorytest.PrizeBaseActivity;
import com.pri.factorytest.R;
import com.pri.factorytest.util.Utils;

public class TouchPanelEdge extends PrizeBaseActivity {

    TPTestSurfaceView mTpTest;
    TextView mLinearity;
    TextView mAccuracy;
    TextView mSensitivity;
    TextView mZoom;
    TextView mLinearityHorizontal;
    TextView mLinearityVertical;
    TextView mFree;
    private final static boolean IS_BANGS_SCREEN = SystemProperties.get("ro.pri.bangs.screen").equals("1");

    PowerManager powerManager = null;
    WakeLock wakeLock = null;

    private boolean mOriginalState = false;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        powerManager = (PowerManager) this
                .getSystemService(Context.POWER_SERVICE);
        wakeLock = this.powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK,
                "My Lock");
    }

    @Override
    public void onResume() {
        super.onResume();
        setBottomUIMenuVisibility(false);
        mTpTest = new TPTestSurfaceView(this);
        mOriginalState = getTouchPointState();
        setContentView(mTpTest);
        wakeLock.acquire();
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        getWindow().setAttributes(layoutParams);
        openTouchPoint(true);
    }

    private void setBottomUIMenuVisibility(boolean show) {
        View decorView = getWindow().getDecorView();
        int uiHideOption = View.SYSTEM_UI_FLAG_IMMERSIVE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        int uiShowOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(show ? uiShowOptions : uiHideOption);
    }

    private void openTouchPoint(boolean isEnabled) {
        Settings.System.putInt(getContentResolver(),
                Settings.System.POINTER_LOCATION, isEnabled ? 1 : 0);
    }

    private boolean getTouchPointState() {
        final int pointerLocationMode = Settings.System.getInt(getContentResolver(),
                Settings.System.POINTER_LOCATION, 0);
        return pointerLocationMode == 1;
    }

    @Override
    protected void onPause() {
        wakeLock.release();
        super.onPause();
        openTouchPoint(mOriginalState);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if (mTpTest.mModeType == mTpTest.TP_TEST_FREE) {
                    confirmButton();
                } else {
                    if (mTpTest.mModeType == mTpTest.TP_TEST_LINEARITY1) {
                        mTpTest.bLinearity1 = false;
                    } else if (mTpTest.mModeType == mTpTest.TP_TEST_LINEARITY2) {
                        mTpTest.bLinearity2 = false;
                    } else if (mTpTest.mModeType == mTpTest.TP_TEST_ACCURACY) {
                        mTpTest.bAccuracy = false;
                    } else if (mTpTest.mModeType == mTpTest.TP_TEST_SENSITIVITY) {
                        mTpTest.bSensitivity = false;
                    } else if (mTpTest.mModeType == mTpTest.TP_TEST_ZOOM) {
                        mTpTest.bZoom = false;
                    } else if (mTpTest.mModeType == mTpTest.TP_TEST_LINEARITY_HORIZONTAL) {
                        mTpTest.bLinearityHorizontal = false;
                    } else if (mTpTest.mModeType == mTpTest.TP_TEST_LINEARITY_VERTICAL) {
                        mTpTest.bLinearityVertical = false;
                    }
                    mTpTest.toNext();
                }
                break;
        }
        return true;
    }

    public void confirmButton() {
        setBottomUIMenuVisibility(true);
        setContentView(R.layout.touchpanel_confirm);
        Utils.paddingLayout(findViewById(R.id.passButton), 0, ACTIVITY_TOP_PADDING, 0, 0);
        mLinearityHorizontal = (TextView) findViewById(R.id.linearityHorizontal);
        mLinearityVertical = (TextView) findViewById(R.id.linearityVertical);
        mFree = (TextView) findViewById(R.id.free);
        if (mTpTest.bLinearityHorizontal) {
            mLinearityHorizontal.setText(getString(R.string.touchpanel_edge_linearityHorizontal) + getString(R.string.touchpanel_edge_pass));
        } else {
            mLinearityHorizontal.setText(getString(R.string.touchpanel_edge_linearityHorizontal) + getString(R.string.touchpanel_edge_fail));
        }
        if (mTpTest.bLinearityVertical) {
            mLinearityVertical.setText(getString(R.string.touchpanel_edge_linearityVertical) + getString(R.string.touchpanel_edge_pass));
        } else {
            mLinearityVertical.setText(getString(R.string.touchpanel_edge_linearityVertical) + getString(R.string.touchpanel_edge_fail));
        }

        super.confirmButton();
        mButtonPass.setEnabled(mTpTest.bLinearityHorizontal && mTpTest.bLinearityVertical);
    }

}
