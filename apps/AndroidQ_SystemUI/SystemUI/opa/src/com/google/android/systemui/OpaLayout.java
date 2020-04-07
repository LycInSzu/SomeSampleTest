package com.google.android.systemui;

import static com.android.systemui.recents.OverviewProxyService.OverviewProxyListener;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.AttrRes;
import android.annotation.NonNull;
import android.annotation.Nullable;
import android.annotation.StyleRes;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.util.ArraySet;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.MotionEvent;
import android.view.RenderNodeAnimator;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import com.android.systemui.Dependency;

import java.util.ArrayList;

import com.android.systemui.Interpolators;
import com.android.systemui.recents.OverviewProxyService;
import com.android.systemui.R;
import com.android.systemui.assist.AssistManager;
import com.android.systemui.statusbar.phone.ButtonInterface;
import com.android.systemui.statusbar.policy.KeyButtonDrawable;
import com.android.systemui.statusbar.policy.KeyButtonView;
import com.android.systemui.shared.system.QuickStepContract;
import java.util.ArrayList;

/**
 * Custom ViewGroup intended to replace the Home Button when OPA is enabled.
 * Implements long-press and tap animations.
 */
public class OpaLayout extends FrameLayout implements ButtonInterface {
    private static final String TAG = "OpaLayout";

    /**
     * Minimum time to spend in diamond animation.
     */
    private static final int MIN_DIAMOND_DURATION = 100;
    /** 
     * Length of time to spend in the diamond animation.
     */
    private static final int DIAMOND_ANIMATION_DURATION = 200;
    /**
     * Time to spend in diamond animation when delayed touched feedback is on played on touch up
     */
    private static final int DELAYED_TOUCH_DIAMOND_DURATION = MIN_DIAMOND_DURATION * 2;
    /**
     * Length of time to spend shrinking the halo.
     */
    private static final int HALO_ANIMATION_DURATION = 100;
    /**
     * Length of time to spend changing Y for straight line animation.
     */
    private static final int LINE_ANIMATION_DURATION_Y = 133;
    /** 
     * Length of time to spend changing X for straight line animation. 
     */
    private static final int LINE_ANIMATION_DURATION_X = 225;
    /**
     * Length of time for collapse animation for green and blue.
     */
    static final int COLLAPSE_DURATION_BG = 150;
    /**
     * Length of time for collapse animation for yellow and red.
     */
    static final int COLLAPSE_DURATION_RY = 133;
    /** 
     * Length of time for dots fullsize animation. 
     */
    private static final int DOTS_RESIZE_DURATION = 200;
    /**
     * Length of time for home button resize animation.
     */
    private static final int HOME_RESIZE_DURATION = 83;
    /**
     * Length of time for home reappear animation.
     */
    private static final int HOME_REAPPEAR_DURATION = 150;
   /** 
    * Length of time for retract animation.
    */
    private static final int RETRACT_ANIMATION_DURATION = 300;
    private static final int RETRACT_ALPHA_OFFSET = 50;
    private static final int ALPHA_ANIMATION_LENGTH = 50;
    /**
     * Offset between the start of collapse animation and the start of the
     * home reappear.
     */
    private static final int HOME_REAPPEAR_ANIMATION_OFFSET = 33;

    /**
     * Scale factor for dots size change during diamond animation.
     */
    private static final float DIAMOND_DOTS_SCALE_FACTOR = 0.8f;
    /**
     * Scale factor for home size change during diamond animation.
     */
    private static final float DIAMOND_HOME_SCALE_FACTOR = 0.625f;
    /**
     * Scale factor for halo size change during diamond animation.
     */
    private static final float HALO_SCALE_FACTOR = 10.0f / 21.0f;

    /** Standard 80/40 interpolator */
    private final Interpolator mFastOutSlowInInterpolator = Interpolators.FAST_OUT_SLOW_IN;
    /** 0/80 path interpolator for home button disappearance. */
    private final Interpolator mHomeDisappearInterpolator = new PathInterpolator(.65f, 0f ,1f ,1f);
    /** Outgoing interpolator for the collapse animation. */
    private final Interpolator mCollapseInterpolator = Interpolators.FAST_OUT_LINEAR_IN;
    /** 100/40 interpolator for dot resize to full size. */
    private final Interpolator mDotsFullSizeInterpolator = new PathInterpolator(0.4f, 0f, 0f, 1f);
    /** 100/40 interpolator for retract animation. */
    private final Interpolator mRetractInterpolator = new PathInterpolator(0.4f, 0f, 0f, 1f);
    /** Interpolator for diamond animation. */
    private final Interpolator mDiamondInterpolator = new PathInterpolator(.2f,0f,.2f,1f);
    /** 0/80 path interpolator for home button disappearance. */
    private final Interpolator HOME_DISAPPEAR_INTERPOLATOR = new PathInterpolator(.65f, 0f, 1f, 1f);
    /** 40/40 interpolator for gesture animation. */
    static final Interpolator INTERPOLATOR_40_40 = new PathInterpolator(0.4f, 0f, 0.6f, 1f);
    /** 40% outgoing interpolator for collapse. */
    static final Interpolator INTERPOLATOR_40_OUT = new PathInterpolator(0.4f, 0f, 1f, 1f);

    private static final int ANIMATION_STATE_NONE = 0;
    private static final int ANIMATION_STATE_DIAMOND = 1;
    private static final int ANIMATION_STATE_RETRACT = 2;
    private static final int ANIMATION_STATE_OTHER = 3;
    

    private final ArraySet<Animator> mCurrentAnimators = new ArraySet<>();
    private final ArrayList<View> mAnimatedViews = new ArrayList<>();

    // Time that the animation started.
    private long mStartTime;

    private View mBlue;
    private View mRed;
    private View mYellow;
    private View mGreen;
    private ImageView mWhite;
    private ImageView mWhiteCutout;
    private ImageView mHalo;
    private KeyButtonView mHome;

    private boolean mDelayTouchFeedback;
    private boolean mDiamondAnimationDelayed;
    private int mTouchDownX;
    private int mTouchDownY;
    private int mScrollTouchSlop;

    private View mTop;
    private View mBottom;
    private View mRight;
    private View mLeft;

    private Resources mResources;

    private int mAnimationState = ANIMATION_STATE_NONE;
    private boolean mWindowVisible;

    private int mHaloDiameter;

    private OverviewProxyService mOverviewProxyService;

    /**
     * Starts retract animation before diamond animation has completed.
     */
    private final Runnable mRetract = new Runnable() {
        @Override
        public void run() {
            // Cancel the diamond animation. We want to start retract from the current state.
            cancelCurrentAnimation();
            startRetractAnimation();
        }
    };
    private boolean mIsVertical;
    private boolean mLongClicked;
    /**
     * Track pressed state locally.
     */
    private boolean mIsPressed;
    /**
     * Starts line animation on long press.
     */
    private final Runnable mCheckLongPress = new Runnable() {
        @Override
        public void run() {
            if (mIsPressed) {
                mLongClicked = true;
            }
        }
    };

    private final OverviewProxyListener mOverviewProxyListener = new OverviewProxyListener() {
        @Override
        public void onConnectionChanged(boolean isConnected) {
            updateOpaLayout();
        }

        @Override
        public void onNavBarButtonAlphaChanged(float alpha, boolean animate) {
            updateOpaLayout();
        }

        @Override
        public void onSystemUiStateChanged(int sysuiStateFlags) {
            updateOpaLayout();
        }
    };

    private boolean mOpaEnabledNeedsUpdate;  // Does the value of mOpaEnabled need to be updated?
    private boolean mOpaEnabled;

    public OpaLayout(@NonNull Context context) {
        this(context, null);
    }

    public OpaLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public OpaLayout(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int
            defStyleAttr, @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mScrollTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    public OpaLayout(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int
            defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mResources = getResources();

        mBlue = findViewById(R.id.blue);
        mRed = findViewById(R.id.red);
        mYellow = findViewById(R.id.yellow);
        mGreen = findViewById(R.id.green);
        mWhite = (ImageView) findViewById(R.id.white);
        mWhiteCutout = (ImageView) findViewById(R.id.white_cutout);
        mHalo = (ImageView) findViewById(R.id.halo);
        mHome = (KeyButtonView) findViewById(R.id.home_button);

        mHalo.setImageDrawable(KeyButtonDrawable.create(getContext(), R.drawable.halo,
                false /* hasShadow */));
        mHaloDiameter = mResources.getDimensionPixelSize(R.dimen.halo_diameter);

        // The mWhiteCutout view has the same drawable and animations of the mWhite view, and is
        // positioned directly behind it; the dots are positioned behind the cutout. A CLEAR blend
        // mode causes the cutout to erase a home-button-shaped area on the canvas before drawing
        // the actual home button on top -- thereby erasing any dots that might show through
        // a non-opaque home button.
        Paint cutoutPaint = new Paint();
        cutoutPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
        mWhiteCutout.setLayerType(View.LAYER_TYPE_HARDWARE, cutoutPaint);

        mAnimatedViews.add(mBlue);
        mAnimatedViews.add(mRed);
        mAnimatedViews.add(mYellow);
        mAnimatedViews.add(mGreen);
        mAnimatedViews.add(mWhite);
        mAnimatedViews.add(mWhiteCutout);
        mAnimatedViews.add(mHalo);

        mOpaEnabledNeedsUpdate = true;

        mOverviewProxyService = Dependency.get(OverviewProxyService.class);

        setOpaEnabled(UserSettingsUtils.load(getContext().getContentResolver()));
    }

    @Override
    public void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        mWindowVisible = visibility == VISIBLE;
        //modify for EWSWQ-244 by liyuchong 20200225 begin
//        if (visibility == VISIBLE) {
//            updateOpaLayout();
//        } else {
//            // Reset everything when the window (i.e. the navigation bar) becomes hidden.
//            cancelCurrentAnimation();
//            skipToStartingValue();
//        }
        if (visibility == VISIBLE) {
            cancelCurrentAnimation();
            skipToStartingValue();
            updateOpaLayout();
        }
        //modify for EWSWQ-244 by liyuchong 20200225 end
    }

    @Override
    public void setOnLongClickListener(@Nullable OnLongClickListener l) {
        // Make sure that the collapse animation is synced with the long click.
        mHome.setOnLongClickListener(v -> {
            return l.onLongClick(mHome);
        });
    }

    @Override
    public void setOnTouchListener(OnTouchListener l) {
        mHome.setOnTouchListener(l);
    }

    private final Runnable mDiamondAnimation = () -> {
        if (mCurrentAnimators.isEmpty()) {
            startDiamondAnimation();
        }
    };

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // Do nothing if opa is not enabled or animators are disabled.
        if (!mOpaEnabled || !ValueAnimator.areAnimatorsEnabled()) {
            return false;
        }
        final int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                // Use raw X and Y to detect gestures in case a parent changes the x and y values
                mTouchDownX = (int) ev.getRawX();
                mTouchDownY = (int) ev.getRawY();

                // If an animation is in progress, do not allow it to be interrupted by another
                // down event UNLESS the current animation is a retract (that returns the dots
                // to a resting state), in which case we finish it immediately and start the new
                // animation.
                boolean isRetracting = false;
                if (!mCurrentAnimators.isEmpty()) {
                    if (mAnimationState == ANIMATION_STATE_RETRACT) {
                        endCurrentAnimation();
                        isRetracting = true;
                    } else {
                        return false;
                    }
                }
                mStartTime = SystemClock.elapsedRealtime();
                mLongClicked = false;
                mIsPressed = true;

                removeCallbacks(mDiamondAnimation);
                removeCallbacks(mRetract);
                removeCallbacks(mCheckLongPress);
                postDelayed(mCheckLongPress, ViewConfiguration.getLongPressTimeout());

                // If not delayed touch feedback or is currently retracting, start diamond animation
                // on touch down now instead of delaying
                if (!mDelayTouchFeedback || isRetracting) {
                    mDiamondAnimationDelayed = false;
                    startDiamondAnimation();
                } else {
                    mDiamondAnimationDelayed = true;
                    postDelayed(mDiamondAnimation, ViewConfiguration.getTapTimeout());
                }
                break;
            case MotionEvent.ACTION_MOVE:
                float slop = QuickStepContract.getQuickStepTouchSlopPx(getContext());
                if (Math.abs((int)ev.getRawX() - mTouchDownX) > slop || Math.abs((int)ev.getRawY() - mTouchDownY) > slop) {
                    abortCurrentGesture();
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                // If animation played starting with a delay, retract after another delay so
                // pressing the button plays some of the animation
                if (mDiamondAnimationDelayed) {
                    if (mIsPressed && !mLongClicked) {
                        postDelayed(mRetract, DELAYED_TOUCH_DIAMOND_DURATION);
                    }
                } else {
                    if (mAnimationState == ANIMATION_STATE_DIAMOND) {
                        // Enforce the minimum duration.
                        final long targetTime = MIN_DIAMOND_DURATION
                                - (SystemClock.elapsedRealtime() - mStartTime);
                        removeCallbacks(mRetract);
                        postDelayed(mRetract, targetTime);
                        removeCallbacks(mDiamondAnimation);
                        removeCallbacks(mCheckLongPress);
                        return false;
                    }
                    final boolean doRetract = mIsPressed && !mLongClicked;
                    if (doRetract) {
                        mRetract.run();
                    }
                }
                mIsPressed = false;
                break;
        }
        return false;
    }

    @Override
    public void setAccessibilityDelegate(@Nullable AccessibilityDelegate delegate) {
        super.setAccessibilityDelegate(delegate);
        mHome.setAccessibilityDelegate(delegate);
    }

    @Override
    public void setImageDrawable(@Nullable Drawable drawable) {
        mWhite.setImageDrawable(drawable);
        mWhiteCutout.setImageDrawable(drawable);
    }

    @Override
    public void abortCurrentGesture() {
        Log.w(TAG, "***Called abortCurrentGesture");
        mHome.abortCurrentGesture();
        mIsPressed = false;
        mLongClicked = false;
        mDiamondAnimationDelayed = false;
        removeCallbacks(mDiamondAnimation);
        removeCallbacks(mCheckLongPress);
        if (mAnimationState == ANIMATION_STATE_OTHER
                || mAnimationState == ANIMATION_STATE_DIAMOND) {
            mRetract.run();
        }
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateOpaLayout();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mOverviewProxyService.addCallback(mOverviewProxyListener);
        updateOpaLayout();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mOverviewProxyService.removeCallback(mOverviewProxyListener);
    }

    private void startDiamondAnimation() {
        if (allowAnimations()) {
            mCurrentAnimators.clear();
            setDotsVisible();
            mCurrentAnimators.addAll(getDiamondAnimatorSet());
            mAnimationState = ANIMATION_STATE_DIAMOND;
            startAll(mCurrentAnimators);
        } else {
            skipToStartingValue();
        }
    }

    private void startRetractAnimation() {
        if (allowAnimations()) {
            mCurrentAnimators.clear();
            mCurrentAnimators.addAll(getRetractAnimatorSet());
            mAnimationState = ANIMATION_STATE_RETRACT;
            startAll(mCurrentAnimators);
        } else {
            skipToStartingValue();
        }
    }

    private void startLineAnimation() {
        if (allowAnimations()) {
            mCurrentAnimators.clear();
            mCurrentAnimators.addAll(getLineAnimatorSet());
            mAnimationState = ANIMATION_STATE_OTHER;
            startAll(mCurrentAnimators);
        } else {
            skipToStartingValue();
        }
    }

    private void startCollapseAnimation() {
        if (allowAnimations()) {
            mCurrentAnimators.clear();
            mCurrentAnimators.addAll(getCollapseAnimatorSet());
            mAnimationState = ANIMATION_STATE_OTHER;
            startAll(mCurrentAnimators);
        } else {
            skipToStartingValue();
        }
    }

    /**
     * @return A RenderNodeAnimator that scales the x of the view by factor.
     */
    private Animator getScaleAnimatorX(View v, float factor, int duration,
            Interpolator interpolator) {
        final RenderNodeAnimator animator = new RenderNodeAnimator(RenderNodeAnimator.SCALE_X, factor);
        animator.setTarget(v);
        animator.setInterpolator(interpolator);
        animator.setDuration(duration);
        return animator;
    }

    /**
     * @return A RenderNodeAnimator that scales the y of the view by factor.
     */
    private Animator getScaleAnimatorY(View v, float factor, int duration,
            Interpolator interpolator) {
        final RenderNodeAnimator animator = new RenderNodeAnimator(RenderNodeAnimator.SCALE_Y, factor);
        animator.setTarget(v);
        animator.setInterpolator(interpolator);
        animator.setDuration(duration);
        return animator;
    }

    /**
     * @return A RenderNodeAnimator that translates the view deltaX from its current x.
     */
    private Animator getDeltaAnimatorX(View v, Interpolator interpolator, float deltaX,
            int duration) {
        final RenderNodeAnimator animator = new RenderNodeAnimator(RenderNodeAnimator.X, v.getX() + deltaX);
        animator.setTarget(v);
        animator.setInterpolator(interpolator);
        animator.setDuration(duration);
        return animator;
    }

    /**
     * @return A RenderNodeAnimator that translates the view deltaY from its current y.
     */
    private Animator getDeltaAnimatorY(View v, Interpolator interpolator, float deltaY,
            int duration) {
        final RenderNodeAnimator animator = new RenderNodeAnimator(RenderNodeAnimator.Y, v.getY() + deltaY);
        animator.setTarget(v);
        animator.setInterpolator(interpolator);
        animator.setDuration(duration);
        return animator;
    }

    /**
     * @return a RenderNodeAnimator that moves the view back to its initial inflated X-position.
     */
    private Animator getTranslationAnimatorX(View v, Interpolator interpolator, int duration) {
        final RenderNodeAnimator animator = new RenderNodeAnimator(RenderNodeAnimator.TRANSLATION_X, 0);
        animator.setTarget(v);
        animator.setInterpolator(interpolator);
        animator.setDuration(duration);
        return animator;
    }

    /**
     * @return a RenderNodeAnimator that moves the view back to its initial inflated Y-position.
     */
    private Animator getTranslationAnimatorY(View v, Interpolator interpolator, int duration) {
        final RenderNodeAnimator animator = new RenderNodeAnimator(RenderNodeAnimator.TRANSLATION_Y, 0);
        animator.setTarget(v);
        animator.setInterpolator(interpolator);
        animator.setDuration(duration);
        return animator;
    }

    private Animator getAlphaAnimator(View v, float alpha, int duration,
                                      Interpolator interpolator) {
        return getAlphaAnimator(v, alpha, duration, 0 /* startDelay */, interpolator);
    }

    /**
     * @return a RenderNodeAnimator that moves the view back to its initial inflated Y-position.
     */
    private Animator getAlphaAnimator(View v, float alpha, int duration, int startDelay,
                                      Interpolator interpolator) {
        final RenderNodeAnimator animator = new RenderNodeAnimator(RenderNodeAnimator.ALPHA, alpha);
        animator.setTarget(v);
        animator.setInterpolator(interpolator);
        animator.setDuration(duration);
        animator.setStartDelay(startDelay);
        return animator;
    }

    private void startAll(ArraySet<Animator> animators) {
        for (int i = animators.size() - 1; i >= 0; i--) {
            animators.valueAt(i).start();
        }
    }

    private boolean allowAnimations() {
        return isAttachedToWindow() && mWindowVisible;
    }

    /**
     * Get the pixel value of the given resource dimension (in dp).
     */
    private float getPxVal(int id) {
        return getResources().getDimensionPixelOffset(id);
    }

    private ArraySet<Animator> getDiamondAnimatorSet() {
        final ArraySet<Animator> animators = new ArraySet<>();

        // Animate top
        animators.add(getDeltaAnimatorY(mTop, mDiamondInterpolator,
                -getPxVal(R.dimen.opa_diamond_translation), DIAMOND_ANIMATION_DURATION));
        animators.add(getScaleAnimatorX(mTop, DIAMOND_DOTS_SCALE_FACTOR, DIAMOND_ANIMATION_DURATION,
                mFastOutSlowInInterpolator));
        animators.add(getScaleAnimatorY(mTop, DIAMOND_DOTS_SCALE_FACTOR, DIAMOND_ANIMATION_DURATION,
                mFastOutSlowInInterpolator));
        animators.add(getAlphaAnimator(mTop, 1.0f,
                ALPHA_ANIMATION_LENGTH, Interpolators.LINEAR));

        // Animate bottom
        animators.add(getDeltaAnimatorY(mBottom, mDiamondInterpolator,
                getPxVal(R.dimen.opa_diamond_translation), DIAMOND_ANIMATION_DURATION));
        animators.add(getScaleAnimatorX(mBottom, DIAMOND_DOTS_SCALE_FACTOR,
                DIAMOND_ANIMATION_DURATION, mFastOutSlowInInterpolator));
        animators.add(getScaleAnimatorY(mBottom, DIAMOND_DOTS_SCALE_FACTOR,
                DIAMOND_ANIMATION_DURATION, mFastOutSlowInInterpolator));
        animators.add(getAlphaAnimator(mBottom, 1.0f,
                ALPHA_ANIMATION_LENGTH, Interpolators.LINEAR));

        // Animate left
        animators.add(getDeltaAnimatorX(mLeft, mDiamondInterpolator,
                -getPxVal(R.dimen.opa_diamond_translation), DIAMOND_ANIMATION_DURATION));
        animators.add(getScaleAnimatorX(mLeft, DIAMOND_DOTS_SCALE_FACTOR,
                DIAMOND_ANIMATION_DURATION, mFastOutSlowInInterpolator));
        animators.add(getScaleAnimatorY(mLeft, DIAMOND_DOTS_SCALE_FACTOR,
                DIAMOND_ANIMATION_DURATION, mFastOutSlowInInterpolator));
        animators.add(getAlphaAnimator(mLeft, 1.0f,
                ALPHA_ANIMATION_LENGTH, Interpolators.LINEAR));

        // Animate right
        animators.add(getDeltaAnimatorX(mRight, mDiamondInterpolator,
                getPxVal(R.dimen.opa_diamond_translation), DIAMOND_ANIMATION_DURATION));
        animators.add(getScaleAnimatorX(mRight, DIAMOND_DOTS_SCALE_FACTOR,
                DIAMOND_ANIMATION_DURATION, mFastOutSlowInInterpolator));
        animators.add(getScaleAnimatorY(mRight, DIAMOND_DOTS_SCALE_FACTOR,
                DIAMOND_ANIMATION_DURATION, mFastOutSlowInInterpolator));
        animators.add(getAlphaAnimator(mRight, 1.0f,
                ALPHA_ANIMATION_LENGTH, Interpolators.LINEAR));

        // Animate home
        animators.add(getScaleAnimatorX(mWhite, DIAMOND_HOME_SCALE_FACTOR,
                DIAMOND_ANIMATION_DURATION, mFastOutSlowInInterpolator));
        animators.add(getScaleAnimatorY(mWhite, DIAMOND_HOME_SCALE_FACTOR,
                DIAMOND_ANIMATION_DURATION, mFastOutSlowInInterpolator));
        animators.add(getScaleAnimatorX(mWhiteCutout, DIAMOND_HOME_SCALE_FACTOR,
                DIAMOND_ANIMATION_DURATION, mFastOutSlowInInterpolator));
        animators.add(getScaleAnimatorY(mWhiteCutout, DIAMOND_HOME_SCALE_FACTOR,
                DIAMOND_ANIMATION_DURATION, mFastOutSlowInInterpolator));

        // Animate halo
        animators.add(getScaleAnimatorX(mHalo, HALO_SCALE_FACTOR,
                HALO_ANIMATION_DURATION, mFastOutSlowInInterpolator));
        animators.add(getScaleAnimatorY(mHalo, HALO_SCALE_FACTOR,
                HALO_ANIMATION_DURATION, mFastOutSlowInInterpolator));
        animators.add(getAlphaAnimator(mHalo, 0.0f, HALO_ANIMATION_DURATION,
                mFastOutSlowInInterpolator));

        getLongestAnim(animators).addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {
                mCurrentAnimators.clear();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                startLineAnimation();
            }
        });
        return animators;
    }

    private ArraySet<Animator> getRetractAnimatorSet() {
        final ArraySet<Animator> animators = new ArraySet<>();

        // We don't need separate landscape and portrait cases for this animation since we're
        // animating in both x and y directions in either case (just in case we start retraction
        // after the diamond is complete).

        // Animate red
        animators.add(getTranslationAnimatorX(mRed, mRetractInterpolator, RETRACT_ANIMATION_DURATION));
        animators.add(getTranslationAnimatorY(mRed, mRetractInterpolator, RETRACT_ANIMATION_DURATION));
        animators.add(getScaleAnimatorX(mRed, 1.0f,
                RETRACT_ANIMATION_DURATION, mRetractInterpolator));
        animators.add(getScaleAnimatorY(mRed, 1.0f,
                RETRACT_ANIMATION_DURATION, mRetractInterpolator));
        animators.add(getAlphaAnimator(mRed, 0.0f, ALPHA_ANIMATION_LENGTH,
                RETRACT_ALPHA_OFFSET, Interpolators.LINEAR));

        // Animate blue
        animators.add(
                getTranslationAnimatorX(mBlue, mRetractInterpolator, RETRACT_ANIMATION_DURATION));
        animators.add(
                getTranslationAnimatorY(mBlue, mRetractInterpolator, RETRACT_ANIMATION_DURATION));
        animators.add(getScaleAnimatorX(mBlue, 1.0f,
                RETRACT_ANIMATION_DURATION, mRetractInterpolator));
        animators.add(getScaleAnimatorY(mBlue, 1.0f,
                RETRACT_ANIMATION_DURATION, mRetractInterpolator));
        animators.add(getAlphaAnimator(mBlue, 0.0f, ALPHA_ANIMATION_LENGTH,
                RETRACT_ALPHA_OFFSET, Interpolators.LINEAR));


        // Animate green
        animators.add(getTranslationAnimatorX(mGreen, mRetractInterpolator,
                RETRACT_ANIMATION_DURATION));
        animators.add(getTranslationAnimatorY(mGreen, mRetractInterpolator,
                RETRACT_ANIMATION_DURATION));
        animators.add(getScaleAnimatorX(mGreen, 1.0f,
                RETRACT_ANIMATION_DURATION, mRetractInterpolator));
        animators.add(getScaleAnimatorY(mGreen, 1.0f,
                RETRACT_ANIMATION_DURATION, mRetractInterpolator));
        animators.add(getAlphaAnimator(mGreen, 0.0f, ALPHA_ANIMATION_LENGTH,
                RETRACT_ALPHA_OFFSET, Interpolators.LINEAR));

        // Animate yellow
        animators.add(getTranslationAnimatorX(mYellow, mRetractInterpolator,
                RETRACT_ANIMATION_DURATION));
        animators.add(getTranslationAnimatorY(mYellow, mRetractInterpolator,
                RETRACT_ANIMATION_DURATION));
        animators.add(getScaleAnimatorX(mYellow, 1.0f,
                RETRACT_ANIMATION_DURATION, mRetractInterpolator));
        animators.add(getScaleAnimatorY(mYellow, 1.0f,
                RETRACT_ANIMATION_DURATION, mRetractInterpolator));
        animators.add(getAlphaAnimator(mYellow, 0.0f, ALPHA_ANIMATION_LENGTH,
                RETRACT_ALPHA_OFFSET, Interpolators.LINEAR));

        // Animate home
        animators.add(getScaleAnimatorX(mWhite, 1.0f,
                RETRACT_ANIMATION_DURATION, mRetractInterpolator));
        animators.add(getScaleAnimatorY(mWhite, 1.0f,
                RETRACT_ANIMATION_DURATION, mRetractInterpolator));
        animators.add(getScaleAnimatorX(mWhiteCutout, 1.0f,
                RETRACT_ANIMATION_DURATION, INTERPOLATOR_40_OUT));
        animators.add(getScaleAnimatorY(mWhiteCutout, 1.0f,
                RETRACT_ANIMATION_DURATION, INTERPOLATOR_40_OUT));

        // Animate halo
        animators.add(getScaleAnimatorX(mHalo, 1.0f,
                RETRACT_ANIMATION_DURATION, mFastOutSlowInInterpolator));
        animators.add(getScaleAnimatorY(mHalo, 1.0f,
                RETRACT_ANIMATION_DURATION, mFastOutSlowInInterpolator));
        animators.add(getAlphaAnimator(mHalo, 1.0f,
                RETRACT_ANIMATION_DURATION, mFastOutSlowInInterpolator));

        getLongestAnim(animators).addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mCurrentAnimators.clear();
                skipToStartingValue();
            }
        });
        return animators;
    }

    private ArraySet<Animator> getCollapseAnimatorSet() {
        final ArraySet<Animator> animators = new ArraySet<>();

        // Animate red
        animators.add(mIsVertical
                ? getTranslationAnimatorY(mRed, INTERPOLATOR_40_OUT,
                      COLLAPSE_DURATION_RY)
                : getTranslationAnimatorX(mRed, INTERPOLATOR_40_OUT,
                      COLLAPSE_DURATION_RY));
        animators.add(getScaleAnimatorX(mRed, 1.0f, DOTS_RESIZE_DURATION,
                INTERPOLATOR_40_OUT));
        animators.add(getScaleAnimatorY(mRed, 1.0f, DOTS_RESIZE_DURATION,
                INTERPOLATOR_40_OUT));
        animators.add(getAlphaAnimator(mRed, 0.0f, ALPHA_ANIMATION_LENGTH,
                HOME_REAPPEAR_ANIMATION_OFFSET, Interpolators.LINEAR));

        // Animate blue
        animators.add(mIsVertical
                ? getTranslationAnimatorY(mBlue, INTERPOLATOR_40_OUT,
                      COLLAPSE_DURATION_BG)
                : getTranslationAnimatorX(mBlue, INTERPOLATOR_40_OUT,
                      COLLAPSE_DURATION_BG));
        animators.add(getScaleAnimatorX(mBlue, 1.0f, DOTS_RESIZE_DURATION,
                INTERPOLATOR_40_OUT));
        animators.add(getScaleAnimatorY(mBlue, 1.0f, DOTS_RESIZE_DURATION,
                INTERPOLATOR_40_OUT));
        animators.add(getAlphaAnimator(mBlue, 0.0f, ALPHA_ANIMATION_LENGTH,
                HOME_REAPPEAR_ANIMATION_OFFSET, Interpolators.LINEAR));

        // Animate yellow
        animators.add(mIsVertical
                ? getTranslationAnimatorY(mYellow, INTERPOLATOR_40_OUT,
                      COLLAPSE_DURATION_RY)
                : getTranslationAnimatorX(mYellow, INTERPOLATOR_40_OUT,
                      COLLAPSE_DURATION_RY));
        animators.add(getScaleAnimatorX(mYellow, 1.0f, DOTS_RESIZE_DURATION,
                INTERPOLATOR_40_OUT));
        animators.add(getScaleAnimatorY(mYellow, 1.0f, DOTS_RESIZE_DURATION,
                INTERPOLATOR_40_OUT));
        animators.add(getAlphaAnimator(mYellow, 0.0f, ALPHA_ANIMATION_LENGTH,
                HOME_REAPPEAR_ANIMATION_OFFSET, Interpolators.LINEAR));

        // Animate green
        animators.add(mIsVertical
                ? getTranslationAnimatorY(mGreen, INTERPOLATOR_40_OUT,
                      COLLAPSE_DURATION_BG)
                : getTranslationAnimatorX(mGreen, INTERPOLATOR_40_OUT,
                      COLLAPSE_DURATION_BG));
        animators.add(getScaleAnimatorX(mGreen, 1.0f, DOTS_RESIZE_DURATION,
                INTERPOLATOR_40_OUT));
        animators.add(getScaleAnimatorY(mGreen, 1.0f, DOTS_RESIZE_DURATION,
                INTERPOLATOR_40_OUT));
        animators.add(getAlphaAnimator(mGreen, 0.0f, ALPHA_ANIMATION_LENGTH,
                HOME_REAPPEAR_ANIMATION_OFFSET, Interpolators.LINEAR));

        // Animate home and halo reappearance after a delay.
        final Animator homeScaleX = getScaleAnimatorX(mWhite, 1.0f, HOME_REAPPEAR_DURATION,
                mFastOutSlowInInterpolator);
        final Animator homeScaleY = getScaleAnimatorY(mWhite, 1.0f, HOME_REAPPEAR_DURATION,
                mFastOutSlowInInterpolator);
        final Animator homeCutoutScaleX = getScaleAnimatorX(mWhiteCutout, 1.0f,
                HOME_REAPPEAR_DURATION, Interpolators.FAST_OUT_SLOW_IN);
        final Animator homeCutoutScaleY = getScaleAnimatorY(mWhiteCutout, 1.0f,
                HOME_REAPPEAR_DURATION, Interpolators.FAST_OUT_SLOW_IN);
        final Animator haloScaleX = getScaleAnimatorX(mHalo, 1.0f, HOME_REAPPEAR_DURATION,
                mFastOutSlowInInterpolator);
        final Animator haloScaleY = getScaleAnimatorY(mHalo, 1.0f, HOME_REAPPEAR_DURATION,
                mFastOutSlowInInterpolator);
        final Animator haloAlpha = getAlphaAnimator(mHalo, 1.0f, HOME_REAPPEAR_DURATION,
                mFastOutSlowInInterpolator);
        homeScaleX.setStartDelay(HOME_REAPPEAR_ANIMATION_OFFSET);
        homeScaleY.setStartDelay(HOME_REAPPEAR_ANIMATION_OFFSET);
        homeCutoutScaleX.setStartDelay(HOME_REAPPEAR_ANIMATION_OFFSET);
        homeCutoutScaleY.setStartDelay(HOME_REAPPEAR_ANIMATION_OFFSET);
        haloScaleX.setStartDelay(HOME_REAPPEAR_ANIMATION_OFFSET);
        haloScaleY.setStartDelay(HOME_REAPPEAR_ANIMATION_OFFSET);
        haloAlpha.setStartDelay(HOME_REAPPEAR_ANIMATION_OFFSET);
        animators.add(homeScaleX);
        animators.add(homeScaleY);
        animators.add(homeCutoutScaleX);
        animators.add(homeCutoutScaleY);
        animators.add(haloScaleX);
        animators.add(haloScaleY);
        animators.add(haloAlpha);

        getLongestAnim(animators).addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mCurrentAnimators.clear();
                skipToStartingValue();
            }
        });
        return animators;
    }

    private ArraySet<Animator> getLineAnimatorSet() {
        final ArraySet<Animator> animators = new ArraySet<>();
        if (mIsVertical) {
            // Animate red
            animators.add(getDeltaAnimatorY(mRed, mFastOutSlowInInterpolator,
                    getPxVal(R.dimen.opa_line_x_trans_ry), LINE_ANIMATION_DURATION_X));
            animators.add(getDeltaAnimatorX(mRed, mFastOutSlowInInterpolator,
                    getPxVal(R.dimen.opa_line_y_translation), LINE_ANIMATION_DURATION_Y));

            // Animate blue
            animators.add(getDeltaAnimatorY(mBlue, mFastOutSlowInInterpolator,
                    getPxVal(R.dimen.opa_line_x_trans_bg), LINE_ANIMATION_DURATION_X));

            //Animate yellow
            animators.add(getDeltaAnimatorY(mYellow, mFastOutSlowInInterpolator,
                    -getPxVal(R.dimen.opa_line_x_trans_ry), LINE_ANIMATION_DURATION_X));
            animators.add(getDeltaAnimatorX(mYellow, mFastOutSlowInInterpolator,
                    -getPxVal(R.dimen.opa_line_y_translation), LINE_ANIMATION_DURATION_Y));

            // Animate green
            animators.add(getDeltaAnimatorY(mGreen, mFastOutSlowInInterpolator,
                    -getPxVal(R.dimen.opa_line_x_trans_bg), LINE_ANIMATION_DURATION_X));
        } else {
            // Animate red
            animators.add(getDeltaAnimatorX(mRed, mFastOutSlowInInterpolator,
                    -getPxVal(R.dimen.opa_line_x_trans_ry), LINE_ANIMATION_DURATION_X));
            animators.add(getDeltaAnimatorY(mRed, mFastOutSlowInInterpolator,
                    getPxVal(R.dimen.opa_line_y_translation), LINE_ANIMATION_DURATION_Y));

            // Animate blue
            animators.add(getDeltaAnimatorX(mBlue, mFastOutSlowInInterpolator,
                    -getPxVal(R.dimen.opa_line_x_trans_bg), LINE_ANIMATION_DURATION_X));

            //Animate yellow
            animators.add(getDeltaAnimatorX(mYellow, mFastOutSlowInInterpolator,
                    getPxVal(R.dimen.opa_line_x_trans_ry), LINE_ANIMATION_DURATION_X));
            animators.add(getDeltaAnimatorY(mYellow, mFastOutSlowInInterpolator,
                    -getPxVal(R.dimen.opa_line_y_translation), LINE_ANIMATION_DURATION_Y));

            // Animate green
            animators.add(getDeltaAnimatorX(mGreen, mFastOutSlowInInterpolator,
                    getPxVal(R.dimen.opa_line_x_trans_bg), LINE_ANIMATION_DURATION_X));
        }

        // Animate home and halo
        animators.add(getScaleAnimatorX(mWhite, 0.0f, HOME_RESIZE_DURATION,
                mHomeDisappearInterpolator));
        animators.add(getScaleAnimatorY(mWhite, 0.0f, HOME_RESIZE_DURATION,
                mHomeDisappearInterpolator));
        animators.add(getScaleAnimatorX(mWhiteCutout, 0.0f, HOME_RESIZE_DURATION,
                HOME_DISAPPEAR_INTERPOLATOR));
        animators.add(getScaleAnimatorY(mWhiteCutout, 0.0f, HOME_RESIZE_DURATION,
                HOME_DISAPPEAR_INTERPOLATOR));
        animators.add(getScaleAnimatorX(mHalo, 0.0f, HOME_RESIZE_DURATION,
                mHomeDisappearInterpolator));
        animators.add(getScaleAnimatorY(mHalo, 0.0f, HOME_RESIZE_DURATION,
                mHomeDisappearInterpolator));

        getLongestAnim(animators).addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                startCollapseAnimation();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mCurrentAnimators.clear();
            }
        });
        return animators;
    }

    public boolean getOpaEnabled() {
        if (mOpaEnabledNeedsUpdate) {
            // Ping AssistManagerGoogle to get the latest OPA enablement state.
            ((AssistManagerGoogle) Dependency.get(AssistManager.class)).dispatchOpaEnabledState();
            if (mOpaEnabledNeedsUpdate) {
                Log.w(TAG, "mOpaEnabledNeedsUpdate not cleared by AssistManagerGoogle!");
            }
        }
        return mOpaEnabled;
    }

    public void setOpaEnabled(boolean enabled) {
        Log.i(TAG, "Setting opa enabled to " + enabled);
        mOpaEnabled = enabled;
        mOpaEnabledNeedsUpdate = false;
        updateOpaLayout();
    }

    public void updateOpaLayout() {
        // Remove halo over home button when quick step icons should be shown
        final boolean showQuickStepIcons = mOverviewProxyService.shouldShowSwipeUpUI();
        boolean haloShown = mOpaEnabled && !showQuickStepIcons;
        //modify EJSL-698 wushanfei start
        //mHalo.setVisibility(haloShown ? VISIBLE : INVISIBLE);
        mHalo.setVisibility(GONE);
        //modify EJSL-698 wushanfei end
        LayoutParams lp = (LayoutParams) mWhite.getLayoutParams();
        lp.width = showQuickStepIcons ? LayoutParams.MATCH_PARENT : mHaloDiameter;
        lp.height = showQuickStepIcons ? LayoutParams.MATCH_PARENT : mHaloDiameter;
        mWhite.setLayoutParams(lp);
        mWhiteCutout.setLayoutParams(lp);
    }

    /**
     * Cancels the current animation, stopping it in its tracks.
     */
    private void cancelCurrentAnimation() {
        if (!mCurrentAnimators.isEmpty()) {
            // Do not finish animation since we want to start retract from current state. We only
            // need to remove the listeners so the animation doesn't jump to the end.
            for (int i = mCurrentAnimators.size() - 1; i >= 0; i--) {
                final Animator a = mCurrentAnimators.valueAt(i);
                a.removeAllListeners();
                a.cancel();
            }
            mCurrentAnimators.clear();
            mAnimationState = ANIMATION_STATE_NONE;
        }
    }

    /**
     * Ends the current animation, winding it to the end values.
     */
    private void endCurrentAnimation() {
        if (!mCurrentAnimators.isEmpty()) {
            for (int i = mCurrentAnimators.size() - 1; i >= 0; i--) {
                final Animator a = mCurrentAnimators.valueAt(i);
                a.removeAllListeners();
                a.end();
            }
            mCurrentAnimators.clear();
        }
        mAnimationState = ANIMATION_STATE_NONE;
    }

    /**
     * @return the animator with the longest
     * {@link Animator#getTotalDuration} from the set
     */
    private Animator getLongestAnim(ArraySet<Animator> animators) {
        long longestDuration = Long.MIN_VALUE;
        Animator longestAnim = null;
        for (int i = animators.size() - 1; i >= 0; i--) {
            Animator a = animators.valueAt(i);
            if (a.getTotalDuration() > longestDuration) {
                longestAnim = a;
                longestDuration = a.getTotalDuration();
            }
        }
        return longestAnim;
    }

    private void setDotsVisible() {
        final int size = mAnimatedViews.size();
        View v;
        for (int i = 0; i < size; ++i) {
            v = mAnimatedViews.get(i);
            v.setAlpha(1.0f);
        }
    }

    /**
     * Forcibly moves views to their starting position and values.
     */
    private void skipToStartingValue() {
        final int size = mAnimatedViews.size();
        View v;
        for (int i = 0; i < size; ++i) {
            v = mAnimatedViews.get(i);
            v.setScaleY(1.0f);
            v.setScaleX(1.0f);
            v.setTranslationY(0);
            v.setTranslationX(0);
            v.setAlpha(0.0f);
        }

        mHalo.setAlpha(1.0f);
        mWhite.setAlpha(1.0f);
        mWhiteCutout.setAlpha(1.0f);

        mAnimationState = ANIMATION_STATE_NONE;
    }

    @Override
    public void setVertical(boolean vertical) {
        mIsVertical = vertical;
        mHome.setVertical(vertical);

        if (mIsVertical) {
            mTop = mGreen;
            mBottom = mBlue;
            mRight = mYellow;
            mLeft = mRed;
        } else {
            mTop = mRed;
            mBottom = mYellow;
            mLeft = mBlue;
            mRight = mGreen;
        }
    }

    @Override
    public void setDarkIntensity(float intensity) {
        if (mWhite.getDrawable() instanceof KeyButtonDrawable) {
            ((KeyButtonDrawable) mWhite.getDrawable()).setDarkIntensity(intensity);
        }
        ((KeyButtonDrawable) mHalo.getDrawable()).setDarkIntensity(intensity);

        // Since we reuse the same drawable for multiple views, we need to invalidate the view
        // manually.
        mWhite.invalidate();
        mHalo.invalidate();

        mHome.setDarkIntensity(intensity);
    }

    @Override
    public void setDelayTouchFeedback(boolean shouldDelay) {
        mHome.setDelayTouchFeedback(shouldDelay);
        mDelayTouchFeedback = shouldDelay;
    }
}
