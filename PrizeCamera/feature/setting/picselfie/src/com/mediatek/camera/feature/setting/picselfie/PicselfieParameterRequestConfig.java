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
        String picselfieValue = mPicselfieParameter.getValue();
        LogHelper.i("",",mPicselfieParameter value: "+picselfieValue);
        int value = 0;

        if ("on".equals(picselfieValue)) {
            value = 1;
        }

        if(mApp.getAppUi().getModeItem() != null && mApp.getAppUi().getModeItem().mModeTitle == IAppUi.ModeTitle.FICSEFILE){
            try{
                captureBuilder.set(CaptureRequest.VENDOR_ARCSOFT_PICSELFIE_MODE, 1);
            }catch (Exception e){
                e.printStackTrace();
            }
        } else if(mPicselfieParameter.getCameraId() == 1 || !FeatureSwitcher.isSupportUVSelfie()){
            try{
                /*prize-modify-bugid:75473 Limit preview size in portrait mode-xiaoping-2019056-start*/
                if((mApp.getAppUi().getModeItem() != null && mApp.getAppUi().getModeItem().mModeTitle != IAppUi.ModeTitle.PHOTO)
                        || "off".equals(mApp.getSettingValue("picsefile_switch","off",mApp.getAppUi().getCameraId()))) {
                    captureBuilder.set(CaptureRequest.VENDOR_ARCSOFT_PICSELFIE_MODE, 0);
                    /*prize-modify-bugid:75473 Limit preview size in portrait mode-xiaoping-2019056-end*/
                } else {
                    captureBuilder.set(CaptureRequest.VENDOR_ARCSOFT_PICSELFIE_MODE, value);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        else if(mPicselfieParameter.getCameraId() == 0){
            try{
                captureBuilder.set(CaptureRequest.VENDOR_UV_BOKEH_MODE, value);
                if(value == 1){
                    setUVPicParameters(captureBuilder);
                }
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

        if(position.size() < 2){
            LogHelper.i("", "setUVPicParameters position is null");
            return;
        }

        Size previewSize = mApp.getAppUi().getPreviewSize();
        float ratio = (float) previewSize.getHeight() / mApp.getAppUi().getSurfaceTextureView().getWidth();

        Size point = new Size((int)(position.get(0).intValue() * ratio), (int)(position.get(1).intValue() * ratio));


        LogHelper.i("", "setUVPicFocus strength="+mApp.getAppUi().getPicselfieStrength()+" radious="+mApp.getAppUi().getRadious()+" x="+point.getWidth()+" y="+point.getHeight() +" ratio="+ratio);
        /*int strength[] = new int[1];
        int radius[] = new int[1];
        strength[0] = 3;
        radius[0] = 200;

        captureBuilder.set(UV_BOKEH_STRENGTH, strength);
        captureBuilder.set(UV_BOKEH_RADIUS, radius);

        android.util.Size position = new android.util.Size(300,300);
        captureBuilder.set(UV_BOKEH_COORDINATE, position);*/

        captureBuilder.set(CaptureRequest.VENDOR_UV_BOKEH_STRENGTH, mApp.getAppUi().getPicselfieStrength());

        captureBuilder.set(CaptureRequest.VENDOR_UV_BOKEH_RADIUS, (int)(mApp.getAppUi().getRadious() * ratio));

        captureBuilder.set(CaptureRequest.VENDOR_UV_BOKEH_COORDINATE, point);

        captureBuilder.set(CaptureRequest.VENDOR_PREVIEWSIZE, previewSize);
    }

}
