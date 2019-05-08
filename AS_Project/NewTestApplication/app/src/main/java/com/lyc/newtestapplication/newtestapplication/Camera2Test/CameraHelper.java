package com.lyc.newtestapplication.newtestapplication.Camera2Test;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.camera2.*;
import android.media.ImageReader;
import android.nfc.Tag;
import android.os.Parcelable;
import android.util.Log;
import android.view.Surface;
import androidx.annotation.NonNull;
import com.google.android.material.tabs.TabLayout;

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

    private CaptureRequest.Builder captureRequestBuilder;
    private CaptureRequest captureRequest;


    private CameraDevice.StateCallback openCameraStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            Log.d("CameraHelper","  ------------------ CameraDevice.StateCallback  onOpened ----------------");
            cameraDevice = camera;
            createCaptureSession();
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
            cameraCaptureSession=session;
            startPreview();
        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession session) {

        }
    };
    private CameraCaptureSession.CaptureCallback previewCaptureCallback=new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureProgressed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureResult partialResult) {
            super.onCaptureProgressed(session, request, partialResult);
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
        }

        @Override
        public void onCaptureFailed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureFailure failure) {
            super.onCaptureFailed(session, request, failure);
        }

        @Override
        public void onCaptureSequenceCompleted(@NonNull CameraCaptureSession session, int sequenceId, long frameNumber) {
            super.onCaptureSequenceCompleted(session, sequenceId, frameNumber);
        }

        @Override
        public void onCaptureSequenceAborted(@NonNull CameraCaptureSession session, int sequenceId) {
            super.onCaptureSequenceAborted(session, sequenceId);
        }

        @Override
        public void onCaptureBufferLost(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull Surface target, long frameNumber) {
            super.onCaptureBufferLost(session, request, target, frameNumber);
        }

        @Override
        public void onCaptureStarted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, long timestamp, long frameNumber) {
            super.onCaptureStarted(session, request, timestamp, frameNumber);
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
                    Log.d("CameraHelper","  ------------------ this.cameraId is ---------------- "+this.cameraId);
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
        Log.d("CameraHelper","  ------------------ createCaptureSession ----------------");
        try {
            cameraDevice.createCaptureSession(outputs,captureSessionCallback,null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void openCamera() {
        Log.d("CameraHelper","  ------------------ openCamera ----------------");
        if (cameraId != null) {

            try {
                cameraManager.openCamera(cameraId, openCameraStateCallback, null);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

    }

    public void startPreview() {
        Log.d("CameraHelper","  ------------------ startPreview ----------------");
        try {
            captureRequestBuilder= cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(outputs.get(0));
            captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            cameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(),previewCaptureCallback,null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }

    public void takePicture() {
        Log.d("CameraHelper","  ------------------ takePicture ----------------");
    }

    public void releaseCamera() {
        Log.d("CameraHelper","  ------------------ releaseCamera ----------------");
        cameraDevice.close();
        captureRequest=null;
        cameraCaptureSession.close();


    }

}
