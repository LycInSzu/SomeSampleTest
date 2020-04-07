package com.gionee.framework.storage;

import android.os.Environment;
import android.os.StatFs;

import com.gionee.framework.log.Logger;

class NormalSdkStorageManager extends AbstractStorageManager {

    private static final long THRESHOLD = 100L;//byte

    private static long getSdcardAvailableSpace(boolean isTotal) {
        Logger.printLog(TAG, "getSdcardAvailableSpace");
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            try {
                StatFs sf = new StatFs(Environment.getExternalStorageDirectory().getAbsolutePath());

                long blocks;
                if (isTotal) {
                    blocks = sf.getBlockCount();
                } else {
                    blocks = sf.getAvailableBlocks();
                }

                long length = sf.getBlockSize() * blocks;
                Logger.printLog(TAG, "getSdcardAvailableSpace length = "
                        + length);
                return length;
            } catch (Exception e) {
            }
        }

        return 0;
    }

    @Override
    public boolean isSupportTwoSdcard() {
        return false;
    }

    @Override
    public boolean hasTwoSdcard() {
        return false;
    }

    @Override
    public boolean isNoSdcardMemory() {
        return getSdcardAvailableSpace(false) <= THRESHOLD;
    }

    @Override
    public boolean isSdcardAvailable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    @Override
    public String getSdcardRootPath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath();
    }

    @Override
    public long getSdcardTotalSize() {
        return getSdcardAvailableSpace(true);
    }

    @Override
    public long getSdcardAvailableSize() {
        return getSdcardAvailableSpace(false);
    }

    @Override
    void onSdcardStatusChange() {

    }

}
