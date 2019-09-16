
package com.cydroid.ota.logic.sync;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import com.cydroid.ota.execption.SettingUpdateRuntimeException;
import com.cydroid.ota.Log;
import org.apache.http.HttpEntity;

import java.io.InputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class Job {
    private final String TAG = getClass().getSimpleName();
    private static final int MSG_PRO_CREATE_SYNC_THREAD = 0x100001;
    protected JobInfo mJobInfo;
    private InternalHandler mHandler;
    private AtomicBoolean isRunning = new AtomicBoolean(false);
    protected ISyncCallback mJobCallback;
    private HandlerThread mSyncThread;
    private Handler mSyncHandler;


    protected Job(ISyncCallback callback) {
        if (callback == null) {
            throw new NullPointerException("callback is null!!!");
        }
        if (!isMainThread()) {
            throw new SettingUpdateRuntimeException("Can not be executed in the main thread !!!");
        }
        mJobCallback = callback;
        mHandler = new InternalHandler(Looper.myLooper());
        mSyncThread = new HandlerThread("SyncThread");
        mHandler.sendEmptyMessage(MSG_PRO_CREATE_SYNC_THREAD);
        mJobInfo = new JobInfo();
        mJobInfo.mCallable = new CallableImpl();
    }

    public abstract <T> T run();

    protected void sendMessage(int what, Object obj) {
        Message msg = mHandler.obtainMessage(what);
        msg.obj = obj;
        sendMessage(msg);
    }

    protected void sendMessage(Message message) {
        message.sendToTarget();
    }

    protected void sendMessage(int what) {
        Message msg = mHandler.obtainMessage(what);
        sendMessage(msg);
    }

    protected void sendSyncMessage(Message message) {
        Log.d(TAG, "Sync job: sendMessage:" + message.what);
        if (mSyncHandler != null) {
            mSyncHandler.sendMessage(message);
        }
    }

    protected Message obtainMessage(int what) {
        return mHandler.obtainMessage(what);
    }

    protected Message obtainSyncMessage(int what) {
        if (mSyncHandler != null) {
            return mSyncHandler.obtainMessage(what);
        }
        return new Message();
    }

    abstract void handleJobMessage(Message msg);

    protected void handleJobSyncMessage(Message message){

    }

    protected static void consume(final HttpEntity entity) {
        if (entity == null) {
            return;
        }
        if (entity.isStreaming()) {
            try {
                InputStream instream = entity.getContent();
                if (instream != null) {
                    instream.close();
                }
            } catch (Throwable t) {
                // do nothing
            }
        }
    }

    protected boolean isTestEnv() {
        return true;
    }

    public boolean cancel() {
        final JobInfo info = mJobInfo;
        if (info.mFuture != null) {
            boolean cancel = info.mFuture.cancel(true);
            Log.d(TAG, TAG + " job cancel = " + cancel);
            return cancel;
        }
        return false;
    }

    public boolean isCanceled() {
        final JobInfo info = mJobInfo;
        return info.mFuture.isCancelled();
    }

    public boolean isRunning() {
        return isRunning.get();
    }

    private void setAtomicBoolean(AtomicBoolean atomic, boolean value) {
        for (; ; ) {
            boolean old = atomic.get();
            if (atomic.compareAndSet(old, value)) {
                return;
            }
        }
    }

    private boolean isMainThread() {
        if (Looper.myLooper().equals(Looper.getMainLooper())) {
            return true;
        }
        return false;
    }

    private class InternalHandler extends Handler {

        InternalHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_PRO_CREATE_SYNC_THREAD:
                mSyncThread.start();
                mSyncHandler = new Handler(mSyncThread.getLooper(), new SyncCallback());
                break;
            default:
                handleJobMessage(msg);
                break;
            }
        }
    }

    private class SyncCallback implements Handler.Callback {
        @Override
        public boolean handleMessage(Message message) {
            Log.d(TAG, "SyncCallback handleMessage:" + message.what);
            handleJobSyncMessage(message);
            return false;
        }
    }

    private class CallableImpl<T> implements Callable<T> {

        @Override
        public T call() throws Exception {
            Log.d(TAG, "start run " + TAG);
            setAtomicBoolean(isRunning, true);
            run();
            setAtomicBoolean(isRunning, false);
            Log.d(TAG, "end run " + TAG);
            return null;
        }
    }

    protected static class JobInfo {
        protected Callable mCallable;
        protected Future mFuture;
    }

    protected static class MSG {
        protected static final int BASE = 100;
        protected final static AtomicInteger sBase = new AtomicInteger(1);
    }
}
