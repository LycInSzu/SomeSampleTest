package com.cydroid.ota.ui.widget;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cydroid.ota.Log;
import com.cydroid.ota.SettingUpdateApplication;
import com.cydroid.ota.SystemTheme;
import com.cydroid.ota.logic.IContextState;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.cydroid.ota.R;

/**
 * @author borney
 *         Created by borney on 6/5/15.
 */
public class AnimatorMainBgView extends RelativeLayout implements ITheme, IStateView {
    private static final String TAG = "AnimatorMainBgView";
    private AnimationBgDrawable mBackground;
    private AnimatorReadyView mAnimatorReadyView;
    private AnimatorLogoView mAnimtorLogoView;
    private AnimatorReadyView mAnimtorReadyView;
    private AnimatorStateLayout mStateLayout;
    private AnimatorMiddleView mAnimatorMiddleView;
    private ExpendTextView mExpendTextView;
    private TextView mCurVersionView;
    private View mMsgBgView;
    private int mStateButtonIndex = -1;
    private ValueAnimator mStateButtonAlphaAnimator;
    private ValueAnimator mAnimatorUp;
    private SystemTheme mSystemTheme;
    private IContextState mContextState, mLastContextState;
    private Context mContext;

    public AnimatorMainBgView(Context context) {
        this(context, null);
    }

    public AnimatorMainBgView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AnimatorMainBgView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        mSystemTheme = ((SettingUpdateApplication) context.getApplicationContext()).getSystemTheme();
        Resources resources = getResources();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        BitmapFactory.Options options = new BitmapFactory.Options();
        mStateButtonAlphaAnimator = (ValueAnimator) AnimatorInflater.loadAnimator(context, R.animator.gn_su_animator_statebutton_alpha);
        options.inDensity = displayMetrics.densityDpi;
        View view = ((Activity) context).getWindow().getDecorView();
        mBackground = new AnimationBgDrawable(view, context);
        mSystemTheme.addTheme(mBackground);
        mBackground.start();
        view.setBackground(mBackground);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mMsgBgView = getView(R.id.gn_su_layout_main_msg_bg);
        mAnimatorUp = (ValueAnimator) AnimatorInflater.loadAnimator(mContext,
                R.animator.gn_su_animator_up);
        mAnimatorUp.setTarget(mMsgBgView);
        mCurVersionView = getView(R.id.gn_su_layout_main_versionview);
        mAnimatorMiddleView = getView(R.id.gn_su_layout_main_middlemsgview);
        mStateLayout = getView(R.id.gn_su_layout_main_statelayout);
        mExpendTextView = getView(R.id.gn_su_layout_main_expend_text);
        mAnimtorLogoView = getView(R.id.gn_su_layout_main_animtorlogoview);
        mAnimtorReadyView = getView(R.id.gn_su_layout_animtorredyview);
        mStateButtonAlphaAnimator.setTarget(mStateLayout);
        mAnimatorReadyView = getView(R.id.gn_su_layout_animtorredyview);
        mAnimatorReadyView.setAnimEndListener(new AnimatorReadyView.AnimtorEndListener() {
            @Override
            public void onAnimEnd(int direction) {
                if (direction == AnimatorReadyView.AnimtorEndListener.DIRECTION_UP) {
                    mStateLayout.setVisibility(VISIBLE);
                    bringChildToIndex(mStateLayout, getChildCount() - 1);
                    if (mLastContextState != null) {
                        mStateButtonAlphaAnimator.start();
                    } else {
                        mStateButtonAlphaAnimator.end();
                    }
                }
            }
        });
        mStateButtonIndex = indexOfChild(mStateLayout);
    }

    @Override
    public void changeState(IContextState contextState) {
        mLastContextState = mContextState;
        mContextState = contextState;
        mCurVersionView.setVisibility(GONE);
        switch (mContextState.state()) {
            case INITIAL:
                mMsgBgView.setVisibility(VISIBLE);
                mCurVersionView.setVisibility(VISIBLE);
                mStateLayout.setBackShow(false);
                if (mContextState.isBackState()) {
                    mAnimatorUp.removeAllListeners();
                    mAnimatorUp.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            mStateLayout.setVisibility(INVISIBLE);
                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mStateLayout.setBackShow(true);
                            mStateLayout.setVisibility(VISIBLE);
                            mStateButtonAlphaAnimator.start();
                        }
                    });
                    Log.d(TAG, "------------AnimatorUp-start------------");
                    mAnimatorUp.start();
                }
                break;
            case CHECKING:
                mMsgBgView.setVisibility(INVISIBLE);
                mStateLayout.setBackShow(false);
                break;
            case READY_TO_DOWNLOAD:
                mStateLayout.setBackShow(true);
                mStateLayout.setVisibility(INVISIBLE);
                break;
            case DOWNLOADING:
                mStateLayout.setBackShow(false);
                bringChildToIndex(mStateLayout, mStateButtonIndex);
                break;
            case DOWNLOAD_INTERRUPT:
            case DOWNLOAD_PAUSE:
            case DOWNLOAD_PAUSEING:
            case DOWNLOAD_COMPLETE:
            case DOWNLOAD_VERIFY:
                break;
            case INSTALLING:
                mStateLayout.setBackShow(false);
                break;
            default:
                break;
        }
        mStateLayout.changeState(mContextState);
        mAnimatorMiddleView.changeState(mContextState);
        mAnimtorLogoView.changeState(mContextState);
        mAnimtorReadyView.changeState(mContextState);
        mExpendTextView.changeState(mContextState);
    }

    @Override
    public void onDestory() {
        mBackground.onDestory();
        mAnimatorMiddleView.onDestory();
        mAnimtorLogoView.onDestory();
        mExpendTextView.onDestory();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    @Override
    public void onChameleonChanged(Chameleon chameleon) {
    }

    private <T extends View> T getView(int id) {
        return (T) findViewById(id);
    }

    public void bringChildToIndex(View child, int index) {
        int childIndex = indexOfChild(child);
        if (childIndex >= 0) {
            Method removeFromArray = getMethod(((Object) this).getClass(), "removeFromArray", int.class);
            Method addInArray = getMethod(((Object) this).getClass(), "addInArray", View.class, int.class);
            Field childParent = getField(child.getClass(), "mParent");
            try {
                removeFromArray.invoke(this, childIndex);
                addInArray.invoke(this, child, index);
                childParent.set(child, this);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
            requestLayout();
            invalidate();
        }


    }

    private Method getMethod(Class objectClass, String name, Class<?>... parameterTypes) {
        try {
            Method declaredMethod = objectClass.getDeclaredMethod(name, parameterTypes);
            declaredMethod.setAccessible(true);
            return declaredMethod;
        } catch (NoSuchMethodException e) {
            Class superclass = objectClass.getSuperclass();
            if (superclass != null) {
                return getMethod(superclass, name, parameterTypes);
            } else {
                e.printStackTrace();
            }
        }
        return null;
    }

    private Field getField(Class objectClass, String name) {
        try {
            Field declaredField = objectClass.getDeclaredField(name);
            declaredField.setAccessible(true);
            return declaredField;
        } catch (NoSuchFieldException e) {
            Class superClass = objectClass.getSuperclass();
            if (superClass == null) {
                return null;
            } else {
                return getField(superClass, name);
            }
        }
    }
}
