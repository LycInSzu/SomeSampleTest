package com.mediatek.camera.feature.setting.cameramute;

import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.setting.SettingBase;

import java.util.List;

public class CameraMute extends SettingBase {
    private static final LogUtil.Tag TAG = new LogUtil.Tag(CameraMute.class.getSimpleName());
    private static final String VALUE_ON = "on";
    private static final String VALUE_OFF = "off";
    private static final String KEY_CAMERAMUTE = "key_cameramute";
    private boolean mIsCameraMuteSupported = true;
    private CameraMuteSettingView mSettingView;
    private ISettingChangeRequester mSettingChangeRequester;

    @Override
    public void refreshViewEntry() {
        super.refreshViewEntry();
        if (mSettingView != null) {
            mSettingView.setChecked(VALUE_ON.equals(getValue()));
            mSettingView.setEnabled(getEntryValues().size() > 1);
        }
    }

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
        return KEY_CAMERAMUTE;
    }

    @Override
    public IParametersConfigure getParametersConfigure() {
        return null;
    }

    @Override
    public ICaptureRequestConfigure getCaptureRequestConfigure() {
        if (mSettingChangeRequester == null) {
            CameraMuteCaptureRequestConfig config = new CameraMuteCaptureRequestConfig(
                    this, mSettingDevice2Requester);
            mSettingChangeRequester = config;
        }
        return (CameraMuteCaptureRequestConfig)mSettingChangeRequester;
    }

    public void initializeValue(List<String> platformSupportedValues, String defaultValue) {
        LogHelper.d(TAG, "[initializeValue], platformSupportedValues:" + platformSupportedValues
                + ", defaultValue:" + defaultValue);
        if (platformSupportedValues != null) {
            mIsCameraMuteSupported = true;
            setSupportedPlatformValues(platformSupportedValues);
            setSupportedEntryValues(platformSupportedValues);
            setEntryValues(platformSupportedValues);
            String value = mDataStore.getValue(getKey(), defaultValue, getStoreScope());
            LogHelper.i(TAG,"mDataStore.getValue: "+value);
            setValue(value);
            mApp.setCameraMuteValue(value);
        }
    }
    @Override
    public void addViewEntry() {
        if (!mIsCameraMuteSupported) {
            return;
        }
        if (mSettingView == null) {
            mSettingView = new CameraMuteSettingView(getKey());
            mSettingView.setCameraMuteClickListener(new CameraMuteSettingView.OnCameraMuteClickListener() {
                @Override
                public void onCameraMuteClicked(boolean checked) {
                    String value = checked ? VALUE_ON : VALUE_OFF;
                    LogHelper.d(TAG, "[onCameraMuteClicked], value:" + value);
                    setValue(value);
                    mApp.setCameraMuteValue(value);
                    mDataStore.setValue(getKey(), value, getStoreScope(), false);
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mSettingChangeRequester.sendSettingChangeRequest();
                        }
                    });
                }
            });
        }
        mAppUi.addSettingView(mSettingView);
    }

    @Override
    public void removeViewEntry() {
        mAppUi.removeSettingView(mSettingView);
    }
}
