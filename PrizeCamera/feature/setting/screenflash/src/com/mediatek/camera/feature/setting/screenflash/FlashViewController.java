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
package com.mediatek.camera.feature.setting.screenflash;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.mediatek.camera.common.IAppUi;
import com.mediatek.camera.R;
import com.mediatek.camera.common.IAppUiListener;
import com.mediatek.camera.common.app.IApp;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.utils.CameraUtil;
import com.mediatek.camera.common.widget.RotateImageView;
import com.mediatek.camera.feature.setting.flash.PrizeFlashMenuContainer;

import java.util.ArrayList;


/**
 * This class manages the looks of the flash and flash mode choice view.
 */
public class FlashViewController {
    private static final LogUtil.Tag TAG =
            new LogUtil.Tag(FlashViewController.class.getSimpleName());

    private static final int FLASH_ENTRY_LIST_SWITCH_SIZE = 2;
    private static final int FLASH_ENTRY_LIST_INDEX_0 = 0;
    private static final int FLASH_ENTRY_LIST_INDEX_1 = 1;
    private static int SCREEN_FLASH_PRIORITY = 35;
    private static final int FLASH_SHUTTER_PRIORITY = 70;

    private static final String FLASH_AUTO_VALUE = "auto";
    private static final String FLASH_OFF_VALUE = "off";
    private static final String FLASH_ON_VALUE = "screen";

    private static final int FLASH_VIEW_INIT = 0;
    private static final int FLASH_VIEW_ADD_QUICK_SWITCH = 1;
    private static final int FLASH_VIEW_REMOVE_QUICK_SWITCH = 2;
    private static final int FLASH_VIEW_HIDE_CHOICE_VIEW = 3;
    private static final int FLASH_VIEW_UPDATE_QUICK_SWITCH_ICON = 4;
    private final FlashOnTouchListener mFlashOnTouchListerer;

    private ImageView mFlashEntryView;
    private ImageView mFlashIndicatorView;
    private ImageView mFlashTip;
    private ImageView mFlashOffIcon;
    private ImageView mFlashAutoIcon;
    private ImageView mFlashOnIcon;
    private ImageView mFlashTorchIcon;
    private View mFlashChoiceView;
    private View mOptionLayout;
    private final ScreenFlash mFlash;
    private final IApp mApp;
    private MainHandler mMainHandler;
    private boolean isCSShot = false;
    private IAppUi.HintInfo mFlashIndicatorHint;
    private PrizeFlashMenuContainer mPrizeFlashMenuContainer;
    private final OnOrientationChangeListenerImpl mOnOrientationChangeListener;

    private static final String FLASH_VALUES[] = new String[]{
            FLASH_OFF_VALUE,
            FLASH_AUTO_VALUE,
            FLASH_ON_VALUE,
    };

    private ArrayList<TextView> mIconViews = new ArrayList<>();
    private int mFlashIconEnd;///prize-add-huangpengfei-2019-03-06

    /**
     * Constructor of flash view.
     * @param flash Flash instance.
     * @param app   The application app level controller.
     */
    public FlashViewController(ScreenFlash flash, IApp app) {
        mFlash = flash;
        mApp = app;
        mMainHandler = new MainHandler(app.getActivity().getMainLooper());
        mMainHandler.sendEmptyMessage(FLASH_VIEW_INIT);
        mOnOrientationChangeListener = new OnOrientationChangeListenerImpl();
        mFlashOnTouchListerer = new FlashOnTouchListener();
        mFlashIndicatorHint = new IAppUi.HintInfo();
        mFlashIndicatorHint.mBackground = null;//app.getActivity().getDrawable(R.drawable.focus_hint_background);
        mFlashIndicatorHint.mType = IAppUi.HintType.TYPE_ALWAYS_BOTTOM_ICON;
        mFlashIndicatorHint.mImage = app.getActivity().getDrawable(R.drawable.indicator_flash_enable);
    }

    /**
     * add flash switch to quick switch.
     */
    public void addQuickSwitchIcon() {

        mMainHandler.sendEmptyMessage(FLASH_VIEW_ADD_QUICK_SWITCH);
    }

    /**
     * remove qiuck switch icon.
     */
    public void removeQuickSwitchIcon() {

        mMainHandler.sendEmptyMessage(FLASH_VIEW_REMOVE_QUICK_SWITCH);
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
        mFlash.resetFlashStatus();
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
                    //mFlashEntryView.setFlash(mFlash);
                    break;

                case FLASH_VIEW_ADD_QUICK_SWITCH:
                    mApp.getAppUi().addToQuickSwitcher(mFlashEntryView, SCREEN_FLASH_PRIORITY);
                    updateFlashEntryView(mFlash.getLocalValue());
                    mApp.getAppUi().registerOnShutterButtonListener(mShutterListener,
                            FLASH_SHUTTER_PRIORITY);
                    break;

                case FLASH_VIEW_REMOVE_QUICK_SWITCH:
                    mApp.getAppUi().removeFromQuickSwitcher(mFlashEntryView);
//                    updateFlashIndicator(mFlash.getValue());
                    mApp.getAppUi().hideScreenHint(mFlashIndicatorHint);
                    mApp.getAppUi().unregisterOnShutterButtonListener(mShutterListener);
                    break;

                case FLASH_VIEW_UPDATE_QUICK_SWITCH_ICON:
                    if ((boolean) msg.obj) {
                        mFlashEntryView.setVisibility(View.VISIBLE);
                        updateFlashEntryView(mFlash.getLocalValue());
                    } else {
                        mFlashEntryView.setVisibility(View.GONE);
                        mApp.getAppUi().hideScreenHint(mFlashIndicatorHint);
                    }
                    break;

                case FLASH_VIEW_HIDE_CHOICE_VIEW:
                    if (mPrizeFlashMenuContainer != null && mPrizeFlashMenuContainer.isMenuShown()) {
                        hideFlashMenuContainer();
                        updateFlashEntryView(mFlash.getValue());
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
        LogHelper.d(TAG, "[updateFlashView] currentValue = " + mFlash.getLocalValue());
        if (FLASH_ON_VALUE.equals(value)&&!isCSShot) {
            mFlashEntryView.setImageResource(R.drawable.prize_ic_screen_flash_status_on);
            mFlashEntryView.setContentDescription(mApp.getActivity().getResources().getString(
                    R.string.accessibility_flash_on));
        } else if (FLASH_AUTO_VALUE.equals(value)&&!isCSShot) {
            mFlashEntryView.setImageResource(R.drawable.prize_ic_screen_flash_status_auto);
            mFlashEntryView.setContentDescription(mApp.getActivity().getResources().getString(
                    R.string.accessibility_flash_auto));
        }  else {
            mFlashEntryView.setImageResource(R.drawable.prize_ic_screen_flash_status_off);
            mFlashEntryView.setContentDescription(mApp.getActivity().getResources().getString(
                    R.string.accessibility_flash_off));
        }
        // Flash indicator no need to show now,would be enable later
        updateFlashIndicator(value);
    }

    /**
     * Initialize the flash view which will add to quick switcher.
     * @return the view add to quick switcher
     */
    private ImageView initFlashEntryView() {
        Activity activity = mApp.getActivity();
        RotateImageView view = (RotateImageView) activity.getLayoutInflater().inflate(
                R.layout.screen_flash_icon, null);
        view.setOnClickListener(mFlashEntryListener);
        mFlashIndicatorView = (RotateImageView) activity.getLayoutInflater().inflate(
                R.layout.screen_flash_indicator, null);
        return view;
    }

    private void prizeInitializeFlashChoiceView(View flashEntryView) {
        if (mOptionLayout == null) {
            mPrizeFlashMenuContainer = mApp.getActivity().findViewById(R.id.prize_flash_menu_container);
            /*prize-add-huangpengfei-2019-03-06-start*/
            int screenWidth = mApp.getAppUi().getScreenPixWidth();
            if (flashEntryView.isLayoutRtl()) {
                mFlashIconEnd = screenWidth - flashEntryView.getRight();
            }else{
                mFlashIconEnd = flashEntryView.getRight();
            }
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mPrizeFlashMenuContainer.getLayoutParams();
            if (mPrizeFlashMenuContainer.isLayoutRtl()) {
                layoutParams.leftMargin = 0;
                layoutParams.rightMargin = mFlashIconEnd;
            }else{
                layoutParams.leftMargin = mFlashIconEnd;
                layoutParams.rightMargin = 0;
            }
            mPrizeFlashMenuContainer.setLayoutParams(layoutParams);
            /*prize-add-huangpengfei-2019-03-06-end*/
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
        mPrizeFlashMenuContainer.hide();
    }

    /**
     * This listener used to monitor the flash quick switch icon click item.
     */
    private final View.OnClickListener mFlashEntryListener = new View.OnClickListener() {
        public void onClick(View view) {
            if (mFlash.getEntryValues().size() <= 1) {
                return;
            }
            //mApp.getAppUi().controlSetting(false,true);
            if (mFlash.getEntryValues().size() > FLASH_ENTRY_LIST_SWITCH_SIZE) {
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
                //mApp.getAppUi().showQuickSwitcherOption(mOptionLayout);
            } else {
                String value = mFlash.getEntryValues().get(FLASH_ENTRY_LIST_INDEX_0);
                if (value.equals(mFlash.getValue())) {
                    value = mFlash.getEntryValues().get(FLASH_ENTRY_LIST_INDEX_1);
                }
                updateFlashEntryView(value);
                // Flash indicator no need to show now,would be enable later
                updateFlashIndicator(value);
                mFlash.onFlashValueChanged(value);
            }
        }
    };

    private View.OnClickListener mFlashChoiceViewListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String value = "";
            if (mFlashAutoIcon == view) {
                value = FLASH_AUTO_VALUE;
            } else if (mFlashOnIcon == view) {
                value = FLASH_ON_VALUE;
            }else {
                value = FLASH_OFF_VALUE;
            }
            //mApp.getAppUi().hideQuickSwitcherOption();
            hideFlashMenuContainer();
            updateFlashEntryView(value);
            // Flash indicator no need to show now,would be enable later
            //updateFlashIndicator(value);
            mFlash.onFlashValueChanged(value);
            mFlash.resetFlashStatus();
        }

    };

    private View.OnClickListener mFlashTipViewListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mApp.getAppUi().hideQuickSwitcherOption();
        }

    };

    public void updateFlashIndicator(String value) {
        if (FLASH_ON_VALUE.equals(value) && !isCSShot){
            mApp.getAppUi().showScreenHint(mFlashIndicatorHint);
        }else {
            mApp.getAppUi().hideScreenHint(mFlashIndicatorHint);
        }
    }

    /**
     * This function used to high light the current choice for.
     * flash if flash choice view is show.
     */
    private void updateChoiceView() {
        if (FLASH_ON_VALUE.equals(mFlash.getLocalValue())) {
            mFlashOnIcon.setImageResource(R.drawable.prize_ic_screen_flash_status_on_selected);
            mFlashOffIcon.setImageResource(R.drawable.prize_ic_screen_flash_status_off);
            mFlashAutoIcon.setImageResource(R.drawable.prize_ic_screen_flash_status_auto);
        } else if (FLASH_OFF_VALUE.equals(mFlash.getLocalValue())) {
            mFlashOnIcon.setImageResource(R.drawable.prize_ic_screen_flash_status_on);
            mFlashOffIcon.setImageResource(R.drawable.prize_ic_screen_flash_status_off_selected);
            mFlashAutoIcon.setImageResource(R.drawable.prize_ic_screen_flash_status_auto);
        } else {
            mFlashOnIcon.setImageResource(R.drawable.prize_ic_screen_flash_status_on);
            mFlashOffIcon.setImageResource(R.drawable.prize_ic_screen_flash_status_off);
            mFlashAutoIcon.setImageResource(R.drawable.prize_ic_screen_flash_status_auto_selected);
        }

    }

    private void initializeFlashChoiceView() {
        if (mFlashChoiceView == null || mOptionLayout == null) {
            /*ViewGroup viewGroup =  mApp.getAppUi().getModeRootView();
            mOptionLayout = mApp.getActivity().getLayoutInflater().inflate(
                    R.layout.screen_flash_option, viewGroup, false);
            mFlashChoiceView = mOptionLayout.findViewById(R.id.flash_choice);
            mFlashOnIcon = (TextView) mOptionLayout.findViewById(R.id.flash_on);
            mFlashOffIcon = (TextView) mOptionLayout.findViewById(R.id.flash_off);
            mFlashAutoIcon = (TextView) mOptionLayout.findViewById(R.id.flash_auto);
            //mFlashTorchIcon = (TextView) mOptionLayout.findViewById(R.id.flash_torch);
            mFlashTip = (ImageView) mOptionLayout.findViewById(R.id.flash);
            mFlashOffIcon.setOnClickListener(mFlashChoiceViewListener);
            mFlashOnIcon.setOnClickListener(mFlashChoiceViewListener);
            mFlashAutoIcon.setOnClickListener(mFlashChoiceViewListener);
            //mFlashTorchIcon.setOnClickListener(mFlashChoiceViewListener);
            mFlashTip.setOnClickListener(mFlashTipViewListener);

            // zhangguo add start
            mIconViews.clear();
            mIconViews.add(mFlashOffIcon);
            mIconViews.add(mFlashAutoIcon);
            mIconViews.add(mFlashOnIcon);*/
            //mIconViews.add(mFlashTorchIcon);
            // zhangguo add end

        }
    }

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


    public void updateFlashEntryIconWhenCS(boolean start){
        if (FLASH_AUTO_VALUE.equals(mFlash.getValue())||FLASH_ON_VALUE.equals(mFlash.getValue())){
            if(start){
                updateFlashEntryView(FLASH_OFF_VALUE);
                isCSShot = true;
            }else {
                isCSShot = false;
                updateFlashEntryView(mFlash.getValue());
            }

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
        public boolean onTouch(MotionEvent event) {
            hideFlashMenuContainer();
            return false;
        }
    }
}
