package com.mediatek.camera.feature.setting.volumekeys;

import android.hardware.Camera;

import com.mediatek.camera.common.device.v1.CameraProxy;
import com.mediatek.camera.common.setting.ICameraSetting;
import com.mediatek.camera.common.setting.ISettingManager;

public class VolumekeysParametersConfig implements ICameraSetting.IParametersConfigure {
    private final ISettingManager.SettingDeviceRequester mDeviceRequester;
    private final Volumekeys mVolumekeys;
    private String mValue;
    private static String KEY_VOLUMEKEYS_MODE = "volumekeys-mode";

    public VolumekeysParametersConfig(Volumekeys volumekeys, ISettingManager.SettingDeviceRequester settingDeviceRequester) {
        mVolumekeys = volumekeys;
        mDeviceRequester = settingDeviceRequester;
    }

    @Override
    public void sendSettingChangeRequest() {
        mDeviceRequester.requestChangeSettingValue(mVolumekeys.getKey());
    }

    @Override
    public void setOriginalParameters(Camera.Parameters originalParameters) {

    }

    @Override
    public boolean configParameters(Camera.Parameters parameters) {
        if (mVolumekeys.getValue() == null) {
            return false;
        }
        boolean changed = !(mVolumekeys.getValue().equals(mValue));
        parameters.set(KEY_VOLUMEKEYS_MODE, mVolumekeys.getValue());
        mValue = mVolumekeys.getValue();
        return changed;
    }

    @Override
    public void configCommand(CameraProxy cameraProxy) {

    }
}
