package com.prize.camera.feature.mode.filter.uvfilter;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.widget.LinearLayout;
import android.widget.Scroller;

/**
 * Created by Abel on 2018/4/18/0018.
 */

public class UVFilterLinearLayout extends LinearLayout {

    private static final String TAG = "UVFilterLinearLayout";

    private static final float MOVE_JUDGE_DISTANCE = 5.0f;

    public static final int DIRECTION_EXPAND = 0;
    public static final int DIRECTION_SHRINK = 1;

    private Scroller mScroller;

    private float    mLastX        = 0.0f;
    private float    mLastY        = 0.0f;
    private float    mStartX       = 0.0f;
    private float    mStartY       = 0.0f;

    private boolean  mHasJudged = false;
    private boolean  mIgnore    = false;

    private int mWidthLeft  = 0;   //LinearLayout左边部分,即开始时显示的部分
    private int mWidthRight = 0;   //LinearLayout右边部分,即开始时未显示的部分

    public UVFilterLinearLayout(Context context) {
        super(context, null);
        if (mScroller == null) {
            mScroller = new Scroller(context);
            this.setOrientation(HORIZONTAL);
        }
    }

    public UVFilterLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (mScroller == null) {
            mScroller = new Scroller(context);
            this.setOrientation(HORIZONTAL);
        }
    }

    public UVFilterLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (mScroller == null) {
            mScroller = new Scroller(context);
            this.setOrientation(HORIZONTAL);
        }
    }

    private OnSwipeListener mOnSwipeListener;
    public void setOnSwipeListener(OnSwipeListener onSwipeListener) {
        mOnSwipeListener = onSwipeListener;
    }
    public interface OnSwipeListener {
        void onDirectionJudged(UVFilterLinearLayout uvll, boolean isHorizontal);
    }
}
