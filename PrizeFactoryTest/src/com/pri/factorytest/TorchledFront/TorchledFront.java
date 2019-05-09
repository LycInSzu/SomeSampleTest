package com.pri.factorytest.TorchledFront;

import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;

import com.pri.factorytest.PrizeBaseActivity;
import com.pri.factorytest.R;
import com.pri.factorytest.util.Utils;

import java.io.FileOutputStream;

public class TorchledFront extends PrizeBaseActivity {
    public static final boolean HasFlashlightFile = false;
    String TAG = "Flashlight";
    final byte[] LIGHTE_ON = {'2', '5', '5'};
    final byte[] LIGHTE_OFF = {'0'};
    private static final String FLASHLIGHT_NODE = "/sys/class/leds/flashlight/brightness";
    private CameraManager mCameraManager = null;
    private String mCameraId = null;

    @Override
    protected void onDestroy() {
        try {
            if (!HasFlashlightFile) {
                mCameraManager.setTorchMode(mCameraId, false);
                finish();
            } else {
                FileOutputStream flashlight;
                flashlight = new FileOutputStream(FLASHLIGHT_NODE);
                flashlight.write(LIGHTE_OFF);
                flashlight.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.torch);
        mCameraManager = (CameraManager) getApplicationContext().getSystemService(Context.CAMERA_SERVICE);
        Utils.paddingLayout(findViewById(R.id.passButton), 0, ACTIVITY_TOP_PADDING, 0, 0);
        confirmButton();
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            if (!HasFlashlightFile) {
                mCameraId = getCameraId();
                mCameraManager.setTorchMode(mCameraId, true);
            } else {
                FileOutputStream flashlight;
                flashlight = new FileOutputStream(FLASHLIGHT_NODE);
                flashlight.write(LIGHTE_ON);
                flashlight.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getCameraId() throws CameraAccessException {
        String[] ids = mCameraManager.getCameraIdList();
        for (String id : ids) {
            CameraCharacteristics c = mCameraManager.getCameraCharacteristics(id);
            Boolean flashAvailable = c.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
            Integer lensFacing = c.get(CameraCharacteristics.LENS_FACING);
            if (flashAvailable != null && flashAvailable
                    && lensFacing != null && lensFacing == CameraCharacteristics.LENS_FACING_FRONT) {
                return id;
            }
        }
        return null;
    }
}
