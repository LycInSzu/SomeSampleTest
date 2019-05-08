package com.ymsl.uvpanorama.UVPanorama;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.SurfaceView;

import com.ymsl.uvpanorama.UVPanorama.Panorama.IUVPanoCallback;
import com.ymsl.uvpanorama.UVPanorama.Panorama.IUVPanoSmallPreview;
import com.ymsl.uvpanorama.UVPanorama.Panorama.IUVPanoThumbPreview;
import com.ymsl.uvpanorama.UVPanorama.UVPanoUtils.UVPanoLoad;
import com.ymsl.uvpanorama.UVPanorama.UVPanoUtils.UVPanoThumbInterface;

public class UVPanoramaInterface {

    private static final String TAG = "UVPanoramaInterface";

    private IUVPanoSmallPreview  mIUVPanoSmallPreview;

    private IUVPanoThumbPreview  mIUVPanoThumbPreview;
    private UVPanoThumbInterface mUVPanoThumbInterface;

    public UVPanoramaInterface(Context context, int cameraId) {

        UVPanoLoad mUVPanoLoad = new UVPanoLoad(context);
        mIUVPanoSmallPreview = mUVPanoLoad.getSmallPreview();
        mIUVPanoThumbPreview = mUVPanoLoad.getThumbPreview();

        mUVPanoThumbInterface = new UVPanoThumbInterface(mIUVPanoThumbPreview);
        mUVPanoThumbInterface.setCameraId(cameraId);
    }

    public void initSmallPreview(SurfaceView surfaceView, int width, int height) {
        if (null != mIUVPanoSmallPreview) {
            mIUVPanoSmallPreview.initSmallPreview(surfaceView, width, height);
        } else {
            Log.e(TAG, "initSmallPreview null == mUVPanoInterface err");
        }
    }

    public void updateSmallPreview(Object object) {
        if (null != mIUVPanoSmallPreview) {
            mIUVPanoSmallPreview.updateSmallPreview(object);
        } else {
            Log.e(TAG, "updateSmallPreview null == mUVPanoInterface err");
        }
    }

    public void destroySmallPreview() {
        if (null != mIUVPanoSmallPreview) {
            mIUVPanoSmallPreview.destroySmallPreview();
        } else {
            Log.e(TAG, "updateSmallPreview null == mUVPanoInterface err");
        }
    }


    public void initThumbPreview(SurfaceView surfaceView, int previewWidth, int previewHeight, int maxThumbWidth,
                                 int smWidth, int smHeight, int format, int orientation, boolean isFrontCamera) {
        if (null != mUVPanoThumbInterface) {
            mUVPanoThumbInterface.init(surfaceView, previewWidth, previewHeight, maxThumbWidth, smWidth, smHeight, format, orientation, isFrontCamera);
        } else {
            Log.e(TAG, "initThumbPreview null == mUVPanoThumbInterface err");
        }
    }

    public void setThumbPreviewScreenSize(int screenWidth, int screenHeight, float smallPreviewRatio) {
        if (null != mUVPanoThumbInterface) {
            mUVPanoThumbInterface.setThumbPreviewScreenSize(screenWidth, screenHeight, smallPreviewRatio);
        } else {
            Log.e(TAG, "initThumbPreview null == mUVPanoThumbInterface err");
        }
    }

    public void setThumbPreviewInteractive(Bitmap rightArrow, Bitmap leftArrow, IUVPanoCallback iuvPanoCallback,
                                           PanoPictureCallback panoPictureCallback) {
        if (null != mUVPanoThumbInterface) {
            mUVPanoThumbInterface.setThumbPreviewInteractive(rightArrow, leftArrow, iuvPanoCallback, panoPictureCallback);
        } else {
            Log.e(TAG, "update null == mUVThumbThread err");
        }
    }

    public void startThumbPreview(Object object) {
        if (null != mUVPanoThumbInterface) {
            mUVPanoThumbInterface.update(object);
        } else {
            Log.e(TAG, "updateThumbPreview null == mUVPanoThumbInterface err");
        }
    }

    public void stopThumbPreview() {
        if (null != mUVPanoThumbInterface) {
            mUVPanoThumbInterface.destroy();
        } else {
            Log.e(TAG, "destroyThumbPreview null == mUVPanoThumbInterface err");
        }
    }

    public interface PanoPictureCallback {
        void onData(byte[] nv21, int width, int height, int targetWidth);
    }
}
