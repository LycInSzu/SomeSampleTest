package com.cydroid.ota.ui.widget;

import android.view.animation.Interpolator;

/**
 * Created by borney on 6/11/15.
 */
public class Dynamics {
    private long mStartTime = 0;
    private long mDuration;
    private Interpolator mInterpolator;

    protected Dynamics() {

    }

    protected Dynamics(long duration) {
        this.mDuration = duration;
    }

    protected void setInterpolator(Interpolator interpolator) {
        this.mInterpolator = interpolator;
    }

    protected void setDuration(long duration) {
        this.mDuration = duration;
    }

    protected void setStartOffset(long startTime) {
        this.mStartTime = startTime;
    }

    protected long getStartOffset() {
        return this.mStartTime;
    }

    protected float update(long now) {
        float normalizedTime;
        if (mDuration != 0) {
            normalizedTime = ((float) (now - mStartTime)) / (float) mDuration;
        } else {
            normalizedTime = now < mStartTime ? 0.0f : 1.0f;
        }
        if (normalizedTime >= 0.0f && normalizedTime <= 1.0f) {
            float result = mInterpolator.getInterpolation(normalizedTime);
            return result;
        }
        return -1;
    }
}
