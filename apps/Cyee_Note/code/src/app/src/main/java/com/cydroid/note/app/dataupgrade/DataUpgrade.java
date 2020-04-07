package com.cydroid.note.app.dataupgrade;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.text.Spannable;
import com.cydroid.note.common.Log;
import android.util.SparseArray;

import com.cydroid.note.app.BuiltInNote;
import com.cydroid.note.app.DataConvert;
import com.cydroid.note.app.LabelManager;
import com.cydroid.note.app.NoteAppImpl;
import com.cydroid.note.app.reminder.ReminderManager;
import com.cydroid.note.app.span.SoundImageSpan;
import com.cydroid.note.common.Constants;
import com.cydroid.note.common.NoteUtils;
import com.cydroid.note.common.ThreadPool;
import com.cydroid.note.provider.NoteContract;
import com.cydroid.note.provider.NoteProvider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by gaojt on 15-8-13.
 */
public class DataUpgrade {

    private static final boolean DEBUG = false;
    private static final String TAG = "DataUpgrade";
    private static final String OLD_DB_NAME = "Notes";
    private static final String OLD_TABLE_NAME = "items";
    public static final int MAX_LABEL_LENGTH = 12;
    public static final int MAX_TITLE_LENGTH = 15;

    private static final int CODE_OLD_DB_QUERY_COUNT_ZERO = -1;
    private static final int CODE_OK = 0;

    private static final int FAIL_CODE_OPEN_OLD_DB_ERROR = 1;
    private static final int FAIL_CODE_OLD_DB_NULL = 2;
    private static final int FAIL_CODE_OLD_DB_QUERY_ERROR = 3;
    private static final int FAIL_CODE_OLD_DB_QUERY_CURSOR_NULL = 4;
    private static final int FAIL_CODE_INSERTOLDTONEWDB_ERROR = 5;
    private static final int FAIL_CODE_NEW_DB_NULL = 6;


    private static final String OLD_ID = "_id";
    private static final String OLD_CONTENT = "content";
    private static final String OLD_UPDATE_DATE = "cdate";
    private static final String OLD_UPDATE_TIME = "ctime";
    private static final String OLD_ALARM_TIME = "atime";
    private static final String OLD_IS_FOLDER = "isfolder";
    private static final String OLD_PARENT_FOLDER = "parentfile";
    private static final String OLD_NOTE_TITLE = "nodeTitle";


    private static final String OLD_MEDIA_TABLE_NAME = "MediaItems";
    private static final String OLD_NOTE_ID = "noteId";
    private static final String OLD_MEDIA_FILE_NAME = "mediaFileName";

    public static final String PREFIX = "<gionee_media:0:";
    public static final String SUFFIX = "/>>";
    public static final String SPLIT = ":";
    public static final String YES = "yes";
    public static final String NO = "no";

    private volatile int mProgress;
    private volatile int mOldDbVersion;
    private volatile boolean mIsUpgradeFinish;
    private volatile boolean mUpgradeFail;
    private volatile int mFailCode;
    private volatile int mUpgradeTotalCount;
    private volatile int mUpgradeSuccessCount;
    private volatile int mUpgradeFailCount;

    public DataUpgrade() {
        start();
    }

    @Override
    public String toString() {
        return "mOldDbVersion = " + mOldDbVersion +
                ",mIsUpgradeFinish = " + mIsUpgradeFinish + ",mProgress = " + mProgress;
    }

    public boolean isUpgradeFail() {
        return mUpgradeFail;
    }

    public boolean isUpgradeFinish() {
        return mIsUpgradeFinish;
    }

    public int getUpgradeTotalCount() {
        return mUpgradeTotalCount;
    }

    public int getUpgradeSuccessCount() {
        return mUpgradeSuccessCount;
    }

    public int getUpgradeFailCount() {
        return mUpgradeFailCount;
    }

    public int getFailCode() {
        return mFailCode;
    }

    private void updateUpgradeTotalCount(int upgradeTotalCount) {
        mUpgradeTotalCount = upgradeTotalCount;
    }

    private void updateUpgradeFailCount(int upgradeFailCount) {
        mUpgradeFailCount = upgradeFailCount;
    }

    private void updateUpgradeSuccessCount(int upgradeSuccessCount) {
        mUpgradeSuccessCount = upgradeSuccessCount;
    }

    private void upgradeFail(int failCode) {
        mFailCode = failCode;
        mUpgradeFail = true;
    }

    private void upgradeFinish() {
        updateProgress(100);
        mIsUpgradeFinish = true;
        NoteAppImpl.getContext().notifyDbInitComplete();
    }

    private void setOldDBVersion(int version) {
        mOldDbVersion = version;
        Log.d(TAG, "oldVersion = " + version);
    }

    private void start() {
        final NoteAppImpl appImpl = NoteAppImpl.getContext();
        appImpl.getThreadPool().submit(new ThreadPool.Job<Void>() {
            @Override
            public Void run(ThreadPool.JobContext jc) {
                updateProgress(1);
                final ArrayList<OldNoteFolderData> oldNoteFolderDatas = new ArrayList();
                final ArrayList<OldNoteItemData> oldNoteItemDatas = new ArrayList();
                int code = getNoteData(appImpl, oldNoteFolderDatas, oldNoteItemDatas);
                if (code > 0) {
                    upgradeFail(code);
                    return null;
                }

                if (code == CODE_OLD_DB_QUERY_COUNT_ZERO) {
                    Log.d(TAG, "CODE_OLD_DB_QUERY_COUNT_ZERO");
                    upgradeFinish();
                    BuiltInNote.insertBuildInNoteSync();
                    delOldDBFile(appImpl);
                    return null;
                }

                BuiltInNote.setIsFirstLaunch(appImpl, false);
                updateProgress(3);
                if (oldNoteFolderDatas.size() > 0) {
                    converFolderToLabel(appImpl, oldNoteFolderDatas);
                    updateDataLabel(oldNoteFolderDatas, oldNoteItemDatas);
                }

                updateProgress(5);
                SparseArray<ArrayList<String>> mediaArray = new SparseArray();
                code = getMediaData(appImpl, mediaArray);
                if (code > 0) {
                    upgradeFail(code);
                    return null;
                }
                updateProgress(7);

                if (mediaArray.size() != 0) {
                    updateNoteMedia(mediaArray, oldNoteItemDatas);
                }
                updateOverplusNoteMedia(oldNoteItemDatas);
                updateProgress(9);
                updateNoteJsonContent(oldNoteItemDatas);
                updateProgress(11);
                code = insertOldToNewDB(appImpl, oldNoteItemDatas);
                if (code > 0) {
                    upgradeFail(code);
                    return null;
                }
                appImpl.getContentResolver().notifyChange(NoteContract.NoteContent.CONTENT_URI, null);
                ReminderManager.scheduleReminder(appImpl);
                updateProgress(99);
                delOldDBFile(appImpl);
                upgradeFinish();
                return null;
            }
        });
    }

    private int getNoteData(NoteAppImpl appImpl, ArrayList<OldNoteFolderData> oldNoteFolderDatas,
                            ArrayList<OldNoteItemData> oldNoteItemDatas) {
        SQLiteDatabase oldDb = null;

        try {
            oldDb = SQLiteDatabase.openDatabase(appImpl.getDatabasePath(OLD_DB_NAME).getPath(),
                    null, SQLiteDatabase.OPEN_READWRITE);
            if (oldDb == null) {
                Log.w(TAG, "oldDb = null");
                return FAIL_CODE_OLD_DB_NULL;
            }

            setOldDBVersion(oldDb.getVersion());
            final String[] columns = new String[]{
                    OLD_ID,
                    OLD_CONTENT,
                    OLD_UPDATE_DATE,
                    OLD_UPDATE_TIME,
                    OLD_ALARM_TIME,
                    OLD_IS_FOLDER,
                    OLD_PARENT_FOLDER,
                    OLD_NOTE_TITLE
            };
            Cursor noteDataCursor = null;
            try {
                noteDataCursor = oldDb.query(OLD_TABLE_NAME, columns, null, null, null, null, null);

                if (noteDataCursor == null) {
                    return FAIL_CODE_OLD_DB_QUERY_CURSOR_NULL;
                }

                int count = noteDataCursor.getCount();
                if (DEBUG) {
                    Log.d(TAG, "count = " + count);
                }
                if (count == 0) {
                    return CODE_OLD_DB_QUERY_COUNT_ZERO;
                }

                while (noteDataCursor.moveToNext()) {
                    try {
                        int id = noteDataCursor.getInt(0);
                        String content = noteDataCursor.getString(1);
                        String cdate = noteDataCursor.getString(2);
                        String ctime = noteDataCursor.getString(3);
                        long atime = noteDataCursor.getLong(4);
                        String isfolder = noteDataCursor.getString(5);
                        String parentfile = noteDataCursor.getString(6);
                        String nodeTitle = noteDataCursor.getString(7);

                        if (YES.equals(isfolder)) {
                            OldNoteFolderData folderData = new OldNoteFolderData(id, nodeTitle);
                            oldNoteFolderDatas.add(folderData);
                        } else {
                            OldNoteItemData oldNoteItemData = new OldNoteItemData(id, content, cdate, ctime,
                                    atime, parentfile, nodeTitle);
                            oldNoteItemDatas.add(oldNoteItemData);
                        }
                    } catch (Exception e) {
                        Log.w(TAG, "error", e);
                    }
                }

            } catch (Exception e) {
                Log.w(TAG, "error", e);
                return FAIL_CODE_OLD_DB_QUERY_ERROR;
            } finally {
                NoteUtils.closeSilently(noteDataCursor);
            }

        } catch (SQLiteException e) {
            Log.w(TAG, "error", e);
            return FAIL_CODE_OPEN_OLD_DB_ERROR;
        } finally {
            NoteUtils.closeSilently(oldDb);
        }
        return CODE_OK;
    }

    private boolean delOldDBFile(NoteAppImpl appImpl) {
        boolean success = false;
        File oldDBFile = appImpl.getDatabasePath(OLD_DB_NAME);
        if (oldDBFile.exists()) {
            success = SQLiteDatabase.deleteDatabase(oldDBFile);
            if (DEBUG) {
                Log.d(TAG, "delOldDBFile success = " + success);
            }
        }
        return success;
    }

    private void updateProgress(int progress) {
        mProgress = progress;
    }

    public int getProgress() {
        return mProgress;
    }

    private void converFolderToLabel(NoteAppImpl appImpl, ArrayList<OldNoteFolderData> oldNoteFolderDatas) {
        LabelManager labelManager = appImpl.getLabelManager();
        ArrayList<LabelManager.LabelHolder> labels = getLabels(labelManager);
        for (OldNoteFolderData folderData : oldNoteFolderDatas) {
            String labelName = folderData.getName().trim();
            if (labelName.length() > MAX_LABEL_LENGTH) {
                labelName = labelName.substring(0, MAX_LABEL_LENGTH);
            }
            int id = getLabelId(labels, labelName);
            if (id < 0) {
                id = labelManager.addLabel(labelName);
                labels = getLabels(labelManager);
            }
            folderData.setNewId(id);
        }
    }

    private void updateDataLabel(ArrayList<OldNoteFolderData> oldNoteFolderDatas, ArrayList<OldNoteItemData> oldNoteItemDatas) {
        for (OldNoteItemData noteItem : oldNoteItemDatas) {
            int folderId = noteItem.getFolderId();
            OldNoteFolderData folderData = OldNoteFolderData.getNoteFolderData(folderId,
                    oldNoteFolderDatas);
            if (folderData != null) {
                noteItem.setLabel(Integer.toString(folderData.getNewId()));
            }
        }
    }

    private int getMediaData(NoteAppImpl appImpl, SparseArray<ArrayList<String>> mediaArray) {
        SQLiteDatabase oldDb = null;

        try {
            oldDb = SQLiteDatabase.openDatabase(appImpl.getDatabasePath(OLD_DB_NAME).getPath(),
                    null, SQLiteDatabase.OPEN_READWRITE);
            if (oldDb == null) {
                Log.w(TAG, "oldDb = null");
                return FAIL_CODE_OLD_DB_NULL;
            }

            Cursor noteMediaCursor = null;
            try {
                final String[] mediaColumns = new String[]{
                        OLD_NOTE_ID,
                        OLD_MEDIA_FILE_NAME
                };
                noteMediaCursor = oldDb.query(OLD_MEDIA_TABLE_NAME, mediaColumns, null, null, null, null, null);
                if (noteMediaCursor == null) {
                    return CODE_OK;
                }

                while (noteMediaCursor.moveToNext()) {
                    try {
                        int noteId = noteMediaCursor.getInt(0);
                        String fileName = noteMediaCursor.getString(1);
                        ArrayList<String> fileNames = mediaArray.get(noteId);
                        if (fileNames == null) {
                            fileNames = new ArrayList();
                            mediaArray.put(noteId, fileNames);
                        }
                        fileNames.add(fileName);
                    } catch (Exception e) {
                        Log.w(TAG, "error", e);
                    }
                }
            } catch (Exception e) {
                Log.w(TAG, "error", e);
                return CODE_OK;
            } finally {
                NoteUtils.closeSilently(noteMediaCursor);
            }

        } catch (SQLiteException e) {
            Log.w(TAG, "error", e);
            return FAIL_CODE_OPEN_OLD_DB_ERROR;
        } finally {
            NoteUtils.closeSilently(oldDb);
        }
        return CODE_OK;

    }

    private void updateNoteMedia(SparseArray<ArrayList<String>> mediaArray, ArrayList<OldNoteItemData> oldNoteItemDatas) {
        for (int i = 0, size = mediaArray.size(); i < size; i++) {
            int id = mediaArray.keyAt(i);
            OldNoteItemData noteItemData = OldNoteItemData.getNoteItemData(id, oldNoteItemDatas);
            if (noteItemData != null) {
                noteItemData.resolveMedia(mediaArray.get(id));
            }
        }
    }

    //Media files are not present, but must be guaranteed by the media view.
    private void updateOverplusNoteMedia(ArrayList<OldNoteItemData> oldNoteItemDatas) {
        for (OldNoteItemData noteItemData : oldNoteItemDatas) {
            if (noteItemData.getSubs() == null) {
                noteItemData.resolveMedia();
            }
        }
    }

    private void updateNoteJsonContent(ArrayList<OldNoteItemData> oldNoteItemDatas) {
        for (OldNoteItemData noteItemData : oldNoteItemDatas) {
            String jsonContent = recoveryJsonContent(noteItemData);
            noteItemData.setJsonContent(jsonContent);
        }
    }

    private int insertOldToNewDB(NoteAppImpl appImpl, ArrayList<OldNoteItemData> oldNoteItemDatas) {

        SQLiteDatabase oldDb = null;
        SQLiteDatabase newDb = null;
        try {

            oldDb = SQLiteDatabase.openDatabase(appImpl.getDatabasePath(OLD_DB_NAME).getPath(),
                    null, SQLiteDatabase.OPEN_READWRITE);

            if (oldDb == null) {
                Log.w(TAG, "oldDb = null");
                return FAIL_CODE_OLD_DB_NULL;
            }

            newDb = SQLiteDatabase.openOrCreateDatabase(appImpl.getDatabasePath(NoteProvider.DATABASE_NAME).getPath(), null);

            if (newDb == null) {
                Log.w(TAG, "oldDb = null");
                return FAIL_CODE_NEW_DB_NULL;
            }
            final int upgradeTotalCount = oldNoteItemDatas.size();
            updateUpgradeTotalCount(upgradeTotalCount);
            int successCount = 0;
            int failCount = 0;
            int curProgress = getProgress();
            for (OldNoteItemData noteItemData : oldNoteItemDatas) {
                ContentValues values = new ContentValues();
                String title = noteItemData.getNoteTile();
                if (title.length() > MAX_TITLE_LENGTH) {
                    title = title.substring(0, MAX_TITLE_LENGTH);
                }
                values.put(NoteContract.NoteContent.COLUMN_TITLE, title);
                values.put(NoteContract.NoteContent.COLUMN_CONTENT, noteItemData.getJsonContent());
                values.put(NoteContract.NoteContent.COLUMN_DATE_CREATED, noteItemData.getCreateTime());
                values.put(NoteContract.NoteContent.COLUMN_DATE_MODIFIED, noteItemData.getCreateTime());
                values.put(NoteContract.NoteContent.COLUMN_REMINDER, noteItemData.getAlarmTime());
                values.put(NoteContract.NoteContent.COLUMN_LABEL, noteItemData.getLabelId());
                long id = newDb.insert(NoteContract.NoteContent.TABLE_NAME, null, values);
                if (DEBUG) {
                    Log.d(TAG, "insert id = " + id);
                }
                if (id > 0) {
                    successCount++;
                    int delCount = oldDb.delete(OLD_TABLE_NAME, "_id = ?", new String[]{String.valueOf(noteItemData.getId())});
                    if (DEBUG) {
                        Log.d(TAG, "del old db delCount = " + delCount);
                    }
                } else {
                    failCount++;
                }
                updateProgress(curProgress + ((successCount + failCount) / upgradeTotalCount) * 87);
            }
            updateUpgradeFailCount(failCount);
            updateUpgradeSuccessCount(successCount);

        } catch (SQLiteException e) {
            return FAIL_CODE_INSERTOLDTONEWDB_ERROR;
        } finally {
            NoteUtils.closeSilently(oldDb);
            NoteUtils.closeSilently(newDb);
        }
        return CODE_OK;

    }

    public static int getLabelId(ArrayList<LabelManager.LabelHolder> labels, String labelName) {
        for (LabelManager.LabelHolder label : labels) {
            if (label.mContent.equals(labelName.trim())) {
                return label.mId;
            }
        }
        return -1;
    }

    public static ArrayList<LabelManager.LabelHolder> getLabels(LabelManager labelManager) {
        ArrayList<LabelManager.LabelHolder> labels;
        labels = labelManager.getLabelList();
        if (labels.size() == 0) {
            while (true) {
                if (labels.size() != 0) {
                    break;
                }
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                labels = labelManager.getLabelList();
            }
        }
        return labels;
    }

    private String recoveryJsonContent(OldNoteItemData noteItemData) {
        ArrayList<SubData> subDatas = noteItemData.getSubs();
        if (subDatas == null || subDatas.size() == 0) {
            return NoteUtils.createPlainTextJsonContent(noteItemData.getContent());
        }
        return createWithMediaTextJsonContent(subDatas);
    }

    private String createWithMediaTextJsonContent(ArrayList<SubData> subDatas) {
        StringBuilder builder = new StringBuilder();
        JSONArray jsonArray = new JSONArray();
        int start;
        int end;
        try {
            for (int i = 0, size = subDatas.size(); i < size; i++) {
                SubData data = subDatas.get(i);
                if (data.isMedia()) {
                    if (builder.length() > 0) {
                        char prc = builder.charAt(builder.length() - 1);
                        if (prc != Constants.CHAR_NEW_LINE) {
                            builder.append(Constants.STR_NEW_LINE);
                        }
                    }
                    start = builder.length();
                    end = start + Constants.MEDIA_SOUND.length();
                    builder.append(Constants.MEDIA_SOUND);
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put(DataConvert.SPAN_ITEM_START, start);
                    jsonObject.put(DataConvert.SPAN_ITEM_END, end);
                    jsonObject.put(DataConvert.SPAN_ITEM_FLAG, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    jsonObject.put(DataConvert.SPAN_ITEM_TYPE, SoundImageSpan.class.getName());
                    jsonObject.put(SoundImageSpan.ORIGIN_PATH, data.getMediaFilePath());
                    jsonObject.put(SoundImageSpan.SOUND_DURATION, data.getTime());
                    jsonArray.put(jsonObject);
                } else {
                    String content = data.getContent();
                    if (i > 0) {
                        if (subDatas.get(i - 1).isMedia() && (content.charAt(0) != Constants.CHAR_NEW_LINE)) {
                            builder.append(Constants.STR_NEW_LINE);
                        }
                    }
                    builder.append(content);
                }
            }
            JSONStringer jsonStringer = new JSONStringer();
            jsonStringer.object();

            jsonStringer.key(DataConvert.JSON_CONTENT_KEY).value(builder.toString());
            if (!(jsonArray == null || jsonArray.length() == 0)) {
                jsonStringer.key(DataConvert.JSON_SPANS_KEY).value(jsonArray);
            }
            jsonStringer.endObject();
            return jsonStringer.toString();
        } catch (JSONException e) {
            Log.w(TAG, "error", e);
            return null;
        }
    }

    public static boolean isExistOldDB(Context context) {
        File oldDBFile = context.getDatabasePath(OLD_DB_NAME);
        return oldDBFile.exists();
    }
}
