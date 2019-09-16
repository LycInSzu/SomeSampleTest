package com.cydroid.ota.bean;

/**
 * Created by borney on 4/21/15.
 */
public interface IUpdateInfo {
    String getSimpleReleaseNote();

    String getReleaseNote();

    String getMd5();

    String getDownloadUrl();

    String getVersion();

    long getFileSize();

    String getFileSize(String fileSizeFormat, long fileSize);

    boolean isPreRelease();

    String getVersionReleaseDate();

    boolean isExtPkg();

    String getReleaseNoteUrl();
    
    String getReleaseNotesId();

    String getInternalVer();

    int getDownloadedPeopleNum();

    boolean isBackup();
}
