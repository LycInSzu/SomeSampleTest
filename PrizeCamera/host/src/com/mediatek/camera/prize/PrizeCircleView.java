package com.mediatek.camera.prize;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.mediatek.camera.ui.preview.PreviewTextureView;

public class PrizeCircleView extends View {
    private Paint mPaint;
    private float mCenterX, mCenterY;
    private float mCircleRadius;
    private int sWidth, sHeight;
    private boolean isFirastInit = true;
    private float mTop;
    private float mBottom;
    private int mScreenWidth;

    public PrizeCircleView(Context context) {
        super(context);
        initUI();
    }

    public PrizeCircleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initUI();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(mScreenWidth, (int)(mBottom - mTop));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isFirastInit) {
            initUI();
            isFirastInit = false;
        }
        if (mCenterX <= 0 || mCenterY <= 0 || mCircleRadius <= 0) {
            return;
        }
        mPaint = new Paint();
        mPaint.setColor(Color.WHITE);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(3);
        canvas.drawCircle(limitCenterX(mCenterX, mCircleRadius), limitCenterY(mCenterY, mCircleRadius), mCircleRadius, mPaint);
    }

    public void setCoordinate(float x, float y) {
        if(0 == x && 0 == y){
            return;
        }
        mCenterX = x;
        mCenterY = y;
    }

    public void setCircleRadius(float circleRadius) {
        mCircleRadius = circleRadius;
    }

    private void initUI() {

        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        int width = wm.getDefaultDisplay().getWidth();
        mScreenWidth = width;

        int maxWidth = mScreenWidth / 2;
        maxWidth = maxWidth - maxWidth / 6;

        setCoordinate(mScreenWidth / 2,getHeight() / 2);
        setCircleRadius(maxWidth / 2);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getPointerCount() > 1 || !isEnabled()) {
            return false;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                setCoordinate(event.getX(), event.getY());
                invalidate();
                break;
            default:
                break;
        }
        return false;
    }

    private float limitCenterX(float centerX, float radius) {
        if (centerX <= radius) {
            centerX = radius;
        } else if (centerX + radius >= getWidth()) {
            centerX = getWidth() - radius;
        }
        mCenterX = centerX;
        return centerX;
    }

    private float limitCenterY(float centerY,float radius) {
        if (centerY <=radius) {
            centerY = radius;
        } else if (centerY + radius >= getHeight()) {
            centerY = getHeight() - radius;
        }
        mCenterY = centerY;
        return centerY;
    }

    public int getsWidth() {
        return sWidth;
    }

    public int getsHeight() {
        return sHeight;
    }

    public float getCircleRadius() {
        return mCircleRadius;
    }

    public float getCenterX() {
        return mCenterX;
    }

    public float getCenterY() {
        return mCenterY;
    }

    public void updatePosition(RectF rectf){

        mTop = rectf.top;
        mBottom = rectf.bottom;
        requestLayout();

        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)getLayoutParams();
        params.topMargin = (int)mTop;
        //Log.d("ZG", "marginTop="+params.topMargin+" mBottom="+mBottom);
        setLayoutParams(params);

        postInvalidate();
    }

    // zhangguo add for bug#74475 reset settings start
    public void reset(){
        isFirastInit = true;
    }
    // zhangguo add for bug#74475 reset settings end
}
