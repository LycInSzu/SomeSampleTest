/*
 * Copyright (C) 2013-2016, Shenzhen Huiding Technology Co., Ltd.
 * All Rights Reserved.
 */

package com.goodix.fingerprint.proxy;

import android.os.RemoteException;

public interface IGFDaemonFido {
    int authenticateFido(int groupId, byte[] aaid, byte[] finalChallenge) throws RemoteException;
    int stopAuthenticateFido() throws RemoteException;
    int isIdValid(int groupId, int fingerId) throws RemoteException;
    int[] getIdList(int groupId) throws RemoteException;
    int invokeFidoCommand(byte[] inBuf, byte[] outBuf) throws RemoteException;
}

