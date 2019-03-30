package com.mediatek.camera.feature.setting.lowlight;

import com.mediatek.camera.common.IAppUi;
import com.mediatek.camera.common.ICameraContext;
import com.mediatek.camera.common.app.IApp;
import com.mediatek.camera.common.mode.ICameraMode;
import com.mediatek.camera.common.mode.lowlight.LowLightMode;
import com.mediatek.camera.common.setting.ISettingManager;
import com.mediatek.camera.common.setting.SettingBase;

public class LowLightParameter extends SettingBase {
    private static final String LOWLIGHT_MODE_KEY = "LowLight_key";
    private ISettingChangeRequester mSettingChangeRequester;
    private ICaptureRequestConfigure mLowLightRequestConfigure;

    /*prize-add for model merging-huangpengfei-2019-03-25-start*/
    @Override
    public void init(IApp app, ICameraContext cameraContext, ISettingManager.SettingController settingController) {
        super.init(app, cameraContext, settingController);
        if (mAppUi.getModeItem().mModeTitle == IAppUi.ModeTitle.LOWLIGHT) {
            mDataStore.setValue(getKey(),"on",getStoreScope(),true);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mSettingChangeRequester != null) {
                        mSettingChangeRequester.sendSettingChangeRequest();
                    }
                }
            });
        }
    }
    /*prize-add for model merging-huangpengfei-2019-03-25-end*/

    @Override
    public void unInit() {

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
        return LOWLIGHT_MODE_KEY;
    }

    @Override
    public IParametersConfigure getParametersConfigure() {
        return null;
    }

    @Override
    public ICaptureRequestConfigure getCaptureRequestConfigure() {
        if (mLowLightRequestConfigure == null) {
            mLowLightRequestConfigure = new LowLightParameterCaptureRequestConfig(this,mSettingDevice2Requester);
        }
        mSettingChangeRequester = mLowLightRequestConfigure;
        return mLowLightRequestConfigure;
    }

    @Override
    public void onModeOpened(String modeKey, ICameraMode.ModeType modeType) {
        super.onModeOpened(modeKey, modeType);
        if ( LowLightMode.class.getName().equals(modeKey)) {
            mDataStore.setValue(getKey(),"on",getStoreScope(),true);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mSettingChangeRequester != null) {
                        mSettingChangeRequester.sendSettingChangeRequest();
                    }
                }
            });
        }
    }

    @Override
    public synchronized void onModeClosed(String modeKey) {
        super.onModeClosed(modeKey);
        if ( LowLightMode.class.getName().equals(modeKey) || "on".equals(getValue())) {
            mDataStore.setValue(getKey(),"off",getStoreScope(),true);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mSettingChangeRequester != null) {
                        mSettingChangeRequester.sendSettingChangeRequest();
                    }
                }
            });
        }
    }

    @Override
    public synchronized String getValue() {
        return mDataStore.getValue(getKey(),"not find",getStoreScope());
    }
}
