/*
 * Copyright (C) 2013-2017, Shenzhen Huiding Technology Co., Ltd.
 * All Rights Reserved.
 */

package com.goodix.fingerprint.service;

import com.goodix.fingerprint.Constants;

import android.content.Context;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import com.goodix.fingerprint.proxy.IGFDaemonFido;

public abstract class AuthenticateFidoClient extends ClientMonitor {

    private static final int ERROR_AUTHENTICATEFIDO_DEFINED = ERROR_CLIENT_DEFINED + 20;
    private static final int ERROR_INVALID_AAID = ERROR_AUTHENTICATEFIDO_DEFINED + 1;
    private static final int ERROR_INVALID_FINAL_CHALLENGE = ERROR_AUTHENTICATEFIDO_DEFINED + 2;

    private byte[] mAaid = null;
    private byte[] mFinalChallenge = null;

    public AuthenticateFidoClient(Context context, IBinder token,
            IGoodixFingerprintCallback receiver, int userId, boolean restricted, String owner,
            byte[] aaid, byte[] finalChallenge) {
        super(context, token, receiver, userId, restricted, owner);
        this.mAaid = aaid;
        this.mFinalChallenge = finalChallenge;
    }

    @Override
    public int start() {
        IGFDaemonFido daemon = getGoodixFingerprintDaemonFido();
        if (daemon == null) {
            Log.w(TAG, "startAuthenticateFido: no goodixfingeprintd!");
            return ERROR_ESRCH;
        }
        if (this.mAaid == null) {
            Log.w(TAG, "startAuthenticateFido: invalid aaid!");
            return ERROR_INVALID_AAID;
        }
        if (this.mFinalChallenge == null) {
            Log.w(TAG, "startAuthenticateFido: invalid finalChallenge!");
            return ERROR_INVALID_FINAL_CHALLENGE;
        }

        try {
            final int result = daemon.authenticateFido(getUserId(), this.mAaid, this.mFinalChallenge);;
            if (result != 0) {
                Log.w(TAG, "authenticateFido failed, result=" + result);
                onError(GoodixFingerprintManager.FINGERPRINT_ERROR_HW_UNAVAILABLE);
                return result;
            }
        } catch (RemoteException e) {
            Log.e(TAG, "startAuthenticateFido failed", e);
            return 0; //return 0 to terminate this client
        }
        return 0; // success
    }

    @Override
    public int stop(boolean initiatedByClient) {
        int result = 0;

        if (initiatedByClient) {
            IGFDaemonFido daemon = getGoodixFingerprintDaemonFido();
            if (daemon == null) {
                Log.w(TAG, "stopAuthenticateFido: no goodixfingeprintd!");
                return ERROR_ESRCH;
            }
            try {
                result = daemon.stopAuthenticateFido();
                if (result != 0) {
                    Log.w(TAG, "stopAuthenticateFido failed, result=" + result);
                }
            } catch (RemoteException e) {
                Log.e(TAG, "stopAuthenticateFido failed", e);
                result = 0; //return 0 to terminate this client
            }
            onError(GoodixFingerprintManager.FINGERPRINT_ERROR_CANCELED);
        }
        return result;
    }

    /**
     * Gets the fingerprint daemon from the cached state in the container class.
     */
    public abstract IGFDaemonFido getGoodixFingerprintDaemonFido();

    @Override
    public boolean onEnrollResult(int fingerId, int rem) {
        Log.w(TAG, "onEnrollResult() called for TestCmd!");
        return true;
    }

    @Override
    public boolean onAuthenticated(int fingerId) {
        Log.w(TAG, "onAuthenticated() called for TestCmd!");
        return true;
    }

    @Override
    public boolean onRemoved(int fingerId) {
        Log.w(TAG, "onRemoved() called for TestCmd!");
        return true;
    }

    @Override
    public boolean onEnumerationResult(int fingerId) {
        Log.w(TAG, "onEnumerationResult() called for TestCmd!");
        return true;
    }

    @Override
    public boolean onTestCmdFinish(int chipType, int cmdId, byte[] result) {
        Log.w(TAG, "onTestCmdFinish() called for TestCmd!");
        return true;
    }

    @Override
    public boolean onAuthenticateFido(int fingerId, byte[] uvt) {
        Log.w(TAG, "onAuthenticateFido() called for TestCmd! fingerId:" + fingerId);
        IGoodixFingerprintCallback receiver = getReceiver();
        if (receiver == null) {
            return true; // client not listening
        }
        try {
            receiver.onAuthenticateFido(fingerId, uvt);
            return true;
        } catch (RemoteException e) {
            Log.w(TAG, "Failed to send HbdData:", e);
            return true; //return true to remove client
        }
    }
}
