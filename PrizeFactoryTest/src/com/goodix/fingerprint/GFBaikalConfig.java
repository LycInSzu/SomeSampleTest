/*
 * Copyright (C) 2013-2017, Shenzhen Huiding Technology Co., Ltd.
 * All Rights Reserved.
 */

package com.goodix.fingerprint;

import android.os.Parcel;
import android.os.Parcelable;

public class GFBaikalConfig implements Parcelable {
    public int mSensorX = Constants.DEFAULT_SENSOR_X;
    public int mSensorY = Constants.DEFAULT_SENSOR_Y;
    public int mSensorWidth = Constants.DEFAULT_SENSOR_WIDTH;
    public int mSensorHeight = Constants.DEFAULT_SENSOR_HEIGHT;
    public boolean mSensorLockAspectRatio = Constants.DEFAULT_LOCK_ASPECT_RATIO;
    public int mSensorAspectRatioWidth = Constants.DEFAULT_ASPECT_RATIO_WIDTH;
    public int mSensorAspectRatioHeight = Constants.DEFAULT_ASPECT_RATIO_HEIGHT;
    public int mSensorPreviewScaleRatio = Constants.DEFAULT_PREVIEW_SCALE_RATIO;
    public int mSensorAreaBackgroundColor = Constants.DEFAULT_SENSOR_AREA_CYAN_COLOR;

    public GFBaikalConfig() {
    }

    private GFBaikalConfig(Parcel in) {
        this.mSensorX = in.readInt();
        this.mSensorY = in.readInt();
        this.mSensorWidth = in.readInt();
        this.mSensorHeight = in.readInt();
        this.mSensorLockAspectRatio = (in.readInt() != 0);
        this.mSensorAspectRatioWidth = in.readInt();
        this.mSensorAspectRatioHeight = in.readInt();
        this.mSensorPreviewScaleRatio = in.readInt();
        this.mSensorAreaBackgroundColor = in.readInt();
    }

    public GFBaikalConfig(GFBaikalConfig config) {
        this.mSensorX = config.mSensorX;
        this.mSensorY = config.mSensorY;
        this.mSensorWidth = config.mSensorWidth;
        this.mSensorHeight = config.mSensorHeight;
        this.mSensorLockAspectRatio = config.mSensorLockAspectRatio;
        this.mSensorAspectRatioWidth = config.mSensorAspectRatioWidth;
        this.mSensorAspectRatioHeight = config.mSensorAspectRatioHeight;
        this.mSensorPreviewScaleRatio = config.mSensorPreviewScaleRatio;
        this.mSensorAreaBackgroundColor = config.mSensorAreaBackgroundColor;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(mSensorX);
        out.writeInt(mSensorY);
        out.writeInt(mSensorWidth);
        out.writeInt(mSensorHeight);
        out.writeInt(mSensorLockAspectRatio ? 1 : 0);
        out.writeInt(mSensorAspectRatioWidth);
        out.writeInt(mSensorAspectRatioHeight);
        out.writeInt(mSensorPreviewScaleRatio);
        out.writeInt(mSensorAreaBackgroundColor);
    }

    public static final Parcelable.Creator<GFBaikalConfig> CREATOR = new Parcelable.Creator<GFBaikalConfig>() {
        @Override
        public GFBaikalConfig createFromParcel(Parcel in) {
            return new GFBaikalConfig(in);
        }

        @Override
        public GFBaikalConfig[] newArray(int size) {
            return new GFBaikalConfig[size];
        }
    };

}
