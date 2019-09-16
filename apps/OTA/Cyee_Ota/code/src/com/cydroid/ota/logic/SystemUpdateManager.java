package com.cydroid.ota.logic;

import android.content.Context;
import android.provider.Settings;
import com.cydroid.ota.Log;
import com.cydroid.ota.bean.IUpdateInfo;
import com.cydroid.ota.logic.sync.IDownloadInfo;
import com.cydroid.ota.logic.sync.ISyncDownloadExecutor;
import com.cydroid.ota.storage.IDataInvoker;
import com.cydroid.ota.storage.IStorage;
import com.cydroid.ota.storage.Key;
import com.cydroid.ota.storage.SettingUpdateDataInvoker;
import com.cydroid.ota.utils.*;
import com.cydroid.ota.utils.Error;

import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.util.Vector;

import static com.cydroid.ota.logic.State.*;
//Chenyee <CY_Req> <xuyongji> <20180417> modify for CSW1702SE-197 begin
import com.cydroid.ota.SettingUpdateApplication;
//Chenyee <CY_Req> <xuyongji> <20180629> modify for CSW1803A-681 begin
import java.util.Map;
import java.util.HashMap;
//Chenyee <CY_Req> <xuyongji> <20180629> modify for CSW1803A-681 end
//Chenyee <CY_Req> <xuyongji> <20180417> modify for CSW1702SE-197 end

/**
 * Created by borney on 4/13/15.
 */
public class SystemUpdateManager implements ISystemUpdate {
    private static final int NOTIFY_BY_ERROR = 1 << 0;
    private static final int NOTIFY_BY_STATE = 1 << 1;
    private static final String TAG = "SystemUpdateManager";
    private static ISystemUpdate sManager;
    private Context mContext;
    private ContextState mContextState;
    private final Vector<WeakReference<IObserver>> mObservers = new Vector<WeakReference<IObserver>>();
    private IDataInvoker mDataInvoker;
    private ICheckExecutor mCheckExecutor;
    private IDownloadExecutor mDownloadExecutor;
    private IInstallExecutor mInstallExecutor;
    private IDownloadCallback mDownloadCallback;
    private IUpdateInfo mUpdateInfo;

    private SystemUpdateManager(Context context) {
        this.mContext = context.getApplicationContext();
        mDataInvoker = SettingUpdateDataInvoker.getInstance(mContext);
        initState();
    }

    private void initState() {
        mContextState = ContextState.createContextState(mDataInvoker.settingStorage().getString(Key.Setting.KEY_SETTING_CONTEXTSTATE, ""));
        setState(INITIAL, false, false, -1);
        Log.d(TAG, "init state = " + mContextState);
    }

    synchronized static ISystemUpdate getInstance(Context context) {
        if (sManager == null) {
            sManager = new SystemUpdateManager(context);
        }

        return sManager;
    }

    @Override
    public IUpdateInfo getSettingUpdateInfo() {
        if (mUpdateInfo == null) {
            mUpdateInfo = mDataInvoker.settingUpdateInfoStorage().getUpdateInfo();
        }
        return mUpdateInfo;
    }


    @Override
    public ICheckExecutor checkUpdate() {
        if (mCheckExecutor == null) {
            mCheckExecutor = new CheckExecutor(mContext, mCheckExecutorCallback);
            ((AbstractExecutor) mCheckExecutor).setHandler(null);
        }
        return mCheckExecutor;
    }

    @Override
    public IDownloadExecutor downUpdate(IDownloadCallback callback) {
        mDownloadCallback = callback;
        if (mDownloadExecutor == null) {
            mDownloadExecutor = new DownloadExecutor(mContext, mDownloadExecutorCallback);
            ((AbstractExecutor) mDownloadExecutor).setHandler((AbstractExecutor) mCheckExecutor);
        }
        return mDownloadExecutor;
    }

    @Override
    public IInstallExecutor installUpdate() {
        if (mInstallExecutor == null) {
            mInstallExecutor = new InstallExecutor(mContext, mInstallExcutorCallback);
            ((AbstractExecutor) mInstallExecutor).setHandler((AbstractExecutor) mDownloadExecutor);
        }
        return mInstallExecutor;
    }

    @Override
    public IContextState getContextState() {
        synchronized (mContextState) {
            return ContextState.createContextState(mContextState.storageString());
        }
    }

    @Override
    public void registerObserver(IObserver observer) {
        if (observer != null) {
            WeakReference<IObserver> reference = new WeakReference<IObserver>(observer);
            notifyObservers(NOTIFY_BY_STATE | NOTIFY_BY_ERROR, reference);
            mObservers.add(reference);
        }
    }

    @Override
    public void unregisterObserver(IObserver observer) {
        if (observer != null) {
            for (WeakReference<IObserver> reference : mObservers) {
                IObserver iObserver = reference.get();
                if (iObserver != null && iObserver.equals(observer)) {
                    mObservers.remove(reference);
                    break;
                }
            }
        }
    }

    private void setState(State state) {
        this.setState(state, mContextState.isRoot());
    }

    private void setState(State state, boolean isRoot) {
        this.setState(state, isRoot, false);
    }

    private void setState(State state, boolean isRoot, boolean isBackState) {
        this.setState(state, isRoot, isBackState, -1);
    }

    private void setState(State state, boolean isRoot, boolean isBackState, int error) {
        if (mContextState.state() == state && mContextState.isRoot() == isRoot && mContextState.isBackState() == isBackState && mContextState.error() == error) {
            Log.d(TAG, "set a same context state!!!");
            return;
        }
        Log.d(TAG, "setState: " + state + " isRoot = " + isRoot + " isBackState = " + isBackState + " error = " + error);
        synchronized (mContextState) {
            mContextState.setState(state);
            mContextState.setRoot(isRoot);
            mContextState.setBackstate(isBackState);
            mContextState.setError(error);
            mDataInvoker.settingStorage().putString(Key.Setting.KEY_SETTING_CONTEXTSTATE, mContextState.storageString());
        }
        notifyObservers(NOTIFY_BY_STATE, getObserverArray());
    }

    private void setError(int error) {
        synchronized (mContextState) {
            mContextState.setError(error);
            mDataInvoker.settingStorage().putString(Key.Setting.KEY_SETTING_CONTEXTSTATE, mContextState.storageString());
        }
        notifyObservers(NOTIFY_BY_ERROR, getObserverArray());
    }

    private void notifyObservers(int flag, WeakReference<IObserver>... refes) {
        Vector<WeakReference<IObserver>> removes = new Vector<WeakReference<IObserver>>();
        for (WeakReference<IObserver> reference : refes) {
            IObserver observer = reference.get();
            if (observer != null) {
                IContextState contextState = getContextState();
                if ((flag & NOTIFY_BY_STATE) == NOTIFY_BY_STATE) {
                    observer.onStateChange(contextState);
                }
                if ((flag & NOTIFY_BY_ERROR) == NOTIFY_BY_ERROR) {
                    observer.onError(contextState, contextState.error());
                }
            } else {
                removes.add(reference);
            }
        }
        mObservers.removeAll(removes);
    }

    private WeakReference<IObserver>[] getObserverArray() {
        WeakReference<IObserver>[] refers = getArrayByClass(WeakReference.class, mObservers.size());
        int i = 0;
        for (WeakReference<IObserver> reference : mObservers) {
            refers[i++] = reference;
        }
        return refers;
    }

    private <T> T[] getArrayByClass(Class<T> tClass, int size) {
        T[] arr = (T[]) Array.newInstance(tClass, size);
        return arr;
    }

    private IExcutorCallback mCheckExecutorCallback = new IExcutorCallback() {

        @Override
        public void onResult(Object... args) {
            if (args.length == 0) {
                setState(CHECKING);
                return;
            }
            boolean isRoot = (Boolean) args[0];
            if (isRoot && args.length == 1) {//root
                setState(mContextState.state(), isRoot);
                return;
            }

            boolean haveNewVersion = (Boolean) args[1];
            if (mContextState.state() != CHECKING) {
                Log.e(TAG, "error state " + mContextState);
            }
            if (haveNewVersion) {
                IUpdateInfo updateInfo = (IUpdateInfo) args[2];
                mUpdateInfo = updateInfo;
                mDataInvoker.settingUpdateInfoStorage().clear();
                mDataInvoker.settingUpdateInfoStorage().storage(updateInfo);
                setState(READY_TO_DOWNLOAD);
                Util.notifyLaucherShowBadge(mContext,1);
            } else {
                setState(INITIAL, mContextState.isRoot(), true, 0);
                Util.notifyLaucherShowBadge(mContext,0);
            }
        }

        @Override
        public void onError(int code) {
            setState(INITIAL, mContextState.isRoot(), true, code);
        }
    };

    private IExcutorCallback mDownloadExecutorCallback = new IExcutorCallback() {
        private long curFileSize = -1;

        @Override
        public void onResult(Object... args) {
            final State state = mContextState.state();
            if (args.length == 0) {
                switch (state) {
                    case READY_TO_DOWNLOAD:
                    case DOWNLOAD_PAUSE:
                        setState(DOWNLOADING);
                        break;
                    case DOWNLOAD_INTERRUPT:
                        ISyncDownloadExecutor executor = (ISyncDownloadExecutor) mDownloadExecutor;
                        if (executor.isStart()) {
                            setState(DOWNLOADING);
                        } else {
                            setState(DOWNLOAD_PAUSE);
                        }
                        break;
                    case DOWNLOADING:
                        setState(DOWNLOAD_PAUSEING);
                        break;
                    case DOWNLOAD_PAUSEING:
                        setState(DOWNLOAD_PAUSE);
                        break;
                    case DOWNLOAD_COMPLETE:
                        setState(DOWNLOAD_VERIFY);
                        boolean isAutoDownload = AutoUpgradeManager.getInstance(mContext).isAutoUpgrade();
                        IStorage settingStorage = SettingUpdateDataInvoker.getInstance(
                                mContext).settingStorage();
                        settingStorage.putBoolean(Key.Setting.KEY_SETTING_UPDATE_LAST_DOWNLOAD_FLAG, isAutoDownload);
						//Chenyee <CY_Req> <xuyongji> <20180417> modify for CSW1702SE-197 begin
                        if (SettingUpdateApplication.sStatisticsHelper != null) {
							//Chenyee <CY_Req> <xuyongji> <20180629> modify for CSW1803A-681 begin
                            String upgradeVersion = getSettingUpdateInfo().getInternalVer();
                            SettingUpdateApplication.sStatisticsHelper.writeStatisticsData(StatisticsHelper.EVENT_FINISH_DOWNLOAD, 
								StatisticsHelper.KEY_UPGRADED_VERSION_INFO, upgradeVersion);
							//Chenyee <CY_Req> <xuyongji> <20180629> modify for CSW1803A-681 end	
                        }
						//Chenyee <CY_Req> <xuyongji> <20180417> modify for CSW1702SE-197 end
                        break;
                    default:
                        break;
                }
                return;
            }

            boolean downloading = ((Boolean) args[0]).booleanValue();
            if (downloading) {
                IDownloadInfo downInfo = (IDownloadInfo) args[1];
                long totalSize = getSettingUpdateInfo().getFileSize();
                curFileSize = downInfo.getDownloadLength();
                if (mDownloadCallback != null) {
                    mDownloadCallback.onProgress(getSettingUpdateInfo().getFileSize(), curFileSize, downInfo.getSpeed());
                }
                if (curFileSize == totalSize) {
                    setState(DOWNLOAD_COMPLETE);
                }
            } else {
                setState(DOWNLOAD_INTERRUPT);
                Log.d(TAG, "DOWNLOAD_INTERRUPT reason:" + mDownloadExecutor.getInterruptReason());
            }
        }

        @Override
        public void onError(int code) {
            Log.d(TAG, "mDownloadExecutorCallback onError:" + code);
            if (code == Error.ERROR_CODE_DOWNLOAD_NETWORK_INTERRUPT) {
                setState(State.DOWNLOAD_INTERRUPT, mContextState.isRoot(), false, code);
            } else if (code == Error.ERROR_CODE_RESUME_DOWNLOAD_BY_MOBILENET) {
                setError(code);
            } else if (code == Error.ERROR_CODE_DOWNLOAD_NETWORK_INTERRUPT_NO_FILE) {
                setState(State.INITIAL, mContextState.isRoot(), false, code);
            } else {
                setState(State.READY_TO_DOWNLOAD, mContextState.isRoot(), false, code);
            }
            if (mDownloadCallback != null) {
                mDownloadCallback.onError(code);
            }
			//Chenyee <CY_Req> <xuyongji> <20180417> modify for CSW1702SE-197 begin
            if (SettingUpdateApplication.sStatisticsHelper != null) {
                String downErrorStr;
                switch (code) {
                    case Error.ERROR_CODE_NETWORK_ERROR:
                        downErrorStr = "NETWORK_ERROR";
                        break;
                    case Error.ERROR_CODE_STORAGE_NOT_MOUNTED:
                        downErrorStr = "STORAGE_NOT_MOUNTED";
                        break;
                    case Error.ERROR_CODE_DOWNLOAD_NO_SPACE:
                        downErrorStr = "DOWNLOAD_NO_SPACE";
                        break;
                    case Error.ERROR_CODE_DOWNLOAD_FILE_NOT_EXIST:
                        downErrorStr = "DOWNLOAD_FILE_NOT_EXIST";
                        break;
                    case Error.ERROR_CODE_DOWNLOAD_FILE_WRITE_ERROR:
                        downErrorStr = "DOWNLOAD_FILE_WRITE_ERROR";
                        break;
                    case Error.ERROR_CODE_DOWNLOAD_FILE_VERIFY_FAILED:
                        downErrorStr = "DOWNLOAD_FILE_VERIFY_FAILED";
                        break;
                    case Error.ERROR_CODE_DOWNLOAD_NETWORK_INTERRUPT:
                        downErrorStr = "DOWNLOAD_NETWORK_INTERRUPT";
                        break;
                    case Error.ERROR_CODE_DOWNLOAD_STORAGE_INTERRUPT:
                        downErrorStr = "DOWNLOAD_STORAGE_INTERRUPT";
                        break;
                    default:
                        downErrorStr = "Unknown";
                        break;
                }
				//Chenyee <CY_Req> <xuyongji> <20180629> modify for CSW1803A-681 begin
                Map<String, String> attrs = new HashMap<String, String>();
                String upgradeVersion = getSettingUpdateInfo().getInternalVer();
                attrs.put(StatisticsHelper.KEY_DOWNLOAD_FAIL_REASON, downErrorStr);
                attrs.put(StatisticsHelper.KEY_UPGRADED_VERSION_INFO, upgradeVersion);
                SettingUpdateApplication.sStatisticsHelper.writeStatisticsData(StatisticsHelper.EVENT_FAIL_DOWNLOAD, attrs);
				//Chenyee <CY_Req> <xuyongji> <20180629> modify for CSW1803A-681 end
            }
			//Chenyee <CY_Req> <xuyongji> <20180417> modify for CSW1702SE-197 end
        }
    };

    private IExcutorCallback mInstallExcutorCallback = new IExcutorCallback() {
        @Override
        public void onResult(Object... objects) {
            if (objects.length == 0) {
                setState(INSTALLING);
				//Chenyee <CY_Req> <xuyongji> <20180417> modify for CSW1702SE-197 begin
                if (SettingUpdateApplication.sStatisticsHelper != null) {
                    SettingUpdateApplication.sStatisticsHelper.writeStatisticsData(StatisticsHelper.EVENT_START_UPGRADE);
                }
				//Chenyee <CY_Req> <xuyongji> <20180417> modify for CSW1702SE-197 end
            }
        }

        @Override
        public void onError(int errorCode) {
            if (errorCode == Error.ERROR_CODE_INSTALL_FILE_NOT_EXIT) {
                setState(State.READY_TO_DOWNLOAD, mContextState.isRoot(),
                        mContextState.isBackState(), errorCode);
            } else {
                setState(State.DOWNLOAD_VERIFY, mContextState.isRoot(),
                        mContextState.isBackState(), errorCode);
            }
        }
    };
}
