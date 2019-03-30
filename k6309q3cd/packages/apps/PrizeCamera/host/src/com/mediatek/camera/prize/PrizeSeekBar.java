package com.mediatek.camera.prize;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import java.math.BigDecimal;
import com.mediatek.camera.R;
public class PrizeSeekBar extends View {
    private static final String TAG = PrizeSeekBar.class.getSimpleName();
    private RectF mTickBarRecf;

    private Paint mTickBarPaint;

    private float mTickBarHeight;

    private int mTickBarColor;

    private Paint mCircleButtonPaint;

    private int mCircleButtonColor;

    private int mCircleButtonTextColor;

    private float mCircleButtonTextSize;

    private float mCircleButtonRadius;

    private RectF mCircleRecf;

    private Paint mCircleButtonTextPaint;

    /**
     * Differentiate different views
     */
    private int mTagid;

    private float mProgressHeight;

    /**
     * @return the id
     */
    public int getTagId() {
        return mTagid;
    }

    /**
     * @param id the id to set
     */
    public void setTagId(int id) {
        this.mTagid = id;
    }


    private Paint mProgressPaint;

    private int mProgressColor;

    /**
     * Progress bar recf, rectangular area
     */
    private RectF mProgressRecf;

    private int mSelectProgress;

    private int mMaxProgress = DEFAULT_MAX_VALUE;

    private static final int DEFAULT_MAX_VALUE = 10;


    /**
     * The total progress width of the view, except for paddingtop and bottom
     */
    private int mViewWidth;
    /**
     * The total progress height of the view, except for paddingtop and bottom
     */
    private int mViewHeight;
    private float mCirclePotionX;
    private boolean mIsShowButtonText;
    private boolean mIsShowButton;
    /**
     * Whether to display fillet
     */
    private boolean mIsRound;
    private int mStartProgress;
    /**
     * @return the progressIncrement
     */
    /**
     * Currently set gear
     */
    private int mCurrentLevel = 4;

    public int getProgressIncrement() {
        return progressIncrement;
    }

    /**
     * @param progressIncrement the progressIncrement to set
     */
    public void setProgressIncrement(int progressIncrement) {
        this.progressIncrement = progressIncrement;
    }

    /**
     * @return the progressText
     */
    public String getProgressText() {
        return progressText;
    }

    /**
     * @param progressText the progressText to set
     */
    public void setProgressText(String progressText) {
        this.progressText = progressText;
    }

    private int progressIncrement = 0;
    private String progressText = "";

    public interface OnProgressChangeListener {
        void onChange(int selectProgress, int id);
    }

    private OnProgressChangeListener mOnProgressChangeListener;

    public void setOnProgressChangeListener(OnProgressChangeListener onProgressChangeListener) {
        mOnProgressChangeListener = onProgressChangeListener;
    }

    public PrizeSeekBar(Context context) {
        this(context, null);
    }

    public PrizeSeekBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PrizeSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }


    /**
     * Initialize the properties of the view in the xml configuration
     *
     * @param context
     * @param attrs
     */
    private void init(Context context, AttributeSet attrs) {

        TypedArray attr = context.obtainStyledAttributes(attrs, R.styleable.PrizeSeekBar);
        mTickBarHeight = attr.getDimensionPixelOffset(R.styleable
                .PrizeSeekBar_tickBarHeight, getDpValue(8));
        mTickBarColor = attr.getColor(R.styleable.PrizeSeekBar_tickBarColor, getResources()
                .getColor(R.color.colorAccent));
        mCircleButtonColor = attr.getColor(R.styleable.PrizeSeekBar_circleButtonColor,
                getResources().getColor(R.color.colorPrimary));
        mCircleButtonTextColor = attr.getColor(R.styleable.PrizeSeekBar_circleButtonTextColor,
                getResources().getColor(R.color.colorPrimary));
        mCircleButtonTextSize = attr.getDimension(R.styleable
                .PrizeSeekBar_circleButtonTextSize, getDpValue(16));
        mCircleButtonRadius = attr.getDimensionPixelOffset(R.styleable
                .PrizeSeekBar_circleButtonRadius, getDpValue(16));
        mProgressHeight = attr.getDimensionPixelOffset(R.styleable
                .PrizeSeekBar_progressHeight, getDpValue(20));
        mProgressColor = attr.getColor(R.styleable.PrizeSeekBar_progressColor,
                getResources().getColor(R.color.colorPrimary));
        mSelectProgress = attr.getInt(R.styleable.PrizeSeekBar_selectProgress, 0);
        mStartProgress = attr.getInt(R.styleable.PrizeSeekBar_startProgress, 0);
        mMaxProgress = attr.getInt(R.styleable.PrizeSeekBar_maxProgress, 10);
        mIsShowButtonText = attr.getBoolean(R.styleable.PrizeSeekBar_isShowButtonText, false);
        mIsShowButton = attr.getBoolean(R.styleable.PrizeSeekBar_isShowButton, true);
        mIsRound = attr.getBoolean(R.styleable.PrizeSeekBar_isRound, true);
        initView();
        attr.recycle();

    }

    private void initView() {
        mProgressPaint = new Paint();
        mProgressPaint.setColor(mProgressColor);
        mProgressPaint.setStyle(Paint.Style.FILL);
        mProgressPaint.setAntiAlias(true);

        mCircleButtonPaint = new Paint();
        mCircleButtonPaint.setColor(mCircleButtonColor);
        mCircleButtonPaint.setStyle(Paint.Style.FILL);
        mCircleButtonPaint.setAntiAlias(true);

        mCircleButtonTextPaint = new Paint();
        mCircleButtonTextPaint.setTextAlign(Paint.Align.CENTER);
        mCircleButtonTextPaint.setColor(mCircleButtonTextColor);
        mCircleButtonTextPaint.setStyle(Paint.Style.FILL);
        mCircleButtonTextPaint.setTextSize(mCircleButtonTextSize);
        mCircleButtonTextPaint.setAntiAlias(true);

        mTickBarPaint = new Paint();
        mTickBarPaint.setColor(mTickBarColor);
        mTickBarPaint.setStyle(Paint.Style.FILL);
        mTickBarPaint.setAntiAlias(true);

        mTickBarRecf = new RectF();
        mProgressRecf = new RectF();
        mCircleRecf = new RectF();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) {
            return false;
        }
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                judgePosition(x);
                return true;
            case MotionEvent.ACTION_DOWN:
                judgePosition(x);
                return true;
            case MotionEvent.ACTION_UP:
                if (mOnProgressChangeListener != null) {
                    mOnProgressChangeListener.onChange(mSelectProgress, mTagid);
                }
                return true;
            default:
                break;
        }

        return super.onTouchEvent(event);
    }

    private void judgePosition(float x) {
        float end = getPaddingLeft() + mViewWidth;
        float start = getPaddingLeft();
        int progress = mSelectProgress;
        if (x >= start) {
            double result = (x - start) / mViewWidth * (float) mMaxProgress;
            BigDecimal bigDecimal = new BigDecimal(result).setScale(0, BigDecimal.ROUND_HALF_UP);
            progress = bigDecimal.intValue();
            if (progress > mMaxProgress) {
                progress = mMaxProgress;
            }
        } else if (x < start) {
            progress = 0;
        }
        int level = valueToGearPosition(progress);
        if (/*progress != mSelectProgress*/mCurrentLevel != level) {
            mCurrentLevel = level;
            setSelectProgress((mCurrentLevel - 1) * (this.getMaxProgress() / 6), false);
        }

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();
        initValues(width, height);
        if (mIsRound) {
            canvas.drawRoundRect(mTickBarRecf, mProgressHeight / 2, mProgressHeight / 2,
                    mTickBarPaint);
            canvas.drawRoundRect(mProgressRecf, mProgressHeight / 2, mProgressHeight / 2,
                    mProgressPaint);
        } else {
            canvas.drawRect(mTickBarRecf, mTickBarPaint);
            canvas.drawRect(mProgressRecf, mProgressPaint);
        }
        if (mIsShowButton) {
            canvas.drawCircle(mCirclePotionX, mViewHeight / 2, mCircleButtonRadius,
                    mCircleButtonPaint);
        }
        if (mIsShowButtonText) {
            Paint.FontMetricsInt fontMetrics = mCircleButtonTextPaint.getFontMetricsInt();
            int baseline = (int) ((mCircleRecf.bottom + mCircleRecf.top - fontMetrics.bottom -
                    fontMetrics
                            .top) / 2);
            //Set horizontally centered after mCircleRecf.centerX
            canvas.drawText(String.valueOf((mCurrentLevel + progressIncrement) + progressText), mCircleRecf.centerX
                            (), baseline,
                    mCircleButtonTextPaint);

        }
    }

    /**
     * Adjust the width and height of the scale bar and the view itself
     *
     * @param width
     * @param height
     */
    private void initValues(int width, int height) {
        mViewWidth = width - getPaddingRight() - getPaddingLeft();
        mViewHeight = height;
        mCirclePotionX = (float) (mSelectProgress - mStartProgress) /
                (mMaxProgress - mStartProgress) * mViewWidth + getPaddingLeft();

        if (mTickBarHeight > mViewHeight) {
            mTickBarHeight = mViewHeight;
        }
        mTickBarRecf.set(getPaddingLeft(), (mViewHeight - mTickBarHeight) / 2,
                mViewWidth + getPaddingLeft(), mTickBarHeight / 2 +
                        mViewHeight / 2);
        if (mProgressHeight > mViewHeight) {
            mProgressHeight = mViewHeight;
        }

        mProgressRecf.set(getPaddingLeft(), (mViewHeight - mProgressHeight) / 2,
                mCirclePotionX, mProgressHeight / 2 + mViewHeight / 2);

        if (mCircleButtonRadius > mViewHeight / 2) {
            mCircleButtonRadius = mViewHeight / 2;
        }
        mCircleRecf.set(mCirclePotionX - mCircleButtonRadius, mViewHeight / 2 -
                        mCircleButtonRadius / 2,
                mCirclePotionX + mCircleButtonRadius, mViewHeight / 2 +
                        mCircleButtonRadius / 2);
    }

    public float getTickBarHeight() {
        return mTickBarHeight;
    }

    public void setTickBarHeight(float tickBarHeight) {
        mTickBarHeight = tickBarHeight;
    }

    public int getTickBarColor() {
        return mTickBarColor;
    }

    public void setTickBarColor(int tickBarColor) {
        mTickBarColor = tickBarColor;
    }

    public int getCircleButtonColor() {
        return mCircleButtonColor;
    }


    public void setCircleButtonColor(int circleButtonColor) {
        mCircleButtonColor = circleButtonColor;
    }

    public int getCircleButtonTextColor() {
        return mCircleButtonTextColor;
    }

    public void setCircleButtonTextColor(int circleButtonTextColor) {
        mCircleButtonTextColor = circleButtonTextColor;
    }

    public float getCircleButtonTextSize() {
        return mCircleButtonTextSize;
    }

    public void setCircleButtonTextSize(float circleButtonTextSize) {
        mCircleButtonTextSize = circleButtonTextSize;
    }


    public float getCircleButtonRadius() {
        return mCircleButtonRadius;
    }


    public void setCircleButtonRadius(float circleButtonRadius) {
        mCircleButtonRadius = circleButtonRadius;
    }


    public float getProgressHeight() {
        return mProgressHeight;
    }


    public void setProgressHeight(float progressHeight) {
        mProgressHeight = progressHeight;
    }


    public int getProgressColor() {
        return mProgressColor;
    }


    public void setProgressColor(int progressColor) {
        mProgressColor = progressColor;
    }

    public int getMaxProgress() {
        return mMaxProgress;
    }

    public void setMaxProgress(int maxProgress) {
        mMaxProgress = maxProgress;
    }

    public void setSelectProgress(int selectProgress) {
        this.setSelectProgress(selectProgress, false);
    }

    public void setSelectProgress(int selectProgress, boolean isNotifyListener) {
        getSelectProgressValue(selectProgress);
        Log.i(TAG, "mSelectProgress: " + mSelectProgress + "  mMaxProgress: " +
                mMaxProgress);
        if (mOnProgressChangeListener != null && isNotifyListener) {
            mOnProgressChangeListener.onChange(mSelectProgress, mTagid);
        }
        invalidate();
    }

    public void setTouchSelctProgress(int selectProgress) {
        getSelectProgressValue(selectProgress);
        invalidate();
    }

    private void getSelectProgressValue(int selectProgress) {
        mSelectProgress = selectProgress;
        if (mSelectProgress > mMaxProgress) {
            mSelectProgress = mMaxProgress;
        } else if (mSelectProgress <= mStartProgress) {
            mSelectProgress = mStartProgress;
        }
    }


    public int getSelectProgress() {
        return mSelectProgress;
    }


    public int getStartProgress() {
        return mStartProgress;
    }


    public void setStartProgress(int startProgress) {
        mStartProgress = startProgress;
    }

    private int getDpValue(int w) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, w, getContext()
                .getResources().getDisplayMetrics());
    }

    /**
     * Convert the value of the progress bar to the corresponding gear
     *
     * @param value progress value
     * @return
     */
    private int valueToGearPosition(int value) {
        int gearPosition = 0;
        int mLevelValue = this.getMaxProgress() / 6;
        gearPosition = 1 + value / mLevelValue;
        if (value % mLevelValue >= (mLevelValue / 3)) {
            gearPosition += 1;
        }
        if (gearPosition >= 7) {
            gearPosition = 7;
        }
        return gearPosition;
    }

    public int getLevel(){
        return mCurrentLevel;
    }
}
