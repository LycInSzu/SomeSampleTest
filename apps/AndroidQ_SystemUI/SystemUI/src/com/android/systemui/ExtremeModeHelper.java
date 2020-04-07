package com.android.systemui;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import com.android.systemui.Dependency;
import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
/**
 * @author lixh
 * @since 20180912
 */
public class ExtremeModeHelper {
	
	static final String EXTREME_MODE = "cyee_powermode";
	private final ArrayList<WeakReference<Callback>> mCallbacks = new ArrayList<>(1);
	private int mMode;
	private final Handler bgHandler;
	private final ContentObserver mObserver; 
    private Context mContext;
    private boolean mListening;

	public static final int POWER_MODE_NORMAL = 0;
	public static final int POWER_MODE_GENERAL = 1;
	public static final int POWER_MODE_EXTREME = 2;
    
	public ExtremeModeHelper(Context context){
		mContext = context;
		bgHandler = new Handler(Dependency.get(Dependency.BG_LOOPER));
		bgHandler.post(this::updateMode);
		mObserver = new ContentObserver(bgHandler) {
	        @Override
	        public void onChange(boolean selfChange) {
	        	int mode = getCurrentMode();
	        	boolean shouldNotify = (mode == POWER_MODE_EXTREME || mMode == POWER_MODE_EXTREME);
	        	mMode = mode;
	        	if(shouldNotify){
				    dispatchCallbacks();
	        	}
	        }
	    };
	    if(!mListening){
	        mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor(EXTREME_MODE),true,mObserver);
	        mListening = true;
	    }
	}
	
	private int getCurrentMode(){
		return Settings.Global.getInt(mContext.getContentResolver(), EXTREME_MODE, POWER_MODE_NORMAL);
	}
	
	private void updateMode(){
		mMode = getCurrentMode();
	}
	
	public void addCallback(Callback callback){
		synchronized (mCallbacks) {
			cleanUpCallback(callback);
			mCallbacks.add(new WeakReference<>(callback));
			callback.notifyExtremeModeChanged(isExtremeMode());
		}
	}
	
	public boolean isExtremeMode(){
		return mMode == POWER_MODE_EXTREME;
	}
	
	public boolean isNormalMode(){
		return mMode == POWER_MODE_NORMAL;
	}
	
	public void removeCallback(Callback callback){
		synchronized (mCallbacks) {
			cleanUpCallback(callback);
		}
	}
	
	private void cleanUpCallback(Callback callback){
		for (int i = mCallbacks.size() - 1; i >= 0; i--) {
			Callback found = mCallbacks.get(i).get();
			if (found == null || found == callback) {
				mCallbacks.remove(i);
            }
		}
	}
	
	private void dispatchCallbacks(){
		synchronized (mCallbacks) {
			final int N = mCallbacks.size();
            boolean cleanup = false;
            for (int i = 0; i < N; i++) {
            	Callback c = mCallbacks.get(i).get();
            	if(c!=null){
            		c.notifyExtremeModeChanged(isExtremeMode());
            	}else{
            		cleanup = true;
            	}
            }
            if(cleanup){
            	cleanUpCallback(null);
            }
		}
	}
	
	public void destroy(){
		if(mListening){
		    mContext.getContentResolver().unregisterContentObserver(mObserver);
		    mListening = false;
		}
	}
	
	public interface Callback{
		void notifyExtremeModeChanged(boolean isExtremeMode);
	}
}
