package com.mediatek.camera.feature.setting.grid;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import com.mediatek.camera.R;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;

public class GridLineView extends View {

	private static final LogUtil.Tag TAG = new LogUtil.Tag(GridLineView.class.getSimpleName());
	Paint paint;
	int screenWidth;
	int screenHeight;
	
	private int pictureRotaionType;
	private int mMarginBottom;
	
	//prize-add for bangs screen-beign
	//private boolean isScreenBangs = android.os.SystemProperties.get("ro.pri.bangs.screen", "0").equals("1");
	//private int mBangsHeight = 72;
	//prize-add for bangs screen-end
	private int mStartTop = 0;
	private int mEndBottom = 0;
	private int mPreviewHeight = mEndBottom - mStartTop;
	private float mPreviewRatio;
	
	public GridLineView(Context context) {
		super(context);
		init(context);
	}

	public GridLineView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	private void init(Context context) {
		paint = new Paint();
		paint.setColor(Color.WHITE);
		paint.setStrokeWidth(context.getResources().getDimensionPixelSize(R.dimen.gridline_strokewidth));
		paint.setAlpha(0x77);
		DisplayMetrics metric = new DisplayMetrics();
		WindowManager wm = (WindowManager) getContext()
				.getSystemService(Context.WINDOW_SERVICE);
		wm.getDefaultDisplay().getRealMetrics(metric);
		screenWidth = metric.widthPixels;
		screenHeight = metric.heightPixels;
		mMarginBottom = screenHeight - (int)getContext().getResources().getDimension(R.dimen.shutter_group_height);
	}

	protected void onDraw(Canvas canvas) {
		canvas.drawColor(Color.TRANSPARENT);
		if (mStartTop == 0 || mEndBottom == 0){
			LogHelper.d(TAG,"[onDraw]  return...");
			return;
		}
		onDrawLine(canvas);
	}

	protected void onDrawLine(Canvas canvas){
		//prize-add for bangs screen-beign
		/*if(isScreenBangs){
		//	leftMargin -= mBangsHeight/2; 
		}*/
		//prize-add for bangs screen-end
		int verticalOffset = mPreviewHeight / 3;
		int horizontalOffset = screenWidth / 3;
		for (int i = 0; i < 2; i++) {
			canvas.drawLine(0, mStartTop + verticalOffset, screenWidth, mStartTop + verticalOffset, paint);//horizontal line
			canvas.drawLine(horizontalOffset, mStartTop, horizontalOffset, mEndBottom, paint);//vertical line
			LogHelper.d(TAG, "[onDrawLine]"
					+ "  vertz :" + verticalOffset
					+ "  horizontalOffset :" + horizontalOffset);
			verticalOffset += mPreviewHeight / 3;
			horizontalOffset += screenWidth / 3;
		}
	}

	public void setPictureRotaionType(int pictureRotaionType){
		this.pictureRotaionType = pictureRotaionType;
		invalidate();
	}

	/*PRIZE-12383-wanzhijuan-2016-03-02-start*/
	public void setPreviewSize(int width, int height) {
		mPreviewRatio = (float) width/height;
		LogHelper.d(TAG, "[setPreviewSize], width:" + width +"  height = " + height + "  mPreviewRatio = "+mPreviewRatio);
		if (width != 0 && height != 0) {
			invalidate();
		}
	}
	/*PRIZE-12383-wanzhijuan-2016-03-02-end*/

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return false;
	}

	public void setPreviewTopBottom(int top, int bottom) {
		LogHelper.d(TAG, "[setPreviewTopBottom], top:" + top +"  bottom = " + bottom);
		mPreviewHeight = bottom - top;
		mStartTop = top;
		mEndBottom = bottom;
		invalidate();
	}
}



























