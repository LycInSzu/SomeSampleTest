package com.mediatek.camera.feature.setting.beautyparameter;

import com.mediatek.camera.common.ICameraContext;
import com.mediatek.camera.common.app.IApp;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.mode.ICameraMode;
import com.mediatek.camera.common.mode.beauty.FaceBeautyMode;
import com.mediatek.camera.common.setting.ISettingManager;
import com.mediatek.camera.common.setting.SettingBase;

public class BeautyParameter extends SettingBase {
    private BeautyParameterViewController mBeautyParameterViewController;
    private static final LogUtil.Tag TAG = new LogUtil.Tag(BeautyParameter.class.getSimpleName());
    private static final String BEAUTY_MODE_KEY = "beauty_key";
    private static final String BEAUTY_MODE_ON = "on";
    private static final String BEAUTY_MODE_OFF = "off";
    public static final String COMPOSITE_KEY = "key_composite";
    public static final String SMOOTHING_KEY = "key_smoothing";
    public static final String SLIMMING_KEY = "key_slimming";
    public static final String CATCHLIGHT_KEY = "key_catchlight";
    public static final String EYESENLARGEMENT_KEY = "key_eyesEnlargement";
    public static final String BEAUTY_PARAMETER_MODE = "key_beauty_parameter";
    private ISettingChangeRequester mSettingChangeRequester;
    private ICaptureRequestConfigure mBeautyParameterRequestConfigure;
    private String smoothingValue;
    private int defaultValue = 50;
    @Override
    public void init(IApp app, ICameraContext cameraContext, ISettingManager.SettingController settingController) {
        super.init(app, cameraContext, settingController);
        if (mBeautyParameterViewController == null) {
            mBeautyParameterViewController = new BeautyParameterViewController(this,app);
        }

    }

    @Override
    public void unInit() {
        if(mBeautyParameterViewController != null){
            mBeautyParameterViewController.uninit();
            mBeautyParameterViewController = null;
        }
        /*prize-modify-bugid:67648 adjusting the beauty progress bar has not been implemented-xiaoping-20181107-start*/
        mBeautyParameterRequestConfigure = null;
        /*prize-modify-bugid:67648 adjusting the beauty progress bar has not been implemented-xiaoping-20181107-end*/
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
        return BEAUTY_MODE_KEY;
    }

    @Override
    public IParametersConfigure getParametersConfigure() {
        return null;
    }

    @Override
    public ICaptureRequestConfigure getCaptureRequestConfigure() {
//        LogHelper.i("","mBeautyParameterRequestConfigure: "+mBeautyParameterRequestConfigure);
        if (mBeautyParameterRequestConfigure == null) {
            mBeautyParameterRequestConfigure = new BeautyParameterCaptureRequestConfig(mSettingDevice2Requester,this);
        }
        mSettingChangeRequester = mBeautyParameterRequestConfigure;
        return mBeautyParameterRequestConfigure;
    }

    @Override
    public void onModeOpened(String modeKey, ICameraMode.ModeType modeType) {
        LogHelper.i(TAG,"getvalue: "+getValue()+",modeKey: "+modeKey+",modeType: "+modeType+",isOnSwitchCamera: "+mAppUi.isOnSwitchCamera());
        if (FaceBeautyMode.class.getName().equals(modeKey) /*&& !mAppUi.isOnSwitchCamera()*/) {//prize-modify fixbug[72543]-huangpengfei-20190325
            mBeautyParameterViewController.showBeautyParameterView();
            mBeautyParameterViewController.setCurrentMode(mDataStore.getValue(BEAUTY_PARAMETER_MODE,COMPOSITE_KEY,getStoreScope()));
            mDataStore.setValue(getKey(),BEAUTY_MODE_ON,getStoreScope(),true);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mSettingChangeRequester != null) {
                        mSettingChangeRequester.sendSettingChangeRequest();
                    }
                }
            });
        }
        mAppUi.setSwitchCameraState(false);
        super.onModeOpened(modeKey, modeType);
    }

    @Override
    public synchronized void onModeClosed(String modeKey) {
        LogHelper.i(TAG,"modeKey: "+modeKey);
        if (FaceBeautyMode.class.getName().equals(modeKey) || "on".equals(getBeautyModeValue())) {
            mBeautyParameterViewController.hideBeautyParameterView();
            mDataStore.setValue(getKey(),BEAUTY_MODE_OFF,getStoreScope(),true);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mSettingChangeRequester != null) {
                        mSettingChangeRequester.sendSettingChangeRequest();
                    }
                }
            });
        }
        super.onModeClosed(modeKey);
    }

    public void setBeautyParameterProgress(String key, int process) {
        LogHelper.i(TAG,"key: "+key+",process: "+process+",getStoreScope: "+getStoreScope());
        mDataStore.setValue(key,String.valueOf(process),getStoreScope(),true);
        setValue(key);
        mDataStore.setValue(BEAUTY_PARAMETER_MODE,key,getStoreScope(),true);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mSettingChangeRequester != null) {
                    mSettingChangeRequester.sendSettingChangeRequest();
                }
            }
        });

    }

    public void setCompositeProgress(String key, int process) {
        mDataStore.setValue(key,String.valueOf(process),getStoreScope(),true);
        setValue(COMPOSITE_KEY);
        mDataStore.setValue(BEAUTY_PARAMETER_MODE,COMPOSITE_KEY,getStoreScope(),true);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mSettingChangeRequester != null) {
                    mSettingChangeRequester.sendSettingChangeRequest();
                }
            }
        });
    }

    public int getBeautyParameterProgress(String key) {
        return Integer.parseInt(mDataStore.getValue(key,String.valueOf(defaultValue),getStoreScope()));
    }

    public String getBeautyModeValue() {
        return mDataStore.getValue(getKey(),BEAUTY_MODE_OFF,getStoreScope());
    }

    public int getBeautyParameterValue(String key) {
        /*prize-modify-bugid:68372 NullPointerException-huangpengfei-20181116-start*/
        if (mBeautyParameterViewController == null){
            LogHelper.d(TAG,"[getBeautyParameterValue]  mBeautyParameterViewController = null  return...");
            return 0;
        }
        /*prize-modify-bugid:68372 NullPointerException-huangpengfei-20181116-end*/
        return (int) (getBeautyParameterProgress(key) * mBeautyParameterViewController.getStep(key));
    }
}
