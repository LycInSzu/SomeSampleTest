package com.mediatek.camera.feature.setting.beautyparameter;

import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CaptureRequest;
import android.view.Surface;

import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.setting.ICameraSetting;
import com.mediatek.camera.common.setting.ISettingManager;

import java.util.List;

public class BeautyParameterCaptureRequestConfig implements ICameraSetting.ICaptureRequestConfigure {
    private ISettingManager.SettingDevice2Requester mDevice2Requester;
    private BeautyParameter mBeautyParameter;

    public BeautyParameterCaptureRequestConfig(ISettingManager.SettingDevice2Requester mDevice2Requester, BeautyParameter mBeautyParameter) {
        this.mDevice2Requester = mDevice2Requester;
        this.mBeautyParameter = mBeautyParameter;
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
        LogHelper.i("","mBeautyParameter,modeValue: "+mBeautyParameter.getBeautyModeValue()+",SMOOTHING_KEY value: "+mBeautyParameter.getBeautyParameterValue(BeautyParameter.SMOOTHING_KEY)+
        "SLIMMING_KEY value: "+mBeautyParameter.getBeautyParameterValue(BeautyParameter.SLIMMING_KEY)+
        "CATCHLIGHT_KEY value: "+mBeautyParameter.getBeautyParameterValue(BeautyParameter.CATCHLIGHT_KEY)+
        "EYESENLARGEMENT_value: "+mBeautyParameter.getBeautyParameterValue(BeautyParameter.EYESENLARGEMENT_KEY));
        int modeValue = 0;
        if ("on".equals(mBeautyParameter.getBeautyModeValue()
        )) {
            modeValue = 1;
        } else {
            modeValue = 0;
        }
        captureBuilder.set(CaptureRequest.VENDOR_FOTONATION_FB_MODE,modeValue);
        captureBuilder.set(CaptureRequest.VENDOR_FOTONATION_FB_SMOOTHING_LEVEL,mBeautyParameter.getBeautyParameterValue(BeautyParameter.SMOOTHING_KEY) );
        captureBuilder.set(CaptureRequest.VENDOR_FOTONATION_FB_TONING_LEVEL,mBeautyParameter.getBeautyParameterValue(BeautyParameter.CATCHLIGHT_KEY) );
        captureBuilder.set(CaptureRequest.VENDOR_FOTONATION_FB_SLIMMING_LEVEL,mBeautyParameter.getBeautyParameterValue(BeautyParameter.SLIMMING_KEY) );
        captureBuilder.set(CaptureRequest.VENDOR_FOTONATION_FB_EYEENLARGE_LEVEL,mBeautyParameter.getBeautyParameterValue(BeautyParameter.EYESENLARGEMENT_KEY));
    }
	
	@Override
    public Surface configRawSurface() {
        return null;
    }
	
    @Override
    public void configSessionSurface(List<Surface> surfaces) {

    }

    @Override
    public CameraCaptureSession.CaptureCallback getRepeatingCaptureCallback() {
        return null;
    }
}
