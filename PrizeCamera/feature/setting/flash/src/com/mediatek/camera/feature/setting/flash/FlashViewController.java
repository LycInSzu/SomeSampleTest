/*
 * Copyright Statement:
 *
 *   This software/firmware and related documentation ("MediaTek Software") are
 *   protected under relevant copyright laws. The information contained herein is
 *   confidential and proprietary to MediaTek Inc. and/or its licensors. Without
 *   the prior written permission of MediaTek inc. and/or its licensors, any
 *   reproduction, modification, use or disclosure of MediaTek Software, and
 *   information contained herein, in whole or in part, shall be strictly
 *   prohibited.
 *
 *   MediaTek Inc. (C) 2016. All rights reserved.
 *
 *   BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 *   THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 *   RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER
 *   ON AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL
 *   WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 *   WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 *   NONINFRINGEMENT. NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH
 *   RESPECT TO THE SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY,
 *   INCORPORATED IN, OR SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES
 *   TO LOOK ONLY TO SUCH THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO.
 *   RECEIVER EXPRESSLY ACKNOWLEDGES THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO
 *   OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES CONTAINED IN MEDIATEK
 *   SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE
 *   RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 *   STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S
 *   ENTIRE AND CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE
 *   RELEASED HEREUNDER WILL BE, AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE
 *   MEDIATEK SOFTWARE AT ISSUE, OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE
 *   CHARGE PAID BY RECEIVER TO MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 *   The following software/firmware and/or related documentation ("MediaTek
 *   Software") have been modified by MediaTek Inc. All revisions are subject to
 *   any receiver's applicable license agreements with MediaTek Inc.
 */
package com.mediatek.camera.feature.setting.flash;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.mediatek.camera.R;
import com.mediatek.camera.common.IAppUi;
import com.mediatek.camera.common.IAppUiListener;
import com.mediatek.camera.common.app.IApp;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.utils.CameraUtil;
import com.mediatek.camera.common.widget.RotateImageView;
import com.mediatek.camera.prize.FeatureSwitcher;


/**
 * This class manages the looks of the flash and flash mode choice view.
 */
public class FlashViewController {
    private static final LogUtil.Tag TAG =
            new LogUtil.Tag(FlashViewController.class.getSimpleName());

    private static final int FLASH_ENTRY_LIST_SWITCH_SIZE = 2;
    private static final int FLASH_ENTRY_LIST_INDEX_0 = 0;
    private static final int FLASH_ENTRY_LIST_INDEX_1 = 1;
    private static final int FLASH_PRIORITY = 5;//prize-change-huangpengfei-2018-9-26
    private static final int FLASH_SHUTTER_PRIORITY = 70;

    private static final String FLASH_AUTO_VALUE = "auto";
    private static final String FLASH_OFF_VALUE = "off";
    private static final String FLASH_ON_VALUE = "on";

    private static final int FLASH_VIEW_INIT = 0;
    private static final int FLASH_VIEW_ADD_QUICK_SWITCH = 1;
    private static final int FLASH_VIEW_REMOVE_QUICK_SWITCH = 2;
    private static final int FLASH_VIEW_HIDE_CHOICE_VIEW = 3;
    private static final int FLASH_VIEW_UPDATE_QUICK_SWITCH_ICON = 4;

    private ImageView mFlashEntryView;
    private ImageView mFlashIndicatorView;
    private ImageView mFlashOffIcon;
    private ImageView mFlashAutoIcon;
    private ImageView mFlashOnIcon;
    private View mFlashChoiceView;
    private View mOptionLayout;
    private final Flash mFlash;
    private final IApp mApp;
    private MainHandler mMainHandler;
	/*prize-add-huangpengfei-2018-9-29-start*/
    private PrizeFlashMenuContainer mPrizeFlashMenuContainer;
    private final OnOrientationChangeListenerImpl mOnOrientationChangeListener;
    private final FlashOnTouchListener mFlashOnTouchListerer;
    private int mFlashIconEnd;
    private IAppUi.HintInfo mFlashHint;
    private final FlashOnBatteryLowListener mFlashOnBatteryLowListener;
    /*prize-add-huangpengfei-2018-9-29-end*/

    /**
     * Constructor of flash view.
     * @param flash Flash instance.
     * @param app   The application app level controller.
     */
    public FlashViewController(Flash flash, IApp app) {
        mFlash = flash;
        mApp = app;
        mMainHandler = new MainHandler(app.getActivity().getMainLooper());
        mMainHandler.sendEmptyMessage(FLASH_VIEW_INIT);
        /*prize-add-huangpengfei-2018-9-29-start*/
        mOnOrientationChangeListener = new OnOrientationChangeListenerImpl();
        mFlashOnTouchListerer = new FlashOnTouchListener();
        mFlashOnBatteryLowListener = new FlashOnBatteryLowListener();
        /*prize-add-huangpengfei-2018-9-29-end*/
    }

    /**
     * add flash switch to quick switch.
     */
    public void addQuickSwitchIcon() {
        mMainHandler.sendEmptyMessage(FLASH_VIEW_ADD_QUICK_SWITCH);
        mApp.registerOnBatteryLowListener(mFlashOnBatteryLowListener);//prize-add-huangpengfei-2019-03-15
    }

    /**
     * remove qiuck switch icon.
     */
    public void removeQuickSwitchIcon() {
        mMainHandler.sendEmptyMessage(FLASH_VIEW_REMOVE_QUICK_SWITCH);
        mApp.unregisterBatteryLowListener(mFlashOnBatteryLowListener);//prize-add-huangpengfei-2019-03-15
    }

    /**
     * for overrides value, for set visibility.
     * @param isShow true means show.
     */
    public void showQuickSwitchIcon(boolean isShow) {
        mMainHandler.obtainMessage(FLASH_VIEW_UPDATE_QUICK_SWITCH_ICON, isShow).sendToTarget();
    }

    /**
     * close option menu.
     */
    public void hideFlashChoiceView() {
        mMainHandler.sendEmptyMessage(FLASH_VIEW_HIDE_CHOICE_VIEW);
    }

    public void uninit(){
        mApp.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                hideFlashMenuContainer();
            }
        });
    }

    // [Add for CCT tool] Receive keycode and enable/disable ZSD @{
    protected IApp.KeyEventListener getKeyEventListener() {
        return new IApp.KeyEventListener() {
            @Override
            public boolean onKeyDown(int keyCode, KeyEvent event) {
                if ((keyCode != CameraUtil.KEYCODE_SET_FLASH_ON
                        && keyCode != CameraUtil.KEYCODE_SET_FLASH_OFF)
                        || !CameraUtil.isSpecialKeyCodeEnabled()) {
                    return false;
                }
                return true;
            }

            @Override
            public boolean onKeyUp(int keyCode, KeyEvent event) {
                if (!CameraUtil.isSpecialKeyCodeEnabled()) {
                    return false;
                }
                if (keyCode != CameraUtil.KEYCODE_SET_FLASH_ON
                        && keyCode != CameraUtil.KEYCODE_SET_FLASH_OFF) {
                    return false;
                }
                if (mFlashEntryView == null) {
                    LogHelper.e(TAG, "[onKeyUp] mFlashEntryView is null");
                    return false;
                }

                if (keyCode == CameraUtil.KEYCODE_SET_FLASH_ON) {
                    LogHelper.i(TAG, "[onKeyUp] update flash on");
                    updateFlashEntryView(FLASH_ON_VALUE);
                    mFlash.onFlashValueChanged(FLASH_ON_VALUE);
                } else if (keyCode == CameraUtil.KEYCODE_SET_FLASH_OFF) {
                    LogHelper.i(TAG, "[onKeyUp] update flash off");
                    updateFlashEntryView(FLASH_OFF_VALUE);
                    mFlash.onFlashValueChanged(FLASH_OFF_VALUE);
                }
                return true;
            }
        };
    }
    // @}

    /**
     * Handler let some task execute in main thread.
     */
    private class MainHandler extends Handler {
        public MainHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            LogHelper.d(TAG, "view handleMessage: " + msg.what);
            switch (msg.what) {
                case FLASH_VIEW_INIT:
                    mFlashEntryView = initFlashEntryView();
                    break;

                case FLASH_VIEW_ADD_QUICK_SWITCH:
                    mApp.getAppUi().addToQuickSwitcher(mFlashEntryView, FLASH_PRIORITY);
                    updateFlashEntryView(mFlash.getValue());
                    mApp.getAppUi().registerOnShutterButtonListener(mShutterListener,
                            FLASH_SHUTTER_PRIORITY);
                    break;

                case FLASH_VIEW_REMOVE_QUICK_SWITCH:
                    mApp.getAppUi().removeFromQuickSwitcher(mFlashEntryView);
                    //updateFlashIndicator(false);
                    mApp.getAppUi().unregisterOnShutterButtonListener(mShutterListener);
                    break;

                case FLASH_VIEW_UPDATE_QUICK_SWITCH_ICON:
                    if ((boolean) msg.obj) {
                        mFlashEntryView.setVisibility(View.VISIBLE);
                        updateFlashEntryView(mFlash.getValue());
                    } else {
                        mFlashEntryView.setVisibility(View.INVISIBLE);
                    }
                    break;

                case FLASH_VIEW_HIDE_CHOICE_VIEW:
                    /*prize-change-huangpengfei-2018-9-29-start*/
                    if (mPrizeFlashMenuContainer != null && mPrizeFlashMenuContainer.isMenuShown()) {
                        //mApp.getAppUi().hideQuickSwitcherOption();
                        hideFlashMenuContainer();
                        /*prize-change-huangpengfei-2018-9-29-end*/
                        updateFlashEntryView(mFlash.getValue());
                        // Flash indicator no need to show now,would be enable later
                        // updateFlashIndicator(mFlash.getValue());
                    }
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Update ui by the value.
     * @param value the value to change.
     *
     */
    private void updateFlashEntryView(final String value) {
        LogHelper.d(TAG, "[updateFlashView] currentValue = " + mFlash.getValue());
        if (FLASH_ON_VALUE.equals(value)) {
            mFlashEntryView.setImageResource(R.drawable.prize_ic_flash_status_on);//prize-change icon name-huangpengfei-2018-9-27
            mFlashEntryView.setContentDescription(mApp.getActivity().getResources().getString(
                    R.string.accessibility_flash_on));
        } else if (FLASH_AUTO_VALUE.equals(value)) {
            mFlashEntryView.setImageResource(R.drawable.prize_ic_flash_status_auto);//prize-change icon name-huangpengfei-2018-9-27
            mFlashEntryView.setContentDescription(mApp.getActivity().getResources().getString(
                    R.string.accessibility_flash_auto));
        } else {
            mFlashEntryView.setImageResource(R.drawable.prize_ic_flash_status_off);//prize-change icon name-huangpengfei-2018-9-27
            mFlashEntryView.setContentDescription(mApp.getActivity().getResources().getString(
                    R.string.accessibility_flash_off));
        }
        // Flash indicator no need to show now,would be enable later
        // updateFlashIndicator(value);
    }

    /**
     * Initialize the flash view which will add to quick switcher.
     * @return the view add to quick switcher
     */
    private ImageView initFlashEntryView() {
        Activity activity = mApp.getActivity();
        RotateImageView view = (RotateImageView) activity.getLayoutInflater().inflate(
                R.layout.flash_icon, null);
        view.setOnClickListener(mFlashEntryListener);
        mFlashIndicatorView = (RotateImageView) activity.getLayoutInflater().inflate(
                R.layout.flash_indicator, null);
        return view;
    }

    /**
     * This listener used to monitor the flash quick switch icon click item.
     */
    private final View.OnClickListener mFlashEntryListener = new View.OnClickListener() {
        public void onClick(View view) {
            if (mFlash.getEntryValues().size() <= 1) {
                return;
            }
            /*prize-add-huangpengfei-2019-03-15-start*/
            if (FeatureSwitcher.getCurrentProjectValue() != 2 && mApp.isBatteryLow()){
                showFlashBatteryLowTips();
                return;
            }
            /*prize-add-huangpengfei-2019-03-15-end*/
            if (mFlash.getEntryValues().size() > FLASH_ENTRY_LIST_SWITCH_SIZE) {
                /*prize-change-huangpengfei-2018-9-29-start*/
                //initializeFlashChoiceView();
                prizeInitializeFlashChoiceView(view);
                updateChoiceView();
                /*prize-modify fixbug[74543]-huangpengfei-20190417-start*/
                if (mPrizeFlashMenuContainer.isMenuShown()){
                    hideFlashMenuContainer();
                } else {
                    showFlashMenuContainer();
                }
                /*prize-modify fixbug[74543]-huangpengfei-20190417-end*/
                //mApp.getAppUi().showQuickSwitcherOption(mPrizeFlashMenuContainer);
                /*prize-change-huangpengfei-2018-9-29-end*/

            } else {
                String value = mFlash.getEntryValues().get(FLASH_ENTRY_LIST_INDEX_0);
                if (value.equals(mFlash.getValue())) {
                    value = mFlash.getEntryValues().get(FLASH_ENTRY_LIST_INDEX_1);
                }
                updateFlashEntryView(value);
                // Flash indicator no need to show now,would be enable later
                // updateFlashIndicator(value);
                mFlash.onFlashValueChanged(value);
            }
        }
    };

    /*prize-add-huangpengfei-2019-03-15-start*/
    private void showFlashBatteryLowTips() {
        if (mFlashHint == null){
            mFlashHint = new IAppUi.HintInfo();
            mFlashHint.mDelayTime = 3000;
            int id = mApp.getActivity().getResources().getIdentifier("hint_text_background",
                    "drawable", mApp.getActivity().getPackageName());
            mFlashHint.mBackground = mApp.getActivity().getDrawable(id);
            mFlashHint.mType = IAppUi.HintType.TYPE_AUTO_HIDE;
            mFlashHint.mHintText = mApp.getActivity().getString(R.string.flash_failed_when_battery_is_low);
        }
        mApp.getAppUi().showScreenHint(mFlashHint);
    }
    /*prize-add-huangpengfei-2019-03-15-end*/

    private View.OnClickListener mFlashChoiceViewListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String value = "";
            if (mFlashAutoIcon == view) {
                value = FLASH_AUTO_VALUE;
            } else if (mFlashOnIcon == view) {
                value = FLASH_ON_VALUE;
            } else {
                value = FLASH_OFF_VALUE;
            }
            /*prize-change-huangpengfei-2018-9-29-start*/
            //mApp.getAppUi().hideQuickSwitcherOption();
            hideFlashMenuContainer();
            /*prize-change-huangpengfei-2018-9-29-end*/
            updateFlashEntryView(value);
            // Flash indicator no need to show now,would be enable later
            // updateFlashIndicator(value);
            mFlash.onFlashValueChanged(value);
        }

    };

    private void updateFlashIndicator(final boolean value) {
        if (value) {
            mApp.getAppUi().addToIndicatorView(mFlashIndicatorView, FLASH_PRIORITY);
        } else {
            mApp.getAppUi().removeFromIndicatorView(mFlashIndicatorView);
        }
    }

    /**
     * This function used to high light the current choice for.
     * flash if flash choice view is show.
     */
    private void updateChoiceView() {
        /*prize-change-huangpengfei-2018-9-27-start*/
        if (FLASH_ON_VALUE.equals(mFlash.getValue())) {
            mFlashOnIcon.setImageResource(R.drawable.prize_ic_flash_status_on_selected);
            mFlashOffIcon.setImageResource(R.drawable.prize_ic_flash_status_off);
            mFlashAutoIcon.setImageResource(R.drawable.prize_ic_flash_status_auto);
        } else if (FLASH_OFF_VALUE.equals(mFlash.getValue())) {
            mFlashOnIcon.setImageResource(R.drawable.prize_ic_flash_status_on);
            mFlashOffIcon.setImageResource(R.drawable.prize_ic_flash_status_off_selected);
            mFlashAutoIcon.setImageResource(R.drawable.prize_ic_flash_status_auto);
        } else {
            mFlashOnIcon.setImageResource(R.drawable.prize_ic_flash_status_on);
            mFlashOffIcon.setImageResource(R.drawable.prize_ic_flash_status_off);
            mFlashAutoIcon.setImageResource(R.drawable.prize_ic_flash_status_auto_selected);
        }
        /*prize-change-huangpengfei-2018-9-27-end*/
    }

    private void initializeFlashChoiceView() {
        if (mFlashChoiceView == null || mOptionLayout == null) {
            ViewGroup viewGroup =  mApp.getAppUi().getModeRootView();
            mOptionLayout = mApp.getActivity().getLayoutInflater().inflate(
                    R.layout.flash_option, viewGroup, false);
            mFlashChoiceView = mOptionLayout.findViewById(R.id.flash_choice);
            mFlashOnIcon = (ImageView) mOptionLayout.findViewById(R.id.flash_on);
            mFlashOffIcon = (ImageView) mOptionLayout.findViewById(R.id.flash_off);
            mFlashAutoIcon = (ImageView) mOptionLayout.findViewById(R.id.flash_auto);
            mFlashOffIcon.setOnClickListener(mFlashChoiceViewListener);
            mFlashOnIcon.setOnClickListener(mFlashChoiceViewListener);
            mFlashAutoIcon.setOnClickListener(mFlashChoiceViewListener);
        }
    }

    /*prize-add-huangpengfei-2018-9-29-start*/
    private void prizeInitializeFlashChoiceView(View flashEntryView) {
        if (mOptionLayout == null) {
            mPrizeFlashMenuContainer = mApp.getActivity().findViewById(R.id.prize_flash_menu_container);
            int screenWidth = mApp.getAppUi().getScreenPixWidth();
            if (flashEntryView.isLayoutRtl()) {
                mFlashIconEnd = screenWidth - flashEntryView.getRight();
            }else{
                mFlashIconEnd = flashEntryView.getRight();
            }
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mPrizeFlashMenuContainer.getLayoutParams();
            LogHelper.d(TAG, "[prizeInitializeFlashChoiceView] mFlashIconEnd = " + mFlashIconEnd);

            if (mPrizeFlashMenuContainer.isLayoutRtl()) {
                layoutParams.leftMargin = 0;
                layoutParams.rightMargin = mFlashIconEnd;
            }else{
                layoutParams.leftMargin = mFlashIconEnd;
                layoutParams.rightMargin = 0;
            }
            mPrizeFlashMenuContainer.setLayoutParams(layoutParams);
            mPrizeFlashMenuContainer.setOnAnimationListener(new PrizeFlashMenuContainer.OnAnimationListener() {
                @Override
                public void onShowAnimationStart() {
                    mApp.getAppUi().hideQuickIconExceptFlash();
                }

                @Override
                public void onShowAnimationEnd() {
                }

                @Override
                public void onHideAnimationEnd() {
                    mApp.getAppUi().showQuickIconExceptFlash();
                    mApp.unregisterOnOrientationChangeListener(mOnOrientationChangeListener);
                    mApp.unregisterOnTouchListener(mFlashOnTouchListerer);
                }
            });
            View view = mApp.getActivity().findViewById(R.id.falsh_menu);
            mPrizeFlashMenuContainer.init(view);
            mFlashOnIcon = mPrizeFlashMenuContainer.findViewById(R.id.prize_flash_on);
            mFlashOffIcon = mPrizeFlashMenuContainer.findViewById(R.id.prize_flash_off);
            mFlashAutoIcon = mPrizeFlashMenuContainer.findViewById(R.id.prize_flash_auto);
            mFlashOffIcon.setOnClickListener(mFlashChoiceViewListener);
            mFlashOnIcon.setOnClickListener(mFlashChoiceViewListener);
            mFlashAutoIcon.setOnClickListener(mFlashChoiceViewListener);
        }
    }


    private void showFlashMenuContainer() {
        int orientation = mApp.getGSensorOrientation();
        CameraUtil.rotateRotateLayoutChildView(mApp.getActivity(), mPrizeFlashMenuContainer, orientation, true);
        mPrizeFlashMenuContainer.show();
        mApp.registerOnOrientationChangeListener(mOnOrientationChangeListener);
        mApp.registerOnTouchListener(mFlashOnTouchListerer);
    }

    private void hideFlashMenuContainer() {
        if(null != mPrizeFlashMenuContainer){
            mPrizeFlashMenuContainer.hide();
        }

    }

    private class OnOrientationChangeListenerImpl implements IApp.OnOrientationChangeListener {

        @Override
        public void onOrientationChanged(int orientation) {
            CameraUtil.rotateRotateLayoutChildView(mApp.getActivity(), mPrizeFlashMenuContainer,
                    orientation, true);
        }
    }

    class FlashOnTouchListener implements IApp.OnTouchListener{

        @Override
        public void onTouch() {
            hideFlashMenuContainer();
        }
    }

    class FlashOnBatteryLowListener implements IApp.OnBatteryLowListener{

        @Override
        public void onBatteryLow() {
            mFlash.onFlashValueChanged(Flash.FLASH_OFF_VALUE);
        }
    }
    /*prize-add-huangpengfei-2018-9-29-end*/

    private final IAppUiListener.OnShutterButtonListener mShutterListener =
            new IAppUiListener.OnShutterButtonListener() {

                @Override
                public boolean onShutterButtonFocus(boolean pressed) {
                    if (pressed) {
                        hideFlashChoiceView();
                    }
                    return false;
                }

                @Override
                public boolean onShutterButtonClick() {
                    return false;
                }

                @Override
                public boolean onShutterButtonLongPressed() {
                    return false;
                }
            };
}
