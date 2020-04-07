package com.gionee.framework.component;

import android.app.Application;

import com.cydroid.note.common.Log;

public class BaseApplication extends Application {

    public static final int FLAG_OPEN_LOG_TO_FILE = 0x00000000;
    public static final int FLAG_OPEN_LOG_TO_LOGCAT = 0x00000001;
    public static final int MASK_LOG_FLAG = 0x00000001;
    static BaseApplication sApplication;

    public static void setsApplication(BaseApplication sApplication) {
        BaseApplication.sApplication = sApplication;
    }

    private static final String TAG = "BaseApplication";

    public BaseApplication() {
        super();
        setsApplication(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    
    /*
    public final void enableLog(int status) {
        Logger.enableLog(status);
    }
	*/

}
