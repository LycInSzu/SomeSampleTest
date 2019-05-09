/*
 * Copyright (C) 2013-2016, Shenzhen Huiding Technology Co., Ltd.
 * All Rights Reserved.
 */

package android.hardware.fingerprint;

interface IGoodixFingerprintDaemonCallback {
    void onDaemonMessage(long devId, int msgId, int cmdId, in byte[] data);
}
