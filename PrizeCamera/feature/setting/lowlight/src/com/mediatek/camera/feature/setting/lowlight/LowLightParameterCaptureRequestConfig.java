package com.mediatek.camera.feature.setting.lowlight;

import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CaptureRequest;
import android.view.Surface;

import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.setting.ICameraSetting;
import com.mediatek.camera.common.setting.ISettingManager;

import java.util.List;

public class LowLightParameterCaptureRequestConfig implements ICameraSetting.ICaptureRequestConfigure {
    private LowLightParameter mLowLightParameter;
    private ISettingManager.SettingDevice2Requester mDevice2Requester;

    public LowLightParameterCaptureRequestConfig(LowLightParameter lowLightParameter, ISettingManager.SettingDevice2Requester device2Requester) {
        mLowLightParameter = lowLightParameter;
        mDevice2Requester = device2Requester;
    }

    @Override
    public void sendSettingChangeRequest() {
        mDevice2Requester.createAndChangeRepeatingRequest();
    }

    @Override
    public void setCameraCharacteristics(CameraCharacteristics characteristics) {

    }

    @Override
    public void configCaptureRequest(CaptureRequest.Builder captureBuilder) {
        if(captureBuilder == null){
            return;
        }
        LogHelper.i("",""+",mLowLightParameter value: "+mLowLightParameter.getValue());
        int value = 0;
        if ("on".equals(mLowLightParameter.getValue())) {
            value = 1;
        } else {
            value = 0;
        }
        captureBuilder.set(CaptureRequest.VENDOR_ARCSOFT_LOWLIGHT_MODE,value);
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
