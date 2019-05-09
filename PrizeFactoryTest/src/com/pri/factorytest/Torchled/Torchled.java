package com.pri.factorytest.Torchled;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.pri.factorytest.PrizeBaseActivity;
import com.pri.factorytest.R;
import com.pri.factorytest.util.Utils;

import java.io.FileOutputStream;

public class Torchled extends PrizeBaseActivity {
    public static final boolean HasFlashlightFile = false;
    String TAG = "Flashlight";
    private Camera mycam = null;
    private Parameters camerPara = null;
    final byte[] LIGHTE_ON = {'2', '5', '5'};
    final byte[] LIGHTE_OFF = {'0'};
    /**
     * not use the dev file
     */
    private static final String FLASHLIGHT_NODE = "/sys/class/leds/flashlight/brightness";

    private CameraManager mCameraManager = null;
    private String mCameraId;

    @Override
    protected void onDestroy() {

        if (!HasFlashlightFile) {
            try {
                mCameraManager.setTorchMode(mCameraId, false);
                finish();
            } catch (Exception e) {
            }
        } else {

            FileOutputStream flashlight;
            try {
                flashlight = new FileOutputStream(FLASHLIGHT_NODE);
                flashlight.write(LIGHTE_OFF);
                flashlight.close();

            } catch (Exception e) {
            }
        }
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout TorchLayout = (LinearLayout) getLayoutInflater().inflate(
                R.layout.torch, null);
        setContentView(TorchLayout);

        if (!HasFlashlightFile) {
            try {
                mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
                tryInitCamera();
                mCameraManager.setTorchMode(mCameraId, true);
            } catch (Exception e) {
            }
        } else {

            FileOutputStream flashlight;
            try {
                flashlight = new FileOutputStream(FLASHLIGHT_NODE);
                flashlight.write(LIGHTE_ON);
                flashlight.close();

            } catch (Exception e) {
            }

        }
        confirmButton();
        Utils.paddingLayout(findViewById(R.id.passButton), 0, ACTIVITY_TOP_PADDING, 0, 0);
    }

    private void tryInitCamera() {
        try {
            mCameraId = getCameraId();
        } catch (Throwable e) {
            Log.e(TAG, "Couldn't initialize.", e);
            return;
        }
    }

    private String getCameraId() throws CameraAccessException {
        String[] ids = mCameraManager.getCameraIdList();
        for (String id : ids) {
            CameraCharacteristics c = mCameraManager.getCameraCharacteristics(id);
            Boolean flashAvailable = c.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
            Integer lensFacing = c.get(CameraCharacteristics.LENS_FACING);
            if (flashAvailable != null && flashAvailable
                    && lensFacing != null && lensFacing == CameraCharacteristics.LENS_FACING_BACK) {
                return id;
            }
        }
        return null;
    }
}
