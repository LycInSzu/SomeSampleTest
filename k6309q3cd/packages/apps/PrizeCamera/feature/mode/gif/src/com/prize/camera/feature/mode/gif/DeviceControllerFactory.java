package com.prize.camera.feature.mode.gif;

import android.app.Activity;

import com.mediatek.camera.common.ICameraContext;
import com.mediatek.camera.common.device.CameraDeviceManagerFactory;

import javax.annotation.Nonnull;

/**
 * Created by yangming.jiang on 2018/10/17.
 */

public class DeviceControllerFactory {
    public IGifDeviceController createDeviceController(
            @Nonnull Activity activity,
            @Nonnull CameraDeviceManagerFactory.CameraApi cameraApi,
            @Nonnull ICameraContext context) {
        return new GifDevice2Controller(activity, context);
    }
}
