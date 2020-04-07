package com.cydroid.note.app.dataupgrade;

import java.util.ArrayList;

class OldNoteFolderData {
    private int mOldId;
    private String mName;
    private int mNewId;

    public OldNoteFolderData(int id, String name) {
        mOldId = id;
        mName = name;
    }

    public int getOldId() {
        return mOldId;
    }

    public int getNewId() {
        return mNewId;
    }

    public void setNewId(int id) {
        mNewId = id;
    }

    public String getName() {
        return mName;
    }

    @Override
    public String toString() {
        return "folder oldId = " + mOldId + ",name = " + mName + ",newId = " + mNewId;
    }

    public static OldNoteFolderData getNoteFolderData(int folderId, ArrayList<OldNoteFolderData> oldNoteFolderDatas) {
        for (OldNoteFolderData noteFolderData : oldNoteFolderDatas) {
            if (noteFolderData.getOldId() == folderId) {
                return noteFolderData;
            }
        }
        return null;
    }
}