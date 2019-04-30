package com.mediatek.camera.feature.setting.volumekeys;

import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CaptureRequest;
import android.view.Surface;

import com.mediatek.camera.common.setting.ICameraSetting;
import com.mediatek.camera.common.setting.ISettingManager;

import java.util.ArrayList;
import java.util.List;

public class VolumekeysCaptureRequestConfig implements ICameraSetting.ICaptureRequestConfigure {
    private final Volumekeys mVolumekeys;
    private String VALUE_VOLUME = "0";
    private String VALUE_SHOOT = "1";
    private String VALUE_ZOOM = "2";
    private final ISettingManager.SettingDevice2Requester mSettingDevice2Requester;

    public VolumekeysCaptureRequestConfig(Volumekeys volumekeys, ISettingManager.SettingDevice2Requester settingDevice2Requester) {
        mVolumekeys = volumekeys;
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
        platformSupportedValues.add(VALUE_VOLUME);
        platformSupportedValues.add(VALUE_SHOOT);
        platformSupportedValues.add(VALUE_ZOOM);
        mVolumekeys.initializeValue(platformSupportedValues, VALUE_SHOOT);
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
