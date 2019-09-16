package com.cydroid.ota.bean;

import java.io.Serializable;

/**
 * Created by borney on 4/21/15.
 */
public class SettingUpdateInfo implements IUpdateInfo, Serializable {
    private static final long serialVersionUID = 1L;
    private String mReleaseNote = "";
    private String mMd5 = "";
    private String mDownloadUrl = "";
    private String mVersion = "";
    private long mFileSize = 0;
    private boolean isPreRelease;
    private String mVersionReleaseDate;
    private String mReleaseNoteUrl = "";
    private String mReleaseNotesId = "";
    private String mInternalVer = "";
    private int mDownloadedPeopleNum;
    private boolean mExtPkg = false;
    private boolean isBackUp = false;
    private String mSimpleReleaseNote;

    @Override
    public String getSimpleReleaseNote() {
        return mSimpleReleaseNote;
    }

    public void setSimpleReleaseNote(String simpleReleaseNote) {
        mSimpleReleaseNote = simpleReleaseNote;
    }

    @Override
    public String getReleaseNote() {
        return mReleaseNote;
    }

    public void setReleaseNote(String releaseNote) {
        mReleaseNote = releaseNote;
    }
    
    public void setReleaseNoteId(String releaseNoteId) {
        mReleaseNotesId = releaseNoteId;
    }

    @Override
    public String getMd5() {
        return mMd5;
    }

    public void setMd5(String md5) {
        mMd5 = md5;
    }

    @Override
    public String getDownloadUrl() {
        return mDownloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        mDownloadUrl = downloadUrl;
    }

    @Override
    public String getVersion() {
        return mVersion;
    }

    public void setVersion(String version) {
        mVersion = version;
    }

    @Override
    public long getFileSize() {
        return mFileSize;
    }

    public void setFileSize(long fileSize) {
        mFileSize = fileSize;
    }

    @Override
    public String getFileSize(String fileSizeFormat, long fileSize) {
        float size = (float) (fileSize / (1024.00 * 1024.00));
        return String.format(fileSizeFormat, size);
    }

    @Override
    public boolean isPreRelease() {
        return isPreRelease;
    }

    public void setPreRelease(boolean isPreRelease) {
        this.isPreRelease = isPreRelease;
    }

    @Override
    public String getVersionReleaseDate() {
        return mVersionReleaseDate;
    }

    public void setVersionReleaseDate(String mVersionDate) {
        this.mVersionReleaseDate = mVersionDate;
    }

    @Override
    public boolean isExtPkg() {
        return mExtPkg;
    }

    public void setExtPkg(boolean mExtPkg) {
        this.mExtPkg = mExtPkg;
    }

    @Override
    public String getReleaseNoteUrl() {
        return mReleaseNoteUrl;
    }
    
    @Override
    public String getReleaseNotesId() {
        return mReleaseNotesId;
    }

    public void setReleaseNoteUrl(String releaseNoteUrl) {
        mReleaseNoteUrl = releaseNoteUrl;
    }

    @Override
    public String getInternalVer() {
        return mInternalVer;
    }

    public void setInternalVer(String internalVer) {
        mInternalVer = internalVer;
    }

    @Override
    public int getDownloadedPeopleNum() {
        return mDownloadedPeopleNum;
    }

    public void setDownloadedPeopleNum(int downloadedNum) {
        mDownloadedPeopleNum = downloadedNum;
    }
    public boolean isBackup() {
        return isBackUp;
    }

    public void setBackUp(boolean isBackUp) {
        this.isBackUp = isBackUp;
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("mReleaseNote = ");
        sb.append(mReleaseNote);
        sb.append("  mMd5 = ");
        sb.append(mMd5);
        sb.append("  mDownloadUrl = ");
        sb.append(mDownloadUrl);
        sb.append("  mVersion = ");
        sb.append(mVersion);
        sb.append("  mFileSize = ");
        sb.append(mFileSize);
        sb.append("  isPreRelease = ");
        sb.append(isPreRelease);
        sb.append("  mVersionDate = ");
        sb.append(mVersionReleaseDate);
        sb.append("  mReleaseNoteUrl = ");
        sb.append(mReleaseNoteUrl);
        sb.append("  mInternalVer = ");
        sb.append(mInternalVer);
        sb.append("  mDownloadedPeopleNum = ");
        sb.append(mDownloadedPeopleNum);
        sb.append("  mExtPkg = ");
        sb.append(mExtPkg);
        return sb.toString();
    }
}
