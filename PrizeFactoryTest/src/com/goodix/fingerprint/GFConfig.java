/*
 * Copyright (C) 2013-2017, Shenzhen Huiding Technology Co., Ltd.
 * All Rights Reserved.
 */

package com.goodix.fingerprint;

import android.os.Parcel;
import android.os.Parcelable;

public class GFConfig implements Parcelable {
    public int mChipType = Constants.GF_CHIP_BAIKAL_SE1;
    public int mChipSeries = Constants.GF_BAIKAL;
    public int mMaxFingers = 32;
    public int mMaxFingersPerUser = 5;
    public int mSupportKeyMode = 0;
    public int mSupportFFMode = 1;
    public int mSupportPowerKeyFeature = 0;
    public int mForbiddenUntrustedEnroll = 0;
    public int mForbiddenEnrollDuplicateFingers = 0;
    public int mSupportBioAssay = 0;
    public int mSupportPerformanceDump = 0;

    public int mSupportNavMode = 0;
    public int mNavDoubleClickTime = 0;
    public int mNavLongPressTime = 0;

    public int mEnrollingMinTemplates = 8;

    public int mValidImageQualityThreshold = 15;
    public int mValidImageAreaThreshold = 65;
    public int mDuplicateFingerOverlayScore = 70;
    public int mIncreaseRateBetweenStitchInfo = 15;

    public int mSupportImageRescan = 1;
    public int mRescanImageQualityThreshold = 10;
    public int mRescanImageAreaThreshold = 60;
    public int mRescanRetryCount = 1;

    public int mScreenOnAuthenticateFailRetryCount = 1;
    public int mScreenOffAuthenticateFailRetryCount = 1;

    public int mScreenOnValidTouchFrameThreshold = 1;
    public int mScreenOffValidTouchFrameThreshold = 1;
    public int mImageQualityThresholdForMistakeTouch = 10;

    public int mAuthenticateOrder = Constants.GF_AUTHENTICATE_BY_USE_RECENTLY;

    public int mReissueKeyDownWhenEntryFfMode = 0;
    public int mReissueKeyDownWhenEntryImageMode = 1;

    public int mSupportSensorBrokenCheck = 0;
    public int mBrokenPixelThresholdForDisableSensor = 600;
    public int mBrokenPixelThresholdForDisableStudy = 300;

    public int mBadPointTestMaxFrameNumber = 2;

    public int mReportKeyEventOnlyEnrollAuthenticate = 0;

    public int mRequireDownAndUpInPairsForImageMode = 1;
    public int mRequireDownAndUpInPairsForFFMode = 0;
    public int mRequireDownAndUpInPairsForKeyMode = 1;
    public int mRequireDownAndUpInPairsForNavMode = 1;

    public int mSupportSetSpiSpeedInTEE = 1;
    public int mSupportFrrAnalysis = 1;
    public int mTemplateUpateSaveThreshold = 20;
    public int mSupportImageSegment = 0;
    public int mSupportBaikalContinuousSampling = 0;
    public int mContinuousSamplingNumber = 4;

    public GFConfig() {
    }

    private GFConfig(Parcel in) {
        this.mChipType = in.readInt();
        this.mChipSeries = in.readInt();
        this.mMaxFingers = in.readInt();
        this.mMaxFingersPerUser = in.readInt();

        this.mSupportKeyMode = in.readInt();
        this.mSupportFFMode = in.readInt();
        this.mSupportPowerKeyFeature = in.readInt();
        this.mForbiddenUntrustedEnroll = in.readInt();
        this.mForbiddenEnrollDuplicateFingers = in.readInt();
        this.mSupportBioAssay = in.readInt();
        this.mSupportPerformanceDump = in.readInt();

        this.mSupportNavMode = in.readInt();
        this.mNavDoubleClickTime = in.readInt();
        this.mNavLongPressTime = in.readInt();

        this.mEnrollingMinTemplates = in.readInt();

        this.mValidImageQualityThreshold = in.readInt();
        this.mValidImageAreaThreshold = in.readInt();
        this.mDuplicateFingerOverlayScore = in.readInt();
        this.mIncreaseRateBetweenStitchInfo = in.readInt();

        this.mSupportImageRescan = in.readInt();
        this.mRescanImageQualityThreshold = in.readInt();
        this.mRescanImageAreaThreshold = in.readInt();
        this.mRescanRetryCount = in.readInt();

        this.mScreenOnAuthenticateFailRetryCount = in.readInt();
        this.mScreenOffAuthenticateFailRetryCount = in.readInt();

        this.mScreenOnValidTouchFrameThreshold = in.readInt();
        this.mScreenOffValidTouchFrameThreshold = in.readInt();
        this.mImageQualityThresholdForMistakeTouch = in.readInt();

        this.mAuthenticateOrder = in.readInt();

        this.mReissueKeyDownWhenEntryFfMode = in.readInt();
        this.mReissueKeyDownWhenEntryImageMode = in.readInt();

        this.mSupportSensorBrokenCheck = in.readInt();
        this.mBrokenPixelThresholdForDisableSensor = in.readInt();
        this.mBrokenPixelThresholdForDisableStudy = in.readInt();

        this.mBadPointTestMaxFrameNumber = in.readInt();

        this.mReportKeyEventOnlyEnrollAuthenticate = in.readInt();

        this.mRequireDownAndUpInPairsForImageMode = in.readInt();
        this.mRequireDownAndUpInPairsForFFMode = in.readInt();
        this.mRequireDownAndUpInPairsForKeyMode = in.readInt();
        this.mRequireDownAndUpInPairsForNavMode = in.readInt();

        this.mSupportSetSpiSpeedInTEE = in.readInt();
        this.mSupportFrrAnalysis = in.readInt();
        this.mTemplateUpateSaveThreshold = in.readInt();
        this.mSupportImageSegment = in.readInt();
        this.mSupportBaikalContinuousSampling = in.readInt();
        this.mContinuousSamplingNumber = in.readInt();
    }

    public GFConfig(GFConfig config) {
        this.mChipType = config.mChipType;
        this.mChipSeries = config.mChipSeries;
        this.mMaxFingers = config.mMaxFingers;
        this.mMaxFingersPerUser = config.mMaxFingersPerUser;
        this.mSupportKeyMode = config.mSupportKeyMode;
        this.mSupportFFMode = config.mSupportFFMode;
        this.mSupportPowerKeyFeature = config.mSupportPowerKeyFeature;
        this.mForbiddenUntrustedEnroll = config.mForbiddenUntrustedEnroll;
        this.mForbiddenEnrollDuplicateFingers = config.mForbiddenEnrollDuplicateFingers;
        this.mSupportBioAssay = config.mSupportBioAssay;
        this.mSupportPerformanceDump = config.mSupportPerformanceDump;

        this.mSupportNavMode = config.mSupportNavMode;
        this.mNavDoubleClickTime = config.mNavDoubleClickTime;
        this.mNavLongPressTime = config.mNavLongPressTime;

        this.mEnrollingMinTemplates = config.mEnrollingMinTemplates;

        this.mValidImageQualityThreshold = config.mValidImageQualityThreshold;
        this.mValidImageAreaThreshold = config.mValidImageAreaThreshold;
        this.mDuplicateFingerOverlayScore = config.mDuplicateFingerOverlayScore;
        this.mIncreaseRateBetweenStitchInfo = config.mIncreaseRateBetweenStitchInfo;

        this.mSupportImageRescan = config.mSupportImageRescan;
        this.mRescanImageQualityThreshold = config.mRescanImageQualityThreshold;
        this.mRescanImageAreaThreshold = config.mRescanImageAreaThreshold;
        this.mRescanRetryCount = config.mRescanRetryCount;

        this.mScreenOnAuthenticateFailRetryCount = config.mScreenOnAuthenticateFailRetryCount;
        this.mScreenOffAuthenticateFailRetryCount = config.mScreenOffAuthenticateFailRetryCount;

        this.mScreenOnValidTouchFrameThreshold = config.mScreenOnValidTouchFrameThreshold;
        this.mScreenOffValidTouchFrameThreshold = config.mScreenOffValidTouchFrameThreshold;
        this.mImageQualityThresholdForMistakeTouch = config.mImageQualityThresholdForMistakeTouch;

        this.mAuthenticateOrder = config.mAuthenticateOrder;

        this.mReissueKeyDownWhenEntryFfMode = config.mReissueKeyDownWhenEntryFfMode;
        this.mReissueKeyDownWhenEntryImageMode = config.mReissueKeyDownWhenEntryImageMode;

        this.mSupportSensorBrokenCheck = config.mSupportSensorBrokenCheck;
        this.mBrokenPixelThresholdForDisableSensor = config.mBrokenPixelThresholdForDisableSensor;
        this.mBrokenPixelThresholdForDisableStudy = config.mBrokenPixelThresholdForDisableStudy;

        this.mBadPointTestMaxFrameNumber = config.mBadPointTestMaxFrameNumber;

        this.mReportKeyEventOnlyEnrollAuthenticate = config.mReportKeyEventOnlyEnrollAuthenticate;

        this.mRequireDownAndUpInPairsForImageMode = config.mRequireDownAndUpInPairsForImageMode;
        this.mRequireDownAndUpInPairsForFFMode = config.mRequireDownAndUpInPairsForFFMode;
        this.mRequireDownAndUpInPairsForKeyMode = config.mRequireDownAndUpInPairsForKeyMode;
        this.mRequireDownAndUpInPairsForNavMode = config.mRequireDownAndUpInPairsForNavMode;

        this.mSupportSetSpiSpeedInTEE = config.mSupportSetSpiSpeedInTEE;
        this.mSupportFrrAnalysis = config.mSupportFrrAnalysis;
        this.mTemplateUpateSaveThreshold = config.mTemplateUpateSaveThreshold;
        this.mSupportImageSegment = config.mSupportImageSegment;
        this.mSupportBaikalContinuousSampling = config.mSupportBaikalContinuousSampling;
        this.mContinuousSamplingNumber = config.mContinuousSamplingNumber;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(mChipType);
        out.writeInt(mChipSeries);
        out.writeInt(mMaxFingers);
        out.writeInt(mMaxFingersPerUser);
        out.writeInt(mSupportKeyMode);
        out.writeInt(mSupportFFMode);
        out.writeInt(mSupportPowerKeyFeature);
        out.writeInt(mForbiddenUntrustedEnroll);
        out.writeInt(mForbiddenEnrollDuplicateFingers);
        out.writeInt(mSupportBioAssay);
        out.writeInt(mSupportPerformanceDump);

        out.writeInt(mSupportNavMode);
        out.writeInt(mNavDoubleClickTime);
        out.writeInt(mNavLongPressTime);

        out.writeInt(mEnrollingMinTemplates);

        out.writeInt(mValidImageQualityThreshold);
        out.writeInt(mValidImageAreaThreshold);
        out.writeInt(mDuplicateFingerOverlayScore);
        out.writeInt(mIncreaseRateBetweenStitchInfo);

        out.writeInt(mSupportImageRescan);
        out.writeInt(mRescanImageQualityThreshold);
        out.writeInt(mRescanImageAreaThreshold);
        out.writeInt(mRescanRetryCount);

        out.writeInt(mScreenOnAuthenticateFailRetryCount);
        out.writeInt(mScreenOffAuthenticateFailRetryCount);

        out.writeInt(mScreenOnValidTouchFrameThreshold);
        out.writeInt(mScreenOffValidTouchFrameThreshold);
        out.writeInt(mImageQualityThresholdForMistakeTouch);

        out.writeInt(mAuthenticateOrder);

        out.writeInt(mReissueKeyDownWhenEntryFfMode);
        out.writeInt(mReissueKeyDownWhenEntryImageMode);

        out.writeInt(mSupportSensorBrokenCheck);
        out.writeInt(mBrokenPixelThresholdForDisableSensor);
        out.writeInt(mBrokenPixelThresholdForDisableStudy);

        out.writeInt(mBadPointTestMaxFrameNumber);

        out.writeInt(mReportKeyEventOnlyEnrollAuthenticate);

        out.writeInt(mRequireDownAndUpInPairsForImageMode);
        out.writeInt(mRequireDownAndUpInPairsForFFMode);
        out.writeInt(mRequireDownAndUpInPairsForKeyMode);
        out.writeInt(mRequireDownAndUpInPairsForNavMode);

        out.writeInt(mSupportSetSpiSpeedInTEE);
        out.writeInt(mSupportFrrAnalysis);
        out.writeInt(mTemplateUpateSaveThreshold);
        out.writeInt(mSupportImageSegment);
        out.writeInt(mSupportBaikalContinuousSampling);
        out.writeInt(mContinuousSamplingNumber);
    }

    public static final Parcelable.Creator<GFConfig> CREATOR = new Parcelable.Creator<GFConfig>() {
        @Override
        public GFConfig createFromParcel(Parcel in) {
            return new GFConfig(in);
        }

        @Override
        public GFConfig[] newArray(int size) {
            return new GFConfig[size];
        }
    };

}
