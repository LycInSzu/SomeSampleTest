package com.gionee.framework.storage;

interface Storage {

    String TAG = "StorageMgr";

    boolean isSupportTwoSdcard();

    boolean hasTwoSdcard();

    boolean isNoSdcardMemory();

    boolean isSdcardAvailable();

    String getSdcardRootPath();

    long getSdcardTotalSize();

    long getSdcardAvailableSize();

    String getInternalAppFilesPath();

    long getInternalAvailableSize();

    long getInternalTotalSize();

    boolean isNoInternalMemory();

    void setOnSdcardStatusListener(SdcardStatusListener listener);

    void setOffSdcardStatusListener(SdcardStatusListener listener);

    String getExternalFilesDir(String path);
}
