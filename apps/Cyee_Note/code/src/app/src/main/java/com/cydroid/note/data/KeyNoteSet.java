package com.cydroid.note.data;

import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;

import com.cydroid.note.app.DataConvert;
import com.cydroid.note.app.NoteAppImpl;
import com.cydroid.note.common.Future;
import com.cydroid.note.common.FutureListener;
import com.cydroid.note.common.NoteUtils;
import com.cydroid.note.common.ThreadPool;

import java.util.ArrayList;
import java.util.List;

public abstract class KeyNoteSet extends NoteSet implements FutureListener<ArrayList<NoteItem>> {
    private static final String TAG = "KeyNoteSet";

    private NoteAppImpl mApp;
    protected Uri mBaseUri;
    protected NoteSet mBaseSet;
    private final Handler mMainHandler;
    private Future<ArrayList<NoteItem>> mLoadTask;
    private String mKey;
    private ArrayList<NoteItem> mLoadBuffer;
    protected ChangeNotifier mNotifier;
    private ArrayList<NoteItem> mKeyNotes = new ArrayList<>();

    public KeyNoteSet(Path path, NoteAppImpl application) {
        super(path, nextVersionNumber());
        mApp = application;
        mMainHandler = new Handler(application.getMainLooper());
        initDataSource(application);
    }

    public synchronized void initDataSource(NoteAppImpl application) {
    }

    @Override
    public Uri getContentUri() {
        return mBaseUri;
    }

    @Override
    public ArrayList<NoteItem> getNoteItem(int start, int count) {
        ArrayList<NoteItem> list = new ArrayList<>();
        ArrayList<NoteItem> noteItems = mKeyNotes;
        int end = start + count;
        int size = noteItems.size();
        if (size == 0 || start >= size) {
            return list;
        }
        if (end > size) {
            end = size;
        }
        List<NoteItem> subList = noteItems.subList(start, end);
        list.addAll(subList);
        return list;
    }

    @Override
    public int getNoteItemCount() {
        return mKeyNotes.size();
    }

    @Override
    public synchronized long reload() {
        if (mNotifier.isDirty()) {
            if (mLoadTask != null) {
                mLoadTask.cancel();
            }
            mLoadTask = mApp.getThreadPool().submit(new KeyNoteLoader(), this);
        }

        if (mLoadBuffer != null) {
            mKeyNotes = mLoadBuffer;
            mLoadBuffer = null;
            mDataVersion = nextVersionNumber();
        }
        return mDataVersion;
    }

    @Override
    public synchronized void onFutureDone(Future<ArrayList<NoteItem>> future) {
        if (mLoadTask != future) {
            return;        // ignore, wait for the latest task
        }
        mLoadBuffer = future.get();
        if (mLoadBuffer == null) {
            mLoadBuffer = new ArrayList<NoteItem>();
        }
        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                notifyContentChanged();
            }
        });
    }

    public synchronized void setKey(String key) {
        mKey = key;
        mLoadBuffer = null;
        mKeyNotes = new ArrayList<>();
        mNotifier.fakeChange();
    }

    private boolean containKey(NoteItem item) {
        String key = mKey;
        if (key == null || TextUtils.isEmpty(key)) {
            return false;
        }
        String title = item.getTitle();
	    //GIONEE wanghaiyan 2017-12-21 modify for 51694 begin
	    /*
        if (title != null && title.contains(key)) {
            return true;
        }
        */
        if (title != null && containsString(title,key,false)){
		    return true;	
        }
        String json = item.getContent();
        String content = DataConvert.getContent(json);
        //return content.contains(key);
        return containsString(content,key,false);
        //GIONEE wanghaiyan 2017-12-21 modify for 51694 end
        
    }
    
    //GIONEE wanghaiyan 2017-12-21 modify for 51694 begin
    public static boolean containsString (String original,String key, boolean caseSensitive){
		if(caseSensitive){
			return original.contains(key);
		}else{
		 	return original.toLowerCase().contains(key.toLowerCase());
		}
    }
    //GIONEE wanghaiyan 2017-12-21 modify for 51694 end

    private class KeyNoteLoader implements ThreadPool.Job<ArrayList<NoteItem>> {
        @Override
        public ArrayList<NoteItem> run(final ThreadPool.JobContext jc) {
            final ArrayList<NoteItem> keyItems = new ArrayList<>();
			//Chenyee wanghaiyan 2018-5-11 modify for CSW1703CX-493 begin
            /*
            if (!NoteUtils.checkExternalStoragePermission(this)) {//wanghaiyan
                return keyItems;
            }
            */
			//Chenyee wanghaiyan 2018-5-11 modify for CSW1703CX-493 end
            mBaseSet.enumerateNoteItems(new ItemConsumer() {//NOSONAR
                @Override
                public void consume(int index, NoteItem item) {//NOSONAR
                    if (containKey(item)) {//NOSONAR
                        keyItems.add(item);//NOSONAR
                    }
                }
            }, jc);//NOSONAR

            return keyItems;
        }
    }
}
