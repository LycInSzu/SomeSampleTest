package com.cydroid.note.app.view;

import android.content.Context;
import android.widget.OverScroller;

public class ScrollerHelper {
    private OverScroller mScroller;

    public ScrollerHelper(Context context) {
        mScroller = new OverScroller(context);
    }

    public boolean advanceAnimation() {
        return mScroller.computeScrollOffset();
    }

    public boolean isFinished() {
        return mScroller.isFinished();
    }

    public void forceFinished() {
        mScroller.forceFinished(true);
    }

    public int getPosition() {
        return mScroller.getCurrX();
    }

    public void fling(int currX, int velocity, int min, int max, int overflingDistance) {
        mScroller.fling(
                currX, 0,      // startX, startY
                velocity, 0,   // velocityX, velocityY
                min, max,      // minX, maxX
                0, 0,          // minY, maxY
                overflingDistance, 0);
    }

    public void springBack(int curPosition, int min, int max) {
        mScroller.springBack(curPosition, 0, min, max, 0, 0);
    }
}