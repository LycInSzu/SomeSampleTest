package com.cydroid.ota.ui.widget;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import com.cydroid.ota.R;

/**
 * Created by borney on 6/12/15.
 */
public class AnimatorMiddleView extends ExpendTextView {
    private ValueAnimator mInAnimator;
    private ValueAnimator mOutAnimator;

    public AnimatorMiddleView(Context context) {
        this(context, null);
    }

    public AnimatorMiddleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mInAnimator = (ValueAnimator) AnimatorInflater.loadAnimator(getContext(), R.animator.gn_su_animator_statebutton_in);
        mInAnimator.setTarget(this);
        mOutAnimator = (ValueAnimator) AnimatorInflater.loadAnimator(getContext(), R.animator.gn_su_animator_statebutton_out);
        mOutAnimator.setTarget(this);
        setAnimation(true);
    }

    public void setAnimatorText(final String animatorText) {
        mInAnimator.removeAllListeners();
        mInAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                setEnabled(true);
            }
        });
        mOutAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                setEnabled(false);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                setExpendText(animatorText);
                mOutAnimator.removeAllListeners();
                mInAnimator.start();
            }
        });
        String show = getText().toString();
        if (!TextUtils.isEmpty(show)) {
            if (!show.equals(animatorText)) {
                mOutAnimator.start();
            }
        } else {
            setExpendText(animatorText);
        }
    }
}
