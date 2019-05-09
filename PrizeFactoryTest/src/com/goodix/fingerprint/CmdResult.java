/*
 * Copyright (C) 2013-2017, Shenzhen Huiding Technology Co., Ltd.
 * All Rights Reserved.
 */

package com.goodix.fingerprint;

import android.os.Parcel;
import android.os.Parcelable;

public class CmdResult implements Parcelable {
    public int mResultCode = 0;
    public byte[] mResultData = null;

    public CmdResult() {
    }

    private CmdResult(Parcel in) {
        mResultCode = in.readInt();
        mResultData = in.createByteArray();
    }

    public CmdResult(CmdResult result) {
        mResultCode = result.mResultCode;
        if (result.mResultData != null && result.mResultData.length != 0) {
            mResultData = new byte[result.mResultData.length];
            System.arraycopy(result.mResultData, 0, mResultData, 0, result.mResultData.length);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(mResultCode);
        if (mResultData != null && mResultData.length > 0) {
            out.writeByteArray(mResultData);
        } else {
            out.writeInt(0);
        }
    }

    public static final Parcelable.Creator<CmdResult> CREATOR = new Parcelable.Creator<CmdResult>() {
        @Override
        public CmdResult createFromParcel(Parcel in) {
            return new CmdResult(in);
        }

        @Override
        public CmdResult[] newArray(int size) {
            return new CmdResult[size];
        }
    };

}
