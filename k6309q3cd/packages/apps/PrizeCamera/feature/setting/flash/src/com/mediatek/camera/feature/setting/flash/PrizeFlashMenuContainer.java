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
    private boolean mIsHide = false;
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
            mScroller.startScroll(menu.getWidth(), 0, -menu.getWidth(), 0, DURATION);
            /*prize-modify-adapt ui of RTL-xiaoping-20181228-end*/
        }else{
            mScroller.startScroll(menu.getWidth(), 0, -menu.getWidth(), 0, DURATION);
            LogHelper.d(TAG, "[show] menu.getWidth() = "+ menu.getWidth()+"   this = "+this );
        }
        mOnHideAnimationListener.onHideAnimationStart();
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
            mScroller.startScroll(0, 0, menu.getWidth(), 0, DURATION);
            /*prize-modify-adapt ui of RTL-xiaoping-20181228-end*/
        }else{
            mScroller.startScroll(0, 0, menu.getWidth(), 0, DURATION);
            LogHelper.d(TAG, "[hide] menu.getWidth() = "+ menu.getWidth());
        }
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
                mOnHideAnimationListener.onHideAnimationEnd();
                menu.setVisibility(View.GONE);
            }
            postInvalidate();
        }
    }

    public interface OnHideAnimationListener {
        void onHideAnimationStart();
        void onHideAnimationEnd();
    }

    private OnHideAnimationListener mOnHideAnimationListener;

    public void setOnAnimationListener(OnHideAnimationListener onHideAnimationListener){
        mOnHideAnimationListener = onHideAnimationListener;
    }

}
