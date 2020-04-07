package com.cydroid.note.app.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.cydroid.note.photoview.PreviewActivity;

public class SharePreView extends View {

    private final GestureDetector mGestureDetector;
    private int mY;

    private ScrollerHelper mScroller;
    private int mScrollLimit;
    private float mScale;

    private boolean mIsScrolling = false;
    private boolean mIsFlinged = false;

    private Rect mSrc = new Rect();
    private RectF mOurSrcRectF = new RectF();
    private RectF mSrcRectF = new RectF();
    private Rect mDst = new Rect();
    private Bitmap mBitmap;
    private boolean mIsInit;
    private int mBitmapW;
    private int mBitmapH;
    private Paint mPaint;

    public SharePreView(Context context) {
        this(context, null);
    }

    public SharePreView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    public void setBitmap(Bitmap bitmap) {
        mBitmap = bitmap;
        if (getWidth() != 0 && getHeight() != 0) {
            init();
            invalidate();
        }
    }

    private void init() {
        mPaint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG);
        mIsInit = true;
        calculate();
    }

    private void calculate() {

        int vw = getWidth();
        int vh = getHeight();

        int dw = mBitmap.getWidth();
        int dh = mBitmap.getHeight();

        mBitmapW = dw;
        mBitmapH = dh;

        float scale = (float) vw / dw;
        mScale = 1 / scale;
        int contentLength = Math.round(scale * dh);
        int limit = contentLength - vh;
        mScrollLimit = limit <= 0 ? 0 : limit;

        if (contentLength > vh) {
            mDst.set(0, 0, vw, vh);
        } else {
            int top = (vh - contentLength) / 2;
            mDst.set(0, top, vw, top + contentLength);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!checkEnvIsOk()) {
            return true;
        }
        mGestureDetector.onTouchEvent(event);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mScroller.forceFinished();
                break;
            case MotionEvent.ACTION_CANCEL:
                if (mIsScrolling && !mIsFlinged) {
                    mScroller.springBack(mY, 0, getScrollLimit());
                }
                mIsScrolling = false;
                mIsFlinged = false;
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                if (mIsScrolling && !mIsFlinged) {
                    mScroller.springBack(mY, 0, getScrollLimit());
                }
                mIsScrolling = false;
                mIsFlinged = false;
                invalidate();
                break;
            default:
                break;
        }

        return true;
    }

    public SharePreView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mScroller = new ScrollerHelper(context);
        mGestureDetector = new GestureDetector(context, new GestureDetector.OnGestureListener() {

            @Override
            public boolean onDown(MotionEvent e) {
                return false;
            }

            @Override
            public void onShowPress(MotionEvent e) {

            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return false;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                float distance = distanceY;
                mIsScrolling = true;
                int min = 0;
                int max = getScrollLimit();
                overScrollBy(Math.round(distance), min, max, 0);
                return true;
            }


            @Override
            public void onLongPress(MotionEvent e) {

            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                int scrollLimit = getScrollLimit();
                if (scrollLimit == 0) return false;
                float velocity = velocityY;
                mIsFlinged = true;
                mScroller.fling(mY, (int) -velocity, 0, scrollLimit, getHeight() / 2);
                invalidate();
                return true;
            }
        });
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!checkEnvIsOk()) {
            return;
        }
        canvas.clipRect(0, 0, getWidth(), getHeight());
        computeY();
        mSrcRectF.set(0, mY, getWidth(), mY + getHeight());
        RectF outRectF = mOurSrcRectF;
        mapRect(outRectF, mSrcRectF, 0, 0, 0, 0, mScale, mScale);
        int left = 0;
        int right = mBitmapW;
        int top = (int) outRectF.top;
        int bottom = (int) outRectF.bottom;
        if (bottom > mBitmapH) {
            bottom = mBitmapH;
        }
        mSrc.set(left, top, right, bottom);
        canvas.drawBitmap(mBitmap, mSrc, mDst, mPaint);
    }

    private boolean checkEnvIsOk() {
        if (!mIsInit || mBitmap == null || mBitmap.isRecycled()) {
            return false;
        }
        return true;
    }

    private void overScrollBy(int deltaY, int min, int max, int overScrollY) {

        int newScrollY = mY + deltaY;
        final int top = min - overScrollY;
        final int bottom = max + overScrollY;
        boolean clampedY = false;
        if (newScrollY > bottom) {
            newScrollY = bottom;
            clampedY = true;
        } else if (newScrollY < top) {
            newScrollY = top;
            clampedY = true;
        }
        onOverScrolled(newScrollY, clampedY);
    }

    private void onOverScrolled(int newScrollY, boolean clampedY) {
        mY = newScrollY;
        if (!mScroller.isFinished()) {
            if (clampedY) {
                mScroller.springBack(mY, 0, getScrollLimit());
            }
        }
        invalidate();
    }

    private void computeY() {
        boolean more = mScroller.advanceAnimation();
        if (more) {
            int oldY = mY;
            int y = mScroller.getPosition();
            overScrollBy(y - oldY, 0, getScrollLimit(), 0);
        }
    }

    private int getScrollLimit() {
        return mScrollLimit;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (mIsInit || mBitmap == null) {
            return;
        }
        init();
        invalidate();
    }

    // We want to draw the "source" on the "target".
    // This method is to find the "output" rectangle which is
    // the corresponding area of the "src".
    //                                   (x,y)  target
    // (x0,y0)  source                     +---------------+
    //    +----------+                     |               |
    //    | src      |                     | output        |
    //    | +--+     |    linear map       | +----+        |
    //    | +--+     |    ---------->      | |    |        |
    //    |          | by (scaleX, scaleY) | +----+        |
    //    +----------+                     |               |
    //      Texture                        +---------------+
    //                                          Canvas
    public static void mapRect(RectF output,
                               RectF src, float x0, float y0, float x, float y, float scaleX,
                               float scaleY) {
        output.set(x + (src.left - x0) * scaleX,
                y + (src.top - y0) * scaleY,
                x + (src.right - x0) * scaleX,
                y + (src.bottom - y0) * scaleY);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mBitmap = null;
        PreviewActivity.recycleSharePreBitmap();
    }
}
