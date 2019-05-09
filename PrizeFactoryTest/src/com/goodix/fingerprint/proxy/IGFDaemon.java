/*
 * Copyright (C) 2013-2016, Shenzhen Huiding Technology Co., Ltd.
 * All Rights Reserved.
 */

package com.goodix.fingerprint.proxy;

import com.goodix.fingerprint.proxy.IGFDaemonCallback;
import android.os.RemoteException;
import com.goodix.fingerprint.CmdResult;

public interface IGFDaemon {
    public void setNotify(IGFDaemonCallback callback);
    public CmdResult sendCommand(int cmdId, byte[] param) throws RemoteException;
}
