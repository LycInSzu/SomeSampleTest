package com.mediatek.camera.feature.setting.location;

import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.setting.SettingBase;

import java.util.List;

public class Location extends SettingBase {
    private static final LogUtil.Tag TAG = new LogUtil.Tag(Location.class.getSimpleName());
    private static final String VALUE_ON = "on";
    private static final String VALUE_OFF = "off";
    private static final String KEY_LOCATION = "key_location";
    private boolean mIsLocationSupported = true;
    private LocationSettingView mSettingView;
    private ISettingChangeRequester mSettingChangeRequester;

    @Override
    public void refreshViewEntry() {
        super.refreshViewEntry();
        if (mSettingView != null) {
            mSettingView.setChecked(VALUE_ON.equals(getValue()));
            mSettingView.setEnabled(getEntryValues().size() > 1);
        }
    }

    public void initializeValue(List<String> platformSupportedValues, String defaultValue) {
        LogHelper.d(TAG, "[initializeValue], platformSupportedValues:" + platformSupportedValues
                + ", defaultValue:" + defaultValue);
        if (platformSupportedValues != null) {
            mIsLocationSupported = true;
            setSupportedPlatformValues(platformSupportedValues);
            setSupportedEntryValues(platformSupportedValues);
            setEntryValues(platformSupportedValues);
            String value = mDataStore.getValue(getKey(), defaultValue, getStoreScope());
            LogHelper.i(TAG,"mDataStore.getValue: "+value);
            setValue(value);
            /*prize-modify-bugid:75388 Location information in photo details with photo location turned off by default-xiaoping-20190506-start*/
            mDataStore.setValue(getKey(), value, getStoreScope(), false);
            /*prize-modify-bugid:75388 Location information in photo details with photo location turned off by default-xiaoping-20190506-end*/
        }
    }

    @Override
    public void addViewEntry() {
        if (!mIsLocationSupported) {
            return;
        }
        if (mSettingView == null) {
            mSettingView = new LocationSettingView(getKey());
            mSettingView.setOnLocationClickListener(new LocationSettingView.OnLocationClickListener() {
                @Override
                public void onLocationClicked(boolean checked) {
                    String value = checked ? VALUE_ON : VALUE_OFF;
                    LogHelper.d(TAG, "[onCameraMuteClicked], value:" + value);
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
        }
        mAppUi.addSettingView(mSettingView);
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
        return KEY_LOCATION;
    }

    @Override
    public IParametersConfigure getParametersConfigure() {
        return null;
    }

    @Override
    public ICaptureRequestConfigure getCaptureRequestConfigure() {
        if (mSettingChangeRequester == null) {
            LocationCaptureRequestConfig config = new LocationCaptureRequestConfig(
                    this, mSettingDevice2Requester);
            mSettingChangeRequester = config;
        }
        return (LocationCaptureRequestConfig)mSettingChangeRequester;
    }

    @Override
    public void removeViewEntry() {
        mAppUi.removeSettingView(mSettingView);
    }
}
