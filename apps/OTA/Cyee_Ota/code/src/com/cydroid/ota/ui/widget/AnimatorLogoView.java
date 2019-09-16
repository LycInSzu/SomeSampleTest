package com.cydroid.ota.ui.widget;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.*;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;
import com.cydroid.ota.logic.IContextState;
import com.cydroid.ota.logic.State;
import com.cydroid.ota.utils.SystemPropertiesUtils;
import com.cydroid.ota.Log;
import com.cydroid.ota.R;

/**
 * @author borney
 *         Created by borney on 6/3/15.
 */
public class AnimatorLogoView extends TextView implements IStateView {
    private static final String TAG = "AnimatorLogoView";
    private static final int DEGRESS_DURATION_MAX = 2000;
    private static final int DEGRESS_DURATION_MIN = 800;
    private static final int SEC_DOA_DELAY = 230;
    private static final int REFRESH_TIME = 16;
    private Bitmap mLogoBitmap;
    private Bitmap mCheckingRotaBitmap;
    private Bitmap mProgressBitmap;
    private Bitmap mDoaBitmap;
    private IContextState mContextState, mLastContextState;
    private OnRotaSingleRingListener mOnRotaSingleRingListener;
    private Paint mBorderPaint;
    private Paint mBroadSidePaint;
    private float mCheckingDegrees = 0;
    private float mSweepAngle = 0;
    private float mFir_degrees = 0, mSec_degrees = 0;
    private int mBorderMaxWidth = 0;
    private int mPrecentSize = 0;
    private int mCircleColor;
    private int mProgress = -1;
    private boolean isDrawProgressAvailable = false;
    private boolean isRotaSingleRing = false;
    private Dynamics mFir_dynamics;
    private Dynamics mSec_dynamics;
    private Dynamics mDynamics_255_To_0;
    private Dynamics mDynamics_0_To_255;
    private AnimatorSet mAnimatorSet;
    private AnimatorSet mAnimatorSetReverse;
    private Context mContext;
    private Resources mResources;
    private int mLogoBitmapWidth;
    private int mLogoBitmapHeight;
    private int mWidth;
    private int mHeight;

    public AnimatorLogoView(Context context) {
        this(context, null);
    }

    public AnimatorLogoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AnimatorLogoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        initAttrs(attrs);
        initResources();
        initDynamics();
        initAnimator();
    }

    private void initAttrs(AttributeSet attrs) {
        TypedArray array = mContext.obtainStyledAttributes(attrs, R.styleable.AnimatorLogoView);
        mBorderMaxWidth = array.getDimensionPixelSize(R.styleable.AnimatorLogoView_borderWidth, 0);
        mPrecentSize = array.getDimensionPixelSize(R.styleable.AnimatorLogoView_precentSize, 0);
        mCircleColor = array.getColor(R.styleable.AnimatorLogoView_circleColor, Color.WHITE);
        array.recycle();
    }

    private void initResources() {
        //获取logo
        mLogoBitmap = getLogoBitmap();
        mLogoBitmapWidth = mLogoBitmap.getWidth();
        mLogoBitmapHeight = mLogoBitmap.getHeight();
        Log.d(TAG, "mLogoBitmap width = " + mLogoBitmap.getWidth() + "  height = " + mLogoBitmap.getHeight());
        mBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        mBorderPaint.setStyle(Paint.Style.STROKE);
        mBorderPaint.setColor(mCircleColor);
        mBorderPaint.setStrokeWidth(mBorderMaxWidth * 2f / 3f);
        mBroadSidePaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        mBroadSidePaint.setColor(colorForState());
        mWidth = (int) mResources.getDimension(R.dimen.gn_su_layout_main_animtorLogoview_width);
        mHeight = (int) mResources.getDimension(R.dimen.gn_su_layout_main_animtorLogoview_height);

        setGravity(Gravity.CENTER);
		//Chenyee <CY_Bug> <xuyongji> <20180210> modify for CSW1705A-1489 begin
        /*Typeface typeface = Typeface
                .createFromAsset(getContext().getAssets(), "fonts/Roboto-Thin.ttf");
        setTypeface(typeface);*/
		//Chenyee <CY_Bug> <xuyongji> <20180210> modify for CSW1705A-1489 end
    }

    private void initDynamics() {
        mFir_dynamics = new Dynamics(DEGRESS_DURATION_MAX);
        mSec_dynamics = new Dynamics(DEGRESS_DURATION_MAX);
        mFir_dynamics.setInterpolator(new DecelerateInterpolator());
        mSec_dynamics.setInterpolator(new DecelerateInterpolator());
        mDynamics_255_To_0 = new Dynamics();
        mDynamics_255_To_0.setInterpolator(new LinearInterpolator());
        mDynamics_0_To_255 = new Dynamics();
        mDynamics_0_To_255.setInterpolator(new LinearInterpolator());
    }

    private void initAnimator() {
        mAnimatorSet = (AnimatorSet) AnimatorInflater.loadAnimator(mContext, R.animator.gn_su_animator_logoview);
        mAnimatorSet.setTarget(this);
        mDynamics_255_To_0.setDuration(mResources.getInteger(R.integer.gn_su_animator_logoview_duration));
        mAnimatorSetReverse = (AnimatorSet) AnimatorInflater.loadAnimator(mContext, R.animator.gn_su_animator_logoview_reverse);
        mAnimatorSetReverse.setTarget(this);
        mDynamics_0_To_255.setDuration(mResources.getInteger(R.integer.gn_su_animator_logoview_reverse_duration));
    }

    @Override
    public void changeState(IContextState contextState) {
        mLastContextState = mContextState;
        mContextState = contextState;
        isDrawProgressAvailable = false;
        Log.debug(TAG, "changeState() state = " + contextState + " lastState = " + mLastContextState);
        switch (mContextState.state()) {
            case INITIAL:
                setText(null);
                if (mContextState.isBackState()) {
                    mBroadSidePaint.setAlpha(255);
                    mDynamics_255_To_0.setStartOffset(0);
                    post(mAlplh_255_To_0);
                }
                break;
            case CHECKING:
                setText(null);
                isRotaSingleRing = false;
                mCheckingDegrees = 0;
                mCheckingRotaBitmap = buildCheckingRotaBitmap();
                removeCallbacks(mCheckingRunnable);
                post(mCheckingRunnable);
                break;
            case READY_TO_DOWNLOAD:
                changeStateReadyToDownload();
                removeCallbacks(mDowongloadingRunnable);
                break;
            case DOWNLOADING:
                changeStateDownloading();
                break;
            case DOWNLOAD_INTERRUPT:
            case DOWNLOAD_PAUSE:
            case DOWNLOAD_PAUSEING:
            case DOWNLOAD_COMPLETE:
            case DOWNLOAD_VERIFY:
                mFir_degrees = 0;
                mSec_degrees = 0;
                isDrawProgressAvailable = true;
                Log.d(TAG, "isDrawProgressAvailable = true");
                removeCallbacks(mDowongloadingRunnable);
                break;
            case INSTALLING:
                break;
            default:
                break;
        }
        invalidate();
    }

    public void setOnRotaSingleRingListener(OnRotaSingleRingListener listener) {
        mOnRotaSingleRingListener = listener;
    }

    private void changeStateReadyToDownload() {
        mAnimatorSet.removeAllListeners();
        mAnimatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                mBroadSidePaint.setAlpha(255);
                post(mAlplh_255_To_0);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mBroadSidePaint.setAlpha(0);
                removeCallbacks(mAlplh_255_To_0);
                mDynamics_255_To_0.setStartOffset(0);
                invalidate();
            }
        });
        mAnimatorSet.start();
        if (mLastContextState == null) {
            mAnimatorSet.end();
        }
        setText(null);
        mCheckingDegrees = 0;
        removeCallbacks(mCheckingRunnable);
    }

    private void changeStateDownloading() {
        if (mLastContextState == null || mLastContextState.state() == State.READY_TO_DOWNLOAD) {
            mAnimatorSetReverse.removeAllListeners();
            mAnimatorSetReverse.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    mBroadSidePaint.setAlpha(0);
                    post(mAlpha_0_To_255);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    if (mContextState.state().value() > State.READY_TO_DOWNLOAD.value()) {
                        isDrawProgressAvailable = true;
                        setText(getPercentString());
                        mDynamics_0_To_255.setStartOffset(0);
                        mBroadSidePaint.setAlpha(255);
                        removeCallbacks(mAlpha_0_To_255);
                        post(mDowongloadingRunnable);
                        invalidate();
                    }
                }
            });
            mAnimatorSetReverse.start();
        } else {
            isDrawProgressAvailable = true;
            post(mDowongloadingRunnable);
        }
    }

    public void changeProgress(int progress) {
        String text = getText().toString();
        if (mProgress >= 0 && mProgress == progress && !TextUtils.isEmpty(text)) {
            return;
        }
        Log.debug(TAG, "mProgress = " + mProgress + " progress = " + progress + " text = (" + text + ")");
        mProgress = progress;
        recycleBitmap(mProgressBitmap);
        mProgressBitmap = getProgressBitmap(mProgress);
        SpannableString span = getPercentString();
        if (isDrawProgressAvailable) {
            setText(span);
            int duration = DEGRESS_DURATION_MIN + (DEGRESS_DURATION_MAX - DEGRESS_DURATION_MIN) * (100 - mProgress) / 100;
            if (mFir_dynamics != null) {
                mFir_dynamics.setDuration(duration);
            }
            if (mSec_dynamics != null) {
                mSec_dynamics.setDuration(duration);
            }
        }
    }

    private SpannableString getPercentString() {
        String str = mProgress + "%";
        SpannableString span = new SpannableString(str);
        span.setSpan(new AbsoluteSizeSpan(mPrecentSize, true), str.length() - 1, str.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return span;
    }

    @Override
    public void onDestory() {
        Log.d(TAG, "onDestory()");
        releaseBitmap();
    }

    private void releaseBitmap() {
        recycleBitmap(mLogoBitmap);
        recycleBitmap(mCheckingRotaBitmap);
        recycleBitmap(mProgressBitmap);
        recycleBitmap(mDoaBitmap);
    }

    private void recycleBitmap(Bitmap bitmap) {
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
        }
    }

    private Bitmap buildDoaBitmap() {
        if (mDoaBitmap == null || mDoaBitmap.isRecycled()) {
            Bitmap bitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            Paint p = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
            p.setColor(colorForState());
            p.setStyle(Paint.Style.FILL);
            canvas.drawCircle(mWidth / 2f, mBorderMaxWidth / 2f, mBorderMaxWidth / 2f, p);
            mDoaBitmap = bitmap;
        }
        return mDoaBitmap;
    }

    private int colorForState() {
        return getTextColors().getColorForState(getDrawableState(), 0);
    }

    private Bitmap getProgressBitmap(int progress) {
        Resources res = getResources();
        int width = (int) res.getDimension(R.dimen.gn_su_layout_main_animtorLogoview_width);
        int height = (int) res.getDimension(R.dimen.gn_su_layout_main_animtorLogoview_height);
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        p.setColor(colorForState());
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(mBorderMaxWidth);
        RectF oval = new RectF(mBorderMaxWidth / 2f, mBorderMaxWidth / 2f, width - mBorderMaxWidth / 2f, height - mBorderMaxWidth / 2f);
        mSweepAngle = -((float) progress / 100) * 360;
        canvas.drawArc(oval, -90, mSweepAngle, false, p);
        Bitmap doaBitmap = buildDoaBitmap();
        canvas.drawBitmap(doaBitmap, 0, 0, null);
        canvas.save();
        canvas.rotate(mSweepAngle, width / 2f, height / 2f);
        canvas.drawBitmap(doaBitmap, 0, 0, null);
        canvas.restore();
        return bitmap;
    }

    private Bitmap buildCheckingRotaBitmap() {
        Bitmap bitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        p.setColor(colorForState());
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(mBorderMaxWidth);
        RectF oval = new RectF(mBorderMaxWidth / 2f, mBorderMaxWidth / 2f, mWidth - mBorderMaxWidth / 2f, mHeight - mBorderMaxWidth / 2f);
        canvas.drawArc(oval, -90, 60.0f, false, p);
        Bitmap doaBitmap = buildDoaBitmap();
        canvas.drawBitmap(doaBitmap, 0, 0, null);
        canvas.save();
        canvas.rotate(60.0f, mWidth / 2f, mHeight / 2f);
        canvas.drawBitmap(doaBitmap, 0, 0, null);
        canvas.restore();
        return bitmap;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.TRANSPARENT);
        canvas.drawCircle(mWidth / 2f, mHeight / 2f, (mWidth - mBorderMaxWidth) / 2f, mBorderPaint);
        if (mContextState == null) {
            return;
        }
        switch (mContextState.state()) {
            case INITIAL:
                canvas.drawBitmap(mLogoBitmap, (mWidth - mLogoBitmapWidth) / 2f, (mHeight - mLogoBitmapHeight) / 2f, null);
                if (mCheckingRotaBitmap != null && !mCheckingRotaBitmap.isRecycled()) {
                    canvas.drawBitmap(mCheckingRotaBitmap, 0, 0, mBroadSidePaint);
                }
                break;
            case CHECKING:
                canvas.drawBitmap(mLogoBitmap, (mWidth - mLogoBitmapWidth) / 2f, (mHeight - mLogoBitmapHeight) / 2f, null);
                canvas.save();
                canvas.rotate(mCheckingDegrees, mWidth / 2f, mHeight / 2f);
                canvas.drawBitmap(mCheckingRotaBitmap, 0, 0, null);
                canvas.restore();
                break;
            case READY_TO_DOWNLOAD:
                canvas.drawBitmap(mLogoBitmap, (mWidth - mLogoBitmapWidth) / 2f, (mHeight - mLogoBitmapHeight) / 2f, null);
                if (mCheckingRotaBitmap != null && !mCheckingRotaBitmap.isRecycled()) {
                    canvas.drawBitmap(mCheckingRotaBitmap, 0, 0, mBroadSidePaint);
                }
                break;
            case DOWNLOADING:
                if (isDrawProgressAvailable) {
                    drawDownload(canvas, null);
                } else {
                    canvas.drawBitmap(mLogoBitmap, (mWidth - mLogoBitmapWidth) / 2f, (mHeight - mLogoBitmapHeight) / 2f, null);
                    drawDownload(canvas, mBroadSidePaint);
                }
                break;
            case DOWNLOAD_INTERRUPT:
            case DOWNLOAD_PAUSE:
            case DOWNLOAD_PAUSEING:
            case DOWNLOAD_COMPLETE:
            case DOWNLOAD_VERIFY:
                if (mProgressBitmap != null && !mProgressBitmap.isRecycled()) {
                    canvas.drawBitmap(mProgressBitmap, 0, 0, mBroadSidePaint);
                }
                break;
            default:

                break;
        }
    }

    private void drawDownload(Canvas canvas, Paint borderpaint) {
        Bitmap doaBitmap = buildDoaBitmap();
        if (doaBitmap != null && !doaBitmap.isRecycled()) {
            canvas.save();
            canvas.rotate(mFir_degrees, mWidth / 2f, mHeight / 2f);
            canvas.drawBitmap(doaBitmap, 0, 0, borderpaint);
            canvas.restore();
            canvas.save();
            canvas.rotate(mSec_degrees, mWidth / 2f, mHeight / 2f);
            canvas.drawBitmap(doaBitmap, 0, 0, borderpaint);
            canvas.restore();
        }
        if (mProgressBitmap != null && !mProgressBitmap.isRecycled()) {
            canvas.drawBitmap(mProgressBitmap, 0, 0, borderpaint);
        }
    }

    private Runnable mCheckingRunnable = new Runnable() {
        @Override
        public void run() {
            mCheckingDegrees += 3;
            if (mCheckingDegrees >= 360) {
                if (!isRotaSingleRing) {
                    isRotaSingleRing = true;
                    if (mOnRotaSingleRingListener != null) {
                        Log.d(TAG, "OnRotaSingleRingListener->onRotaSingleRing()");
                        mOnRotaSingleRingListener.onRotaSingleRing();
                    }
                }
                mCheckingDegrees = 0;
            }
            invalidate();
            postDelayed(this, REFRESH_TIME);
        }
    };

    private Runnable mAlplh_255_To_0 = new Runnable() {
        @Override
        public void run() {
            long now = System.currentTimeMillis();
            if (mDynamics_255_To_0.getStartOffset() == 0) {
                mDynamics_255_To_0.setStartOffset(now);
                delayedRestart();
                return;
            }
            int alpha = mBroadSidePaint.getAlpha();
            if (mDynamics_255_To_0.getStartOffset() != 0) {
                alpha = 255 - (int) (mDynamics_255_To_0.update(now) * 255);
            }
            if (alpha >= 0 && alpha <= 255) {
                mBroadSidePaint.setAlpha(alpha);
                invalidate();
                delayedRestart();
            } else {
                mBroadSidePaint.setAlpha(0);
                invalidate();
                removeCallbacks(this);
            }
        }

        private void delayedRestart() {
            removeCallbacks(this);
            postDelayed(this, REFRESH_TIME);
        }
    };

    private Runnable mAlpha_0_To_255 = new Runnable() {
        @Override
        public void run() {
            long now = System.currentTimeMillis();
            if (mDynamics_0_To_255.getStartOffset() == 0) {
                mDynamics_0_To_255.setStartOffset(now);
                delayedRestart();
                return;
            }
            int alpha = mBroadSidePaint.getAlpha();
            if (mDynamics_0_To_255.getStartOffset() != 0) {
                alpha = (int) (mDynamics_0_To_255.update(now) * 255);
            }
            if (alpha >= 0 && alpha <= 255) {
                mBroadSidePaint.setAlpha(alpha);
                invalidate();
                delayedRestart();
            } else {
                mBroadSidePaint.setAlpha(255);
                invalidate();
                removeCallbacks(this);
            }
        }

        private void delayedRestart() {
            removeCallbacks(this);
            postDelayed(this, REFRESH_TIME);
        }
    };

    private Runnable mDowongloadingRunnable = new Runnable() {
        @Override
        public void run() {
            long now = System.currentTimeMillis();
            if (mFir_dynamics.getStartOffset() == 0) {
                mFir_dynamics.setStartOffset(now);
                delayedRestart();
                return;
            }

            if ((now - mFir_dynamics.getStartOffset()) >= SEC_DOA_DELAY && mSec_dynamics.getStartOffset() == 0) {
                mSec_dynamics.setStartOffset(now);
                delayedRestart();
                return;
            }
            if (mFir_dynamics.getStartOffset() != 0) {
                mFir_degrees = (360 + mSweepAngle) * mFir_dynamics.update(now);
            }

            if (mSec_dynamics.getStartOffset() != 0) {
                mSec_degrees = (360 + mSweepAngle) * mSec_dynamics.update(now);
            }
            if (mFir_degrees > 0 || mSec_degrees > 0) {
                if (mFir_degrees < 0) {
                    mFir_degrees = 0;
                }
                if (mSec_degrees < 0) {
                    mSec_degrees = 0;
                }
                invalidate();
                delayedRestart();
            } else {
                mFir_degrees = 0;
                mSec_degrees = 0;
                mFir_dynamics.setStartOffset(0);
                mSec_dynamics.setStartOffset(0);
                delayedRestart();
            }
        }

        private void delayedRestart() {
            removeCallbacks(this);
            postDelayed(this, REFRESH_TIME);
        }
    };

    public interface OnRotaSingleRingListener {
        void onRotaSingleRing();
    }

    public Bitmap getLogoBitmap() {
        mResources = getResources();
        DisplayMetrics displayMetrics = mResources.getDisplayMetrics();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inDensity = displayMetrics.densityDpi;
        if (SystemPropertiesUtils.sCustom.equals("AFGHANISTAN_ASHNA")) {
            return BitmapFactory.decodeResource(mResources, R.drawable.gn_su_anim_bitmap_logo_ah, options);
        }
        if (SystemPropertiesUtils.sCustom.equals("ARGENTINA_SOLNIK")) {
            return BitmapFactory.decodeResource(mResources, R.drawable.gn_su_anim_bitmap_logo_hy, options);
        }
        return BitmapFactory.decodeResource(mResources, R.drawable.gn_su_anim_bitmap_logo, options);
    }
}
