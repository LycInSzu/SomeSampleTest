/*
 * Copyright (C) 2013-2016, Shenzhen Huiding Technology Co., Ltd.
 * All Rights Reserved.
 */

package android.hardware.fingerprint;

import android.hardware.fingerprint.IGoodixFingerprintDaemonCallback;
import com.goodix.fingerprint.CmdResult;

interface IGoodixFingerprintDaemon {
    void setNotify(IGoodixFingerprintDaemonCallback callback);
    CmdResult sendCommand(int cmdId, in byte[] param);
}
