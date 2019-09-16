package com.cydroid.ota.ui.widget;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.TextView;
import com.cydroid.ota.logic.IContextState;
import com.cydroid.ota.Log;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author borney
 */
public class ExpendTextView extends TextView implements IStateView, ITheme {
    private static final String TAG = "ExpendTextView";
    private static final int DURATION = 500;
    private static final String EXPENDSTRING = "...";
    private boolean isAnimation = false;
    private boolean isBackShow = false;
    private Chameleon mChameleon;
    private AnimationThread mAnimationThread;

    public ExpendTextView(Context context,
                          AttributeSet attrs) {
        super(context, attrs);
        mAnimationThread = new AnimationThread();
        post(new Runnable() {
            @Override
            public void run() {
                mAnimationThread.start();
            }
        });
    }

    public void setAnimation(boolean isAnimation) {
        this.isAnimation = isAnimation;
    }

    public void setBackShow(boolean isBackShow) {
        this.isBackShow = isBackShow;
    }

    @Override
    public void onChameleonChanged(Chameleon chameleon) {
        mChameleon = chameleon;
    }

    public void setExpendText(final String text) {
        Log.debug(TAG, "setExpendText text = " + text);
        if (!isAnimation) {
            setText(text);
            return;
        }
        ViewGroup.LayoutParams params = getLayoutParams();
        params.width = (int) getPaint().measureText(text);
        setLayoutParams(params);
        if (!TextUtils.isEmpty(text) && text.endsWith(EXPENDSTRING)) {
            int length = EXPENDSTRING.length();
            String[] stateTexts = new String[length];
            for (int i = 0; i < length; i++) {
                stateTexts[i] = (String) text.subSequence(0, text.length() - i);
            }
            setGravity(Gravity.START);
            mAnimationThread.setStateTexts(stateTexts);
        } else {
            setGravity(Gravity.CENTER);
            mAnimationThread.setStateTexts(text);
        }
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        super.setText(text, type);
        if (isBackShow) {
            setTextColor(mChameleon.AccentColor_G1);
        } else {
            setTextColor(getTextColors().getColorForState(getDrawableState(), 0));
        }
    }

    @Override
    public void changeState(IContextState contextState) {
    }

    @Override
    public void onDestory() {
        mAnimationThread.onDestory();
    }

    class AnimationThread extends Thread {
        private AtomicInteger index = new AtomicInteger();
        private String[] stateTexts;
        private final Object object = new Object();
        private boolean isLoop = true;

        AnimationThread() {
        }

        private void setAtomicInteger(AtomicInteger integer, int value) {
            int old = integer.get();
            for (;;) {
                if (integer.compareAndSet(old, value)) {
                    return;
                }
            }
        }

        void setStateTexts(String... stateTexts) {
            this.stateTexts = stateTexts;
            setAtomicInteger(index , stateTexts != null ? stateTexts.length - 1 : 0);
            synchronized (object) {
                object.notifyAll();
            }
        }

        void onDestory() {
            this.isLoop = false;
            synchronized (object) {
                object.notifyAll();
            }
        }

        @Override
        public void run() {
            while (isLoop) {
                try {
                    if (stateTexts == null || stateTexts.length == 0 || stateTexts.length == 1) {
                        if (stateTexts != null && stateTexts.length == 1) {
                            post(new Runnable() {
                                @Override
                                public void run() {
                                    setText(stateTexts[0]);
                                }
                            });
                        }
                        synchronized (object) {
                            object.wait(Integer.MAX_VALUE);
                        }
                    }
                    int value = index.get();
                    final String text = stateTexts[value];
                    post(new Runnable() {
                        @Override
                        public void run() {
                            setText(text);
                        }
                    });
                    setAtomicInteger(index, --value);
                    if (index.get() < 0) {
                        setAtomicInteger(index , stateTexts.length - 1);
                    }
                    synchronized (object) {
                        object.wait(DURATION);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (IndexOutOfBoundsException e) {
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
