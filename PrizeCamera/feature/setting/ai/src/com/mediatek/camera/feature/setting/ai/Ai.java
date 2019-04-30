package com.mediatek.camera.feature.setting.ai;

import com.mediatek.camera.common.IAppUi;
import com.mediatek.camera.common.ICameraContext;
import com.mediatek.camera.common.app.IApp;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.mode.ICameraMode;
import com.mediatek.camera.common.relation.Relation;
import com.mediatek.camera.common.setting.ISettingManager;
import com.mediatek.camera.common.setting.SettingBase;
import com.mediatek.camera.prize.PrizeDataRevert;
import com.mediatek.camera.common.relation.StatusMonitor.StatusChangeListener;
import java.util.List;
import com.mediatek.camera.common.relation.StatusMonitor;


import javax.annotation.Nonnull;

public class Ai extends SettingBase implements PrizeDataRevert {
    private static final LogUtil.Tag TAG = new LogUtil.Tag(Ai.class.getSimpleName());
    private AiViewController mAiViewController;
    private ISettingChangeRequester mSettingChangeRequester;
    private static final String VALUE_ON = "on";
    private static final String VALUE_OFF = "off";
    public static final String AI_KEY = "key_ai";
    private AiParameterConfigure mAiParameterConfigure;
    private AiRequestConfigure mAiRequestConfigure;
    private String AI_DEFAULT_VALUE = "off";
    private String KEY_PICSELFIE = "status_key_picselfie";
    private String KEY_AI_STATUS = "status_key_ai_status";
    private StatusMonitor.StatusResponder mAiStatusResponder;

    @Override
    public void init(IApp app, ICameraContext cameraContext, ISettingManager.SettingController settingController) {
        super.init(app, cameraContext, settingController);
        LogHelper.d(TAG, "init");
        if (mAiViewController == null) {
            mAiViewController = new AiViewController(this, app);
        }
        mAiStatusResponder = mStatusMonitor.getStatusResponder(KEY_AI_STATUS);
        mStatusMonitor.registerValueChangedListener(KEY_PICSELFIE, mStatusListener);
    }

    @Override
    public void addViewEntry() {
        int cameraId = Integer.parseInt(mSettingController.getCameraId());
        if (cameraId == 0 && mAppUi.getModeItem().mModeTitle == IAppUi.ModeTitle.PHOTO) {
            mAiViewController.addQuickSwitchIcon();
        }else{
            mAppUi.setAiEnable(false);
        }
    }

    @Override
    public void removeViewEntry() {
        mAiViewController.removeQuickSwitchIcon();
    }

    @Override
    public void refreshViewEntry() {
        int cameraId = Integer.parseInt(mSettingController.getCameraId());
        if (0 == cameraId && mAppUi.getModeItem().mModeTitle == IAppUi.ModeTitle.PHOTO) {
            mAiViewController.showQuickSwitchIcon(true);
            LogHelper.d(TAG, "[refreshViewEntry] showQuickSwitchIcon(true)");
        } else {
            mAiViewController.showQuickSwitchIcon(false);
            mAppUi.setAiEnable(false);
            LogHelper.d(TAG, "[refreshViewEntry] showQuickSwitchIcon(false)");
        }
    }

    @Override
    public void unInit() {
        mStatusMonitor.unregisterValueChangedListener(KEY_PICSELFIE, mStatusListener);
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
        return AI_KEY;
    }

    @Override
    public IParametersConfigure getParametersConfigure() {
        if (mAiParameterConfigure == null) {
            mAiParameterConfigure = new AiParameterConfigure(this, mSettingDeviceRequester);
        }
        mSettingChangeRequester = mAiParameterConfigure;
        return mAiParameterConfigure;
    }

    @Override
    public ICaptureRequestConfigure getCaptureRequestConfigure() {
        if (mAiRequestConfigure == null) {
            mAiRequestConfigure = new AiRequestConfigure(this, mSettingDevice2Requester);
        }
        mSettingChangeRequester = mAiRequestConfigure;
        return mAiRequestConfigure;
    }

    public void initializeValue(List<String> platformSupportedValues, String defaultValue) {
        LogHelper.d(TAG, "[initializeValue], platformSupportedValues:" + platformSupportedValues
                + ", defaultValue:" + defaultValue);
        if (platformSupportedValues != null) {
            setSupportedPlatformValues(platformSupportedValues);
            setSupportedEntryValues(platformSupportedValues);
            setEntryValues(platformSupportedValues);
            String value = mDataStore.getValue(AI_KEY, defaultValue, getStoreScope());
            setValue(value);
            mAppUi.setAiEnable("on".equals(value));
        }
    }

    public void onAiValueChanged(String value) {
        LogHelper.d(TAG, "[onAiValueChanged]");
        mHandler.post(new Runnable() {

            @Override
            public void run() {
                LogHelper.d(TAG, "zhangguo onAiValueChanged  setValue = "+ value);
                mAiStatusResponder.statusChanged(KEY_AI_STATUS, value);

                mDataStore.setValue(getKey(), value, getStoreScope(),false);
                setValue(value);
                LogHelper.d(TAG, "[onAiValueChanged]  setValue = "+ value);
                boolean enable = "on".equals(value);
                mAppUi.setAiEnable(enable);
                handleAiRestriction(true);
            }
        });
    }

    @Override
    public void clearCache() {
        mDataStore.setValue(getKey(), "off", getStoreScope(),false);
        onAiValueChanged("off");
    }

    public void updateAiSenceIcon(int sceneId){
        LogHelper.d(TAG, "[updateAiSenceIcon]  sceneId = "+sceneId);
        mAiViewController.updateSceneIcon(sceneId);
    }

    public void resetRestriction() {
        Relation hdrRelation = AiRestriction.getAiRestriction().getRelation(
                AI_DEFAULT_VALUE, true);
        LogHelper.d(TAG, "[resetRestriction] ai");
        mSettingController.postRestriction(hdrRelation);
    }

    private void handleAiRestriction(boolean empty){
        String aiCurrentValue = getValue();
        LogHelper.d(TAG, "[handleAiRestriction] aiCurrentValue = "+aiCurrentValue);
        Relation aiRelation = AiRestriction.getAiRestriction().getRelation(aiCurrentValue,
                empty);
        if (aiRelation == null) {
            return;
        }
        LogHelper.d(TAG, "[handleAiRestriction] postRestriction");
        mSettingController.postRestriction(aiRelation);
    }

    @Override
    public void overrideValues(@Nonnull String headerKey, String currentValue,
                               List<String> supportValues) {
        String lastValue = getValue();
        LogHelper.i(TAG, "[overrideValues] headerKey = " + headerKey
                + ", currentValue = " + currentValue + ",supportValues = " + supportValues);
        if (headerKey.equals("key_hdr") && currentValue != null && (currentValue != lastValue)) {
            onAiValueChanged(currentValue);
        }
    }

    @Override
    public synchronized void onModeClosed(String modeKey) {
        LogHelper.i(TAG, "[onModeClosed]");
        mAppUi.setAiEnable(false);
        super.onModeClosed(modeKey);
    }

    @Override
    public void onModeOpened(String modeKey, ICameraMode.ModeType modeType) {
        super.onModeOpened(modeKey, modeType);
        if (canOpenAi()){
            mAiViewController.showQuickSwitchIcon(true);
        }else {
            mAiViewController.showQuickSwitchIcon(false);
        }
    }

    public boolean canOpenAi(){
        int cameraId = Integer.parseInt(mSettingController.getCameraId());
        if (cameraId == 0 && mAppUi.getModeItem().mModeTitle == IAppUi.ModeTitle.PHOTO) {
            return true;
        }else{
            return false;
        }
    }

    private StatusChangeListener mStatusListener = new StatusChangeListener() {
        @Override
        public void onStatusChanged(String key, String value) {
            LogHelper.d(TAG, "mStatusListener, key: " + key + ", value: " + value);
            if (KEY_PICSELFIE.equalsIgnoreCase(key)){
                if ("on".equals(value) && "on".equals(getValue()) && null != mAiViewController){
                    mAiViewController.picselfieTurnsOn();
                }
            }
        }
    };
}
