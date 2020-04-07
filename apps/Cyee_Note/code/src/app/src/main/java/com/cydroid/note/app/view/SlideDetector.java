package com.cydroid.note.app.view;

/**
 * Created by spc on 16-3-24.
 */

import android.view.MotionEvent;

public class SlideDetector {

    public interface OnSlideListener {

        public boolean onSlideStart(SlideDetector detector);

        public void onSlideEnd(SlideDetector detector);

        public boolean onSlide(SlideDetector detector);
    }

    private boolean mGestureInProgress;

    // when points > 3 and the first/second finger up, the gesture is invalid.
    private boolean mInvalidGesture;


    private final OnSlideListener mListener;

    private float mStartY0 = 0;
    private float mStartY1 = 0;
    private float mEndY0 = 0;
    private float mEndY1 = 0;

    public SlideDetector(OnSlideListener listener) {
        mListener = listener;
        reset();
    }

    public boolean onTouchEvent(MotionEvent event) {

        final int action = event.getActionMasked();

        if (action == MotionEvent.ACTION_DOWN) {
            reset(); // Start fresh
        }

        switch (action) {

            case MotionEvent.ACTION_DOWN:
                break;

            case MotionEvent.ACTION_POINTER_DOWN: {
                int count = event.getPointerCount();

                if (count == 2) {
                    mStartY0 = event.getY(0);
                    mStartY1 = event.getY(1);
                    if (mListener != null) {
                        mGestureInProgress = mListener.onSlideStart(this);
                    }
                    mInvalidGesture = false;
                }
                break;
            }

            case MotionEvent.ACTION_MOVE:
                if (mGestureInProgress && !mInvalidGesture) {
                    mListener.onSlide(this);
                }
                break;

            case MotionEvent.ACTION_POINTER_UP: {
                int count = event.getPointerCount();

                if (mGestureInProgress && count == 2 && !mInvalidGesture) {
                    mEndY0 = event.getY(0);
                    mEndY1 = event.getY(1);
                    boolean trigger = (mEndY0 - mStartY0 > 300) && (mEndY1 - mStartY1 > 300);
                    if (mListener != null && trigger) {
                        mListener.onSlideEnd(this);
                    }
                    mInvalidGesture = true;
                }
                break;
            }

            case MotionEvent.ACTION_UP:
                reset();
                break;

            case MotionEvent.ACTION_CANCEL:
                reset();
                break;
            default:
                break;
        }

        return mGestureInProgress;
    }

    private void reset() {
        mStartY0 = 0;
        mStartY1 = 0;
        mEndY0 = 0;
        mEndY1 = 0;
        mGestureInProgress = false;
        mInvalidGesture = false;
    }
}
