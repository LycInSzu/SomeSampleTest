/*
 * Copyright Statement:
 *
 *   This software/firmware and related documentation ("MediaTek Software") are
 *   protected under relevant copyright laws. The information contained herein is
 *   confidential and proprietary to MediaTek Inc. and/or its licensors. Without
 *   the prior written permission of MediaTek inc. and/or its licensors, any
 *   reproduction, modification, use or disclosure of MediaTek Software, and
 *   information contained herein, in whole or in part, shall be strictly
 *   prohibited.
 *
 *   MediaTek Inc. (C) 2016. All rights reserved.
 *
 *   BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 *   THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 *   RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER
 *   ON AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL
 *   WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 *   WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 *   NONINFRINGEMENT. NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH
 *   RESPECT TO THE SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY,
 *   INCORPORATED IN, OR SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES
 *   TO LOOK ONLY TO SUCH THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO.
 *   RECEIVER EXPRESSLY ACKNOWLEDGES THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO
 *   OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES CONTAINED IN MEDIATEK
 *   SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE
 *   RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 *   STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S
 *   ENTIRE AND CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE
 *   RELEASED HEREUNDER WILL BE, AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE
 *   MEDIATEK SOFTWARE AT ISSUE, OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE
 *   CHARGE PAID BY RECEIVER TO MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 *   The following software/firmware and/or related documentation ("MediaTek
 *   Software") have been modified by MediaTek Inc. All revisions are subject to
 *   any receiver's applicable license agreements with MediaTek Inc.
 */
package com.mediatek.camera.ui.shutter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceControl;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Scroller;
import android.view.animation.ScaleAnimation;

import com.mediatek.camera.common.IAppUiListener;
import com.mediatek.camera.common.app.IApp;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.utils.CameraUtil;
import com.mediatek.camera.prize.FeatureSwitcher;
import com.mediatek.camera.ui.prize.BlurPic;
//prize-added by tangan-custom ui-begin
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import com.mediatek.camera.R;
import com.mediatek.camera.CameraActivity;
//prize-added by tangan-custom ui-end

/**
 * Shutter button root layout, control the shutter ui layout and scroll animation.
 */
class ShutterRootLayout extends RelativeLayout implements ShutterView.OnShutterTextClicked {
    /**
     * Shutter type change listener.
     */
    public interface OnShutterChangeListener {
        /**
         * When current valid shutter changed, invoke the listener to notify.
         * @param newShutterName The new valid shutter name.
         */
        void onShutterChangedStart(String newShutterName);

        /**
         * When shutter change animation finish, invoke the listener to notify.
         */
        void onShutterChangedEnd();
    }
    private static final LogUtil.Tag TAG = new LogUtil.Tag(
                                        ShutterRootLayout.class.getSimpleName());
    private static final int ANIM_DURATION_MS = 0;
    private Scroller mScroller;

    private static final int MINI_SCROLL_LENGTH = 100;

    private int mCurrentIndex = 0;
    private int mScrollDistance = 0;

    private OnShutterChangeListener mListener;

    private boolean mResumed = false;
//prize-added by tangan-custom ui-begin
    private Context mContext;
    private List<String> mBackModeTile,mFrontModeTile;
    private HashMap<String,Integer> mTileHashMap= null;
    private HashMap<Integer,String> mIndexHashMap= null;
    private SelectHorizontalScrollerLayout mChsView;
    private int mTargetIndex = -1;
    private int shutviewWidth = 0;
    private boolean mLayout = false;
    private  String mBackDefaultMode,mFrontDefaultMode;

//prize-added by tangan-custom ui-end

    /*prize-modify-transition animation of change mode -xiaoping-20180929-start*/
    private int screenWidth;
    private int screenheight;
    private ImageView screenShotImg;
    private int mCurrentWhitch;
    private static final int MSG_HIDE_SURFACE_COVER = 4;
	/*prize-add fixbug:[70734][70006][69414][69396][69295]-huangpengfei-2019-01-14-start*/
    private static final int MSG_ANIMATION_HAS_START = 5;
    private static final int MSG_SHUTTER_SNAP = 6;
	/*prize-add fixbug:[70734][70006][69414][69396][69295]-huangpengfei-2019-01-14-end*/

    private boolean isFirst = true;

    private float lastPreviewHeight;

    private float mLastBlurBottomMarg;

    private float mBlurBottomMarg;

    /*prize-add-adjust the animation effect-xiaoping-20171017-start*/
    private static final float SCALLTOBIG_PIVOTYVALUE = 0.25f;
    private static final float SCALLTOSMALL_PIVOTYVALUE = 0.3f;
    /*prize-add-adjust the animation effect-xiaoping-20171017-end*/
    /*prize-modify-transition animation of change mode -xiaoping-20180929-end*/
    /*prize-modify-optimization mode switching process-xiaoping-20181109-start*/
    private long mLastScrollTime;
    /*prize-modify-optimization mode switching process-xiaoping-20181109-start*/

    /*prize-modify-add portrait mode -xiaoping-20181212-start*/
    private int mLastwhichShutter = FeatureSwitcher.getVideoModeIndex() + 1;
    private  boolean isFirstOpenCamera = true;
    private static final int mVideoModeIndex = FeatureSwitcher.getVideoModeIndex();
    /*prize-modify-add portrait mode -xiaoping-20181212-end*/

    /*prize-modify fixbug:[70734][70006][69414][69396][69295]-huangpengfei-2019-01-14-start*/
    private static final long HIDE_SURFACE_COVER_TIME_DELAY = 500;
    private boolean mIsCoverAnimationRuninng = false;
    /*prize-modify fixbug:[70734][70006][69414][69396][69295]-huangpengfei-2019-01-14-end*/

    public void setOnShutterChangedListener(OnShutterChangeListener listener) {
        mListener = listener;
    }

    public IAppUiListener.OnGestureListener getGestureListener() {
        return new GestureListenerImpl();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return true;
    }
    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        View child;
        for (int i = 0; i < getChildCount(); i++) {
            child = getChildAt(i);
            ((ShutterView) child).onScrolled(l, (getWidth() + 1) / 2, mScrollDistance);
        }
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            postInvalidate();
            if (mScroller.isFinished() && mListener != null) {
                mListener.onShutterChangedEnd();
            }
        }
    }

    public ShutterRootLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initScreenWidth((CameraActivity) context);
        mScroller = new Scroller(context, new DecelerateInterpolator());
       //prize-added by tangan-custom ui-begin
	    mContext = context;
        mBackModeTile = new ArrayList<String>();
        mFrontModeTile = new ArrayList<String>();
        mTileHashMap = new HashMap<String,Integer>();
        mIndexHashMap = new HashMap<Integer,String>();
        mBackDefaultMode = (mContext.getResources().getString(R.string.shutter_type_photo));
        /*prize-modify-add switches to third-party algorithms-xiaoping-20181026-start*/
        if (FeatureSwitcher.isFaceBeautyupported() && !FeatureSwitcher.isFrontModeNormal()) {
            mFrontDefaultMode = (mContext.getResources().getString(R.string.shutter_type_photo_beauty));
        } else {
            mFrontDefaultMode = (mContext.getResources().getString(R.string.shutter_type_photo));
        }
        /*prize-modify-add switches to third-party algorithms-xiaoping-20181026-end*/
		//prize-added by tangan-custom ui-end
    }


    @Override
    public void onShutterTextClicked(int index) {
        LogHelper.d(TAG, "onShutterTextClicked index = " + index);
        if (mScroller.isFinished() && isEnabled() && mResumed) {
            snapTOShutter(index, ANIM_DURATION_MS);
        }
    }

    public void updateCurrentShutterIndex(int shutterIndex) {
            doShutterAnimation(shutterIndex, 0);
            //prize-added by tangan-custom ui-begin
            if(shutterIndex < mBackModeTile.size()){
                if(mChsView !=  null){
                    if (mChsView != null && shutterIndex < mBackModeTile.size()) {
                        ShutterView shutter = (ShutterView) getChildAt(shutterIndex);
                        if(getCurrentCameraId() == 0){
                            mChsView.setSelectIndex(mBackModeTile.indexOf(shutter.getName()));
                        }else{
                            mChsView.setSelectIndex(mFrontModeTile.indexOf(shutter.getName()));
                        }

                    }

                }
            }
            //prize-added by tangan-custom ui-end
    }


   //prize-added by tangan-custom ui-begin
    @Override
    public void addView(View child) {
        super.addView(child);
        if(child instanceof ShutterView ){
            String tileName = ((ShutterView)child).getName();
            if(!mBackModeTile.contains(tileName)){
                mBackModeTile.add(tileName);
                LogHelper.i(TAG,"mBackModeTile add mode: "+tileName);
            }
            if(!mFrontModeTile.contains(tileName)){
                // zhangguo modify for front mode switch
                /*if((mContext.getResources().getString(R.string.shutter_type_photo_beauty)).equals(tileName)
                        ||(mContext.getResources().getString(R.string.shutter_type_photo)).equals(tileName)
                        ||(mContext.getResources().getString(R.string.shutter_type_video)).equals(tileName)
                        || (mContext.getResources().getString(R.string.shutter_type_photo_picsefile)).equals(tileName)){
                    mFrontModeTile.add(tileName);
                }*/
                mFrontModeTile.add(tileName);
            }
            mTileHashMap.put(tileName,getChildCount()-1);
            mIndexHashMap.put(getChildCount()-1,tileName);


        }
    }

    public String getDefaultMode(int cameraId){
        if(cameraId == 0){
            return mBackDefaultMode;
        }else{
            return mFrontDefaultMode;
        }
    }

    public void onCameraSelected(){
        HorizontalScrollLayoutAdapter adapter = new HorizontalScrollLayoutAdapter(mContext, mBackModeTile, R.layout.photo_mode_item);
        String modeItem = mBackDefaultMode;
        if(getCurrentCameraId() != 0){
            adapter = new HorizontalScrollLayoutAdapter(mContext, mFrontModeTile, R.layout.photo_mode_item);
            modeItem = mFrontDefaultMode;
        }
        mChsView.setAdapter(adapter);
        snapTOShutter(modeItem,true);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        mLayout = true;
        if(mTargetIndex >= 0){
            doShutterAnimation(mTargetIndex,0);
            if(mCurrentIndex < mBackModeTile.size()){
                if (mChsView != null && mCurrentIndex < mBackModeTile.size()) {
                    ShutterView shutter = (ShutterView) getChildAt(mCurrentIndex);
                    if(getCurrentCameraId() == 0){
                        mChsView.setSelectIndex(mBackModeTile.indexOf(shutter.getName()));
                    }else{
                        mChsView.setSelectIndex(mFrontModeTile.indexOf(shutter.getName()));
                    }
                }
            }
            mTargetIndex = -1;
        }
    }

    //prize-added by tangan-custom ui-end


    public void onResume() {
        LogHelper.i(TAG,",mBackModeTile: "+mBackModeTile);
        mResumed = true;

    }

	//prize-added by tangan-custom ui-begin
    public  void initChsView(){
        clearListener();
        mChsView = getRootView().findViewById(R.id.mode_scrollview);
        HorizontalScrollLayoutAdapter adapter = new HorizontalScrollLayoutAdapter(mContext, mBackModeTile, R.layout.photo_mode_item);
        if(getCurrentCameraId() != 0){
            adapter = new HorizontalScrollLayoutAdapter(mContext, mFrontModeTile, R.layout.photo_mode_item);
        }
        mChsView.setAdapter(adapter);
        if(mBackModeTile.size()<2){
            mChsView.setVisibility(GONE);
        }
        if(mCurrentIndex < mBackModeTile.size()){
            if(mChsView !=  null){
                if (mChsView != null && mCurrentIndex < mBackModeTile.size()) {
                    ShutterView shutter = (ShutterView) getChildAt(mCurrentIndex);
                    if(getCurrentCameraId() == 0){
                        mChsView.setSelectIndex(mBackModeTile.indexOf(shutter.getName()));
                    }else{
                        mChsView.setSelectIndex(mFrontModeTile.indexOf(shutter.getName()));
                    }

                }

            }
        }
        applyListener();
		//prize-added by tangan-custom ui-end
    }

	//prize-added by tangan-custom ui-begin
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if(mChsView != null){
            mChsView.setEnabled(enabled);
        }
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        LogHelper.i(TAG,",visibility: "+visibility+",mBackModeTile: "+mBackModeTile);
        if(mChsView != null){
            if(mBackModeTile.size()<2){
                mChsView.setVisibility(GONE);
            }else{
                mChsView.setVisibility(visibility);
            }
        }
    }

    private SelectHorizontalScrollerLayout.OnItemClickListener mOnItemChangeListener = new SelectHorizontalScrollerLayout.OnItemClickListener() {

        @Override
        public void onItemClick(int pos) {
            // TODO Auto-generated method stub
            if (mScroller.isFinished() && isEnabled() && mResumed) {
                if(getCurrentCameraId() == 0){
                    if(pos > (mBackModeTile.size() -1) || pos < 0){
                        pos = mBackModeTile.size() -1;
                    }
                    snapTOShutter(mBackModeTile.get(pos));
                }else{
                    if(pos > (mFrontModeTile.size() -1) || pos < 0){
                        pos = mFrontModeTile.size() -1;
                    }
                    snapTOShutter(mFrontModeTile.get(pos));
                }

            }
        }


    };

    private void applyListener() {
        mChsView.setOnItemClickListener(mOnItemChangeListener);
    }

    private void clearListener() {
        if(mChsView != null){
            mChsView.setOnItemClickListener(null);
        }
    }

    /*prize-modify for Model merging-huangpengfei-2019-02-23-start*/
    private int getCurrentCameraId() {
        //return  ((CameraActivity) mContext).getAppUi().getCameraId();
        return 0;
    }
    /*prize-modify for Model merging-huangpengfei-2019-02-23-end*/
	//prize-added by tangan-custom ui-end

    public void onPause() {
        mResumed = false;
    }

    /**
     * Control the shutterview UI that shows the photo or video
     * @param whichShutter
     * @param animationDuration
     */
    private void doShutterAnimation(int whichShutter, int animationDuration) {
        LogHelper.i(TAG,",whichShutter: "+whichShutter+",mCurrentIndex: "+mCurrentIndex);
        int shutterviewwidth = (int) mContext.getResources().getDimension(R.dimen.mode_text_width);
	//prize-added by tangan-custom ui-end
        if(!mLayout){
            mTargetIndex = whichShutter;
            return;
        }
        if (whichShutter > getChildCount() - 1) {
            whichShutter = getChildCount() - 1;
        }
        int dx = 0;
        if (whichShutter == 0) {
            dx = -getScrollX();
        } else {
            int width = 0;
            if( whichShutter > mCurrentIndex){
                for(int i=mCurrentIndex;i<whichShutter;i++){
					if(getChildAt(i)!= null && shutviewWidth == 0){
                        shutviewWidth = getChildAt(i).getMeasuredWidth();
					}
                    LogHelper.i("tangan","doShutterAnimation wchildWidth="+shutviewWidth);
                    width=width+shutviewWidth;
                }
            }else{
                for(int i=whichShutter;i<mCurrentIndex;i++){
					if(getChildAt(i) != null && shutviewWidth == 0){
                        shutviewWidth = getChildAt(i).getMeasuredWidth();
					}
                    LogHelper.i("tangan","doShutterAnimation wchildWidth="+shutviewWidth);
                    width=width-shutviewWidth;

                }
            }
            LogHelper.i("tangan","width="+width);
            dx = width;
        }
        mCurrentIndex = whichShutter;
        //prize-added by tangan-custom ui-end

        /*prize-modify-add portrait mode -xiaoping-20181212-start*/
        /*prize-modify-adapt ui of RTL-xiaoping-20181228-start*/
        if (((CameraActivity)mContext).getAppUi().isRTL()) {
            if (whichShutter == mVideoModeIndex) {
                mScroller.startScroll(getScrollX(), 0, shutterviewwidth, 0, animationDuration);
                mScrollDistance = Math.abs(dx);
            } else if (mLastwhichShutter == mVideoModeIndex) {
                mScroller.startScroll(getScrollX(), 0, -shutterviewwidth, 0, animationDuration);
                mScrollDistance = Math.abs(dx);
            }
            if (isFirstOpenCamera) {
                mScroller.startScroll(getScrollX(), 0, -dx, 0, animationDuration);
                mScrollDistance = Math.abs(dx);
                isFirstOpenCamera = false;
            }
            /*prize-modify-adapt ui of RTL-xiaoping-20181228-end*/
        } else {
            if (whichShutter == mVideoModeIndex) {
                mScroller.startScroll(getScrollX(), 0, -shutterviewwidth, 0, animationDuration);
                mScrollDistance = Math.abs(dx);
                ((CameraActivity)mContext).getAppUi().revetButtonState();
            } else if (mLastwhichShutter == mVideoModeIndex) {
                mScroller.startScroll(getScrollX(), 0, shutterviewwidth, 0, animationDuration);
                mScrollDistance = Math.abs(dx);
            }
            if (isFirstOpenCamera) {
                mScroller.startScroll(getScrollX(), 0, dx, 0, animationDuration);
                mScrollDistance = Math.abs(dx);
                isFirstOpenCamera = false;
            }
        }
        invalidate();
        mLastwhichShutter = whichShutter;
        /*prize-modify-add portrait mode -xiaoping-20181212-end*/
    }

	//prize-added by tangan-custom ui-begin
    public void snapTOShutter(String modeName){
        int index = 0;
        if(mTileHashMap.containsKey(modeName)){
            index = mTileHashMap.get(modeName);
        }
        /*prize-modify-Optimization mode switching process-xiaoping-20181109-start*/
        if (System.currentTimeMillis() - mLastScrollTime > 1000) {
            mLastScrollTime = System.currentTimeMillis();
            snapTOShutter(index,ANIM_DURATION_MS);
            /*prize-add fixbug:[70734][70006][69414][69396][69295]-huangpengfei-2019-01-14-start*/
            mChsView.setShutterSwitchFinish(false);
            mHandler.sendEmptyMessageDelayed(MSG_SHUTTER_SNAP,1000);
            /*prize-add fixbug:[70734][70006][69414][69396][69295]-huangpengfei-2019-01-14-end*/
        } else {
            LogHelper.w(TAG,"The interval between two switching modes is less than 1s,do not need changemode");
        }
        /*prize-modify-Optimization mode switching process-xiaoping-20181109-end*/

    }
    public void snapTOShutter(String modeName,boolean force){
        int index = 0;
        if(mTileHashMap.containsKey(modeName)){
            index = mTileHashMap.get(modeName);
        }
        snapTOShutter(index,ANIM_DURATION_MS,force);
    }
    public void snapTOShutter(int whichShutter, int animationDuration){
        snapTOShutter(whichShutter,animationDuration,false);
    }
    public void snapTOShutter(int whichShutter, int animationDuration,boolean force) {
        if (whichShutter == mCurrentIndex && !force) {
            return;
        }
        ShutterView shutterView = (ShutterView) getChildAt(whichShutter);
        /*prize-modify-transition animation of change mode -xiaoping-20180929-start*/
        LogHelper.i(TAG, "snapTOShutter,whichShutter: " + whichShutter + ",mCurrentIndex: " + mCurrentIndex);
        mCurrentWhitch = whichShutter;
        doShutterAnimation(whichShutter, animationDuration);
        //showSurfaceCover();
		/*prize-add fixbug:[70734][70006][69414][69396][69295]-huangpengfei-2019-01-14-start*/
        if (mListener != null) {
            ShutterView shutter = (ShutterView) getChildAt(mCurrentIndex);
            mListener.onShutterChangedStart(shutter.getType());
        }
        if (mChsView != null && mCurrentWhitch < mBackModeTile.size()) {
            ShutterView shutter = (ShutterView) getChildAt(mCurrentWhitch);
            if (getCurrentCameraId() == 0) {
                mChsView.setSelectIndex(mBackModeTile.indexOf(shutter.getName()));
            } else {
                mChsView.setSelectIndex(mFrontModeTile.indexOf(shutter.getName()));
            }
        }
		/*prize-add fixbug:[70734][70006][69414][69396][69295]-huangpengfei-2019-01-14-end*/
        isFirst = false;
        /*prize-modify-transition animation of change mode -xiaoping-20180929-end*/
    }
	//prize-added by tangan-custom ui-end

    /**
     * Gesture listener implementer.
     */
    private class GestureListenerImpl implements IAppUiListener.OnGestureListener {

        private float mTransitionX;
        private float mTransitionY;
        private boolean mIsScale;

        @Override
        public boolean onDown(MotionEvent event) {
            mTransitionX = 0;
            mTransitionY = 0;
            return false;
        }

        @Override
        public boolean onUp(MotionEvent event) {
            mTransitionX = 0;
            mTransitionY = 0;
            return false;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float dx, float dy) {
            if (e2.getPointerCount() > 1) {
                return false;
            }
            if (getChildCount() < 2) {
                return false;
            }
            if (mIsScale) {
                return false;
            }
            if (mScroller.isFinished() && isEnabled() && mResumed) {
                mTransitionX += dx;
                mTransitionY += dy;

                Configuration config = getResources().getConfiguration();
                if (config.orientation == Configuration.ORIENTATION_PORTRAIT) {
                    if (Math.abs(mTransitionX) > MINI_SCROLL_LENGTH
                            && Math.abs(mTransitionY) < Math.abs(mTransitionX)) {
                        if (mTransitionX > 0 && mCurrentIndex < (getChildCount() - 1)) {
                            if (getVisibility() != VISIBLE) {
                                return false;
                            }
                            if (getChildAt(mCurrentIndex + 1).getVisibility() != VISIBLE) {
                                return false;
                            }
							//prize-added by tangan-custom ui-begin
                            if(!mChsView.isEnabled()){
                                return false;
                            }
                            if(getCurrentCameraId() == 0){
                                String shutName = mIndexHashMap.get(mCurrentIndex);
                                if(mBackModeTile.indexOf(shutName) < mBackModeTile.size() -1){
                                    if (((CameraActivity)mContext).getAppUi().isRTL()) {
                                        snapTOShutter(mBackModeTile.get(filterIndex(mBackModeTile.indexOf(shutName)-1)));
                                    } else {
                                        snapTOShutter(mBackModeTile.get(filterIndex(mBackModeTile.indexOf(shutName)+1)));
                                    }
                                }

                            }else{
                                String shutName = mIndexHashMap.get(mCurrentIndex);
                                if(mFrontModeTile.indexOf(shutName) < mFrontModeTile.size() -1){
                                    if (((CameraActivity)mContext).getAppUi().isRTL()) {
                                        snapTOShutter(mFrontModeTile.get(filterIndex(mFrontModeTile.indexOf(shutName)-1)));
                                    } else {
                                        snapTOShutter(mFrontModeTile.get(filterIndex(mFrontModeTile.indexOf(shutName)+1)));
                                    }
                                }
                            }
							//prize-added by tangan-custom ui-end
                        } else if (mTransitionX < 0 && mCurrentIndex > 0) {
                            if (getVisibility() != VISIBLE) {
                                return false;
                            }
                            if (getChildAt(mCurrentIndex - 1).getVisibility() != VISIBLE) {
                                return false;
                            }
							//prize-added by tangan-custom ui-begin
                            if(!mChsView.isEnabled()){
                                return false;
                            }
                            if(getCurrentCameraId() == 0){
                                String shutName = mIndexHashMap.get(mCurrentIndex);
                                if(mBackModeTile.indexOf(shutName) > 0){
                                    if (((CameraActivity)mContext).getAppUi().isRTL()) {
                                        snapTOShutter(mBackModeTile.get(filterIndex(mBackModeTile.indexOf(shutName)+1)));
                                    } else {
                                        snapTOShutter(mBackModeTile.get(filterIndex(mBackModeTile.indexOf(shutName)-1)));
                                    }
                                }

                            }else{
                                String shutName = mIndexHashMap.get(mCurrentIndex);
                                if(mFrontModeTile.indexOf(shutName) >0){
                                    if (((CameraActivity)mContext).getAppUi().isRTL()) {
                                        snapTOShutter(mFrontModeTile.get(filterIndex(mFrontModeTile.indexOf(shutName) + 1)));
                                    } else {
                                        snapTOShutter(mFrontModeTile.get(filterIndex(mFrontModeTile.indexOf(shutName)- 1)));
                                    }
                                    /*prize-modify-adapt ui of RTL-xiaoping-20181228-start*/
                                } else if (((CameraActivity)mContext).getAppUi().isRTL()) {
                                    snapTOShutter(mFrontModeTile.get(filterIndex(mFrontModeTile.indexOf(shutName) + 1)));
                                }
                                    /*prize-modify-adapt ui of RTL-xiaoping-20181228-end*/
                            }
							//prize-added by tangan-custom ui-end
                            /*prize-modify-adapt ui of RTL-xiaoping-20181228-start*/
                        } else if (((CameraActivity)mContext).getAppUi().isRTL()) {
                            if (getVisibility() != VISIBLE) {
                                return false;
                            }

                            if(!mChsView.isEnabled()){
                                return false;
                            }
                            String shutName = mIndexHashMap.get(mCurrentIndex);
                            if(getCurrentCameraId() == 0) {
                                if (mCurrentIndex == 0) {
                                    snapTOShutter(mBackModeTile.get(1));
                                } else if (mCurrentIndex == mBackModeTile.size() - 1) {
                                    snapTOShutter(mBackModeTile.get(mBackModeTile.size() - 2));
                                }
                            } else {
                                if (mFrontModeTile.indexOf(shutName) == 0) {
                                    snapTOShutter(mFrontModeTile.get(filterIndex(mFrontModeTile.indexOf(shutName) + 1)));
                                } else if (mFrontModeTile.indexOf(shutName) == mFrontModeTile.size() - 1) {
                                    snapTOShutter(mFrontModeTile.get(filterIndex(mFrontModeTile.size() - 2 )));
                                }
                            }
                        }
                        /*prize-modify-adapt ui of RTL-xiaoping-20181228-end*/
                        return true;
                    }
                } else if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    if (Math.abs(mTransitionY) > MINI_SCROLL_LENGTH
                            && Math.abs(mTransitionX) < Math.abs(mTransitionY)) {
                        if (mTransitionY < 0 && mCurrentIndex < (getChildCount() - 1)) {
                            if (getChildAt(mCurrentIndex + 1).getVisibility() != VISIBLE) {
                                return false;
                            }
							//prize-added by tangan-custom ui-begin
                            if(!mChsView.isEnabled()){
                                return false;
                            }
                            if(getCurrentCameraId() == 0){
                                String shutName = mIndexHashMap.get(mCurrentIndex);
                                if(mBackModeTile.indexOf(shutName) < mBackModeTile.size() -1){
                                    if (((CameraActivity)mContext).getAppUi().isRTL()) {
                                        snapTOShutter(mBackModeTile.get(filterIndex(mBackModeTile.indexOf(shutName)-1)));
                                    } else {
                                        snapTOShutter(mBackModeTile.get(filterIndex(mBackModeTile.indexOf(shutName)+1)));
                                    }
                                }

                            }else{
                                String shutName = mIndexHashMap.get(mCurrentIndex);
                                if(mFrontModeTile.indexOf(shutName) < mFrontModeTile.size() -1){
                                    if (((CameraActivity)mContext).getAppUi().isRTL()) {
                                        snapTOShutter(mFrontModeTile.get(filterIndex(mFrontModeTile.indexOf(shutName)-1)));
                                    } else {
                                        snapTOShutter(mFrontModeTile.get(filterIndex(mFrontModeTile.indexOf(shutName)+1)));
                                    }
                                }
                            }
							//prize-added by tangan-custom ui-end
                        } else if (mTransitionY > 0 && mCurrentIndex > 0) {
                            if (getChildAt(mCurrentIndex - 1).getVisibility() != VISIBLE) {
                                return false;
                            }
							//prize-added by tangan-custom ui-begin
                            if(!mChsView.isEnabled()){
                                return false;
                            }
                            if(getCurrentCameraId() == 0){
                                String shutName = mIndexHashMap.get(mCurrentIndex);
                                if(mBackModeTile.indexOf(shutName) > 0){
                                    if (((CameraActivity)mContext).getAppUi().isRTL()) {
                                        snapTOShutter(mBackModeTile.get(filterIndex(mBackModeTile.indexOf(shutName)+1)));
                                    } else {
                                        snapTOShutter(mBackModeTile.get(filterIndex(mBackModeTile.indexOf(shutName)-1)));
                                    }
                                }

                            }else{
                                String shutName = mIndexHashMap.get(mCurrentIndex);
                                if(mFrontModeTile.indexOf(shutName) >0){
                                    if (((CameraActivity)mContext).getAppUi().isRTL()) {
                                        snapTOShutter(mFrontModeTile.get(filterIndex(mFrontModeTile.indexOf(shutName)+ 1)));
                                    } else {
                                        snapTOShutter(mFrontModeTile.get(filterIndex(mFrontModeTile.indexOf(shutName)- 1)));
                                    }
                                }
                            }
							//prize-added by tangan-custom ui-end
                        }
                    }
                }
                return false;
            } else {
                return true;
            }
        }

        @Override
        public boolean onSingleTapUp(float x, float y) {
            return false;
        }

        @Override
        public boolean onSingleTapConfirmed(float x, float y) {
            return false;
        }

        @Override
        public boolean onDoubleTap(float x, float y) {
            return false;
        }

        @Override
        public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
            return false;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector) {
            mIsScale = true;
            return false;
        }

        @Override
        public boolean onScaleEnd(ScaleGestureDetector scaleGestureDetector) {
            mIsScale = false;
            return false;
        }

        @Override
        public boolean onLongPress(float x, float y) {
            return false;
        }
    }

    // [Add for CCT tool] Receive keycode and switch photo/video mode @{
    public IApp.KeyEventListener getKeyEventListener() {
        return new KeyEventListenerImpl();
    }

    private class KeyEventListenerImpl implements IApp.KeyEventListener {

        @Override
        public boolean onKeyDown(int keyCode, KeyEvent event) {
            if ((keyCode != CameraUtil.KEYCODE_SWITCH_TO_PHOTO
                    && keyCode != CameraUtil.KEYCODE_SWITCH_TO_VIDEO)
                    || !CameraUtil.isSpecialKeyCodeEnabled()) {
                return false;
            }
            return true;
        }

        @Override
        public boolean onKeyUp(int keyCode, KeyEvent event) {
            if (!CameraUtil.isSpecialKeyCodeEnabled()) {
                return false;
            }
            if ((keyCode != CameraUtil.KEYCODE_SWITCH_TO_PHOTO
                    && keyCode != CameraUtil.KEYCODE_SWITCH_TO_VIDEO)) {
                return false;
            }
            if (getChildCount() < 2) {
                LogHelper.w(TAG, "onKeyUp no need to slide betwwen photo mode and video mode," +
                        "one mode olny");
                return false;
            }
            if (keyCode == CameraUtil.KEYCODE_SWITCH_TO_PHOTO
                    && getChildCount() == 2
                    && getChildAt(0).getVisibility() == View.VISIBLE
                    && getChildAt(1).getVisibility() == View.VISIBLE) {
                onShutterTextClicked(0);
            } else if (keyCode == CameraUtil.KEYCODE_SWITCH_TO_VIDEO
                    && getChildCount() == 2
                    && getChildAt(0).getVisibility() == View.VISIBLE
                    && getChildAt(1).getVisibility() == View.VISIBLE) {
                onShutterTextClicked(1);
            }
            return true;
        }
    }
    // @}

    /*prize-modify-switch mode after switch camera-xiaoping-20180913-start*/
    public void setCurrentIndex(int currentIndex) {
        LogHelper.i("","currentIndex: "+currentIndex);
        snapTOShutter(currentIndex,ShutterRootLayout.ANIM_DURATION_MS,true);
    }

    public void cleatModeTitle() {
        LogHelper.i(TAG,"AllModeTitle has clean");
        if (mBackModeTile != null) {
            mBackModeTile.clear();
        }
    }
    /*prize-modify-switch mode after switch camera-xiaoping-20180913-end*/

    /*prize-modify-transition animation of change mode -xiaoping-20180929-start*/
    class BlurAsyncTask extends AsyncTask<Bitmap, Bitmap, Bitmap> {

        @Override
        protected Bitmap doInBackground(Bitmap... bitmaps) {
            return BlurPic.blurScale(bitmaps[0]);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            showSurfaceCover(bitmap);
        }
    }

    public void showSurfaceCover() {
        BlurAsyncTask mBlurAsyncTask = new BlurAsyncTask();
        mBlurAsyncTask.execute(takeScreenShot());
    }

    /*prize-modify fixbug:[70734][70006][69414][69396][69295]-huangpengfei-2019-01-14-start*/
    public Bitmap takeScreenShot() {
        try {
            //Bitmap mBitmap = SurfaceControl.screenshot(screenWidth, screenheight);
            Bitmap mBitmap = drawableToBitmap(mContext.getDrawable(R.drawable.modepicker_long));
            int top = /*getContext().getPreviewSurfaceView().getBottom() - getContext().getPreviewFrameHeight()*/0;
            int bottom = /*getContext().getPreviewFrameHeight()*/((CameraActivity) mContext).getAppUi().getPreviewFrameLayout().getPreviewHeight();
            /*if((mPreCurrentMode == MODE_PREVIDEO && modeState == MODE_STATE_CHANGING) && screenheight <= getContext().getPreviewFrameHeight()){
                bottom = bottom - (int)getContext().getResources().getDimension(R.dimen.shutter_group_height);
            }*/
            //return Bitmap.createBitmap(mBitmap, 0, top >= 0 ? top : 0, screenWidth, bottom < screenheight ? bottom : screenheight, null, false);
            return mBitmap;
        } catch (Exception e) {
            LogHelper.d(TAG, "takeScreenShot exception=" + String.valueOf(e));
            return null;
        }

    }


    public void initScreenWidth(CameraActivity mContext) {
        DisplayMetrics metric = new DisplayMetrics();
        mContext.getWindowManager().getDefaultDisplay().getRealMetrics(metric);
        screenWidth = metric.widthPixels;
        screenheight = metric.heightPixels;
    }

    public void showSurfaceCover(Bitmap result) {
        if (mIsCoverAnimationRuninng)return;
        lastPreviewHeight = ((CameraActivity) mContext).getAppUi().getPreviewFrameLayout().getPreviewHeight();
        screenShotImg = (ImageView) ((CameraActivity) mContext).findViewById(R.id.sf_screenshot);
        /*if (screenShotImg != null) {
            if (((CameraActivity) mContext).getAppUi().getPreviewFrameLayout().getPreviewHeight() >= 1270) {
                adapter169display();
                mLastBlurBottomMarg = 0;
            } else {
                adapter43display();
                mLastBlurBottomMarg = getContext().getResources().getDimension(R.dimen.shutter_group_height);
            }

        }*/
        screenShotImg.setImageBitmap(result);
        Animation mAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.surfacecover_show);
        mAnimation.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
                LogHelper.i("", "showSurfaceCover onAnimationStart");
                screenShotImg.setAlpha(1.0f);
                mIsCoverAnimationRuninng = true;
                mHandler.removeMessages(MSG_ANIMATION_HAS_START);
                mHandler.sendEmptyMessageDelayed(MSG_ANIMATION_HAS_START,2000);
                /*if (mChsView != null && mCurrentWhitch < mBackModeTile.size()) {
                    ShutterView shutter = (ShutterView) getChildAt(mCurrentWhitch);
                    if (getCurrentCameraId() == 0) {
                        mChsView.setSelectIndex(mBackModeTile.indexOf(shutter.getName()));
                    } else {
                        mChsView.setSelectIndex(mFrontModeTile.indexOf(shutter.getName()));
                    }
                }*/
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                LogHelper.d(TAG, "showSurfaceCover onAnimationEnd");
                /*if (mListener != null) {
                    ShutterView shutter = (ShutterView) getChildAt(mCurrentIndex);
                    mListener.onShutterChangedStart(shutter.getName());
                }*/
                mIsCoverAnimationRuninng = false;
                /*prize-modify-opt:Optimize the switch camera mode process-xiaoping-20181127-start*/
                mHandler.sendEmptyMessageDelayed(MSG_HIDE_SURFACE_COVER, HIDE_SURFACE_COVER_TIME_DELAY);
                /*prize-modify-opt:Optimize the switch camera mode process-xiaoping-20181127-end*/
            }
        });
        screenShotImg.startAnimation(mAnimation);
    }
	/*prize-modify fixbug:[70734][70006][69414][69396][69295]-huangpengfei-2019-01-14-end*/

    public void adapter43display() {
        LogHelper.d(TAG, "adapter43display,getBottom: " + ((CameraActivity) mContext).getAppUi().getPreviewFrameLayout().getBottom());
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) screenShotImg.getLayoutParams();
        lp.width = ((CameraActivity) mContext).getAppUi().getPreviewFrameLayout().getPreviewWidth();
        lp.height = ((CameraActivity) mContext).getAppUi().getPreviewFrameLayout().getPreviewHeight();
        lp.bottomMargin = (int)getContext().getResources().getDimension(R.dimen.shutter_group_height) - (screenheight - lp.height) / 2;
        lp.topMargin = 0;
        screenShotImg.setLayoutParams(lp);
    }

    public void adapter169display() {
        LogHelper.d(TAG, "adapter169display,getBottom: " + ((CameraActivity) mContext).getAppUi().getPreviewFrameLayout().getBottom());
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) screenShotImg.getLayoutParams();
        lp.width = ((CameraActivity) mContext).getAppUi().getPreviewFrameLayout().getPreviewWidth();
        lp.height = ((CameraActivity) mContext).getAppUi().getPreviewFrameLayout().getPreviewHeight();
        lp.bottomMargin = 0;
        lp.topMargin = 0;
        screenShotImg.setLayoutParams(lp);
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

    public void hideSurfaceCover() {
        if (screenShotImg == null) {
            LogHelper.d(TAG, "hideSurfaceCover screenShotImg == null return");
            return;
        }
        //Animation mAnimation = getScalAnimation();
        Animation mAnimation = null;
        if (mAnimation == null) {
            mAnimation = (AnimationSet) AnimationUtils.loadAnimation(getContext(), R.anim.surfacecover_hide);
        }
        LogHelper.d(TAG, "hideSurfaceCover hideSurfaceCover");
        mAnimation.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
                LogHelper.d(TAG, "hideSurfaceCover onAnimationStart");
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                LogHelper.d(TAG, "hideSurfaceCover onAnimationEnd screenShotLayout = ");
                screenShotImg.setAlpha(0.0f);
                screenShotImg.setBackgroundResource(R.color.alpht);
                /*prize-modify-opt:Optimize the switch camera mode process-xiaoping-20181127-start*/
//                setEnabled(true);
                /*prize-modify-opt:Optimize the switch camera mode process-xiaoping-20181127-end*/
            }
        });

        if (screenShotImg != null) {
            screenShotImg.startAnimation(mAnimation);
        }
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            LogHelper.i(TAG,"[handleMessage] msg.what = " + msg.what);
            switch (msg.what) {
                case MSG_HIDE_SURFACE_COVER:
                    hideSurfaceCover();
                    break;
                /*prize-add fixbug:[70734][70006][69414][69396][69295]-huangpengfei-2019-01-14-start*/
                case MSG_ANIMATION_HAS_START:
                    mIsCoverAnimationRuninng = false;
                    break;
                case MSG_SHUTTER_SNAP:
                    mChsView.setShutterSwitchFinish(true);
                    break;
                /*prize-add fixbug:[70734][70006][69414][69396][69295]-huangpengfei-2019-01-14-end*/
            }

        }
    };

    private ScaleAnimation getScalAnimation() {
        LogHelper.d(TAG,"ScaleAnimation,getPreviewFrameHeight: "+((CameraActivity) mContext).getAppUi().getPreviewFrameLayout().getPreviewHeight()+",lastPreviewHeight: "+lastPreviewHeight+",mBlurBottomMarg: "+mBlurBottomMarg);
        if(((CameraActivity) mContext).getAppUi().getPreviewFrameLayout().getPreviewHeight() != lastPreviewHeight){
            /**
             * Scale big:
             *    lastPreviewHeight*pivotXValue*(getPreviewFrameHeight()/lastPreviewHeight) = lastBlurBottomMarg + lastPreviewHeight*pivotXValue;
             *    pivotXValue = lastBlurBottomMarg / (lastPreviewHeight*(getPreviewFrameHeight()/lastPreviewHeight) -1));
             *
             * Scale small:
             *    lastPreviewHeight*pivotXValue*(getPreviewFrameHeight()/lastPreviewHeight) = lastPreviewHeight*pivotXValue - lastBlurBottomMarg;
             *    pivotXValue = lastBlurBottomMarg / (lastPreviewHeight*(getPreviewFrameHeight()/lastPreviewHeight) + 1));
             *
             */
            float pivotXValue;
            float pivotYValue;
            float toY;

            if (((CameraActivity) mContext).getAppUi().getPreviewFrameLayout().getPreviewHeight() >= 1270){ // != 16:9
                mBlurBottomMarg = getContext().getResources().getDimension(R.dimen.shutter_group_height);
            }else{
                mBlurBottomMarg = Math.abs(getContext().getResources().getDimension(R.dimen.shutter_group_height) - mLastBlurBottomMarg) ;
            }
            if(((CameraActivity) mContext).getAppUi().getPreviewFrameLayout().getPreviewHeight() > lastPreviewHeight){ //Scale big:
                toY = ((CameraActivity) mContext).getAppUi().getPreviewFrameLayout().getPreviewHeight() / lastPreviewHeight;
                pivotXValue = mBlurBottomMarg / (lastPreviewHeight*(toY -1));
                /*prize-add-adjust the animation effect-xiaoping-20170927-start*/
                pivotYValue = SCALLTOBIG_PIVOTYVALUE;
            }else{ // Scale small:
                toY = ((CameraActivity) mContext).getAppUi().getPreviewFrameLayout().getPreviewHeight() / lastPreviewHeight;
                pivotXValue = mBlurBottomMarg / (lastPreviewHeight*(1-toY));
                pivotYValue = SCALLTOSMALL_PIVOTYVALUE;
            }
            LogHelper.i(TAG,"toY: "+toY+",pivotYValue: "+pivotYValue);
            ScaleAnimation mScaleAnimation = new ScaleAnimation(1.0f, 1.0f, 1.0f, toY,
                    Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,pivotYValue);
            /*prize-add-adjust the animation effect-xiaoping-20170927-end*/
            mScaleAnimation.setDuration(200);
            mScaleAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
            LogHelper.i(TAG,"mScaleAnimation end");
            return mScaleAnimation;
        }
        return null;
    }
    /*prize-modify-transition animation of change mode -xiaoping-20180929-end*/
    /*prize-modify-add animation of takepictur-xiaoping-20181105-start*/
    public void setVisibility(int visibility,boolean isPlayAnimation) {
        super.setVisibility(visibility);
    }
    /*prize-modify-add animation of takepictur-xiaoping-20181105-end*/

    /*prize-modify-adapt ui of RTL-xiaoping-20181228-start*/
    private int filterIndex(int index) {
        int tempIndex = index;
        if (((CameraActivity)mContext).getAppUi().getCameraId() == 0) {
            if (index >= mBackModeTile.size()) {
                tempIndex = mBackModeTile.size() - 1;
            } else if (index < 0) {
                tempIndex = 0;
            }
        } else {
            if (index >= mFrontModeTile.size() -1) {
                tempIndex = mFrontModeTile.size() - 1;
            } else if (index < 0) {
                tempIndex = 0;
            }
        }
        return tempIndex;
    }
    /*prize-modify-adapt ui of RTL-xiaoping-20181228-end*/

    /*prize-add for external intent-huangpengfei-2019-2-16-start*/
    public void updateModeTitle(){
        HorizontalScrollLayoutAdapter adapter = new HorizontalScrollLayoutAdapter(mContext, mBackModeTile, R.layout.photo_mode_item);
        if(getCurrentCameraId() != 0){
            adapter = new HorizontalScrollLayoutAdapter(mContext, mFrontModeTile, R.layout.photo_mode_item);
        }
        mChsView.setAdapter(adapter);
    }
    /*prize-add for external intent-huangpengfei-2019-2-16-end*/

    /*prize-modify-bug the bottom camera shutter button is hidden in the countdown state-xiaoping-20190325-start*/
    public void setChsViewVisilibity(int visibility) {
        if(mChsView != null){
            if(mBackModeTile.size()<2){
                mChsView.setVisibility(GONE);
            }else{
                mChsView.setVisibility(visibility);
            }
        }
    }
    /*prize-modify-bug the bottom camera shutter button is hidden in the countdown state-xiaoping-20190325-end*/
	
	public boolean needScrollToPlugin(){
        if(null == mBackModeTile){
            return false;
        }

        return mBackModeTile.size() - 1 != mCurrentIndex;
    }
}
