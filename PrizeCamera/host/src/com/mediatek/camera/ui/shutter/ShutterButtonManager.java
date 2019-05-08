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

package com.mediatek.camera.ui.shutter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.mediatek.camera.CameraActivity;
import com.mediatek.camera.R;
import com.mediatek.camera.common.IAppUi;
import com.mediatek.camera.common.IAppUiListener.OnShutterButtonListener;
import com.mediatek.camera.common.app.IApp;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.mode.photo.PhotoMode;
import com.mediatek.camera.common.utils.CameraUtil; //prize-added by tangan-20190327-fixbug 72564
import com.mediatek.camera.common.utils.PriorityConcurrentSkipListMap;
import com.mediatek.camera.feature.setting.volumekeys.VolumekeyState;//prize-add-huangpengfei-2018-12-19
import com.mediatek.camera.prize.CaptureAnimation;
import com.mediatek.camera.ui.AbstractViewManager;


import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicInteger;
/*prize-modify-Modify the translation of picturezoom for Blu-xiaoping-20190305-start*/
import android.os.SystemProperties;
/*prize-modify-Modify the translation of picturezoom for Blu-xiaoping-20190305-end*/
/**
 * A manager for {@link ShutterButton}.
 */
public class ShutterButtonManager extends AbstractViewManager implements
                                                    ShutterRootLayout.OnShutterChangeListener {
	/*prize-add-huangpengfei-2018-12-19-start*/
    private int mVolumekeyState = VolumekeyState.VOLUME_KEY_STATE_UP;

    public void setVolumekeyState(int state) {
        mVolumekeyState = state;
    }
	/*prize-add-huangpengfei-2018-12-19-end*/

    /**
     * Shutter type change listener.
     */
    public interface OnShutterChangeListener {
        /**
         * When current valid shutter changed, invoke the listener to notify.
         * @param newShutterName The new valid shutter name.
         */
       void onShutterTypeChanged(String newShutterName);
    }
    private final static int SHUTTER_GESTURE_PRIORITY = 20;
    private static final LogUtil.Tag TAG = new LogUtil.Tag(
                              ShutterButtonManager.class.getSimpleName());
    private ShutterButton.OnShutterButtonListener mShutterButtonListener;

    private PriorityConcurrentSkipListMap<String, OnShutterButtonListener> mShutterButtonListeners
            = new PriorityConcurrentSkipListMap<>(true);

    /*prize-modify-switch mode after switch camera-xiaoping-20180913-start*/
    private static final int BACK_CAMERA_PHOTOMODE_DEFAULT = 3;
    private static final int FRONT_CAMERA_FACEBEAUTYMODE_DEFAULT = 2;
    /*prize-modify-switch mode after switch camera-xiaoping-20180913-end*/
    /*prize-modify-add switches to third-party algorithms-xiaoping-20181026-start*/
    public static final int MODE_PHOTO = 0;
    public static final int MODE_VIDEO = 1;
    public static final int MODE_FACE_BEAUTY = 2;
    public static final int MODE_PICTURE_ZOOM = 3;
    public static final int MODE_LOWLIGHT_SHOT = 4;
    public static final int MODE_PICSELFIE = 5;
    public static final int MODE_PROFESSIONAL = 6;
    public static final int MODE_APERTURE = 7;
    public static final int MODE_HDR = 8;
    public static final int MODE_GIF = 9;
    public static final int MODE_SCAN = 10;
    public static final int MODE_FILTER = 11;
    public static final int MODE_SLOWMOTION = 12;
    public static final int MODE_PRIZE_PANO = 13;

    /*prize-modify-add animation of takepictur-xiaoping-20181105-start*/
    private CaptureAnimation mCaptureAnimation;
    /*prize-modify-add animation of takepictur-xiaoping-20181105-end*/

    /*prize-modify-add switches to third-party algorithms-xiaoping-20181026-end*/

    /*prize-modify-bugid:68305 capture animation is abnormal under time-lapse photo-xiaoping-20181115-start*/
    private UiHandler mUiHandler;
    private static final int START_CAPTURE_ANIMATION = 0;
    private static final int FORCE_STOP_ANIMATION = 1;
    /*prize-modify-bugid:68305 capture animation is abnormal under time-lapse photo-xiaoping-20181115-end*/
    /**
     * Used to store a shutter information.
     */
    private static class ShutterItem {
        public Drawable mShutterDrawable;
        public String mShutterType;
        public String mShutterName;
        public ShutterView mShutterView;

    }
    private ConcurrentSkipListMap<Integer, ShutterItem> mShutterButtons =
            new ConcurrentSkipListMap<>();

    private ShutterRootLayout mShutterLayout;
    private LayoutInflater mInflater;
    private OnShutterChangeListener mListener;

    /**
     * constructor of ShutterButtonManager.
     * @param app The {@link IApp} implementer.
     * @param parentView the root view of ui.
     */
    public ShutterButtonManager(IApp app, ViewGroup parentView) {
        super(app, parentView);
        mShutterButtonListener = new ShutterButtonListenerImpl();
        mInflater = (LayoutInflater) app.getActivity()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        /*prize-modify-bugid:68305 capture animation is abnormal under time-lapse photo-xiaoping-20181115-start*/
        mUiHandler = new UiHandler(app.getActivity().getMainLooper());
        /*prize-modify-bugid:68305 capture animation is abnormal under time-lapse photo-xiaoping-20181115-end*/
    }

    @Override
    protected View getView() {
        mShutterLayout = (ShutterRootLayout) mApp.getActivity().findViewById(R.id.shutter_root);
        mShutterLayout.setOnShutterChangedListener(this);
        /*prize-modify-add animation of takepictur-xiaoping-20181105-start*/
        mCaptureAnimation = (CaptureAnimation) mApp.getActivity().findViewById(R.id.shutter_captureanimation);
        /*prize-modify-add animation of takepictur-xiaoping-20181105-end*/
        mApp.getAppUi().registerGestureListener(mShutterLayout.getGestureListener(),
                SHUTTER_GESTURE_PRIORITY);
        // [Add for CCT tool] Receive keycode and switch photo/video mode @{
        //mApp.registerKeyEventListener(mShutterLayout.getKeyEventListener(), IApp.DEFAULT_PRIORITY);//prize-remove bug[70904] not allow other ways switch mode-huangpengfei-2019-01-22
        // @}
        return mShutterLayout;
    }

    public void setOnShutterChangedListener(OnShutterChangeListener listener) {
        mListener = listener;
    }

    @Override
    public void onShutterChangedStart(String newShutterName) {
        if (mListener != null) {
            mListener.onShutterTypeChanged(newShutterName);
        }
    }

    @Override
    public void onShutterChangedEnd() {
    }

    @Override
    public void setEnabled(boolean enabled) {
        LogHelper.d(TAG, "[setEnabled] enabled = " + enabled);
        if (mShutterLayout != null) {
            mShutterLayout.setEnabled(enabled);
            LogHelper.d(TAG, "setEnabled:"+enabled);//prize-added by tangan-20190327-add log 
            int count = mShutterLayout.getChildCount();
            for (int i = 0; i < count; i++) {
                View view = mShutterLayout.getChildAt(i);
                view.setEnabled(enabled);
                LogHelper.d(TAG, "[setEnabled] view = " + view + " : setEnabled = " + enabled);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mShutterLayout != null) {
            mShutterLayout.onResume();
            /*prize-modify-bugid:70831 Switch mode error-xiaoping-20190119-start*/
            setEnabled(true);
            /*prize-modify-bugid:70831 Switch mode error-xiaoping-20190119-end*/
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mShutterLayout != null) {
            mShutterLayout.onPause();
        }
    }

    /**
     * Set Shutter text can be clicked or not.
     * @param enabled True shutter text can be clicked.
     *                False shutter text can not be clicked.
     */
    public void setTextEnabled(boolean enabled) {
        if (mShutterLayout != null) {
            mShutterLayout.setEnabled(enabled);
            int count = mShutterLayout.getChildCount();
            for (int i = 0; i < count; i++) {
                View view = mShutterLayout.getChildAt(i);
                ((ShutterView) view).setTextEnabled(enabled);
            }
        }
    }

    /**
     * Register shutterButton listener.
     * @param listener
     *            the listener set to shutterButtonManager.
     * @param priority The listener priority.
     */
    public void registerOnShutterButtonListener(OnShutterButtonListener listener, int priority) {
        if (listener == null) {
            LogHelper.e(TAG, "registerOnShutterButtonListener error [why null]");
        }
        mShutterButtonListeners.put(mShutterButtonListeners.getPriorityKey(priority, listener),
                listener);
    }

    /**
     * Unregister shutter button listener.
     *
     * @param listener The listener to be unregistered.
     */
    public void unregisterOnShutterButtonListener(OnShutterButtonListener listener) {
        if (listener == null) {
            LogHelper.e(TAG, "unregisterOnShutterButtonListener error [why null]");
        }
        if (mShutterButtonListeners.containsValue(listener)) {
            mShutterButtonListeners.remove(mShutterButtonListeners.findKey(listener));
        }
    }

    /**
     * Register shutter button UI.
     * @param drawable The shutter button icon drawable.
     * @param type The shutter type, such as "Picture" or "Photo", the type will be shown
     *             above the shutter icon as a text.
     * @param priority The shutter ui priority, the smaller the value, the higher the priority.
     *                 the high priority icon will be located on the left.
     */
    public void registerShutterButton(Drawable drawable, String type, int priority) {
        if (mShutterButtons.containsKey(priority)) {
            return;
        }
        ShutterItem item = new ShutterItem();
        item.mShutterDrawable = drawable;
        item.mShutterType = type;
        /*prize-add-add mode-xiaoping-20180901-start*/
        if ("Picture".equals(type)) {

            switch (priority) {
                case 4:
                    item.mShutterName =
                            (String) mApp.getActivity().getResources().getText(R.string.shutter_type_photo_beauty);
                    break;
                case 3:
                    item.mShutterName =
                            (String) mApp.getActivity().getResources().getText(R.string.shutter_type_photo);
                    break;
                case 2:
                    /*prize-modify-Modify the translation of picturezoom for Blu-xiaoping-20190305-start*/
                    if (SystemProperties.getInt("ro.pri.current.project",0) == 1) {
                        item.mShutterName =
                                (String) mApp.getActivity().getResources().getText(R.string.shutter_type_photo_picturezoom_bl);
                    } else {
                        item.mShutterName =
                                (String) mApp.getActivity().getResources().getText(R.string.shutter_type_photo_picturezoom);
                    }
                    /*prize-modify-Modify the translation of picturezoom for Blu-xiaoping-20190305-end*/
                    break;
                case 1:
                    item.mShutterName =
                            (String) mApp.getActivity().getResources().getText(R.string.shutter_type_photo_lowlight);
                    break;
            }
/*            if (priority == 0) {
                item.mShutterName =
                        (String) mApp.getActivity().getResources().getText(R.string.shutter_type_photo_beauty);
            } else {
                item.mShutterName =
                        (String) mApp.getActivity().getResources().getText(R.string.shutter_type_photo);
            }*/
        } else if ("Video".equals(type)) {
            item.mShutterName =
                    (String) mApp.getActivity().getResources().getText(R.string.shutter_type_video);
        }
        /*prize-add-add mode-xiaoping-20180901-start*/
        mShutterButtons.put(priority, item);
    }

    /*prize-modify-add switches to third-party algorithms-xiaoping-20181026-start*/
    /**
     * Register shutter button UI.
     * @param drawable The shutter button icon drawable.
     * @param type The shutter type, such as "Picture" or "Photo", the type will be shown
     *             above the shutter icon as a text.
     * @param priority The shutter ui priority, the smaller the value, the higher the priority.
     *                 the high priority icon will be located on the left.
     * @param mode
     */

    public void registerShutterButton(Drawable drawable, String type, int priority,int mode){
        registerShutterButton(drawable, type, priority, mode, false);
    }

    public void registerShutterButton(Drawable drawable, String type, int priority,int mode, boolean replace) {
        if (mShutterButtons.containsKey(priority)) {
            if(replace){
                mShutterButtons.remove(priority);
            }else{
                return;
            }
        }
        ShutterItem item = new ShutterItem();
        item.mShutterDrawable = drawable;
        item.mShutterType = type;
        switch (mode) {
            case MODE_PHOTO:
                item.mShutterName =
                        (String) mApp.getActivity().getResources().getText(R.string.shutter_type_photo);
                break;
            case MODE_VIDEO:
                item.mShutterName =
                        (String) mApp.getActivity().getResources().getText(R.string.shutter_type_video);
                break;
            case MODE_FACE_BEAUTY:
                item.mShutterName =
                        (String) mApp.getActivity().getResources().getText(R.string.shutter_type_photo_beauty);
                break;
            case MODE_PICTURE_ZOOM:
                /*prize-modify-Modify the translation of picturezoom for Blu-xiaoping-20190305-start*/
                if (SystemProperties.getInt("ro.pri.current.project",0) == 1) {
                    item.mShutterName =
                            (String) mApp.getActivity().getResources().getText(R.string.shutter_type_photo_picturezoom_bl);
                } else {
                    item.mShutterName =
                            (String) mApp.getActivity().getResources().getText(R.string.shutter_type_photo_picturezoom);
                }
                /*prize-modify-Modify the translation of picturezoom for Blu-xiaoping-20190305-end*/
                break;
            case MODE_LOWLIGHT_SHOT:
                item.mShutterName =
                        (String) mApp.getActivity().getResources().getText(R.string.shutter_type_photo_lowlight);
                break;
            case MODE_PICSELFIE:
                item.mShutterName =
                        (String) mApp.getActivity().getResources().getText(R.string.shutter_type_photo_picsefile);
                break;
            case MODE_PROFESSIONAL:
                item.mShutterName =
                        (String) mApp.getActivity().getResources().getText(R.string.pref_camera_professional_title);
                break;
            case MODE_APERTURE:
                item.mShutterName =
                        (String) mApp.getActivity().getResources().getText(R.string.shutter_type_photo_aperture);
                break;
            case MODE_GIF:
                item.mShutterName =
                        (String) mApp.getActivity().getResources().getText(R.string.shutter_type_photo_gif);
                break;
            case MODE_HDR:
                item.mShutterName = (String) mApp.getActivity().getResources().getText(R.string.shutter_type_photo_hdr);
                break;
            case MODE_SCAN:
                item.mShutterName = (String) mApp.getActivity().getResources().getText(R.string.shutter_type_photo_intelligent_scanning);
                break;
            case MODE_FILTER:
                item.mShutterName = (String) mApp.getActivity().getResources().getText(R.string.shutter_type_photo_filter);
                break;
            case MODE_SLOWMOTION:
                item.mShutterName =
                        (String) mApp.getActivity().getResources().getText(R.string.shutter_type_photo_slow_motion);
                break;
            case MODE_PRIZE_PANO:
                item.mShutterName =
                        (String) mApp.getActivity().getResources().getText(R.string.shutter_type_photo_panorama);
                break;
            default:
                break;
        }
        mShutterButtons.put(priority, item);
    }
    /*prize-modify-add switches to third-party algorithms-xiaoping-20181026-end*/

    /**
     * Register shutter done, it will trigger to refresh the ui.
     */
    public void registerDone() {
        ShutterItem shutter;
        ShutterView shutterView;
        ShutterItem prevShutter = null;
        /*prize-modify-adjust the bottom mode layout-xiaoping-20180911-start*/
        ShutterItem firstShutter = null;
        
        if (mShutterLayout.getChildCount() != 0 ) {
//            return;
        }
		
        mShutterLayout.removeAllViews();
        mShutterLayout.cleatModeTitle();
		/*prize-modify-adjust the bottom mode layout-xiaoping-20180911-end*/
        int index = 0;
        //when registerDone, add the shutter view to root layout.
        for (Integer key: mShutterButtons.keySet()) {
            shutter = mShutterButtons.get(key);

                    //inflate the view
            shutterView = (ShutterView) mInflater.inflate(
                    R.layout.shutter_item, mShutterLayout, false);
            shutterView.setType(shutter.mShutterType);
            shutterView.setName(shutter.mShutterName);
            shutterView.setDrawable(shutter.mShutterDrawable);
            shutterView.setId(generateViewId());
            shutterView.setOnShutterTextClickedListener(mShutterLayout);
            shutterView.setTag(index);
            /*prize-modify-bug The photo button can also be clicked during the timed photo taking process-xiaoping-20190409-start*/
            shutterView.setApp(mApp);
            /*prize-modify-bug The photo button can also be clicked during the timed photo taking process-xiaoping-20190409-end*/
            mShutterLayout.addView(shutterView);
            shutterView.setOnShutterButtonListener(mShutterButtonListener);
            shutter.mShutterView = shutterView;
            RelativeLayout.LayoutParams params =
                    (RelativeLayout.LayoutParams) shutterView.getLayoutParams();
            /*prize-modify-adjust the bottom mode layout-xiaoping-20180911-start*/
            if (prevShutter == null) {
                params.addRule(RelativeLayout.CENTER_IN_PARENT);
            } else {
                /*prize-modify-adapt ui of RTL-xiaoping-20181228-start*/
                if (mApp.getAppUi().isRTL()) {
                    params.addRule(RelativeLayout.LEFT_OF, prevShutter.mShutterView.getId());
                } else {
                    params.addRule(RelativeLayout.RIGHT_OF, prevShutter.mShutterView.getId());
                }
                /*prize-modify-adapt ui of RTL-xiaoping-20181228-end*/
            }
            prevShutter = shutter;
            if (index == 0) {
                firstShutter = shutter;
            }
            index++;
        }
        mShutterLayout.initChsView();
		//prize-added by tangan-20190327-fixbug 72564-begin
        int orientation = mApp.getGSensorOrientation();
        CameraUtil.rotateRotateLayoutChildView(mApp.getActivity(), mShutterLayout, orientation, false);
		//prize-added by tangan-20190327-fixbug 72564-end
        /*prize-modify-adjust the bottom mode layout-xiaoping-20180911-end*/
    }

    /**
     * Invoke the onShutterButtonCLicked listener to start a capture event.
     * @param currentPriority Trigger module shutter button listener priority,
     *                        the trigger event will be pass to the modules which shutter listener
     *                        priority is lower than currentPriority value.
     *                        The zero value will pass the click event to all listeners.
     */
    public void triggerShutterButtonClicked(final int currentPriority) {
        // shutter button may be trigger manually by mode or setting,
        // here should judge whether is enabled, if not enabled ignore this trigger.
        if (mVolumekeyState == VolumekeyState.VOLUME_KEY_STATE_DOWN) return;//prize-add-huangpengfei-2018-12-19
        if (isEnabled()) {
            Iterator iterator = mShutterButtonListeners.entrySet().iterator();
            OnShutterButtonListener listener;
            while (iterator.hasNext()) {
                Map.Entry map = (Map.Entry) iterator.next();
                listener = (OnShutterButtonListener) map.getValue();
                int priority = mShutterButtonListeners.getPriorityByKey(
                        (String) map.getKey());
                if (priority > currentPriority
                        && listener != null
                        && listener.onShutterButtonClick()) {
                    /*prize-modify-bugid:68305 capture animation is abnormal under time-lapse photo-xiaoping-20181115-start*/
                    LogHelper.i(TAG,"listener: "+listener);
                    /*prize-modify-bugid:71466 take picture fail on picturezoom mode -xiaoping-20190326-start*/
  /*                  if (listener instanceof PhotoMode && isNeedPlayAnimation()) {
                        if (mUiHandler != null && mUiHandler.hasMessages(START_CAPTURE_ANIMATION)) {
                            mUiHandler.removeMessages(START_CAPTURE_ANIMATION);
                        }
                        mUiHandler.sendEmptyMessageDelayed(START_CAPTURE_ANIMATION,50);
                    }*/
                    /*prize-modify-bugid:71466 take picture fail on picturezoom mode -xiaoping-20190326-end*/
                    /*prize-modify-bugid:68305 capture animation is abnormal under time-lapse photo-xiaoping-20181115-end*/
                    return;
                }
            }
        }
    }

    /**
     * Invoke the triggerShutterButtonLongPressed listener to start a capture event.
     * @param currentPriority Trigger module shutter button listener priority,
     *                        the trigger event will be pass to the modules which shutter listener
     *                        priority is lower than currentPriority value.
     *                        The zero value will pass the click event to all listeners.
     */
    public void triggerShutterButtonLongPressed(final int currentPriority) {
        // shutter button may be trigger manually by mode or setting,
        // here should judge whether is enabled, if not enabled ignore this trigger.
        if (isEnabled()) {
            Iterator iterator = mShutterButtonListeners.entrySet().iterator();
            OnShutterButtonListener listener;
            while (iterator.hasNext()) {
                Map.Entry map = (Map.Entry) iterator.next();
                listener = (OnShutterButtonListener) map.getValue();
                int priority = mShutterButtonListeners.getPriorityByKey(
                        (String) map.getKey());
                if (priority > currentPriority
                        && listener != null
                        && listener.onShutterButtonLongPressed()) {
                    LogHelper.i(TAG,"listener: "+listener);
                    return;
                }
            }
        }
    }

    /*prize-add-huangpengfei-20181121-start*/
    public void stopContinuousShot() {
        Iterator iterator = mShutterButtonListeners.entrySet().iterator();
        OnShutterButtonListener listener;
        while (iterator.hasNext()) {
            Map.Entry map = (Map.Entry) iterator.next();
            listener = (OnShutterButtonListener) map.getValue();
            if (listener != null && listener.onShutterButtonFocus(false)) {
                return;
            }
        }
    }
    /*prize-add-huangpengfei-20181121-end*/

    /**
     * Update current mode support shutter types.
     * @param currentType Current mode type.
     * @param types Support type list.
     */
    public void updateModeSupportType(String currentType, String[] types) {

        for (int i = 0; i < mShutterLayout.getChildCount(); i++) {
            boolean isSupported = false;
            ShutterView shutter = (ShutterView) mShutterLayout.getChildAt(i);
            for (int j = 0; j < types.length; j++) {
                if (types[j].equals(shutter.getType())) {
                    isSupported = true;
                }
            }
            if (isSupported) {
                shutter.setVisibility(View.VISIBLE);
            } else {
                shutter.setVisibility(View.INVISIBLE);
            }
        }

        String targetType = null;

        if (types.length == 1) {
            targetType = types[0];
        } else {
            targetType = currentType;
        }
        LogHelper.d(TAG, "currentType = " + currentType + " targetType = " + targetType);
        for (int i = 0; i < mShutterLayout.getChildCount(); i++) {
            ShutterView shutter = (ShutterView) mShutterLayout.getChildAt(i);
            if (targetType.equals(shutter.getType()) /*&&  shutter.getName().equals(mShutterLayout.getDefaultMode(0))*/) {//prize-tangan-add prize camera-begin
                mShutterLayout.updateCurrentShutterIndex(i);
            }
        }
    }

    /*prize-add-huangpengfei-2018-11-19-start*/
    public void setDefaultShutterIndex(){
        LogHelper.d(TAG,"[setDefaultShutterIndex]");
        for (int i = 0; i < mShutterLayout.getChildCount(); i++) {
            ShutterView shutter = (ShutterView) mShutterLayout.getChildAt(i);
            if (shutter.getName().equals(mShutterLayout.getDefaultMode(mApp.getAppUi().getCameraId()))) {
                mShutterLayout.updateCurrentShutterIndex(i);
            }
        }
    }
    /*prize-add-huangpengfei-2018-11-19-end*/

    /*prize-add for external intent-huangpengfei-2019-2-16-start*/
    public void switchToShutterByIntent(String modeName){
        LogHelper.d(TAG,"[switchToShutterByIntent] modeName = "+modeName);
        if (mShutterLayout != null){
            for (int i = 0; i < mShutterLayout.getChildCount(); i++) {
                ShutterView shutter = (ShutterView) mShutterLayout.getChildAt(i);
                if (shutter.getName().equals(modeName)) {
                    mShutterLayout.updateCurrentShutterIndex(i);
                }
            }
        }
    }

    public void updateModeTitle(){
        mShutterLayout.updateModeTitle();
    }
    /*prize-add for external intent-huangpengfei-2019-2-16-end*/

    /**
     * Update current mode shutter info.
     * @param type Current mode's shutter type.
     * @param drawable Current mode's shutter drawable.
     */
    public void updateCurrentModeShutter(String type, Drawable drawable) {
        if (drawable != null) {
            for (int i = 0; i < mShutterLayout.getChildCount(); i++) {
                ShutterView shutter = (ShutterView) mShutterLayout.getChildAt(i);
                if (shutter.getType().equals(type)) {
                    shutter.setDrawable(drawable);
                }
            }
        } else {
            for (int i = 0; i < mShutterLayout.getChildCount(); i++) {
                ShutterView shutter = (ShutterView) mShutterLayout.getChildAt(i);
                ShutterItem item;
                for (Integer key: mShutterButtons.keySet()) {
                    item = mShutterButtons.get(key);
                    if (shutter.getType().equals(item.mShutterType)) {
                        shutter.setDrawable(item.mShutterDrawable);
                    }
                }
            }
        }
    }

    /**
     * Get shutter root view.
     * @return the shutter root view.
     */
    public View getShutterRootView() {
        return mShutterLayout;
    }

    /**
     * Implementer of {@link OnShutterButtonListener}, receiver the UI event and notify the event
     * by the priority.
     */
    private class ShutterButtonListenerImpl implements ShutterButton.OnShutterButtonListener {

        @Override
        public void onShutterButtonFocused(boolean pressed) {
            if (mVolumekeyState == VolumekeyState.VOLUME_KEY_STATE_DOWN) return;//prize-add-huangpengfei-2018-12-19
            Iterator iterator = mShutterButtonListeners.entrySet().iterator();
            OnShutterButtonListener listener;
            while (iterator.hasNext()) {
                Map.Entry map = (Map.Entry) iterator.next();    
                listener = (OnShutterButtonListener) map.getValue();
                if (listener != null && listener.onShutterButtonFocus(pressed)) {
                    return;
                }
            }
        }

        @Override
        public void onShutterButtonClicked() {
            if (mVolumekeyState == VolumekeyState.VOLUME_KEY_STATE_DOWN) return;//prize-add-huangpengfei-2018-12-19
            Iterator iterator = mShutterButtonListeners.entrySet().iterator();
            OnShutterButtonListener listener;
            while (iterator.hasNext()) {
                Map.Entry map = (Map.Entry) iterator.next();
                listener = (OnShutterButtonListener) map.getValue();
                if (listener != null && listener.onShutterButtonClick()) {
                    mApp.getAppUi().hideSetting(); // zhangguo add 20190506, for new setting style
                    /*prize-modify-add animation of takepictur-xiaoping-20181105-start*/
                    LogHelper.i(TAG,"listener: "+listener);
                    /*prize-modify-bugid:71466 take picture fail on picturezoom mode -xiaoping-20190326-start*/
/*                    if (listener instanceof PhotoMode && "0".equals(mApp.getSettingValue("key_self_timer","0",mApp.getAppUi().getCameraId())) && isNeedPlayAnimation()) {
                        if (mUiHandler != null && mUiHandler.hasMessages(START_CAPTURE_ANIMATION)) {
                            mUiHandler.removeMessages(START_CAPTURE_ANIMATION);
                        }
                        mUiHandler.sendEmptyMessageDelayed(START_CAPTURE_ANIMATION,20);
                    }*/
                    /*prize-modify-bugid:71466 take picture fail on picturezoom mode -xiaoping-20190326-end*/
                    /*prize-modify-add animation of takepictur-xiaoping-20181105-end*/
                    return;
                }
            }

        }

        @Override
        public void onShutterButtonLongPressed() {
            if (mVolumekeyState == VolumekeyState.VOLUME_KEY_STATE_DOWN) return;//prize-add-huangpengfei-2018-12-19
            Iterator iterator = mShutterButtonListeners.entrySet().iterator();
            OnShutterButtonListener listener;
            while (iterator.hasNext()) {
                Map.Entry map = (Map.Entry) iterator.next();
                listener = (OnShutterButtonListener) map.getValue();
                if (listener != null && listener.onShutterButtonLongPressed()) {
                    LogHelper.i(TAG,"listener: "+listener);
                    return;
                }
            }
        }
    }
    private static final AtomicInteger sNextGenerateId = new AtomicInteger(1);

    private static int generateViewId() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            for (; ; ) {
                final int result = sNextGenerateId.get();
                //aapt-generated IDs have the high byte nonzero; clamp to the range under that.
                int newValue = result + 1;
                if (newValue > 0x00FFFFFF) {
                    newValue = 1; //Roll over to 1, not 0.
                }
                if (sNextGenerateId.compareAndSet(result, newValue)) {
                    return result;
                }
            }
        } else {
            return View.generateViewId();
        }
    }

    /*prize-modify-switch mode after switch camera-xiaoping-20180913-start*/
    public void clearShutterButtons() {
        if (mShutterButtons != null && mShutterButtons.size() > 0) {
            mShutterButtons.clear();
        }
    }

    public void onCameraSelected(int newcameraid) {
//        if (newcameraid == 0) {
//           mShutterLayout.setCurrentIndex(BACK_CAMERA_PHOTOMODE_DEFAULT);
//        } else {
//        mShutterLayout.setCurrentIndex(FRONT_CAMERA_FACEBEAUTYMODE_DEFAULT);
//        }
        mShutterLayout.onCameraSelected();
    }
    /*prize-modify-switch mode after switch camera-xiaoping-20180913-end*/

    /*prize-modify-add animation of takepicture-xiaoping-20181105-start*/
    public CaptureAnimation getCaptureAnimation () {
        return mCaptureAnimation;
    }

    public boolean isNeedPlayAnimation () {
        if (((CameraActivity)mApp.getActivity()).isStrorageReady() && (mApp.getAppUi().getModeItem().mModeTitle == IAppUi.ModeTitle.PICTUREZOOM || mApp.getAppUi().getModeItem().mModeTitle == IAppUi.ModeTitle.LOWLIGHT
                || mApp.getAppUi().getModeItem().mModeTitle == IAppUi.ModeTitle.BEAUTY
                || "on".equals(mApp.getAppUi().getHdrValue()) || "on".equals(mApp.getAppUi().getPicsflieValue())
                || mApp.getAppUi().getModeItem().mModeTitle == IAppUi.ModeTitle.FICSEFILE)) {
            return true;
        }
        return false;
    }
    /*prize-modify-add animation of takepicture-xiaoping-20181105-end*/

    /*prize-modify-bugid:68305 capture animation is abnormal under time-lapse photo-xiaoping-20181115-start*/
    private class UiHandler extends Handler{
        public UiHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            LogHelper.i(TAG,""+msg.what);
            switch (msg.what) {
                case START_CAPTURE_ANIMATION:
                    if (isNeedPlayAnimation()) {
                        mShutterLayout.setVisibility(View.INVISIBLE,true);
                        mCaptureAnimation.setVisibility(View.VISIBLE);
                        mCaptureAnimation.start();
                        mUiHandler.sendEmptyMessageDelayed(FORCE_STOP_ANIMATION,10000);
                    }
                    break;
                case FORCE_STOP_ANIMATION:
                    if (mCaptureAnimation != null && mCaptureAnimation.getVisibility() == View.VISIBLE) {
                        LogHelper.w(TAG,"Photographed image not generated, forced end capture animation");
                        mApp.getAppUi().revetButtonState();
                    }
                    break;
                default:
                    break;
            }
        }
    }
    /*prize-modify-bugid:68305 capture animation is abnormal under time-lapse photo-xiaoping-20181115-end*/

    /*prize-modify-opt-optimize the photo animation process-xiaoping-20181122-start*/
    public void removeMessageOfStopAnimation() {
        if (mUiHandler != null && mUiHandler.hasMessages(FORCE_STOP_ANIMATION)) {
            mUiHandler.removeMessages(FORCE_STOP_ANIMATION);
        }
    }

    @Override
    public void setVisibility(int visibility) {
        /*prize-modify-bug the bottom camera shutter button is hidden in the countdown state-xiaoping-20190325-start*/
        if (mApp != null && mApp.getAppUi().getSelfTimerState() && visibility != View.VISIBLE) {
            mShutterLayout.setChsViewVisilibity(visibility);
            /*prize-modify-bug the bottom camera shutter button is hidden in the countdown state-xiaoping-20190325-end*/
        } else {
            if (visibility == View.VISIBLE && mCaptureAnimation != null && mCaptureAnimation.getVisibility() == View.VISIBLE) {
                LogHelper.w(TAG,"we need to display the camera button, but the camera animation is still displayed, so hide");
                mApp.getAppUi().revetButtonState();
            }
            super.setVisibility(visibility);
        }
    }
    /*prize-modify-opt-optimize the photo animation process-xiaoping-20181122-end*/

    /*prize-modify-bugid:71466 take picture fail on picturezoom mode -xiaoping-20190326-start*/
    public void startCaptureAnimation() {
        if (mUiHandler != null && mUiHandler.hasMessages(START_CAPTURE_ANIMATION)) {
            mUiHandler.removeMessages(START_CAPTURE_ANIMATION);
        }
        mUiHandler.sendEmptyMessage(START_CAPTURE_ANIMATION);
    }
    /*prize-modify-bugid:71466 take picture fail on picturezoom mode -xiaoping-20190326-end*/

    public void scrollToLastShutter(){
        if(null != mShutterLayout && mShutterLayout.needScrollToPlugin()){
            ShutterView shutter = (ShutterView) mShutterLayout.getChildAt(mShutterLayout.getChildCount() - 1);
            mShutterLayout.updateCurrentShutterIndex((Integer) shutter.getTag());
        }
    }
}
