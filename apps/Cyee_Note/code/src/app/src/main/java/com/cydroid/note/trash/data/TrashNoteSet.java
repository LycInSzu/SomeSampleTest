package com.cydroid.note.trash.data;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;

import com.cydroid.note.common.Log;
import com.cydroid.note.app.NoteAppImpl;
import com.cydroid.note.common.NoteUtils;
import com.cydroid.note.data.ChangeNotifier;
import com.cydroid.note.data.DataManager;
import com.cydroid.note.data.NoteItem;
import com.cydroid.note.data.NoteSet;
import com.cydroid.note.data.Path;
import com.cydroid.note.provider.NoteContract;

import java.util.ArrayList;

/**
 * Created by xiaozhilong on 7/1/16.
 */
public class TrashNoteSet extends NoteSet {
    private static final String TAG = "TrashNoteSet";
    public static final String[] COUNT_PROJECTION = {NoteContract.TrashContent._ID}; //NOSONAR
    public static final String[] NOTE_PROJECTION = { //NOSONAR
            NoteContract.TrashContent._ID,
            NoteContract.TrashContent.COLUMN_TITLE,
            NoteContract.TrashContent.COLUMN_CONTENT,
            NoteContract.TrashContent.COLUMN_LABEL,
            NoteContract.TrashContent.COLUMN_DATE_CREATED,
            NoteContract.TrashContent.COLUMN_DATE_MODIFIED,
            NoteContract.TrashContent.COLUMN_DATE_DELETED,
            NoteContract.TrashContent.COLUMN_REMINDER,
            NoteContract.TrashContent.COLUMN_ENCRYPT_HINT_STATE,
            NoteContract.TrashContent.CLOUMN_ENCRYPT_REMIND_READ_STATE};
    private static final int INVALID_COUNT = -1;
    private int mCachedCount = INVALID_COUNT;
    private final String mOrderClause;
    private final Uri mBaseUri;
    private final NoteAppImpl mApplication;
    private final ContentResolver mResolver;
    private final ChangeNotifier mNotifier;
    private final Path mItemPath;

    public TrashNoteSet(Path path, NoteAppImpl application) {
        super(path, nextVersionNumber());
        mApplication = application;
        mResolver = application.getContentResolver();
        mOrderClause = NoteContract.TrashContent.COLUMN_DATE_DELETED + " DESC";
        mBaseUri = NoteContract.TrashContent.CONTENT_URI;
        mItemPath = TrashNoteItem.ITEM_PATH;
        mNotifier = new ChangeNotifier(this, mBaseUri, application);
    }

    private static NoteItem loadOrUpdateItem(Path path, Cursor cursor, DataManager dataManager,
                                             NoteAppImpl app) {
        synchronized (DataManager.LOCK) {
            TrashNoteItem item = (TrashNoteItem) dataManager.peekNoteObject(path);
            if (item == null) {
                item = new TrashNoteItem(path, app, cursor);
            } else {
                item.updateContent(cursor);
            }
            return item;
        }
    }

    public static Cursor getItemCursor(ContentResolver resolver, Uri uri,
                                       String[] projection, int id) {
        return resolver.query(uri, projection, "_id=?", new String[]{String.valueOf(id)}, null);
    }

    @Override
    public Uri getContentUri() {
        return mBaseUri;
    }

    @Override
    public ArrayList<NoteItem> getNoteItem(int start, int count) {
        DataManager dataManager = mApplication.getDataManager();
        Uri uri = mBaseUri.buildUpon().appendQueryParameter("limit", start + "," + count).build();
        ArrayList<NoteItem> list = new ArrayList<>();
        Cursor cursor = mResolver.query(uri, NOTE_PROJECTION, null, null, mOrderClause);
        if (cursor == null) {
            Log.d(TAG, "query fail: " + uri);
            return list;
        }

        try {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(TrashNoteItem.INDEX_ID);
                Path childPath = mItemPath.getChild(id);
                NoteItem item = loadOrUpdateItem(childPath, cursor, dataManager, mApplication);
                list.add(item);
            }
        } finally {
            NoteUtils.closeSilently(cursor);
        }
        return list;
    }

    @Override
    public int getNoteItemCount() {
		//Chenyee wanghaiyan 2018-5-11 modify for CSW1703CX-493 begin
        /*
        if (!NoteUtils.checkExternalStoragePermission(this)) {//wanghaiyan
            return 0;
        }
        */
		//Chenyee wanghaiyan 2018-5-11 modify for CSW1703CX-493 end
        if (mCachedCount == INVALID_COUNT) {
            Cursor cursor = mResolver.query(mBaseUri, COUNT_PROJECTION, null, null, null);
            if (cursor == null) {
                Log.d(TAG, "query fail");
                return 0;
            }
            try {
                mCachedCount = cursor.getCount();
            } catch (Exception e) {
                Log.d(TAG, "TrashNoteSet getNoteItemCount E = " + e);
            } finally {
                NoteUtils.closeSilently(cursor);
            }
        }
        return mCachedCount;
    }

    @Override
    public long reload() {
        if (mNotifier.isDirty()) {
            mDataVersion = nextVersionNumber();
            mCachedCount = INVALID_COUNT;
        }
        return mDataVersion;
    }
}
