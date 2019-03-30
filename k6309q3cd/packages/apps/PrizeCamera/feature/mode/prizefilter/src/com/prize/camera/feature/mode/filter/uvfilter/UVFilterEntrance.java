package com.prize.camera.feature.mode.filter.uvfilter;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.util.Log;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.ViewGroup;
import com.prize.camera.feature.mode.filter.uvfilter.UVFilterEntrance;

import com.android.camera.uvfilter.UVFilterAdapter;

import java.util.List;

/**
 * Created by Abel on 2018/5/30/0030.
 */

public class UVFilterEntrance {

    private static final String TAG = "UVFilterEntrance";

    private UVFilterUI        mUVFilterUI;
    private List<SurfaceView> mSurfaceViews;
    private UVFilterAdapter   mUVFilterAdapter;

    public UVFilterEntrance(Context context){
        mUVFilterAdapter = new UVFilterAdapter(context);
        //mUVFilterAdapter.rotate90(true);
    }

    public UVFilterEntrance(ViewGroup rootView, Context context, int itemWidth, int itemHeight) {
        mUVFilterAdapter = new UVFilterAdapter(context);
        //mUVFilterAdapter.rotate90(true);
        mUVFilterUI      = new UVFilterUI();
        mSurfaceViews    = mUVFilterUI.initUVFilterSubItem(rootView, itemWidth, itemHeight);
    }

    public SurfaceTexture init(TextureView textureView) {
        Log.e(TAG, "jiangym init == start");
        SurfaceTexture surfaceTexture = null;

        if (null == textureView) {
            Log.e(TAG, "init null == textureView err");
            return null;
        }

        if (null != mUVFilterAdapter) {
            Log.e(TAG, "jiangym init 2222");
            surfaceTexture = mUVFilterAdapter.init(textureView);
            Log.e(TAG, "jiangym init 3333");
        } else {
            Log.e(TAG, "init null == mUVFilterAdapter err");
        }

        Log.e(TAG, "jiangym init == end surfaceTexture="+surfaceTexture);
        return surfaceTexture;
    }

    public void update(int id) {
        if (id < 0 || id > 100) {
            Log.e(TAG, "update id < 0 || id > 100 err id:" + id);
            return;
        }

        if (null != mUVFilterAdapter) {
            mUVFilterAdapter.update(id);
        } else {
            Log.e(TAG, "update null == mUVFilterAdapter err");
        }
    }

    public void destroy() {
        if (null != mUVFilterAdapter) {
            mUVFilterAdapter.destroy();
        } else {
            Log.e(TAG, "destroy null == mUVFilterAdapter err");
        }
    }

    public void init(int cameraId) {
        if (null != mUVFilterUI) {
            mUVFilterAdapter.init(mSurfaceViews, mUVFilterUI.getWidth(), mUVFilterUI.getHeight(), cameraId);
        } else {
            Log.e(TAG, "init s null == mUVFilterUI err");
        }
    }

    public void update(byte[] data, int width, int height, int format) {
        if (null != mUVFilterUI) {
            mUVFilterAdapter.update(data, width, height, format, 0);
        } else {
            Log.e(TAG, "update s null == mUVFilterUI err");
        }
    }

    public void destroySP() {
        if (null != mUVFilterUI) {
            mUVFilterAdapter.destroySP();
        } else {
            Log.e(TAG, "init s null == mUVFilterUI err");
        }
    }

    public void updateOnce(String yuvFileName, int width, int height) {
        if (null != mUVFilterUI) {
            mUVFilterAdapter.updateOnce(yuvFileName, width, height);
        } else {
            Log.e(TAG, "updateOnce s null == mUVFilterUI err");
        }
    }
}
