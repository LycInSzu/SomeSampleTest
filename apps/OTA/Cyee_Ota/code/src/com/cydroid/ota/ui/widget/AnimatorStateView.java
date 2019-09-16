package com.cydroid.ota.ui.widget;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.RippleDrawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import com.cydroid.ota.Log;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.cydroid.ota.logic.config.EnvConfig;
import com.cydroid.ota.R;
import com.cydroid.ota.SettingUpdateApplication;
import com.cydroid.ota.SystemTheme;
import com.cydroid.ota.logic.IContextState;
import com.cydroid.ota.logic.State;

import java.util.HashMap;
import java.util.Map;

/**
 * @author borney
 *         Created by liuyanfeng on 15-6-2.
 */
public class AnimatorStateView extends LinearLayout implements ITheme, IStateView {
    private static final String TAG = "AnimatorStateView";
    private RectF mRect;
    private RectF mClipRect;
    private Paint mPaint;
    private Path mPath;
    private Resources mResources;
    private IContextState mContextState;
    private SystemTheme mSystemTheme;
    private float mPaintWidth;
    private boolean isBackShow = false;
    private Context mContext;
    private Map<State, Map<Boolean, String>> mStateTextMap = new HashMap<State, Map<Boolean, String>>();
    private ValueAnimator mInAnimator;
    private ValueAnimator mOutAnimator;
    private TextView mContainView;

    public AnimatorStateView(Context context) {
        this(context, null);
    }

    public AnimatorStateView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mSystemTheme = ((SettingUpdateApplication) context.getApplicationContext()).getSystemTheme();
        mSystemTheme.addTheme(this);
        mResources = context.getResources();
        mContainView = obtainView();
        addView(mContainView);
        initStateText();
        init();
        initData();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mRect = new RectF();
        mRect.left = mPaintWidth / 2;
        mRect.top = mPaintWidth / 2;
        mRect.bottom = getMeasuredHeight() - mPaintWidth / 2;
        mRect.right = getMeasuredWidth() - mPaintWidth / 2;

        mClipRect = new RectF();
        mClipRect.left = mRect.left + mPaintWidth / 2;
        mClipRect.right = mRect.right - mPaintWidth / 2;
        mClipRect.top = mRect.top + mPaintWidth / 2;
        mClipRect.bottom = mRect.bottom - mPaintWidth / 2;

        mPath.reset();
        mPath.addRoundRect(mClipRect, (getMeasuredHeight() - mPaintWidth) / 2,
                (getMeasuredHeight() - mPaintWidth) / 2, Path.Direction.CCW);

        mPaint.setColor(mContainView.getTextColors().getColorForState(getDrawableState(), 0));
    }

    public void setBackShow(boolean isBackShow) {
        this.isBackShow = isBackShow;
        setShowColor();
        invalidate();
    }

    public void setTextColor(int color) {
        boolean isClickable = isClickable();
        int packColor = SimilarColor.pack8888(SimilarColor.getR32(color), SimilarColor.getG32(color), SimilarColor.getB32(color), SimilarColor.getA32(color) & 0X55);
        mPaint.setColor(isClickable ? color : packColor);
        mContainView.setTextColor(isClickable ? color : packColor);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float rx = getHeight() / 2f;
        if (mRect != null) {
            canvas.drawRoundRect(mRect, rx, rx, mPaint);
        }
    }

    @Override
    public void onChameleonChanged(Chameleon chameleon) {
    }

    @Override
    public void changeState(final IContextState contextState) {
        Log.d(TAG, "changeState() state = " + contextState);
        mContextState = contextState;
        boolean clickable = true;
        switch (mContextState.state()) {
            case INITIAL:
                break;
            case CHECKING:
                clickable = false;
                break;
            case READY_TO_DOWNLOAD:
                break;
            case DOWNLOADING:
            case DOWNLOAD_INTERRUPT:
            case DOWNLOAD_PAUSE:
                break;
            case DOWNLOAD_PAUSEING:
                clickable = false;
                break;
            case DOWNLOAD_COMPLETE:
                clickable = false;
                break;
            case DOWNLOAD_VERIFY:
                break;
            case INSTALLING:
                break;
        }
        setClickable(clickable);
        final String changeText = mStateTextMap.get(contextState.state()).get(
                Boolean.valueOf(contextState.isRoot() || EnvConfig.isTestRoot()));
        Log.d(TAG, "changeText = " + changeText);
        setChangeAnimator(changeText);
        setShowColor();
    }

    private void setShowColor() {
        int textColor = getResources().getColor(R.color.gn_su_layout_main_statebutton_textColor);
        int rippleColor = mSystemTheme.getChameleon().AppbarColor_A1;
        if (isBackShow) {
            textColor = mSystemTheme.getChameleon().AppbarColor_A1;
            rippleColor = getResources().getColor(R.color.gn_su_layout_main_statebutton_textColor);
        }
        setTextColor(textColor);
        setBackground(getRippleDrawable(isBackShow ? rippleColor : SimilarColor.getSimilarColor(rippleColor), textColor));
    }

    private void setChangeAnimator(final String changeText) {
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
                mContainView.setText(changeText);
                mOutAnimator.removeAllListeners();
                mInAnimator.start();
            }
        });
        String text = mContainView.getText().toString();
        if (!TextUtils.isEmpty(text)) {
            if (!text.equals(changeText)) {
                mOutAnimator.start();
            }
        } else {
            mContainView.setText(changeText);
        }
    }

    public IContextState getShowState() {
        return mContextState;
    }

    @Override
    public void onDestory() {
        mInAnimator.removeAllListeners();
        mOutAnimator.removeAllListeners();
    }

    private void initData() {
        mInAnimator = (ValueAnimator) AnimatorInflater.loadAnimator(getContext(), R.animator.gn_su_animator_statebutton_in);
        mInAnimator.setTarget(mContainView);
        mOutAnimator = (ValueAnimator) AnimatorInflater.loadAnimator(getContext(), R.animator.gn_su_animator_statebutton_out);
        mOutAnimator.setTarget(mContainView);
    }

    private void init() {
        mPaintWidth = mResources
                .getDimension(R.dimen.gn_su_layout_main_statebutton_border_width) / mResources.getDisplayMetrics().density;
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mPaintWidth);
        mPaint.setColor(mResources.getColor(R.color.gn_su_layout_main_statebutton_textColor));
        mPath = new Path();
    }

    private void initStateText() {
        Map<Boolean, String> initial = new HashMap<Boolean, String>();
        initial.put(Boolean.TRUE, mResources.getString(R.string.gn_su_layout_main_statebutton_checknew));
        initial.put(Boolean.FALSE, mResources.getString(R.string.gn_su_layout_main_statebutton_checknew));
        mStateTextMap.put(State.INITIAL, initial);

        Map<Boolean, String> checking = new HashMap<Boolean, String>();
        checking.put(Boolean.TRUE, mResources.getString(R.string.gn_su_layout_main_statebutton_checknew));
        checking.put(Boolean.FALSE, mResources.getString(R.string.gn_su_layout_main_statebutton_checknew));
        mStateTextMap.put(State.CHECKING, checking);

        Map<Boolean, String> readyToDownload = new HashMap<Boolean, String>();
        readyToDownload.put(Boolean.TRUE, mResources.getString(R.string.gn_su_layout_main_statebutton_readydownload_recover));
        readyToDownload.put(Boolean.FALSE, mResources.getString(R.string.gn_su_layout_main_statebutton_readydownload));
        mStateTextMap.put(State.READY_TO_DOWNLOAD, readyToDownload);

        Map<Boolean, String> downloading = new HashMap<Boolean, String>();
        downloading.put(Boolean.TRUE, mResources.getString(R.string.gn_su_layout_main_statebutton_pause));
        downloading.put(Boolean.FALSE, mResources.getString(R.string.gn_su_layout_main_statebutton_pause));
        mStateTextMap.put(State.DOWNLOADING, downloading);

        Map<Boolean, String> pause = new HashMap<Boolean, String>();
        pause.put(Boolean.TRUE, mResources.getString(R.string.gn_su_layout_main_statebutton_continue));
        pause.put(Boolean.FALSE, mResources.getString(R.string.gn_su_layout_main_statebutton_continue));
        mStateTextMap.put(State.DOWNLOAD_PAUSE, pause);

        Map<Boolean, String> pauseing = new HashMap<Boolean, String>();
        pauseing.put(Boolean.TRUE, mResources.getString(R.string.gn_su_layout_main_statebutton_pause));
        pauseing.put(Boolean.FALSE, mResources.getString(R.string.gn_su_layout_main_statebutton_pause));
        mStateTextMap.put(State.DOWNLOAD_PAUSEING, pauseing);

        Map<Boolean, String> interrupt = new HashMap<Boolean, String>();
        interrupt.put(Boolean.TRUE, mResources.getString(R.string.gn_su_layout_main_statebutton_pause));
        interrupt.put(Boolean.FALSE, mResources.getString(R.string.gn_su_layout_main_statebutton_pause));
        mStateTextMap.put(State.DOWNLOAD_INTERRUPT, interrupt);

        Map<Boolean, String> complete = new HashMap<Boolean, String>();
        complete.put(Boolean.TRUE, mResources.getString(R.string.gn_su_layout_main_statebutton_pause));
        complete.put(Boolean.FALSE, mResources.getString(R.string.gn_su_layout_main_statebutton_pause));
        mStateTextMap.put(State.DOWNLOAD_COMPLETE, complete);

        Map<Boolean, String> verify = new HashMap<Boolean, String>();
        verify.put(Boolean.TRUE, mResources.getString(R.string.gn_su_layout_main_statebutton_restartrecovery));
        verify.put(Boolean.FALSE, mResources.getString(R.string.gn_su_layout_main_statebutton_download_complete));
        mStateTextMap.put(State.DOWNLOAD_VERIFY, verify);

        Map<Boolean, String> installing = new HashMap<Boolean, String>();
        installing.put(Boolean.TRUE, mResources.getString(R.string.gn_su_layout_main_statebutton_recoverying));
        installing.put(Boolean.FALSE, mResources.getString(R.string.gn_su_layout_main_statebutton_installing));
        mStateTextMap.put(State.INSTALLING, installing);
        Log.d(TAG, "initStateText() " + mStateTextMap);
    }

    private TextView obtainView() {
        TextView textView = new TextView(mContext);
        LinearLayout.LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        textView.setLayoutParams(params);
        textView.setGravity(Gravity.CENTER);
        Resources res = getResources();
        textView.setTextColor(res.getColor(R.color.gn_su_layout_main_statebutton_textColor));
        float scaleRatio = res.getDisplayMetrics().density;
        float dimenPix = res.getDimension(R.dimen.gn_su_layout_main_statebutton_textSize);
        textView.setTextSize(dimenPix / scaleRatio);
        textView.setEnabled(false);
        textView.setClickable(false);
        return textView;
    }

    private RippleDrawable getRippleDrawable(int normalColor, int pressedColor) {
        return new RippleDrawable(getPressedColorSelector(normalColor, pressedColor), getColorDrawableFromColor(normalColor), null) {
            @Override
            public void draw(Canvas canvas) {
                canvas.save();
                canvas.clipPath(mPath);
                super.draw(canvas);
                canvas.restore();
            }
        };
    }

    private ColorStateList getPressedColorSelector(int normalColor, int pressedColor) {
        return new ColorStateList(
                new int[][]{
                        new int[]{android.R.attr.state_pressed},
                        new int[]{android.R.attr.state_focused},
                        new int[]{android.R.attr.state_activated},
                        new int[]{}
                },
                new int[]{
                        pressedColor,
                        pressedColor,
                        pressedColor,
                        normalColor
                }
        );
    }

    private ColorDrawable getColorDrawableFromColor(int color) {
        return new ColorDrawable(color);
    }
}
