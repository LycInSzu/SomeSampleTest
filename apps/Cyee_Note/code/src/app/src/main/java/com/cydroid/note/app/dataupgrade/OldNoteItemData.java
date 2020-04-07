package com.cydroid.note.app.dataupgrade;


import com.cydroid.note.common.NoteUtils;

import java.util.ArrayList;
import java.util.Calendar;

class OldNoteItemData {

    private int mId;
    private String mContent;
    private long mAlarmTime;
    private long mCreateTime;
    private String mParentFile;
    private String mNoteTile;
    private String mLabelId;
    private ArrayList<SubData> mSubs;
    private String mJsonContent;

    public OldNoteItemData(int id, String content, String cDate, String cTime, long aTime,
                           String parentFile, String noteTile) {
        mId = id;
        mContent = content;
        mAlarmTime = aTime;
        mParentFile = parentFile;
        mNoteTile = noteTile;

        String[] ymd = cDate.split("-");
        String[] hms = cTime.split(":");
        Calendar calendar = Calendar.getInstance();
        calendar.set(Integer.parseInt(ymd[0]), Integer.parseInt(ymd[1]) - 1,
                Integer.parseInt(ymd[2]), Integer.parseInt(hms[0]),
                Integer.parseInt(hms[1]), Integer.parseInt(hms[2]));
        mCreateTime = calendar.getTimeInMillis();

    }

    public int getId() {
        return mId;
    }

    public long getAlarmTime() {
        return mAlarmTime;
    }

    public long getCreateTime() {
        return mCreateTime;
    }

    public String getContent() {
        return mContent;
    }

    public void setLabel(String labelId) {
        mLabelId = labelId;
    }

    public String getLabelId() {
        return mLabelId;
    }

    public int getFolderId() {
        if (mParentFile == null || DataUpgrade.NO.equals(mParentFile)) {
            return -1;
        }
        return Integer.parseInt(mParentFile);
    }

    public void setJsonContent(String jsonContent) {
        mJsonContent = jsonContent;
    }

    public String getJsonContent() {
        return mJsonContent;
    }

    public String getNoteTile() {
        return mNoteTile != null ? mNoteTile : "";
    }

    public void resolveMedia(ArrayList<String> mediaFileNames) {
        ArrayList<SubData> subDatas = getSubDatas(mContent, DataUpgrade.PREFIX, DataUpgrade.SUFFIX);
        if (subDatas != null) {
            for (SubData subData : subDatas) {
                if (subData.isMedia()) {
                    subData.resolveMedia(mediaFileNames);
                }
            }
            mSubs = subDatas;
        }
    }

    public void resolveMedia() {
        ArrayList<SubData> subDatas = getSubDatas(mContent, DataUpgrade.PREFIX, DataUpgrade.SUFFIX);
        if (subDatas != null) {
            for (SubData subData : subDatas) {
                if (subData.isMedia()) {
                    subData.resolveMedia();
                }
            }
            mSubs = subDatas;
        }
    }

    public ArrayList<SubData> getSubs() {
        return mSubs;
    }

    @Override
    public String toString() {
        return "item mId = " + mId + ",mContent = " + mContent + ",mCreateTime = " + mCreateTime
                + ",mAlarmTime = " + mAlarmTime + ",mParentFile = " + mParentFile
                + ",mNoteTile = " + mNoteTile + ",mLabelId = " + mLabelId;
    }

    private ArrayList<SubData> getSubDatas(String content, String prefix, String suffix) {

        ArrayList<Integer> prefixs = NoteUtils.indexofs(content, prefix);
        if (prefixs == null) {
            return null;
        }
        ArrayList<Integer> suffixs = NoteUtils.indexofs(content, suffix);
        if (suffixs == null) {
            return null;
        }
        if (prefixs.size() != suffixs.size()) {
            return null;
        }
        final int prefixLength = prefix.length();
        final int suffixLength = suffix.length();

        ArrayList<SubData> subDatas = new ArrayList();
        int ps = 0;
        int pe;
        for (int i = 0, size = prefixs.size(); i < size; i++) {
            int start = prefixs.get(i);
            pe = start;
            if (pe > 0) {
                subDatas.add(new SubData((content.substring(ps, pe)), false));
            }
            ps = start;
            int end = suffixs.get(i) + suffixLength;
            pe = end;
            subDatas.add(new SubData((content.substring(ps + prefixLength, pe - suffixLength)), true));
            ps = end;
        }
        int length = content.length();
        if (ps < length) {
            subDatas.add(new SubData((content.substring(ps, length)), false));
        }
        return subDatas;
    }

    public static OldNoteItemData getNoteItemData(int noteId, ArrayList<OldNoteItemData> oldNoteItemDatas) {
        for (OldNoteItemData noteItemData : oldNoteItemDatas) {
            if (noteItemData.getId() == noteId) {
                return noteItemData;
            }
        }
        return null;
    }
}