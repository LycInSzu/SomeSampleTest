/*
 * Copyright (C) 2013-2017, Shenzhen Huiding Technology Co., Ltd.
 * All Rights Reserved.
 */

package com.goodix.fingerprint.shenzhen.sensor;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.view.View;

public class FindSensorView extends View {
    private String TAG = "FindSensorView";

    private Paint mPaint;
    private Paint mSrcInPaint;
    private int mStrokeWidth = 4;
    private int mLineInterval = 20;
    private int mPadding = 0;
    private int mStartX = 0;
    private int mStartY = 0;
    private int mStopX = 0;
    private int mStopY = 0;

    public FindSensorView(Context context) {
        this(context, null);
    }

    public FindSensorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.BLACK);
        mPaint.setStrokeWidth(mStrokeWidth);
        mPaint.setAntiAlias(true);

        mSrcInPaint = new Paint(mPaint);
        mSrcInPaint.setAlpha(0);
        mSrcInPaint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
    }

    public void setColor(int color) {
        mPaint.setColor(color);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawOval(0, 0, getWidth(), getHeight(), mPaint);
        canvas.drawOval(mStrokeWidth, mStrokeWidth, getWidth() - mStrokeWidth,
                getHeight() - mStrokeWidth, mSrcInPaint);

        canvas.drawOval(getWidth() / 8, getHeight() / 8, getWidth() * 7 / 8, getHeight() * 7 / 8, mPaint);
        canvas.drawOval(getWidth() / 8 + mStrokeWidth, getHeight() / 8 + mStrokeWidth,
                getWidth() * 7 / 8 - mStrokeWidth, getHeight() * 7 / 8 - mStrokeWidth, mSrcInPaint);

        canvas.drawOval(getWidth() / 4, getHeight() / 4, getWidth() * 3 / 4, getHeight() * 3 / 4, mPaint);
        canvas.drawOval(getWidth() / 4 + mStrokeWidth, getHeight() / 4 + mStrokeWidth,
                getWidth() * 3 / 4 - mStrokeWidth, getHeight() * 3 / 4 - mStrokeWidth, mSrcInPaint);

        canvas.drawOval(getWidth() * 3 / 8, getHeight() * 3 / 8, getWidth() * 5 / 8, getHeight() * 5 / 8, mPaint);
        canvas.drawOval(getWidth() * 3 / 8 + mStrokeWidth, getHeight() * 3 / 8 + mStrokeWidth,
                getWidth() * 5 / 8 - mStrokeWidth, getHeight() * 5 / 8 - mStrokeWidth, mSrcInPaint);

        mStartX = mPadding + mStrokeWidth / 2;
        mStartY = mPadding + mStrokeWidth / 2;
        mStopX = getWidth() - mPadding - mStrokeWidth / 2;
        mStopY = getHeight() - mPadding - mStrokeWidth / 2;

/*
        int stopY = getHeight() / 2 - (mStrokeWidth + mLineInterval);
        for (int y = mStartY; y < stopY; y += (mStrokeWidth + mLineInterval)) {
            canvas.drawLine(mStartX, y, mStopX, y, mPaint);
        }
        stopY = getHeight() / 2 + (mStrokeWidth + mLineInterval);
        for (int y = mStopY; y > stopY; y -= (mStrokeWidth + mLineInterval)) {
            canvas.drawLine(mStartX, y, mStopX, y, mPaint);
        }
*/

        canvas.drawLine(mStartX, mStartY, mStopX, mStartY, mPaint);
        canvas.drawLine(mStartX, mStopY, mStopX, mStopY, mPaint);
        canvas.drawLine(mStartX, mStartY, mStartX, mStopY, mPaint);
        canvas.drawLine(mStopX, mStartY, mStopX, mStopY, mPaint);

        canvas.drawLine(mStartX, getHeight() / 2, mStopX, getHeight() / 2, mPaint);
        canvas.drawLine(getWidth() / 2, mStartY, getWidth() / 2, mStopY, mPaint);

        canvas.drawLine(getWidth() / 8 + mStrokeWidth / 2, mStartY, getWidth() / 8 + mStrokeWidth / 2, mStopY, mPaint);
        canvas.drawLine(getWidth() * 7 / 8 - mStrokeWidth / 2, mStartY, getWidth() * 7 / 8 - mStrokeWidth / 2, mStopY, mPaint);

        canvas.drawLine(getWidth() / 4 + mStrokeWidth / 2, mStartY, getWidth() / 4 + mStrokeWidth / 2, mStopY, mPaint);
        canvas.drawLine(getWidth() * 3 / 4 - mStrokeWidth / 2, mStartY, getWidth() * 3 / 4 - mStrokeWidth / 2, mStopY, mPaint);

        canvas.drawLine(getWidth() * 3 / 8 + mStrokeWidth / 2, mStartY, getWidth() * 3 / 8 + mStrokeWidth / 2, mStopY, mPaint);
        canvas.drawLine(getWidth() * 5 / 8 - mStrokeWidth / 2, mStartY, getWidth() * 5 / 8 - mStrokeWidth / 2, mStopY, mPaint);

        canvas.drawCircle(getWidth() / 2, getWidth() / 2, 10, mPaint);

        canvas.drawLine(getWidth() / 2, mStartY, getWidth() * 3 / 8, mStartY + 40, mPaint);
        canvas.drawLine(getWidth() / 2, mStartY, getWidth() * 5 / 8, mStartY + 40, mPaint);

        canvas.drawLine(getWidth() / 2, getHeight() / 4, getWidth() * 3 / 8, getHeight() / 4 + 40, mPaint);
        canvas.drawLine(getWidth() / 2, getHeight() / 4, getWidth() * 5 / 8, getHeight() / 4 + 40, mPaint);
    }

}
