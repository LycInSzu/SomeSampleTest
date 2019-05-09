package com.pri.factorytest.CameraFront;

import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Camera.Size;
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

//added by tangan-begin
//added by tangan-end

public class CameraFront extends PrizeBaseActivity implements SurfaceHolder.Callback {

    private Camera mCamera = null;
    private Button takeButton;
    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    public static final String ZSD_MODE_ON = "on";
    public static final String ZSD_MODE_OFF = "off";
    private WindowManager.LayoutParams lp;

    @Override
    public void finish() {
        stopCamera();
        super.finish();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_front);
        lp = getWindow().getAttributes();
        lp.screenBrightness = 1.0f;
        getWindow().setAttributes(lp);
        mSurfaceView = (SurfaceView) findViewById(R.id.mSurfaceView);
        Utils.paddingLayout(findViewById(R.id.mSurfaceView), 0, ACTIVITY_TOP_PADDING, 0, 0);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(CameraFront.this);
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        bindView();
    }

    void bindView() {

        takeButton = (Button) findViewById(R.id.take_picture);
        takeButton.setOnClickListener(new Button.OnClickListener() {

            public void onClick(View arg0) {

                takeButton.setVisibility(View.GONE);
                try {
                    if (mCamera != null) {
                        takePicture();
                    } else {
                        finish();
                    }
                } catch (Exception e) {

                }
            }
        });
        confirmButton();

    }

    public void surfaceCreated(SurfaceHolder surfaceholder) {
        int oritationAdjust = 0;
        try {
            mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
            mCamera.setDisplayOrientation(oritationAdjust);
        } catch (Exception exception) {
            exception.printStackTrace();
            showToast(getString(R.string.cameraback_fail_open));
            mCamera = null;
        }

        if (mCamera == null) {
            finish();
        } else {
            try {
                setCameraParameters();
            } catch (Exception exception) {
                mCamera.release();
                mCamera = null;
                finish();
            }
        }
    }

    private void setCameraParameters() throws Exception{
        if (mCamera != null) {
            Camera.Parameters parameters = mCamera.getParameters();
            List<Size> previewSizeList = parameters.getSupportedPreviewSizes();
            int size = previewSizeList.size();
            int previewSizeWidth = previewSizeList.get(size - 1).width;
            int previewSizeHeight = previewSizeList.get(size - 1).height;
            int maxPreviewSizeWidth = 0;
            int maxpreviewSizeHeight = 0;
            for (int i = 1; i <= size; i++) {
                int listWidth = previewSizeList.get(size - i).width;
                int listHeight = previewSizeList.get(size - i).height;
                Log.e("xxx", "previewSizeWidthi=" + (double) listWidth + ",previewSizeHeighti=" + listHeight);
                if (Math.abs((double) listWidth / listHeight - (double) 4 / 3) < 0.01) {

                    if (listWidth > maxPreviewSizeWidth) {
                        maxPreviewSizeWidth = listWidth;
                        maxpreviewSizeHeight = listHeight;
                    }
                    //break;
                }
            }
            previewSizeWidth = maxPreviewSizeWidth;
            previewSizeHeight = maxpreviewSizeHeight;
            Log.e("xxx", "previewSizeWidth=" + previewSizeWidth + ",previewSizeHeight=" + previewSizeHeight);
            parameters.setPreviewSize(previewSizeWidth, previewSizeHeight);
            parameters.setPictureFormat(PixelFormat.JPEG);
            if (mCamera.getParameters().getSupportedFlashModes() != null && mCamera.getParameters().getSupportedFlashModes().contains(Camera.Parameters.FLASH_MODE_ON)) {
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
            }

            mCamera.setParameters(parameters);
            //mCamera.setDisplayOrientation(90);
            fixCameraDisplayOrientation();
            mCamera.setPreviewDisplay(mSurfaceHolder);
            mCamera.startPreview();
        }
    }

    private void fixCameraDisplayOrientation() {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_BACK, info);
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

    public void surfaceChanged(SurfaceHolder surfaceholder, int format, int w, int h) {
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

    private ShutterCallback mShutterCallback = new ShutterCallback() {

        public void onShutter() {

            try {
                takeButton.setVisibility(View.GONE);
                mButtonFail.setVisibility(View.VISIBLE);
                mButtonPass.setVisibility(View.VISIBLE);
            } catch (Exception e) {

            }
        }
    };

    private PictureCallback rawPictureCallback = new PictureCallback() {

        public void onPictureTaken(byte[] _data, Camera _camera) {

            try {
                takeButton.setVisibility(View.GONE);
                mButtonFail.setVisibility(View.VISIBLE);
                mButtonPass.setVisibility(View.VISIBLE);
            } catch (Exception e) {

            }
        }
    };

    private PictureCallback jpegCallback = new PictureCallback() {

        public void onPictureTaken(byte[] _data, Camera _camera) {

            try {
                takeButton.setVisibility(View.GONE);
                mButtonFail.setVisibility(View.VISIBLE);
                mButtonPass.setVisibility(View.VISIBLE);
            } catch (Exception e) {

            }
        }
    };

    public final class AutoFocusCallback implements
            android.hardware.Camera.AutoFocusCallback {

        public void onAutoFocus(boolean focused, Camera camera) {

            if (focused) {
                takePicture();
            }
        }
    };

    private void stopCamera() {
        if (mCamera != null) {
            try {
                mCamera.setPreviewCallback(null);
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void showToast(String str) {
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }
}
