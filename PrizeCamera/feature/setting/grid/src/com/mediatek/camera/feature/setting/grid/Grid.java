package com.mediatek.camera.feature.setting.grid;

import android.view.View;

import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.setting.SettingBase;

import java.util.List;

public class Grid extends SettingBase {
    private static final LogUtil.Tag TAG = new LogUtil.Tag(Grid.class.getSimpleName());

    private static final String VALUE_ON = "on";
    private static final String VALUE_OFF = "off";
    private static final String KEY_GRID = "key_grid";

    @Override
    public void refreshViewEntry() {
        super.refreshViewEntry();
        if (mSettingView != null) {
            mSettingView.setChecked(VALUE_ON.equals(getValue()));
            mSettingView.setEnabled(getEntryValues().size() > 1);
        }
    }

    private GridSettingView mSettingView;
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
        return KEY_GRID;
    }

    @Override
    public IParametersConfigure getParametersConfigure() {

        if (mSettingChangeRequester == null) {
            GridParametersConfig config = new GridParametersConfig(this, mSettingDeviceRequester);
            mSettingChangeRequester = config;
        }
        return (GridParametersConfig)mSettingChangeRequester;
    }

    @Override
    public ICaptureRequestConfigure getCaptureRequestConfigure() {
        if (mSettingChangeRequester == null) {
            GridCaptureRequestConfig config = new GridCaptureRequestConfig(
                    this, mSettingDevice2Requester);
            mSettingChangeRequester = config;
        }
        return (GridCaptureRequestConfig)mSettingChangeRequester;
    }

    @Override
    public void addViewEntry() {
        super.addViewEntry();
        if (mSettingView == null) {
            mSettingView = new GridSettingView(getKey());
            mSettingView.setGridOnClickListener(new GridSettingView.OnGridClickListener() {
                @Override
                public void onGridClicked(boolean checked) {
                    String value = checked ? VALUE_ON : VALUE_OFF;
                    LogHelper.d(TAG, "[onGridClicked], value:" + value);
                    setValue(value);
                    mAppUi.setUIVisibility(mAppUi.GRID,checked? View.VISIBLE:View.GONE);
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

    public void initializeValue(List<String> platformSupportedValues, String defaultValue) {
        LogHelper.d(TAG, "[initializeValue], platformSupportedValues:" + platformSupportedValues
                + ", defaultValue:" + defaultValue);
        if (platformSupportedValues != null) {
            setSupportedPlatformValues(platformSupportedValues);
            setSupportedEntryValues(platformSupportedValues);
            setEntryValues(platformSupportedValues);
            String value = mDataStore.getValue(getKey(), defaultValue, getStoreScope());
            setValue(value);
            mAppUi.setUIVisibility(mAppUi.GRID,value.equals(VALUE_ON)? View.VISIBLE:View.GONE);
        }
    }

    @Override
    public void removeViewEntry() {
        mAppUi.removeSettingView(mSettingView);
    }
}
