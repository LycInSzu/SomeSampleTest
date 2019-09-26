package com.cydroid.screenrecorder.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.LinearLayout;
import com.cydroid.screenrecorder.ScreenRecorderService;

public class MyLinearLayout extends LinearLayout {
	
	public static final String TAG = "MyLinearLayout";
	public ScreenRecorderService myService = null;
	private int action;
	
	public MyLinearLayout(Context context) {
		super(context);
	}
	
	public MyLinearLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if(event.getKeyCode() == KeyEvent.KEYCODE_BACK){
		    if( !myService.isRecordingNow.get()){
		        myService.exitScreenRecorder(false);
		        return true;
            }
		}
		return super.dispatchKeyEvent(event);
	}
	
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
	    action = event.getAction();
	    if(action == MotionEvent.ACTION_DOWN){
	    }else if (action == MotionEvent.ACTION_MOVE) {
	       // myService.updateMainLayoutPosition((int)event.getX());
        }else if (action == MotionEvent.ACTION_UP) {
        }
	    
	    return super.onTouchEvent(event);
	}
	
	public void setMyService(ScreenRecorderService myService){
		this.myService = myService;
	}
	
}
