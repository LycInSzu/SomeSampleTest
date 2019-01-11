package com.lyc.newtestapplication.newtestapplication.LifeBalance.Beans;

import android.os.CountDownTimer;
import com.lyc.newtestapplication.newtestapplication.LifeBalance.Interfaces.TimeTrickerInterface;

public class MyCountDownTimer extends CountDownTimer {
    /**
     * @param millisInFuture    The number of millis in the future from the call
     *                          to {@link #start()} until the countdown is done and {@link #onFinish()}
     *                          is called.
     * @param countDownInterval The interval along the way to receive
     *                          {@link #onTick(long)} callbacks.
     */
    public MyCountDownTimer(long millisInFuture, long countDownInterval,TimeTrickerInterface anInterface) {
        super(millisInFuture, countDownInterval);
        this.anInterface=anInterface;
    }
    private TimeTrickerInterface anInterface;

    @Override
    public void onTick(long millisUntilFinished) {
        anInterface.onTick(millisUntilFinished);
    }

    @Override
    public void onFinish() {
        anInterface.onFinish();
    }
}
