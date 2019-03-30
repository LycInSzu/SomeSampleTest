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


    @Override
    public void unInit() {
        LogHelper.i("","....");
        mPicselfieParameterViewController = null;
        mPicselfieRequestConfigure = null;
    }

    @Override
    public void init(IApp app, ICameraContext cameraContext, ISettingManager.SettingController settingController) {
        LogHelper.i("","init,mApp: "+mApp+",settingController: "+settingController);
        super.init(app, cameraContext, settingController);
        if (mPicselfieParameterViewController == null) {
            mPicselfieParameterViewController = new PicselfieParameterViewController(this,app);
        }
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

        if(FeatureSwitcher.isSupportDualCam() && "0".equals(getCameraId())){
            if("on".equals(value)){
                mAppUi.selectPluginMode(SdofPhotoEntry.class.getName(), false);
            }else{
                mAppUi.selectPluginMode(PhotoModeEntry.class.getName(), false);
            }
        }
    }

    public void setPicsefileValue (String value) {

        mDataStore.setValue(getKey(),value,getStoreScope(),true);
        /*prize-modify-add animation of takepicture-xiaoping-20181105-start*/
        mAppUi.setPicsflieValue(value);
        /*prize-modify-add animation of takepicture-xiaoping-20181105-end*/

        if(!FeatureSwitcher.isSupportDualCam() || "1".equals(getCameraId())){
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mSettingChangeRequester != null) {
                        mSettingChangeRequester.sendSettingChangeRequest();
                    }
                }
            });

            boolean show = 0 == getCameraId() && "on".equals(value);
            mAppUi.showBlurView(show, this);
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

        if (mAppUi.getModeItem().mModeTitle == IAppUi.ModeTitle.PHOTO || mAppUi.getModeItem().mModeTitle == IAppUi.ModeTitle.APERTURE) {
            mPicselfieParameterViewController.showPicSelfieIndicator(true);
        }else{
            mPicselfieParameterViewController.showPicSelfieIndicator(false);
        }
    }

    @Override
    public synchronized void onModeClosed(String modeKey) {
        super.onModeClosed(modeKey);
        if (PicselfieMode.class.getName().equals(modeKey)) {
            setPicsefileValue("off");
        } else if (PhotoMode.class.getName().equals(modeKey) || "on".equals(getValue())) {
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
        if (mAppUi.getModeItem().mModeTitle == IAppUi.ModeTitle.PHOTO || mAppUi.getModeItem().mModeTitle == IAppUi.ModeTitle.APERTURE) {
            mPicselfieParameterViewController.showPicSelfieIndicator(true);
        }else{
            mPicselfieParameterViewController.showPicSelfieIndicator(false);
        }
    }

    @Override
    public void refreshViewEntry() {
        super.refreshViewEntry();

        if (mAppUi.getModeItem().mModeTitle == IAppUi.ModeTitle.PHOTO || mAppUi.getModeItem().mModeTitle == IAppUi.ModeTitle.APERTURE) {
            mPicselfieParameterViewController.showPicSelfieIndicator(true);
        }else{
            mPicselfieParameterViewController.showPicSelfieIndicator(false);
        }
    }

    @Override
    public void removeViewEntry() {
        LogHelper.d(TAG, "[removeViewEntry]");
        mPicselfieParameterViewController.showPicSelfieIndicator(false);
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
}
