package com.cydroid.note.app.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import com.cydroid.note.common.Log;
import android.util.TypedValue;

import com.cydroid.note.R;

import cyee.changecolors.ChameleonColorManager;
import cyee.widget.CyeeEditText;

import static android.graphics.Paint.ANTI_ALIAS_FLAG;

public class PasswordInputView extends CyeeEditText {
    private static final String TAG = "PasswordInputView";

    private static final int DEFAULT_BACKGROUND_COLOR = 0xfffdfdfd;
    private static final int DEFAULT_PAINT_COLOR = 0xaa000000;
    private static final int PASSWORD_LENGTH = 4;
    private static final int DEFAULT_PASSWORD_RADIUS = 9;

    private int mPasswordColor = DEFAULT_PAINT_COLOR;
    private float mPasswordRadius = DEFAULT_PASSWORD_RADIUS;

    private Paint mPasswordPaint = new Paint(ANTI_ALIAS_FLAG);
    private Paint mBorderPaint = new Paint(ANTI_ALIAS_FLAG);
    private int mTextLength;

    public PasswordInputView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        DisplayMetrics dm = getResources().getDisplayMetrics();
        mPasswordRadius = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, mPasswordRadius, dm);

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.PasswordInputView, 0, 0);
        mPasswordColor = a.getColor(R.styleable.PasswordInputView_passwordColor, DEFAULT_PAINT_COLOR);
        mPasswordRadius = a.getDimension(R.styleable.PasswordInputView_passwordRadius, DEFAULT_PASSWORD_RADIUS);
        a.recycle();

        ChameleonColorManager mChameleonColorManager = ChameleonColorManager.getInstance();
        int backgroudColor = mChameleonColorManager == null ? DEFAULT_BACKGROUND_COLOR
                : mChameleonColorManager.getBackgroudColor_B1();
        setBackgroundColor(backgroudColor);

        mPasswordColor = (mChameleonColorManager != null && mChameleonColorManager.isNeedChangeColor()) ?
                mChameleonColorManager.getContentColorPrimaryOnBackgroud_C1() : DEFAULT_PAINT_COLOR;

        mBorderPaint.setStrokeWidth(3);
        mBorderPaint.setStyle(Style.STROKE);
        mBorderPaint.setColor(mPasswordColor);
        mPasswordPaint.setStrokeWidth(10);
        mPasswordPaint.setStyle(Paint.Style.FILL);
        mPasswordPaint.setColor(mPasswordColor);
        setFocusable(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int width = getWidth();
        int height = getHeight();
//        Log.i(TAG, "onDraw---- width  = " + width + "; height = " + height);

        float cx, cy = height / (float) 2;
        float half = width / PASSWORD_LENGTH / (float) 2; //NOSONAR
        for (int i = 0; i < mTextLength; i++) {
            cx = width * i / PASSWORD_LENGTH + half;
            Log.i(TAG, "solid circle： No. =" + i + ";  cx  :" + cx + " ; cy : " + cy);
            canvas.drawCircle(cx, cy, mPasswordRadius, mPasswordPaint);
        }

        for (int i = 0; i < PASSWORD_LENGTH - mTextLength; i++) {
            cx = width * (i + mTextLength) / PASSWORD_LENGTH + half;
            Log.i(TAG, "hollow  circle： No. = " + (i + mTextLength) + ";  cx  :" + cx + " ; cy : " + cy);
            canvas.drawCircle(cx, cy, mPasswordRadius, mBorderPaint);
        }
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);
        this.mTextLength = text.toString().length();
        Log.i(TAG, "onTextChanged---- textLength  = " + mTextLength);
        invalidate();

        if (mTextLength == PASSWORD_LENGTH) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mTextChangeLisenter != null) {
                        mTextChangeLisenter.onTextChanged();
                    }
                }
            }, 60);
        }
    }

    private Handler mHandler = new Handler();
    private onTextChangedLisenter mTextChangeLisenter;

    public interface onTextChangedLisenter {
        void onTextChanged();
    }

    public void setOnTextChangedLisenter(onTextChangedLisenter lisenter) {
        mTextChangeLisenter = lisenter;
    }
}
