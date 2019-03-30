package com.mediatek.camera.feature.setting.grid;

import android.hardware.Camera;

import com.mediatek.camera.common.device.v1.CameraProxy;
import com.mediatek.camera.common.setting.ICameraSetting;
import com.mediatek.camera.common.setting.ISettingManager;
public class GridParametersConfig implements ICameraSetting.IParametersConfigure {

    private final Grid mGrid;
    private final ISettingManager.SettingDeviceRequester mDeviceRequester;
    private String mValue;
    private static String KEY_GRID_MODE = "grid-mode";

    public GridParametersConfig(Grid grid, ISettingManager.SettingDeviceRequester settingDeviceRequester) {
        this.mGrid =  grid;
        this.mDeviceRequester = settingDeviceRequester;
    }

    @Override
    public void sendSettingChangeRequest() {
        mDeviceRequester.requestChangeSettingValue(mGrid.getKey());
    }

    @Override
    public void setOriginalParameters(Camera.Parameters originalParameters) {

    }

    @Override
    public boolean configParameters(Camera.Parameters parameters) {
        if (mGrid.getValue() == null) {
            return false;
        }
        boolean changed = !(mGrid.getValue().equals(mValue));
        parameters.set(KEY_GRID_MODE, mGrid.getValue());
        mValue = mGrid.getValue();
        return changed;
    }

    @Override
    public void configCommand(CameraProxy cameraProxy) {

    }
}
