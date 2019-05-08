package com.mediatek.camera.feature.setting.picselfie;

import com.mediatek.camera.common.IAppUi;
import com.mediatek.camera.common.ICameraContext;
import com.mediatek.camera.common.app.IApp;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.mode.ICameraMode;
import com.mediatek.camera.common.mode.photo.PhotoMode;
import com.mediatek.camera.common.mode.photo.PhotoModeEntry;
import com.mediatek.camera.common.mode.picselfie.PicselfieMode;
import com.mediatek.camera.common.mode.picselfie.PicselfieModeEntry;
import com.mediatek.camera.common.relation.StatusMonitor;
import com.mediatek.camera.common.setting.ISettingManager;
import com.mediatek.camera.common.setting.SettingBase;
import com.mediatek.camera.feature.mode.vsdof.photo.SdofPhotoEntry;
import com.mediatek.camera.prize.FeatureSwitcher;

public class PicselfieParameter extends SettingBase implements IAppUi.UVPicselfieCallback{
    private static final LogUtil.Tag TAG = new LogUtil.Tag(PicselfieParameter.class.getSimpleName());
    private static final String PICSEFILE_KEY = "key_picsefile";
    private ISettingChangeRequester mSettingChangeRequester;
    private ICaptureRequestConfigure mPicselfieRequestConfigure;
    private PicselfieParameterViewController mPicselfieParameterViewController;
    /*prize-modify-bugid:67420 turn off HDR when opening beauty mode -xiaoping-20181031-start*/
    private static final String PICSEFILE_SWITCH = "picsefile_switch";
    /*prize-modify-bugid:67420 turn off HDR when opening beauty mode -xiaoping-20181031-end*/
    private String KEY_PICSELFIE = "status_key_picselfie";
    private String KEY_AI_STATUS = "status_key_ai_status";
    private String KEY_RESTORE_SETTINGS = "key_restore_settings";
    private StatusMonitor.StatusResponder mPicselfieStatusResponder;

    @Override
    public void unInit() {
        LogHelper.i("","....");
        mPicselfieParameterViewController = null;
        mPicselfieRequestConfigure = null;
        mStatusMonitor.unregisterValueChangedListener(KEY_AI_STATUS, mStatusListener);
        mStatusMonitor.unregisterValueChangedListener(KEY_RESTORE_SETTINGS, mStatusListener);
    }

    @Override
    public void init(IApp app, ICameraContext cameraContext, ISettingManager.SettingController settingController) {
        LogHelper.i("","init,mApp: "+mApp+",settingController: "+settingController);
        super.init(app, cameraContext, settingController);
        if (mPicselfieParameterViewController == null) {
            mPicselfieParameterViewController = new PicselfieParameterViewController(this,app);
        }
        mPicselfieStatusResponder = mStatusMonitor.getStatusResponder(KEY_PICSELFIE);
        mStatusMonitor.registerValueChangedListener(KEY_AI_STATUS, mStatusListener);
        mStatusMonitor.registerValueChangedListener(KEY_RESTORE_SETTINGS, mStatusListener);
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
        return PICSEFILE_KEY;
    }

    @Override
    public IParametersConfigure getParametersConfigure() {
        return null;
    }

    @Override
    public ICaptureRequestConfigure getCaptureRequestConfigure() {
        if (mPicselfieRequestConfigure == null) {
            mPicselfieRequestConfigure = new PicselfieParameterRequestConfig(mSettingDevice2Requester,this, mApp);
        }
        mSettingChangeRequester = mPicselfieRequestConfigure;
        return mPicselfieRequestConfigure;
    }

    public void setPicselfieValueByClick(String value){
        setPicsefileValue(value);
        setPicsefileSwitch(value);
        mPicselfieStatusResponder.statusChanged(KEY_PICSELFIE, value);
    }

    public void setPicsefileValue (String value) {

        mDataStore.setValue(getKey(),value,getStoreScope(),true);
        /*prize-modify-add animation of takepicture-xiaoping-20181105-start*/
        mAppUi.setPicsflieValue(value);
        /*prize-modify-add animation of takepicture-xiaoping-20181105-end*/
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mSettingChangeRequester != null) {
                    mSettingChangeRequester.sendSettingChangeRequest();
                }
            }
        });

        if (FeatureSwitcher.isSupportDualCam()) {
            /*prize-modify-bugid:75473 Limit preview size in portrait mode-xiaoping-2019056-start*/
            if (0 == getCameraId()) {
                if ("on".equals(value) && mAppUi.getModeItem() != null && mAppUi.getModeItem().mModeTitle != IAppUi.ModeTitle.FICSEFILE) {
                    mApp.getAppUi().selectPluginMode(SdofPhotoEntry.class.getName(), false, false);
                } else if ("off".equals(value) && mAppUi.getModeItem() != null && mAppUi.getModeItem().mModeTitle == IAppUi.ModeTitle.FICSEFILE) {
                    mApp.getAppUi().selectPluginMode(PhotoModeEntry.class.getName(), false, false);
                }
            } else if (1 == getCameraId()) {
                if ("on".equals(value) && mAppUi.getModeItem() != null && mAppUi.getModeItem().mModeTitle == IAppUi.ModeTitle.PHOTO) {
                    mApp.getAppUi().selectPluginMode(PicselfieModeEntry.class.getName(), false, false);
                } else if ("off".equals(value) && mAppUi.getModeItem() != null && mAppUi.getModeItem().mModeTitle == IAppUi.ModeTitle.FICSEFILE) {
                    mApp.getAppUi().selectPluginMode(PhotoModeEntry.class.getName(), false, false);
                }
            }
            /*prize-modify-bugid:75473 Limit preview size in portrait mode-xiaoping-2019056-end*/
        } else {
            if (FeatureSwitcher.isSupportUVSelfie()) {
                if (mAppUi.getModeItem() != null) {
                    boolean show = 0 == getCameraId() && "on".equals(value);
                    if(mAppUi.getModeItem().mModeTitle == IAppUi.ModeTitle.FICSEFILE){
                        if(!show){
                            mApp.getAppUi().selectPluginMode(PhotoModeEntry.class.getName(), false, false);
                        }
                    }else if(mAppUi.getModeItem().mModeTitle == IAppUi.ModeTitle.PHOTO){
                        if(show){
                            mApp.getAppUi().selectPluginMode(PicselfieModeEntry.class.getName(), false, false);
                        }
                    }

                    mAppUi.showBlurView(show, this);
                }
                /*prize-modify-bugid:75473 Limit preview size in portrait mode-xiaoping-2019056-start*/
            } else if (FeatureSwitcher.isPortraitupported()) {
                if ("on".equals(value) && mAppUi.getModeItem() != null && mAppUi.getModeItem().mModeTitle == IAppUi.ModeTitle.PHOTO) {
                    mApp.getAppUi().selectPluginMode(PicselfieModeEntry.class.getName(), false, false);
                } else if ("off".equals(value) && mAppUi.getModeItem() != null && mAppUi.getModeItem().mModeTitle == IAppUi.ModeTitle.FICSEFILE) {
                    mApp.getAppUi().selectPluginMode(PhotoModeEntry.class.getName(), false, false);
                }
                /*prize-modify-bugid:75473 Limit preview size in portrait mode-xiaoping-2019056-end*/
            }
        }
    }

    @Override
    public synchronized String getValue() {
        /*prize-modify-add portrait mode -xiaoping-20181212-start*/
        return /*mDataStore.getValue(getKey(),"off",getStoreScope())*/mAppUi.getPicsflieValue();
        /*prize-modify-add portrait mode -xiaoping-20181212-end*/
    }

    @Override
    public void onModeOpened(String modeKey, ICameraMode.ModeType modeType) {
        LogHelper.i("","Picselfie modeKey: "+modeKey+",modeType: "+modeType+",key cameraid: "+",getCameraId: "+getCameraId());
        super.onModeOpened(modeKey, modeType);
        if (PicselfieMode.class.getName().equals(modeKey)) {
            setPicsefileValue("on");
        } else if (PhotoMode.class.getName().equals(modeKey)) {
            setPicsefileValue(getPicsefileSwitchValue());
        } else  {
            setPicsefileValue("off");
            //mPicselfieParameterViewController.setHidePicselfieButton();
        }

        if(null != mPicselfieParameterViewController){
            if (mAppUi.getModeItem().mModeTitle == IAppUi.ModeTitle.PHOTO || mAppUi.getModeItem().mModeTitle == IAppUi.ModeTitle.FICSEFILE) {
                mPicselfieParameterViewController.showPicSelfieIndicator(true);
            }else{
                mPicselfieParameterViewController.showPicSelfieIndicator(false);
            }
        }
    }

    @Override
    public synchronized void onModeClosed(String modeKey) {
        super.onModeClosed(modeKey);
        /*if (PicselfieMode.class.getName().equals(modeKey)) {
            setPicsefileValue("off");
        } else */if (PhotoMode.class.getName().equals(modeKey) || "on".equals(getValue())) {
            /*prize-modify-bugid:68372 NullPointerException-huangpengfei-20181116-start*/
            if (mPicselfieParameterViewController == null){
                LogHelper.d(TAG,"[onModeClosed]  mPicselfieParameterViewController = null  return...");
                return;
            }
            /*prize-modify-bugid:68372 NullPointerException-huangpengfei-20181116-end*/
            //mPicselfieParameterViewController.setHidePicselfieButton();
        }
    }

    /*prize-modify-bugid:67420 turn off HDR when opening beauty mode -xiaoping-20181031-start*/
    public void setPicsefileSwitch(String value) {
        mDataStore.setValue(PICSEFILE_SWITCH, value, getStoreScope(),true);
    }

    public String getPicsefileSwitchValue() {
        return mDataStore.getValue(PICSEFILE_SWITCH,"off", getStoreScope());
    }
    /*prize-modify-bugid:67420 turn off HDR when opening beauty mode -xiaoping-20181031-end*/

    @Override
    public void addViewEntry() {
        LogHelper.d(TAG, "[addViewEntry]");
        if(null != mPicselfieParameterViewController){
            if (mAppUi.getModeItem().mModeTitle == IAppUi.ModeTitle.PHOTO || mAppUi.getModeItem().mModeTitle == IAppUi.ModeTitle.FICSEFILE) {
                mPicselfieParameterViewController.showPicSelfieIndicator(true);
            }else{
                mPicselfieParameterViewController.showPicSelfieIndicator(false);
            }
        }
    }

    @Override
    public void refreshViewEntry() {
        super.refreshViewEntry();

        if(null != mPicselfieParameterViewController){
            if (mAppUi.getModeItem().mModeTitle == IAppUi.ModeTitle.PHOTO || mAppUi.getModeItem().mModeTitle == IAppUi.ModeTitle.FICSEFILE) {
                mPicselfieParameterViewController.showPicSelfieIndicator(true);
            }else{
                mPicselfieParameterViewController.showPicSelfieIndicator(false);
            }
        }
    }

    @Override
    public void removeViewEntry() {
        LogHelper.d(TAG, "[removeViewEntry]");
        if(null != mPicselfieParameterViewController){
            mPicselfieParameterViewController.showPicSelfieIndicator(false);
        }
    }

    public int getCameraId() {
        int cameraId = Integer.parseInt(mSettingController.getCameraId());
        return cameraId;
    }

    @Override
    public void onPicselfieDataChanged() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mSettingChangeRequester != null) {
                    mSettingChangeRequester.sendSettingChangeRequest();
                }
            }
        });
    }

    private StatusMonitor.StatusChangeListener mStatusListener = new StatusMonitor.StatusChangeListener() {
        @Override
        public void onStatusChanged(String key, String value) {
            LogHelper.d(TAG, "mStatusListener, key: " + key + ", value: " + value);
            if (KEY_AI_STATUS.equalsIgnoreCase(key)){
                if ("on".equals(value) && null != mPicselfieParameterViewController){
                    mPicselfieParameterViewController.onAiTurnsOn();
                }
            }else if(KEY_RESTORE_SETTINGS.equalsIgnoreCase(key)){
                if(null != mPicselfieParameterViewController){
                    mPicselfieParameterViewController.restoreSettings();
                }
            }
        }
    };
}
