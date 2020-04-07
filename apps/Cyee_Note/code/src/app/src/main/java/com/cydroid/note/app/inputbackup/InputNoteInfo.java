package com.cydroid.note.app.inputbackup;

import android.content.ContentValues;
import com.cydroid.note.common.Log;

import com.cydroid.note.app.dataupgrade.DataUpgrade;
import com.cydroid.note.common.FileUtils;

import java.io.File;
import java.util.ArrayList;

class InputNoteInfo {

    private static final boolean DEBUG = false;
    private static final String TAG = "InputNoteInfo";
    private static final int LENGTH_TWO = 2;
    private static final int LENGTH_THREE = 3;

    private String mText;
    private ArrayList<File> mMediaFiles;

    public void setText(String text) {
        mText = text;
    }

    public void putMediaFilePath(File mediaFile) {
        if (mMediaFiles == null) {
            mMediaFiles = new ArrayList();
        }
        mMediaFiles.add(mediaFile);
    }

    public void writToTemp() throws Exception {
        if (mText != null && mText.trim().length() > 0) {
            String[] ss = mText.split(String.valueOf(ImportBackUp.CONTENT_SPLIT), -1);
            if (ss != null) {
                if (DEBUG) {
                    Log.d(TAG, "ss length = " + ss.length);
                }

                if (ss.length == LENGTH_TWO) {
                    ContentValues values = new ContentValues();
                    values.put(ImportDBHelp.COLUMN_TITLE, ss[0]);
                    String content = ss[1].replaceAll(String.valueOf(ImportBackUp.ENTER_REPLACE), ImportBackUp.STR_ENTER);
                    content = modifyMediaInfoAndCopyMediaToTemp(content);
                    values.put(ImportDBHelp.COLUMN_CONTENT, content);
                    writeToDB(values);
                    return;
                }

                if (ss.length == LENGTH_THREE) {
                    ContentValues values = new ContentValues();
                    values.put(ImportDBHelp.COLUMN_LABEL, ss[0]);
                    values.put(ImportDBHelp.COLUMN_TITLE, ss[1]);
                    String content = ss[2].replaceAll(String.valueOf(ImportBackUp.ENTER_REPLACE), ImportBackUp.STR_ENTER);
                    content = modifyMediaInfoAndCopyMediaToTemp(content);
                    values.put(ImportDBHelp.COLUMN_CONTENT, content);
                    writeToDB(values);
                    return;
                }
            }
        }

    }

    private boolean writeToDB(ContentValues values) throws Exception {
        ImportDBHelp dbHelp = new ImportDBHelp();
        try {
            boolean insertSuccess = dbHelp.insert(values) > 0;
            if (DEBUG) {
                Log.d(TAG, "insertSuccess = " + insertSuccess);
            }
            if (!insertSuccess) {
                throw new ImportError();
            }
            return insertSuccess;
        } finally {
            dbHelp.close();
        }
    }

    private String modifyMediaInfoAndCopyMediaToTemp(String content) throws Exception {
        ArrayList<File> mediaFiles = mMediaFiles;
        if (mediaFiles == null || mediaFiles.size() == 0) {
            return content;
        }

        String newContent = content;
        for (File file : mediaFiles) {
            File dstFile = getNewFile();
            File parentFile = dstFile.getParentFile();
            if (!parentFile.exists()) {
                if (!parentFile.mkdirs()) {
                    throw new ImportError();
                }
            }
            boolean success = FileUtils.copyFile(file.getPath(), dstFile.getPath());
            if (DEBUG) {
                Log.d(TAG, "copy media success = " + success);
            }
            if (success) {
                String src = DataUpgrade.PREFIX + file.getName();
                String dst = DataUpgrade.PREFIX + dstFile.getName();
                newContent = newContent.replaceAll(src, dst);
            } else {
                throw new ImportError();
            }
        }
        return newContent;
    }

    private File getNewFile() {
        File file = new File(ImportBackUp.sTempSaveFileMedia, System.currentTimeMillis()
                + ImportBackUp.SUFFIX_MP3);
        if (!file.exists()) {
            return file;
        }
        return getNewFile();
    }
}
