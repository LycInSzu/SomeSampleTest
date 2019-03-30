package com.mediatek.camera.feature.setting.volumekeys;

import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.setting.SettingBase;

import java.util.List;

public class Volumekeys extends SettingBase {
    private static final LogUtil.Tag TAG = new LogUtil.Tag(Volumekeys.class.getSimpleName());
    private static final String KEY_VOLUMEKEYS= "key_volumekeys";
    private VolumekeysSettingView mSettingView;
    private ISettingChangeRequester mSettingChangeRequester;


    @Override
    public void unInit() {

    }

    @Override
    public void postRestrictionAfterInitialized() {

    }

    @Override
    public SettingType getSettingType() {
        return SettingType.PHOTO_AND_VIDEO;
    }

    @Override
    public String getKey() {
        return KEY_VOLUMEKEYS;
    }

    @Override
    public IParametersConfigure getParametersConfigure() {

        if (mSettingChangeRequester == null) {
            VolumekeysParametersConfig config = new VolumekeysParametersConfig(this, mSettingDeviceRequester);
            mSettingChangeRequester = config;
        }
        return (VolumekeysParametersConfig)mSettingChangeRequester;
    }

    @Override
    public ICaptureRequestConfigure getCaptureRequestConfigure() {
        if (mSettingChangeRequester == null) {
            VolumekeysCaptureRequestConfig config = new VolumekeysCaptureRequestConfig(this, mSettingDevice2Requester);
            mSettingChangeRequester = config;
        }
        return (VolumekeysCaptureRequestConfig)mSettingChangeRequester;
    }

    @Override
    public void addViewEntry() {
        super.addViewEntry();
        if (mSettingView == null) {
            mSettingView = new VolumekeysSettingView(getKey());
        }
        mSettingView.setOnDataChangeListener(new VolumekeysSettingView.OnDataChangeListener() {
            @Override
            public void onDataChange(String value) {
                LogHelper.d(TAG, "[onClick], value:" + value);
                setValue(value);
                mDataStore.setValue(getKey(), value, getStoreScope(), false);
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mSettingChangeRequester.sendSettingChangeRequest();
                    }
                });
            }
        });
        mAppUi.addSettingView(mSettingView);
        mSettingView.setValue(getValue());
    }

    @Override
    public void removeViewEntry() {
        mAppUi.removeSettingView(mSettingView);
    }

    public void initializeValue(List<String> platformSupportedValues, String defaultValue) {
        LogHelper.d(TAG, "[initializeValue], platformSupportedValues:" + platformSupportedValues
                + ", defaultValue:" + defaultValue);
        if (platformSupportedValues != null) {
            setSupportedPlatformValues(platformSupportedValues);
            setSupportedEntryValues(platformSupportedValues);
            setEntryValues(platformSupportedValues);
            String value = mDataStore.getValue(getKey(), defaultValue, getStoreScope());
            setValue(value);
        }
    }

    @Override
    public void refreshViewEntry() {
        if (mSettingView != null) {
            mSettingView.setValue(getValue());
        }
    }
}
