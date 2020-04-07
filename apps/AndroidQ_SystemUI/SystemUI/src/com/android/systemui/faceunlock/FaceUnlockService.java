/*
 * Copyright (C) 2014 The Android Open Source Project
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
 * limitations under the License
 */

package com.android.systemui.faceunlock;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Debug;
import android.os.IBinder;
import android.os.Process;
import android.os.SystemProperties;
import android.util.Log;

import com.android.internal.policy.IFaceUnlockService;
import com.android.internal.policy.IFaceUnlockCallback;
import com.android.systemui.SystemUIApplication;
import com.android.systemui.faceunlock.FaceUnlockUtil;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class FaceUnlockService extends Service {
    static final String TAG = "FaceUnlockService";
    static final String PERMISSION = android.Manifest.permission.CONTROL_KEYGUARD;

    private FaceUnlockUtil mFaceUnlock = null;

    @Override
    public void onCreate() {
        if (FaceUnlockUtil.isFaceUnlockSupport()) {
            Log.d(TAG, "onCreate");
            mFaceUnlock = FaceUnlockUtil.getInstance();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        return mBinder;
    }

    void checkPermission() {
        // Avoid deadlock by avoiding calling back into the system process.
        if (Binder.getCallingUid() == Process.SYSTEM_UID) return;

        
    }

    private final IFaceUnlockService.Stub mBinder = new IFaceUnlockService.Stub() {

        @Override // Binder interface
        public void doUnlock() {
            Log.d(TAG, "doUnlock mFaceUnlock = " + mFaceUnlock);
            if (mFaceUnlock != null) {
                checkPermission();
                // TODO: Remove wakeup
                mFaceUnlock.doUnlock();
            }
        }

        @Override
        public void registerFaceUnlockCallback(IFaceUnlockCallback callback) {
            Log.d(TAG, "registerFaceUnlockCallback");
            if (mFaceUnlock != null) {
                checkPermission();
                mFaceUnlock.registerFaceUnlockCallback(callback);
            }
        }

        @Override
        public void showTipsOrAnimation(int reason, String tips) {
            Log.d(TAG, "showUnLockScreen");
            if (mFaceUnlock != null) {
                checkPermission();
                mFaceUnlock.showTipsOrAnimation(reason,tips);
            }
        }
    };
}

