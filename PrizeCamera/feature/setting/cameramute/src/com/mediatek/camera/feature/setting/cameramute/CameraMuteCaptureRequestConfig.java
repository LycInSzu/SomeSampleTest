package com.mediatek.camera.feature.setting.cameramute;

import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CaptureRequest;
import android.view.Surface;

import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.setting.ICameraSetting;
import com.mediatek.camera.common.setting.ISettingManager;

import java.util.ArrayList;
import java.util.List;

public class CameraMuteCaptureRequestConfig implements ICameraSetting.ICaptureRequestConfigure {
    private static final LogUtil.Tag TAG = new LogUtil.Tag(
            CameraMuteCaptureRequestConfig.class.getSimpleName());
    private static final String VALUE_ON = "on";
    private static final String VALUE_OFF = "off";

    public CameraMuteCaptureRequestConfig(CameraMute cameraMute, ISettingManager.SettingDevice2Requester settingDevice2Requester) {
        mCameraMute = cameraMute;
        mSettingDevice2Requester = settingDevice2Requester;
    }

    private final CameraMute mCameraMute;
    private final ISettingManager.SettingDevice2Requester mSettingDevice2Requester;
    @Override
    public void sendSettingChangeRequest() {
        mSettingDevice2Requester.createAndChangeRepeatingRequest();
    }

    @Override
    public void setCameraCharacteristics(CameraCharacteristics characteristics) {
        List<String> platformSupportedValues = new ArrayList<String>();
        platformSupportedValues.clear();
        platformSupportedValues.add(VALUE_ON);
        platformSupportedValues.add(VALUE_OFF);
        mCameraMute.initializeValue(platformSupportedValues, VALUE_OFF);

    }

    @Override
    public void configCaptureRequest(CaptureRequest.Builder captureBuilder) {

    }

    @Override
    public void configSessionSurface(List<Surface> surfaces) {

    }

    @Override
    public Surface configRawSurface() {
        return null;
    }

    @Override
    public CameraCaptureSession.CaptureCallback getRepeatingCaptureCallback() {
        return null;
    }
}
