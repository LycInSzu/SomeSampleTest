/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 *
 * MediaTek Inc. (C) 2010. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER ON
 * AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL WARRANTIES,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NONINFRINGEMENT.
 * NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH RESPECT TO THE
 * SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY, INCORPORATED IN, OR
 * SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES TO LOOK ONLY TO SUCH
 * THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO. RECEIVER EXPRESSLY ACKNOWLEDGES
 * THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES
 * CONTAINED IN MEDIATEK SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK
 * SOFTWARE RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S ENTIRE AND
 * CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE RELEASED HEREUNDER WILL BE,
 * AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE MEDIATEK SOFTWARE AT ISSUE,
 * OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE CHARGE PAID BY RECEIVER TO
 * MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek Software")
 * have been modified by MediaTek Inc. All revisions are subject to any receiver's
 * applicable license agreements with MediaTek Inc.
 */

package com.cydroid.ota.logic.sync;

import android.os.IBinder;

/**
 * define a interface.
 */
public interface SystemUpdateBinder extends android.os.IInterface {
    /**
     * class.
     *
     */
    public abstract static class Stub extends android.os.Binder implements SystemUpdateBinder {
        private static final String DESCRIPTOR = "GoogleOtaBinder";

        /**
         * stub.
         */
        public Stub() {
            this.attachInterface(this, DESCRIPTOR);
        }

        /**
         *
         * @param obj android.is.IBinder.
         * @return binder.
         */
        public static SystemUpdateBinder asInterface(IBinder obj) {
            if ((obj == null)) {
                return null;
            }
            android.os.IInterface iin = (android.os.IInterface) obj.queryLocalInterface(DESCRIPTOR);
            if (((iin != null) && (iin instanceof SystemUpdateBinder))) {
                return ((SystemUpdateBinder) iin);
            }
            return new SystemUpdateBinder.Stub.Proxy(obj);
        }

        /**
         *
         * @return binder.
         */
        public IBinder asBinder() {
            return this;
        }

        /**
         *
         * @param code code
         * @param data data
         * @param reply replu
         * @param flags flags
         * @return result result
         * @throws android.os.RemoteException RemoteException.
         */
        public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply,
                int flags)
                throws android.os.RemoteException {
            return true;
        }

        /**
         *Proxy.
         */
        private static class Proxy implements SystemUpdateBinder {
            private IBinder mRemote;

            Proxy(IBinder remote) {
                mRemote = remote;
            }

            /**
             *
             * @return mRemote.
             */
            public IBinder asBinder() {
                return mRemote;
            }

            /**
             * set reboot flag.
             * @return result.
             */
            public boolean setRebootFlag() throws android.os.RemoteException {
                android.os.Parcel data = android.os.Parcel.obtain();
                android.os.Parcel reply = android.os.Parcel.obtain();
                boolean result;
                try {
                    data.writeInterfaceToken(DESCRIPTOR);
                    mRemote.transact(Stub.TRANSACTION_SET_REBOOT_FLAG, data, reply, 0);
                    result = (0 != reply.readInt());
                } finally {
                    reply.recycle();
                    data.recycle();
                }
                return result;
            }

            /**
             * clear update result before upgrade.
             */
            public boolean clearUpdateResult() throws android.os.RemoteException {
                android.os.Parcel data = android.os.Parcel.obtain();
                android.os.Parcel reply = android.os.Parcel.obtain();
                boolean result;
                try {
                    data.writeInterfaceToken(DESCRIPTOR);
                    mRemote.transact(Stub.TRANSACTION_CLEAR_UPGRADE_RESULT, data, reply, 0);
                    result = (0 != reply.readInt());
                } finally {
                    reply.recycle();
                    data.recycle();
                }
                return result;
            }

            /**
             * read upgrade result.
             */
            public boolean readUpgradeResult() throws android.os.RemoteException {
                android.os.Parcel data = android.os.Parcel.obtain();
                android.os.Parcel reply = android.os.Parcel.obtain();
                boolean result;
                try {
                    data.writeInterfaceToken(DESCRIPTOR);
                    mRemote.transact(Stub.TRANSACTION_READ_UPGRADE_RESULT, data, reply, 0);
                    result = (0 != reply.readInt());
                } finally {
                    reply.recycle();
                    data.recycle();
                }
                return result;
            }

        }

        /**
         * reboot flag.
         */
        static final int TRANSACTION_SET_REBOOT_FLAG = (IBinder.FIRST_CALL_TRANSACTION + 100);
        /**
         * clear upgrade result flag.
         */
        static final int TRANSACTION_CLEAR_UPGRADE_RESULT = (IBinder.FIRST_CALL_TRANSACTION + 101);
        /**
         * read upgrade result flag.
         */
        static final int TRANSACTION_READ_UPGRADE_RESULT = (IBinder.FIRST_CALL_TRANSACTION + 111);

    }

    /**
     *
     * @return result
     * @throws android.os.RemoteException RemoteException.
     */
    boolean setRebootFlag() throws android.os.RemoteException;
    /**
     * @return clear result
     * @throws android.os.RemoteException RemoteException.
     */
    boolean clearUpdateResult() throws android.os.RemoteException;
    /**
     *
     * @return read result
     * @throws android.os.RemoteException RemoteException.
     */
    boolean readUpgradeResult() throws android.os.RemoteException;

}
