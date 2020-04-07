/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.systemui.screenshot;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.UserManager;
import android.util.Log;
import android.view.WindowManager;
// add by wangjian for YJSQ-117 start
import com.wtk.screenshot.WtkFreeScreenShot;
import android.os.SystemProperties;
import com.android.systemui.R;
// add by wangjian for YJSQ-117 end
import com.android.systemui.SysUIToast;
import android.provider.Settings;
import com.android.systemui.ExtremeModeHelper;

public class TakeScreenshotService extends Service {
    private static final String TAG = "TakeScreenshotService";

    private static GlobalScreenshot mScreenshot;
    // add by wangjian for YJSQ-117 start
    private static WtkFreeScreenShot mWtkFreeScreenShot;
    // add by wangjian for YJSQ-117 end

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            final Messenger callback = msg.replyTo;
            Runnable finisher = new Runnable() {
                @Override
                public void run() {
                    Message reply = Message.obtain(null, 1);
                    try {
                        callback.send(reply);
                    } catch (RemoteException e) {
                    }
                }
            };

            // If the storage for this user is locked, we have no place to store
            // the screenshot, so skip taking it instead of showing a misleading
            // animation and error notification.
            if (!getSystemService(UserManager.class).isUserUnlocked()) {
                Log.w(TAG, "Skipping screenshot because storage is locked!");
                post(finisher);
                return;
            }

            //add for TEJWQE-531 by liyuchong 20200403 begin
            if (isExtremePowerMode()){
                return;
            }
            //add for TEJWQE-531 by liyuchong 20200403 end

            if (mScreenshot == null) {
                mScreenshot = new GlobalScreenshot(TakeScreenshotService.this);
            }

            switch (msg.what) {
                case WindowManager.TAKE_SCREENSHOT_FULLSCREEN:
                    //modified by wangjian for YJSQ-117 start
                    //mScreenshot.takeScreenshot(finisher, msg.arg1 > 0, msg.arg2 > 0);
                    boolean showSuperScreenshot = TakeScreenshotService.this.getResources().getBoolean(R.bool.show_super_screenshot);
                    Log.e("wangjian","showSuperScreenshot = " + showSuperScreenshot);
                    if (showSuperScreenshot){
                        if (mWtkFreeScreenShot == null){
                            mWtkFreeScreenShot = new WtkFreeScreenShot(TakeScreenshotService.this);
                        }

                        mWtkFreeScreenShot.start(() -> {
                            mWtkFreeScreenShot.saveFinish();
                            Message reply = Message.obtain(null, 1);
                            try {
                                callback.send(reply);
                            } catch (RemoteException e) {
                            }
                        });

                    }else{
                        mScreenshot.takeScreenshot(finisher, msg.arg1 > 0, msg.arg2 > 0);
                    }
                    //modified by wangjian for YJSQ-117 end
                    break;
                case WindowManager.TAKE_SCREENSHOT_SELECTED_REGION:
                    mScreenshot.takeScreenshotPartial(finisher, msg.arg1 > 0, msg.arg2 > 0);
                    break;
                default:
                    Log.d(TAG, "Invalid screenshot option: " + msg.what);
            }
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return new Messenger(mHandler).getBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        if (mScreenshot != null) mScreenshot.stopScreenshot();
        return true;
    }
    //add for TEJWQE-531 by liyuchong 20200403 begin
    private boolean isExtremePowerMode(){
        boolean extremeMode = (Settings.Global.getInt(getContentResolver(),"cyee_powermode",ExtremeModeHelper.POWER_MODE_NORMAL) == ExtremeModeHelper.POWER_MODE_EXTREME);
        if(extremeMode){
            notifyNotAllowScreenshot();
        }
        return extremeMode;
    }
    private void notifyNotAllowScreenshot(){
        SysUIToast.showToast(TakeScreenshotService.this, getResources().getString(R.string.cy_forbid_screenshot_tip));
    }
    //add for TEJWQE-531 by liyuchong 20200403 end
}
