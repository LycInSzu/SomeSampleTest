package com.gionee.framework.storage;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Environment;
import android.os.StatFs;

import com.gionee.framework.component.ApplicationContextHolder;
import com.cydroid.note.common.Log;

import java.io.File;
import java.util.concurrent.CopyOnWriteArrayList;

abstract class AbstractStorageManager implements Storage, ApplicationContextHolder {
    private static final CopyOnWriteArrayList<SdcardStatusListener> ALL_LISTENERS = new CopyOnWriteArrayList<SdcardStatusListener>();
    static final boolean DEBUG = true;

    private enum SdcardStatus {
        ENABLED, DISABLED
    }

    private SdcardStatus mSdcardStatus;
    protected boolean mSdcardAvailble = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);

    AbstractStorageManager() {
        registerSdcardReceiver();
        setupStatus();
    }

    private BroadcastReceiver mSdcardStateChangeListener = new BroadcastReceiver() {

        @Override
        public void onReceive(Context ctx, Intent intent) {
            String action = intent.getAction();
            if (DEBUG) {
                Log.d(TAG, "action = " + action);
                Uri uri = intent.getData();
                String path = uri.getPath();
                Log.d(TAG, "path = " + path);
            }

            onSdcardStatusChange();

            if (action.equals(Intent.ACTION_MEDIA_MOUNTED)) {
                if (mSdcardStatus.equals(SdcardStatus.DISABLED)) {
                    mSdcardAvailble = true;
                    setupStatus();
                    performCallbackTraversal(mSdcardAvailble);
                }
            } else {
                if (mSdcardStatus.equals(SdcardStatus.ENABLED)) {
                    mSdcardAvailble = false;
                    setupStatus();
                    performCallbackTraversal(mSdcardAvailble);
                }
            }

        }
    };

    private void registerSdcardReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        filter.addAction(Intent.ACTION_MEDIA_REMOVED);
        filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        filter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);
        filter.addAction(Intent.ACTION_MEDIA_EJECT);
        filter.addDataScheme("file");
        CONTEXT.registerReceiver(mSdcardStateChangeListener, filter);
    }

    private void setupStatus() {
        mSdcardStatus = mSdcardAvailble ? SdcardStatus.ENABLED : SdcardStatus.DISABLED;
    }

    private void performCallbackTraversal(boolean enabled) {
        for (SdcardStatusListener callback : ALL_LISTENERS) {
            if (enabled) {
                callback.onEnabled();
            } else {
                callback.onDisabled();
            }
        }
    }

    private long getInternalMemorySizeHelper(boolean isTotal) {
        final File path = Environment.getDataDirectory();
        final StatFs stat = new StatFs(path.getPath());
        final long blockSize = stat.getBlockSize();
        if (DEBUG) {
            Log.d(TAG, "Internal storage directory is " + path + ",isTotal is " + isTotal);
        }

        long blocks;
        if (isTotal) {
            blocks = stat.getBlockCount();
        } else {
            blocks = stat.getAvailableBlocks();
        }
        final long totalSize = blockSize * blocks;

        if (DEBUG) {
            Log.d(TAG, "blockSize is " + blockSize + ",blocks is " + blocks + ",totalSize is  " + totalSize);
        }
        return totalSize;
    }

    @Override
    public String getInternalAppFilesPath() {
        return CONTEXT.getFilesDir().getAbsolutePath();
    }

    @Override
    public long getInternalAvailableSize() {
        long availableSize = getInternalMemorySizeHelper(false);

        if (DEBUG) {
            Log.d(TAG, "Internal total available size is  " + (int) availableSize + " KB");
        }

        return availableSize;
    }

    @Override
    public long getInternalTotalSize() {
        long totalSize = getInternalMemorySizeHelper(true);

        if (DEBUG) {
            Log.d(TAG, "Internal total size is  " + (int) totalSize + " KB");
        }

        return totalSize;
    }

    @Override
    public boolean isNoInternalMemory() {
        return getInternalAvailableSize() <= 0;
    }

    @Override
    public void setOnSdcardStatusListener(SdcardStatusListener listener) {
        ALL_LISTENERS.add(listener);
    }

    @Override
    public void setOffSdcardStatusListener(SdcardStatusListener listener) {
        ALL_LISTENERS.remove(listener);
    }

    abstract void onSdcardStatusChange();

    @Override
    public String getExternalFilesDir(String path) {
        File file = CONTEXT.getExternalFilesDir(path);
        if (file != null) {
            return file.getPath();
        } else {
            return getSdcardRootPath() + "/Android/data/com.gionee.amiweather/files/" + path;
        }
    }

}
