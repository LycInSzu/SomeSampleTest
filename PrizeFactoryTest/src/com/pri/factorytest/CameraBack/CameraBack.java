package com.pri.factorytest.CameraBack;

import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.pri.factorytest.PrizeBaseActivity;
import com.pri.factorytest.R;
import com.pri.factorytest.util.Utils;

import java.util.List;

public class CameraBack extends PrizeBaseActivity implements SurfaceHolder.Callback {

    private Camera mCamera = null;
    private Button takeButton;
    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    private Object mFaceDetectionSync = new Object();
    final static String TAG = "CameraBack";
    public static final String ZSD_MODE_ON = "on";
    public static final String ZSD_MODE_OFF = "off";

    /* prize-xucm-20160127-add FocuseView-start */
    private FocuseView mFocuseView;
    private final AutoFocusCallback mAutoFocusCallback = new AutoFocusCallback();
    /* prize-xucm-20160127-add FocuseView-end */

    private WindowManager.LayoutParams lp;

    private volatile boolean mCaptureFlag = false;

    @Override
    public void finish() {
        stopCamera();
        super.finish();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_back);
        lp = getWindow().getAttributes();
        lp.screenBrightness = 1.0f;
        getWindow().setAttributes(lp);
        Utils.paddingLayout(findViewById(R.id.mSurfaceView), 0, ACTIVITY_TOP_PADDING, 0, 0);
        mSurfaceView = (SurfaceView) findViewById(R.id.mSurfaceView);
        mSurfaceView.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (mCaptureFlag) {
                    return true;
                }
                // TODO Auto-generated method stub
                mFocuseView.moveView(event.getRawX() - mFocuseView.getWidth() / 2,
                        event.getRawY() - mFocuseView.getHeight() / 2);
                mFocuseView.showStart();
                try {
                    if (mCamera != null && isSupportFocusMode(Parameters.FOCUS_MODE_AUTO, mCamera
                            .getParameters().getSupportedFocusModes())) {
                        Camera.Parameters parameters = mCamera.getParameters();
                        parameters.setFocusMode(Parameters.FOCUS_MODE_AUTO);
                        mCamera.setParameters(parameters);
                        mCamera.cancelAutoFocus();
                        mCamera.autoFocus(mAutoFocusCallback);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            }
        });
		/* prize-xucm-20160127-add FocuseView-start */
        mFocuseView = (FocuseView) findViewById(R.id.focuseview);
		/* prize-xucm-20160127-add FocuseView-end */
        bindView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(CameraBack.this);
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mSurfaceView.setVisibility(View.VISIBLE);
        if (takeButton != null && mButtonPass != null && mButtonFail != null) {
            takeButton.setVisibility(View.VISIBLE);
            mButtonPass.setVisibility(View.GONE);
            mButtonFail.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSurfaceView.setVisibility(View.GONE);
    }

    void bindView() {
        takeButton = (Button) findViewById(R.id.take_picture);
        takeButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View arg0) {
                mCaptureFlag = true;
                takeButton.setVisibility(View.GONE);
                try {
                    synchronized (mFaceDetectionSync) {
                        if (mCamera != null) {
                            takePicture();
                        } else {
                            finish();
                        }
                    }
                } catch (Exception e) {
                    fail(getString(R.string.autofocus_fail));
                }
            }
        });
        confirmButton();

    }

    public void surfaceCreated(SurfaceHolder surfaceholder) {
        try {
            mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
        } catch (Exception exception) {
            exception.printStackTrace();
            toast(getString(R.string.cameraback_fail_open));
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

    private void setCameraParameters() throws Exception {
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
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
            parameters.setRotation(CameraInfo.CAMERA_FACING_BACK);
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            mCamera.setParameters(parameters);
            //mCamera.setDisplayOrientation(90);
            fixCameraDisplayOrientation();
            mCamera.setPreviewDisplay(mSurfaceHolder);
            mCamera.startPreview();
            mCamera.cancelAutoFocus();
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
                mButtonPass.setVisibility(View.VISIBLE);
                mButtonFail.setVisibility(View.VISIBLE);
            } catch (Exception e) {

            }
        }
    };

    private PictureCallback rawPictureCallback = new PictureCallback() {

        public void onPictureTaken(byte[] _data, Camera _camera) {
            try {
                takeButton.setVisibility(View.GONE);
                mButtonPass.setVisibility(View.VISIBLE);
                mButtonFail.setVisibility(View.VISIBLE);
            } catch (Exception e) {

            }
        }
    };

    private PictureCallback jpegCallback = new PictureCallback() {

        public void onPictureTaken(byte[] _data, Camera _camera) {
            try {
                takeButton.setVisibility(View.GONE);
                mButtonPass.setVisibility(View.VISIBLE);
                mButtonFail.setVisibility(View.VISIBLE);
            } catch (Exception e) {

            }
        }
    };

    public final class AutoFocusCallback implements
            android.hardware.Camera.AutoFocusCallback {

        public void onAutoFocus(boolean focused, Camera camera) {
            mFocuseView.showSuccess(true);
        }
    };

    private void stopCamera() {
        synchronized (mFaceDetectionSync) {
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
    }

    void fail(String msg) {
        toast(msg);
        setResult(RESULT_CANCELED);
        finish();
    }

    public void toast(String s) {
        if (s == null)
            return;
        Toast.makeText(this, s + "", Toast.LENGTH_SHORT).show();
    }

    private boolean isSupportFocusMode(Object value, List<?> supported) {
        return supported == null ? false : supported.indexOf(value) >= 0;
    }

    @Override
    protected void onStop() {
        mFocuseView.moveView(0, 0);
        super.onStop();
    }
}
