/*
 * Copyright (C) 2013-2016, Shenzhen Huiding Technology Co., Ltd.
 * All Rights Reserved.
 */

package android.hardware.fingerprint;

interface IGoodixFingerprintDaemonFido {
    int authenticateFido(int groupId, in byte[] aaid, in byte[] finalChallenge);
    int stopAuthenticateFido();
    int isIdValid(int groupId, int fingerId);
    int[] getIdList(int groupId);
    int invokeFidoCommand(in byte[] inBuf, out byte[] outBuf);
}
