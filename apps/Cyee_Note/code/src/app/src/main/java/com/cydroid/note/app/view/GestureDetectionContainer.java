package com.cydroid.note.app.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

/**
 * Created by spc on 16-3-24.
 */
public class GestureDetectionContainer extends FrameLayout {

    private SlideDetector mSlideDetector;
    private SlideScaleListener mSlideScaleListener;
    private boolean mDisableGestureDetect;

    public GestureDetectionContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        mSlideScaleListener = new SlideScaleListener();
        mSlideDetector = new SlideDetector(mSlideScaleListener);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (mDisableGestureDetect) {
            return false;
        }
        return mSlideDetector.onTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!mDisableGestureDetect) {
            mSlideDetector.onTouchEvent(event);
            return true;
        }
        return super.onTouchEvent(event);
    }

    public final SlideScaleListener getSlideScaleListener() {
        return mSlideScaleListener;
    }

    public void setDisableGestureDetect(boolean disableGestureDetect) {
        mDisableGestureDetect = disableGestureDetect;
    }

    public boolean getDisableGestureDetect() {
        return mDisableGestureDetect;
    }

}
