package com.cydroid.note.dataghost;

import android.app.Service;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
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
import android.util.SparseIntArray;

import com.gionee.dataghost.plugin.aidl.IDataGhostService;
import com.gionee.dataghost.plugin.vo.FileInfo;
import com.gionee.dataghost.plugin.vo.PathInfo;
import com.gionee.dataghost.plugin.vo.PluginInfo;
import com.cydroid.note.R;
import com.cydroid.note.app.LabelManager;
import com.cydroid.note.app.reminder.ReminderManager;
import com.cydroid.note.common.Constants;
import com.cydroid.note.common.FileUtils;
import com.cydroid.note.common.NoteUtils;
import com.cydroid.note.common.PlatformUtil;
import com.cydroid.note.common.StorageUtils;
import com.cydroid.note.data.LocalNoteItem;
import com.cydroid.note.data.LocalNoteSet;
import com.cydroid.note.data.NoteItem;
import com.cydroid.note.data.SecretNoteItem;
import com.cydroid.note.provider.LabelContract;
import com.cydroid.note.provider.NoteContract;
import com.cydroid.note.provider.NoteProvider;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by xiaozhilong on 5/24/16.
 */
public class DataTransferService extends Service {
    private static final String TAG = "DataTransferService";

    private final IDataGhostService.Stub mBinder = new IDataGhostService.Stub() {
        @Override
        public PluginInfo getPluginInfo() throws RemoteException {
            PluginInfo pluginInfo = new PluginInfo();
            pluginInfo.setName(getString(R.string.app_name));
            pluginInfo.setSize(getSize());

            Log.d(TAG, "getPluginInfo, size = " + pluginInfo.getSize());
            return pluginInfo;
        }

        @Override
        public boolean prepare() throws RemoteException {
            Log.d(TAG, "prepare");
            return true;
        }

        @Override
        public List<FileInfo> getFileInfo() throws RemoteException {
            Log.d(TAG, "getFileInfo");
            Context context = DataTransferService.this.getApplicationContext();
            List<String> rootPaths = StorageUtils.getLocalRootPath(context);
            String photoPath = "/" + Environment.DIRECTORY_PICTURES + "/.NoteMedia/photo";
            String soundPath = "/" + Environment.DIRECTORY_PICTURES + "/.NoteMedia/sound";
            String thumbnail = "/" + Environment.DIRECTORY_PICTURES + "/.NoteMedia/thumbnail";
            List<FileInfo> infos = new ArrayList<>();

            //get Media file
            getMediaFileInfo(infos, rootPaths, photoPath);
            getMediaFileInfo(infos, rootPaths, soundPath);
            getMediaFileInfo(infos, rootPaths, thumbnail);

            //get Encrypt file
            getEncryptFile(infos);

            //get database
            getDatabaseFileInfo(context, infos);

            return infos;
        }


        @Override
        public void handleSendCompleted() throws RemoteException {
            Log.d(TAG, "handleSendCompleted");
        }

        @Override
        public boolean restore(List<String> list, long versionCode) throws RemoteException {
            return true;
        }

        @Override
        public boolean restoreV2(List<PathInfo> list, long versionCode) throws RemoteException {
            Log.d(TAG, "restoreV2");
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

                SparseIntArray labelOldMapNew = new SparseIntArray();
                restoreLabel(resolver, db, labelOldMapNew);
                restoreNote(resolver, db, labelOldMapNew);
                restoreSecretNote(resolver, db, labelOldMapNew);

                File dbFile = new File(noteDbPath);
                dbFile.delete();//NOSONAR
            } finally {
                NoteUtils.closeSilently(db);
            }

        }

        private void restoreLabel(ContentResolver resolver, SQLiteDatabase db, SparseIntArray map) {
            String tableName = LabelContract.LabelContent.TABLE_NAME;
            String[] projection = LabelManager.LABEL_PROJECTION;
            Cursor cursor = db.query(tableName, projection, null, null, null, null, null);

            if (cursor == null) {
                Log.d(TAG, "restoreLabel fail.");
                return;
            }

            ArrayList<ContentProviderOperation> insertOps = new ArrayList<>();
            Uri labelUri = LabelContract.LabelContent.CONTENT_URI;
            int[] oldIds = new int[cursor.getCount()];
            int i = 0;
            try {
                while (cursor.moveToNext()) {
                    int id = cursor.getInt(0);
                    String label = cursor.getString(1);
                    oldIds[i++] = id;
                    ContentProviderOperation ops = ContentProviderOperation.newInsert(labelUri)
                            .withValue(LabelContract.LabelContent.COLUMN_LABEL_CONTENT, label)
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
                ContentProviderResult[] results = resolver.applyBatch(LabelContract.AUTHORITY, insertOps);
                i = 0;
                for (ContentProviderResult result : results) {
                    map.put(oldIds[i++], Integer.parseInt(result.uri.getPathSegments().get(1)));
                }
            } catch (Exception e) {
                Log.d(TAG, "applyBatch labels fail : " + e);
            }
        }

        private void restoreNote(ContentResolver resolver, SQLiteDatabase db, SparseIntArray map) {
            String tableName = NoteContract.NoteContent.TABLE_NAME;
            String[] projection = LocalNoteSet.NOTE_PROJECTION;
            Cursor cursor = db.query(tableName, projection, null, null, null, null, null);

            if (cursor == null) {
                Log.d(TAG, "restoreNote fail.");
                return;
            }

            ArrayList<ContentProviderOperation> insertOps = new ArrayList<>();
            Uri noteUri = NoteContract.NoteContent.CONTENT_URI;
            try {
                while (cursor.moveToNext()) {
                    ContentValues values = new ContentValues();
                    values.put(NoteContract.NoteContent.COLUMN_TITLE, cursor.getString(LocalNoteItem.INDEX_TITLE));
                    values.put(NoteContract.NoteContent.COLUMN_CONTENT, cursor.getString(LocalNoteItem.INDEX_CONTENT));
                    String label = convertLabel(cursor.getString(LocalNoteItem.INDEX_LABEL), map);
                    values.put(NoteContract.NoteContent.COLUMN_LABEL, label);
                    values.put(NoteContract.NoteContent.COLUMN_DATE_CREATED, cursor.getLong(LocalNoteItem.INDEX_DATE_CREATED));
                    values.put(NoteContract.NoteContent.COLUMN_DATE_MODIFIED, cursor.getLong(LocalNoteItem.INDEX_DATE_MODIFIED));
                    values.put(NoteContract.NoteContent.COLUMN_REMINDER, cursor.getLong(LocalNoteItem.INDEX_REMINDER));
                    values.put(NoteContract.NoteContent.COLUMN_ENCRYPT_HINT_STATE, cursor.getInt(LocalNoteItem.INDEX_ENCRYPT_HINT_STATE));

                    ContentProviderOperation ops = ContentProviderOperation.newInsert(noteUri)
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
                Log.d(TAG, "applyBatch notes fail : " + e);
            }
        }

        private void restoreSecretNote(ContentResolver resolver, SQLiteDatabase db, SparseIntArray map) {
            if (PlatformUtil.isSecurityOS()) {
                return;
            }

            String tableName = NoteContract.NoteContent.SECRET_TABLE_NAME;
            String[] projection = SecretNoteItem.NOTE_PROJECTION;
            Cursor cursor = db.query(tableName, projection, null, null, null, null, null);

            if (cursor == null) {
                Log.d(TAG, "restoreSecretNote fail.");
                return;
            }

            ArrayList<ContentProviderOperation> insertOps = new ArrayList<>();
            Uri uri = NoteContract.NoteContent.SECRET_CONTENT_URI;
            try {
                while (cursor.moveToNext()) {
                    ContentValues values = new ContentValues();
                    values.put(NoteContract.NoteContent.COLUMN_TITLE, cursor.getString(SecretNoteItem.INDEX_TITLE));
                    values.put(NoteContract.NoteContent.COLUMN_CONTENT, cursor.getString(SecretNoteItem.INDEX_CONTENT));
                    String label = convertLabel(cursor.getString(SecretNoteItem.INDEX_LABEL), map);
                    values.put(NoteContract.NoteContent.COLUMN_LABEL, label);
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
                Log.d(TAG, "applyBatch secret notes fail : " + e);
            }
        }

        private String convertLabel(String oldLabel, SparseIntArray map) {
            if (oldLabel == null) {
                return null;
            }

            String[] temps = oldLabel.split(LocalNoteItem.LABEL_SEPARATOR);
            ArrayList<Integer> label = new ArrayList<>();
            for (String temp : temps) {
                int oldId = Integer.parseInt(temp);
                label.add(map.get(oldId, oldId));
            }
            return NoteItem.convertToStringLabel(label);
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
                        Log.d(TAG, "restoreMediaFile, mkDirs fail");
                        return;
                    }
                }
                boolean result = targetFile.createNewFile();
                if (!result) {
                    Log.d(TAG, "restoreMediaFile, createNewFile fail");
                    return;
                }

                FileUtils.copyFile(sourceFile, targetFile);
                sourceFile.delete();//NOSONAR
            } catch (IOException e) {
                Log.d(TAG, "restoreMediaFile fail. : " + e);
            }
        }

        private void restoreNoteReminder(Context context) {
            ReminderManager.scheduleItemReminder(context, NoteContract.NoteContent.CONTENT_URI, false);

            if (!PlatformUtil.isSecurityOS()) {
                ReminderManager.scheduleItemReminder(context, NoteContract.NoteContent.SECRET_CONTENT_URI, true);
            }
        }

        private void getEncryptFile(List<FileInfo> infos) {
            if (PlatformUtil.isSecurityOS()) {
                return;
            }

            String soundPath = Constants.SOUND_ENCRYPT_PATH;
            File soundDir = new File(soundPath);
            File[] files = soundDir.listFiles();
            if (files == null || files.length == 0) {
                return;
            }

            for (File file : files) {
                FileInfo fileInfo = new FileInfo();
                fileInfo.setFile(true);
                fileInfo.setPath(file.getAbsolutePath());
                fileInfo.setSystemData(true);
                infos.add(fileInfo);
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
            Context context = DataTransferService.this.getApplicationContext();
            List<String> rootPaths = StorageUtils.getLocalRootPath(context);
            String mediaPath = "/" + Environment.DIRECTORY_PICTURES + "/.NoteMedia";
            long totalSize = 0;
            for (String rootPath : rootPaths) {
                String absoluteRootPath = rootPath + mediaPath;
                File dir = new File(absoluteRootPath);
                if (!dir.exists()) {
                    continue;
                }
                totalSize += FileUtils.getFileTotalSize(dir);
            }

            if (!PlatformUtil.isSecurityOS()) {
                File encryptSound = new File(Constants.SOUND_ENCRYPT_PATH);
                totalSize += FileUtils.getFileTotalSize(encryptSound);
            }

            File databaseFile = context.getDatabasePath(NoteProvider.DATABASE_NAME);
            totalSize += databaseFile.length();

            return totalSize;
        }

        private void getMediaFileInfo(List<FileInfo> infos, List<String> rootPaths, String mediaPath) {
            for (String rootPath : rootPaths) {
                String absoluteRootPath = rootPath + mediaPath;
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
                    fileInfo.setSystemData(false);
                    infos.add(fileInfo);
                }
            }
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
