package com.cydroid.note.app;

import android.app.filecrypt.zyt.filesdk.FileCryptSDK;
import android.content.Context;
import android.content.res.Configuration;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Process;

import com.gionee.framework.component.BaseApplication;
import com.cydroid.note.app.inputbackup.ImportBackUp;
import com.cydroid.note.common.GnChameleonObserver;
import com.cydroid.note.common.NoteUtils;
import com.cydroid.note.common.PlatformUtil;
import com.cydroid.note.common.ThreadPool;
import com.cydroid.note.data.DataManager;
import com.cydroid.note.widget.WidgetUtil;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import java.util.ArrayList;
import java.util.Iterator;

public class NoteAppImpl extends BaseApplication {
    private static NoteAppImpl mApp;
    private DataManager mDataManager;
    private LabelManager mLabelManager;

    private ThreadPool mThreadPool;
    private Looper mNoteBackgroundLooper;
    private ImportBackUp mImportBackUp;
    private ArrayList<NoteDbInitCompleteNotify> mCurrentNotifys = new ArrayList();
    private int mDensityDpi;

    @Override
    public void onCreate() {
        super.onCreate();
        initContext();
        NoteUtils.initScreenSize(this);
        new GnChameleonObserver(this);
        initThreadPool();
        initLabelManager();
        ImageLoaderConfiguration configuration = new ImageLoaderConfiguration.Builder(this)
                .imageDownloader(new CustomImageDownloder(this)).build();
        ImageLoader.getInstance().init(configuration);
        new StatisticsSameApp().checkAsync(this);
        if (PlatformUtil.isSecurityOS()) {
            initFileCryptSDK();
        }
        new NoteActionExecutor().startRecoveryEncryptUnCompleteNote();
        mDensityDpi = getResources().getDisplayMetrics().densityDpi;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        enableLog(BaseApplication.FLAG_OPEN_LOG_TO_FILE);
    }

    private void initFileCryptSDK() {
        FileCryptSDK.init(this);
    }

    private void initContext() {
        mApp = this;
    }

    private void initLabelManager() {
        mLabelManager = new LabelManager(this);
        mLabelManager.init();
    }

    private void initThreadPool() {
        mThreadPool = new ThreadPool();
    }

    public ThreadPool getThreadPool() {
        return mThreadPool;
    }

    public synchronized DataManager getDataManager() {
        if (mDataManager == null) {
            mDataManager = new DataManager(this);
            mDataManager.initializeSourceMap();
        }
        return mDataManager;
    }

    public LabelManager getLabelManager() {
        return mLabelManager;
    }

    public static NoteAppImpl getContext() {
        return mApp;
    }

    public synchronized Looper getNoteBackgroundLooper() {
        if (mNoteBackgroundLooper == null) {
            HandlerThread handlerThread = new HandlerThread("save note data");
            handlerThread.start();
            mNoteBackgroundLooper = handlerThread.getLooper();
        }
        return mNoteBackgroundLooper;
    }

    public ImportBackUp getImportBackUp() {
        synchronized (this) {//NOSONAR
            if (mImportBackUp == null) {//NOSONAR
                mImportBackUp = new ImportBackUp();//NOSONAR
            }//NOSONAR
        }
        mImportBackUp.resetEnv();//NOSONAR
        return mImportBackUp;//NOSONAR
    }

    public void registerNoteDbInitCompleteNotify(NoteDbInitCompleteNotify notify) {
        if (!mCurrentNotifys.contains(notify)) {
            mCurrentNotifys.add(notify);
        }
    }

    public void unRegisterNoteDbInitCompleteNotify(NoteDbInitCompleteNotify notify) {
        mCurrentNotifys.remove(notify);
    }

    public void notifyDbInitComplete() {
        WidgetUtil.updateAllWidgets();

        for (Iterator it = mCurrentNotifys.iterator(); it.hasNext(); ) {
            NoteDbInitCompleteNotify notify = (NoteDbInitCompleteNotify) it.next();
            notify.onNoteDbInitComplete();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        int densityDpi = newConfig.densityDpi;
        if (mDensityDpi != densityDpi) {
            mDensityDpi = densityDpi;
            Process.killProcess(Process.myPid());
        }

        super.onConfigurationChanged(newConfig);
    }
}
