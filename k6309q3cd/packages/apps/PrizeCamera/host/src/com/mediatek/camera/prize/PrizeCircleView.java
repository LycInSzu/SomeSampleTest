package com.mediatek.camera.prize;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.widget.FrameLayout;

import com.mediatek.camera.ui.preview.PreviewTextureView;

public class PrizeCircleView extends View {
    private Paint mPaint;
    private float mCenterX, mCenterY;
    private float mCircleRadius;
    private int sWidth, sHeight;
    private boolean isFirastInit = true;

    private TextureView mTextureView;

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
        sWidth = MeasureSpec.getSize(widthMeasureSpec);
        sHeight = MeasureSpec.getSize(heightMeasureSpec);
        Log.i("sWidth: ",sWidth+",sHeight: "+sHeight);

        if(null != mTextureView){
            setMeasuredDimension(mTextureView.getWidth(), mTextureView.getHeight());

            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)mTextureView.getLayoutParams();

            Log.d("ZG", "marginTop="+params.topMargin+" bottomMargin="+params.bottomMargin);

            setLayoutParams(params);
        }
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

        int maxWidth = getWidth() / 2;
        maxWidth = maxWidth - maxWidth / 6;

        setCoordinate(getWidth() / 2,getHeight() / 2);
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

    public void updatePosition(TextureView tv){
        mTextureView = tv;
        requestLayout();
    }
}
