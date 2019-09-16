package com.cydroid.ota.ui;

import android.Manifest;
import android.app.ActionBar;
import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.cydroid.ota.Log;
import com.cydroid.ota.bean.IUpdateInfo;
import com.cydroid.ota.logic.AutoUpgradeManager;
import com.cydroid.ota.logic.ICheckExecutor;
import com.cydroid.ota.logic.IContextState;
import com.cydroid.ota.logic.IDownloadCallback;
import com.cydroid.ota.logic.IDownloadExecutor;
import com.cydroid.ota.logic.IObserver;
import com.cydroid.ota.logic.ISystemUpdate;
import com.cydroid.ota.logic.QuestionnaireManager;
import com.cydroid.ota.logic.RuntimePermissionsManager;
import com.cydroid.ota.logic.State;
import com.cydroid.ota.logic.SystemUpdateFactory;
import com.cydroid.ota.logic.bean.CheckType;
import com.cydroid.ota.logic.bean.IQuestionnaireInfo;
import com.cydroid.ota.logic.config.EnvConfig;
import com.cydroid.ota.storage.IStorage;
import com.cydroid.ota.storage.Key;
import com.cydroid.ota.storage.SettingUpdateDataInvoker;
import com.cydroid.ota.ui.widget.AnimatorLogoView;
import com.cydroid.ota.ui.widget.AnimatorMainBgView;
import com.cydroid.ota.ui.widget.AnimatorMiddleView;
import com.cydroid.ota.ui.widget.AnimatorStateView;
import com.cydroid.ota.ui.widget.ExpendTextView;
import com.cydroid.ota.utils.BatteryUtil;
import com.cydroid.ota.utils.BitmapUtils;
import com.cydroid.ota.utils.Constants;
import com.cydroid.ota.utils.Error;
import com.cydroid.ota.utils.NetworkUtils;
import com.cydroid.ota.utils.SystemPropertiesUtils;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import cyee.changecolors.ChameleonColorManager;
import com.cydroid.ota.R;
//Chenyee <CY_Bug> <xuyongji> <20181011> modify for CSW1702A-2460 begin
import android.graphics.Color;
//Chenyee <CY_Bug> <xuyongji> <20181011> modify for CSW1702A-2460 end
//Chenyee <CY_Req> <xuyongji> <20180417> modify for CSW1702SE-197 begin
import com.cydroid.ota.SettingUpdateApplication;
import com.cydroid.ota.utils.StatisticsHelper;
//Chenyee <CY_Req> <xuyongji> <20180417> modify for CSW1702SE-197 end

/**
 * Created by kangjj on 15-6-8.
 */
public class SystemUpdateAnimActivity extends Activity {
    public static final String TAG = "TAGSystemUpdateAnimActivity";
    private static final boolean TRACEDEBUG = EnvConfig.isTestTrace();
    private static final long UPDATE_DOWNLOAD_SPEED_TIME = 4000;
    private static final double BYTES_PER_MIB = 1024 * 1024;
    private static final double BYTES_PRE_KB = 1024;
    private static final int VIBRATE_LONG = 100;
    private static final int DOWNLOAD_COMPLETE_DELAY_MILLIS = 2000;
    private static final int DOWNLOAD_PAUSING_DELAY_MILLIS = 1000;
    private IContextState mContextState;
    private Resources mResources;
    private AnimatorMainBgView mAnimatorMainBgView;
    private ExpendTextView mExpendTextView;
    private AnimatorStateView mStateView;
    private AnimatorLogoView mAnimatorLogoView;
    private AnimatorMiddleView mMiddleMsgView;
    private TextView mNewVersionView;
    private TextView mFileSizeView;
    private TextView mReadyNoteView;
    private TextView mIntroductionView;
    private TextView mCurVersionView;
    private TextView mGotoIntroductionView;
    private ISystemUpdate mSystemUpdate;
    private ICheckExecutor mCheckExecutor;
    private BlockPool<IContextState> mBlockStatePool;
    private SoundPool mSoundPool = null;
    private IStorage mSettingStorage;
    private boolean isResumeFromOnCreate = false;
    private DialogFragment mCurrentDialogFragment;
    private int mCurrentDialogId = -1;
    private static boolean mHavePermissions = false;
    private  IContextState mState;
    protected Myhandler mHandler = new Myhandler(SystemUpdateAnimActivity.this);
    private Context mContext;
    private static final int REQUIRED_PERMISSIONS_REQUEST_CODE = 2000;
	//Chenyee <CY_Bug> <xuyongji> <20180104> modify for  CSW1705A-295 begin
    private static final int SYSTEM_UI_FLAG_NAVIGATION_BAR_COLOR = 0x00000020;
	//Chenyee <CY_Bug> <xuyongji> <20180104> modify for  CSW1705A-295 end
	//Chenyee <CY_Req> <xuyongji> <20180417> modify for CSW1702SE-197 begin
    private static final long sCheckNewDuration = 2 * 60 * 1000;
    private long mCheckNewTime = 0;
	//Chenyee <CY_Req> <xuyongji> <20180417> modify for CSW1702SE-197 end

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (TRACEDEBUG) Debug.startMethodTracing("otatrace");
        setTheme(R.style.Theme_Light_TranslucentActionBar);
        super.onCreate(savedInstanceState);
        ChameleonColorManager.getInstance().onCreate(this);
        mContext = this;
		//Chenyee <CY_Bug> <xuyongji> <20180104> modify for  CSW1705A-295 begin
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | SYSTEM_UI_FLAG_NAVIGATION_BAR_COLOR);
		//Chenyee <CY_Bug> <xuyongji> <20180104> modify for  CSW1705A-295 end		
		//Chenyee <CY_Bug> <xuyongji> <20181011> modify for CSW1702A-2460 begin
        getWindow().setNavigationBarColor(Color.TRANSPARENT);
		//Chenyee <CY_Bug> <xuyongji> <20181011> modify for CSW1702A-2460 end
        mSettingStorage = SettingUpdateDataInvoker.getInstance(this).settingStorage();
        isResumeFromOnCreate = true;
        mResources = getResources();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.gn_su_layout_main);
        initActionBar();
        initView();
        init();
    }
    
    public void requestPermissions(){
        if (RuntimePermissionsManager.isBuildSysNeedRequiredPermissions()) {

            if (RuntimePermissionsManager.hasNeedRequiredPermissions(this)) {
                RuntimePermissionsManager.requestRequiredPermissions(this,
                        REQUIRED_PERMISSIONS_REQUEST_CODE);
            } else {
                autoCheckVersion();
				//Chenyee <CY__Req> <xuyongji> <20180703> modify for CSW1803A-749 begin
                writeManualCheckData();
				//Chenyee <CY__Req> <xuyongji> <20180703> modify for CSW1803A-749 end
            }
        }
    }
    
    @Override
    public void onRequestPermissionsResult( final int requestCode, final String permissions[], final int[] grantResults) {

        if (!isRequestPermissionsResult(requestCode)) {
            return;

        }

        if (RuntimePermissionsManager.hasDeniedPermissions(permissions, grantResults)) {

            Toast toast = Toast.makeText(SystemUpdateAnimActivity.this,
                    R.string.no_permission,
                    Toast.LENGTH_SHORT);
            toast.show();
            finish();

        }

    }

    private boolean isRequestPermissionsResult(int requestCode) {

        return REQUIRED_PERMISSIONS_REQUEST_CODE == requestCode;

    }

    private void init() {
        mBlockStatePool = new BlockPool<IContextState>();
        mBlockStatePool.registerOnConsumeState(mOnConsumeListener);
        mSystemUpdate = SystemUpdateFactory.systemUpdate(SystemUpdateAnimActivity.this);
        mSystemUpdate.registerObserver(mObserver);
        mSoundPool = new SoundPool(1, AudioManager.STREAM_SYSTEM, 0);
        mSoundPool.load(SystemUpdateAnimActivity.this, R.raw.new_version_ring, 1);
        SystemUpdateFactory.questionnaire(this).registerDataChange(mQuestionnaireDataChange);
    }


    @Override
    public Resources getResources() {
        Resources res = super.getResources();
        Configuration config=new Configuration();
        config.setToDefaults();
        res.updateConfiguration(config, res.getDisplayMetrics());
        return res;
    }

    private void playSound(int soundId, int number) {
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        float audioMaxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM);
        float audioCurrentVolume = audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);
        float volumnRatio = audioCurrentVolume / audioMaxVolume;
        mSoundPool.play(1, volumnRatio, volumnRatio, 1, number, 1);
    }

    private void initView() {
        mAnimatorMainBgView = getView(R.id.gn_su_layout_main_bgview);
        mCurVersionView = getView(R.id.gn_su_layout_main_versionview);
        mMiddleMsgView = getView(R.id.gn_su_layout_main_middlemsgview);
        mStateView = getView(R.id.gn_su_layout_main_statebutton);
        mStateView.setOnClickListener(mOnClickListener);
        mExpendTextView = getView(R.id.gn_su_layout_main_expend_text);
        mExpendTextView.setAnimation(true);
        mExpendTextView.setOnClickListener(mOnClickListener);
        mAnimatorLogoView = getView(R.id.gn_su_layout_main_animtorlogoview);
        mAnimatorLogoView.setOnRotaSingleRingListener(mOnRotaSingleRingListener);
        mNewVersionView = getView(R.id.gn_su_layout_ready_newversion);
        mFileSizeView = getView(R.id.gn_su_layout_ready_filesize);
        mReadyNoteView = getView(R.id.gn_su_layout_ready_noteview);
        mIntroductionView = getView(R.id.gn_su_layout_ready_introduction);
        mGotoIntroductionView = getView(R.id.gn_su_layout_ready_gotointroduction);
        mGotoIntroductionView.setOnClickListener(mOnClickListener);
    }
    
    private void  autoCheckVersion() {
        Log.d(TAG, " now state :" + mState.state());
        switch (mState.state()) {
        case INITIAL:
            if (needCheckVersionWhenResume()) {
               mHandler.postDelayed(mRunnable, 700);
            }
            break;
        default:
            break;
        }
    }

    public boolean verifyPermission(int[] grantResults) {
        return grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume()");
        mState = mSystemUpdate.getContextState();
        requestPermissions();

        invalidateOptionsMenu();
        if (TRACEDEBUG) Debug.stopMethodTracing();
        if (TRACEDEBUG) Debug.startMethodTracing("otaonresume");
    }

    @Override
    protected void onPause() {
        isResumeFromOnCreate = false;
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        MenuItem settingMenu = menu.findItem(R.id.menu_setting);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                final View v = findViewById(R.id.menu_setting);
                if (v != null) {
                    v.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            return true;
                        }
                    });
                }
            }
        });
        IQuestionnaireInfo questionnaireInfo = SystemUpdateFactory
                .questionnaire(this).getQuestionnaireInfo();
        Drawable drawable = getResources()
                .getDrawable(R.drawable.gn_ota_upgrade_settings_icon);
        boolean isRead = SettingUpdateDataInvoker.getInstance(this).settingStorage()
                .getBoolean(Key.Setting.KEY_SETTING_QUESTIONNAIRE_READ, false);
        Log.d(TAG, Log.getFunctionName() + ":" + isRead);
        if (questionnaireInfo != null && questionnaireInfo.getStatus() == 0
                && !isRead) {
            settingMenu.setIcon(BitmapUtils.getSettingsDrawable(drawable, this));
        } else {
            settingMenu.setIcon(drawable);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_setting:
                doSettings();
                break;
            default:
                break;
        }
        return true;
    }

    protected static int get_M_File(long size) {
        return (int) (size / (1024 * 1024));
    }

    private void doSettings() {
        Intent intent = new Intent();
        intent.setClass(SystemUpdateAnimActivity.this,
                OtaSettingsActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (TRACEDEBUG) Debug.stopMethodTracing();
    }

    //<wangpf> <2016-4-28> modify for CR01683723 begin
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.d(TAG, "onSaveInstanceState()");
    }
    //<wangpf> <2016-4-28> modify for CR01683723 end

    @Override
    protected void onDestroy() {
        Log.d(TAG, Log.getFunctionName());
        mBlockStatePool.onDestory();
        mSystemUpdate.unregisterObserver(mObserver);
        SystemUpdateFactory.questionnaire(this).unregisterDataChange(mQuestionnaireDataChange);
        mAnimatorMainBgView.onDestory();
        if (mSoundPool != null) {
            mSoundPool.unload(1);
            mSoundPool.release();
        }
        super.onDestroy();
        ChameleonColorManager.getInstance().onDestroy(this);
    }

    protected void updateContextState(IContextState s) {
        mContextState = s;
        mAnimatorMainBgView.changeState(mContextState);
        switch (mContextState.state()) {
            case INITIAL:
                mCheckExecutor = mSystemUpdate.checkUpdate();
                updateInitialView();
                break;
            case CHECKING:
                updateCheckingView();
                break;
            case READY_TO_DOWNLOAD:
                updateReadyDownloadView();
                playSound(1, 0);
                break;
            case DOWNLOADING:
                updateDownloadingView();
                break;
            case DOWNLOAD_INTERRUPT:
                updateDownloadInterruptView();
                break;
            case DOWNLOAD_PAUSE:
                updatePauseView();
                break;
            case DOWNLOAD_PAUSEING:
                updatePausingView();
                break;
            case DOWNLOAD_COMPLETE:
                updateDownloadCompleteView();
                break;
            case DOWNLOAD_VERIFY:
                updateDownloadVerifyView();
                break;
            default:
                break;
        }
    }

    private IStorage.OnKeyChangeListener mCheckListenerInMobile = new IStorage.OnKeyChangeListener() {
        @Override
        public void onKeyChange(String key) {
            Log.d(TAG, " check version ");
            if (key.equals(Key.Setting.KEY_MOBILE_NET_ENABLE)) {
                if (mSettingStorage.getBoolean(key, false)) {
                    strartCheck();
                }
                mSettingStorage.unregisterOnkeyChangeListener(this);
            }
        }
    };

    public void strartCheck() {
        if (mCheckExecutor == null) {
            return;
        }
        if(isFromPushNotify()){
            setCheckTypePushValue();
            mCheckExecutor.check(CheckType.CHECK_TYPE_PUSH);
        }else{
            mCheckExecutor.check(CheckType.CHECK_TYPE_DEFAULT);
        }
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(VIBRATE_LONG);
    }

    private void setCheckTypePushValue() {
        IStorage iStorage = SettingUpdateDataInvoker.getInstance(this).pushStorage();
        int push_id = iStorage.getInt(Key.Push.KEY_PUSH_RECEIVER_NOTIFIER_ID,0);
        CheckType.CHECK_TYPE_PUSH.setTypeValue(push_id);
    }

    private void docheck() {
        if (isCanUseNet()) {
            strartCheck();
        } else {
            mSettingStorage.registerOnkeyChangeListener(mCheckListenerInMobile);
        }
		//Chenyee <CY__Req> <xuyongji> <20180703> modify for CSW1803A-749 begin
        writeManualCheckData();
		//Chenyee <CY__Req> <xuyongji> <20180703> modify for CSW1803A-749 end

    }

    //Chenyee <CY__Req> <xuyongji> <20180703> modify for CSW1803A-749 begin
	private void writeManualCheckData() {
        long curTime = System.currentTimeMillis();
        if (curTime - mCheckNewTime > sCheckNewDuration && SettingUpdateApplication.sStatisticsHelper != null) {
            Log.d(TAG, "Statistics checkNew out of 2 mins");
            String curVersion = SystemPropertiesUtils.getInternalVersion();
            SettingUpdateApplication.sStatisticsHelper.writeStatisticsData(StatisticsHelper.EVENT_CHECK_NEW,
                    StatisticsHelper.KEY_CURRENT_VERSION, curVersion);
            mCheckNewTime = curTime;
        }
    }
	//Chenyee <CY__Req> <xuyongji> <20180703> modify for CSW1803A-749 end

    private void showNoNetworkError(int resId) {
        Toast.makeText(this, mResources.getString(resId), Toast.LENGTH_SHORT).show();
    }

    private void checkNetTypeAndDownload() {
        if (!NetworkUtils.isNetworkAvailable(this)) {
            showNoNetworkError(
                    R.string.gn_su_layout_main_expend_text_download_no_network_error);
            return;
        }
        if (NetworkUtils.isMobileNetwork(SystemUpdateAnimActivity.this)) {
            showFragmentDialog(R.id.DIALOG_ID_MOBILENET_HINT);
            return;
        }
        if (NetworkUtils.isWIFIConnection(SystemUpdateAnimActivity.this)) {
            requestDownload();
        }
    }

    public void requestDownload() {
        IDownloadExecutor downloadExecutor = getDownloadExecutor();
        if (mContextState.state() == State.READY_TO_DOWNLOAD) {
            downloadExecutor.start();
        } else {
            downloadExecutor.restart();
        }
        checkAndDisplayAutodownloadDialog();
    }


    private void updateInitialView() {
        Log.d(TAG, "updateInitialView() ");
        if (mContextState == null) {
            return;
        }
        String curText = getString(R.string.gn_su_string_curversion);
        mCurVersionView.setText(String.format(curText,
                SystemPropertiesUtils.getCurrentVersion()));
        mMiddleMsgView.setText("");
        Log.d(TAG, "updateInitialView()  mContextState.error() = " + mContextState.error());
        if (mContextState.error() == Error.ERROR_CODE_NETWORK_ERROR || mContextState.error() == Error.ERROR_CODE_PARSER_ERROR) {
            mExpendTextView.setExpendText(getString(
                    R.string.gn_su_layout_main_expend_text_network_error));
        } else if (mContextState.error() == 0) {
            if (mContextState.isRoot()) {
                mMiddleMsgView.setText(getString(R.string.gn_su_layout_no_recovery_root));
                mExpendTextView.setExpendText("");
            } else {
                mExpendTextView.setExpendText(getString(
                        R.string.gn_su_layout_main_expend_text_noversion));
            }
        } else {
            mExpendTextView.setExpendText("");
        }
    }

    private void updateCheckingView() {
        Log.d(TAG, "updateCheckingView()");
        mMiddleMsgView.setText("");
        mExpendTextView.setExpendText(getString(R.string.gn_su_layout_main_expend_text_checking));
    }

    private void updateReadyDownloadView() {
        IUpdateInfo info = mSystemUpdate.getSettingUpdateInfo();
        Log.d(TAG, "updateReadyDownloadView()");
        String newVersion = getString(R.string.gn_su_string_ready_newversion);
        if (info.isPreRelease() || EnvConfig.isTestPrelease()
                || mContextState.isRoot() || EnvConfig.isTestRoot() || info.isBackup() || EnvConfig.isTestBackup()) {
            if (mContextState.isRoot() || EnvConfig.isTestRoot()) {
                mReadyNoteView.setVisibility(View.VISIBLE);
                mReadyNoteView.setText(getString(
                        R.string.gn_su_layout_ready_noteview_root));
            } else if (info.isPreRelease() || EnvConfig.isTestPrelease()) {
                mReadyNoteView.setVisibility(View.VISIBLE);
                mReadyNoteView.setText(getString(
                        R.string.gn_su_layout_ready_noteview_prelease));
            } else if (info.isBackup() || EnvConfig.isTestBackup()) {
                mReadyNoteView.setVisibility(View.VISIBLE);
                mReadyNoteView.setText(getString(R.string.gn_su_layout_ready_noteview_backup));
            }
        } else {
            mReadyNoteView.setVisibility(View.GONE);
            Log.d(TAG, "mReadyNoteView = GONE NOT PreRelease OR NOT ROOT");
        }
        Log.d(TAG, "updateReadyDownloadView error = " + mContextState.error());
        switch (mContextState.error()) {
            case Error.ERROR_CODE_DOWNLOAD_NO_SPACE:
                mExpendTextView.setExpendText(getString(R.string.gn_su_layout_main_middlemdgview_download_error_no_space));
                break;
            case Error.ERROR_CODE_DOWNLOAD_FILE_VERIFY_FAILED:
            case Error.ERROR_CODE_DOWNLOAD_FILE_NOT_EXIST:
            case Error.ERROR_CODE_DOWNLOAD_FILE_WRITE_ERROR:
                mExpendTextView.setExpendText(getString(R.string.gn_su_string_download_file_error));
                break;
            default:
                mExpendTextView.setExpendText("");
                break;
        }
        //display new version
        mNewVersionView.setText(String.format(newVersion, info.getVersion()));
        mFileSizeView.setText(get_M_File(info.getFileSize()) + "MB");
		//Chenyee <CY_Req> <xuyongji> <20180822> modify for SW17W16SE-128 begin
        String simpleReleaseNote = info.getReleaseNote();
		//Chenyee <CY_Req> <xuyongji> <20180822> modify for SW17W16SE-128 end
        if (TextUtils.isEmpty(simpleReleaseNote)) {
            mIntroductionView.setText("");
        } else {
            mIntroductionView.setText(String.format(getString(R.string.gn_su_layout_ready_introduction_newversion), simpleReleaseNote));
        }
    }

    private void updateDownloadingView() {
        Log.d(TAG, "updateDownloadingView()");
        mMiddleMsgView.setAnimatorText(getSpeedString(
                mSystemUpdate.getSettingUpdateInfo().getFileSize(),
                getDownloadExecutor().getDownloadedFileSize(), 0));
        mExpendTextView.setExpendText(getString(R.string.gn_su_layout_main_expend_text_gotointroduction));
        mAnimatorLogoView.changeProgress(getDownloadExecutor().getDownloadProgress());
        if (mCurrentDialogId == R.id.DIALOG_ID_MOBILENET_HINT) {
            dismissFragementDialog();
        }
    }

    private void updateDownloadInterruptView() {
        Log.d(TAG, "updateDownloadInterruptView()");
        mExpendTextView.setExpendText(getString(R.string.gn_su_layout_main_expend_text_gotointroduction));
        mAnimatorLogoView.changeProgress(getDownloadExecutor().getDownloadProgress());
        Log.d(TAG, "mContextState.error :" + mContextState.error());
        switch (mContextState.error()) {
            case Error.ERROR_CODE_DOWNLOAD_NETWORK_INTERRUPT:
                if (NetworkUtils.isMobileNetwork(SystemUpdateAnimActivity.this)) {
					//Chenyee <CY_Req> <xuyongji> <20180930> modify for CSW1803DP-43 begin
                    mMiddleMsgView.setAnimatorText(getString(SystemPropertiesUtils.isDPFlag()
                            ? R.string.gn_su_layout_main_expend_text_network_error_inmobile_dp
                            : R.string.gn_su_layout_main_expend_text_network_error_inmobile));
					//Chenyee <CY_Req> <xuyongji> <20180930> modify for CSW1803DP-43 end		
                } else {
                    mMiddleMsgView.setAnimatorText(getString(R.string.gn_su_layout_main_middlemsgview_downloading_interrupt_by_net));
                }
                break;
            case Error.ERROR_CODE_DOWNLOAD_STORAGE_INTERRUPT:
                mMiddleMsgView.setAnimatorText(getString(R.string.gn_su_layout_main_middlemsgview_downloading_interrupt_by_storage));
                break;
            default:
                mMiddleMsgView.setAnimatorText("");
                break;
        }
    }

    private void updatePauseView() {
        Log.d(TAG, "updatePauseView()");
        mMiddleMsgView.setAnimatorText(String.format(getString(R.string.gn_su_layout_main_middlemsgview_pause),
                get_M_File(getDownloadExecutor().getDownloadedFileSize()),
                get_M_File(mSystemUpdate.getSettingUpdateInfo().getFileSize())));
        mExpendTextView.setExpendText(getString(R.string.gn_su_layout_main_expend_text_gotointroduction));
        mAnimatorLogoView.changeProgress(getDownloadExecutor().getDownloadProgress());
    }

    private void updatePausingView() {
        Log.d(TAG, "updatePausingView()");
        mMiddleMsgView.setAnimatorText(
                getString(R.string.gn_su_layout_main_middlemsgview_pausing));
        mExpendTextView.setExpendText(getString(R.string.gn_su_layout_main_expend_text_gotointroduction));
        mAnimatorLogoView.changeProgress(getDownloadExecutor().getDownloadProgress());
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mBlockStatePool != null) {
                    mBlockStatePool.wakeBlock();
                }
            }
        }, DOWNLOAD_PAUSING_DELAY_MILLIS);
    }

    private void updateDownloadCompleteView() {
        Log.d(TAG, "updateDownloadCompleteView()");
        mMiddleMsgView.setAnimatorText(getString(R.string.gn_su_layout_main_middlemsgview_downloadcomplete));
        mExpendTextView.setExpendText(getString(R.string.gn_su_layout_main_expend_text_gotointroduction));
        mAnimatorLogoView.changeProgress(getDownloadExecutor().getDownloadProgress());
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mBlockStatePool != null) {
                    mBlockStatePool.wakeBlock();
                }
            }
        }, DOWNLOAD_COMPLETE_DELAY_MILLIS);
    }

    private void updateDownloadVerifyView() {
        Log.d(TAG, "updateDownloadVerifyView()");
        mMiddleMsgView.setAnimatorText(getString(R.string.gn_su_layout_main_middlemsgview_verifysuccess));
        mExpendTextView.setExpendText(getString(
                R.string.gn_su_layout_main_expend_text_gotointroduction));
        mAnimatorLogoView.changeProgress(getDownloadExecutor().getDownloadProgress());
    }

    private boolean needCheckVersionWhenResume() {
        return NetworkUtils.isWIFIConnection(SystemUpdateAnimActivity.this)
                && isResumeFromOnCreate;
    }

    private IDownloadExecutor getDownloadExecutor() {
        return mSystemUpdate.downUpdate(mDownloadCallback);
    }

    private <T extends View> T getView(int id) {
        return (T) findViewById(id);
    }

    private void initActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);
    }

    private void checkAndDisplayAutodownloadDialog() {
        IStorage wlanAutoStorage = SettingUpdateDataInvoker.getInstance(SystemUpdateAnimActivity.this).wlanAutoStorage();
		//Chenyee <CY_Req> <xuyongji> <20180822> modify for SW17W16SE-128 begin
        boolean isOpen = wlanAutoStorage.getBoolean(Key.WlanAuto.KEY_WLAN_AUTO_UPGRADE_SWITCH, mContext.getResources().getBoolean(
                R.bool.auto_download_only_wlan));
		//Chenyee <CY_Req> <xuyongji> <20180822> modify for SW17W16SE-128 end		
        if (!isOpen) {
            showFragmentDialog(R.id.DIALOG_ID_AUTODOWNLOAD_HINT);
        }
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.gn_su_layout_main_statebutton:
                    onClickStateView();
                    break;
                case R.id.gn_su_layout_main_expend_text:
                    switch (mContextState.state()) {
                        case DOWNLOADING:
                        case DOWNLOAD_PAUSE:
                        case DOWNLOAD_PAUSEING:
                        case DOWNLOAD_INTERRUPT:
                        case DOWNLOAD_COMPLETE:
                        case DOWNLOAD_VERIFY:
                            gotoNewVersionDetailsInfo();
                            break;
                        default:
                            break;
                    }
                    break;
                case R.id.gn_su_layout_ready_gotointroduction:
                    gotoNewVersionDetailsInfo();
                    break;
                default:
                    break;
            }
        }

        private void gotoNewVersionDetailsInfo() {
            if (isCanUseNet()) {
                Intent detailIntent = new Intent();
                detailIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                detailIntent.setClass(SystemUpdateAnimActivity.this,
                        DetailsInfoActivity.class);
                detailIntent.putExtra(DetailsInfoActivity.IS_CURRENT_VERSION_INFO,
                        false);
                IUpdateInfo updateInfo = mSystemUpdate.getSettingUpdateInfo();
                detailIntent.putExtra(DetailsInfoActivity.MROE_INFO_URL, updateInfo.getReleaseNoteUrl());
                detailIntent.putExtra(DetailsInfoActivity.INTERNAL_VERSION, updateInfo.getInternalVer());
                detailIntent.putExtra(DetailsInfoActivity.RELEASE_NOTE_ID,updateInfo.getReleaseNotesId());
                startActivity(detailIntent);
            } else {
                mSettingStorage.registerOnkeyChangeListener(
                        introduceNewVersionListener);
            }
                //add by cuijiuyu for image releasenote
//                IUpdateInfo updateInfo = mSystemUpdate.getSettingUpdateInfo();
//            Intent intent = new Intent(SystemUpdateAnimActivity.this, ImageReleaseNoteActivity.class);
//            intent.putExtra(ImageReleaseNoteActivity.RELEASE_NOTE_ID,updateInfo.getReleaseNotesId());
//            startActivity(intent);
        }

        private void onClickStateView() {
            IContextState showState = mStateView.getShowState();
            if (showState == null) {
                Log.e(TAG, "onClickStateView() showState is null !!!");
                return;
            }
            Log.d(TAG, "onClickStateView() showState = " + showState);
            if (showState.state() != mSystemUpdate.getContextState().state()) {
                Log.e(TAG, "onClickStateView() showState = " + showState
                        + "  ContextState = " + mSystemUpdate.getContextState());
                return;
            }
            IDownloadExecutor downloadExecutor = getDownloadExecutor();
            switch (showState.state()) {
                case INITIAL:
                    docheck();
                    break;
                case READY_TO_DOWNLOAD:
                    checkNetTypeAndDownload();
                    break;
                case DOWNLOADING:
                    downloadExecutor.pause();
                    break;
                case DOWNLOAD_INTERRUPT:
                    downloadExecutor.pause();
                    break;
                case DOWNLOAD_PAUSE:
                    checkNetTypeAndDownload();
                    break;
                case DOWNLOAD_VERIFY:
                    displayRebootdialog();
                    break;
                default:
                    break;
            }
        }

        private IStorage.OnKeyChangeListener introduceNewVersionListener = new IStorage.OnKeyChangeListener() {
            @Override
            public void onKeyChange(String key) {
                if (Key.Setting.KEY_MOBILE_NET_ENABLE.equals(key)) {
                    if (mSettingStorage.getBoolean(key, false)) {
                        gotoNewVersionDetailsInfo();
                    }
                    mSettingStorage.unregisterOnkeyChangeListener(this);
                }
            }
        };
    };


    private boolean isCanUseNet() {
        if (NetworkUtils.isWIFIConnection(this)) {
            return true;
        }
        if (NetworkUtils.isMobileNetwork(this)) {
            boolean enable = mSettingStorage.getBoolean(Key.Setting.KEY_MOBILE_NET_ENABLE, false);
            if (enable) {
                return true;
            }
            Log.d(TAG, "can not use net ");
            showFragmentDialog(R.id.DIALOG_ID_MOBILE_PERMISSION);
            return false;
        }
        showNoNetworkError(
                R.string.gn_su_layout_main_expend_text_no_network_error);
        return false;
    }


    private void displayRebootdialog() {

        int batteryLevel = BatteryUtil.getBatteryLevel();
// Gionee zhouhuiquan 2017-04-01 add for 101983 begin
        if (batteryLevel < Constants.MINI_CHARGE) {    //电量小于20%
            showFragmentDialog(R.id.DIALOG_ID_UPDATE_TOO_LOW_POWER_HINT);
        }else if (batteryLevel < Constants.LOWER_CHARGE){  //电量大于20%,小于40%
            if (BatteryUtil.isCharging()){
                showFragmentDialog(R.id.DIALOG_ID_REBOOT_TO_UPGRADE);
            }else {
                showFragmentDialog(R.id.DIALOG_ID_UPDATE_LOW_POWER_HINT);
            }
        }else {   //电量大于40%
            showFragmentDialog(R.id.DIALOG_ID_REBOOT_TO_UPGRADE);
        }
// Gionee zhouhuiquan 2017-04-01 add for 101983 end
    }

    private String getSpeedString(long totalSize, long downloadSize, double speed) {
        int total = get_M_File(totalSize);
        int downloaded = get_M_File(downloadSize);
        double speed_MB = speed / BYTES_PER_MIB;
        double speed_KB = speed / BYTES_PRE_KB;
        int time = (int) ((totalSize - downloadSize) / speed);
        String text = "";
        if (speed_MB > 1) {
            text = String.format(mResources.getString(R.string.gn_su_layout_main_middlemsgview_downloading_second), speed_MB, downloaded, total, time);
            if (time >= 60) {
                time = time / 60;
                text = String.format(mResources.getString(R.string.gn_su_layout_main_middlemsgview_downloading_minute), speed_MB, downloaded, total, time);
                if (time >= 60) {
                    text = String.format(mResources.getString(R.string.gn_su_layout_main_middlemsgview_downloading_hour), speed_MB, downloaded, total);
                }
            }
        } else {
            text = String.format(mResources.getString(R.string.gn_su_layout_main_middlemsgview_downloading_second_kb), speed_KB, downloaded, total, time);
            if (time >= 60) {
                time = time / 60;
                text = String.format(mResources.getString(R.string.gn_su_layout_main_middlemsgview_downloading_minute_kb), speed_KB, downloaded, total, time);
                if (time >= 60) {
                    text = String.format(mResources.getString(R.string.gn_su_layout_main_middlemsgview_downloading_hour_kb), speed_KB, downloaded, total);
                }
            }
        }
        return text;
    }

    private AnimatorLogoView.OnRotaSingleRingListener mOnRotaSingleRingListener = new AnimatorLogoView.OnRotaSingleRingListener() {
        @Override
        public void onRotaSingleRing() {
            mBlockStatePool.wakeBlock();
        }
    };

    private BlockPool.OnConsumeListener<IContextState> mOnConsumeListener = new BlockPool.OnConsumeListener<IContextState>() {
        @Override
        public void onConsume(IContextState contextState) {
            updateContextState(contextState);
        }
    };

    private IObserver mObserver = new IObserver() {
        @Override
        public void onStateChange(IContextState state) {
            Log.d(TAG, "mObserver state : " + state);
            switch (state.state()) {
                case CHECKING:
                case DOWNLOAD_PAUSEING:
                case DOWNLOAD_COMPLETE:
                    mBlockStatePool.offer(state, true);
                    break;
                default:
                    mBlockStatePool.offer(state, false);
                    break;
            }
        }

        @Override
        public void onError(IContextState state, int error) {
            Log.d(TAG, "mObserver state : " + state + "  error : " + error);
            switch (error) {
                case Error.ERROR_CODE_RESUME_DOWNLOAD_BY_MOBILENET:
                    if (state.state() == State.DOWNLOAD_INTERRUPT) {
                        showFragmentDialog(R.id.DIALOG_ID_MOBILENET_HINT);
                    }
                    break;
                default:
                    break;
            }
        }
    };

    private IDownloadCallback mDownloadCallback = new IDownloadCallback() {
        long startTime = System.currentTimeMillis();
        boolean isStartShow = false;

        @Override
        public void onProgress(long totalSize, long downloadSize, double speed) {
            mAnimatorLogoView.changeProgress(
                    getDownloadExecutor().getDownloadProgress());
            long current = System.currentTimeMillis();
            if ((current - startTime) > UPDATE_DOWNLOAD_SPEED_TIME) {
                startTime = current;
                String text = getSpeedString(totalSize, downloadSize, speed);
                mMiddleMsgView.setExpendText(text);
            } else if (!isStartShow) {
                isStartShow = true;
                String text = getSpeedString(totalSize, downloadSize, speed);
                mMiddleMsgView.setExpendText(text);
            }
        }

        @Override
        public void onError(int errorCode) {
            switch (errorCode) {
                case Error.ERROR_CODE_DOWNLOAD_NETWORK_INTERRUPT_NO_FILE:
                    IUpdateInfo info = mSystemUpdate.getSettingUpdateInfo();
                    if (info.isPreRelease()) {
                        showFragmentDialog(R.id.DIALOG_ID_PRERELEASE_CANCEL);
                    }
                    break;
                default:
                    break;
            }

        }
    };

    private QuestionnaireManager.QuestionnaireDataChange mQuestionnaireDataChange
            = new QuestionnaireManager.QuestionnaireDataChange() {
        @Override
        public void onDataChange() {
            Log.d(TAG, "onDataChange");
            invalidateOptionsMenu();
        }
    };

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
			//Chenyee <CY_Bug> <xuyongji> <20180703> modify for SWW1618OTA-706 begin
            if (mCheckExecutor == null) {
                return;
            }
			//Chenyee <CY_Bug> <xuyongji> <20180703> modify for SWW1618OTA-706 end
            if (isFromPushNotify()) {
                setCheckTypePushValue();
                mCheckExecutor.check(CheckType.CHECK_TYPE_PUSH);
            } else {
                mCheckExecutor.check(CheckType.CHECK_TYPE_AUTO);
            }
        }
    };

    private boolean isFromPushNotify() {

        if (Constants.PUSH_NOTIFICATION_ACTION.equals(getIntent().getAction())) {
            return true;
        }
        return false;
    }

    static class Myhandler extends Handler {
        private WeakReference<SystemUpdateAnimActivity> reference = null;

        public Myhandler(SystemUpdateAnimActivity activity) {
            reference = new WeakReference<SystemUpdateAnimActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            if (reference.get() == null) {
                return;
            }
        }
    }

    protected void showFragmentDialog(int id) {
        synchronized (SystemUpdateAnimActivity.class) {
            dismissFragementDialog();
            mCurrentDialogFragment = DialogFactory.newInstance(id);
            dialogShow(id);
        }
    }

    private void dismissFragementDialog() {
        synchronized (SystemUpdateAnimActivity.class) {
            if (mCurrentDialogFragment != null) {
                mCurrentDialogFragment.dismissAllowingStateLoss();
            }
        }
    }

    private void dialogShow(int id) {
        if (mCurrentDialogFragment != null) {
            mCurrentDialogId = id;
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.add(mCurrentDialogFragment, "dailog_" + id);
            ft.commitAllowingStateLoss();
        }
    }
}
