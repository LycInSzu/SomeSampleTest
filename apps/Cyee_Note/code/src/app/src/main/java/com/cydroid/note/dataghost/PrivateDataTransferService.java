package com.cydroid.note.dataghost;

import android.app.Service;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import com.cydroid.note.common.Log;

import com.gionee.dataghost.plugin.aidl.IDataGhostService;
import com.gionee.dataghost.plugin.vo.FileInfo;
import com.gionee.dataghost.plugin.vo.PathInfo;
import com.gionee.dataghost.plugin.vo.PluginInfo;
import com.cydroid.note.R;
import com.cydroid.note.app.reminder.ReminderManager;
import com.cydroid.note.common.Constants;
import com.cydroid.note.common.FileUtils;
import com.cydroid.note.common.NoteUtils;
import com.cydroid.note.common.StorageUtils;
import com.cydroid.note.data.SecretNoteItem;
import com.cydroid.note.provider.NoteContract;
import com.cydroid.note.provider.NoteProvider;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by xiaozhilong on 9/20/16.
 */

public class PrivateDataTransferService extends Service {
    private static final String TAG = PrivateDataTransferService.class.getSimpleName();

    private final IDataGhostService.Stub mBinder = new IDataGhostService.Stub() {
        @Override
        public PluginInfo getPluginInfo() throws RemoteException {
            PluginInfo pluginInfo = new PluginInfo();
            pluginInfo.setName(getString(R.string.app_name));
            pluginInfo.setSize(getSize());

            Log.d(TAG, "Private getPluginInfo, size = " + pluginInfo.getSize());
            return pluginInfo;
        }

        @Override
        public boolean prepare() throws RemoteException {
            Log.d(TAG, "Private prepare");
            return true;
        }

        @Override
        public List<FileInfo> getFileInfo() throws RemoteException {
            Log.d(TAG, "Private getFileInfo");
            Context context = PrivateDataTransferService.this.getApplicationContext();
            List<String> rootPaths = StorageUtils.getLocalRootPath(context);
            String photoPath = "/" + Environment.DIRECTORY_PICTURES + "/.NoteMedia/photo";
            String soundPath = "/" + Environment.DIRECTORY_PICTURES + "/.NoteMedia/sound";
            String thumbnail = "/" + Environment.DIRECTORY_PICTURES + "/.NoteMedia/thumbnail";
            List<FileInfo> infos = new ArrayList<>();

            //get Media file
            getMediaFileInfo(infos, rootPaths, photoPath);
            getMediaFileInfo(infos, rootPaths, soundPath);
            getMediaFileInfo(infos, rootPaths, thumbnail);

            //get database
            getDatabaseFileInfo(context, infos);

            return infos;
        }


        @Override
        public void handleSendCompleted() throws RemoteException {
            Log.d(TAG, "Private handleSendCompleted");
        }

        @Override
        public boolean restore(List<String> list, long versionCode) throws RemoteException {
            return true;
        }

        @Override
        public boolean restoreV2(List<PathInfo> list, long versionCode) throws RemoteException {
            Log.d(TAG, "Private restoreV2");
            ContentResolver resolver = getContentResolver();
            for (PathInfo info : list) {
                String sendPath = info.getSendPath();
                String receivePath = info.getReceivePath();

                if (receivePath.endsWith(NoteProvider.DATABASE_NAME)) {
                    restoreDBData(resolver, receivePath);
                } else {
                    restoreMediaFile(sendPath, receivePath);
                }
            }

            restoreNoteReminder(getApplicationContext());

            return true;
        }

        @Override
        public void handleRestoreCompleted() throws RemoteException {
            stopSelf();
            System.exit(0);//NOSONAR
            android.os.Process.killProcess(android.os.Process.myPid());
        }

        private void restoreDBData(ContentResolver resolver, String noteDbPath) {
            SQLiteDatabase db = null;
            try {
                db = SQLiteDatabase.openDatabase(noteDbPath, null, SQLiteDatabase.OPEN_READONLY);

                restoreSecretNote(resolver, db);

                File dbFile = new File(noteDbPath);
                dbFile.delete();//NOSONAR
            } finally {
                NoteUtils.closeSilently(db);
            }

        }

        private void restoreSecretNote(ContentResolver resolver, SQLiteDatabase db) {
            String tableName = NoteContract.NoteContent.SECRET_TABLE_NAME;
            String[] projection = SecretNoteItem.NOTE_PROJECTION;
            Cursor cursor = db.query(tableName, projection, null, null, null, null, null);

            if (cursor == null) {
                Log.d(TAG, "Private restoreSecretNote fail.");
                return;
            }

            ArrayList<ContentProviderOperation> insertOps = new ArrayList<>();
            Uri uri = NoteContract.NoteContent.SECRET_CONTENT_URI;
            try {
                while (cursor.moveToNext()) {
                    ContentValues values = new ContentValues();
                    values.put(NoteContract.NoteContent.COLUMN_TITLE, cursor.getString(SecretNoteItem.INDEX_TITLE));
                    values.put(NoteContract.NoteContent.COLUMN_CONTENT, cursor.getString(SecretNoteItem.INDEX_CONTENT));
                    values.put(NoteContract.NoteContent.COLUMN_LABEL, cursor.getString(SecretNoteItem.INDEX_LABEL));
                    values.put(NoteContract.NoteContent.COLUMN_DATE_CREATED, cursor.getLong(SecretNoteItem.INDEX_DATE_CREATED));
                    values.put(NoteContract.NoteContent.COLUMN_DATE_MODIFIED, cursor.getLong(SecretNoteItem.INDEX_DATE_MODIFIED));
                    values.put(NoteContract.NoteContent.COLUMN_REMINDER, cursor.getLong(SecretNoteItem.INDEX_REMINDER));
                    values.put(NoteContract.NoteContent.COLUMN_ENCRYPT_HINT_STATE, cursor.getInt(SecretNoteItem.INDEX_ENCRYPT_HINT_STATE));
                    values.put(NoteContract.NoteContent.CLOUMN_ENCRYPT_REMIND_READ_STATE, cursor.getInt(SecretNoteItem.INDEX_ENCRYPT_REMIND_READ_STATE));

                    ContentProviderOperation ops = ContentProviderOperation.newInsert(uri)
                            .withValues(values)
                            .build();
                    insertOps.add(ops);
                }
            } finally {
                NoteUtils.closeSilently(cursor);
            }

            if (insertOps.size() == 0) {
                return;
            }
            try {
                resolver.applyBatch(NoteContract.AUTHORITY, insertOps);
            } catch (Exception e) {
                Log.d(TAG, "Private applyBatch secret notes fail : " + e);
            }
        }

        private void restoreMediaFile(String sendPath, String receivePath) {
            File sourceFile = new File(receivePath);
            File targetFile = new File(sendPath);
            if (targetFile.exists()) {
                return;
            }

            try {
                File parentDir = targetFile.getParentFile();
                if (!parentDir.exists()) {
                    boolean result = parentDir.mkdirs();
                    if (!result) {
                        Log.d(TAG, "Private restoreMediaFile, mkDirs fail");
                        return;
                    }
                }
                boolean result = targetFile.createNewFile();
                if (!result) {
                    Log.d(TAG, "Private restoreMediaFile, createNewFile fail");
                    return;
                }

                FileUtils.copyFile(sourceFile, targetFile);
                sourceFile.delete();//NOSONAR
            } catch (IOException e) {
                Log.d(TAG, "Private restoreMediaFile fail. : " + e);
            }
        }

        private void restoreNoteReminder(Context context) {
            ReminderManager.scheduleItemReminder(context, NoteContract.NoteContent.SECRET_CONTENT_URI, true);
        }

        private void getMediaFileInfo(List<FileInfo> infos, List<String> rootPaths, String mediaPath) {
            for (String rootPath : rootPaths) {
                String absoluteRootPath = Constants.SECURITY_OS_ENCRYPT_PATH + rootPath + mediaPath;
                File mediaDir = new File(absoluteRootPath);
                if (!mediaDir.exists() || !mediaDir.isDirectory()) {
                    continue;
                }
                File[] files = mediaDir.listFiles();
                if (files == null || files.length == 0) {
                    continue;
                }

                for (File file : files) {
                    FileInfo fileInfo = new FileInfo();
                    fileInfo.setFile(true);
                    fileInfo.setPath(file.getAbsolutePath());
                    fileInfo.setSystemData(true);
                    infos.add(fileInfo);
                }
            }
        }

        private void getDatabaseFileInfo(Context context, List<FileInfo> infos) {
            File databaseFile = context.getDatabasePath(NoteProvider.DATABASE_NAME);
            if (!databaseFile.exists()) {
                return;
            }

            FileInfo info = new FileInfo();
            info.setFile(true);
            info.setPath(databaseFile.getAbsolutePath());
            info.setSystemData(true);
            infos.add(info);
        }

        private long getSize() {
            Context context = PrivateDataTransferService.this.getApplicationContext();
            List<String> rootPaths = StorageUtils.getLocalRootPath(context);
            String mediaPath = "/" + Environment.DIRECTORY_PICTURES + "/.NoteMedia";
            long totalSize = 0;
            for (String rootPath : rootPaths) {
                String absoluteRootPath = Constants.SECURITY_OS_ENCRYPT_PATH + rootPath + mediaPath;
                File dir = new File(absoluteRootPath);
                if (!dir.exists()) {
                    continue;
                }
                totalSize += FileUtils.getFileTotalSize(dir);
            }

            File databaseFile = context.getDatabasePath(NoteProvider.DATABASE_NAME);
            totalSize += databaseFile.length();

            return totalSize;
        }
    };


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }
}
