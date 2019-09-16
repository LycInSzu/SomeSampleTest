package com.cydroid.ota.ui.widget;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.text.method.ScrollingMovementMethod;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.cydroid.ota.SettingUpdateApplication;
import com.cydroid.ota.SystemTheme;
import com.cydroid.ota.logic.IContextState;
import com.cydroid.ota.logic.State;
import com.cydroid.ota.Log;
import com.cydroid.ota.R;

import java.lang.reflect.Method;

/**
 * @author borney
 *         Created by borney on 6/4/15.
 */
public class AnimatorReadyView extends RelativeLayout implements IStateView, ITheme {
    private static final String TAG = "AnimatorReadyView";
    private IContextState mContextState, mLastContextState;
    private ValueAnimator mAnimatorUp;
    private ValueAnimator mAnimatorAplha;
    private View mAlphaAreaView;
    private TextView mIntroductionView;
    private TextView mGotoIntroductionView;
    private SystemTheme mSystemTheme;
    private Context mContext;
    private Resources mResources;
    private AnimtorEndListener mAnimtorEndListener;

    public AnimatorReadyView(Context context) {
        this(context, null);
    }

    public AnimatorReadyView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AnimatorReadyView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        mResources = context.getResources();
        mSystemTheme = ((SettingUpdateApplication) context.getApplicationContext()).getSystemTheme();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        Log.d(TAG, "onFinishInflate");
        mAlphaAreaView = getView(R.id.gn_su_layout_animtorredyalpha);
        mAnimatorUp = (ValueAnimator) AnimatorInflater.loadAnimator(mContext,
                R.animator.gn_su_animator_up);
        mAnimatorUp.setTarget(this);
        mAnimatorAplha = (ValueAnimator) AnimatorInflater.loadAnimator(mContext,
                R.animator.gn_su_animator_readyviewalpha);
        mAnimatorAplha.setTarget(mAlphaAreaView);
        mIntroductionView = getView(R.id.gn_su_layout_ready_introduction);
        mIntroductionView.setMovementMethod(new ScrollingMovementMethod());
        mGotoIntroductionView = getView(R.id.gn_su_layout_ready_gotointroduction);
        mGotoIntroductionView.setTextColor(mSystemTheme.getChameleon().AppbarColor_A1);
        boolean showNavigationBar = isShowNavigationBar();
        Log.d(TAG, "isShowNavigationBar = " + showNavigationBar);
        float readyViewHeight = mResources.getDimension(R.dimen.gn_su_layout_main_animtorredyview_height);
        float introductionViewMaxheight = mResources.getDimension(R.dimen.gn_su_layout_ready_introduction_height);
        float navigationBarHeight = getNavigationBarHeight();
        readyViewHeight = showNavigationBar ? (readyViewHeight - navigationBarHeight) : readyViewHeight;
        introductionViewMaxheight = showNavigationBar ? (introductionViewMaxheight - navigationBarHeight) : introductionViewMaxheight;
        setTranslationY(readyViewHeight);
        RelativeLayout.LayoutParams readyViewParams = (LayoutParams) getLayoutParams();
        readyViewParams.height = (int) readyViewHeight;
        setLayoutParams(readyViewParams);
        mIntroductionView.setMaxHeight((int) introductionViewMaxheight);
    }

    public void setAnimEndListener(AnimtorEndListener listener) {
        Log.d(TAG, "setAnimEndListener = " + listener);
        mAnimtorEndListener = listener;
    }

    @Override
    public void onChameleonChanged(Chameleon chameleon) {

    }

    @Override
    public void changeState(IContextState contextState) {
        Log.debug(TAG, "changeState = " + contextState);
        mLastContextState = mContextState;
        mContextState = contextState;
        switch (mContextState.state()) {
            case INITIAL:
                break;
            case CHECKING:
                break;
            case READY_TO_DOWNLOAD:
                startReadyDownloadAnimation();
                break;
            case DOWNLOADING:
                if (mLastContextState != null && mLastContextState.state() == State.READY_TO_DOWNLOAD) {
                    startDownloadingAnimation();
                }
                break;
            case DOWNLOAD_INTERRUPT:
            case DOWNLOAD_PAUSE:
            case DOWNLOAD_PAUSEING:
            case DOWNLOAD_COMPLETE:
            case DOWNLOAD_VERIFY:
                break;
            case INSTALLING:
                break;
        }
    }

    @Override
    public void onDestory() {

    }

    private boolean isShowNavigationBar() {
        boolean hasNavigationBar = false;
        int id = mResources.getIdentifier("config_showNavigationBar", "bool", "android");
        if (id > 0) {
            hasNavigationBar = mResources.getBoolean(id);
        }
        try {
            Class systemPropertiesClass = Class.forName("android.os.SystemProperties");
            Method m = systemPropertiesClass.getMethod("get", String.class);
            String navBarOverride = (String) m.invoke(systemPropertiesClass, "qemu.hw.mainkeys");
            if ("1".equals(navBarOverride)) {
                hasNavigationBar = false;
            } else if ("0".equals(navBarOverride)) {
                hasNavigationBar = true;
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }

        return hasNavigationBar;
    }

    private float getNavigationBarHeight() {
        int resourceId = mResources.getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return mResources.getDimensionPixelSize(resourceId);
        }
        return 0;
    }

    private void startReadyDownloadAnimation() {
        mAnimatorUp.removeAllListeners();
        mAnimatorUp.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                Log.d(TAG, "startReadyDownloadAnimation");
                if (mAnimtorEndListener != null) {
                    mAnimtorEndListener.onAnimEnd(AnimtorEndListener.DIRECTION_UP);
                }
            }
        });
        if (mLastContextState != null) {
            mAnimatorUp.start();
            mAnimatorAplha.start();
        } else {
            mAnimatorUp.end();
            mAnimatorAplha.end();
        }
    }

    private void startDownloadingAnimation() {
        mAnimatorUp.reverse();
        mAnimatorUp.removeAllListeners();
        mAnimatorUp.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (mAnimtorEndListener != null) {
                    mAnimtorEndListener.onAnimEnd(AnimtorEndListener.DIRECTION_DOWN);
                }
            }
        });
    }

    private <T extends View> T getView(int id) {
        return (T) findViewById(id);
    }

    public interface AnimtorEndListener {
        int DIRECTION_UP = 0;
        int DIRECTION_DOWN = 1;

        void onAnimEnd(int direction);
    }
}
