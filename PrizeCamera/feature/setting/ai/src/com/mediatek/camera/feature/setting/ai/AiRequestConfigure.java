package com.mediatek.camera.feature.setting.ai;

import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CaptureRequest;
import android.view.Surface;

import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.setting.ICameraSetting;
import com.mediatek.camera.common.setting.ISettingManager;

import java.util.ArrayList;
import java.util.List;

public class AiRequestConfigure implements ICameraSetting.ICaptureRequestConfigure {
    private static final LogUtil.Tag TAG =
            new LogUtil.Tag(AiRequestConfigure.class.getSimpleName());
    private final ISettingManager.SettingDevice2Requester mSettingDevice2Requester;
    private final Ai mAi;
    private static final String VALUE_ON = "on";
    private static final String VALUE_OFF = "off";

    public AiRequestConfigure(Ai ai, ISettingManager.SettingDevice2Requester settingDevice2Requester) {
        mAi = ai;
        mSettingDevice2Requester = settingDevice2Requester;
    }

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
        mAi.initializeValue(platformSupportedValues, VALUE_OFF);
    }

    @Override
    public void configCaptureRequest(CaptureRequest.Builder captureBuilder) {
        /*prize-add fixbug:[72344]-huangpengfei-20190321-start*/
        if (captureBuilder == null) {
            LogHelper.d(TAG, "[configCaptureRequest] captureBuilder is null");
            return;
        }
        /*prize-add fixbug:[72344]-huangpengfei-20190321-end*/
        String value = mAi.getValue();
        int enable = 0;
        if ("on".equals(value) && mAi.canOpenAi()) enable = 1;
        LogHelper.i("",",configCaptureRequest enable: " + enable);
        captureBuilder.set(CaptureRequest.VENDOR_AICAMERA_ENABLE, enable);

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
