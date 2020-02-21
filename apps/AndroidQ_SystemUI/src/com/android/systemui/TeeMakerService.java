package com.android.systemui;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Color;
import android.os.IBinder;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.Gravity;
import android.widget.TextView;
import android.util.Log;

public class TeeMakerService extends Service {
    
    private final String TAG = "TeeMakerService";
	private WindowManager mWindowManager ;
	private TextView warter;
	private boolean mAdded = false;
	
	@Override
	public void onCreate() {
		super.onCreate();
		mWindowManager = (WindowManager)this.getSystemService(Context.WINDOW_SERVICE);
		mAdded = false;
        Log.e(TAG,"onCreate");
	}
	
	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
        Log.e(TAG,"onStart");
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
        if(!mAdded){
            WindowManager.LayoutParams mParams = new WindowManager.LayoutParams();
            mParams.height = LayoutParams.WRAP_CONTENT;
            mParams.width = LayoutParams.WRAP_CONTENT;
            mParams.gravity = Gravity.TOP | Gravity.RIGHT;
            mParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE 
                            | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
            mParams.type = WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG;
            mParams.format = PixelFormat.TRANSPARENT;
            warter = new TextView(this);
            warter.setText("Not Verify");
            warter.setTextColor(Color.RED);
            warter.setTextSize(20.0f);
            mWindowManager.addView(warter,mParams);
            mAdded = true;
        }
		return super.onStartCommand(intent, flags, startId);
	}
	
	@Override
	public void onDestroy() {
        Log.e(TAG,"onDestroy ");
		if(mAdded && null!=warter){
			mWindowManager.removeView(warter);
			warter = null;
		}
		super.onDestroy();
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}
