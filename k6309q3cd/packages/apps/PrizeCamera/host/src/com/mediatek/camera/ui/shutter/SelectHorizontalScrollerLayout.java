package com.mediatek.camera.ui.shutter;

import android.content.Context;
import android.support.v4.view.ViewConfigurationCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.HorizontalScrollView;

import com.mediatek.camera.CameraActivity;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class SelectHorizontalScrollerLayout extends HorizontalScrollView {

	private static final LogUtil.Tag TAG = new LogUtil.Tag(SelectHorizontalScrollerLayout.class.getSimpleName());

    private int mSelectIndex = -1;
    private HorizontalScrollLayoutAdapter mAdapter;
    private OnItemClickListener mOnItemClickListener;
    private Map<View, Integer> mViewPos = new HashMap<View, Integer>();
    protected final HorizontalScrollStrip mScrollStrip;

    private boolean isNeedReload = true;
    /*prize-modify-bugid:67603 Limit mode switching interval-xiaoping-20181114-start*/
	private long mLastClickTime;
	/*prize-modify-bugid:67603 Limit mode switching interval-xiaoping-20181114-end*/
	private Context mContext;
	private OnClickListener mClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {

			/*prize-modify-bugid:67603 Limit mode switching interval-xiaoping-20181114-start*/
			/*prize-modify fixbug:[70734][70006][69414][69396][69295]-huangpengfei-2019-01-14-start*/
			boolean isCaptureOrVideo = ((CameraActivity)mContext).getAppUi().isCaptureOrVideo();
			LogHelper.i(TAG, "[onClick] isEnabled() = " + isEnabled() + " mCanClick = " + mCanClick +",isCaptureOrVideo: "+isCaptureOrVideo);
			if(!isEnabled() || !mCanClick || isCaptureOrVideo/*(System.currentTimeMillis() - mLastScrollTime < 1000)*/){
				//LogHelper.w(TAG,"SelectHorizontalScrollerLayout enable false or Two clicks time is: "+(System.currentTimeMillis() - mLastScrollTime)+"ms,less than 1s,return");
				return;
			}
			/*prize-modify fixbug:[70734][70006][69414][69396][69295]-huangpengfei-2019-01-14-end*/
			int index = mViewPos.get(v);
			LogHelper.i(TAG, "[onClick] index=" + index + " mSelectIndex=" + mSelectIndex);
			if (index != mSelectIndex) {
				//mLastClickTime = System.currentTimeMillis();
				/*prize-modify-bugid:67603 Limit mode switching interval-xiaoping-20181114-end*/
				scrollToCenter(index);
			}
		}
	};

	public interface OnItemClickListener {
		void onItemClick(int pos);
	}

    public SelectHorizontalScrollerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
//        setBackgroundColor(Color.TRANSPARENT);
     // Disable the Scroll Bar
		mContext = context;
     	setHorizontalScrollBarEnabled(false);
     	setOverScrollMode(OVER_SCROLL_NEVER);
		this.mScrollStrip = new HorizontalScrollStrip(context, attrs, false);
		setFillViewport(false);
		addView(mScrollStrip, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		ViewConfiguration configuration = ViewConfiguration.get(context);
		mTouchSlop = ViewConfigurationCompat.getScaledPagingTouchSlop(configuration);
		LogHelper.i(TAG, "SelectHorizontalScrollerLayout mTouchSlop=" + mTouchSlop);
    }

    public SelectHorizontalScrollerLayout(Context context, AttributeSet attrs, HorizontalScrollLayoutAdapter adapter) {
        this(context, attrs);
        mContext = context;
        setAdapter(adapter);
    }

    public void setAdapter(HorizontalScrollLayoutAdapter adapter) {
    	mAdapter = adapter;
    	initView();
    }

    private void initView() {
    	int count = mAdapter.getCount();
    	mScrollStrip.removeAllViews();
    	mSelectIndex = -1;
    	mViewPos.clear();
    	isNeedReload = true;
    	//smoothScrollTo(0, 0);
    	for (int i = 0; i < count; i++) {
    		View childView = mAdapter.getView(i, null, this);
    		mViewPos.put(childView, i);
    		childView.setOnClickListener(mClickListener);
    		mScrollStrip.addView(childView);
    	}
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
    	mOnItemClickListener = listener;
    }

    @Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		if (mScrollStrip.getChildCount() > 0) {
			View firstTab = mScrollStrip.getChildAt(0);
			View lastTab = mScrollStrip.getChildAt(mScrollStrip.getChildCount() - 1);
			int start = (w - Utils.getMeasuredWidth(firstTab)) / 2;
//					- Utils.getMarginStart(firstTab);
			int end = (w - Utils.getMeasuredWidth(lastTab)) / 2;
//					- Utils.getMarginEnd(lastTab);
			mScrollStrip.setMinimumWidth(mScrollStrip.getMeasuredWidth());
			setPaddingRelative(start, getPaddingTop(), end, getPaddingBottom());
//			ViewCompat.setPaddingRelative(this, start, getPaddingTop(), end,
//					getPaddingBottom());
			setClipToPadding(false);
			LogHelper.i(TAG, "onSizeChanged w=" + w + " start=" + start + " end=" + end);
		}
	}

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
    	LogHelper.i(TAG, "onLayout changed=" + changed);
    	super.onLayout(changed, l, t, r, b);
        if ((changed || isNeedReload) && mSelectIndex != -1) {
        	isNeedReload = false;
        	scrollToCenter(mSelectIndex);
        }
    }



	private void scrollToCenter(int tabIndex) {
		final int tabStripChildCount = mScrollStrip.getChildCount();
		LogHelper.i(TAG, "scrollToCenter tabStripChildCount=" + tabStripChildCount);
		if (tabStripChildCount == 0 || tabIndex < 0
				|| tabIndex >= tabStripChildCount) {
			return;
		}
		LogHelper.i(TAG, "scrollToTab tabIndex=" + tabIndex + " scrollX=" + getScrollX());
		View selectedTab = mScrollStrip.getChildAt(tabIndex);
		View firstTab = mScrollStrip.getChildAt(0);
		int first = Utils.getWidth(firstTab);
//				+ Utils.getMarginStart(firstTab);
		int selected = Utils.getWidth(selectedTab);
//				+ Utils.getMarginStart(selectedTab);
		int x = Utils.getStart(selectedTab);
//				- Utils.getMarginStart(selectedTab);
		LogHelper.i(TAG, "scrollToCenter Utils.getStart(selectedTab) = "+x+",getScrollX()="+getScrollX());
		x -= (first - selected) / 2;
		LogHelper.i(TAG, "scrollToCenter tabIndex=" + tabIndex + " x=" + x +",first="+first+",selected="+selected);
		if (mSelectIndex != tabIndex) {
			mSelectIndex = tabIndex;
			if (mOnItemClickListener != null) {
				mOnItemClickListener.onItemClick(mSelectIndex);
			}
		}
		setSelect(mSelectIndex);
//		scrollTo(x, 0);
//		smoothScrollTo(x, 0);
		/*prize-modify-bugid:44423 adjust roactive text spacing-xiaoping-20171012-start*/
//		if (tabStripChildCount == 3) {
////			x = isLanguageOfZH() ? x - 24 : x + 24;
//		}
		smoothScrollTo(x, 0);
		/*prize-modify-bugid:44423 adjust roactive text spacing-xiaoping-20171012-end*/
		refreshView(mSelectIndex);
		setEnabled(false);//prize-fixbug[70525]-huangpengfei-2019-01-08
	}

	@Override
	public void setFocusable(boolean focusable) {
    	LogHelper.i("tangan","setfocus:"+focusable);
		super.setFocusable(focusable);
	}

	private void setSelect(int index) {
		for (int i = 0, count = mScrollStrip.getChildCount(); i < count; i++) {
			View childView = mScrollStrip.getChildAt(i);
			if (i == index) {
				childView.setSelected(true);
			} else {
				childView.setSelected(false);
			}
		}
	}

	private boolean mIsChange;
	private float mDownX;
	private float mXMove;
	private int mTouchSlop;

	@Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
		/*prize-xuchunming-20180522-bugid:58575-start*/
		/*if(isEnabled() == false){
			return true;
		}*/
		/*prize-xuchunming-20180522-bugid:58575-end*/
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
            	mDownX = ev.getRawX();
				mIsChange = false;
				LogHelper.i(TAG, "onInterceptTouchEvent mDownX=" + mDownX);
                break;
            case MotionEvent.ACTION_MOVE:
                mXMove = ev.getRawX();
                float diff = Math.abs(mXMove - mDownX);
				LogHelper.i(TAG, "onInterceptTouchEvent diff=" + diff + " mTouchSlop=" + mTouchSlop);
                if (diff > mTouchSlop) {
                    return true;
                }
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
    	if(isEnabled() == false){
    		return true;
		}
		/*prize-add fixbug:[70734][70006][69414][69396][69295]-huangpengfei-2019-01-14-start*/
		if (!mCanClick) {
			LogHelper.w(TAG,"[onTouchEvent]  return...");
			return true;
		}
		/*prize-add fixbug:[70734][70006][69414][69396][69295]-huangpengfei-2019-01-14-start*/
    	float currentX = event.getRawX();
        switch (event.getAction()) {
        	case MotionEvent.ACTION_DOWN:
        		mIsChange = false;
        		mDownX = event.getRawX();
				LogHelper.i(TAG, "onTouchEvent down");
        		break;
        	case MotionEvent.ACTION_MOVE:
        		if (!mIsChange && mScrollStrip.getChildAt(mSelectIndex) != null) {
        			int width = mScrollStrip.getChildAt(mSelectIndex).getWidth();
            		if ((mSelectIndex < mScrollStrip.getChildCount() - 1) && (mDownX - currentX > (width / 2))) {
            			mIsChange = true;
            			scrollToCenter(mSelectIndex + 1);
            		} else if ((mSelectIndex > 0) && (currentX - mDownX > (width / 2))) {
            			mIsChange = true;
            			scrollToCenter(mSelectIndex - 1);
            		}
					LogHelper.i(TAG, "onTouchEvent move mSelectIndex=" + mSelectIndex + " mDownX=" + mDownX + " currentX=" + currentX + " width=" + width);
        		}
        		break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
				LogHelper.i(TAG, "up onTouchEvent mSelectIndex=" + mSelectIndex + " mDownX=" + mDownX + " currentX=" + currentX + " mIsChange=" + mIsChange);
                if (!mIsChange) {
                	if ((mSelectIndex < mScrollStrip.getChildCount() - 1) && (mDownX - currentX > 10)) {
            			mIsChange = true;
            			scrollToCenter(mSelectIndex + 1);
            		} else if ((mSelectIndex > 0) && (currentX - mDownX > 10)) {
            			mIsChange = true;
            			scrollToCenter(mSelectIndex - 1);
            		}
                }
                mIsChange = false;
            	break;
        }
        return true;
    }

    @Override
    public void fling(int velocityX) {
        super.fling(velocityX);
    }

	public void setSelectIndex(int index) {
		// TODO Auto-generated method stub
		LogHelper.i(TAG, "setSelectIndex index=" + index + " mSelectIndex=" + mSelectIndex);
		if (index != mSelectIndex) {
			scrollToCenter(index);
		}
	}

	public int getCount(){
		if(mAdapter != null ){
			return mAdapter.getCount();
		}
		return -1;
	}

	public void refreshView(int index) {
		for (int i = 0; i < mAdapter.getCount(); i++) {
			View view = mScrollStrip.getChildAt(i);
			if (mAdapter.getCount() ==3 && index == 0) {
				LogHelper.i(TAG,"left: "+view.getLeft()+",right: "+view.getRight()+",bottom: "+view.getBottom()+",top: "+view.getTop());
				/*prize-modify-adjust roactive text spacing-xiaoping-20171012-start*/
//				if (isLanguageOfZH()) {
//					mScrollStrip.setPaddingRelative(24,0,0,0);
//				}
				/*prize-modify-adjust proactive text spacing-xiaoping-20171012-end*/
			}
			//view.setScaleX((float) (1-(Math.abs(i-index)) *(0.07)));//prize-remove-huangpengfei-2018-12-19
			requestLayout();
		}
	}

	/*prize-add-judge the current input method-xiaoping-20171219-start*/
	private boolean isLanguageOfZH() {
		Locale locale = getContext().getResources().getConfiguration().locale;
		LogHelper.i(TAG,"locale.getLanguage(): "+locale.getLanguage());
		return locale.getLanguage().endsWith("zh");
	}
	/*prize-add-judge the current input method-xiaoping-20171219-start*/

	@Override
	public void setVisibility(int visibility) {
		super.setVisibility(visibility);
	}

	@Override
	public void setEnabled(boolean enabled) {
		LogHelper.i(TAG,"enabled: "+enabled);
		super.setEnabled(enabled);
		if (enabled) {
			LogHelper.i(TAG,"enable: "+enabled+""+android.util.Log.getStackTraceString(new Throwable()));
		}
	}

	/*prize-add fixbug:[70734][70006][69414][69396][69295]-huangpengfei-2019-01-14-start*/
	private boolean mCanClick = true;

	public void setShutterSwitchFinish(boolean finish) {
		LogHelper.i(TAG, "[setShutterSwitchFinish] finish =" + finish);
		mCanClick = finish;
	}
	/*prize-add fixbug:[70734][70006][69414][69396][69295]-huangpengfei-2019-01-14-end*/
}
