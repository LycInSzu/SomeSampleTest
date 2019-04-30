package com.mediatek.camera.feature.setting.flash;
/***
 * Created by haungpengfei on 2018-10-08
 */

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.Scroller;

import com.mediatek.camera.CameraActivity;
import com.mediatek.camera.R;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;


public class PrizeFlashMenuContainer extends LinearLayout {

    private static final LogUtil.Tag TAG =
            new LogUtil.Tag(PrizeFlashMenuContainer.class.getSimpleName());
    private int mMeasuredWidth;
    private int mMeasuredHeight;
    private Scroller mScroller;
    private static int DURATION = 200;
    private boolean mIsHide = true;
    private View menu;

    public PrizeFlashMenuContainer(Context context) {
        super(context);
    }

    public PrizeFlashMenuContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        mScroller = new Scroller(context,new DecelerateInterpolator());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec,heightMeasureSpec);
        mMeasuredWidth = getMeasuredWidth();
        mMeasuredHeight = getMeasuredHeight();
        LogHelper.d(TAG,"[onMeasure]  width = "+ mMeasuredWidth +"  height = "+ mMeasuredHeight+"   this = "+this );

    }

    public void init(View view){
        menu = view;
    }
    public void show(){
        boolean haveOffset = mScroller.computeScrollOffset();
        if (haveOffset){
            return;
        }
        menu.setVisibility(View.VISIBLE);
        mIsHide = false;
        if(isLayoutRtl()){
            /*prize-modify-adapt ui of RTL-xiaoping-20181228-start*/
//            mScroller.startScroll(0, 0, -getMeasuredWidth(), 0, DURATION);
            mScroller.startScroll(-menu.getWidth(), 0, menu.getWidth(), 0, DURATION);
            /*prize-modify-adapt ui of RTL-xiaoping-20181228-end*/
        }else{
            mScroller.startScroll(menu.getWidth(), 0, -menu.getWidth(), 0, DURATION);
            LogHelper.d(TAG, "[show] menu.getWidth() = "+ menu.getWidth()+"   this = "+this );
        }
        if (mOnHideAnimationListener != null){
            mOnHideAnimationListener.onShowAnimationStart();
        }
        invalidate();
    }

    public void hide(){
        boolean haveOffset = mScroller.computeScrollOffset();
        if (haveOffset){
            return;
        }
        mIsHide = true;
        if(isLayoutRtl()){
            /*prize-modify-adapt ui of RTL-xiaoping-20181228-start*/
//            mScroller.startScroll(-getMeasuredWidth(), 0, getMeasuredWidth(), 0, DURATION);
            mScroller.startScroll(0, 0, -menu.getWidth(), 0, DURATION);
            /*prize-modify-adapt ui of RTL-xiaoping-20181228-end*/
        }else{
            mScroller.startScroll(0, 0, menu.getWidth(), 0, DURATION);
            LogHelper.d(TAG, "[hide] menu.getWidth() = "+ menu.getWidth());
        }

        ((CameraActivity)getContext()).getAppUi().showQuickIconExceptFlash(); // zhangguo add for bug#74020 quick icon is gone when flash icon clicked
        invalidate();
    }

    @Override
    public void computeScroll() {
        boolean haveOffset = mScroller.computeScrollOffset();
        LogHelper.d(TAG, "[computeScroll] haveOffset = "+ haveOffset);
        if (haveOffset) {
            menu.scrollTo(mScroller.getCurrX(), 0);
            LogHelper.d(TAG, "[computeScroll] mScroller.getCurrX() = "+mScroller.getCurrX());
            if (mIsHide && menu.getWidth() == Math.abs(mScroller.getCurrX())){
                if (mOnHideAnimationListener != null){
                    mOnHideAnimationListener.onHideAnimationEnd();
                }
                menu.setVisibility(View.GONE);
            } else if (!mIsHide && Math.abs(mScroller.getCurrX()) == 0){
                if (mOnHideAnimationListener != null){
                    mOnHideAnimationListener.onShowAnimationEnd();
                }
            }
            postInvalidate();
        }
    }

    public interface OnAnimationListener {
        void onShowAnimationStart();
        void onShowAnimationEnd();
        void onHideAnimationEnd();
    }

    private OnAnimationListener mOnHideAnimationListener;

    public void setOnAnimationListener(OnAnimationListener onHideAnimationListener){
        mOnHideAnimationListener = onHideAnimationListener;
    }

    public boolean isMenuShown(){
        return !mIsHide;
    }

}
