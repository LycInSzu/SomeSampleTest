package com.cydroid.note.encrypt;

import com.cydroid.note.common.Log;

import com.cydroid.note.common.NoteUtils;

/**
 * Created by spc on 16-5-24.
 */
public class ZYTProgressListener implements android.app.filecrypt.zyt.callback.ProgressListener {

    private static final String TAG = "ZYTProgressListener";
    private String mFilePath;
    private boolean mDelete;
    private ZYTProgressCompleteListener mCompleteListener;

    public ZYTProgressListener(String filePath, boolean delete,
                               ZYTProgressCompleteListener completeListener) {
        mFilePath = filePath;
        mDelete = delete;
        mCompleteListener = completeListener;
    }

    @Override
    public void onStart(String s, String s1, boolean b, Object o) {
    }

    @Override
    public void onProgress(String s, String s1, boolean b, long l, long l1, Object o) {
    }

    @Override
    public void onCompleted(String s, String s1, boolean b, Object o) {
        if (null != mCompleteListener) {
            mCompleteListener.onCompleted();
            return;
        }
        if (mDelete) {
            NoteUtils.deleteFile(mFilePath);
        }
        Log.i(TAG, "onCompleted mFilePath =" + mFilePath);
    }

    @Override
    public void onError(String s, String s1, boolean b, int i, String s2, Object o) {
        Log.d(TAG, "CryptWork encrypt error S = " + s + ",,,s1,,," + s1 + ",,,,s2,,," + s2);
    }

    public interface ZYTProgressCompleteListener {
        public void onCompleted();
    }
}
