package com.cydroid.ota.logic;

import android.content.Context;
import com.cydroid.ota.logic.sync.ISyncCallback;
import com.cydroid.ota.logic.sync.InstallJob;
import com.cydroid.ota.logic.sync.Job;
import com.cydroid.ota.logic.sync.ScanAvailableJob;
import com.cydroid.ota.Log;

import java.util.ArrayList;

/**
 * Created by borney on 4/21/15.
 */
public class ScanLocalExecutor extends AbstractExecutor implements ILocalUpdateExecutor {
    private static final String TAG = "ScanLocalExecutor";
    private Job mScanJob;
    private Job mInstallJob;
    private Context mContext;
    private IExcutorCallback mExcutorCallback;

    protected ScanLocalExecutor(Context context, IExcutorCallback callback) {
        this.mContext = context;
        mExcutorCallback = callback;
    }

    @Override
    public void scanAvailable() {
        if (mScanJob == null) {
            mScanJob = new ScanAvailableJob(mContext, isSystemRoot(), mScanCallback);
        } else if (mScanJob.isRunning()) {
            Log.e(TAG, "scan job is running!!!!");
            return;
        }
        syncexe(mScanJob);
    }

    @Override
    public void install(String fileName) {
        if (mScanJob != null && mScanJob.isRunning()) {
            Log.e(TAG, "scan job not ended when systemRestart " + fileName);
            return;
        }
        if (mInstallJob == null) {
            mInstallJob = new InstallJob(mContext, fileName, mInstallCallback);
        } else {
            Log.e(TAG, "install job is runninng!!!");
            return;
        }
        syncexe(mInstallJob);
    }

    @Override
    protected void handler() {
    }

    private ISyncCallback mScanCallback = new ISyncCallback() {
        @Override
        public void onResult(Object... objects) {
            ArrayList<String> availables = (ArrayList<String>) objects[0];
            /*for (int i = 0, length = availables.size(); i < length; i++) {
                Log.d(TAG, "scan[" + i + "] = " + availables.get(i));
            }*/
            if (mExcutorCallback != null) {
                mExcutorCallback.onResult(availables);
            }
        }

        @Override
        public void onError(int errorCode) {
            Log.e(TAG, "onError = " + errorCode);
            if (mExcutorCallback != null) {
                mExcutorCallback.onError(errorCode);
            }
        }
    };

    private ISyncCallback mInstallCallback = new ISyncCallback() {
        @Override
        public void onResult(Object... objects) {

        }

        @Override
        public void onError(int errorCode) {

        }
    };
}
