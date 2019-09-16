package com.cydroid.ota.logic.sync;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Message;
import android.provider.MediaStore;
import android.webkit.MimeTypeMap;
import com.cydroid.ota.Log;
import com.cydroid.ota.logic.utils.VerifyZipForUpgrade;
import com.cydroid.ota.logic.config.EnvConfig;
import com.cydroid.ota.utils.*;
import com.cydroid.ota.utils.Error;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by borney on 4/21/15.
 */
public class ScanAvailableJob extends Job {
    private static final String TAG = "ScanAvailableJob";
    private final Object mObject = new Object();
    private Context mContext;
    private boolean isRoot;

    public ScanAvailableJob(Context context, boolean isRoot, ISyncCallback callback) {
        super(callback);
        this.mContext = context;
        this.isRoot = isRoot;
    }

    @Override
    public <T> T run() {
        if (!StorageUtils.checkExternalStorageMounted()
                && !StorageUtils.checkInternalStorageMounted(mContext)) {
            sendMessage(MSG.MSG_STORAGE_NOT_MOUNTED);
            return null;
        }

        List<String> dirlist = StorageUtils.getAllMountedStorageVolumesPath(mContext);
        Log.d(TAG, "dirlist size " + dirlist.size());
        String[] paths = new String[dirlist.size()];
        for (int i = 0; i < dirlist.size(); i++) {
            paths[i] = dirlist.get(i);
        }
        scanFile(mContext, paths, null, new MediaScannerConnection.OnScanCompletedListener() {
            @Override
            public void onScanCompleted(String path, Uri uri) {
                Log.d(TAG, "------onScanCompleted-----");
                synchronized (mObject) {
                    mObject.notifyAll();
                }
            }
        });
        try {
            synchronized (mObject) {
                Log.d(TAG, "------startScan-----");
                mObject.wait(Integer.MAX_VALUE);
            }
        } catch (InterruptedException e) {

        }

        List<String> dirs = scanDirByDB(mContext.getApplicationContext(), "zip");
        Log.d(TAG, "return java scanner size = " + dirs.size());

        ArrayList<String> availableFiles = new ArrayList<String>();

        boolean testLocalScan = EnvConfig.isTestLocalScan();
        Log.d(TAG, "testLocalScan = " + testLocalScan);

        for (String path : dirs) {
            if (testLocalScan) {
                availableFiles.add(path);
            } else if (VerifyZipForUpgrade.verifyZipForUpgrade(path, isRoot)) {
                availableFiles.add(path);
            }
        }
        sendMessage(MSG.MSG_SCAN_AVAILABLE_RESULT, availableFiles);
        return null;
    }

    @Override
    void handleJobMessage(Message msg) {
        Log.d(TAG, "msg " + msg);
        switch (msg.what) {
            case MSG.MSG_STORAGE_NOT_MOUNTED:
                mJobCallback.onError(Error.ERROR_CODE_STORAGE_NOT_MOUNTED);
                break;
            case MSG.MSG_SCAN_AVAILABLE_RESULT:
                ArrayList<String> availableFiles = (ArrayList<String>) msg.obj;
                mJobCallback.onResult(availableFiles);
                break;
            default:
                break;
        }
    }

    private List<String> scanDirByDB(final Context context, final String type) {
        Log.d(TAG, "scanDirByDB()");
        final List<String> files = new ArrayList<String>();
        ContentResolver cr = context.getContentResolver();
        Uri uri = MediaStore.Files.getContentUri("external");
        String[] projection = null;
        String selectionMimeType = MediaStore.Files.FileColumns.MIME_TYPE + "=?";
        String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(type);
        String[] selectionArgsZip = new String[]{mimeType};
        Cursor cursor = cr.query(uri, projection, selectionMimeType, selectionArgsZip, null);
        if (cursor == null) {
            Log.e(TAG, "scanDirByDB cursor = null");
            return files;
        }
        Log.d(TAG, "scanDirByDB = " + cursor.getColumnCount());
        int dataIndex = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA);
        try {
            if (cursor.moveToFirst()) {
                do {
                    String dir = cursor.getString(dataIndex);
                    Log.i(TAG, dir);
                    files.add(dir);
                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
        }
        return files;
    }

    private MediaScannerConnection scanFile(Context context, String[] filePath, String[] mineType,
                                            MediaScannerConnection.OnScanCompletedListener sListener) {
        Log.d(TAG, "scanFile");
        ClientProxy client = new ClientProxy(filePath, mineType, sListener);

        try {
            MediaScannerConnection connection = new MediaScannerConnection(
                    context.getApplicationContext(), client);
            Log.d(TAG, "connection = " + connection);
            client.setConnection(connection);
            client.connect();
            return connection;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static final class ClientProxy implements MediaScannerConnection.MediaScannerConnectionClient {
        final String[] mPaths;
        final String[] mMimeTypes;
        final MediaScannerConnection.OnScanCompletedListener scanCompletedListener;
        private MediaScannerConnection mConnection;
        int mNextPathIndex = 0;

        ClientProxy(String[] paths, String[] mimeTypes,
                    MediaScannerConnection.OnScanCompletedListener client) {
            mPaths = paths;
            mMimeTypes = mimeTypes;
            scanCompletedListener = client;
        }

        void setConnection(MediaScannerConnection connection) {
            this.mConnection = connection;
        }

        void connect() {
            if (mConnection != null) {
                mConnection.connect();
            } else {
                Log.e(TAG, "connect mConnect == null");
            }
        }

        public void onMediaScannerConnected() {
            Log.d(TAG, "onMediaScannerConnected t = " + Thread.currentThread().getName());
            scanNextPath();
        }

        public void onScanCompleted(String path, Uri uri) {
            Log.d(TAG, "onScanCompleted " + path + " t = " + Thread.currentThread().getName());
            if (scanCompletedListener != null && mNextPathIndex == mPaths.length) {
                scanCompletedListener.onScanCompleted(path, uri);
            }
            scanNextPath();
        }

        void scanNextPath() {
            if (mConnection == null) {
                Log.e(TAG, "mConnection = null!!!");
                return;
            }
            if (mNextPathIndex >= mPaths.length) {
                mConnection.disconnect();
                return;
            }
            String mimeType = mMimeTypes != null ? mMimeTypes[mNextPathIndex] : null;
            mConnection.scanFile(mPaths[mNextPathIndex], mimeType);
            mNextPathIndex++;
            if (mNextPathIndex >= mPaths.length) {
                mConnection.disconnect();
                Log.d("TAG", "scanNextPath " + mConnection.isConnected());
                return;
            }
        }
    }

    private static final class MSG extends Job.MSG {
        private static final int BASE_MSG = BASE * 4;

        /*static {
            Log.d(TAG, TAG + " BASE_MSG = " + BASE_MSG);
        }*/

        static final int MSG_STORAGE_NOT_MOUNTED = BASE_MSG + 1;
        static final int MSG_SCAN_AVAILABLE_RESULT = BASE_MSG + 2;
    }
}
