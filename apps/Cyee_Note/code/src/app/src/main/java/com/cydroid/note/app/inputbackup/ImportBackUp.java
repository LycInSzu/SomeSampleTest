package com.cydroid.note.app.inputbackup;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Environment;
import android.text.Spannable;
import com.cydroid.note.common.Log;

import com.cydroid.note.app.DataConvert;
import com.cydroid.note.app.LabelManager;
import com.cydroid.note.app.NoteAppImpl;
import com.cydroid.note.app.dataupgrade.DataUpgrade;
import com.cydroid.note.app.span.SoundImageSpan;
import com.cydroid.note.common.Constants;
import com.cydroid.note.common.FileUtils;
import com.cydroid.note.common.NoteUtils;
import com.cydroid.note.common.StorageUtils;
import com.cydroid.note.provider.NoteContract;
import com.cydroid.note.provider.NoteProvider;
import com.cydroid.note.provider.NoteShareDataManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ImportBackUp {

    private static final String TAG = "ImportBackUp";
    private static final long MIN_LIMIT_SIZE = 1024 * 1024 * 20;
    private static final File MAIN_ROOT_FILE = Environment.getExternalStorageDirectory();
    private static final File OLD_BACKUP_FILE_0 = new File(MAIN_ROOT_FILE, "/cyee/CyeeNote/Memo");
    private static final File OLD_BACKUP_FILE_1 = new File(MAIN_ROOT_FILE, "/备份/便签");
    private static final File OLD_BACKUP_FILE_2 = new File(MAIN_ROOT_FILE, "/backup/Memo");

    private static final File TEMP_SAVE_FILE = new File(Constants.NOTE_MEDIA_PATH, "temp_import_save");
    private static final String MEDIA_NAME_HEAD = "import_";

    static final int FAIL_CODE_NO_SPACE = 1;
    static final int FAIL_CODE_SAVE_TO_TEMP_ERROR = 2;
    static final int FAIL_CODE_WRITE_CONFIG_ERROR = 3;
    static final int FAIL_CODE_INSERT_DB_ERROR = 4;

    static final String SUFFIX_MP3 = ".mp3";
    static final String SUFFIX_TXT = ".txt";
    static final char CONTENT_SPLIT = '\u00AB';
    static final char ENTER_REPLACE = '\u00BB';
    static final String STR_ENTER = "\n";

    private ArrayList<File> mBackupFiles;
    private long mMinSize;
    private int mFailCode;
    private boolean mImportBackupDataFinish;
    private boolean mImportFail;
    private int mProgress;
    private boolean mRuning;

    static File sTempSaveFile;
    static File sTempSaveFileMedia;

    public void start() {
        synchronized (this) {
            if (mRuning) {
                Log.d(TAG, "runing-----------");
                return;
            }
            mRuning = true;
        }
        if (mImportBackupDataFinish) {
            return;
        }
        resetStates();
        setProgress(1);
        boolean isImportToTempFinish = getImportToTempFinishValue();
        Log.d(TAG, "isImportToTempFinish = " + isImportToTempFinish);
        String tempSaveFilePath = null;
        if (isImportToTempFinish) {
            tempSaveFilePath = getTempFilePath();
        }

        File tempSaveFile;
        if (tempSaveFilePath == null) {
            clearTempFile();
            tempSaveFile = StorageUtils.getAvailableFileDirectory(NoteAppImpl.getContext(), mMinSize, TEMP_SAVE_FILE);
        } else {
            tempSaveFile = new File(tempSaveFilePath);
        }

        if (tempSaveFile == null) {
            importFail(FAIL_CODE_NO_SPACE);
            return;
        }

        sTempSaveFile = tempSaveFile;
        sTempSaveFileMedia = new File(tempSaveFile, "/media");

        setProgress(2);
        if (!isImportToTempFinish) {
            if (!saveAllImportNoteInfoToTemp(mBackupFiles)) {
                importFail(FAIL_CODE_SAVE_TO_TEMP_ERROR);
                return;
            }
            if (!writeFinishImportToTemp()) {
                Log.d(TAG, "write value fail KEY_BACKUP_DATA_TO_TEMP_FINISH");
                importFail(FAIL_CODE_WRITE_CONFIG_ERROR);
                return;
            }
            writeTempFilePath(sTempSaveFile.getPath());
        }
        setProgress(3);
        importBackupData();
    }

    private void resetStates() {
        mImportFail = false;
        mFailCode = 0;
        mProgress = 0;
    }

    private void clearTempFile() {
        FileUtils.deleteContents(TEMP_SAVE_FILE);
        List<String> rootPaths = StorageUtils.getLocalRootPath(NoteAppImpl.getContext());
        File otherSDBackUpFile = StorageUtils.createOtherSdCardFile(rootPaths, TEMP_SAVE_FILE.getAbsolutePath());
        if (otherSDBackUpFile != null) {
            FileUtils.deleteContents(otherSDBackUpFile);
        }
    }

    private boolean getImportToTempFinishValue() {
        return NoteShareDataManager.getImportToTempFinishValue(NoteAppImpl.getContext());
    }

    private String getTempFilePath() {
        return NoteShareDataManager.getTempFilePath(NoteAppImpl.getContext());
    }

    private boolean writeFinishImportToTemp() {
        return NoteShareDataManager.writeFinishImportToTemp(NoteAppImpl.getContext());
    }

    private void writeTempFilePath(String filePath) {
        NoteShareDataManager.writeTempFilePath(NoteAppImpl.getContext(), filePath);
    }

    public static boolean writeFinishImport() {
        return NoteShareDataManager.writeFinishImport(NoteAppImpl.getContext());
    }

    private void setProgress(int progress) {
        mProgress = progress;
    }

    public int getProgress() {
        return mProgress;
    }

    private void importBackupFinish() {
        mImportBackupDataFinish = true;
        setProgress(100);
        runFinish();
    }

    private void runFinish() {
        synchronized (this) {
            mRuning = false;
        }
    }

    public void resetEnv() {
        synchronized (this) {
            if (mRuning) {
                Log.d(TAG, "runing-----------");
                return;
            }
        }
        initBackupFilesAndMinSize();
        initImportBackupDataFinishMember();

    }

    private void initImportBackupDataFinishMember() {
        mImportBackupDataFinish = NoteShareDataManager.
                getImportBackupDataFinish(NoteAppImpl.getContext());
    }

    private void importBackupData() {
        ArrayList<OldNoteInfo> oldNoteInfos = getOldNoteInfos();
        if (oldNoteInfos == null || oldNoteInfos.size() == 0) {
            importBackupFinish();
            return;
        }
        Log.d(TAG, "size = " + oldNoteInfos.size());
        resolveNoteInfoMedia(oldNoteInfos);
        updateNoteInfoLabelId(oldNoteInfos);
        try {
            insertOldDataToNewDB(oldNoteInfos);
        } catch (Exception e) {
            Log.w(TAG, "error", e);
            importFail(FAIL_CODE_INSERT_DB_ERROR);
            return;
        }
        if (!writeFinishImport()) {
            Log.d(TAG, "write value fail KEY_IMPORT_BACKUP_DATA_FINISH");
            importFail(FAIL_CODE_WRITE_CONFIG_ERROR);
            return;
        }
        clearTempFile();
        NoteAppImpl.getContext().getContentResolver().notifyChange(NoteContract.NoteContent.CONTENT_URI, null);
        importBackupFinish();
        Log.d(TAG, "importBackupData finish");
    }

    private void insertOldDataToNewDB(ArrayList<OldNoteInfo> oldNoteInfos) throws Exception {
        int progress = getProgress();
        for (int i = 0, size = oldNoteInfos.size(); i < size; i++) {
            OldNoteInfo oldNoteInfo = oldNoteInfos.get(i);
            ArrayList<SubInfo> subInfos = oldNoteInfo.getSubInfos();
            if (subInfos == null || subInfos.size() == 0) {
                String jsonContent = NoteUtils.createPlainTextJsonContent(oldNoteInfo.getContent());
                if (!insertOldDataToNewDB(oldNoteInfo.getId(), oldNoteInfo.getTitle(),
                        jsonContent, oldNoteInfo.getLabelId())) {
                    throw new ImportError();
                }
            } else {
                insertOldDataToNewDB(subInfos, oldNoteInfo.getId(), oldNoteInfo.getTitle(),
                        oldNoteInfo.getLabelId());
            }
            setProgress(progress + ((i * 86) / size));
        }
    }

    private void updateNoteInfoLabelId(ArrayList<OldNoteInfo> oldNoteInfos) {
        LabelManager labelManager = NoteAppImpl.getContext().getLabelManager();
        ArrayList<LabelManager.LabelHolder> labels = DataUpgrade.getLabels(labelManager);
        for (OldNoteInfo oldNoteInfo : oldNoteInfos) {
            String labelName = oldNoteInfo.getLabel();
            if (labelName != null && labelName.trim().length() > 0) {
                if (labelName.length() > DataUpgrade.MAX_LABEL_LENGTH) {
                    labelName = labelName.substring(0, DataUpgrade.MAX_LABEL_LENGTH);
                }
                int id = DataUpgrade.getLabelId(labels, labelName);
                if (id < 0) {
                    id = labelManager.addLabel(labelName);
                    labels = DataUpgrade.getLabels(labelManager);
                }
                oldNoteInfo.setLabelId(Integer.toString(id));
            }
        }
    }

    private void resolveNoteInfoMedia(ArrayList<OldNoteInfo> oldNoteInfos) {
        for (OldNoteInfo oldNoteInfo : oldNoteInfos) {
            oldNoteInfo.resolveMedia();
        }
    }

    private void insertOldDataToNewDB(ArrayList<SubInfo> subDatas, long oldId,
                                      String title, String labelId) throws Exception {
        StringBuilder builder = new StringBuilder();
        JSONArray jsonArray = new JSONArray();
        ArrayList<File> delFiles = new ArrayList();
        ArrayList<File> dstFiles = new ArrayList();
        for (int i = 0, size = subDatas.size(); i < size; i++) {
            SubInfo data = subDatas.get(i);
            if (data.isMedia()) {
                if (builder.length() > 0) {
                    char prc = builder.charAt(builder.length() - 1);
                    if (prc != Constants.CHAR_NEW_LINE) {
                        builder.append(Constants.STR_NEW_LINE);
                    }
                }
                int start = builder.length();
                int end = start + Constants.MEDIA_SOUND.length();
                builder.append(Constants.MEDIA_SOUND);
                String mediaFilePath = data.getMediaFilePath();
                mediaFilePath = getDstMediaFilePath(delFiles, dstFiles, mediaFilePath);
                putToJsonArray(jsonArray, data, start, end, mediaFilePath);
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
        String jsonContent = jsonStringer.toString();
        if (insertOldDataToNewDB(oldId, title, jsonContent, labelId)) {
            deleteFiles(delFiles);
        } else {
            deleteFiles(dstFiles);
            throw new ImportError();
        }
    }

    private boolean deleteFiles(ArrayList<File> delFiles) {
        boolean deleteResult = true;
        List<File> deleteFiles = delFiles;
        for (File file : deleteFiles) {
            if (!file.delete()) {
                deleteResult = false;
            }
        }
        return deleteResult;
    }

    private void putToJsonArray(JSONArray jsonArray, SubInfo data, int start, int end, String mediaFilePath)
            throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(DataConvert.SPAN_ITEM_START, start);
        jsonObject.put(DataConvert.SPAN_ITEM_END, end);
        jsonObject.put(DataConvert.SPAN_ITEM_FLAG, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        jsonObject.put(DataConvert.SPAN_ITEM_TYPE, SoundImageSpan.class.getName());
        jsonObject.put(SoundImageSpan.ORIGIN_PATH, mediaFilePath);
        jsonObject.put(SoundImageSpan.SOUND_DURATION, data.getTime());
        jsonArray.put(jsonObject);
    }

    private String getDstMediaFilePath(ArrayList<File> delFiles, ArrayList<File> dstFiles, String mediaFilePath) {
        String dstMediaFilePath = mediaFilePath;
        File srcFile = getScrFile(dstMediaFilePath);
        if (srcFile != null) {
            File dstFile = getNewFile(srcFile.length());
            File dstParentFile = dstFile.getParentFile();
            if (!dstParentFile.exists() && !dstParentFile.mkdirs()) {
                Log.i(TAG, "getDstMediaFilePath dstParentFile.mkdirs failure!!!!!");
            }
            if (FileUtils.copyFile(srcFile.getPath(), dstFile.getPath())) {
                dstMediaFilePath = dstFile.getPath();
                delFiles.add(srcFile);
                dstFiles.add(dstFile);
            }
        }
        return dstMediaFilePath;
    }

    private File getScrFile(String name) {
        File rootFile = sTempSaveFileMedia;
        if (!rootFile.exists()) {
            return null;
        }
        File[] listFiles = rootFile.listFiles();
        if (listFiles == null || listFiles.length == 0) {
            return null;
        }
        for (File file : listFiles) {
            if (file.getPath().endsWith(name)) {
                return file;
            }
        }
        return null;
    }

    private File getNewFile(long size) {
        File dirFile = StorageUtils.getAvailableFileDirectory(NoteAppImpl.getContext(),
                size, Constants.NOTE_MEDIA_SOUND_PATH);
        File file = new File(dirFile, MEDIA_NAME_HEAD + System.currentTimeMillis());
        if (!file.exists()) {
            return file;
        }
        return getNewFile(size);
    }

    private boolean insertOldDataToNewDB(long oldId, String title, String content, String labelId) {
        SQLiteDatabase newDb = null;
        try {
            newDb = SQLiteDatabase.openOrCreateDatabase(NoteAppImpl.getContext().getDatabasePath(
                    NoteProvider.DATABASE_NAME).getPath(), null);

            ContentValues values = new ContentValues();
            String newTitle = title;
            if (newTitle.length() > DataUpgrade.MAX_TITLE_LENGTH) {
                newTitle = newTitle.substring(0, DataUpgrade.MAX_TITLE_LENGTH);
            }
            long time = System.currentTimeMillis();
            values.put(NoteContract.NoteContent.COLUMN_TITLE, newTitle);
            values.put(NoteContract.NoteContent.COLUMN_CONTENT, content);
            values.put(NoteContract.NoteContent.COLUMN_DATE_CREATED, time);
            values.put(NoteContract.NoteContent.COLUMN_DATE_MODIFIED, time);
            values.put(NoteContract.NoteContent.COLUMN_LABEL, labelId);

            long id = newDb.insert(NoteContract.NoteContent.TABLE_NAME, null, values);
            if (id > 0) {
                delOldDataFromTempDB(oldId);
            }
            return id > 0;

        } catch (SQLiteException e) {
            Log.w(TAG, "error", e);
        } finally {
            NoteUtils.closeSilently(newDb);
        }
        return false;
    }

    private int delOldDataFromTempDB(long id) {
        ImportDBHelp dbHelp = new ImportDBHelp();
        try {
            return dbHelp.delete("_id = ?", new String[]{String.valueOf(id)});
        } finally {
            dbHelp.close();
        }
    }

    private ArrayList<OldNoteInfo> getOldNoteInfos() {
        ImportDBHelp dbHelp = new ImportDBHelp();
        try {
            Cursor cursor = null;
            try {
                final String[] columns = new String[]{ImportDBHelp.COLUMN_ID, ImportDBHelp.COLUMN_TITLE,
                        ImportDBHelp.COLUMN_CONTENT, ImportDBHelp.COLUMN_LABEL};

                cursor = dbHelp.query(columns, null, null, null, null, null);
                if (cursor == null || cursor.getCount() == 0) {
                    return null;
                }
                ArrayList<OldNoteInfo> noteInfos = new ArrayList();
                while (cursor.moveToNext()) {
                    long id = cursor.getLong(0);
                    String title = cursor.getString(1);
                    String content = cursor.getString(2);
                    String label = cursor.getString(3);
                    OldNoteInfo noteInfo = new OldNoteInfo(id, title, content, label);
                    noteInfos.add(noteInfo);
                }
                return noteInfos;
            } finally {
                NoteUtils.closeSilently(cursor);
            }
        } finally {
            dbHelp.close();
        }
    }

    private boolean saveAllImportNoteInfoToTemp(ArrayList<File> backupFiles) {
        try {
            for (File file : backupFiles) {
                File[] listFiles = file.listFiles();
                if (listFiles != null && listFiles.length > 0) {
                    for (File f : listFiles) {
                        saveOneExportInputNoteInfoToTemp(f);
                    }
                }
            }
            return true;
        } catch (Exception e) {
            Log.w(TAG, "error", e);
            return false;
        }

    }

    private void saveOneExportInputNoteInfoToTemp(File file) throws Exception {
        if (file == null) {
            return;
        }

        if (file.isFile()) {
            return;
        }
        File[] files = file.listFiles();
        if (files == null || files.length == 0) {
            return;
        }
        for (File f : files) {
            saveInputNoteInfoToTemp(f);
        }
    }

    private void saveInputNoteInfoToTemp(File file) throws Exception {
        if (file == null) {
            return;
        }
        if (file.isFile()) {
            return;
        }
        File[] files = file.listFiles();
        if (files == null || files.length == 0) {
            return;
        }
        InputNoteInfo item = new InputNoteInfo();
        for (File f : files) {
            String fileName = f.getName();
            if (fileName.endsWith(SUFFIX_TXT)) {
                item.setText(txtToString(f));
            } else if (fileName.endsWith(SUFFIX_MP3)) {
                item.putMediaFilePath(f);
            }
        }
        item.writToTemp();
    }

    private String txtToString(File file) {
        BufferedReader br = null;
        try {
//            br = new BufferedReader(new FileReader(file));
            br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "utf-8"));
            return br.readLine();
        } catch (Exception e) {
            Log.w(TAG, "error", e);
            return null;
        } finally {
            NoteUtils.closeSilently(br);
        }
    }

    private void importFail(int failCode) {
        mFailCode = failCode;
        mImportFail = true;
        runFinish();
        Log.d(TAG, "importFail failCode = " + failCode);
    }

    public boolean isImportFail() {
        return mImportFail;
    }

    public int getFailCode() {
        return mFailCode;
    }

    private void addBackup(File file) {
        if (mBackupFiles == null) {
            mBackupFiles = new ArrayList<>();
        }
        mBackupFiles.add(file);
    }

    private void initBackupFilesAndMinSize() {
        List<String> rootPaths = StorageUtils.getLocalRootPath(NoteAppImpl.getContext());
        long minSize = MIN_LIMIT_SIZE;
        minSize += initBackupFilesAndMinSize(rootPaths, OLD_BACKUP_FILE_0);
        minSize += initBackupFilesAndMinSize(rootPaths, OLD_BACKUP_FILE_1);
        minSize += initBackupFilesAndMinSize(rootPaths, OLD_BACKUP_FILE_2);
        mMinSize = minSize;
    }

    private long initBackupFilesAndMinSize(List<String> rootPaths, File mainSDBackUpFile) {
        long totalSize = 0;
        long mainSDBackUpFileTotalSize = FileUtils.getFileTotalSize(mainSDBackUpFile);
        if (mainSDBackUpFileTotalSize > 0) {
            totalSize += mainSDBackUpFileTotalSize;
            addBackup(mainSDBackUpFile);
        }
        File otherSDBackUpFile_0 = StorageUtils.createOtherSdCardFile(rootPaths, mainSDBackUpFile.getAbsolutePath());
        long otherSDBackUpFileTotalSize = FileUtils.getFileTotalSize(otherSDBackUpFile_0);
        if (otherSDBackUpFileTotalSize > 0) {
            totalSize += otherSDBackUpFileTotalSize;
            addBackup(otherSDBackUpFile_0);
        }
        return totalSize;
    }

    public boolean isNeedInputBackup() {
        return (mBackupFiles != null) && (mImportBackupDataFinish == false);
    }

    public long getMinSize() {
        return mMinSize;
    }

}
