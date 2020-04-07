package com.cydroid.note.app.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.cydroid.note.app.effect.DrawableManager;

public class NoteCardBottomView extends RelativeLayout {

    private int mEffect;
    private boolean mIsBgInitialized;

    public NoteCardBottomView(Context context) {
        this(context, null);
    }

    public NoteCardBottomView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NoteCardBottomView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (!mIsBgInitialized) {
            setCardBg(mEffect);
        }
    }

    public void setCardBg(int effect) {
        mEffect = effect;
        int w = getWidth();
        int h = getHeight();
        if (w == 0 || h == 0) {
            return;
        }
        mIsBgInitialized = true;
        Drawable bgDrawable = DrawableManager.getCardEffectDrawable(getContext(), effect, w, h);
        setBackground(bgDrawable);
    }
}
