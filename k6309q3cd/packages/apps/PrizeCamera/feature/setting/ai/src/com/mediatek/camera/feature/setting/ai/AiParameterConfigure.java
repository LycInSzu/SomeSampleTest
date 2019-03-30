package com.mediatek.camera.feature.setting.ai;

import android.app.Activity;
import android.hardware.Camera;

import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.device.v1.CameraProxy;
import com.mediatek.camera.common.setting.ICameraSetting;
import com.mediatek.camera.common.setting.ISettingManager;

public class AiParameterConfigure implements ICameraSetting.IParametersConfigure {
    private static final LogUtil.Tag TAG =
            new LogUtil.Tag(AiParameterConfigure.class.getSimpleName());

    private Ai mAi;
    private ISettingManager.SettingDeviceRequester mSettingDeviceRequester;

    public AiParameterConfigure(Ai ai, ISettingManager.SettingDeviceRequester settingDeviceRequester) {
        mAi = ai;
        mSettingDeviceRequester = settingDeviceRequester;
    }

    @Override
    public void sendSettingChangeRequest() {

    }

    @Override
    public void setOriginalParameters(Camera.Parameters originalParameters) {

    }

    @Override
    public boolean configParameters(Camera.Parameters parameters) {
        return false;
    }

    @Override
    public void configCommand(CameraProxy cameraProxy) {

    }
}
