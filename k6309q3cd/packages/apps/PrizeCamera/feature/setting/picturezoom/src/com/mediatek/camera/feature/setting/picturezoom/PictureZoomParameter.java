package com.mediatek.camera.feature.setting.picturezoom;

import com.mediatek.camera.common.IAppUi;
import com.mediatek.camera.common.ICameraContext;
import com.mediatek.camera.common.app.IApp;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.mode.ICameraMode;
import com.mediatek.camera.common.mode.picturezoom.PictureZoomMode;
import com.mediatek.camera.common.setting.ISettingManager;
import com.mediatek.camera.common.setting.SettingBase;

public class PictureZoomParameter extends SettingBase {
    private static final String PICTUREZOOM_MODE_KEY = "picturezoom_key";
    private ISettingChangeRequester mSettingChangeRequester;
    private ICaptureRequestConfigure mPictureZoomRequestConfigure;

    /*prize-add for model merging-huangpengfei-2019-03-25-start*/
    @Override
    public void init(IApp app, ICameraContext cameraContext, ISettingManager.SettingController settingController) {
        super.init(app, cameraContext, settingController);
        if (mAppUi.getModeItem().mModeTitle == IAppUi.ModeTitle.PICTUREZOOM) {
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
        return PICTUREZOOM_MODE_KEY;
    }

    @Override
    public IParametersConfigure getParametersConfigure() {
        return null;
    }

    @Override
    public ICaptureRequestConfigure getCaptureRequestConfigure() {
        if (mPictureZoomRequestConfigure == null) {
            mPictureZoomRequestConfigure = new PictureZoomParameterCaptureRequestConfig(mSettingDevice2Requester,this);
        }
        mSettingChangeRequester = mPictureZoomRequestConfigure;
        return mPictureZoomRequestConfigure;
    }

    @Override
    public void onModeOpened(String modeKey, ICameraMode.ModeType modeType) {
        LogHelper.i("",""+modeKey+",modeType: "+modeType);
        super.onModeOpened(modeKey, modeType);
        if ( PictureZoomMode.class.getName().equals(modeKey)) {
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
        LogHelper.i("",""+modeKey);
        super.onModeClosed(modeKey);
        if ( PictureZoomMode.class.getName().equals(modeKey) || "on".equals(getValue())) {
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
        return mDataStore.getValue(getKey(),"not value",getStoreScope());
    }
}
