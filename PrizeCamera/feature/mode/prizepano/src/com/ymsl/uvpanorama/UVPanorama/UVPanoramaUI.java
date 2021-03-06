package com.ymsl.uvpanorama.UVPanorama;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import com.mediatek.camera.R;
import com.ymsl.uvpanorama.UVPanorama.Panorama.IUVPanoCallback;

public class UVPanoramaUI {

    private static final String TAG = "UVPanoramaUI";

    private static final float     mScale     = 0.12f;//0.1667f;
    private static final float     mMarginTop = 0.3f;
    private static final float     mRatio     = 9.0f / 16;

    private UVPanoramaInterface mUVPanoramaInterface;

    private int mScreenWidth       = 0;
    private int mScreenHeight      = 0;

    private int mMaxThumbWidth     = 0;

    private Bitmap mBmpArrowL2R;
    private Bitmap mBmpArrowR2L;
    public UVPanoramaUI(Activity activity, RelativeLayout mPanoramaLayout, int width, int height, boolean isFrontCamera) {

        mScreenWidth  = width;
        mScreenHeight = height;

        Log.e(TAG, "UVPanoramaUI isFrontCamera:" + isFrontCamera);
        //LayoutInflater layoutInflater = LayoutInflater.from(activity);
        //layoutInflater.inflate(R.layout.panorama_layout, root, true);
        //RelativeLayout mPanoramaLayout = root.findViewById(R.id.panorama_layout);


        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)mPanoramaLayout.getLayoutParams();
        params.height    = (int)(height * mScale);
        /*if(isFrontCamera){
            params.width     = (int)(params.height * mRatio * 2.6);
        }else*/{
            params.width     = ViewGroup.LayoutParams.MATCH_PARENT;
        }
        params.topMargin = (int)(height * mMarginTop);
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
        params.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
        mPanoramaLayout.setLayoutParams(params);


        mSmallPreview = mPanoramaLayout.findViewById(R.id.small_preview);
        RelativeLayout.LayoutParams spParams = (RelativeLayout.LayoutParams)mSmallPreview.getLayoutParams();
        spParams.height  = mSmallPreviewHeight = (int)(height * mScale);
        spParams.width   = mSmallPreviewWidth  = (int)(params.height * mRatio);
        if (isFrontCamera) {
            spParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        } else {
            spParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        }
        mSmallPreview.setLayoutParams(spParams);
        mSmallPreview.setVisibility(View.VISIBLE);

        mThumbPreview = mPanoramaLayout.findViewById(R.id.thumb_preview);
        RelativeLayout.LayoutParams tpParams = (RelativeLayout.LayoutParams)mThumbPreview.getLayoutParams();
        tpParams.width   = ViewGroup.LayoutParams.WRAP_CONTENT;
        tpParams.height  = (int)(height * mScale);

        if (isFrontCamera) {
            tpParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        } else {
            tpParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        }

        tpParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        mThumbPreview.setLayoutParams(tpParams);
        mThumbPreview.setVisibility(View.INVISIBLE);

        mBmpArrowL2R = BitmapFactory.decodeResource(activity.getResources(), R.drawable.ltr);
        mBmpArrowR2L = BitmapFactory.decodeResource(activity.getResources(), R.drawable.rtl);

        if (isFrontCamera) {
            mMaxThumbWidth = mScreenWidth / 3;
        } else {
            mMaxThumbWidth = mScreenWidth;
        }
    }

    private SurfaceView mSmallPreview;
    private SurfaceView mThumbPreview;

    private int mSmallPreviewWidth;
    private int mSmallPreviewHeight;

    public void initSamllPreview(Context context, int cameraId) {
        mUVPanoramaInterface = new UVPanoramaInterface(context, cameraId);
        mUVPanoramaInterface.initSmallPreview(mSmallPreview, mSmallPreviewWidth, mSmallPreviewHeight);
        mSmallPreview.setVisibility(View.VISIBLE);
    }

    public void updateSmallPreview(Object object) {
        if (null != mUVPanoramaInterface) {
            mUVPanoramaInterface.updateSmallPreview(object);
        } else {
            Log.e(TAG, "updateSmallPreview null == mUVPanoramaInterface err");
        }
    }

    public void destroySmallPreview() {
        //mSmallPreview.setVisibility(View.INVISIBLE);
        if (null != mUVPanoramaInterface) {
            mUVPanoramaInterface.destroySmallPreview();
        } else {
            Log.e(TAG, "destroySmallPreview null == mUVPanoramaInterface err");
        }
    }

    public void initThumbPreview(int previewWidth, int previewHeight, int format, int capDirection, boolean isFrontCamera) {

        if (null != mUVPanoramaInterface) {
            mUVPanoramaInterface.initThumbPreview(mThumbPreview, previewWidth, previewHeight, mMaxThumbWidth,
                    mSmallPreviewWidth, mSmallPreviewHeight, format, capDirection, isFrontCamera);

            mUVPanoramaInterface.setThumbPreviewScreenSize(mScreenWidth, mScreenHeight, mScale);
        } else {
            Log.e(TAG, "initThumbPreview null == mUVPanoramaInterface err");
        }

        mThumbPreview.setVisibility(View.VISIBLE);
        mSmallPreview.setVisibility(View.INVISIBLE);
    }

    public void setThumbPreviewInteractive(IUVPanoCallback iuvPanoCallback,
                                           UVPanoramaInterface.PanoPictureCallback panoPictureCallback) {
        if (null != mUVPanoramaInterface) {
            mUVPanoramaInterface.setThumbPreviewInteractive(mBmpArrowL2R, mBmpArrowR2L,
                    iuvPanoCallback, panoPictureCallback);
        } else {
            Log.e(TAG, "setThumbPreviewInteractive null == mUVPanoramaInterface err");
        }
    }

    public void startThumbPreview(Object object) {
        if (null != mUVPanoramaInterface) {
            mUVPanoramaInterface.startThumbPreview(object);
        } else {
            Log.e(TAG, "startThumbPreview null == mUVPanoramaInterface err");
        }
    }

    public void stopThumbPreview() {

        mSmallPreview.setVisibility(View.VISIBLE);
        mThumbPreview.setVisibility(View.INVISIBLE);

        if (null != mUVPanoramaInterface) {
            mUVPanoramaInterface.stopThumbPreview();
        } else {
            Log.e(TAG, "destroyThumbPreview null == mUVPanoramaInterface err");
        }
    }
}
