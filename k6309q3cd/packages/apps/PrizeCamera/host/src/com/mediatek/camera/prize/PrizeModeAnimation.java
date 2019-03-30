package com.mediatek.camera.prize;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.mediatek.camera.common.IAppUiListener;
import com.mediatek.camera.common.app.IApp;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.R;
import com.mediatek.camera.common.utils.Size;
import com.mediatek.camera.ui.prize.BlurPic;


/**
 * Created by huangpengfei on 2019/1/27.
 */
public class PrizeModeAnimation {

    private static final LogUtil.Tag TAG = new LogUtil.Tag(
            PrizeModeAnimation.class.getSimpleName());

    private Activity mActivity;
    private Bitmap mCoverBitmap;
    private ImageView mCoverImageView;
    private boolean mIsCoverAnimationRuninng;
    private final View mAiPortraitTextContainer;
    private final TextView mSceneTitle;
    private boolean mSwitchAnim = false;
    private final View mBlackCover;
    private int mPreviewTop = 0;
    private int mPreviewBottom = 0;
    private int mInitialHeight;
    private int mScreenWidth;
    private int mScreenheight;
    private Bitmap mCaptureBitmap;

    public PrizeModeAnimation(IApp app) {
        this.mActivity = app.getActivity();

        DisplayMetrics metric = new DisplayMetrics();
        app.getActivity().getWindowManager().getDefaultDisplay().getRealMetrics(metric);
        mScreenWidth = metric.widthPixels;
        mScreenheight = metric.heightPixels;

        Bitmap bitmap = drawableToBitmap(mActivity.getDrawable(R.drawable.modepicker_long));
        mCoverImageView = (ImageView) (app.getActivity().findViewById(R.id.sf_screenshot));
        mBlackCover = app.getActivity().findViewById(R.id.preview_cover);
        mAiPortraitTextContainer = app.getActivity().findViewById(R.id.ai_scene_text_container);
        mSceneTitle = (TextView) app.getActivity().findViewById(R.id.ai_scene_mode_title);
        mCoverBitmap = BlurPic.blurScale(bitmap);
        mCoverImageView.setImageBitmap(mCoverBitmap);

        mCoverImageView.setAlpha(0.0f);
        /*app.getAppUi().registerOnPreviewAreaChangedListener(new IAppUiListener.OnPreviewAreaChangedListener() {
            @Override
            public void onPreviewAreaChanged(RectF newPreviewArea, Size previewSize) {
                mPreviewTop = (int) newPreviewArea.top;
                if (mPreviewTop < 0){
                    mPreviewTop = 0;
                }
                mPreviewBottom = (int) newPreviewArea.bottom;
                LogHelper.d(TAG, "[onPreviewAreaChanged]  mPreviewTop = " + mPreviewTop + "  mPreviewBottom = " + mPreviewBottom);
                if (mInitialHeight == 0) {
                    mInitialHeight = mCoverImageView.getMeasuredHeight();
                    FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mCoverImageView.getLayoutParams();
                    lp.width = mScreenWidth;
                    lp.height = mPreviewBottom - mPreviewTop;
                    lp.topMargin = 0;
                    lp.bottomMargin = mInitialHeight - mPreviewBottom;
                    mCoverImageView.setLayoutParams(lp);
                    LogHelper.d(TAG, "[onPreviewAreaChanged]  mInitialHeight = " + mInitialHeight);
                }

            }
        });*/
    }

    public void showModeCoverAnimation(boolean needShowAiSceneText) {
        LogHelper.d(TAG, "[showModeCoverAnimation]  needShowAiSceneText = " + needShowAiSceneText);
        if (mIsCoverAnimationRuninng) return;
        if (mCoverImageView == null) {
            LogHelper.d(TAG, "[showModeCoverAnimation] mCoverImageView == null return");
            return;
        }
        Animation animationCover = AnimationUtils.loadAnimation(mActivity, R.anim.prize_anim_mode_change);
        animationCover.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
                LogHelper.i("", "[showModeCoverAnimation] onAnimationStart");
                mCoverImageView.setAlpha(1.0f);
                mIsCoverAnimationRuninng = true;
                if (!needShowAiSceneText) {
                    mAiPortraitTextContainer.setVisibility(View.GONE);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                LogHelper.d(TAG, "[showModeCoverAnimation] onAnimationEnd");
                mIsCoverAnimationRuninng = false;
                mCoverImageView.setAlpha(0.0f);
                if (needShowAiSceneText) {
                    showSceneModeTextAnimation();
                }
            }
        });
        mCoverImageView.startAnimation(animationCover);

    }

    public void showSceneModeTextAnimation() {
        if (mAiPortraitTextContainer == null) return;
        Animation sceneModeChange = AnimationUtils.loadAnimation(mActivity, R.anim.prize_anim_ai_scene_change);
        mAiPortraitTextContainer.setVisibility(View.VISIBLE);
        mAiPortraitTextContainer.startAnimation(sceneModeChange);
    }


    public void hideModeCoverAnimation() {
        LogHelper.d(TAG, "[hideModeAnimation]");
        if (mCoverImageView == null || mSwitchAnim) {
            LogHelper.d(TAG, "[]hideModeAnimation] mCoverImageView == null return");
            return;
        }
        Animation mAnimation = null;

        if (mAnimation == null) {
            mAnimation = AnimationUtils.loadAnimation(mActivity, R.anim.surfacecover_hide);
        }
        mAnimation.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
                LogHelper.d(TAG, "[hideModeAnimation] onAnimationStart");
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                LogHelper.d(TAG, "[hideModeAnimation] onAnimationEnd");
                mCoverImageView.setAlpha(0.0f);

            }
        });

        if (mCoverImageView != null) {
            mCoverImageView.startAnimation(mAnimation);
        }
    }

    public void showSwitchCameraAnimation1() {
        if (mCoverImageView != null) {
            mCoverImageView.setAlpha(1.0f);
        }
        AnimatorSet animatorSet = new AnimatorSet();
        AnimatorSet animatorSetAlpha = new AnimatorSet();
        animatorSet.setDuration(350);
        ObjectAnimator rotation = ObjectAnimator.ofFloat(mCoverImageView, "rotationY", 0, 180);
        ObjectAnimator scaleX1 = ObjectAnimator.ofFloat(mCoverImageView, "scaleX", 1, 0.85f);
        ObjectAnimator scaleY1 = ObjectAnimator.ofFloat(mCoverImageView, "scaleY", 1, 0.85f);
        ObjectAnimator scaleX2 = ObjectAnimator.ofFloat(mCoverImageView, "scaleX", 0.85f, 1);
        ObjectAnimator scaleY2 = ObjectAnimator.ofFloat(mCoverImageView, "scaleY", 0.85f, 1);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(mCoverImageView, "alpha", 1, 0.0f);
        //alpha.setRepeatCount(-1);
        //alpha.setRepeatMode(ObjectAnimator.RESTART);
        //alpha.setStartDelay(1000);
        //alpha.setInterpolator(new AccelerateInterpolator());
        animatorSet.play(rotation).before(scaleX2).before(scaleY2).after(scaleX1).after(scaleY1);
        animatorSetAlpha.play(animatorSet).before(alpha);
        mBlackCover.setVisibility(View.VISIBLE);
        animatorSetAlpha.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                mSwitchAnim = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mSwitchAnim = false;
                mBlackCover.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mSwitchAnim = false;
                mBlackCover.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                mSwitchAnim = false;
                mBlackCover.setVisibility(View.GONE);
            }
        });
        animatorSetAlpha.start();
    }

    public void showScalAnimation() {
        float pivotYValue = ((mPreviewTop + mPreviewBottom + 110) / 2) / (float) mScreenheight;
        float measuredHeight = mCoverImageView.getMeasuredHeight();
        float newPreviewHeight = mPreviewBottom - mPreviewTop;
        LogHelper.d(TAG, "[showScalAnimation] measuredHeight = " + measuredHeight + "  newPreviewHeight = " + newPreviewHeight + " mScreenheight = " + mScreenheight);
        float toY = newPreviewHeight / measuredHeight;
        LogHelper.d(TAG, "[showScalAnimation] toY = " + toY + "  pivotYValue = " + pivotYValue);
        ScaleAnimation mScaleAnimation = new ScaleAnimation(1.0f, 1.0f, 1.0f, toY,
                Animation.ABSOLUTE, 0.5f, Animation.ABSOLUTE, 0.15f * newPreviewHeight);
        mScaleAnimation.setDuration(200);
        mScaleAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
        mScaleAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                LogHelper.d(TAG, "[mScaleAnimation] onAnimationEnd  mPreviewBottom = " + mPreviewBottom
                        + "  mPreviewTop = " + mPreviewTop + "  mInitialHeight = " + mInitialHeight);
                hideModeCoverAnimation();
                mCoverImageView.setAlpha(0.0f);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mCoverImageView.startAnimation(mScaleAnimation);
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = Bitmap.createBitmap(
                drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight()/*1340*/,
                drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return bitmap;

    }

    public void hideSceneText() {
        if (mAiPortraitTextContainer != null) {
            mAiPortraitTextContainer.setVisibility(View.GONE);
        }
    }

    public void updateSceneModeTitle(int title) {
        if (mSceneTitle != null) {
            mSceneTitle.setText(title);
        }
    }

    public void showCover() {
        LogHelper.d(TAG, "[showCover]");
        if (mCoverImageView != null) {
            mCoverImageView.setAlpha(1.0f);
        }
    }

    public void hideCover() {
        LogHelper.d(TAG, "[hideCover]");
        if (mCoverImageView != null) {
            mCoverImageView.setAlpha(0.0f);
        }
    }

}
