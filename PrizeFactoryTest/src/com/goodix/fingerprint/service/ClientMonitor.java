/*
 * Copyright (C) 2013-2017, Shenzhen Huiding Technology Co., Ltd.
 * All Rights Reserved.
 */

package com.goodix.fingerprint.service;

import android.content.Context;
import android.hardware.fingerprint.IGoodixFingerprintDaemon;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

public abstract class ClientMonitor implements IBinder.DeathRecipient {
    protected static final String TAG = "Client"; // TODO: get specific name
    protected static final boolean DEBUG = GoodixFingerprintService.DEBUG;
    protected static final int ERROR_ESRCH = 3; // Likely goodixingerprintd is dead.
    protected static final int ERROR_CLIENT_DEFINED = 100; // Likely goodixingerprintd is dead.
    private Context mContext;
    private IBinder mToken;
    private IGoodixFingerprintCallback mReceiver;
    private int mUserId;
    private boolean mRestricted; // True if client does not have MANAGE_FINGERPRINT permission
    private String mOwner;

    public ClientMonitor(Context context, IBinder token, IGoodixFingerprintCallback receiver,
            int userId, boolean restricted, String owner) {
        this.mContext = context;
        this.mToken = token;
        this.mReceiver = receiver;
        this.mUserId = userId;
        this.mRestricted = restricted;
        this.mOwner = owner;
        try {
            if (token != null) {
                token.linkToDeath(this, 0);
            }
        } catch (RemoteException e) {
            Log.w(TAG, "caught remote exception in linkToDeath: ", e);
        }
    }

    /**
     * Contacts goodixfingerprintd to start the client.
     * @return 0 on succes, errno from driver on failure
     */
    public abstract int start();

    /**
     * Contacts goodixfingerprintd to stop the client.
     * @param initiatedByClient whether the operation is at the request of a client
     */
    public abstract int stop(boolean initiatedByClient);

    public abstract boolean onEnrollResult(int fingerId, int rem);
    public abstract boolean onAuthenticated(int fingerId);
    public abstract boolean onRemoved(int fingerId);
    public abstract boolean onEnumerationResult(int fingerId);
    public abstract boolean onTestCmdFinish(int chipSeries, int cmdId, byte[] result);
    public abstract boolean onAuthenticateFido(int fingerId, byte[] uvt);

    public void destroy() {
        if (mToken != null) {
            try {
                mToken.unlinkToDeath(this, 0);
            } catch (Exception e) {
                // TODO: remove when duplicate call bug is found
                Log.e(TAG, "destroy(): " + this + ":", new Exception("here"));
            }
            mToken = null;
        }
        mReceiver = null;
    }

    @Override
    public void binderDied() {
        mToken = null;
        mReceiver = null;
        onError(GoodixFingerprintManager.FINGERPRINT_ERROR_HW_UNAVAILABLE);
    }

    public boolean onError(int error) {
        if (mReceiver != null) {
            try {
                mReceiver.onError(error);
            } catch (RemoteException e) {
                Log.w(TAG, "Failed to invoke sendError:", e);
            }
        }
        return true; // errors always remove current client
    }

    public boolean onAcquired(int acquiredInfo) {
        if (mReceiver == null) {
            return true; // client not connected
        }
        try {
            mReceiver.onAcquired(acquiredInfo);
            return false; // acquisition continues...
        } catch (RemoteException e) {
            Log.w(TAG, "Failed to invoke sendAcquired:", e);
            return true; // client failed
        }
   }

    @Override
    protected void finalize() throws Throwable {
        try {
            if (mToken != null) {
                if (DEBUG) Log.w(TAG, "removing leaked reference: " + mToken);
                onError(GoodixFingerprintManager.FINGERPRINT_ERROR_HW_UNAVAILABLE);
            }
        } finally {
            super.finalize();
        }
    }

    public Context getContext() {
        return mContext;
    }

    public IBinder getToken() {
        return mToken;
    }

    public IGoodixFingerprintCallback getReceiver() {
        return mReceiver;
    }

    public int getUserId() {
        return mUserId;
    }

    public boolean isRestricted() {
        return mRestricted;
    }

    public String getOwner() {
        return mOwner;
    }
}
