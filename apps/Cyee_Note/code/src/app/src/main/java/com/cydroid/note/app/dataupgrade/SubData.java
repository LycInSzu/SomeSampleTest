package com.cydroid.note.app.dataupgrade;

import java.util.ArrayList;

class SubData {
    private String mContent;
    private int mTime;
    private boolean mIsMedia;
    private String mMediaFilePath;

    public SubData(String content, boolean isMedia) {
        mContent = content;
        mIsMedia = isMedia;
    }

    public void resolveMedia(ArrayList<String> mediaFileNames) {
        if (mIsMedia) {
            String[] cs = mContent.split(DataUpgrade.SPLIT);
            String mediaName = cs[0];
            calculateTime(cs);
            String realFilePath = converMediaFilePath(mediaName, mediaFileNames);
            if (realFilePath == null) {
                realFilePath = mediaName;
            }
            mMediaFilePath = realFilePath;
        }
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

    public boolean isMedia() {
        return mIsMedia;
    }

    public String getMediaFilePath() {
        return mMediaFilePath;
    }

    public int getTime() {
        return mTime;
    }

    public String getContent() {
        return mContent;
    }

    @Override
    public String toString() {
        return "mContent = " + mContent + ",mIsMedia = " + mIsMedia + ",mMediaFilePath = " + mMediaFilePath;
    }

    private String converMediaFilePath(String name, ArrayList<String> mediaFileNames) {
        String filePath = null;
        for (String path : mediaFileNames) {
            if (path.contains(name)) {
                filePath = path;
                break;
            }
        }
        if (filePath != null) {
            return filePath.substring(1, filePath.length());
        }
        return null;
    }
}
