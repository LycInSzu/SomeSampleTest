/*
 * Copyright (C) 2013-2016, Shenzhen Huiding Technology Co., Ltd.
 * All Rights Reserved.
 */

package com.goodix.fingerprint.proxy;

import binder.*;
import android.util.Log;

public class DaemonManager {
    private static final String TAG= "DaemonManager";
    private static DaemonManager mInstance;
    private DaemonProxyBase mDaemonProxy = null;

    private DaemonManager() {
        mDaemonProxy = new DaemonProxy();
    }

    public static DaemonManager getInstance() {
        Log.d(TAG, "DaemonManager");
        if (mInstance == null)
        {
            mInstance = new DaemonManager();
        }
        return mInstance;
    }

    public IGFDaemon getGoodixFingerprintDaemon(IGFDaemonCallback callback) {
        return mDaemonProxy.getGoodixFingerprintDaemon(callback);
    }

    public IGFDaemonFido getGoodixFingerprintDaemonFido(IGFDaemonCallback callback) {
        return mDaemonProxy.getGoodixFingerprintDaemonFido(callback);
    }

}

