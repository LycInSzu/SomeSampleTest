package com.cydroid.ota.ui;

import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;

import com.cydroid.ota.CyeePrt;
import com.cydroid.ota.Log;
import com.cydroid.ota.SettingUpdateApplication;
import com.cydroid.ota.bean.LocalFileInfo;
import com.cydroid.ota.logic.IExcutorCallback;
import com.cydroid.ota.logic.ILocalUpdateExecutor;
import com.cydroid.ota.logic.SystemUpdateFactory;
import com.cydroid.ota.logic.utils.VerifyZipForUpgrade;
import com.cydroid.ota.utils.BatteryUtil;
import com.cydroid.ota.utils.Constants;
import com.cydroid.ota.utils.FileUtils;
import com.cydroid.ota.utils.StorageUtils;
import com.cydroid.ota.utils.SystemPropertiesUtils;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import cyee.app.CyeeActionBar;
import cyee.widget.CyeeListView;
import cyee.widget.CyeeProgressBar;
import cyee.widget.CyeeTextView;
import com.cydroid.ota.R;

/**
 * Created by liuyanfeng on 15-6-8.
 */
public class LocalUpgradeActivity extends AbsActivity {
    private static final String TAG = "LocalUpgradeActivity";
    private static final int VERIFY_RESULT_SHOW_DELAY_MILLIS = 1000;
    private static final int MSG_UI_SHOW_VERIFY_DIALOG = 0x1002;
    private static final int MSG_UI_VERIFY_END = 0x1003;
    private static final int MSG_UI_LOADING_FILES_START = 0x1004;
    private static final int MSG_UI_LOADING_FILES_END = 0x1005;
    private static final int MSG_PROC_GET_FILES = 0x2001;
    private static final String ROOT_PATH = "root_path";
    private static final String SEPARATOR = File.separator;
    private CyeeProgressBar mCyeeProgressBar;
    private CyeeTextView mTipsView;
    private CyeeListView mCyeeListView;
    private CyeeTextView mTitleView;
    private CyeeTextView mEmptyView;

    public boolean isAutoScanner() {
        return isAutoScanner;
    }

    private boolean isAutoScanner;
    private ILocalUpdateExecutor mLocalUpdateExecutor;
    private LocalFilesAdapter mFilesAdapter;
    private String mUpgradeLocalFilePath = "";
    protected DialogFragment mCurrentDialogFragment = null;
    private Handler mUIHandler;
    private HandlerThread mProcHandleThread;
    private Handler mProcHandler;
    private String mCurrentPath;
    private Context mContext;
    private CyeeActionBar mActionBar;
    private StorageMountReceiver mStorageMountRecevier;
    private boolean isRegister = false;

    @Override
    protected void onResume() {
        super.onResume();
        if (!isAutoScanner) {
            registerStorageMountReceiver();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SettingUpdateApplication application = (SettingUpdateApplication) getApplication();
        if (!application.getSystemTheme().isNeedChangeColor()) {
            setTheme(R.style.Theme_Light_NormalTheme);
            if (SystemPropertiesUtils.getBlueStyle()) {
                setTheme(R.style.Theme_Light_BlueTheme);
            }
        }
        super.onCreate(savedInstanceState);
        mContext = this;
        parseIntent();
        initActionbar();
        setContentView(R.layout.gn_su_layout_local_upgrade);
        initViews();
        initDatas();

    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterStorageMountReceiver();
    }

    @Override
    public void onBackPressed() {
        backPressed();
    }

    public void systemRestartIm() {
        if (mLocalUpdateExecutor != null) {
            mLocalUpdateExecutor.install(mUpgradeLocalFilePath);
        }
    }

    public String getCurrentUpgradeFileName() {
        Log.d(TAG, "mUpgradeLocalFilePath:" + mUpgradeLocalFilePath);
        String[] paths = mUpgradeLocalFilePath.split(SEPARATOR);
        return paths[paths.length - 1];
    }

    private void parseIntent() {
        Intent intent = getIntent();
        isAutoScanner = intent.getBooleanExtra(Constants.AUTO_SCANNER, false);
        Log.d(TAG, "isAutoScanner=" + isAutoScanner);
    }

    private void initActionbar() {
        CyeeActionBar actionBar = getCyeeActionBar();
        if (actionBar != null) {
            mActionBar = actionBar;
            mActionBar.setDisplayOptions(CyeeActionBar.DISPLAY_HOME_AS_UP
                | CyeeActionBar.DISPLAY_SHOW_TITLE
                | CyeeActionBar.DISPLAY_SHOW_HOME);
            if (isAutoScanner) {
                mActionBar.setTitle(getString(
                    R.string.gn_su_string_settings_local_upgrade_auto));
            } else {
                mActionBar.setTitle(getString(
                    R.string.gn_su_string_settings_local_upgrade_select));
            }
            mActionBar.setOnBackClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    backPressed();
                }
            });
        }
    }

    private <T extends View> T getView(int id) {
        return (T) findViewById(id);
    }


    private void initViews() {
        mCyeeProgressBar = getView(R.id.gn_su_layout_localUpgrade_progressBar);
        mCyeeListView = getView(R.id.gn_su_layout_localUpgrade_lv);
        mTipsView = getView(R.id.gn_su_layout_localUpgrade_tips);
        mEmptyView = getView(R.id.gn_su_layout_localUpgrade_empty);
        mTitleView = getView(R.id.gn_su_layout_localUpgrade_title);
    }

    private void updateViewLoadingState(boolean isLoading, String loadingMessage) {
        if (isLoading) {
            if (!TextUtils.isEmpty(loadingMessage)) {
                mTipsView.setText(loadingMessage);
            }
            mCyeeListView.setVisibility(View.GONE);
            mCyeeProgressBar.setVisibility(View.VISIBLE);
            mTipsView.setVisibility(View.VISIBLE);
        } else {
            mCyeeListView.setVisibility(View.VISIBLE);
            mCyeeProgressBar.setVisibility(View.GONE);
            mTipsView.setVisibility(View.GONE);
        }
        mEmptyView.setVisibility(View.GONE);
    }

    private void updateFileInfoViews(List<LocalFileInfo> fileVos) {
        if (fileVos == null || fileVos.isEmpty()) {
            mCyeeListView.setVisibility(View.GONE);
            mCyeeProgressBar.setVisibility(View.INVISIBLE);
            mEmptyView.setVisibility(View.VISIBLE);
            mEmptyView.setText(R.string.gn_su_string_localUpgrade_forder_empty);
        } else {
            mCyeeListView.setVisibility(View.VISIBLE);
            mEmptyView.setVisibility(View.GONE);
        }
    }

    private void updateResultList(List<String> result) {
        if ((result == null || result.isEmpty()) && isAutoScanner) {
            mCyeeListView.setVisibility(View.GONE);
            mCyeeProgressBar.setVisibility(View.INVISIBLE);
            mTipsView.setVisibility(View.GONE);
            mEmptyView.setVisibility(View.VISIBLE);
            mEmptyView.setText(R.string.gn_su_string_localUpgrade_no_files);
            return;
        }
        List<LocalFileInfo> fileVos = initFileVos(result);
        if (mFilesAdapter == null) {
            mFilesAdapter = new LocalFilesAdapter(mContext, fileVos);
            mCyeeListView.setAdapter(mFilesAdapter);
            mCyeeListView.setOnItemClickListener(mItemClickListener);
        } else {
            updateFileInfoViews(fileVos);
            mFilesAdapter.updateDatas(fileVos);
        }

        if (!TextUtils.isEmpty(mCurrentPath)) {
            if (StorageUtils.getAllMountedStorageVolumesPath(mContext).size() == 1) {
                mActionBar.setTitle(R.string.gn_su_string_settings_local_upgrade_select);
                return;
            }
            boolean isSDcardInserted = StorageUtils.isExSdcardInserted(mContext);
            if (StorageUtils.getInternalStoragePath(mContext).equals(mCurrentPath)) {
                mActionBar.setTitle(
                    getString(R.string.gn_su_string_internal_storage));
            } else if (isSDcardInserted && StorageUtils.getSDCARDPATH().equals(mCurrentPath)) {
                mActionBar.setTitle(
                    getString(R.string.gn_su_string_sdcard));
            } else if (ROOT_PATH.equals(mCurrentPath)) {
                mActionBar.setTitle(R.string.gn_su_string_settings_local_upgrade_select);
            }
        }
    }

    private List<LocalFileInfo> initFileVos(List<String> result) {
        File file = null;
        List<LocalFileInfo> localFileVos = new ArrayList<LocalFileInfo>();
        boolean isSDcardInserted = StorageUtils.isExSdcardInserted(mContext);
        for (String filePath : result) {
            LocalFileInfo fileVo = new LocalFileInfo();
            fileVo.setFilePath(filePath);
            file = new File(filePath);
            if (file.isDirectory()) {
                fileVo.setIsFolder(true);
                fileVo.setFileSize("");
            } else {
                fileVo.setIsFolder(false);
                long fileSize = file.length();
                fileVo.setFileSize(String.format(getString(
                    R.string.gn_su_string_file_size), fileSize / 1024 / 1024));
            }
            Log.d(TAG, " filepath :" + filePath);
            filePath = filePath +"/";
            Log.d(TAG, "initFileVos inner :" + StorageUtils.getInternalStoragePath(mContext)
                    + " isSDcardInserted :" + isSDcardInserted
                    + "sdcard : " + StorageUtils.getSDCARDPATH());
            if (StorageUtils.getInternalStoragePath(mContext).equals(filePath)) {
                fileVo.setFileName(mContext.getString(R.string.gn_su_string_internal_storage));
            } else if (isSDcardInserted && StorageUtils.getSDCARDPATH().equals(filePath)) {
                fileVo.setFileName(mContext.getString(R.string.gn_su_string_sdcard));
            } else if (StorageUtils.isOTGStorage(mContext, filePath) && StorageUtils.getOTGStorage().equals(filePath)) {
                fileVo.setFileName(mContext.getString(R.string.OTG_name));
            }else {
                String[] paths = filePath.split(SEPARATOR);
                fileVo.setFileName(paths[paths.length - 1]);
               // fileVo.setFileName(mContext.getString(R.string.OTG_name));
            }
            localFileVos.add(fileVo);
        }
        return localFileVos;
    }

    private void initDatas() {
        mUIHandler = new MyHandle(this);
        mProcHandleThread = new HandlerThread(LocalUpgradeActivity.class.getName());
        mProcHandleThread.start();
        mProcHandler = new Handler(mProcHandleThread.getLooper(),
            mProcHandlerCallback);
        mLocalUpdateExecutor = SystemUpdateFactory.
            localUpdate(mContext, mLocalAutoScannerCallback);
        if (isAutoScanner) {
            updateViewLoadingState(true,
                getString(R.string.gn_su_string_localUpgrade_loading));
            mLocalUpdateExecutor.scanAvailable();
        } else {
            mTitleView.setVisibility(View.GONE);
            initCurrentPath();
        }
    }

    private void initCurrentPath() {
        List<String> rootPaths = StorageUtils.getAllMountedStorageVolumesPath(mContext);
        Log.d(TAG, "rootPaths:" + rootPaths.size());
        if (rootPaths.size() == 1) {
            mCurrentPath = rootPaths.get(0);
            mProcHandler.sendEmptyMessage(MSG_PROC_GET_FILES);
        } else {
            mCurrentPath = ROOT_PATH;
            Message fileMessage = Message.obtain(mUIHandler, MSG_UI_LOADING_FILES_END);
            fileMessage.obj = rootPaths;
            mUIHandler.sendMessage(fileMessage);
        }
    }

    private boolean isRootPath(String filePath) {
        if (ROOT_PATH.equals(filePath)) {
            return true;
        }
        List<String> rootPaths = StorageUtils.getAllMountedStorageVolumesPath(mContext);
        if (rootPaths.size() == 1) {
            if (rootPaths.get(0).equals(filePath)) {
                return true;
            }
        }
        return false;
    }

    private void dismissFragementDialog() {
        synchronized (DialogFactory.class) {
            if (mCurrentDialogFragment != null) {
                mCurrentDialogFragment.dismissAllowingStateLoss();
            }
        }
    }

    private void dialogShow(int id) {
        if (mCurrentDialogFragment != null) {
            if (!isDestroyed()) {
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.add(mCurrentDialogFragment, "dailog_" + id);
                ft.commitAllowingStateLoss();
            } else {
                Log.e(TAG, "show dialog id = " + id + " activity destroyed");
            }
        } else {
            Log.e(TAG, "CurrentDialogFragment is null!!!");
        }
    }

    private void doShowDialog(int dialogID) {
        synchronized (DialogFactory.class) {
            dismissFragementDialog();
            mCurrentDialogFragment = DialogFactory.newInstance(dialogID);
            dialogShow(dialogID);
        }
    }

    private AdapterView.OnItemClickListener mItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view,
                                int index, long id) {
            String filePath = mFilesAdapter.getItem(index).getFilePath();
            Log.d(TAG, "i:" + index + ":filePath=" + filePath);
            File file = new File(filePath);
            if (file.isDirectory()) {
                mCurrentPath = filePath;
                mProcHandler.sendEmptyMessage(MSG_PROC_GET_FILES);
            }
            if (file.isFile() && isAutoScanner) {
                mUpgradeLocalFilePath = filePath;
                doUpgradeFileVerifyEnd(true);
                return;
            }

            if (file.isFile()) {
                mUpgradeLocalFilePath = filePath;
                mUIHandler.sendEmptyMessage(MSG_UI_SHOW_VERIFY_DIALOG);
                boolean isVerify = VerifyZipForUpgrade.verifyZipForUpgrade(
                    file.getAbsolutePath(),
                    CyeePrt.isSystemRoot());
                Message message = Message.obtain(mUIHandler, MSG_UI_VERIFY_END);
                message.obj = isVerify;
                mUIHandler.sendMessageDelayed(message,
                    VERIFY_RESULT_SHOW_DELAY_MILLIS);

            }
        }
    };

    private void backPressed() {
        Log.d(TAG, "title click: mCurrentPath=" + mCurrentPath + ":"
            + isRootPath(mCurrentPath));
        if (TextUtils.isEmpty(mCurrentPath)) {
            super.onBackPressed();
            return;
        }
        if (isRootPath(mCurrentPath)) {
            super.onBackPressed();
            return;
        }
        if (!FileUtils.isMountedPath(mCurrentPath, mContext)) {
            initCurrentPath();
            return;
        }
        List<String> mountedStorage = StorageUtils
            .getAllMountedStorageVolumesPath(
                mContext);

        mCurrentPath = mountedStorage.contains(mCurrentPath) ? ROOT_PATH :
            mCurrentPath.substring(0, mCurrentPath.lastIndexOf(SEPARATOR));

        if (isRootPath(mCurrentPath) && (mountedStorage.size() > 1)) {
            updateResultList(mountedStorage);
        } else {
            updateResultList(FileUtils.getFileListPaths(mCurrentPath));
        }
    }

    private void doUpgradeFileVerifyEnd(boolean isVerify) {
        int batteryLevel = BatteryUtil.getBatteryLevel();
        if (isVerify) {
// Gionee zhouhuiquan 2017-04-01 add for 101983 begin
            if (batteryLevel < Constants.MINI_CHARGE) {    //电量小于20%
                doShowDialog(R.id.DIALOG_ID_POWER_TOO_LOW);
            }else if (batteryLevel < Constants.LOWER_CHARGE){  //电量大于20%,小于40%
                if (BatteryUtil.isCharging()){
                    doShowDialog(R.id.DIALOG_ID_LOCAL_RESTART_UPGRADE);
                }else {
                    doShowDialog(R.id.DIALOG_ID_POWER_LOW);
                }
            }else {   //电量大于40%
                doShowDialog(R.id.DIALOG_ID_LOCAL_RESTART_UPGRADE);
            }
// Gionee zhouhuiquan 2017-04-01 add for 101983 end
        } else {
            doShowDialog(R.id.DIALOG_ID_FILE_MD5_CHECK_ERR);
        }
    }

    private Handler.Callback mProcHandlerCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            switch (message.what) {
                case MSG_PROC_GET_FILES:
                    mUIHandler.sendEmptyMessage(MSG_UI_LOADING_FILES_START);
                    List<String> filePaths = FileUtils.getFileListPaths(mCurrentPath);
                    Message fileMessage = Message.obtain(mUIHandler, MSG_UI_LOADING_FILES_END);
                    fileMessage.obj = filePaths;
                    mUIHandler.sendMessage(fileMessage);
                    break;
                default:
                    break;
            }
            return false;
        }
    };

    private class MyHandle extends Handler {
        private WeakReference<LocalUpgradeActivity> reference;

        MyHandle(LocalUpgradeActivity activity) {
            reference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message message) {
            LocalUpgradeActivity activity = reference.get();
            if (activity == null) {
                return;
            }
            switch (message.what) {
                case MSG_UI_SHOW_VERIFY_DIALOG:
                    activity.doShowDialog(R.id.DIALOG_ID_LOCAL_FILE_VERIFY);
                    break;
                case MSG_UI_VERIFY_END:
                    boolean isVerify = (Boolean) message.obj;
                    activity.dismissFragementDialog();
                    activity.doUpgradeFileVerifyEnd(isVerify);
                    break;
                case MSG_UI_LOADING_FILES_START:
                    activity.updateViewLoadingState(true, getString(
                        R.string.gn_su_string_loading_files));
                    break;
                case MSG_UI_LOADING_FILES_END:
                    Log.d(TAG, "message.obj=" + message.obj);
                    List<String> filePaths = (List<String>) message.obj;
                    activity.updateViewLoadingState(false, null);
                    activity.updateResultList(filePaths);
                    break;
                default:
                    break;
            }
        }
    };

    private IExcutorCallback mLocalAutoScannerCallback = new IExcutorCallback() {
        @Override
        public void onResult(Object... objects) {
            Log.d(TAG, "result" + objects[0]);
            mTitleView.setVisibility(View.GONE);
            updateViewLoadingState(false, null);
            updateResultList((ArrayList<String>) objects[0]);
        }

        @Override
        public void onError(int errorCode) {
            updateViewLoadingState(false, null);
            updateResultList(null);
        }
    };

    private void registerStorageMountReceiver() {
        if (!isRegister) {
            mStorageMountRecevier = new StorageMountReceiver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(Intent.ACTION_MEDIA_EJECT);
            intentFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
            intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
            intentFilter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);
            intentFilter.addDataScheme("file");
            mContext.registerReceiver(mStorageMountRecevier, intentFilter);
            isRegister = true;
        }
    }

    private void unregisterStorageMountReceiver() {
        if (isRegister) {
            mContext.unregisterReceiver(mStorageMountRecevier);
            mStorageMountRecevier =null;
            isRegister = false;
        }
    }

    private class StorageMountReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "action:" + action);
            if (action.equals(Intent.ACTION_MEDIA_MOUNTED)) {
                List<String> mountedStorage = StorageUtils
                    .getAllMountedStorageVolumesPath(
                        mContext);
                if (isRootPath(mCurrentPath) && mountedStorage.size() > 1) {
                    updateResultList(mountedStorage);
                }
            } else {
                if (TextUtils.isEmpty(mCurrentPath) || !FileUtils.isMountedPath(mCurrentPath, mContext)) {
                    initCurrentPath();
                }
            }
        }
    }
}
