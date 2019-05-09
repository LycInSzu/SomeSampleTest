/*
 * Copyright (C) 2013-2016, Shenzhen Huiding Technology Co., Ltd.
 * All Rights Reserved.
 */

package com.goodix.fingerprint.proxy;

public interface IGFDaemonCallback extends IDaemonDiedCallback {
    void onDaemonMessage(long devId, int msgId, int cmdId, byte[] data);
}

