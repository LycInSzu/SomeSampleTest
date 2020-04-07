package com.gionee.framework.storage;

//import com.gionee.amiweather.framework.ApplicationProperty;

import com.cydroid.note.common.Log;

public final class StorageMgr implements Storage {


    private Storage mImplStorageManager;

    private StorageMgr() {
/*        if (ApplicationProperty.isGioneeVersion()) {
            mImplStorageManager = new CyeeStorageManager();
        } else {
            mImplStorageManager = new NormalSdkStorageManager();
        }*/
        mImplStorageManager = new NormalSdkStorageManager();
    }

    private final static class Holder {
        private static final StorageMgr INSTANCE = new StorageMgr();
    }

    public static StorageMgr getInstance() {
        return Holder.INSTANCE;
    }

    @Override
    public boolean isSupportTwoSdcard() {
        return mImplStorageManager.isSupportTwoSdcard();
    }

    @Override
    public boolean hasTwoSdcard() {
        return mImplStorageManager.hasTwoSdcard();
    }

    @Override
    public boolean isNoSdcardMemory() {
        return mImplStorageManager.isNoSdcardMemory();
    }

    @Override
    public boolean isSdcardAvailable() {
        return mImplStorageManager.isSdcardAvailable();
    }

    @Override
    public String getSdcardRootPath() {
        Log.d(TAG, "sdcard path = " + mImplStorageManager.getSdcardRootPath());
        return mImplStorageManager.getSdcardRootPath();
    }

    @Override
    public long getSdcardTotalSize() {
        return mImplStorageManager.getSdcardTotalSize();
    }

    @Override
    public long getSdcardAvailableSize() {
        return mImplStorageManager.getSdcardAvailableSize();
    }

    @Override
    public String getInternalAppFilesPath() {
        return mImplStorageManager.getInternalAppFilesPath();
    }

    @Override
    public long getInternalAvailableSize() {
        return mImplStorageManager.getInternalAvailableSize();
    }

    @Override
    public long getInternalTotalSize() {
        return mImplStorageManager.getInternalTotalSize();
    }

    @Override
    public boolean isNoInternalMemory() {
        return mImplStorageManager.isNoInternalMemory();
    }

    @Override
    public void setOnSdcardStatusListener(SdcardStatusListener listener) {
        mImplStorageManager.setOnSdcardStatusListener(listener);
    }

    @Override
    public void setOffSdcardStatusListener(SdcardStatusListener listener) {
        mImplStorageManager.setOffSdcardStatusListener(listener);
    }

    @Override
    public String getExternalFilesDir(String path) {
        return mImplStorageManager.getExternalFilesDir(path);
    }

}
