package com.mediatek.camera.feature.setting.videomute;

import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.setting.SettingBase;

import java.util.List;

public class VideoMute extends SettingBase {
    private static final LogUtil.Tag TAG = new LogUtil.Tag(VideoMute.class.getSimpleName());
    private static final String VALUE_ON = "on";
    private static final String VALUE_OFF = "off";
    private static final String KEY_VIDEOAMUTE = "key_videomute";
    private boolean mIsCameraMuteSupported = true;
    private VideoMuteSettingView mSettingView;
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
        return SettingType.VIDEO;
    }

    @Override
    public String getKey() {
        return KEY_VIDEOAMUTE;
    }

    @Override
    public IParametersConfigure getParametersConfigure() {
        return null;
    }

    @Override
    public ICaptureRequestConfigure getCaptureRequestConfigure() {
        if (mSettingChangeRequester == null) {
            VideoMuteCaptureRequestConfig config = new VideoMuteCaptureRequestConfig(
                    this, mSettingDevice2Requester);
            mSettingChangeRequester = config;
        }
        return (VideoMuteCaptureRequestConfig)mSettingChangeRequester;
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
            mSettingView = new VideoMuteSettingView(getKey());
            mSettingView.setOnVideoMuteClickListener(new VideoMuteSettingView.OnVideoMuteClickListener() {
                @Override
                public void onVideoMuteClicked(boolean checked) {
                    String value = checked ? VALUE_ON : VALUE_OFF;
                    LogHelper.d(TAG, "[onVideoMuteClicked], value:" + value);
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
