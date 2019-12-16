package com.cydroid.myviews;

/**
 * Created by zhaocaili on 18-6-27.
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import com.cydroid.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class LargeImageView extends View implements GestureDetector.OnGestureListener {
    private static final String TAG = "LargeImageView";

    private BitmapRegionDecoder mDecoder;

    /**
     * 绘制的区域
     */
    private volatile Rect mRect = new Rect();

    private int mScaledTouchSlop;

    // 分别记录上次滑动的坐标
    private int mLastX = 0;
    private int mLastY = 0;
    /**
     * 图片的宽度和高度
     */
    private int mImageWidth, mImageHeight;
    private GestureDetector mGestureDetector;
    private BitmapFactory.Options options;

    private int mScaledImageWidth, mScaledImageHeight;
    private Bitmap bitmap, drawBitmap;

    public LargeImageView(Context context) {
        this(context, null);
    }

    public LargeImageView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LargeImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void setBitmap(Bitmap b) {
        if (b == null){
            if (this.bitmap != null && !this.bitmap.isRecycled()){
                this.bitmap.recycle();
            }
            if (this.drawBitmap != null && !this.drawBitmap.isRecycled()){
                this.drawBitmap.recycle();
            }
        }else {
            this.bitmap =  b;
        }
    }

    private void setInputStream(InputStream is){
        try
        {
            mDecoder = BitmapRegionDecoder.newInstance(is, false);
            BitmapFactory.Options tmpOptions = new BitmapFactory.Options();
            // Grab the bounds for the scene dimensions
            tmpOptions.inJustDecodeBounds = true;

            is.reset();

            BitmapFactory.decodeStream(is, null, tmpOptions);
            mImageWidth = tmpOptions.outWidth;
            mImageHeight = tmpOptions.outHeight;

            android.util.Log.d(TAG, "width:" + mImageWidth + ",height:" + mImageHeight);

            requestLayout();
            invalidate();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void init(Context context) {
        options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;


        mScaledTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        Log.d(TAG, "sts:" + mScaledTouchSlop);
        //初始化手势控制器
        mGestureDetector = new GestureDetector(context, this);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //把触摸事件交给手势控制器处理
        return mGestureDetector.onTouchEvent(event);
    }

    @Override
    public boolean onDown(MotionEvent e) {
        mLastX = (int) e.getRawX();
        mLastY = (int) e.getRawY();
        return true;
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
        int x = (int) e2.getRawX();
        int y = (int) e2.getRawY();
        move(x, y);

        return true;
    }

    /**
     * 移动的时候更新图片显示的区域
     *
     * @param x
     * @param y
     */
    private void move(int x, int y) {

        boolean isInvalidate = false;

        int deltaX = x - mLastX;
        int deltaY = y - mLastY;
        //如果图片宽度大于屏幕宽度
        if (mScaledImageWidth > getWidth()) {
            //移动rect区域
            mRect.offset(-deltaX, 0);
            //检查是否到达图片最右端
            if (mRect.right > mScaledImageWidth) {
                mRect.right = mScaledImageWidth;
                mRect.left = 0;
            }

            //检查左端
            if (mRect.left < 0) {
                mRect.left = 0;
                mRect.right = mScaledImageWidth;
            }
            isInvalidate = true;

        }
        //如果图片高度大于屏幕高度
        if (mScaledImageHeight > getHeight()) {
            mRect.offset(0, -deltaY);

            //是否到达最底部
            if (mRect.bottom > mScaledImageHeight) {
                mRect.bottom = mScaledImageHeight;
                mRect.top = mScaledImageHeight - getHeight();
            }

            if (mRect.top < 0) {
                mRect.top = 0;
                mRect.bottom = getHeight();
            }
            isInvalidate = true;

        }

        if (isInvalidate) {
            invalidate();
        }

        mLastX = x;
        mLastY = y;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        mLastX = (int) e.getRawX();
        mLastY = (int) e.getRawY();
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        int x = (int) e2.getRawX();
        int y = (int) e2.getRawY();
        move(x, y);
        return true;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        //显示图片
        Bitmap bm = mDecoder.decodeRegion(mRect, options);
        canvas.drawBitmap(bm, 0, 0, null);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if (drawBitmap == null) {
            drawBitmap = resizeImage(bitmap, width);
            setInputStream(bitmap2InputStream(drawBitmap));
        }
        scaledBitmapSize(width );

        //默认显示图片的中心区域
        mRect.left = 0;//imageWidth / 2 - width / 2;
        mRect.top = 0;
        mRect.right = mScaledImageWidth;//mRect.left + width;
        mRect.bottom = height;
    }

    public Bitmap resizeImage(Bitmap bitmap, int w) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float scaleWidth = ((float) w) / width;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleWidth);
        return Bitmap.createBitmap(bitmap, 0, 0, width,
                height, matrix, true);
    }

    private void scaledBitmapSize(int w){
        float scale = ((float) w) / mImageWidth;
        mScaledImageWidth = w;
        mScaledImageHeight = (int)(mImageHeight * scale);
    }

    private InputStream bitmap2InputStream(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        InputStream is = new ByteArrayInputStream(baos.toByteArray());
        return is;
    }
}
