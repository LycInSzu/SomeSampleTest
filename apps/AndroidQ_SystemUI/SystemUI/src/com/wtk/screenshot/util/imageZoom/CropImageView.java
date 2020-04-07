
package com.wtk.screenshot.util.imageZoom;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import com.wtk.screenshot.util.ShotUtil;
import com.android.systemui.R;

public class CropImageView extends ImageViewTouchBase {
    /* Common */
    // Default
    public static final String TAG = ShotUtil.TAG;
    public static final float CROP_BOUND_PADDING = 10;
    public static final int LOCAL_SCREEN_MODE = 1;
    public static final int PAINT_SCREEN_MODE = 2;
    public static final int FREE_SCREEN_MODE = 3;
    // Util
    public Context mContext;

    // Flag
    public Matrix cropInScreenMatrix;
    public Rect mDrawRect;
    public RectF mCropRectF = new RectF();
    public Path mCropPath = new Path();
    public Rect fullScreenRect = new Rect();
    private int viewMode;
    private float[] coordinates = new float[2];
    private RectF computeRectF = new RectF();
    private Rect computeRect = new Rect();
    private RectF growByRectF = new RectF();
    private Matrix growByMatrix = new Matrix();
    private Bitmap mFullScreenBitMap;

    /* View */
    public SquareLightView mSquareLightView;
    public FreeLightView mFreeLightView;

    public CropImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        /* Common */
        mContext = context;

        // Default
        setCustomAttributes(attrs);
        switch (viewMode) {
            case LOCAL_SCREEN_MODE:
                mSquareLightView = new SquareLightView(this);
                break;
            case PAINT_SCREEN_MODE:
                mFreeLightView = new FreeLightView(this);
                break;
            case FREE_SCREEN_MODE:
                mSquareLightView = new SquareLightView(this);
                mFreeLightView = new FreeLightView(this);
                break;
            default:
                break;
        }
    }

    private void setCustomAttributes(AttributeSet attrs) {
        TypedArray attr = mContext.obtainStyledAttributes(attrs,
                R.styleable.cropImageView);
        viewMode = attr.getInt(R.styleable.cropImageView_viewMode, 1);
        Log.i(TAG, "CropImageView;setCustomAttributes;viewMode=" + viewMode);
    }

    public int getViewMode() {
        return viewMode;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (mSquareLightView != null) {
            mSquareLightView.sizeChanged(w, h, oldw, oldh);
        }
        if (mFreeLightView != null) {
            mFreeLightView.sizeChanged(w, h, oldw, oldh);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right,
                            int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (mBitmapDisplayed.getBitmap() != null) {
            cropInScreenMatrix.set(getImageMatrix());
            mDrawRect = computeLayout();

            if (mSquareLightView != null) {
                mSquareLightView.layout(changed, left, top, right, bottom);
            }
            if (mFreeLightView != null) {
                mFreeLightView.layout(changed, left, top, right, bottom);
            }
            centerBasedOnHighlightView();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //add by wangjian for YWSW-817 20190410 start
        //Log.e("wangjian","CropImageView onDraw = " + canvas.isHardwareAccelerated() + " / " + canvas.isHwBitmapsInSwModeEnabled());
        canvas.setHwBitmapsInSwModeEnabled(true);
        //add by wangjian for YWSW-817 20190410 end
        super.onDraw(canvas);
        if (mSquareLightView != null) {
            mSquareLightView.draw(canvas);
        }
        if (mFreeLightView != null) {
            mFreeLightView.draw(canvas);
        }
    }

    public boolean onMyTouchEvent(MotionEvent event) {
        if (mSquareLightView != null) {
            mSquareLightView.touchEvent(event);
        }
        if (mFreeLightView != null) {
            mFreeLightView.touchEvent(event);
        }
        invalidate();
        return true;
    }

    @Override
    protected void zoomTo(float scale, float centerX, float centerY) {
        super.zoomTo(scale, centerX, centerY);
        cropInScreenMatrix.set(getImageMatrix());
        mDrawRect = computeLayout();
    }

    @Override
    protected void zoomIn() {
        super.zoomIn();
        cropInScreenMatrix.set(getImageMatrix());
        mDrawRect = computeLayout();
    }

    @Override
    protected void zoomOut() {
        super.zoomOut();
        cropInScreenMatrix.set(getImageMatrix());
        mDrawRect = computeLayout();
    }

    @Override
    protected void postTranslate(float deltaX, float deltaY) {
        super.postTranslate(deltaX, deltaY);
        cropInScreenMatrix.set(getImageMatrix());
        mDrawRect = computeLayout();
    }

    public void reset() {
        reset(mFullScreenBitMap);
    }

    public void reset(Bitmap bitmap) {
        // mFullScreenBitMap = mShotUtil.getFullScreenBitmap();
        mFullScreenBitMap = bitmap;
        if (mFullScreenBitMap == null) {
            return;
        }

        // Background
        setImageBitmapResetBase(mFullScreenBitMap, true);
        if (getScale() == 1.0f) {
            center(true, true);
        }

        // Square Light View
        int width = mFullScreenBitMap.getWidth();
        int height = mFullScreenBitMap.getHeight();
        cropInScreenMatrix = getImageMatrix();
        fullScreenRect.set(0, 0, width, height);

        switch (getViewMode()) {
            case LOCAL_SCREEN_MODE:
                int part = (width / 4 < height / 4) ? width * 4 / 5
                        : height * 4 / 5;
                mCropRectF.set((width - part) / 2, (height - part) / 2,
                        (width - part) / 2 + part, (height - part) / 2 + part);
                break;
            case PAINT_SCREEN_MODE:
            case FREE_SCREEN_MODE:
                mCropRectF.setEmpty();
                break;
            default:
                break;
        }

        mDrawRect = computeLayout();

        if (null != mSquareLightView) {
            mSquareLightView.reset();
        }
        if (null != mFreeLightView) {
            mFreeLightView.reset();
        }

        invalidate();
    }

    public void centerBasedOnHighlightView() {
        Rect drawRect = mDrawRect;

        float width = drawRect.width();
        float height = drawRect.height();

        float thisWidth = getWidth();
        float thisHeight = getHeight();

        float z1 = thisWidth / width * .6F;
        float z2 = thisHeight / height * .6F;

        float zoom = Math.min(z1, z2);
        zoom = zoom * this.getScale();
        zoom = Math.max(1F, zoom);

        if ((Math.abs(zoom - getScale()) / zoom) > .1) {
            coordinates[0] = mCropRectF.centerX();
            coordinates[1] = mCropRectF.centerY();
            getImageMatrix().mapPoints(coordinates);
            zoomTo(zoom, coordinates[0], coordinates[1], 300F);
            mHandler.postDelayed(mRunnable, 350);
        } else {
            ensureVisible();
        }
    }

    private Runnable mRunnable = new Runnable() {

        @Override
        public void run() {
            ensureVisible();
        }
    };

    public void ensureVisible() {
        Rect r = mDrawRect;

        int panDeltaX1 = Math.max(0, getLeft() - r.left);
        int panDeltaX2 = Math.min(0, getRight() - r.right);

        int panDeltaY1 = Math.max(0, getTop() - r.top);
        int panDeltaY2 = Math.min(0, getBottom() - r.bottom);

        int panDeltaX = panDeltaX1 != 0 ? panDeltaX1 : panDeltaX2;
        int panDeltaY = panDeltaY1 != 0 ? panDeltaY1 : panDeltaY2;
        if (prePanByMs == -1 && (panDeltaX != 0 || panDeltaY != 0)) {
            panBy(panDeltaX, panDeltaY, 300F);
        }
    }

    public Rect computeLayout() {
        computeRectF.set(mCropRectF.left, mCropRectF.top, mCropRectF.right,
                mCropRectF.bottom);
        cropInScreenMatrix.mapRect(computeRectF);
        computeRect.set(Math.round(computeRectF.left),
                Math.round(computeRectF.top), Math.round(computeRectF.right),
                Math.round(computeRectF.bottom));
        return computeRect;
    }

    void handleMotion(int edge, float dx, float dy) {
        Rect r = computeLayout();
        if (edge == SquareLightView.GROW_NONE) {
            return;
        } else if (edge == SquareLightView.MOVE) {
            moveBy(dx * (mCropRectF.width() / r.width()),
                    dy * (mCropRectF.height() / r.height()));
        } else {
            if (((SquareLightView.GROW_LEFT_EDGE | SquareLightView.GROW_RIGHT_EDGE) & edge) == 0) {
                dx = 0;
            }

            if (((SquareLightView.GROW_TOP_EDGE | SquareLightView.GROW_BOTTOM_EDGE) & edge) == 0) {
                dy = 0;
            }

            if (r.width() + 2 * dx
                    * (((edge & SquareLightView.GROW_LEFT_EDGE) != 0) ? -1 : 1) > mThisWidth) {
                dx = 0;
            }
            if (r.height() + 2 * dy
                    * (((edge & SquareLightView.GROW_TOP_EDGE) != 0) ? -1 : 1) > mThisHeight) {
                dy = 0;
            }

            float xDelta = dx * (mCropRectF.width() / r.width());
            float yDelta = dy * (mCropRectF.height() / r.height());
            growBy((((edge & SquareLightView.GROW_LEFT_EDGE) != 0) ? -1 : 1)
                            * xDelta,
                    (((edge & SquareLightView.GROW_TOP_EDGE) != 0) ? -1 : 1)
                            * yDelta);
        }
    }

    void moveBy(float dx, float dy) {
        // Rect invalRect = new Rect(mDrawRect);

        mCropRectF.offset(dx, dy);

        float leftOffset = Math.max(0, fullScreenRect.left - mCropRectF.left);
        float topOffset = Math.max(0, fullScreenRect.top - mCropRectF.top);
        float rightOffset = Math
                .min(0, fullScreenRect.right - mCropRectF.right);
        float bottomOffset = Math.min(0, fullScreenRect.bottom
                - mCropRectF.bottom);

        mCropRectF.offset(leftOffset, topOffset);

        mCropRectF.offset(rightOffset, bottomOffset);

        mCropPath.offset(dx, dy);

        mCropPath.offset(leftOffset, topOffset);

        mCropPath.offset(rightOffset, bottomOffset);

        mDrawRect = computeLayout();
        // invalRect.union(mDrawRect);
        // invalRect.inset(-10, -10);
        // invalidate(invalRect);
        invalidate();
    }

    public void growBy(float dx, float dy) {
        growByRectF.set(mCropRectF);
        growByMatrix.reset();

        //modify for EJQQQ-904 by liyuchong 20200312 begin
        //final float capMinWidth = 25F;
        //final float capMinheight = 25F;
        final float capMinWidth = 51F;
        final float capMinheight = 51F;
        //modify for EJQQQ-904 by liyuchong 20200312 end

        if (dx > 0F && growByRectF.width() + 2 * dx > fullScreenRect.width()) {
            float adjustment = (fullScreenRect.width() - growByRectF.width()) / 2F;
            dx = adjustment;
        }
        if (dy > 0F && growByRectF.height() + 2 * dy > fullScreenRect.height()) {
            float adjustment = (fullScreenRect.height() - growByRectF.height()) / 2F;
            dy = adjustment;
        }

        if (growByRectF.width() + 2 * dx < capMinWidth) {
            dx = (capMinWidth - growByRectF.width()) / 2F;
        }
        if (growByRectF.height() + 2 * dy < capMinheight) {
            dy = (capMinheight - growByRectF.height()) / 2F;
        }

        growByMatrix.setScale((dx * 2 / growByRectF.width()) + 1,
                (dy * 2 / growByRectF.height()) + 1, growByRectF.centerX(),
                growByRectF.centerY());
        growByRectF.inset(-dx, -dy);

        if (growByRectF.left < fullScreenRect.left) {
            growByMatrix.postTranslate(fullScreenRect.left - growByRectF.left,
                    0F);
            growByRectF.offset(fullScreenRect.left - growByRectF.left, 0F);
        } else if (growByRectF.right > fullScreenRect.right) {
            growByMatrix.postTranslate(
                    -(growByRectF.right - fullScreenRect.right), 0F);
            growByRectF.offset(-(growByRectF.right - fullScreenRect.right), 0);
        }
        if (growByRectF.top < fullScreenRect.top) {
            growByMatrix
                    .postTranslate(0F, fullScreenRect.top - growByRectF.top);
            growByRectF.offset(0F, fullScreenRect.top - growByRectF.top);
        } else if (growByRectF.bottom > fullScreenRect.bottom) {
            growByMatrix.postTranslate(0F,
                    -(growByRectF.bottom - fullScreenRect.bottom));
            growByRectF.offset(0F,
                    -(growByRectF.bottom - fullScreenRect.bottom));
        }

        mCropRectF.set(growByRectF);
        mCropPath.transform(growByMatrix);
        mDrawRect = computeLayout();
        invalidate();
    }

}