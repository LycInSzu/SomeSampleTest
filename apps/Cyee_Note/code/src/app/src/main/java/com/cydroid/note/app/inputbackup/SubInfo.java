package com.cydroid.note.app.inputbackup;

import com.cydroid.note.app.dataupgrade.DataUpgrade;

public class SubInfo {
    private String mContent;
    private int mTime;
    private boolean mIsMedia;
    private String mMediaFilePath;

    public SubInfo(String content, boolean isMedia) {
        mContent = content;
        mIsMedia = isMedia;
    }

    public String getContent() {
        return mContent;
    }

    public int getTime() {
        return mTime;
    }

    public boolean isMedia() {
        return mIsMedia;
    }

    public String getMediaFilePath() {
        return mMediaFilePath;
    }

    public void resolveMedia() {
        if (mIsMedia) {
            String[] cs = mContent.split(DataUpgrade.SPLIT);
            String mediaName = cs[0];
            calculateTime(cs);
            mMediaFilePath = mediaName;
        }
    }

    private void calculateTime(String[] cs) {
        String m = cs[1];
        String s = cs[2];
        mTime = Integer.valueOf(m) * 60 + Integer.valueOf(s);
    }
}
