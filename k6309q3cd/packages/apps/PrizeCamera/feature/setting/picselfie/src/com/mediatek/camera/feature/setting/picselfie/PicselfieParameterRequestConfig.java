package com.mediatek.camera.feature.setting.picselfie;

import android.content.Context;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CaptureRequest;
import android.util.Size;
import android.view.Surface;

import com.mediatek.camera.common.IAppUi;
import com.mediatek.camera.common.app.IApp;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.loader.DeviceDescription;
import com.mediatek.camera.common.mode.CameraApiHelper;
import com.mediatek.camera.common.setting.ICameraSetting;
import com.mediatek.camera.common.setting.ISettingManager;
import com.mediatek.camera.prize.FeatureSwitcher;

import java.util.ArrayList;
import java.util.List;

public class PicselfieParameterRequestConfig implements ICameraSetting.ICaptureRequestConfigure {
    private ISettingManager.SettingDevice2Requester mDevice2Requester;
    private PicselfieParameter mPicselfieParameter;


    //private Context mContext;
    private IApp mApp;
    private boolean mIsSupportDualCam;

    public PicselfieParameterRequestConfig(ISettingManager.SettingDevice2Requester device2Requester, PicselfieParameter picselfieParameter, IApp app) {
        mDevice2Requester = device2Requester;
        mPicselfieParameter = picselfieParameter;
        //mContext = context;
        mApp = app;
        mIsSupportDualCam = FeatureSwitcher.isSupportDualCam();
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
        LogHelper.i("",",mPicselfieParameter value: "+mPicselfieParameter.getValue());
        int value = 0;

        if ("on".equals(mPicselfieParameter.getValue())) {
            value = 1;
        }

        if(mPicselfieParameter.getCameraId() == 0){

            if(!mIsSupportDualCam){
                try{
                    captureBuilder.set(CaptureRequest.VENDOR_UV_BOKEH_MODE, value);
                    if(value == 1){
                        setUVPicParameters(captureBuilder);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        } else if(mPicselfieParameter.getCameraId() == 1){
            try{
                captureBuilder.set(CaptureRequest.VENDOR_ARCSOFT_PICSELFIE_MODE, value);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
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

    private void setUVPicParameters(CaptureRequest.Builder captureBuilder){

        ArrayList<Integer> position = mApp.getAppUi().getCircleCoordiNate();
        Size size = new Size(position.get(0).intValue(), position.get(1).intValue());

        LogHelper.i("", "setUVPicFocus strength="+mApp.getAppUi().getPicselfieStrength()+" radious="+mApp.getAppUi().getRadious()+" x="+size.getWidth()+" y="+size.getHeight());
        /*int strength[] = new int[1];
        int radius[] = new int[1];
        strength[0] = 3;
        radius[0] = 200;

        captureBuilder.set(UV_BOKEH_STRENGTH, strength);
        captureBuilder.set(UV_BOKEH_RADIUS, radius);

        android.util.Size position = new android.util.Size(300,300);
        captureBuilder.set(UV_BOKEH_COORDINATE, position);*/

        captureBuilder.set(CaptureRequest.VENDOR_UV_BOKEH_STRENGTH, mApp.getAppUi().getPicselfieStrength());

        captureBuilder.set(CaptureRequest.VENDOR_UV_BOKEH_RADIUS, (int)mApp.getAppUi().getRadious());

        captureBuilder.set(CaptureRequest.VENDOR_UV_BOKEH_COORDINATE, size);

        captureBuilder.set(CaptureRequest.VENDOR_PREVIEWSIZE, mApp.getAppUi().getPreviewSize());
    }

}
