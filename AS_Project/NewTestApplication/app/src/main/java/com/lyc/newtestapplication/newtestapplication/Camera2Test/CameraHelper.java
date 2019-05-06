package com.lyc.newtestapplication.newtestapplication.Camera2Test;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.camera2.*;
import android.media.ImageReader;
import android.os.Parcelable;
import android.view.Surface;
import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

public class CameraHelper {
    private CameraManager cameraManager;
    private CameraDevice cameraDevice;
    private CameraCaptureSession cameraCaptureSession;
    private CameraCharacteristics cameraCharacteristics;
    private String cameraId;
    private List<Surface> outputs;


    private CameraDevice.StateCallback openCameraStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            cameraDevice = camera;

        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {

        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {

        }
    };
    private CameraCaptureSession.StateCallback captureSessionCallback=new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(@NonNull CameraCaptureSession session) {

        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession session) {

        }
    };

    public CameraHelper(CameraManager cameraManager, @NonNull List<Surface> outputs) {
        this.cameraManager = cameraManager;
        this.outputs=outputs;
    }


    public void init() {
        try {
            String[] cameraIdList = cameraManager.getCameraIdList();
            for (String cameraId : cameraIdList) {
                if (cameraManager.getCameraCharacteristics(cameraId).get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK) {
                    this.cameraId = cameraId;
                    break;
                }
            }

            cameraCharacteristics = cameraManager.getCameraCharacteristics(this.cameraId);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }


    }

    public void getCharacteristics() {

    }

    public void createCaptureSession() {
        try {
            cameraDevice.createCaptureSession(outputs,captureSessionCallback,null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void openCamera() {
        if (cameraId != null) {

            try {
                cameraManager.openCamera(cameraId, openCameraStateCallback, null);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

    }

    public void startPreview() {

    }

    public void takePicture() {

    }

    public void releaseCamera() {

    }

}
