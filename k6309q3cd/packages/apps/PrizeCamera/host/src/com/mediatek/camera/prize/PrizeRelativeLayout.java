package com.mediatek.camera.prize;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

import com.mediatek.camera.common.debug.LogHelper;

public class PrizeRelativeLayout extends RelativeLayout {
    public PrizeRelativeLayout(Context context) {
        super(context);
    }

    public PrizeRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        super.dispatchTouchEvent(ev);
        return /*super.dispatchTouchEvent(ev)*/true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        return /*super.onTouchEvent(event)*/true;
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return super.onInterceptTouchEvent(ev);
    }
}
