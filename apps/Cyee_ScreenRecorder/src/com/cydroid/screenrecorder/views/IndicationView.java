package com.cydroid.screenrecorder.views;

import android.R.integer;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;
import com.cydroid.screenrecorder.R;
import com.cydroid.screenrecorder.ScreenRecorderService;
import com.cydroid.screenrecorder.utils.Log;
import com.cydroid.screenrecorder.utils.MTool;

import java.util.concurrent.atomic.AtomicBoolean;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;

/**
 * when main layout was hided, this view will show on the screen edge 
 * 
 * @author fuwenzhi
 *
 */
public class IndicationView extends View {
    public static final String TAG = "IndicationView";
    
    private ScreenRecorderService mService;
    private Paint normalPaint = new Paint();
    private Paint subtransparentPaint = new Paint();

    /**
     * view's top left corner x value 
     */
    //private int locationX;
    private int indicationWidth;
    private int indicationHeight;
    private int indicationWidthDimen;
    private int indicationHeightDimen;
    private String normalPaintColor = "#00A6CE";
    private String subtransparentPaintColor = "#FFFF00";
    private int action;
    private int touchRawX;
    /**
     * contain status bar height
     */
    private int touchRawY;
    private int touchDownX;
    private int touchDownY;
    private boolean isFirstDraw = true;
    private AtomicBoolean isTouchMe = new AtomicBoolean(false);
    private boolean isDragedMeUpOrDown = false;
    private boolean isDragMeOut = false;
    private int minMoveDistance = 30;
    
    
    
    public IndicationView(ScreenRecorderService service) {
        super(service.mContext);
        this.mService = service;
        indicationWidth = service.mIndicationWidth;
        indicationHeight = service.mIndicationHeight;
        indicationWidthDimen = (int)service.mContext.getResources().getDimension(R.dimen.indication_width);
        indicationHeightDimen = (int)service.mContext.getResources().getDimension(R.dimen.indication_height);
        
        //touchX = service.mScreenWidth-indicationWidth;
        
        normalPaint.setColor(Color.parseColor(normalPaintColor));
        normalPaint.setStyle(Paint.Style.FILL);
        normalPaint.setStrokeWidth(8.0f);
        normalPaint.setAntiAlias(true);
        
        
        subtransparentPaint.setColor(Color.parseColor(subtransparentPaintColor));
        subtransparentPaint.setStyle(Paint.Style.FILL);
        subtransparentPaint.setStrokeWidth(2.0f);
        subtransparentPaint.setAlpha(0);
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(indicationWidthDimen, indicationHeightDimen);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // Gionee <gn_by_SCREENRECORDER> <shipeixian> <2017-04-10> add for #78565 begin
        //chenyee zhaocaili 20180425 modify for CSW1703A-1984 begin
        BitmapDrawable drawable = (BitmapDrawable) mService.mResources.getDrawable(R.drawable.hide_indicator);
        Bitmap bitmap = drawable.getBitmap();
        //canvas.drawBitmap(bitmap, indicationWidth/2+1, 0,normalPaint);
        //canvas.drawRect(indicationWidth/4*3+indicationWidth/7, 0, indicationWidth, indicationHeight, normalPaint);
        canvas.drawBitmap(bitmap, 0, 0, normalPaint);
        canvas.drawRect(bitmap.getWidth(), 0, indicationWidth, indicationHeight, normalPaint);
        //chenyee zhaocaili 20180425 modify for CSW1703A-1984 end
        // Gionee <gn_by_SCREENRECORDER> <shipeixian> <2017-04-10> add for #78565 end
        /*canvas.drawRect(0, 0, indicationWidth/2, indicationHeight, subtransparentPaint);
        canvas.drawRoundRect(indicationWidth/2, 0, indicationWidth, indicationHeight, indicationWidth/2, indicationWidth/2, normalPaint);
        canvas.drawRect(indicationWidth/4*3, 0, indicationWidth, indicationHeight, normalPaint);*/
        /*canvas.drawRect(service.mScreenWidth - service.mIndicationWidth,service.mMainLayoutLocationY, 
                service.mScreenWidth, service.mMainLayoutLocationY + service.mIndicationHeight, normalPaint);*/
        if (isFirstDraw) {
            isFirstDraw = false;
            return;
        }
        // service.updateMainLayoutPosition(touchX);
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        action = event.getAction();
        touchRawX = (int)event.getRawX();
        touchRawY = (int)event.getRawY();
        if(action == MotionEvent.ACTION_DOWN){
            return doDownEvent(touchRawX, touchRawY, event);
        }else if (action == MotionEvent.ACTION_MOVE) {
            return doMoveEvent(touchRawX, touchRawY, event);
        }else if (action == MotionEvent.ACTION_UP) {
            return doUpEvent(touchRawX, touchRawY, event);
        }
        return super.onTouchEvent(event);
    }
    
    private boolean doDownEvent(int touchRawX , int touchRawY , MotionEvent event){
        if(isTouchMe(touchRawX, touchRawY)){
            isTouchMe.getAndSet(true);
            touchDownX = touchRawX;
            touchDownY = touchRawY;
        }
        return super.onTouchEvent(event);
    }
    
    private boolean doMoveEvent(int touchRawX, int touchRawY , MotionEvent event){        
        if( ! isTouchMe.get()){
            return true;
        }
        
        if(mService.isMyselfVisible.get()){
            return true;
        }
        
        if(Math.abs(touchDownX - touchRawX) < minMoveDistance && Math.abs(touchDownY - touchRawY) < minMoveDistance){
            return true;
        }
        
        if(isTouchMe.get()){
            if((touchDownX - touchRawX) > Math.abs(touchDownY - touchRawY)){
                if( ! isDragedMeUpOrDown){
                    dragingMeOut();
                    isTouchMe.getAndSet(false);
                    return true;
                }
            }
        }
        if(isTouchMe.get()){
        	//GIONEE 20160920 lixiaohong modify for CR01762055 begin
            //if (isDragingMeUpOrDown(touchRawX, touchRawY)) {
                if(touchRawY <= mService.mMinYlimited || touchRawY >= mService.mMaxYLimited){
                    return true;
                }
                isDragedMeUpOrDown = true;
                mService.mMainLayoutLocationY = touchRawY - mService.mStatusBarHeight;
                if(mService.mMainLayoutLocationY < mService.mMinYlimited) mService.mMainLayoutLocationY = mService.mMinYlimited;
                if(mService.mMainLayoutLocationY > mService.mMaxYLimited) mService.mMainLayoutLocationY = mService.mMaxYLimited;
                mService.updateViewsLocationY(mService.mMainLayoutLocationY);
            //}
            //GIONEE 20160920 lixiaohong modify for CR01762055 end
        }
        return super.onTouchEvent(event);
    }
    
    private boolean doUpEvent(int touchRawX , int touchRawY , MotionEvent event){
        isTouchMe.getAndSet(false);
        touchDownX = 0;
        touchDownY = 0;
        if(touchRawY <= (mService.mMinYlimited)  || touchRawY >= (mService.mMaxYLimited + mService.mIndicationHeight)){
            return true;
        }
        
        if(isDragedMeUpOrDown) {
            isDragedMeUpOrDown = false;
            return true;
        }
        
        if (isDragMeOut) {
            isDragMeOut = false;
            return true;
        }
        return super.onTouchEvent(event);
    }
    
    private boolean isTouchMe(int rawX, int rawY) {
       if ((rawX >= (mService.mScreenWidth - mService.mIndicationWidth)) && (rawY <= (mService.mMainLayoutLocationY + mService.mIndicationHeight+mService.mStatusBarHeight) && rawY >= mService.mMainLayoutLocationY+mService.mStatusBarHeight)){
            return true;
        } else {
            return false;
        }
    }
    
    private boolean isDragingMeUpOrDown(int rawX , int rawY){
        if((rawY > (mService.mMainLayoutLocationY + mService.mIndicationHeight+mService.mStatusBarHeight)) || (rawY < mService.mMainLayoutLocationY+mService.mStatusBarHeight)) {
            return true;
        } else {
            return false;
        }
    }
    
    private void dragingMeOut(){
        isDragMeOut = true;
        mService.showMainLayoutAnimation();
    }
}
