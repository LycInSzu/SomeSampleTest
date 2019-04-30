package com.mediatek.camera.common.mode.lowlight;

import com.mediatek.camera.common.mode.DeviceUsage;
import com.mediatek.camera.common.mode.photo.PhotoMode;
import com.mediatek.camera.common.relation.DataStore;
import com.mediatek.camera.prize.FeatureSwitcher;

import java.util.ArrayList;

import javax.annotation.Nonnull;


public class LowLightMode extends PhotoMode {

    @Override
    protected void initCameraId() {
        if(FeatureSwitcher.isSupportNightCam()){
            mCameraId = "3";
        }else{
            super.initCameraId();
        }
    }

    @Override
    public DeviceUsage getDeviceUsage(@Nonnull DataStore dataStore, DeviceUsage oldDeviceUsage) {
        if(FeatureSwitcher.isSupportNightCam()){
            ArrayList<String> openedCameraIds = new ArrayList<>();
            String cameraId = getCameraIdByFacing(dataStore.getValue(
                    KEY_CAMERA_SWITCHER, null, dataStore.getGlobalScope()));
            openedCameraIds.add(cameraId);
            updateModeDefinedCameraApi();
            return new DeviceUsage(DeviceUsage.DEVICE_TYPE_LOWNIGHT, mCameraApi, openedCameraIds);
        }else{
            return super.getDeviceUsage(dataStore, oldDeviceUsage);
        }
    }
}
