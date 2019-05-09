package com.pri.factorytest.CameraBackSub;

import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.pri.factorytest.PrizeBaseActivity;
import com.pri.factorytest.R;
import com.pri.factorytest.util.Utils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CameraBackSub extends PrizeBaseActivity implements SurfaceHolder.Callback {

    private Camera mCamera = null;
    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    public static final String ZSD_MODE_ON = "on";
    public static final String ZSD_MODE_OFF = "off";
    private WindowManager.LayoutParams lp;
    private Button mTakePicBtn;
    private int mCameraId;

    @Override
    public void finish() {
        stopCamera();
        super.finish();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCameraId = getIntent().getIntExtra("rear_camer_id", 2);
        setContentView(R.layout.camera_back_sub);
        lp = getWindow().getAttributes();
        lp.screenBrightness = 1.0f;
        getWindow().setAttributes(lp);
        mSurfaceView = (SurfaceView) findViewById(R.id.mSurfaceView);
        Utils.paddingLayout(findViewById(R.id.mSurfaceView), 0, ACTIVITY_TOP_PADDING, 0, 0);
        confirmButton();
        mTakePicBtn = findViewById(R.id.take_picture);
        mTakePicBtn.setOnClickListener(view -> takePicture());
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(CameraBackSub.this);
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mSurfaceView.setVisibility(View.VISIBLE);
        if (mTakePicBtn != null && mButtonPass != null && mButtonFail != null) {
            mTakePicBtn.setVisibility(View.VISIBLE);
            mButtonPass.setVisibility(View.GONE);
            mButtonFail.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSurfaceView.setVisibility(View.GONE);
    }

    public void surfaceCreated(SurfaceHolder surfaceholder) {
        try {
            mCamera = Camera.open(mCameraId);
        } catch (Exception e) {
            e.printStackTrace();
            showToast(getString(R.string.cameraback_fail_open));
            mCamera = null;
        }

        if (mCamera == null) {
            finish();
        } else {
            try {
                setCameraParameters();
            } catch (Exception e) {
                e.printStackTrace();
                mCamera.release();
                mCamera = null;
                finish();
            }
        }
    }

    public void surfaceChanged(SurfaceHolder surfaceholder, int format, int w, int h) {
    }

    private void setCameraParameters() throws Exception {
        if (mCamera != null) {
            Camera.Parameters parameters = mCamera.getParameters();
            List<Camera.Size> previewSizeList = parameters.getSupportedPreviewSizes();
            List<Camera.Size> size4_3 = previewSizeList.stream().filter(
                    x -> (double) x.width / x.height - (double) 4 / 3 < 0.01).collect(Collectors.toList());
            Camera.Size maxSize = size4_3.stream().max((s1, s2) -> new Integer(s1.width).compareTo(new Integer(s2.width))).get();
            int previewSizeWidth = maxSize.width;
            int previewSizeHeight = maxSize.height;
            Log.e("CameraBackSub", "previewSizeWidth=" + previewSizeWidth + ",previewSizeHeight=" + previewSizeHeight);
            parameters.setPreviewSize(previewSizeWidth, previewSizeHeight);
            parameters.setPictureFormat(PixelFormat.JPEG);
            //parameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
            parameters.setRotation(Camera.CameraInfo.CAMERA_FACING_BACK);
            //parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            mCamera.setParameters(parameters);
            //mCamera.setDisplayOrientation(270);
            fixCameraDisplayOrientation();
            mCamera.setPreviewDisplay(mSurfaceHolder);
            mCamera.startPreview();
            mCamera.cancelAutoFocus();
        }
    }

    private void fixCameraDisplayOrientation() {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(2, info);
        int rotation = this.getWindowManager().getDefaultDisplay().getRotation();
        int degree = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degree = 0;
                break;
            case Surface.ROTATION_90:
                degree = 90;
                break;
            case Surface.ROTATION_180:
                degree = 180;
                break;
            case Surface.ROTATION_270:
                degree = 270;
                break;
        }
        int result = (info.orientation - degree + 360) % 360;
        if (mCamera != null) {
            mCamera.setDisplayOrientation(result);
        }
    }

    public void surfaceDestroyed(SurfaceHolder surfaceholder) {
        surfaceholder.removeCallback(this);
        stopCamera();
    }

    private void takePicture() {
        if (mCamera != null) {
            try {
                mCamera.takePicture(mShutterCallback, rawPictureCallback,
                        jpegCallback);
            } catch (Exception e) {
                e.printStackTrace();
                finish();
            }
        } else {
            finish();
        }
    }

    private ShutterCallback mShutterCallback = () -> {
        mTakePicBtn.setVisibility(View.GONE);
        mButtonPass.setVisibility(View.VISIBLE);
        mButtonFail.setVisibility(View.VISIBLE);
    };

    private PictureCallback rawPictureCallback = (a, b) -> {
        mTakePicBtn.setVisibility(View.GONE);
        mButtonPass.setVisibility(View.VISIBLE);
        mButtonFail.setVisibility(View.VISIBLE);
    };

    private PictureCallback jpegCallback = (a, b) -> {
        mTakePicBtn.setVisibility(View.GONE);
        mButtonPass.setVisibility(View.VISIBLE);
        mButtonFail.setVisibility(View.VISIBLE);
    };

    public final class AutoFocusCallback implements
            android.hardware.Camera.AutoFocusCallback {
        public void onAutoFocus(boolean focused, Camera camera) {
            if (focused) {
                //takePicture();
            }
        }
    }

    private void stopCamera() {
        Optional.ofNullable(mCamera).ifPresent(x -> {
            try {
                x.setPreviewCallback(null);
                x.stopPreview();
                x.release();
                x = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void showToast(String str) {
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }
}
