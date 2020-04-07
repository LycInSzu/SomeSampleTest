package com.cydroid.note.app.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.Editable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import com.cydroid.note.R;
import com.cydroid.note.app.utils.InputTextNumLimitHelp;

import cyee.widget.CyeeEditText;

public class NoteTitleEditText extends CyeeEditText {
    private static final String TAG = "NoteTitleEditText";
    private boolean mTextChanged = false;
    private InputTextNumLimitHelp mInputTextNumLimitHelp;
    private Paint mLinePaint;
    private View mShareView;
    private View mTitleMoreView;

    public NoteTitleEditText(Context context) {
        super(context);
        init(context);
    }

    public NoteTitleEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public NoteTitleEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        initConstField(context);
        setSaveEnabled(false);
        initPaint();
    }

    private void initPaint() {
        mLinePaint = new Paint(getPaint());
        mLinePaint.setColor(getResources().getColor(R.color.note_edit_text_line_color));
        mLinePaint.setAntiAlias(false);
        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setStrokeWidth(0.0F);
    }

    private void initConstField(Context context) {
        mContext = context;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawLine(canvas);
        super.onDraw(canvas);
    }

    private void drawLine(Canvas canvas) {
        int w = getWidth();
        int h = getHeight();
        if (w == 0 || h == 0) {
            return;
        }
        canvas.drawLine(0, h - 1, w, h - 1, mLinePaint);
    }

    public void setTextChanged(boolean changed) {
        synchronized (NoteTitleEditText.this) {
            mTextChanged = changed;
        }
    }

    public boolean getAndResetTextChanged() {
        synchronized (NoteTitleEditText.this) {
            boolean textChanged = mTextChanged;
            mTextChanged = false;
            return textChanged;
        }
    }

    public void initWatcher(View shareView, View titleMoreView) {
        mShareView = shareView;
        mTitleMoreView = titleMoreView;
        mInputTextNumLimitHelp = new InputTextNumLimitHelp(this, 30, 15, 30);
        mInputTextNumLimitHelp.setTextChangedListener(mTextChangedListener);
    }

    private InputTextNumLimitHelp.TextChangedListener mTextChangedListener = new InputTextNumLimitHelp.
            TextChangedListener() {

        @Override
        public void onTextChange(Editable s) {
            setTextChanged(true);
            if (null != mShareView) {
                mShareView.setEnabled(!TextUtils.isEmpty(s.toString()));
            }
            if (null != mTitleMoreView) {
                mTitleMoreView.setEnabled(!TextUtils.isEmpty(s.toString()));
            }
        }
    };

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mInputTextNumLimitHelp != null) {
            mInputTextNumLimitHelp.unRegisterWatcher();
            setText(null);
        }
    }
}
