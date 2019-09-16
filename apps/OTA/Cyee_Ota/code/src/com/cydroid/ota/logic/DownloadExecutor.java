package com.cydroid.ota.logic;

import android.app.ActivityManager;
import android.app.Notification;
//Chenyee <CY_Bug> <xuyongji> <20180109> modify for CSW1702A-813 begin
import android.app.NotificationChannel;
//Chenyee <CY_Bug> <xuyongji> <20180109> modify for CSW1702A-813 end
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.*;
import android.net.ConnectivityManager;
import android.os.Environment;
import android.text.TextUtils;

import com.cydroid.ota.Log;
import com.cydroid.ota.SettingUpdateApplication;
import com.cydroid.ota.bean.IUpdateInfo;
import com.cydroid.ota.logic.sync.DownloadJob;
import com.cydroid.ota.logic.sync.ISyncCallback;
import com.cydroid.ota.logic.sync.ISyncDownloadExecutor;
import com.cydroid.ota.storage.IStorage;
import com.cydroid.ota.storage.Key;
import com.cydroid.ota.storage.SettingUpdateDataInvoker;
//Chenyee <CY_Bug> <xuyongji> <20180224> modify for CSW1702A-3019 begin
import com.cydroid.ota.ui.SystemUpdateAnimActivity;
//Chenyee <CY_Bug> <xuyongji> <20180224> modify for CSW1702A-3019 end
import com.cydroid.ota.utils.*;
import com.cydroid.ota.utils.Error;

import com.cydroid.ota.R;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by borney on 4/20/15.
 */
public class DownloadExecutor extends AbstractExecutor implements IDownloadExecutor,
    ISyncDownloadExecutor, IObserver {
    private static final String TAG = "DownloadExecutor";
    private Context mContext;
    private IExcutorCallback mCallback;
    private ISystemUpdate mSystemUpdate;
    private DownloadJob mDownloadJob;
    private NetworkBroadcastReceiver mNetworkBroadcastReceiver;
    private StorageBroadcastReceiver mStorageBroadcastReceiver;
    private Notification.Builder mDownloadNotificationBaseBuilder;
    private NotificationManager mNotificationManager;
    private State mNotifyState = State.ERROR;
    private boolean isRegister = false;
    private boolean isStart = false;
    private boolean isReadyToDownload = false;
    private int mInterruptReason;
    private int mNotifyProgress = -1;
    private final Map<State, String> mNotifyContens = new HashMap<State, String>();

    DownloadExecutor(Context context, IExcutorCallback callback) {
        this.mContext = context;
        this.mCallback = callback;
        initNotify();
        mSystemUpdate = SystemUpdateFactory.systemUpdate(context);
        mSystemUpdate.registerObserver(this);
        mNetworkBroadcastReceiver = new NetworkBroadcastReceiver();
        mStorageBroadcastReceiver = new StorageBroadcastReceiver();
    }

    private void initNotify() {
        mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotifyContens.put(State.DOWNLOADING, mContext.getString(
            R.string.gn_su_download_notify_content_downloading));
        mNotifyContens.put(State.DOWNLOAD_PAUSEING, mContext.getString(R.string.gn_su_download_notify_content_pausing));
        mNotifyContens.put(State.DOWNLOAD_PAUSE, mContext.getString(R.string.gn_su_download_notify_content_pause));
        mNotifyContens.put(State.DOWNLOAD_INTERRUPT, mContext.getString(R.string.gn_su_download_notify_content_interrupt));
        mNotifyContens.put(State.DOWNLOAD_COMPLETE, mContext.getString(
            R.string.gn_su_download_notify_content_downloadcomplete));
        mNotifyContens.put(State.DOWNLOAD_VERIFY, mContext.getString(
            R.string.gn_su_download_notify_content_downloadverfy));
    }

    @Override
    public void start() {
        Log.d(TAG, "start()");
        isStart = true;
        isReadyToDownload = true;
        IContextState state = mSystemUpdate.getContextState();
        if (state.state() != State.READY_TO_DOWNLOAD) {
            return;
        }
        if (mCallback != null) {
            mCallback.onResult(); //change state to DOWNLOADING
        }
        if (mDownloadJob != null && !mDownloadJob.isCanceled()) {
            mDownloadJob.cancel();
            mDownloadJob = null;
        }
        mDownloadJob = new DownloadJob(mContext, mDownloadJobCallback, this);
        syncexe(mDownloadJob);
        //Chenyee <CY_Req> <xuyongji> <20180417> modify for CSW1702SE-197 begin
        if (SettingUpdateApplication.sStatisticsHelper != null) {
            SettingUpdateApplication.sStatisticsHelper.writeStatisticsData(StatisticsHelper.EVENT_START_DOWNLOAD,
                    StatisticsHelper.KEY_NET_TYPE, NetworkUtils.getNetStatisticsInfo(mContext));
        }
        //Chenyee <CY_Req> <xuyongji> <20180417> modify for CSW1702SE-197 end
		//Chenyee <CY_Bug> <xuyongji> <20180208> remove for CSW1705A-1639 begin
        //startOtaUpgradeService();
		//Chenyee <CY_Bug> <xuyongji> <20180208> remove for CSW1705A-1639 end
    }

    @Override
    public void pause() {
        isStart = false;
        isReadyToDownload = false;
        IContextState state = mSystemUpdate.getContextState();
        if (state.state() == State.DOWNLOADING || state.state() == State.DOWNLOAD_INTERRUPT) {
            if (mCallback != null) {
                Log.d(TAG, "pause-------()callback state = " + state);
                mCallback.onResult(); //change state to DOWNLOAD_PAUSEING
            }
        }
		//Chenyee <CY_Bug> <xuyongji> <20180208> remove for CSW1705A-1639 begin
        //stopOtaUpgradeService();
		//Chenyee <CY_Bug> <xuyongji> <20180208> remove for CSW1705A-1639 end
    }

    @Override
    public void cancel() {
        super.cancel();
        isStart = false;
        isReadyToDownload = false;
        mCallback.onError(Error.ERROR_CODE_JOB_CANCEL);
		//Chenyee <CY_Bug> <xuyongji> <20180208> remove for CSW1705A-1639 begin
        //stopOtaUpgradeService();
		//Chenyee <CY_Bug> <xuyongji> <20180208> remove for CSW1705A-1639 end
    }

    @Override
    public boolean isContinue() {
        IContextState state = mSystemUpdate
            .getContextState();
        if (state.state() == State.DOWNLOADING) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isStart() {
        return isStart;
    }

    @Override
    public boolean isReadyToDownload() {
        return isReadyToDownload;
    }

    @Override
    public boolean isAutoUpgrade() {
        return SystemUpdateFactory.autoUpgrade(mContext).isAutoUpgrade();
    }

    @Override
    public boolean isRootState() {
        return mSystemUpdate.getContextState().isRoot();
    }

    @Override
    public IUpdateInfo getUpgradeInfo() {
        return mSystemUpdate.getSettingUpdateInfo();
    }

    @Override
    public void restart() {
        isStart = true;
        isReadyToDownload = false;
        Log.d(TAG, "restart() " + mDownloadJob + "  cancel = " + ((mDownloadJob != null) ? mDownloadJob.isCanceled() : "no"));
        IContextState state = mSystemUpdate.getContextState();
        if (state.state() == State.DOWNLOAD_PAUSE
            || state.state() == State.DOWNLOAD_INTERRUPT) {
            if (mCallback != null) {
                mCallback.onResult(); //change state to DOWNLOADING
            }
            if (mDownloadJob != null && !mDownloadJob.isCanceled()) {
                mDownloadJob = null;
            }
            mDownloadJob = new DownloadJob(mContext, mDownloadJobCallback, this);
            syncexe(mDownloadJob);
			//Chenyee <CY_Bug> <xuyongji> <20180208> remove for CSW1705A-1639 begin
            //startOtaUpgradeService();
			//Chenyee <CY_Bug> <xuyongji> <20180208> remove for CSW1705A-1639 end
        }
    }

    @Override
    public int getInterruptReason() {
        IContextState state = mSystemUpdate.getContextState();
        if (state.state() == State.DOWNLOAD_INTERRUPT) {
            return mInterruptReason;
        }
        return INTERRUPT_REASON_DEFAULT;
    }

    @Override
    public long getDownloadedFileSize() {
        IStorage settingStorage = SettingUpdateDataInvoker.getInstance(mContext).settingStorage();
        String fileName = settingStorage.getString(Key.Setting.KEY_SETTING_DOWNLOAD_FILE_NAME, "");
        File file = new File(fileName);
        long fileSize = 0;
        if (file.exists()) {
            fileSize = file.length();
        }
        return fileSize;
    }

    @Override
    public int getDownloadProgress() {
        long fileSize = mSystemUpdate.getSettingUpdateInfo().getFileSize();
        if (fileSize == 0) {
            return 0;
        }
        return (int) (getDownloadedFileSize() * 100 / fileSize);
    }

    @Override
    protected void handler() {
        mDownloadJob = null;
    }

    @Override
    public void onStateChange(IContextState state) {
        switch (state.state()) {
            case DOWNLOADING:
                registerDownloadBroadcastReceiver();
                updateNotification();
                break;
            case DOWNLOAD_PAUSEING:
            case DOWNLOAD_INTERRUPT:
                updateNotification();
                break;
            case READY_TO_DOWNLOAD:
            case DOWNLOAD_COMPLETE:
            case DOWNLOAD_PAUSE:
                unRegisterDownloadBroadcastReceiver();
                updateNotification();
                break;
            case DOWNLOAD_VERIFY:
                updateNotification();
				//Chenyee <CY_Bug> <xuyongji> <20180208> remove for CSW1705A-1639 begin
                //stopOtaUpgradeService();
				//Chenyee <CY_Bug> <xuyongji> <20180208> remove for CSW1705A-1639 end
                break;
            default:
                mNotificationManager.cancel(Constants.GN_SU_DOWNLOAD_NOTIFICATION_ID);
                break;
        }
    }

    @Override
    public void onError(IContextState state, int error) {

    }

    private void startOtaUpgradeService() {
        Intent otaService = new Intent(Intent.ACTION_MAIN);
        ComponentName componentName = new ComponentName("com.cydroid.ota",
            "com.cydroid.ota.logic.OtaUpgradeService");
        otaService.setComponent(componentName);
        mContext.startService(otaService);
    }

    private void stopOtaUpgradeService() {
        Intent otaService = new Intent(Intent.ACTION_MAIN);
        ComponentName componentName = new ComponentName("com.cydroid.ota",
            "com.cydroid.ota.logic.OtaUpgradeService");
        otaService.setComponent(componentName);
        mContext.stopService(otaService);
    }

    private void updateNotification() {
        
        int progress = getDownloadProgress();
        State state = mSystemUpdate.getContextState().state();
        if (progress != mNotifyProgress || mNotifyState != state) {
            mNotifyProgress = progress;
            mNotifyState = state;
            Notification.Builder builder = getBaseNotifyBuilder();
            builder.setContentText(mNotifyContens.get(state));
            builder.setProgress(100, mNotifyProgress, false);
			//Chenyee <CY_Bug> <xuyongji> <20180109> modify for CSW1702A-813 begin
            mNotificationManager.createNotificationChannel(new NotificationChannel(
                    SystemPropertiesUtils.NOTIFICATION_CHANNEL_ID_OTA,
                    SystemPropertiesUtils.NOTIFICATION_CHANNEL_NAME_OTA,
                    NotificationManager.IMPORTANCE_LOW
            ));
			//Chenyee <CY_Bug> <xuyongji> <20180109> modify for CSW1702A-813 end
            mNotificationManager.notify(Constants.GN_SU_DOWNLOAD_NOTIFICATION_ID, builder.build());
        }
    }

    private Notification.Builder getBaseNotifyBuilder() {
        if (mDownloadNotificationBaseBuilder == null) {
			//Chenyee <CY_Bug> <xuyongji> <20180109> modify for CSW1702A-813 begin
            mDownloadNotificationBaseBuilder = new Notification.Builder(mContext, SystemPropertiesUtils.NOTIFICATION_CHANNEL_ID_OTA);
			//Chenyee <CY_Bug> <xuyongji> <20180109> modify for CSW1702A-813 end
            mDownloadNotificationBaseBuilder.setOngoing(true);
            mDownloadNotificationBaseBuilder.setSmallIcon(
                R.drawable.gn_su_notification_small_icon);
            mDownloadNotificationBaseBuilder.setColor(
                mContext.getResources()
                    .getColor(R.color.gn_su_notification_back_color));
            Intent intent = mContext.getPackageManager().getLaunchIntentForPackage(mContext.getPackageName());
			//Chenyee <CY_Bug> <xuyongji> <20180224> modify for CSW1702A-3019 begin
            if (intent == null) {
                intent = new Intent(mContext, SystemUpdateAnimActivity.class);
            }
			//Chenyee <CY_Bug> <xuyongji> <20180224> modify for CSW1702A-3019 end
            PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
            mDownloadNotificationBaseBuilder.setContentIntent(pendingIntent);
            String contentTitle = mContext.getString(R.string.gn_su_notify_auto_update_title);
            mDownloadNotificationBaseBuilder.setContentTitle(contentTitle);
        }
        return mDownloadNotificationBaseBuilder;
    }

    private void registerDownloadBroadcastReceiver() {
        Log.d(TAG, "registerDownloadBroadcastReceiver isRegister = " + isRegister);
        if (!isRegister) {
            IntentFilter netIntentFilter = new IntentFilter();
            netIntentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            mContext.registerReceiver(mNetworkBroadcastReceiver, netIntentFilter);
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(Intent.ACTION_MEDIA_EJECT);
            intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
            intentFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
            intentFilter.addAction(Intent.ACTION_MEDIA_SHARED);
            intentFilter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);
            intentFilter.addDataScheme("file");
            mContext.registerReceiver(mStorageBroadcastReceiver, intentFilter);

            isRegister = true;
        }
    }

    private void unRegisterDownloadBroadcastReceiver() {
        Log.d(TAG, "unRegisterDownloadBroadcastReceiver isRegister = " + isRegister);
        if (isRegister) {
            mContext.unregisterReceiver(mStorageBroadcastReceiver);
            mContext.unregisterReceiver(mNetworkBroadcastReceiver);
            isRegister = false;
        }
    }

    private ISyncCallback mDownloadJobCallback = new ISyncCallback() {
        @Override
        public void onResult(Object... objects) {
            if (mCallback != null) {
                mCallback.onResult(objects);
            }
            updateNotification();
        }

        @Override
        public void onError(int errorCode) {
            if (errorCode == Error.ERROR_CODE_DOWNLOAD_NETWORK_INTERRUPT) {
                mInterruptReason = INTERRUPT_REASON_BY_NET;
            }
            if (errorCode == Error.ERROR_CODE_DOWNLOAD_STORAGE_INTERRUPT) {
                mInterruptReason = INTERRUPT_REASON_BY_STORAGE;
            }
            if (mCallback != null) {
                mCallback.onError(errorCode);
            }
        }
    };

    private class NetworkBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "onReceive==>" + action);
            if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                Log.d(TAG, "network connect change!");

                if (!NetworkUtils.isNetworkAvailable(mContext)) {
                    return;
                }
                IContextState state = mSystemUpdate.getContextState();
                IStorage wlanAutoStorage = SettingUpdateDataInvoker
                    .getInstance(context).wlanAutoStorage();
				//Chenyee <CY_Req> <xuyongji> <20180822> modify for SW17W16SE-128 begin	
                boolean isWifiCanUse = wlanAutoStorage
                    .getBoolean(Key.WlanAuto.KEY_WLAN_AUTO_UPGRADE_SWITCH, mContext.getResources().getBoolean(
                            R.bool.auto_download_only_wlan));
				//Chenyee <CY_Req> <xuyongji> <20180822> modify for SW17W16SE-128 end			

                if (state.state() == State.DOWNLOAD_INTERRUPT) {
                    if (NetworkUtils.isWIFIConnection(mContext) && isWifiCanUse) {
                        restart();
                    }
                    if (NetworkUtils.isMobileNetwork(mContext) && !isBackgroundProcess(mContext)) {
                        mCallback.onError(Error.ERROR_CODE_RESUME_DOWNLOAD_BY_MOBILENET);
                    }
                }

            }
        }
    }

    //    private boolean isBackgroundProcess(Context context) {
//        IStorage settingStorage = SettingUpdateDataInvoker.getInstance(context).settingStorage();
//        int lastPidFromUser = settingStorage.getInt(Key.Setting.KEY_SETTING_UPDATE_CURRENT_PID_FROM_USER, -1);
//        if (lastPidFromUser != android.os.Process.myPid()) {
//            return true;
//        }
//        return false;
//    }
    //Gionee zhouhuiquan 2017-03-14 add for 73281 begin
    /**
     * 程序是否在前台运行
     * @return
     */
    private boolean isBackgroundProcess(Context context) {
        // Returns a list of application processes that are running on the device
        ActivityManager activityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        String packageName = context.getPackageName();

        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager
                .getRunningAppProcesses();
        if (appProcesses == null){
            return false;
        }
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            // The name of the process that this object is associated with.
            if (appProcess.processName.equals(packageName)
                    && appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                //Log.d(TAG,"程序处于前台");
                return false;
            }
        }
        //Log.d(TAG,"程序处于后台");
        return true;
    }
    //Gionee zhouhuiquan 2017-03-14 add for 73281 end


    private class StorageBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            IContextState state = mSystemUpdate.getContextState();

            if (action.equals(Intent.ACTION_MEDIA_MOUNTED)) {
                if (!canContinueWhenReceiveMountedBroadcast(mContext)) {
                    return;
                }

                if (state.state() == State.DOWNLOAD_INTERRUPT) {
                    if (NetworkUtils.isWIFIConnection(context)) {
                        restart();
                    }
                    if (NetworkUtils.isMobileNetwork(context) && !isBackgroundProcess(mContext)) {
                        mCallback.onError(Error.ERROR_CODE_RESUME_DOWNLOAD_BY_MOBILENET);
                    }
                }
            }
        }


        private boolean canContinueWhenReceiveMountedBroadcast(Context context) {
            if (!NetworkUtils.isNetCanUse(context)) {
                return false;
            }
            IStorage storage = SettingUpdateDataInvoker.getInstance(mContext).settingStorage();
            String fileName = storage.getString(Key.Setting.KEY_SETTING_DOWNLOAD_FILE_NAME, "");

            return FileUtils.isMemoryEnoughForDownload(fileName, getUpgradeInfo().getFileSize());
        }
    }
}
