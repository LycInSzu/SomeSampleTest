package com.cydroid.ota.logic;

import com.cydroid.ota.CyeePrt;
import com.cydroid.ota.Log;
import com.cydroid.ota.execption.SettingUpdateException;
import com.cydroid.ota.logic.sync.AsyncWorkService;
import com.cydroid.ota.logic.sync.Job;

/**
 * Created by borney on 4/15/15.
 */
public abstract class AbstractExecutor implements IExecutor {
    private static final String TAG = "AbstractExecutor";
    private Job mJob;
    private AbstractExecutor mHandler;

    static {
        try {
            AbstractExecutor.class.getClassLoader().loadClass("com.cydroid.ota.CyeePrt");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    void syncexe(Job job) {
        releaseHandler();
        Log.d(TAG, "syncexe " + getClass().getSimpleName());
        if (job == null || job.isRunning()) {
            return;
        }
        mJob = job;
        try {
            AsyncWorkService.getInstance().submit(mJob);
        } catch (SettingUpdateException e) {
            e.printStackTrace();
        }
    }

    void exe(Job job) {
        releaseHandler();
        Log.d(TAG, "exe " + getClass().getSimpleName());
        if (job == null || job.isRunning()) {
            return;
        }
        mJob = job;
        mJob.run();
    }

    @Override
    public void cancel() {
        Log.d(TAG, "cancel " + getClass().getSimpleName());
        if (mJob != null && !mJob.isCanceled()) {
            mJob.cancel();
            mJob = null;
        }
    }

    @Override
    public boolean isRunning() {
        if (mJob != null) {
            return mJob.isRunning();
        }
        return false;
    }

    protected boolean isSystemRoot() {

        return CyeePrt.isSystemRoot();
    }

    protected void setHandler(AbstractExecutor executor) {
        mHandler = executor;
    }

    protected abstract void handler();

    private void releaseHandler() {
        if (mHandler != null) {
            mHandler.handler();
        }
    }
}
