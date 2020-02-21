package com.wtk.screenshot.util.imageZoom;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;

import com.android.systemui.R;

public class SquareLightView {
    /* Common */
    // Default
    private static final String TAG = CropImageView.TAG;
    public static final int GROW_NONE = (1 << 0);
    public static final int GROW_LEFT_EDGE = (1 << 1);
    public static final int GROW_RIGHT_EDGE = (1 << 2);
    public static final int GROW_TOP_EDGE = (1 << 3);
    public static final int GROW_BOTTOM_EDGE = (1 << 4);
    public static final int MOVE = (1 << 5);

    // Util
    private final Paint mFocusPaint = new Paint();
    private final Paint mOutlinePaint = new Paint();
    private Path path = new Path();
    private Rect viewDrawingRect = new Rect();
    private RectF rectF = new RectF();

    // Flag
    boolean mHidden = false;
    private ModifyMode mMode = ModifyMode.None;
    private float mLastX, mLastY;
    private int mMotionEdge;

    public static enum ModifyMode {
        None, Move, Grow
    }

    /* View */
    private CropImageView mCropImageView;
    private Drawable mResizeDrawableWE;
    private Drawable mResizeDrawableNS;
    private Drawable mResizeDrawableES;
    private Drawable mResizeDrawableWS;

    public SquareLightView(CropImageView ctx) {
        mCropImageView = ctx;

        init();

        mFocusPaint.setARGB(125, 50, 50, 50);
        mOutlinePaint.setStrokeWidth(5F);
        mOutlinePaint.setStyle(Paint.Style.STROKE);
        mOutlinePaint.setAntiAlias(true);
        mOutlinePaint.setColor(0xFFFF8A00);
    }

    protected void sizeChanged(int w, int h, int oldw, int oldh) {

    }

    protected void layout(boolean changed, int left, int top, int right,
                          int bottom) {
    }

    protected boolean touchEvent(MotionEvent event) {
        if (mHidden || mCropImageView.mDrawRect == null
                || mCropImageView.mDrawRect.width() < 5) {
            return true;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                int edge = getHit(event.getX(), event.getY());
                mMotionEdge = edge;
                if (edge != GROW_NONE) {
                    mLastX = event.getX();
                    mLastY = event.getY();
                    setMode((edge == MOVE) ? ModifyMode.Move : ModifyMode.Grow);
                }
                break;
            case MotionEvent.ACTION_UP:

                if (mMotionEdge == GROW_NONE) {
                    mCropImageView.reset();
                } else {
                    setMode(ModifyMode.None);

                    mCropImageView.centerBasedOnHighlightView();
                    mCropImageView.center(true, true);

                }
                break;
            case MotionEvent.ACTION_MOVE:
                mCropImageView.handleMotion(mMotionEdge, event.getX() - mLastX,
                        event.getY() - mLastY);
                mLastX = event.getX();
                mLastY = event.getY();

                if (mCropImageView.getScale() == 1F) {
                    mCropImageView.center(true, true);
                }
                break;
        }

        return true;
    }

    protected void draw(Canvas canvas) {
        if (mHidden || mCropImageView.mDrawRect == null
                || mCropImageView.mDrawRect.width() < 5) {
            return;
        }
        canvas.save();
        path.reset();
        viewDrawingRect.setEmpty();
        mCropImageView.getDrawingRect(viewDrawingRect);
        rectF.set(mCropImageView.mDrawRect);
        // rectF.inset(CropImageView.CROP_BOUND_PADDING,
        // CropImageView.CROP_BOUND_PADDING);
        path.addRect(rectF, Path.Direction.CW);
        canvas.clipPath(path, Region.Op.DIFFERENCE);
        canvas.drawRect(viewDrawingRect, mFocusPaint);
        canvas.drawPath(path, mOutlinePaint);
        canvas.restore();

        if (mMode == ModifyMode.Grow) {
            int left = mCropImageView.mDrawRect.left;
            int right = mCropImageView.mDrawRect.right;
            int top = mCropImageView.mDrawRect.top;
            int bottom = mCropImageView.mDrawRect.bottom;

            int weWidth = mResizeDrawableWE.getIntrinsicWidth() / 2;
            int weHeight = mResizeDrawableWE.getIntrinsicHeight() / 2;
            int nsHeight = mResizeDrawableNS.getIntrinsicHeight() / 2;
            int nsWidth = mResizeDrawableNS.getIntrinsicWidth() / 2;
            int esWidth = mResizeDrawableES.getIntrinsicWidth() / 2;
            int esHeight = mResizeDrawableES.getIntrinsicHeight() / 2;
            int wsHeight = mResizeDrawableWS.getIntrinsicHeight() / 2;
            int wsWidth = mResizeDrawableWS.getIntrinsicWidth() / 2;

            int xMiddle = mCropImageView.mDrawRect.left
                    + ((mCropImageView.mDrawRect.right - mCropImageView.mDrawRect.left) / 2);
            int yMiddle = mCropImageView.mDrawRect.top
                    + ((mCropImageView.mDrawRect.bottom - mCropImageView.mDrawRect.top) / 2);

            mResizeDrawableWE.setBounds(left - weWidth, yMiddle - weHeight,
                    left + weWidth, yMiddle + weHeight);
            mResizeDrawableWE.draw(canvas);

            mResizeDrawableWE.setBounds(right - weWidth, yMiddle - weHeight,
                    right + weWidth, yMiddle + weHeight);
            mResizeDrawableWE.draw(canvas);

            mResizeDrawableNS.setBounds(xMiddle - nsWidth, top - nsHeight,
                    xMiddle + nsWidth, top + nsHeight);
            mResizeDrawableNS.draw(canvas);

            mResizeDrawableNS.setBounds(xMiddle - nsWidth, bottom - nsHeight,
                    xMiddle + nsWidth, bottom + nsHeight);
            mResizeDrawableNS.draw(canvas);

            mResizeDrawableES.setBounds(left - esWidth, bottom - esHeight, left
                    + esWidth, bottom + esHeight);
            mResizeDrawableES.draw(canvas);

            mResizeDrawableWS.setBounds(left - wsWidth, top - wsHeight, left
                    + wsWidth, top + wsHeight);
            mResizeDrawableWS.draw(canvas);

            mResizeDrawableES.setBounds(right - esWidth, top - esHeight, right
                    + esWidth, top + esHeight);
            mResizeDrawableES.draw(canvas);

            mResizeDrawableWS.setBounds(right - wsWidth, bottom - wsHeight,
                    right + wsWidth, bottom + wsHeight);
            mResizeDrawableWS.draw(canvas);

        }
    }

    public void reset() {
        mMode = ModifyMode.None;
    }

    private void init() {
        Resources resources = mCropImageView.getResources();
        mResizeDrawableWE = resources.getDrawable(R.drawable.camera_crop_we);
        mResizeDrawableNS = resources.getDrawable(R.drawable.camera_crop_ns);
        mResizeDrawableES = resources.getDrawable(R.drawable.camera_crop_es);
        mResizeDrawableWS = resources.getDrawable(R.drawable.camera_crop_ws);
    }

    public void setHidden(boolean hidden) {
        mHidden = hidden;
        mCropImageView.invalidate();
    }

    public void setMode(ModifyMode mode) {
        if (mode != mMode) {
            mMode = mode;
        }
    }

    public ModifyMode getMode() {
        return mMode;
    }

    public int getHit(float x, float y) {
        Rect r = mCropImageView.computeLayout();
        final float hysteresis = 50F;
        int retval = GROW_NONE;

        boolean verticalCheck = (y >= r.top - hysteresis)
                && (y < r.bottom + hysteresis);
        boolean horizCheck = (x >= r.left - hysteresis)
                && (x < r.right + hysteresis);

        if ((Math.abs(r.left - x) < hysteresis) && verticalCheck) {
            retval |= GROW_LEFT_EDGE;
        }
        if ((Math.abs(r.right - x) < hysteresis) && verticalCheck) {
            retval |= GROW_RIGHT_EDGE;
        }
        if ((Math.abs(r.top - y) < hysteresis) && horizCheck) {
            retval |= GROW_TOP_EDGE;
        }
        if ((Math.abs(r.bottom - y) < hysteresis) && horizCheck) {
            retval |= GROW_BOTTOM_EDGE;
        }

        if (retval == GROW_NONE && r.contains((int) x, (int) y)) {
            retval = MOVE;
        }
        return retval;
    }
}
