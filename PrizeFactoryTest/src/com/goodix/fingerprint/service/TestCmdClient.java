/*
 * Copyright (C) 2013-2017, Shenzhen Huiding Technology Co., Ltd.
 * All Rights Reserved.
 */

package com.goodix.fingerprint.service;

import com.goodix.fingerprint.Constants;
import com.goodix.fingerprint.BaikalConstants;
import com.goodix.fingerprint.CmdResult;
import com.goodix.fingerprint.utils.FingerprintUtils;
import com.goodix.fingerprint.proxy.IGFDaemon;
import android.content.Context;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

public abstract class TestCmdClient extends ClientMonitor {

    private static final int INVALID_CMD_ID = 9999999;
    private static final int ERROR_TESTCMD_DEFINED = ERROR_CLIENT_DEFINED + 10;
    private static final int ERROR_INVALID_CMD_ID = ERROR_TESTCMD_DEFINED + 1;

    private int mCmdId = INVALID_CMD_ID;
    private byte[] mParam = null;
    private boolean mInitiatedByClient = false;

    public TestCmdClient(Context context, IBinder token, IGoodixFingerprintCallback receiver,
            int userId, boolean restricted, String owner, int cmdId, byte[] param, boolean initiatedByClient) {
        super(context, token, receiver, userId, restricted, owner);
        this.mCmdId = cmdId;
        this.mParam = param;
        this.mInitiatedByClient = initiatedByClient;
    }

    @Override
    public int start() {
        IGFDaemon daemon = getGoodixFingerprintDaemon();
        if (daemon == null) {
            Log.w(TAG, "startTestCmd: no goodixfingeprintd!");
            return ERROR_ESRCH;
        }
        if (this.mCmdId == INVALID_CMD_ID) {
            Log.w(TAG, "startTestCmd: invalid cmdId!");
            return ERROR_INVALID_CMD_ID;
        }

        try {
            CmdResult result = daemon.sendCommand(this.mCmdId, this.mParam);
            if (result.mResultCode!= 0) {
                Log.w(TAG, "startTestCmd failed, result=" + result);
                onError(GoodixFingerprintManager.FINGERPRINT_ERROR_HW_UNAVAILABLE);
                return result.mResultCode;
            }
        } catch (RemoteException e) {
            Log.e(TAG, "startTestCmd failed", e);
            return 0; //return 0 to terminate this client
        }
        return 0; // success
    }

    @Override
    public int stop(boolean initiatedByClient) {
        CmdResult result = null;

        if (initiatedByClient) {
            IGFDaemon daemon = getGoodixFingerprintDaemon();
            if (daemon == null) {
                Log.w(TAG, "stopTestCmd: no goodixfingeprintd!");
                return ERROR_ESRCH;
            }

            try {
                result = daemon.sendCommand(Constants.CMD_TEST_CANCEL, null);
                if (result.mResultCode != 0) {
                    Log.w(TAG, "stopTestCmd failed, result=" + result);
                }
            } catch (RemoteException e) {
                Log.e(TAG, "stopTestCmd failed", e);
                result.mResultCode = 0; //return 0 to terminate this client
            }
            onError(GoodixFingerprintManager.FINGERPRINT_ERROR_CANCELED);
            return result.mResultCode;
        }
        return 0;
    }

    /**
     * Gets the fingerprint daemon from the cached state in the container class.
     */
    public abstract IGFDaemon getGoodixFingerprintDaemon();

    @Override
    public boolean onEnrollResult(int fingerId, int rem) {
        Log.w(TAG, "onEnrollResult() called for TestCmd!");
        return sendEnrollResult(fingerId, rem);
    }

    @Override
    public boolean onAuthenticated(int fingerId) {
        Log.w(TAG, "onAuthenticated() called for TestCmd!");
        return sendAuthenticated(fingerId);
    }

    @Override
    public boolean onRemoved(int fingerId) {
        Log.w(TAG, "onRemoved() called for TestCmd!");
        return sendRemoved(fingerId);
    }

    @Override
    public boolean onEnumerationResult(int fingerId) {
        Log.w(TAG, "onEnumerationResult() called for TestCmd!");
        return true;
    }

    @Override
    public boolean onTestCmdFinish(int chipSeries, int cmdId, byte[] result) {
        Log.w(TAG, "onTestCmdFinish() called for TestCmd!");
        boolean removeClient = false;
        removeClient = sendTestCmdFinish(chipSeries, cmdId, result);
        switch (cmdId) {
            case BaikalConstants.CMD_TEST_NOISE:
            case BaikalConstants.CMD_TEST_K_B_CALIBRATION:
            case BaikalConstants.CMD_TEST_PERFORMANCE_TESTING:
            case BaikalConstants.CMD_TEST_LOCATION_CIRCLE_CALIBRATION:
                removeClient = false;
                break;
            default:
                break;
        }
        return removeClient;
    }

    @Override
    public boolean onAuthenticateFido(int fingerId, byte[] uvt) {
        Log.w(TAG, "onAuthenticateFido() called for TestCmd!");
        return true;
    }

    /*
     * @return true if we're done.
     */
    private boolean sendEnrollResult(int fpId, int remaining) {
        IGoodixFingerprintCallback receiver = getReceiver();
        if (receiver == null) {
            return true; // client not listening
        }
        FingerprintUtils.vibrateFingerprintSuccess(getContext());
        try {
            if (mInitiatedByClient) {
                receiver.onEnrollResult(fpId, remaining);
            }
            return remaining == 0;
        } catch (RemoteException e) {
            Log.w(TAG, "Failed to notify EnrollResult:", e);
            return true;
        }
    }

    /*
     * @return true if we're done.
     */
    private boolean sendRemoved(int fingerId) {
        IGoodixFingerprintCallback receiver = getReceiver();
        if (receiver == null) {
            return true; // client not listening
        }
        try {
            if (mInitiatedByClient) {
                receiver.onRemoved(fingerId);
            }
            return fingerId == 0;
        } catch (RemoteException e) {
            Log.w(TAG, "Failed to notify Removed:", e);
            return false;
        }
    }

    /*
     * @return true if we're done.
     */
    private boolean sendTestCmdFinish(int chipSeries, int cmdId, byte[] result) {
        IGoodixFingerprintCallback receiver = getReceiver();
        if (receiver == null) {
            return true; // client not listening
        }
        try {
            if (mInitiatedByClient) {
                receiver.onTestCmd(chipSeries, cmdId, result);
            }
            return true;
        } catch (RemoteException e) {
            Log.w(TAG, "Failed to notify TestCmdFinish:", e);
            return false;
        }
    }

    /*
     * @return true if we're done.
     */
    private boolean sendAuthenticated(int fpId) {
        IGoodixFingerprintCallback receiver = getReceiver();
        boolean result = false;
        boolean authenticated = fpId != 0;
        if (receiver != null) {
            try {
                if (!authenticated) {
                    if (mInitiatedByClient) {
                        receiver.onAuthenticationFailed();
                    }
                    FingerprintUtils.vibrateFingerprintError(getContext());
                    result = false;
                } else {
                    if (mInitiatedByClient) {
                        receiver.onAuthenticationSucceeded(fpId);
                    }
                    FingerprintUtils.vibrateFingerprintSuccess(getContext());
                    result = true;
                }
            } catch (RemoteException e) {
                Log.w(TAG, "Failed to notify Authenticated:", e);
                result = true; // client failed
            }
        } else {
            result = true; // client not listening
        }
        return result;
    }

    /*
     * @return true if we're done.
     */
    private boolean sendError(int error) {
        IGoodixFingerprintCallback receiver = getReceiver();
        if (receiver != null) {
            try {
                if (mInitiatedByClient) {
                    receiver.onError(error);
                }
            } catch (RemoteException e) {
                Log.w(TAG, "Failed to invoke sendError:", e);
            }
        }
        return true; // errors always terminate progress
    }

}
