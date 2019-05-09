/*
 * Copyright (C) 2013-2016, Shenzhen Huiding Technology Co., Ltd.
 * All Rights Reserved.
 */

package com.goodix.fingerprint.proxy;

import android.os.IBinder;

public abstract class DaemonProxyBase {
    protected Daemon mGFDaemon = null;
    protected Daemon mGFDaemonFido = null;

    public IGFDaemon getGoodixFingerprintDaemon(IGFDaemonCallback callback) {
        if (mGFDaemon == null) {
            mGFDaemon = getGFDaemonImp(callback);
        }
        if (mGFDaemon.getService() == null) {
            return null;  // return null, service not ready
        }
        return (IGFDaemon)mGFDaemon;
    }

    public IGFDaemonFido getGoodixFingerprintDaemonFido(IGFDaemonCallback callback) {
        if (mGFDaemonFido == null) {
            mGFDaemonFido = getGFDaemonFidoImp(callback);
        }
        if (mGFDaemonFido.getService() == null) {
            return null;  // return null, service not ready
        }
        return (IGFDaemonFido)mGFDaemonFido;
    }

    public abstract class Daemon {
        public abstract Object getService();
    }

    protected abstract Daemon getGFDaemonImp(IGFDaemonCallback callback);
    protected abstract Daemon getGFDaemonFidoImp(IGFDaemonCallback callback);
}

