/*
 * Copyright (C) 2013-2017, Shenzhen Huiding Technology Co., Ltd.
 * All Rights Reserved.
 */


package com.goodix.fingerprint;

import android.os.Parcel;
import android.os.Parcelable;

import com.goodix.fingerprint.ShenzhenConstants;

public class GFShenzhenConfig implements Parcelable {
    public int mSensorX = ShenzhenConstants.DEFAULT_SENSOR_X;
    public int mSensorY = ShenzhenConstants.DEFAULT_SENSOR_Y;
    public int mSensorWidth = ShenzhenConstants.DEFAULT_SENSOR_WIDTH;
    public int mSensorHeight = ShenzhenConstants.DEFAULT_SENSOR_HEIGHT;
    public boolean mSensorLockAspectRatio = ShenzhenConstants.DEFAULT_LOCK_ASPECT_RATIO;
    public int mSensorAspectRatioWidth = ShenzhenConstants.DEFAULT_ASPECT_RATIO_WIDTH;
    public int mSensorAspectRatioHeight = ShenzhenConstants.DEFAULT_ASPECT_RATIO_HEIGHT;
    public int mSensorPreviewScaleRatio = ShenzhenConstants.DEFAULT_PREVIEW_SCALE_RATIO;
    public int mSensorAreaBackgroundColor = ShenzhenConstants.DEFAULT_SENSOR_AREA_CYAN_COLOR;

    public int mScaleRatio;
    public int mLongFrameAvgNum;
    public int mLongPAGGain;

    public int mShortExposureTime;
    public int mShortFrameAvgNum;
    public int mShortPAGGain;

    public int munLowerthresh;
    public int munHighthresh;

    public int mFusionratio;

    public int mPreprocess;

    public int mRect_x;
    public int mRect_y;
    public int mRect_width;
    public int mRect_height;
    public int mRect_bmp_col;
    public int mRect_bmp_row;
    public int mSensorRow;
    public int mSensorCol;

    public int mBaseCalibrateSwitch;
    public int mLDCSwitch;

    public int mTNR_Thresh;
    public int mTNRSwitch;
    public int mLPFSwitch;
    public int mLSCSwitch;
    public int mEnhance_Level;

    public int mLongExpoRadius;


    public int mKeydefluat;
    public int mKeyBaseCalibrate;
    public int mKeyKrCalibrate;
    public int mKeyUpdateKB;
    public int mKeyDFTConst;
    public int mKeyDFTSet;
    public int mKeyPreguassian;
    public int mKeyFullQaun;
    public int mKeyDeNoise;
    public int mKeyBackEqual;
    public int mKeyLocalQuan;
    public int mKeyDirEnhance;
    public int mKeySito;
    public int mKeyInverBmp;
    public int mKeyLdc;
    public int mIsSensorPreviewEnhanced;

    public int misSensorPreviewBmp;
    public int mEnrollTemplateCount;
    public int mRetryCount;
    public GFShenzhenConfig() {
    }

    private GFShenzhenConfig(Parcel in) {
        this.mSensorX = in.readInt();
        this.mSensorY = in.readInt();
        this.mSensorWidth = in.readInt();
        this.mSensorHeight = in.readInt();
        this.mSensorLockAspectRatio = (in.readInt() != 0);
        this.mSensorAspectRatioWidth = in.readInt();
        this.mSensorAspectRatioHeight = in.readInt();
        this.mSensorPreviewScaleRatio = in.readInt();
        this.mSensorAreaBackgroundColor = in.readInt();
        this.mScaleRatio = in.readInt();
        this.mLongFrameAvgNum = in.readInt();
        this.mLongPAGGain = in.readInt();
        this.mShortExposureTime = in.readInt();
        this.mShortFrameAvgNum = in.readInt();
        this.mShortPAGGain = in.readInt();
        this.munLowerthresh = in.readInt();
        this.munHighthresh = in.readInt();
        this.mFusionratio = in.readInt();
        this.mPreprocess = in.readInt();
        this.mRect_x = in.readInt();
        this.mRect_y = in.readInt();
        this.mRect_width = in.readInt();
        this.mRect_height = in.readInt();
        this.mRect_bmp_col = in.readInt();
        this.mRect_bmp_row = in.readInt();
        this.mSensorRow = in.readInt();
        this.mSensorCol = in.readInt();
        this.mBaseCalibrateSwitch = in.readInt();
        this.mLDCSwitch = in.readInt();
        this.mTNRSwitch = in.readInt();
        this.mLPFSwitch = in.readInt();
        this.mLSCSwitch = in.readInt();
        this.mEnhance_Level = in.readInt();
        this.mTNR_Thresh = in.readInt();
        this.mLongExpoRadius = in.readInt();

        this.mKeydefluat = in.readInt();
        this.mKeyBaseCalibrate = in.readInt();
        this.mKeyKrCalibrate = in.readInt();
        this.mKeyUpdateKB = in.readInt();
        this.mKeyDFTConst = in.readInt();
        this.mKeyDFTSet = in.readInt();
        this.mKeyPreguassian = in.readInt();
        this.mKeyFullQaun = in.readInt();
        this.mKeyDeNoise = in.readInt();
        this.mKeyBackEqual = in.readInt();
        this.mKeyLocalQuan = in.readInt();
        this.mKeyDirEnhance = in.readInt();
        this.mKeySito = in.readInt();
        this.mKeyInverBmp = in.readInt();
        this.mKeyLdc = in.readInt();

        this.misSensorPreviewBmp = in.readInt();
        this.mEnrollTemplateCount = in.readInt();
        this.mRetryCount = in.readInt();
        this.mIsSensorPreviewEnhanced = in.readInt();
    }

    public GFShenzhenConfig(GFShenzhenConfig config) {
        this.mSensorX = config.mSensorX;
        this.mSensorY = config.mSensorY;
        this.mSensorWidth = config.mSensorWidth;
        this.mSensorHeight = config.mSensorHeight;
        this.mSensorLockAspectRatio = config.mSensorLockAspectRatio;
        this.mSensorAspectRatioWidth = config.mSensorAspectRatioWidth;
        this.mSensorAspectRatioHeight = config.mSensorAspectRatioHeight;
        this.mSensorPreviewScaleRatio = config.mSensorPreviewScaleRatio;
        this.mSensorAreaBackgroundColor = config.mSensorAreaBackgroundColor;
        this.mScaleRatio = config.mScaleRatio;
        this.mLongFrameAvgNum = config.mLongFrameAvgNum;
        this.mLongPAGGain = config.mLongPAGGain;
        this.mShortExposureTime = config.mShortExposureTime;
        this.mShortFrameAvgNum = config.mShortFrameAvgNum;
        this.mShortPAGGain = config.mShortPAGGain;

        this.munLowerthresh = config.munLowerthresh;
        this.munHighthresh = config.munHighthresh;
        this.mFusionratio = config.mFusionratio;
        this.mPreprocess = config.mPreprocess;
        this.mRect_x = config.mRect_x;
        this.mRect_y = config.mRect_y;
        this.mRect_width = config.mRect_width;
        this.mRect_height = config.mRect_height;
        this.mRect_bmp_col = config.mRect_bmp_col;
        this.mRect_bmp_row = config.mRect_bmp_row;
        this.mSensorRow = config.mSensorRow;
        this.mSensorCol = config.mSensorCol;
        this.mBaseCalibrateSwitch = config.mBaseCalibrateSwitch;
        this.mLDCSwitch = config.mLDCSwitch;
        this.mTNRSwitch = config.mTNRSwitch;
        this.mLPFSwitch = config.mLPFSwitch;
        this.mLSCSwitch = config.mLSCSwitch;
        this.mEnhance_Level = config.mEnhance_Level;
        this.mTNR_Thresh = config.mTNR_Thresh;

        this.mLongExpoRadius = config.mLongExpoRadius;

        this.mKeydefluat = config.mKeydefluat;
        this.mKeyBaseCalibrate = config.mKeyBaseCalibrate;
        this.mKeyKrCalibrate = config.mKeyKrCalibrate;
        this.mKeyUpdateKB = config.mKeyUpdateKB;
        this.mKeyDFTConst = config.mKeyDFTConst;
        this.mKeyDFTSet = config.mKeyDFTSet;
        this.mKeyPreguassian = config.mKeyPreguassian;
        this.mKeyFullQaun = config.mKeyFullQaun;
        this.mKeyDeNoise = config.mKeyDeNoise;
        this.mKeyBackEqual = config.mKeyBackEqual;
        this.mKeyLocalQuan = config.mKeyLocalQuan;
        this.mKeyDirEnhance = config.mKeyDirEnhance;
        this.mKeySito = config.mKeySito;
        this.mKeyInverBmp = config.mKeyInverBmp;
        this.mKeyLdc = config.mKeyLdc;
        this.mIsSensorPreviewEnhanced = config.mIsSensorPreviewEnhanced;

        this.misSensorPreviewBmp = config.misSensorPreviewBmp;
        this.mEnrollTemplateCount = config.mEnrollTemplateCount;
        this.mRetryCount = config.mRetryCount;
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
        out.writeInt(mScaleRatio);
        out.writeInt(mLongFrameAvgNum);
        out.writeInt(mLongPAGGain);
        out.writeInt(mShortExposureTime);
        out.writeInt(mShortFrameAvgNum);
        out.writeInt(mShortPAGGain);

        out.writeInt(munLowerthresh);
        out.writeInt(munHighthresh);
        out.writeInt(mFusionratio);
        out.writeInt(mPreprocess);
        out.writeInt(mRect_x);
        out.writeInt(mRect_y);
        out.writeInt(mRect_width);
        out.writeInt(mRect_height);
        out.writeInt(mRect_bmp_col);
        out.writeInt(mRect_bmp_row);
        out.writeInt(mSensorRow);
        out.writeInt(mSensorCol);
        out.writeInt(mBaseCalibrateSwitch);
        out.writeInt(mLDCSwitch);
        out.writeInt(mTNRSwitch);
        out.writeInt(mLPFSwitch);
        out.writeInt(mLSCSwitch);
        out.writeInt(mEnhance_Level);
        out.writeInt(mTNR_Thresh);
        out.writeInt(mLongExpoRadius);

        out.writeInt(mKeydefluat);
        out.writeInt(mKeyBaseCalibrate);
        out.writeInt(mKeyKrCalibrate);
        out.writeInt(mKeyUpdateKB);
        out.writeInt(mKeyDFTConst);
        out.writeInt(mKeyDFTSet);
        out.writeInt(mKeyPreguassian);
        out.writeInt(mKeyFullQaun);
        out.writeInt(mKeyDeNoise);
        out.writeInt(mKeyBackEqual);
        out.writeInt(mKeyLocalQuan);
        out.writeInt(mKeyDirEnhance);
        out.writeInt(mKeySito);
        out.writeInt(mKeyInverBmp);
        out.writeInt(mKeyLdc);
        out.writeInt(mIsSensorPreviewEnhanced);

        out.writeInt(misSensorPreviewBmp);
        out.writeInt(mEnrollTemplateCount);
        out.writeInt(mRetryCount);
    }

    public static final Parcelable.Creator<GFShenzhenConfig> CREATOR = new Parcelable.Creator<GFShenzhenConfig>() {
        @Override
        public GFShenzhenConfig createFromParcel(Parcel in) {
            return new GFShenzhenConfig(in);
        }

        @Override
        public GFShenzhenConfig[] newArray(int size) {
            return new GFShenzhenConfig[size];
        }
    };

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof GFShenzhenConfig)) {
            return false;
        }
        if (this == obj) {
            return true;
        }

        return equals((GFShenzhenConfig) obj);
    }

    private boolean equals(GFShenzhenConfig config) {
        if (this.mSensorX != config.mSensorX || this.mSensorY != config.mSensorY) {
            return false;
        }

        if (this.mSensorWidth != config.mSensorWidth || this.mSensorHeight != config.mSensorHeight) {
            return false;
        }

        if (this.mSensorAspectRatioWidth != config.mSensorAspectRatioWidth || this.mSensorAspectRatioHeight != config.mSensorAspectRatioHeight) {
            return false;
        }

        if (this.mSensorPreviewScaleRatio != config.mSensorPreviewScaleRatio || this.mSensorAreaBackgroundColor != config.mSensorAreaBackgroundColor) {
            return false;
        }

        if (this.mSensorLockAspectRatio != config.mSensorLockAspectRatio) {
            return false;
        }

        if (this.mScaleRatio != config.mScaleRatio || this.mLongFrameAvgNum != config.mLongFrameAvgNum) {
            return false;
        }

        if (this.mLongPAGGain != config.mLongPAGGain || this.mShortExposureTime != config.mShortExposureTime) {
            return false;
        }

        if (this.mShortFrameAvgNum != config.mShortFrameAvgNum || this.mShortPAGGain != config.mShortPAGGain) {
            return false;
        }

        if (this.munLowerthresh != config.munLowerthresh || this.munHighthresh != config.munHighthresh) {
            return false;
        }

        if (this.mFusionratio != config.mFusionratio || this.mPreprocess != config.mPreprocess) {
            return false;
        }

        if (this.mRect_x != config.mRect_x || this.mRect_y != config.mRect_y) {
            return false;
        }

        if (this.mRect_width != config.mRect_width || this.mRect_height != config.mRect_height) {
            return false;
        }
        if (this.mBaseCalibrateSwitch != config.mBaseCalibrateSwitch || this.mLDCSwitch != config.mLDCSwitch) {
            return false;
        }
        if (this.mEnhance_Level != config.mEnhance_Level || this.mTNRSwitch != config.mTNRSwitch) {
            return false;
        }
        if (this.mLSCSwitch != config.mLSCSwitch || this.mLPFSwitch != config.mLPFSwitch) {
            return false;
        }
        if (this.mTNR_Thresh != config.mTNR_Thresh || this.mLongExpoRadius != config.mLongExpoRadius) {
            return false;
        }
        if (this.mRect_bmp_col != config.mRect_bmp_col || this.mRect_bmp_row != config.mRect_bmp_row) {
            return false;
        }
        if(this.mSensorRow != config.mSensorRow || this.mSensorCol != config.mSensorCol){
            return false;
        }

        if (this.mKeydefluat != config.mKeydefluat ||
                this.mKeyBaseCalibrate != config.mKeyBaseCalibrate ||
                this.mKeyKrCalibrate != config.mKeyKrCalibrate ||
                this.mKeyUpdateKB != config.mKeyUpdateKB ||
                this.mKeyDFTConst != config.mKeyDFTConst ||
                this.mKeyDFTSet != config.mKeyDFTSet ||
                this.mKeyPreguassian != config.mKeyPreguassian ||
                this.mKeyFullQaun != config.mKeyFullQaun ||
                this.mKeyDeNoise != config.mKeyDeNoise ||
                this.mKeyBackEqual != config.mKeyBackEqual ||
                this.mKeyLocalQuan != config.mKeyLocalQuan ||
                this.mKeyDirEnhance != config.mKeyDirEnhance ||
                this.mKeySito != config.mKeySito ||
                this.mKeyInverBmp != config.mKeyInverBmp ||
                this.mKeyLdc != config.mKeyLdc ||
                this.misSensorPreviewBmp != config.misSensorPreviewBmp ||
                this.mEnrollTemplateCount != config.mEnrollTemplateCount ||
                this.mIsSensorPreviewEnhanced != config.mIsSensorPreviewEnhanced ||
                this.mRetryCount != config.mRetryCount) {
            return false;
        }

        return true;
    }
}
