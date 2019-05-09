/*
 * Copyright (C) 2013-2016, Shenzhen Huiding Technology Co., Ltd.
 * All Rights Reserved.
 */

package com.goodix.fingerprint;

import android.os.Parcel;
import android.os.Parcelable;

public class GFDevice implements Parcelable {

    public int mSafeClass = Constants.GF_SAFE_CLASS_MEDIUM;

    public GFDevice() {

    }

    private GFDevice(Parcel in) {
        this.mSafeClass = in.readInt();
    }

    public GFDevice(GFDevice config) {
        this.mSafeClass = config.mSafeClass;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(mSafeClass);
    }

    public static final Parcelable.Creator<GFDevice> CREATOR = new Parcelable.Creator<GFDevice>() {
        @Override
        public GFDevice createFromParcel(Parcel in) {
            return new GFDevice(in);
        }

        @Override
        public GFDevice[] newArray(int size) {
            return new GFDevice[size];
        }
    };

}
