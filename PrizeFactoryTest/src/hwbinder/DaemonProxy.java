/*
 * Copyright (C) 2013-2016, Shenzhen Huiding Technology Co., Ltd.
 * All Rights Reserved.
 */

package binder;

import com.goodix.fingerprint.CmdResult;
import com.goodix.fingerprint.proxy.DaemonProxyBase;
import com.goodix.fingerprint.proxy.IDaemonDiedCallback;
import com.goodix.fingerprint.proxy.IGFDaemon;
import com.goodix.fingerprint.proxy.IGFDaemonCallback;
import com.goodix.fingerprint.proxy.IGFDaemonFido;

import vendor.goodix.hardware.biometrics.fingerprint.V2_1.IGoodixFingerprintDaemon;
//import vendor.goodix.hardware.biometrics.fingerprint.V2_1.IGoodixFingerprintDaemonFido;
import vendor.goodix.hardware.biometrics.fingerprint.V2_1.IGoodixFingerprintDaemonCallback;
import android.os.IHwBinder;
import android.os.RemoteException;
import android.util.Log;
import java.util.Arrays;
import java.util.ArrayList;
import java.lang.Byte;

public class DaemonProxy extends DaemonProxyBase {
    private static final String TAG = "HwBinderDaemonProxy";

    protected Daemon getGFDaemonImp(IGFDaemonCallback callback) {
        return new GFDaemonImp(callback);
    }

    protected Daemon getGFDaemonFidoImp(IGFDaemonCallback callback) {
        return new GFDaemonFidoImp(callback);
    }

    private abstract class HwBinderDaemon extends Daemon implements IHwBinder.DeathRecipient {
        IDaemonDiedCallback mDaemonCallback = null;

        abstract void handleServiceDied();

        @Override
        public void serviceDied(long cookie) {
            handleServiceDied();
            if (mDaemonCallback != null)
            {
                mDaemonCallback.onDaemonDied();
            }
        }

        ArrayList<Byte> toArrayList(byte[] param) {
            ArrayList<Byte> arrayList;
            if (param == null || param.length == 0) {
                arrayList = new ArrayList<Byte>();
            } else {
                Byte[] p = new Byte[param.length];
                for (int i = 0; i < param.length; i++) {
                    p[i] = param[i];
                }
                arrayList = new ArrayList<Byte>(Arrays.asList(p));
            }
            return arrayList;
        }

        byte[] toByteArray(ArrayList<Byte> list) {
            if (list == null || list.size() == 0) {
                return null;
            }
            Byte[] tmp = new Byte[list.size()];
            list.toArray(tmp);
            byte[] ret = new byte[tmp.length];
            for (int i = 0; i < tmp.length; i++) {
                ret[i] = tmp[i].byteValue();
            }
            return ret;
        }

        int[] toIntArray(ArrayList<Integer> list) {
            if (list == null || list.size() == 0) {
                return null;
            }
            Integer[] tmp = new Integer[list.size()];
            list.toArray(tmp);
            int[] ret = new int[tmp.length];
            for (int i = 0; i < tmp.length; i++) {
                ret[i] = tmp[i].byteValue();
            }
            return ret;
        }
    }

    private class GFDaemonImp extends HwBinderDaemon implements IGFDaemon {
        private IGoodixFingerprintDaemon mDaemon = null;

        public GFDaemonImp(IGFDaemonCallback callback) {
            mDaemonCallback = callback;
        }

        @Override
        public Object getService() {
            if (mDaemon == null) {
                try {
                    mDaemon = IGoodixFingerprintDaemon.getService();
                } catch (java.util.NoSuchElementException e) {
                    // Service doesn't exist or cannot be opened. Logged below.
                } catch (RemoteException e) {
                    Log.e(TAG, "Failed to get IGoodixFingerprintDaemon");
                }

                if (mDaemon != null) {
                    try {
                        mDaemon.asBinder().linkToDeath(this, 0);
                        mDaemon.setNotify(mGFDaemonCallback);
                    } catch (RemoteException e) {
                        mDaemon = null;
                    }
                }
            }

            return mDaemon;
        }

        @Override
        void handleServiceDied() {
            mDaemon = null;
        }

        @Override
        public void setNotify(IGFDaemonCallback callback) {
            mDaemonCallback = callback;
        }

        @Override
        public CmdResult sendCommand(int cmdId, byte[] param) throws RemoteException {
            final CmdResult ret = new CmdResult();
            IGoodixFingerprintDaemon daemon = (IGoodixFingerprintDaemon) getService();
            if (mDaemon != null)
            {
                ArrayList<Byte> paramList = toArrayList(param);
                daemon.sendCommand(cmdId, paramList, new IGoodixFingerprintDaemon.sendCommandCallback() {
                    @Override
                    public void onValues(int resultCode, ArrayList<Byte> out_buf) {
                        ret.mResultCode = resultCode;
                        ret.mResultData = toByteArray(out_buf);
                    }
                });
            }
            return ret;
        }

        private IGoodixFingerprintDaemonCallback mGFDaemonCallback = new IGoodixFingerprintDaemonCallback.Stub() {
            @Override
            public void onDaemonMessage(long devId, int msgId, int cmdId, ArrayList<Byte> data)
            {
                if (mDaemonCallback != null)
                {
                    ((IGFDaemonCallback)mDaemonCallback).onDaemonMessage(devId, msgId, cmdId, toByteArray(data));
                }
            }
        };
    };

    private class GFDaemonFidoImp extends HwBinderDaemon implements IGFDaemonFido {
        // private IGoodixFingerprintDaemonFido mDaemon = null;
        private int[] mIdList = null;
        private byte[] mCmdOutBuf = null;
        private int mCmdRet = 0;

        public GFDaemonFidoImp(IGFDaemonCallback callback) {
            mDaemonCallback = callback;
        }

        public Object getService() {
            /*
            if (mDaemon == null) {
                try {
                    mDaemon = IGoodixFingerprintDaemonFido.getService();
                } catch (java.util.NoSuchElementException e) {
                    // Service doesn't exist or cannot be opened. Logged below.
                } catch (RemoteException e) {
                    Log.e(TAG, "Failed to get IGoodixFingerprintDaemon");
                }

                if (mDaemon != null) {
                    mDaemon.asBinder().linkToDeath(this, 0);
                }
            }

            return mDaemon;*/
            return null;
        }

        @Override
        void handleServiceDied() {
            //mDaemon = null;
        }

        @Override
        public int authenticateFido(int groupId, byte[] aaid, byte[] finalChallenge) throws RemoteException {
            int ret = 0;
            /*
            IGoodixFingerprintDaemonFido daemon = (IGoodixFingerprintDaemonFido) getService();
            if (mDaemon != null)
            {
                ArrayList<Byte> aaidList = toArrayList(aaid);
                ArrayList<Byte> fcList = toArrayList(finalChallenge);
                ret = daemon.authenticateFido(groupId, aaidList, fcList);
            }*/
            return ret;
        }

        @Override
        public int stopAuthenticateFido() throws RemoteException {
            int ret = 0;
            /*
            IGoodixFingerprintDaemonFido daemon = (IGoodixFingerprintDaemonFido) getService();
            if (mDaemon != null)
            {
                ret = daemon.stopAuthenticateFido();
            }*/
            return ret;
        }

        @Override
        public int isIdValid(int groupId, int fingerId) throws RemoteException {
            int ret = 0;
            /*
            IGoodixFingerprintDaemonFido daemon = (IGoodixFingerprintDaemonFido) getService();
            if (mDaemon != null)
            {
                ret = daemon.isIdValid(groupId, fingerId);
            }*/
            return ret;
        }

        @Override
        public synchronized int[] getIdList(int groupId) throws RemoteException {
            int [] result = null;
            /*
            mIdList = null;
            IGoodixFingerprintDaemonFido daemon = (IGoodixFingerprintDaemonFido) getService();
            if (mDaemon != null)
            {
                daemon.getIdList(groupId, new IGoodixFingerprintDaemonFido.getIdListCallback() {
                    @Override
                    public void onValues(int debugErrno, ArrayList<Integer> list) {
                        if (debugErrno == 0)
                        {
                            mIdList = toIntArray(list);
                        }
                    }
                });
            }
            if (mIdList != null && mIdList.length > 0)
            {
                result = new int[mIdList.length];
                for (int i = 0; i < mIdList.length; i++)
                {
                    result[i] = mIdList[i];
                }
            }
            mIdList = null;*/
            return result;
        }

        @Override
        public synchronized int invokeFidoCommand(byte[] inBuf, byte[] outBuf) throws RemoteException {
            mCmdRet = 0;
            /*
            mCmdOutBuf = null;
            IGoodixFingerprintDaemonFido daemon = (IGoodixFingerprintDaemonFido) getService();
            if (mDaemon != null)
            {
                ArrayList<Byte> in = toArrayList(inBuf);
                daemon.invokeFidoCommand(in, new IGoodixFingerprintDaemonFido.invokeFidoCommandCallback() {
                    @Override
                    public void onValues(int debugErrno, ArrayList<Byte> outList) {
                        mCmdRet = debugErrno;
                        if (mCmdRet == 0)
                        {
                            mCmdOutBuf = toByteArray(outList);
                        }
                    }
                });
            }
            if (mCmdOutBuf != null && mCmdOutBuf.length > 0 && mCmdOutBuf.length <= outBuf.length)
            {
                for (int i = 0; i < mCmdOutBuf.length && i < outBuf.length; i++)
                {
                    outBuf[i] = mCmdOutBuf[i];
                }
                mCmdRet = mCmdOutBuf.length;
            }
            else
            {
                mCmdRet = 0;
            }

            mCmdOutBuf = null;*/
            // return value is outBuf length, but not error code
            return mCmdRet;
        }
    };
}

