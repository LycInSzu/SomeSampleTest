package com.prize.camera.feature.mode.smartscan;

import android.app.Activity;

import com.mediatek.camera.common.ICameraContext;
import com.mediatek.camera.common.device.CameraDeviceManagerFactory;

import javax.annotation.Nonnull;

/**
 * Created by yangming.jiang on 2018/10/17.
 */

public class DeviceControllerFactory {
    public ISmartScanDeviceController createDeviceController(
            @Nonnull Activity activity,
            @Nonnull CameraDeviceManagerFactory.CameraApi cameraApi,
            @Nonnull ICameraContext context) {
        return new SmartScanDevice2Controller(activity, context);
    }
}
