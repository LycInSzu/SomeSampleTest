
package com.wtk.screenshot.util.imageZoom;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region;
import android.view.MotionEvent;

import com.wtk.screenshot.util.imageZoom.SquareLightView.ModifyMode;

public class FreeLightView {
    /* Common */
    // Default
    private static final float TOUCH_TOLERANCE = 4;

    // Util
    private Paint mPathPaint = new Paint();
    private Paint showPaint = new Paint();
    private final Paint mOutPaint = new Paint();
    private Paint mSavePrePaint = new Paint(Paint.DITHER_FLAG);
    private Path mPath = new Path();

    // Check whether in the path
    // private Region region = new Region();

    // Flag
    private float startX, startY;
    private boolean showPath = true;
    //private Bitmap mSavePreBitmap;
    // private RectF mSaveBitmapRec;
    private Path pt = new Path();
    private RectF mF = new RectF();
    private Matrix verMatrix = new Matrix();

    /* View */
    private CropImageView mCropImageView;

    public FreeLightView(CropImageView ctx) {
        mCropImageView = ctx;
        // Set Paint
        mPathPaint.setAntiAlias(true);
        mPathPaint.setDither(true);
        mPathPaint.setColor(Color.RED);
        mPathPaint.setStyle(Paint.Style.STROKE);
        mPathPaint.setStrokeJoin(Paint.Join.ROUND);
        mPathPaint.setStrokeCap(Paint.Cap.ROUND);
        mPathPaint.setStrokeWidth(5);
        mPathPaint.setPathEffect(new DashPathEffect(new float[]{4, 8}, 0));

        showPaint.setAntiAlias(true);
        showPaint.setDither(true);
        showPaint.setColor(Color.RED);
        showPaint.setStyle(Paint.Style.STROKE);
        showPaint.setStrokeJoin(Paint.Join.ROUND);
        showPaint.setStrokeCap(Paint.Cap.ROUND);
        showPaint.setStrokeWidth(8);

        // mSaveBitmapRec = new RectF();

        mOutPaint.setARGB(125, 50, 50, 50);
    }

    protected void sizeChanged(int w, int h, int oldw, int oldh) {
        //mSavePreBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565);
    }

    protected void layout(boolean changed, int left, int top, int right,
                          int bottom) {
    }

    protected void draw(Canvas canvas) {
        // rebuit/save Pre-View
        // canvas.drawBitmap(mSavePreBitmap, 0, 0, mSavePrePaint);
        if (showPath) {
            if (mCropImageView.getViewMode() == CropImageView.PAINT_SCREEN_MODE) {
                canvas.drawPath(mPath, showPaint);
            } else {
                canvas.drawPath(mPath, mPathPaint);
            }
        } else if (mCropImageView.getViewMode() == CropImageView.FREE_SCREEN_MODE) {
            pt.reset();
            mCropImageView.mCropPath.transform(
                    mCropImageView.cropInScreenMatrix, pt);
            canvas.clipPath(pt, Region.Op.DIFFERENCE);
            canvas.drawPath(pt, mPathPaint);
            mF.set(mCropImageView.mDrawRect);
            // mF.inset(CropImageView.CROP_BOUND_PADDING,
            // CropImageView.CROP_BOUND_PADDING);
            canvas.drawRect(mF, mOutPaint);
        } else if (mCropImageView.getViewMode() == CropImageView.PAINT_SCREEN_MODE) {
            pt.reset();
            mCropImageView.mCropPath.transform(
                    mCropImageView.cropInScreenMatrix, pt);
            canvas.drawPath(pt, showPaint);
        }
    }

    protected boolean touchEvent(MotionEvent event) {
        if (mCropImageView == null) {
            return true;
        }
        ModifyMode mMode = ModifyMode.None;
        if (mCropImageView.mSquareLightView != null) {
            mMode = mCropImageView.mSquareLightView.getMode();
        }
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (mMode == ModifyMode.None) {
                    showPath = true;
                    // checkinView((int) x, (int) y);
                    touchDown(x, y);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (mMode == ModifyMode.None) {
                    touchMove(x, y);
                }
                break;
            case MotionEvent.ACTION_UP:
                if (showPath) {
                    showPath = false;
                    touchUp();
                }
                break;
        }
        return true;
    }

    public void reset() {
        mPath.reset();
        clearAll();
    }

    private void touchDown(float x, float y) {
        mPathPaint.setStyle(Paint.Style.STROKE);
        if (mCropImageView.getViewMode() == CropImageView.FREE_SCREEN_MODE) {
            clearAll();
        }
        mPath.moveTo(x, y);
        startX = x;
        startY = y;
    }

    private void clearAll() {
        mPath.reset();
        mCropImageView.mCropPath.reset();
        mCropImageView.mCropRectF.setEmpty();
        mCropImageView.mDrawRect = mCropImageView.computeLayout();
    }

    private void touchMove(float x, float y) {
        float dx = Math.abs(x - startX);
        float dy = Math.abs(y - startY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.quadTo(startX, startY, (x + startX) / 2, (y + startY) / 2);
            startX = x;
            startY = y;
        }
    }

    private void touchUp() {
        // mPathPaint.setStyle(Paint.Style.FILL);
        if (mCropImageView.getViewMode() == CropImageView.FREE_SCREEN_MODE) {
            mPath.close();
        }
        saveView();
        if (mCropImageView.mDrawRect == null
                || mCropImageView.mDrawRect.width() < 5) {
            mCropImageView.reset();
        }
    }

    private void saveView() {
        if (mCropImageView == null) {
            return;
        }
        verMatrix.reset();
        if (!mCropImageView.cropInScreenMatrix.invert(verMatrix)) {
            return;
        }
        mPath.transform(verMatrix, mCropImageView.mCropPath);
        mCropImageView.mCropPath.computeBounds(mCropImageView.mCropRectF, true);
        mCropImageView.mCropRectF.inset(
                -CropImageView.CROP_BOUND_PADDING
                        * mCropImageView.mCropRectF.width()
                        / mCropImageView.computeLayout().width(),
                -CropImageView.CROP_BOUND_PADDING
                        * mCropImageView.mCropRectF.width()
                        / mCropImageView.computeLayout().width());
        mCropImageView.mDrawRect = mCropImageView.computeLayout();
        if (mCropImageView.getViewMode() == CropImageView.FREE_SCREEN_MODE) {
            mCropImageView.centerBasedOnHighlightView();
        }
        // region.setPath(mPath, new Region((int) mSaveBitmapRec.left,
        // (int) mSaveBitmapRec.top, (int) mSaveBitmapRec.right,
        // (int) mSaveBitmapRec.bottom));
    }

    // private boolean checkinView(int x, int y) {
    // Log.i(TAG, "isInArea=" + region.contains(x, y));
    // return region.contains(x, y);
    // }
}