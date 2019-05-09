/*
 * Copyright (C) 2013-2017, Shenzhen Huiding Technology Co., Ltd.
 * All Rights Reserved.
 */

package com.goodix.fingerprint.shenzhen.preview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private static final String TAG = "CameraPreview";

    private SurfaceHolder mSurfaceHolder;
    private Rect mSurfaceRect;

    private Bitmap mOriginalBitmap = null;

    private boolean mSurfaceCreated = false;

    public CameraPreview(Context context) {
        this(context, null);
        init();
    }

    public CameraPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        Log.d(TAG, "init");

        mSurfaceHolder = getHolder();
        mSurfaceHolder.addCallback(this);
    }

    public void updateBitmap(int[] bmp, int width, int height) {
        if (mOriginalBitmap != null) {
            mOriginalBitmap.recycle();
        }

        int[] display = new int[bmp.length];
        int pixel = 0;
        for (int i = 0; i < bmp.length; i++) {
            pixel = bmp[i] & 0xFF;
            display[i] = 0xFF000000 | (pixel << 16) | (pixel << 8) | pixel;
        }

        mOriginalBitmap = Bitmap.createBitmap(display, width, height, Bitmap.Config.ARGB_8888);
        displayBitmap();
        Log.d(TAG, "=========updateBitmap=========");
    }

    public void updateBitmap(byte[] bmp, int width, int height) {
        if (mOriginalBitmap != null) {
            mOriginalBitmap.recycle();
        }

        int[] display = new int[bmp.length];
        for (int i = 0; i < bmp.length; i++) {
            int pixel = (int) bmp[i];
            pixel = pixel & 0xFF;
            display[i] = 0xFF000000 | (pixel << 16) | (pixel << 8) | pixel;
        }

        Bitmap tmpBmp = Bitmap.createBitmap(display, width, height, Bitmap.Config.ARGB_8888);
        // the bitmap is upside down vertically, here we setup a matrix to revert it
        Matrix flipMatrix = new Matrix();
        flipMatrix.setScale(1, -1, width/2, height/2);
        mOriginalBitmap = Bitmap.createBitmap(tmpBmp, 0, 0, width, height, flipMatrix, true);
        displayBitmap();
        Log.d(TAG, "=========updateBitmap[byte]=========");
    }

    private void displayBitmap() {
        if (!mSurfaceCreated) {
            Log.d(TAG, "=========mSurfaceCreated not create=========");
            return;
        }

        synchronized (mSurfaceHolder) {
            Canvas canvas = mSurfaceHolder.lockCanvas();

            if (canvas == null) {
                return;
            }

            if (mOriginalBitmap != null) {
                canvas.drawBitmap(mOriginalBitmap, null, mSurfaceRect, null);
            } else {
                Log.d(TAG, "=========mOriginalBitmap not create=========");
                canvas.drawColor(Color.GRAY);
            }

            mSurfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        Log.d(TAG, "onAttachedToWindow");
    }

    @Override
    protected void onDetachedFromWindow() {
        Log.d(TAG, "onDetachedFromWindow");
        super.onDetachedFromWindow();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "surfaceCreated");

        mSurfaceCreated = true;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.d(TAG, "surfaceChanged, width = " + width + ", height = " + height);
        mSurfaceRect = new Rect(0, 0, width, height);
//        if (mOriginalBitmap != null) {
//            mOriginalBitmap.recycle();
//        }
//        mOriginalBitmap = null;
        displayBitmap();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(TAG, "surfaceDestroyed");
        if (mOriginalBitmap != null) {
            mOriginalBitmap.recycle();
        }
        mOriginalBitmap = null;
        mSurfaceCreated = false;
    }
}
