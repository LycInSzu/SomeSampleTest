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

package com.mediatek.camera.feature.setting;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.View;

import com.mediatek.camera.R;
import com.mediatek.camera.common.IAppUi;
import com.mediatek.camera.common.IAppUiListener;
import com.mediatek.camera.common.ICameraContext;
import com.mediatek.camera.common.app.IApp;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.memory.IMemoryManager;
import com.mediatek.camera.common.memory.MemoryManagerImpl;
import com.mediatek.camera.common.mode.ICameraMode;
import com.mediatek.camera.common.relation.Relation;
import com.mediatek.camera.common.setting.ISettingManager;
import com.mediatek.camera.common.setting.SettingBase;
import com.mediatek.camera.common.storage.IStorageService;
import com.mediatek.camera.common.storage.MediaSaver;
import com.mediatek.camera.common.utils.BitmapCreator;
import com.mediatek.camera.common.utils.CameraUtil;
import com.mediatek.camera.common.utils.Size;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import android.os.SystemProperties;
import static com.mediatek.camera.common.relation.StatusMonitor.*;

/**
 * control the continuous shot main flow.
 */

class ContinuousShotBase extends SettingBase implements
        IAppUiListener.OnShutterButtonListener, IMemoryManager.IMemoryListener {
    private static final LogUtil.Tag TAG = new LogUtil.Tag(
            ContinuousShotBase.class.getSimpleName());
    private static final String KEY_ZSD = "key_zsd";
    private static final String KEY_FOCUS = "key_focus_state";
    private static final String KEY_PICTURE_SIZE = "key_picture_size";
    private static final String KEY_SCENE_MODE = "key_scene_mode";
    private static final String VALUE_OF_AUTO_SCENE_DETECTION = "auto-scene-detection";
    protected static final String KEY_CSHOT = "key_continuous_shot";
    protected static final String KEY_CSHOT_START = "start";
    protected static final String KEY_CSHOT_STOP = "stop";
    protected static final String CONTINUOUSSHOT_ON = "on";
    protected static final String CONTINUOUSSHOT_OFF = "off";
    protected static final int SHUTTER_BUTTON_PRIORITY = 50;
    protected Byte JPEG_QUALITY_VALUE = 90;
    protected static  int MAX_CAPTURE_NUMBER = 20;

    private final Object mNumlock = new Object();
    private volatile int mCurrentShotsNum;
    private volatile int mSavedNum;
    private long mShutterTime = 0;
    private long mFreeStorageForCapture;
    private boolean mIsLongPressed = false;
    private boolean mIsCshotDone = true;
    private volatile boolean mIsCshotStopped = true;
    private String mFileDirectory;
    private CsNamingRule mCsNamingRule;
    private CaptureSound mCaptureSound;
    private MemoryManagerImpl mMemoryManager;
    private ICameraContext mCameraContext;
    private ContinuousShotView mContinuousShotView;
    private ICameraMode.ModeType mModeType = ICameraMode.ModeType.PHOTO;
    /*prize-modify-add message of can not ContinuousShot-xiaoping-20181015-start*/
    private IAppUi.HintInfo mCanNotContinusShotHint;
    /*prize-modify-add message of can not ContinuousShot-xiaoping-20181015-end*/
    private ArrayList<String> mSavingList = new ArrayList<>();
    @Override
    public void init(IApp app, ICameraContext cameraContext,
                     ISettingManager.SettingController settingController) {
        super.init(app, cameraContext, settingController);
        LogHelper.d(TAG, "[init]");
        mCameraContext = cameraContext;
        mFileDirectory = mCameraContext.getStorageService().getFileDirectory();
        mCsNamingRule = new CsNamingRule();
        mCaptureSound = new CaptureSound(app.getActivity());
        mCaptureSound.load();
        mMemoryManager = new MemoryManagerImpl(app.getActivity());
        mContinuousShotView = new ContinuousShotView();
        mContinuousShotView.initIndicatorView(app);
        mStatusMonitor.registerValueChangedListener(KEY_FOCUS, mStatusListener);
        /*prize-modify-add message of can not ContinuousShot-xiaoping-20181015-start*/
        mCanNotContinusShotHint = new IAppUi.HintInfo();
        mCanNotContinusShotHint.mDelayTime = 3000;
        int id = mApp.getActivity().getResources().getIdentifier("hint_text_background",
                "drawable", mApp.getActivity().getPackageName());
        mCanNotContinusShotHint.mBackground = mActivity.getDrawable(id);
        mCanNotContinusShotHint.mType = IAppUi.HintType.TYPE_AUTO_HIDE;
        mCanNotContinusShotHint.mHintText = mActivity.getString(
                R.string.cannot_continus_shot_hint);
        /*prize-modify-add message of can not ContinuousShot-xiaoping-20181015-end*/
    }

    @Override
    public void unInit() {
        LogHelper.d(TAG, "[unInit]");
        mAppUi.unregisterOnShutterButtonListener(this);
        mCaptureSound.release();
        dismissSavingProcess();
        mContinuousShotView.clearIndicatorAllMessage();
        mContinuousShotView.unInitIndicatorView();
        mStatusMonitor.unregisterValueChangedListener(KEY_FOCUS, mStatusListener);
    }

    @Override
    public void postRestrictionAfterInitialized() {

    }

    @Override
    public SettingType getSettingType() {
        return SettingType.PHOTO;
    }

    @Override
    public String getKey() {
        return KEY_CSHOT;
    }

    @Override
    public IParametersConfigure getParametersConfigure() {
        return null;
    }

    @Override
    public ICaptureRequestConfigure getCaptureRequestConfigure() {
        return null;
    }

    @Override
    public void onModeOpened(String modeKey, ICameraMode.ModeType modeType) {
        super.onModeOpened(modeKey, modeType);
        LogHelper.d(TAG, "[onModeOpened] modeType = " + modeType);
        mModeType = modeType;
        if (ICameraMode.ModeType.PHOTO == modeType) {
            mAppUi.registerOnShutterButtonListener(this, SHUTTER_BUTTON_PRIORITY);
        }
    }

    @Override
    public void onModeClosed(String modeKey) {
        super.onModeClosed(modeKey);
        LogHelper.d(TAG, "[onModeClosed]");
        //error handler? how about wait for jpeg
        mIsCshotStopped = true;
        mContinuousShotView.clearIndicatorMessage(
                ContinuousShotView.SHOW_CONTINUOUS_SHOT_VIEW);
        mContinuousShotView.hideIndicatorView();
        mMemoryManager.removeListener(this);
        mCameraContext.getStorageService().unRegisterStorageStateListener(
                mStorageStateListener);
        dismissSavingProcess();
        mAppUi.unregisterOnShutterButtonListener(this);
    }

    @Override
    public void onMemoryStateChanged(IMemoryManager.MemoryAction state) {
        LogHelper.d(TAG, "[onMemoryStateChanged] memory state = " + state);
        switch (state) {
            case ADJUST_SPEED:
                slowDownContinuousShot();
                break;
            case STOP:
                stopContinuousShot();
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onShutterButtonFocus(boolean pressed) {
        //if release shutter button, stop continuous shot.
        LogHelper.d(TAG, "[onShutterButtonFocus], pressed = " + pressed);
        if (!pressed && ICameraMode.ModeType.PHOTO == mModeType && mIsLongPressed) {
            //all files captured, notify 3a cshot stopped
            mStatusResponder.statusChanged(KEY_CSHOT, KEY_CSHOT_STOP);
            stopContinuousShot();
            mIsLongPressed = false;
            return true;
        }
        return false;
    }

    @Override
    public boolean onShutterButtonClick() {
        LogHelper.d(TAG, "[onShutterButtonClick]");
        return false;
    }

    @Override
    public boolean onShutterButtonLongPressed() {
        // start continuous shot
        if (ICameraMode.ModeType.PHOTO == mModeType && isReadyForCapture()) {
            LogHelper.d(TAG, "[onShutterButtonLongPressed]");
            //notify 3a cshot started
            mIsLongPressed = true;
            /*prize-modify-add message of can not ContinuousShot-xiaoping-20181015-start*/
            if (mAppUi.getCameraId() == 0 && mAppUi.getModeItem().mModeTitle == IAppUi.ModeTitle.PHOTO &&  "off".equals(mDataStore.getValue("key_hdr","off",getStoreScope()))) {
                /*prize-modify-bug:69847 turn off the flash during continuous shooting-xiaoping-20181226-start*/
                mDataStore.setValue(KEY_CSHOT,"on",getStoreScope(),true);
                /*prize-modify-bug:69847 turn off the flash during continuous shooting-xiaoping-20181226-end*/
                mStatusResponder.statusChanged(KEY_CSHOT, KEY_CSHOT_START);
            } else {
                mApp.getAppUi().showScreenHint(mCanNotContinusShotHint,(int) mActivity.getResources().getDimension(R.dimen.default_hint_margintop));
            }
            /*prize-modify-add message of can not ContinuousShot-xiaoping-20181015-end*/
            return true;
        }
        return false;
    }

    protected ICameraMode.ModeType getModeType() {
        return mModeType;
    }

    private StatusChangeListener mStatusListener = new StatusChangeListener() {
        @Override
        public void onStatusChanged(String key, String value) {
            LogHelper.d(TAG, "mStatusListener, key: " + key + ", value: " + value);
            //if focused and long pressed, do cshot
            if (KEY_FOCUS.equalsIgnoreCase(key) && mIsLongPressed
                    && ("ACTIVE_FOCUSED".equalsIgnoreCase(value)
                    || "ACTIVE_UNFOCUSED".equalsIgnoreCase(value))) {
                startContinuousShot();
            }
        }
    };

    protected boolean startContinuousShot() {
        // send start restriction
        return false;
    }

    protected boolean stopContinuousShot() {
        // send stop restriction
        return false;
    }

    protected void onContinuousShotStarted() {
        synchronized (mNumlock) {
            mCurrentShotsNum = 0;
            mSavedNum = 0;
        }
        mIsCshotStopped = false;
        mShutterTime = System.currentTimeMillis();
        mSavingList.add(KEY_CSHOT_START);
        disableAllUIExceptShutter();
        mContinuousShotView.setIndicatorViewOrientation(mApp.getGSensorOrientation());
        mApp.disableGSensorOrientation();
        mApp.getAppUi().onContinuousShotStarted();//prize-add-huangpengfei-20190314
        /*prize-modify-bugid:70941-Select external sd card to record video error-xiaoping-20190124-start*/
        String defaultvalue = "phone";
        if (SystemProperties.getInt("ro.pri.current.project",0) == 3) {
            defaultvalue = "sd";
        }
        if (mApp != null && mApp.isExtranelStorageMount()/* && mApp.isHasPermissionForSD() modify for bug:71247 xiaoping*/
                && "sd".equals(mApp.getSettingValue("key_storagepath",defaultvalue,mApp.getAppUi().getCameraId()))) {
            mFileDirectory = mApp.getCameraDirectorySD();
        } else {
            mFileDirectory = mCameraContext.getStorageService().getFileDirectory();
        }
        /*prize-modify-bugid:70941-Select external sd card to record video error-xiaoping-20190124-end*/
        mMemoryManager.addListener(this);
        mMemoryManager.initStateForCapture(mFreeStorageForCapture);
        mMemoryManager.initStartTime();
        mCameraContext.getStorageService().registerStorageStateListener(
                mStorageStateListener);
        postRestriction(CONTINUOUSSHOT_ON);
    }

    protected void onContinuousShotStopped() {
        mApp.enableGSensorOrientation();
        mApp.getAppUi().onContinuousShotStopped();//prize-add-huangpengfei-20190314
        mContinuousShotView.hideIndicatorView();
        mIsCshotStopped = true;
        mMemoryManager.removeListener(this);
        mCameraContext.getStorageService().unRegisterStorageStateListener(
                mStorageStateListener);
        postRestriction(CONTINUOUSSHOT_OFF);
    }

    protected void onContinuousShotDone(int captureNum) {
        LogHelper.i(TAG, "onContinuousShotDone(), captureNum = " + captureNum);
        synchronized (mNumlock) {
            mAppUi.applyAllUIEnabled(true);
            mContinuousShotView.clearIndicatorMessage(
                    ContinuousShotView.SHOW_CONTINUOUS_SHOT_VIEW);
            if (mSavedNum < captureNum) {
                showSavingProgress(true);
            } else {
                dismissSavingProcess();
            }
        }
    }

    protected void requestChangeOverrideValues() {

    }

    protected void slowDownContinuousShot() {

    }

    /*prize-fixbug[69861]-huangpengfei-20190102-start*/
    protected void playSound() {
        String cameraMuteValue = mApp.getCameraMuteValue();
        if ("off".equals(cameraMuteValue)) {
            mCaptureSound.play();
        }
    }

    protected void stopSound() {
        String cameraMuteValue = mApp.getCameraMuteValue();
        if ("off".equals(cameraMuteValue)) {
            mCaptureSound.stop();
        }
    }
    /*prize-fixbug[69861]-huangpengfei-20190102-end*/

    protected void disableIndicator() {
        LogHelper.d(TAG, "disableIndicator()");
        mContinuousShotView.disableIndicator();
    }

    protected void initializeValue(boolean isCshotSupported) {
        List<String> supportedList = new ArrayList<>();
        String defaultValue = CONTINUOUSSHOT_OFF;
        if (isCshotSupported) {
            defaultValue = CONTINUOUSSHOT_ON;
        }
        supportedList.add(CONTINUOUSSHOT_ON);
        supportedList.add(CONTINUOUSSHOT_OFF);
        setSupportedPlatformValues(supportedList);
        setSupportedEntryValues(supportedList);
        setEntryValues(supportedList);
        mDataStore.getValue(getKey(), defaultValue, getStoreScope());
        setValue(defaultValue);
    }

    protected void postRestriction(String isOn) {
        Relation baseRalation = ContinuousShotRestriction.getRestriction().getRelation(isOn, true);
        if (CONTINUOUSSHOT_ON.equalsIgnoreCase(isOn)) {
            if (ContinuousShotEntry.mIsBurstMode) {
                baseRalation.addBody(KEY_ZSD, "off", "off, on");
            } else {
                baseRalation.addBody(KEY_ZSD, "on", "off, on");
            }
        }
        mSettingController.postRestriction(baseRalation);
        mSettingController.postRestriction(ContinuousShotRestriction.getFocusUiRestriction()
                .getRelation(isOn, true));
        mSettingController.postRestriction(ContinuousShotRestriction.getFocusSoundRestriction()
                .getRelation(isOn, true));
        if (CONTINUOUSSHOT_ON.equals(isOn)) {
            if (VALUE_OF_AUTO_SCENE_DETECTION
                    .equals(mSettingController.queryValue(KEY_SCENE_MODE))) {
                mSettingController.postRestriction(
                        ContinuousShotRestriction.getAsdRestriction()
                                .getRelation(CONTINUOUSSHOT_ON, true));
            }
        } else {
            mSettingController.postRestriction(
                    ContinuousShotRestriction.getAsdRestriction()
                    .getRelation(CONTINUOUSSHOT_OFF, true));
        }
        mSettingController.refreshViewEntry();
        requestChangeOverrideValues();
    }

    protected void saveJpeg(byte[] data) {
        synchronized (mNumlock) {
            mCurrentShotsNum++;
        }
        LogHelper.i(TAG, "[saveJpeg] data = " + data + ", mCurrentShotsNum = " + mCurrentShotsNum);
        if (data == null) {
            stopContinuousShot();
            return;
        }
        if (!mIsCshotStopped && mCurrentShotsNum <= MAX_CAPTURE_NUMBER) {
            updateThumbnail(data);
            mContinuousShotView.showIndicatorView(mCurrentShotsNum);
        }
        //Save image.
        Size jpegSize = CameraUtil.getSizeFromExif(data);
        /*prize-modify-bugid:70663 Continuous photo does not display photo location information-xiaoping-20190115-start*/
        ContentValues contentValues = mCsNamingRule.createContentValues(data, mFileDirectory,
                jpegSize.getWidth(), jpegSize.getHeight(), mShutterTime, mCurrentShotsNum,mApp);
        /*prize-modify-bugid:70663 Continuous photo does not display photo location information-xiaoping-20190115-end*/
        mCameraContext.getMediaSaver().addSaveRequest(data, contentValues, null,
                mMediaSaverListener);
        if (mCurrentShotsNum == MAX_CAPTURE_NUMBER) {
            stopContinuousShot();
            return;
        }
        //check DVM and system memory
        mMemoryManager.checkContinuousShotMemoryAction(data.length,
                mCameraContext.getMediaSaver().getBytesWaitingToSave());
    }

    protected int getSuitableSpeed() {
        return mMemoryManager.getSuitableSpeed();
    }


    MediaSaver.MediaSaverListener mMediaSaverListener = new MediaSaver.MediaSaverListener() {
        @Override
        public void onFileSaved(Uri uri) {
            synchronized (mNumlock) {
                mSavedNum++;
                LogHelper.d(TAG, "[onFileSaved] uri = " + uri + ", savedNum = " + mSavedNum);
                if (mSavedNum >= mCurrentShotsNum) {
                    dismissSavingProcess();
                }
            }
            mApp.notifyNewMedia(uri, true);
        }
    };

    private boolean isReadyForCapture() {
        if (mSavingList.contains(KEY_CSHOT_START)) {
            return false;
        }
        // check storage space.
        mFreeStorageForCapture = mCameraContext.getStorageService().getCaptureStorageSpace();
        if (mFreeStorageForCapture <= 0) {
            LogHelper.w(TAG, "[isReadyForCapture] there is not enough storage space!");
            return false;
        }
        //check picture size
        String pictureSize = mSettingController.queryValue(KEY_PICTURE_SIZE);
        if (pictureSize == null) {
            LogHelper.w(TAG, "[isReadyForCapture] there is no picture size,need check");
            return false;
        }
        return true;
    }

    private IStorageService.IStorageStateListener mStorageStateListener =
            new IStorageService.IStorageStateListener() {
                @Override
                public void onStateChanged(int storageState, Intent intent) {
                    if (Intent.ACTION_MEDIA_EJECT.equals(intent.getAction())) {
                        LogHelper.i(
                                TAG,
                                "[onStateChanged] storage out service Intent.ACTION_MEDIA_EJECT");
                        stopContinuousShot();
                    }
                }
            };

    private void updateThumbnail(byte[] bytes) {
        LogHelper.d(TAG, "updateThumbnail()");
        Bitmap bitmap = BitmapCreator.createBitmapFromJpeg(bytes, mApp.getAppUi()
                .getThumbnailViewWidth());
        mApp.getAppUi().updateThumbnail(bitmap);
    }

    private void showSavingProgress(boolean isShotDone) {
        synchronized (mSavingList) {
            if (mCurrentShotsNum == 0 || !mSavingList.contains(KEY_CSHOT_START)) {
                return;
            }
            String message = null;
            if (isShotDone) {
                //show the message such as:saving 99 ....
                message = String.format(Locale.ENGLISH, mActivity.getString(R.string
                        .continuous_saving_pictures), mCurrentShotsNum);
            }
            LogHelper.d(TAG, "[showSavingProgress],isShotDone = "
                    + isShotDone + ",msg = " + message);
            mAppUi.showSavingDialog(message, true);
            mAppUi.applyAllUIVisibility(View.INVISIBLE);
        }
    }

    private void dismissSavingProcess() {
        synchronized (mSavingList) {
            if (!mSavingList.contains(KEY_CSHOT_START)) {
                return;
            }
            mSavingList.clear();
            LogHelper.d(TAG, "[dismissSavingProcess]");
            mAppUi.hideSavingDialog();
            mAppUi.applyAllUIVisibility(View.VISIBLE);
        }
    }

    private void disableAllUIExceptShutter() {
        mAppUi.applyAllUIEnabled(false);
        mAppUi.setUIEnabled(IAppUi.SHUTTER_BUTTON, true);
        mAppUi.setUIEnabled(IAppUi.SHUTTER_TEXT, false);
    }
}
