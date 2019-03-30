package com.mediatek.camera.feature.setting.picturezoom;

import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CaptureRequest;
import android.view.Surface;

import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.setting.ICameraSetting;
import com.mediatek.camera.common.setting.ISettingManager;

import java.util.List;

public class PictureZoomParameterCaptureRequestConfig implements ICameraSetting.ICaptureRequestConfigure {
    private ISettingManager.SettingDevice2Requester mDevice2Requester;
    private PictureZoomParameter mPictureZoomParameter;

    public PictureZoomParameterCaptureRequestConfig(ISettingManager.SettingDevice2Requester device2Requester, PictureZoomParameter pictureZoomParameter) {
        mDevice2Requester = device2Requester;
        mPictureZoomParameter = pictureZoomParameter;
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
        LogHelper.i("","xiaop"+",mPictureZoomParameter,value: "+ mPictureZoomParameter.getValue());
        int value = 0;
        if ("on".equals(mPictureZoomParameter.getValue())) {
            value = 1;
        } else {
            value = 0;
        }
        captureBuilder.set(CaptureRequest.VENDOR_ARCSOFT_PICZOOM_MODE,value);

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
