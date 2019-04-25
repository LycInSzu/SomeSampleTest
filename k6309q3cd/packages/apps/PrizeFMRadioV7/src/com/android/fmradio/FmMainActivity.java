/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.fmradio;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.WindowManager;
//import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.TextView;
import android.widget.Toast;

import com.android.fmradio.FmService.Callback;
import com.android.fmradio.dialogs.DeleteFavoriteDialog;
import com.android.fmradio.dialogs.FmSaveDialog;
import com.android.fmradio.dialogs.NoAntennaDialog;
import com.android.fmradio.dialogs.SearchChannelsDialog;
import com.android.fmradio.dialogs.UsbPluggedDialog;
import com.android.fmradio.views.FmSnackBar;
import com.android.fmradio.R;

import com.prize.ui.FmFavoriteTools;
import com.prize.ui.FmUiManager;
import com.prize.ui.FmUiManager.IControl;
import com.prize.ui.LogTools;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import android.app.ActivityManager;
/// M: [Plugin Add] for change frequency of favorite @{
import com.mediatek.fmradio.ext.ExtensionHelper;
import com.mediatek.fmradio.ext.IFavoriteExt;
/// @}

// Nav bar color customized feature. prize-linkh-2017.08.31 @{
import com.mediatek.common.prizeoption.PrizeOption;
import android.graphics.Color;
// @}

/**
 * This class interact with user, provider FM basic function and FM recording
 * function
 */
public class FmMainActivity extends Activity implements
        OnDismissListener,
        NoAntennaDialog.NoAntennaListener,
        SearchChannelsDialog.CancelSearchListener,
        DeleteFavoriteDialog.DeleteFavoriteListener {
    // Logging
    private static final String TAG = "FmRx/Activity";

    // Dialog tags
    private static final String TAG_SEARCH = "Search";
    private static final String TAG_SAVE_RECORDINGD = "SaveRecording";
    private static final String TAG_NO_ANTENNA = "NoAntenna";
    private static final String TAG_DELETE_FAVORITE = "DeleteFavorite";
    private static final String TAG_USB_PLUGGED = "UsbPlugged";

    // Request code
    private static final int REQUEST_CODE_FAVORITE = 1;
    
    public static final int REQUEST_CODE_RECORDING = 2;
    
    private static final int PERMISSION_REQUEST_POWER_ON = 100;
    
    private static final int PERMISSION_REQUEST_CODE_RECORDING = 101;

    private static final int PERMISSION_REQUEST_CODE_SAVED_RECORDING = 102;

    // State variables
    private boolean mIsServiceStarted = false;
    private boolean mIsServiceBinded = false;
    private boolean mNeedTuneto = false;
    private boolean mIsNeedDisablePower = false;
    private boolean mIsPlaying = false;
    private boolean mIsNeedShowRecordDlg = false;
    private boolean mIsNeedShowNoAntennaDlg = false;
    private boolean mIsNeedShowSearchDlg = true;
    private boolean mIsActivityForeground = true;
    private boolean mIsNeedShowUsbPluggedDlg = false;
 // State variables
    private boolean mIsServiceConnected = false;
    private boolean mIsOnStopCalled = false;

    private int mPrevRecorderState = FmRecorder.STATE_INVALID;
    private int mCurrentStation = FmUtils.DEFAULT_STATION;

    // Instance variables
    private FmService mService = null;
    private Context mContext = null;
    private Toast mToast = null;
    private FragmentManager mFragmentManager = null;
    
 // Extra for result of request REQUEST_CODE_RECORDING
    public static final String EXTRA_RESULT_STRING = "result_string";

    //Main interface management class
    private FmUiManager mFmUiManager;
    
    private FmSettings mFmSettings;

    // Service listener
    private FmListener mFmRadioListener = new FmListener() {
        @Override
        public void onCallBack(Bundle bundle) {
            int flag = bundle.getInt(FmListener.CALLBACK_FLAG);
            Log.d(TAG, "call back method flag:" + flag);

            if (flag == FmListener.MSGID_FM_EXIT) {
                mHandler.removeCallbacksAndMessages(null);
            }

            // remove tag message first, avoid too many same messages in queue.
            Message msg = mHandler.obtainMessage(flag);
            msg.setData(bundle);
            mHandler.removeMessages(flag);
            mHandler.sendMessage(msg);
        }
    };

    private IControl mControl = new IControl() {
		
		@Override
		public void onTuneFrequency(int frequency) {
			
 			tuneToStation(frequency);
		}
		
		@Override
		public void onSwitchPower() {
			
/*			if (mIsPlaying) {
				powerDownFm();
			} else {
				tryPowerUp();
			}
			*/
			
			   if (mService != null) {
                        if (mService.getPowerStatus() == FmService.POWER_UP) {
                            powerDownFm();
                        } else {
                            powerUpFm();
                        }
                    }
		}
		
		@Override
		public void onPreRadio() {
			
			Log.d(TAG, "onClick PrevStation");
            // Search for the previous station.
            seekStation(mCurrentStation, false); // false: previous station
            // true: next station
		}
		
		@Override
		public void onNextRadio() {
			
			// Search for the next station.
            seekStation(mCurrentStation, true); // false: previous station
            // true: next station
		}
		
		@Override
		public void onAddFavorite(int position) {
			
			Log.d(TAG, "onClick AddToFavorite start");
			updateFavoriteStation(mCurrentStation, true);
            Log.d(TAG, "onClick AddToFavorite end");
		}

		@Override
		public void onDeleteFavorite(int frequency) {
			showDeleteFavoriteDialog();
			mDeleteFreq = frequency;
		}

		@Override
		public void onMore() {
        if (mPopupWindow == null) {
           	initPopupWindow();
           	findPopupViews();
           }
		   if(null != mService){
               boolean isPlaying = mService.isPowerUp();
               if (isPlaying) {
                    mChannelListBtn.setEnabled(true);
                    mSearchBtn.setEnabled(true);
                    mSettingBtn.setEnabled(true);
                }
			}
            refreshSoundModeVisiable();
            mPopupWindow.update();
            int y = getStatusBarHeight() + (int) getResources().getDimension(R.dimen.actionbar_height);
            mPopupWindow.showAtLocation(mFmUiManager.getRootView(), Gravity.RIGHT | Gravity.TOP, (int) getResources().getDimension(R.dimen.popupwindow_margin_right), y);  
		}

		@Override
		public void onSwitchSound() {
			setSpeakerPhoneOn(!mService.isSpeakerUsed());
		}
	};
    /**
     * 
     * If after PowerUp, detection PowerUp
     */
    private void checkPowerUp(boolean isPowerup) {
    	if (isPowerup) {
            mFmUiManager.updatePowerStatus(true);
            refreshPopupMenuItem(true);
            refreshActionMenuItem(true);
        } else {
            showToast(getString(R.string.not_available));
        }
    	refreshPowerImage(true);
    }
    
    /**
     * 
     * Refresh the PowerDown interface
     */
    private void checkPowerDown() {
    	mFmUiManager.updatePowerStatus(false);
        refreshActionMenuItem(false);
        refreshPopupMenuItem(false);
        refreshPowerImage(true);
    }
    
    /**
     * 
     * FM after the completion of the test
     */
    private void turnFinish(boolean isFinish) {
    	if (!isFinish) {
            Log.d(TAG, "mHandler.tune: " + isFinish);
            refreshActionMenuItem(mIsPlaying);
            refreshImageButton(mIsPlaying);
            refreshPopupMenuItem(mIsPlaying);
            return;
        }
        refreshImageButton(true);
        refreshActionMenuItem(true);
        refreshPopupMenuItem(true);
    }
    
    /**
     * 
     * Search to complete
     */
    private void scanFinish() {
    	refreshActionMenuItem(mIsPlaying);
        refreshImageButton(mIsPlaying);
        refreshPopupMenuItem(mIsPlaying);
    }

    /**
     * Main thread handler to update UI
     */
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG, "mHandler.handleMessage: what = " + msg.what +
                    ",hashcode:" + mHandler.hashCode());
            Bundle bundle;
            switch (msg.what) {
            	
            	case FmListener.MSGID_FM_USB_CHANGED:
            		bundle = msg.getData();
                    boolean isUsbPlugged = bundle.getBoolean(FmListener.KEY_IS_SWITCH_USB);
                    Log.d(TAG, "[FmRadioActivity.mHandler] swtich usb: " + isUsbPlugged);
                    mFmSettings.dismissSettingDialog();
                    dismissPopupWindow();
                    if (isUsbPlugged && mService.isAntennaAvailable()) {
                        if (mIsActivityForeground) {
                        	dismissUsbPluggedDialog();
                            showUsbPluggedDialog();
                        } else {
                            Log.d(TAG, "need show no usb dialog after onResume:");
                            mIsNeedShowUsbPluggedDlg = true;
                        }
                    } else {
                    	mIsNeedShowUsbPluggedDlg = false;
                        dismissUsbPluggedDialog();
                    }
            		break;
                case FmListener.MSGID_POWERUP_FINISHED:
                    bundle = msg.getData();
                    boolean isPowerup = mService.isPowerUp();
                    mIsPlaying = isPowerup;
                    Log.d(TAG, "updateFMState: FMRadio is powerup = " + isPowerup);
                    int station = bundle.getInt(FmListener.KEY_TUNE_TO_STATION);
                    if (mCurrentStation != station) {
                    	mCurrentStation = station;
                    	refreshStationUI(mCurrentStation);
                    }
                    checkPowerUp(isPowerup);
                    break;

                case FmListener.MSGID_SWITCH_ANTENNA:
                    bundle = msg.getData();
                    boolean isSwitch = bundle.getBoolean(FmListener.KEY_IS_SWITCH_ANTENNA);
                    Log.d(TAG, "[FmRadioActivity.mHandler] swtich antenna: " + isSwitch);
                    if (!isSwitch) {
                    	mIsNeedShowUsbPluggedDlg = false;
                    	dismissUsbPluggedDialog();
                        if (mIsActivityForeground) {
                            dismissNoAntennaDialog();
                            showNoAntennaDialog();
                        } else {
                            Log.d(TAG, "need show no antenna dialog after onResume:");
                            mIsNeedShowNoAntennaDlg = true;
                        }
                    } else {
                        mIsNeedShowNoAntennaDlg = false;
                        dismissNoAntennaDialog();
                    }
                    refreshPowerImage(true);
                    break;

                case FmListener.MSGID_POWERDOWN_FINISHED:
                    mFmSettings.dismissSettingDialog();
                    dismissPopupWindow();
                	boolean powerup = mService.isPowerUp();
                    mIsPlaying = powerup;
                    checkPowerDown();
					if (mService != null) {
                        mService.removeNotification();
                    }
                    break;

                case FmListener.MSGID_TUNE_FINISHED:
                    bundle = msg.getData();
                    boolean tuneFinish = bundle.getBoolean(FmListener.KEY_IS_TUNE);
                    boolean isPowerUp = mService.isPowerUp();
                    // when power down state, tune from channel list,
                    // will call back send mIsPowerup state.
                    mIsPlaying = mIsPlaying ? mIsPlaying : isPowerUp;

                    // tune finished, should make power enable
                    mIsNeedDisablePower = false;
                    float frequency = bundle.getFloat(FmListener.KEY_TUNE_TO_STATION);
                    mCurrentStation = FmUtils.computeStation(frequency);
                    // After tune to station finished, refresh favorite button and
                    // other button status.
                    refreshStationUI(mCurrentStation);
                    // tune fail,should resume button status
                    turnFinish(tuneFinish);
                    break;

                case FmListener.MSGID_SCAN_FINISHED:
                    bundle = msg.getData();
                    // cancel scan happen
                    boolean isScan = bundle.getBoolean(FmListener.KEY_IS_SCAN);
//                    int tuneToStation = bundle.getInt(FmRadioListener.KEY_TUNE_TO_STATION);
                    int searchedNum = bundle.getInt(FmListener.KEY_STATION_NUM);
                    scanFinish();

                    if (!isScan) {
                        dismissSearchDialog();
                        Log.d(TAG, "mHandler.scan canceled. not enter to channel list.");
                        return;
                    }

                    mCurrentStation = mService.getFrequency();
                    // After tune to station finished, refresh favorite button and
                    // other button status.
                    refreshStationUI(mCurrentStation);
                    dismissSearchDialog();

                    if (searchedNum == 0) {
                        showToast(getString(R.string.toast_cannot_search));
                        return;
                    }

                    enterChannelList();
                    // Show toast to tell user how many stations have been searched
                    showToast(getString(R.string.toast_channel_searched) + " " +
                            String.valueOf(searchedNum));
                    break;

                case FmListener.MSGID_FM_EXIT:
                    finish();
                    break;

                case FmListener.LISTEN_RDSSTATION_CHANGED:
                    bundle = msg.getData();
                    int rdsStation = bundle.getInt(FmListener.KEY_RDS_STATION);
                    refreshStationUI(rdsStation);
                    break;

                case FmListener.LISTEN_SPEAKER_MODE_CHANGED:
                    bundle = msg.getData();
                    updateSoundRes();
                	refreshSoundModeVisiable();
                    break;

                default:
                    Log.d(TAG, "invalid message");
                    break;
            }
            Log.d(TAG, "handleMessage");
        }
    };

    // When call bind service, it will call service connect. register call back
    // listener and initial device
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        /**
         * called by system when bind service
         *
         * @param className component name
         * @param service service binder
         */
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.d(TAG, "FmRadioActivity.onServiceConnected start");
            
            Log.d(TAG, "onServiceConnected");
            mIsServiceConnected = true;
            if (mIsOnStopCalled && mIsServiceBinded) {
                unbindService(mServiceConnection);
                mIsServiceBinded = false;
            }
            mService = ((FmService.ServiceBinder) service).getService();
            if (null == mService) {
                Log.e(TAG, "onServiceConnected, mService is null");
                finish();
                return;
            }

            mService.registerFmRadioListener(mFmRadioListener);
            if (FmRecorder.STATE_RECORDING != mService.getRecorderState()) {
                mService.removeNotification();
            }
            mService.setFmMainActivityForeground(mIsActivityForeground);
			mService.setCallback(new Callback() {

				@Override
				public void updatespeakerRes(final boolean isSpeaker) {
					// TODO Auto-generated method stub
					Handler mainHandler = new Handler(Looper.getMainLooper());
					mainHandler.post(new Runnable() {
						@Override
						public void run() {
							mFmUiManager.setSoundRes(isSpeaker); //bugid:38109
						}
					});
					
                 }
            });
            if (!mService.isServiceInited()) {
                mService.initService(mCurrentStation);
                mFmUiManager.updatePowerStatus(false);
//                Log.d(TAG, "Powering up FM");
//                powerUpFm();
            } else {
            	if (mService.isDeviceOpen()) {

                    // ALPS01768123 Need to power up for this case
                    // Without earphone->Start FM->Click Home->Plug in earphone->Enter FM
                    // -> Power Menu will be power down status and disabled
                    // ALPS01811383 Cannot power up when in call, because cannot get AudioFocus
				    if (!mService.isPowerUp()) {
                        // if onStop() has been called before resuming this activity during
                        // phone call, need check whether show no-antenna dialog.
                        if (!mIsNeedShowNoAntennaDlg && !mService.isAntennaAvailable()) {
                            Log.w(TAG, "Need to show no antenna dialog for plug out earphone in onPause state");
                            dismissNoAntennaDialog();
                            showNoAntennaDialog();
                        }
				    }

                    // tunetostation during changing language,we need to tune
                    // again when service bind success
                    if (mNeedTuneto) {
                        tuneToStation(mCurrentStation);
                        mNeedTuneto = false;
                    }
                    updateCurrentStation();
                    boolean isPlaying = mService.isPowerUp();
                    // back key destroy activity, mIsPlaying will be the default
                    // false.
                    // but it may be true. so the power button will be in wrong
                    // state.
                    mIsPlaying = isPlaying;
                    updateMenuStatus();
                    updateDialogStatus();

                    mFmUiManager.updatePowerStatus(mIsPlaying);
                } else {
                    // Normal case will not come here
                    // Need to exit FM for this case
                    Log.e(TAG, "ServiceConnection: service is exiting while start FM again");
                    exitService();
                    finish();
                }
            }
        }

        /**
         * When unbind service will call this method
         *
         * @param className The component name
         */
        @Override
        public void onServiceDisconnected(ComponentName className) {
            Log.d(TAG, "FmRadioActivity.onServiceDisconnected");
        }
    };
    
    /**
     * 
     * Update the favorite UI state
     * @param requency Radio frequency
     * @param isAddtrue Add a collection ,false Cancel the collection
     */
    private boolean updateFavoriteStation(int frequency, boolean isAdd) {
        String showString = null;
        // Judge the current output and switch between the devices.
        if (isAdd) {
        	if (FmStation.isFavoriteStation(mContext, frequency)) {
                // Tips have been added
                showString = getString(R.string.toast_channel_added1);
            } else { // Add a collection
            	FmStation.addToFavorite(mContext,
                		frequency);
            	showString = getString(R.string.toast_channel_added);
            	showToast(showString);
            	mFmUiManager.updateFrequency(frequency, isAdd);
            	return true;
            }
        } else {
        	if (FmStation.isFavoriteStation(mContext, frequency)) { // Cancel the radio collection
                // Need to delete this favorite channel.
                FmStation.removeFromFavorite(mContext,
                        frequency);
                showString = getString(R.string.toast_channel_deleted);
                showToast(showString);
                mFmUiManager.updateFrequency(frequency, isAdd);
                return true;
            }
        }
        if (showString != null) {
        	showToast(showString);
        }
        return false;
    }

    /**
     * Called when the activity is first created, initial variables
     *
     * @param savedInstanceState The saved bundle in onSaveInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "FmRadioActivity.onCreate start");
        // Bind the activity to FM audio stream.
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        mFmUiManager = new FmUiManager(this, mControl);
        mFmSettings = new FmSettings(this);
        setContentView(mFmUiManager.getRootView());
        mFragmentManager = getFragmentManager();
        mContext = getApplicationContext();

        initUiComponent();
        Log.d(TAG, "FmRadioActivity.onCreate end");
		/// M: [Plugin Modify] for change frequency of favorite @{
        IFavoriteExt ext = ExtensionHelper.getFavoriteExtension(mContext);
        ext.setActivity(this);
        /// @}

        // Nav bar color customized feature. prize-linkh-2017.08.31 @{
    /*    if(PrizeOption.PRIZE_NAVBAR_COLOR_CUST) {
            getWindow().setNavigationBarColor(Color.TRANSPARENT);
        } // @}         */ 
    }

    /**
     * Go to channel list activity
     */
    private void enterChannelList() {
        Log.d(TAG, "enterChannelList");
        if (mService != null) {
            // AMS change the design for background start
            // activity. need check app is background in app code
            if (mIsActivityForeground) {
                Intent intent = new Intent();
                intent.setClass(FmMainActivity.this, FmFavoriteActivity.class);
                startActivityForResult(intent, REQUEST_CODE_FAVORITE);
            } else {
                Log.d(TAG, "enterChannelList. activity is background, not enter channel list.");
            }
        }
    }

    /**
     * Refresh the favorite button with the given station, if the station is
     * favorite station, show favorite icon, else show non-favorite icon.
     *
     * @param station The station frequency
     */
    private void refreshStationUI(int station) {
        // Change the station frequency displayed.
        mFmUiManager.updateFrequency(station);
    }

    @SuppressWarnings("deprecation")
    private void restoreConfiguration() {
        // after configuration change, need to reduction else the UI is abnormal
        if (null != getLastNonConfigurationInstance()) {
            Log.d(TAG,
                    "Configration changes,activity restart,need to reset UI!");
            Bundle bundle = (Bundle) getLastNonConfigurationInstance();
            if (null == bundle) {
                return;
            }
            mPrevRecorderState = bundle.getInt("mPrevRecorderState");
            mIsNeedShowRecordDlg = bundle.getBoolean("mIsFreshRecordingStatus");
            // mIsNeedShowNoAntennaDlg = bundle.getBoolean("mIsNeedShowNoAntennaDlg");
            mIsNeedShowSearchDlg = bundle.getBoolean("mIsNeedShowSearchDlg");
            // we doesn't get it from service because the service may be
            mIsPlaying = bundle.getBoolean("mIsPlaying");
            Log.d(TAG, "bundle = " + bundle);
        }
    }

    /**
     * Start and bind service, reduction variable values if configuration
     * changed
     */
    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "FmRadioActivity.onStart start");
     // Should start FM service first.
        try {
            if (null == startService(new Intent(FmMainActivity.this, FmService.class))) {
                Log.e(TAG, "onStart, cannot start FM service");
                return;
            }
        } catch (IllegalStateException e) {
            Log.d(TAG, "unable to start service due to illegal state");
            return;

        }

        if (!mIsServiceStarted || mService == null) {
            mIsServiceStarted = true;
            mIsServiceConnected = false;
            mIsServiceBinded = bindService(new Intent(FmMainActivity.this, FmService.class),
                    mServiceConnection, Context.BIND_AUTO_CREATE);
        }

        if (!mIsServiceBinded && mService == null) {
            Log.e(TAG, "onStart, cannot bind FM service");
            finish();
            return;
        }
        restoreConfiguration();
        Log.d(TAG, "FmRadioActivity.onStart end");
    }

    /**
     * Refresh UI, when stop search, dismiss search dialog, pop up recording
     * dialog if FM stopped when recording in background
     */
    @Override
    public void onResume() {
        super.onResume();
        /// M: [Plugin Modify] for change frequency of favorite @{
        IFavoriteExt ext = ExtensionHelper.getFavoriteExtension(mContext);
        ext.onResume();
        showStatusBar();
        /// @}
        mIsActivityForeground = true;
        mIsOnStopCalled = false;
        if (null == mService) {
            Log.d(TAG, "onResume, mService is null");
            mIsNeedShowNoAntennaDlg = false;
            mIsNeedShowUsbPluggedDlg = false;
            return;
        }
        mFmUiManager.updateFavorite();
        mService.setFmMainActivityForeground(mIsActivityForeground);
        if (FmRecorder.STATE_RECORDING != mService.getRecorderState()) {
            mService.removeNotification();
        }
        updateMenuStatus();
        updateDialogStatus();
        checkNoAntennaDialogInOnResume();
        checkUsbPluggedDialogInOnResume();
        Log.d(TAG, "FmRadioActivity.onResume end");
    }
  
    private void showStatusBar() {
    	WindowManager.LayoutParams attrs = getWindow().getAttributes();
    	attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
    	getWindow().setAttributes(attrs);
    }
    /**
     * In call and plug out earphone(in onPause state), need to show no antenna dialog but
     * use mIsNeedShowNoAntennaDlg to save the state, because there is no where to show this dialog after onResume() for this case
     */
    private void checkNoAntennaDialogInOnResume() {
        if (mIsNeedShowNoAntennaDlg && mService != null && !mService.isAntennaAvailable()) {
            Log.w(TAG, "Need to show no antenna dialog for plug out earphone in onPause state");
            dismissNoAntennaDialog();
            showNoAntennaDialog();
        }
    }
    
    private void checkUsbPluggedDialogInOnResume() {
        if (mIsNeedShowUsbPluggedDlg && mService != null && mService.isUsbPlugged()) {
            Log.w(TAG, "Need to show usb plugged dialog for plug out earphone in onPause state");
            dismissUsbPluggedDialog();
            showUsbPluggedDialog();
        }
    }

    /**
     * When activity is paused call this method, indicate activity enter
     * background if press exit, power down FM
     */
    @Override
    public void onPause() {
    		Log.d(TAG, "onPause");
        /// M: [Plugin Modify] for change frequency of favorite @{
        IFavoriteExt ext = ExtensionHelper.getFavoriteExtension(mContext);
        ext.onPause();
        /// @}
        mIsActivityForeground = false;
        if (null != mService) {
            mService.setFmMainActivityForeground(mIsActivityForeground);
        }
        /**
         * Should dismiss before call onSaveInstance, or it will resume
         * automatic
         */
        mIsNeedShowSearchDlg = true;
        dismissSearchDialog();

        /**
         * should dismiss before call onSaveInstance, or it will resume
         * automatic.
         */
        FmSaveDialog df = (FmSaveDialog) mFragmentManager
                .findFragmentByTag(TAG_SAVE_RECORDINGD);
        if (null != df && df.getShowsDialog()) {
            Log.d(TAG, "onPause.dismissSaveRecordingDialog()");
            if (mService != null) {
                mService.setModifiedRecordingName(df.getRecordingNameToSave());
            }
            dismissSaveRecordingDialog();
            mIsNeedShowRecordDlg = true;
        }

        // Need to dismiss avoid AMS popup this dialog again for power up will show this dialog
        if (dismissNoAntennaDialog()) {
            mIsNeedShowNoAntennaDlg = true;
            refreshPowerImage(true);
        }
        
        if (dismissUsbPluggedDialog()) {
            mIsNeedShowUsbPluggedDlg = true;
            refreshPowerImage(true);
        }

        Log.d(TAG, "end FmRadioActivity.onPause");
        super.onPause();
    }

    /**
     * Called when activity enter stopped state, unbind service, if exit
     * pressed, stop service
     */
    @Override
    public void onStop() {
    	if (null != mService) {
            mService.setNotificationClsName(FmMainActivity.class.getName());
            mService.updatePlayingNotification();
        }
        if (mIsServiceBinded && mIsServiceConnected) {
            mIsServiceConnected = false;
            unbindService(mServiceConnection);
            mIsServiceBinded = false;
        }
        mIsOnStopCalled = true;
        mIsNeedShowNoAntennaDlg = false;
        mIsNeedShowUsbPluggedDlg = false;
        Log.d(TAG, "end FmRadioActivity.onStop");
        super.onStop();
        dismissPopupWindow();
		mFmSettings.dismissSettingDialog();
    }

    /**
     * W activity destroy, unregister broadcast receiver and remove handler
     * message
     */
    @Override
    public void onDestroy() {
        Log.d(TAG, "start FmRadioActivity.onDestroy");
        // need to call this function because if doesn't do this,after
        // configuration change will have many instance and recording time
        // or playing time will not refresh
        // Remove all the handle message
        mHandler.removeCallbacksAndMessages(null);
        if (mService != null) {
            mService.unregisterFmRadioListener(mFmRadioListener);
        }
        mFmRadioListener = null;
        if (null != mPopupWindow) {
        	mPopupWindow.dismiss();
        	mPopupWindow = null;
        }
        Log.d(TAG, "end FmRadioActivity.onDestroy");
        super.onDestroy();
    }

    /**
	 * Used to get the height of the status bar.
	 */
	private int getStatusBarHeight() {
		if (mStatusBarHeight == 0) {
			try {
				Class<?> c = Class.forName("com.android.internal.R$dimen");
				Object o = c.newInstance();
				Field field = c.getField("status_bar_height");
				int x = (Integer) field.get(o);
				mStatusBarHeight = getResources().getDimensionPixelSize(x);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return mStatusBarHeight;
	}
	
	/** The height of the status bar**/
	private int mStatusBarHeight;
    private PopupWindow mPopupWindow = null;
    private TextView mSearchBtn;
    private TextView mChannelListBtn;
    private TextView mSettingBtn;
    private TextView mRecordBtn;
    private View mPopupView;
    
    private View.OnClickListener mOnClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			
			switch (v.getId()) {
            case R.id.fm_search: // Search radio station
				// Don't show search dialog when activity is background
                if (!mIsActivityForeground) {
                    Log.w(TAG, "click searh menu in background, don't show search dialog");
                    break;
                }
                mIsNeedShowSearchDlg = true;
                search();
                showSearchDialog();
                FmStation.cleanSearchedStations(mContext);
                mService.startScanAsync();
                break;

            case R.id.fm_channel_list: // The channel list
                refreshImageButton(false);
                refreshActionMenuItem(false);
                refreshPopupMenuItem(false);
                // Show favorite activity.
                enterChannelList();
                break;
                
            case R.id.fm_setting: // Set up the
            	mFmSettings.showSettingDialog();
                break;
            case R.id.fm_record: // Set up the
            	startRecording();
                break;
            default:
                Log.d(TAG, "invalid menu item");
                break;
			}
			mPopupWindow.dismiss();
		}
	};
    
	/**
	 * 
	 * Initialize PowupWindow
	 */
    private void initPopupWindow() {
    	LayoutInflater inflater = LayoutInflater.from(this); 
    	mPopupView = inflater.inflate(R.layout.fm_popup, null); 
        mPopupWindow = new PopupWindow(mPopupView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, false);
        mPopupWindow.setAnimationStyle(R.style.popupWindowAnimation);
        mPopupWindow.setOnDismissListener(this);
        mPopupWindow.setBackgroundDrawable(new BitmapDrawable()); 
        //Click Settings window outside the window
        mPopupWindow.setOutsideTouchable(true); 
        // Set this parameter to obtain focus, otherwise you won't click
        mPopupWindow.setFocusable(true); 
        // PopupWindow is displayed, press the menu button disappeared PopupWindow
        mPopupView.setFocusable(true);
        mPopupView.setFocusableInTouchMode(true);
        mPopupView.setOnKeyListener(new OnKeyListener() {
			
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				
				if (keyCode == KeyEvent.KEYCODE_MENU && event.getAction() == KeyEvent.ACTION_UP) {
					if (mPopupWindow != null && mPopupWindow.isShowing()) {
						mPopupWindow.dismiss();
					}
					return true;
				}
				return false;
			}
		});
    }

    private void dismissPopupWindow() {
        if (mPopupWindow != null && mPopupWindow.isShowing()) {
            mPopupWindow.dismiss();
        }
    }

    private void findPopupViews() {
    	mSearchBtn = (TextView) mPopupView.findViewById(R.id.fm_search);
    	mChannelListBtn = (TextView) mPopupView.findViewById(R.id.fm_channel_list);
    	mSettingBtn = (TextView) mPopupView.findViewById(R.id.fm_setting);
    	mRecordBtn = (TextView) mPopupView.findViewById(R.id.fm_record);
    	
    	mSearchBtn.setOnClickListener(mOnClickListener);
    	mChannelListBtn.setOnClickListener(mOnClickListener);
    	mSettingBtn.setOnClickListener(mOnClickListener);
    	mRecordBtn.setOnClickListener(mOnClickListener);
    }

    /**
     * When on activity result, tune to station which is from channel list
     *
     * @param requestCode The request code
     * @param resultCode The result code
     * @param data The intent from channel list
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (RESULT_OK == resultCode) {/*
            if (REQUEST_CODE_FAVORITE != requestCode) {
                Log.e(TAG, "Error: Invalid requestcode.");
                return;
            }
            int iStation = data.getIntExtra(
                    FmFavoriteActivity.ACTIVITY_RESULT, mCurrentStation);
            // Tune to this station.
            mCurrentStation = iStation;
            // if tune from channel list, we should disable power menu,
            // especially for
            // power down state
            mIsNeedDisablePower = true;
            Log.d(TAG, "onActivityForReult:" + mIsNeedDisablePower);
            if (null == mService) {
                Log.d(TAG, "activity.onActivityResult mService is null");
                mNeedTuneto = true;
                return;
            }
            tuneToStation(iStation);
            return;*/
        	
        	if (REQUEST_CODE_RECORDING == requestCode) {
                final Uri playUri = data.getData();
                boolean isSaved = playUri != null;
                String title = data.getStringExtra(EXTRA_RESULT_STRING);
                String action = null;
                FmSnackBar.OnActionTriggerListener listener = null;

                if (isSaved) {
                    action = FmMainActivity.this.getString(R.string.toast_listen);
                    listener = new FmSnackBar.OnActionTriggerListener() {
                        @Override
                        public void onActionTriggered() {
                            Intent playMusicIntent = new Intent(Intent.ACTION_VIEW);
                            try {
                                playMusicIntent.setClassName("com.android.music",
                                        "com.android.music.AudioPreview");
                                playMusicIntent.setDataAndType(playUri, "audio/3gpp");
                                startActivity(playMusicIntent);
                            } catch (ActivityNotFoundException e1) {
                                try {
                                    playMusicIntent = new Intent(Intent.ACTION_VIEW);
                                    playMusicIntent.setDataAndType(playUri, "audio/3gpp");
                                    startActivity(playMusicIntent);
                                } catch (ActivityNotFoundException e2) {
                                    // No activity respond
                                    Log.d(TAG,"onActivityResult, no activity "
                                            + "respond play record file intent");
                                }
                            }
                        }
                    };
                }
                FmSnackBar.make(FmMainActivity.this, title, action, listener,
                        FmSnackBar.DEFAULT_DURATION).show();
            } else if (REQUEST_CODE_FAVORITE == requestCode) {
			    int iStation = data.getIntExtra(
                    FmFavoriteActivity.ACTIVITY_RESULT, mCurrentStation);
            // Tune to this station.
            mCurrentStation = iStation;
            // if tune from channel list, we should disable power menu,
            // especially for
            // power down state
            mIsNeedDisablePower = true;
            Log.d(TAG, "onActivityForReult:" + mIsNeedDisablePower);
            if (null == mService) {
                Log.d(TAG, "activity.onActivityResult mService is null");
                mNeedTuneto = true;
                return;
            }
            tuneToStation(iStation);
			
                
            } else {
                Log.e(TAG, "onActivityResult, invalid requestcode.");
                return;
            }
        }

        // Do not handle other result.
        Log.v(TAG, "The activity for requestcode " + requestCode
                + " does not return any data.");
    }

    /**
     * Power up FM
     */
    private void powerUpFm() {
        Log.v(TAG, "start powerUpFm");
        // New flight mode judgment, Fm unavailable flight mode
        if (!mService.isAirplaneMode() && !mService.isUsbPlugged()) {
        	refreshImageButton(false);
            refreshActionMenuItem(false);
            refreshPopupMenuItem(false);
            refreshPowerImage(false);
            int recordAudioPermission = checkSelfPermission(Manifest.permission.RECORD_AUDIO);
            List<String> mPermissionStrings = new ArrayList<String>();
            boolean mRequest = false;
            if (recordAudioPermission != PackageManager.PERMISSION_GRANTED) {
                mPermissionStrings.add(Manifest.permission.RECORD_AUDIO);
                mRequest = true;
            }
            if (mRequest == true) {
                String[] mPermissionList = new String[mPermissionStrings.size()];
                mPermissionList = mPermissionStrings.toArray(mPermissionList);
                requestPermissions(mPermissionList, PERMISSION_REQUEST_POWER_ON);
                return;
            }
            mService.setRecordingPermission(true);
            mService.powerUpAsync(FmUtils.computeFrequency(mCurrentStation));
            Log.v(TAG, "end powerUpFm");
        } else if (mService.isUsbPlugged()) {
        	showUsbPluggedDialog();
        } else {
        	showToast(getResources().getString(R.string.airplane_mode_fm_disable));
        }
    }
    private void showUsbPluggedDialog() {
    	// TODO
    	UsbPluggedDialog newFragment = UsbPluggedDialog.newInstance();
        newFragment.show(mFragmentManager, TAG_USB_PLUGGED);
        mFragmentManager.executePendingTransactions();
    }
    
    private boolean dismissUsbPluggedDialog() {
    	UsbPluggedDialog newFragment = (UsbPluggedDialog) mFragmentManager
                .findFragmentByTag(TAG_USB_PLUGGED);
        if (null != newFragment) {
            newFragment.dismissAllowingStateLoss();
            return true;
        }
        return false;
    }
    
    /**
     * Power down FM
     */
    private void powerDownFm() {
        Log.v(TAG, "start powerDownFm");
        refreshImageButton(false);
        refreshActionMenuItem(false);
        refreshPopupMenuItem(false);
        refreshPowerImage(false);
        mService.powerDownAsync();
        Log.v(TAG, "end powerDownFm");
    }

    /**
	 * Set FM audio from speaker or not
	 *
	 * @param isSpeaker
	 *            true if set FM audio from speaker
	 */
    private void setSpeakerPhoneOn(boolean isSpeaker) {
        if (isSpeaker) {
            Log.v(TAG, "UseSpeaker");
            mService.setSpeakerPhoneOn(true);
        } else {
            Log.v(TAG, "UseEarphone");
            mService.setSpeakerPhoneOn(false);
        }
        updateSoundRes();
    }
    
    private void updateSoundRes() {
        if(mService == null){
			mFmUiManager.setSoundRes(false);
        }else{
			mFmUiManager.setSoundRes(mService.isSpeakerUsed());
        }
    }

    /**
     * Tune to a station
     *
     * @param station The tune station
     */
    private void tuneToStation(final int station) {
        refreshImageButton(false);
        refreshActionMenuItem(false);
        refreshPopupMenuItem(false);
        mService.tuneStationAsync(FmUtils.computeFrequency(station));
    }

    /**
     * Seek station according current frequency and direction
     *
     * @param station The seek start station
     * @param direction The seek direction
     */
    private void seekStation(final int station, boolean direction) {
        // If the seek AsyncTask has been executed and not canceled, cancel it
        // before start new.
        refreshImageButton(false);
        refreshActionMenuItem(false);
        refreshPopupMenuItem(false);
        mService.seekStationAsync(FmUtils.computeFrequency(station),
                direction);
    }

    // Refresh Button mButtonPrevStation mButtonNextStation
    private void refreshImageButton(boolean enabled) {
        mFmUiManager.setControlEnable(enabled);
    }
    
    private void refreshPowerImage(boolean enabled) {
        mFmUiManager.setPowerEnable(enabled);
    }

    // Refresh action menu except power menu
    private void refreshActionMenuItem(boolean enabled) {
        // action menu
    	mFmUiManager.refreshActionBar(enabled && !(mService.isBluetoothHeadsetInUse() &&
                mService.isRender()), enabled);
    }

    // Refresh PopupWindow Item
    private void refreshPopupMenuItem(boolean enabled) {
    	if (null != mPopupWindow) {
            mChannelListBtn.setEnabled(enabled);
            mSearchBtn.setEnabled(enabled);
            mSettingBtn.setEnabled(true);
            refreshSoundModeVisiable();
        }
    }

    private void refreshSoundModeVisiable() {
    	// Need hide only short antenna support and not plug in earphone
        boolean showSoundMode = mService.isAntennaAvailable();
        mFmUiManager.refreshSoundModeVisiable(showSoundMode && mIsPlaying ? View.VISIBLE : View.GONE);
    }

    /**
     * Called when back pressed
     */
    @Override
    public void onBackPressed() {
        Log.d(TAG, "begin FmRadioActivity.onBackPressed");
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        // check if activity is pinned
	    if (!activityManager.isInLockTaskMode()) {
            // exit fm, disable all button
            if ((null != mService) && (!mService.isPowerUp())) {
            	refreshImageButton(false);
                refreshActionMenuItem(false);
                refreshPopupMenuItem(false);
                exitService();
            }
        }

        super.onBackPressed();
        Log.d(TAG, "end FmRadioActivity.onBackPressed");
    }

    private void showToast(CharSequence text) {
        if (null == mToast) {
            mToast = Toast.makeText(mContext.getApplicationContext(), text, Toast.LENGTH_SHORT);
        }
        mToast.setText(text);
        mToast.show();
        Log.v(TAG, "FmRadioActivity.showToast: toast = " + text);
    }
    
    /**
     * use onRetainNonConfigurationInstance because after configuration change,
     * activity will destroy and create need use this function to save some
     * important variables
     */
    @Override
    public Object onRetainNonConfigurationInstance() {
        final int size = 5;
        Bundle bundle = new Bundle(size);
        bundle.putInt("mPrevRecorderState", mPrevRecorderState);
        bundle.putBoolean("mIsFreshRecordingStatus", mIsNeedShowRecordDlg);
        //bundle.putBoolean("mIsNeedShowNoAntennaDlg", mIsNeedShowNoAntennaDlg);
        bundle.putBoolean("mIsNeedShowSearchDlg", mIsNeedShowSearchDlg);
        bundle.putBoolean("mIsPlaying", mIsPlaying);
        Log.d(TAG, "onRetainNonConfigurationInstance() bundle:" + bundle);
        return bundle;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.d(TAG, "onSaveInstanceState");
        //super.onSaveInstanceState(outState);
    }
    
    /**
     * 
     * Search UI update
     */
    private void search() {
    	refreshImageButton(false);
        refreshActionMenuItem(false);
        refreshPopupMenuItem(false);
    }

    /**
     * Exit FM service
     */
    private void exitService() {
        Log.i(TAG, "exitService");
        if (mIsServiceBinded) {
            unbindService(mServiceConnection);
            mIsServiceBinded = false;
        }

        if (mIsServiceStarted) {
            boolean isSuccess = stopService(new Intent(
                    FmMainActivity.this, FmService.class));
            if (!isSuccess) {
                Log.e(TAG, "Error: Cannot stop the FM service.");
            }
            mIsServiceStarted = false;
        }
    }

    /**
     * Show no antenna dialog
     */
    public void showNoAntennaDialog() {
        NoAntennaDialog newFragment = NoAntennaDialog.newInstance();
        newFragment.show(mFragmentManager, TAG_NO_ANTENNA);
        mFragmentManager.executePendingTransactions();
    }

    /**
     * Show save recording dialog
     *
     * @param name The recording file name
     */
    public void showSaveRecordingDialog() {
        String sdcard = FmService.getRecordingSdcard();
        String defaultName = mService.getRecordingName();
        String recordingName = mService.getModifiedRecordingName();
        FmSaveDialog newFragment =
                new FmSaveDialog(sdcard, defaultName, recordingName);
        newFragment.show(mFragmentManager, TAG_SAVE_RECORDINGD);
        mFragmentManager.executePendingTransactions();
    }

    /**
     * Show search dialog
     */
    private void showSearchDialog() {
        SearchChannelsDialog newFragment = SearchChannelsDialog.newInstance();
        newFragment.show(mFragmentManager, TAG_SEARCH);
        mFragmentManager.executePendingTransactions();
    }

    /**
     * Dismiss search dialog
     */
    private void dismissSearchDialog() {
        SearchChannelsDialog newFragment = (SearchChannelsDialog) mFragmentManager
                .findFragmentByTag(TAG_SEARCH);
        if (null != newFragment) {
            newFragment.dismissAllowingStateLoss();
        }
    }

    /**
     * Dismiss save recording dialog
     */
    private void dismissSaveRecordingDialog() {
        FragmentTransaction ft = mFragmentManager.beginTransaction();
        Fragment fragment = mFragmentManager.findFragmentByTag(TAG_SAVE_RECORDINGD);
        if (null != fragment) {
            ft.remove(fragment);
            ft.commitAllowingStateLoss();
        }
    }

    /**
     * Dismiss no antenna dialog
     */
    private boolean dismissNoAntennaDialog() {
        NoAntennaDialog newFragment = (NoAntennaDialog) mFragmentManager
                .findFragmentByTag(TAG_NO_ANTENNA);
        if (null != newFragment) {
            newFragment.dismissAllowingStateLoss();
            return true;
        }
        return false;
    }

    /**
     * Cancel search progress
     */
    public void cancelSearch() {
        Log.d(TAG, "FmRadioActivity.cancelSearch");
        mService.stopScan();
    }

    /**
     * No antenna continue to operate
     */
    @Override
    public void noAntennaContinue() {
        // We let user use the app if no antenna.
        // But we do not automatically start FM.
        Log.d(TAG, " noAntennaContinue.onClick ok to continue");
        tryPowerUp();
    }
    
    private void tryPowerUp() {
    	if (mService.isAntennaAvailable()) {
            powerUpFm();
        } else {
            Log.d(TAG, "noAntennaContinue.earphone is not ready");
            mService.switchAntennaAsync(1);
        }
    }

    /**
     * No antenna cancel to operate
     */
    @Override
    public void noAntennaCancel() {
        Log.d(TAG, " onClick Negative");
        /*if (mService != null && !mService.isInLockTaskMode()) {
            exitService();
        } else {
            Log.d(TAG, "No need exit Service and Activity cause current is lock mode");
        }*/
        refreshPowerImage(true);
    }

    /**
     * Update current station according service station
     */
    private void updateCurrentStation() {
        // get the frequency from service, set frequency in activity, UI,
        // database
        // same as the frequency in service
        int freq = mService.getFrequency();
        if (FmUtils.isValidStation(freq)) {
            if (mCurrentStation != freq) {
                Log.d(TAG, "frequency in service isn't same as in database");
                mCurrentStation = freq;
                FmStation.setCurrentStation(mContext, mCurrentStation);
                refreshStationUI(mCurrentStation);
            }
        }
    }

    /**
     * Update button status, and dialog status
     */
    private void updateDialogStatus() {
        Log.d(TAG, "updateDialogStatus.mIsNeedShowSearchDlg:" + mIsNeedShowSearchDlg);
        boolean isScan = mService.isScanning();
        // check whether show search dialog, because it may be dismissed
        // onSaveInstance
        if (isScan && mIsNeedShowSearchDlg) {
            Log.d(TAG, "updateDialogStatus: show search dialog. isScan is " + isScan);
            mIsNeedShowSearchDlg = false;
            showSearchDialog();
        }

        // check whether show recorder dialog, when activity is foreground
        if (mIsNeedShowRecordDlg) {
            Log.d(TAG, "updateDialogStatus.resume recordDlg.mPrevRecorderState:" +
                    mPrevRecorderState);
            showSaveRecordingDialog();
            mIsNeedShowRecordDlg = false;
        }

    }

    /**
     * Update menu status, and animation
     */
    private void updateMenuStatus() {
        boolean isPlaying = mService.isPowerUp();
        boolean isPoweruping = mService.isPowerUping();
        boolean isSeeking = mService.isSeeking();
        boolean isScan = mService.isScanning();
        Log.d(TAG, "updateMenuStatus.isSeeking:" + isSeeking);
        boolean fmStatus = (isScan || isSeeking || isPoweruping);
        // when seeking, all button should disabled,
        // else should update as origin status
        refreshImageButton(fmStatus ? false : isPlaying);
        refreshPopupMenuItem(fmStatus ? false : isPlaying);
        refreshActionMenuItem(fmStatus ? false : isPlaying);
		if(isPlaying){
			setSpeakerPhoneOn(mService.isSpeakerUsed());
		}
    }

    private void initUiComponent() {
        Log.i(TAG, "initUIComponent");

        // initial mPopupWindow
        initPopupWindow();
        findPopupViews();

        // put favorite button here since it might be used very early in
        // changing recording mode
        mCurrentStation = FmStation.getCurrentStation(mContext);

        updateFmFrequency();
    }

    /**
     * 
     * Update the broadcast frequency
     */
    private void updateFmFrequency() {
    	mFmUiManager.updateFrequency(mCurrentStation);
    }
    
    /** To cancel the frequency of the collection**/
    private int mDeleteFreq;
    
    /**
     * 
     * Pop-up cancel collection confirmation box dialog
     */
    private void showDeleteFavoriteDialog() {
		DeleteFavoriteDialog newFragment = DeleteFavoriteDialog.newInstance();
		newFragment.show(getFragmentManager(), TAG_DELETE_FAVORITE);
	}

	@Override
	public void deleteFavorite() {
		updateFavoriteStation(mDeleteFreq, false);
	}

	@Override
	public void onDismiss() {
		
		Log.d(TAG, "popwindow dismiss listener:");
        invalidateOptionsMenu();
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		
		//The radio response when the Menu button and click more Menu the same treatment
		if (keyCode == KeyEvent.KEYCODE_MENU) {
			if (mIsPlaying) {
				mFmUiManager.keyMore();
			}
			return true;
		}
		return super.onKeyUp(keyCode, event);
	}
	
	 /**
     * start recording
     */
    private void startRecording() {
        int readExtStorage = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
        int writeExtStorage = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        List<String> mPermissionStrings = new ArrayList<String>();
        boolean mRequest = false;

        if (readExtStorage != PackageManager.PERMISSION_GRANTED) {
            mPermissionStrings.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            mRequest = true;
        }
        if (writeExtStorage != PackageManager.PERMISSION_GRANTED) {
            mPermissionStrings.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            mRequest = true;
        }
        if (mRequest == true) {
            String[] mPermissionList = new String[mPermissionStrings.size()];
            mPermissionList = mPermissionStrings.toArray(mPermissionList);
            requestPermissions(mPermissionList, PERMISSION_REQUEST_CODE_RECORDING);
            return;
        }
        Intent recordIntent = new Intent(this, FmRecordActivity.class);
        recordIntent.putExtra(FmStation.CURRENT_STATION, mCurrentStation);
        startActivityForResult(recordIntent, REQUEST_CODE_RECORDING);
    }
    private void openSavedRecordings() {
        int readExtStorage = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
        int writeExtStorage = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        List<String> mPermissionStrings = new ArrayList<String>();
        boolean mRequest = false;

        if (readExtStorage != PackageManager.PERMISSION_GRANTED) {
            mPermissionStrings.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            mRequest = true;
        }
        if (writeExtStorage != PackageManager.PERMISSION_GRANTED) {
            mPermissionStrings.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            mRequest = true;
        }
        if (mRequest == true) {
            String[] mPermissionList = new String[mPermissionStrings.size()];
            mPermissionList = mPermissionStrings.toArray(mPermissionList);
            requestPermissions(mPermissionList, PERMISSION_REQUEST_CODE_SAVED_RECORDING);
            return;
        }
        Intent playMusicIntent = new Intent(Intent.ACTION_VIEW);
                int playlistId = FmRecorder.getPlaylistId(mContext);
                Bundle extras = new Bundle();
                extras.putInt("playlist", playlistId);
                try {
                    playMusicIntent.putExtras(extras);
                    playMusicIntent.setClassName("com.google.android.music",
                            "com.google.android.music.ui.TrackContainerActivity");
                    playMusicIntent.setType("vnd.android.cursor.dir/playlist");
                    startActivity(playMusicIntent);
                } catch (ActivityNotFoundException e1) {
                    try {
                        playMusicIntent = new Intent();
                        Bundle extraData = new Bundle();
                        extraData.putString("playlist", String.valueOf(playlistId));
                        playMusicIntent.putExtras(extraData);
                        playMusicIntent.setClassName("com.android.music",
                            "com.android.music.PlaylistBrowserActivity");
                        startActivity(playMusicIntent);
                    } catch (ActivityNotFoundException e2) {
                        try {
                        playMusicIntent = new Intent(Intent.ACTION_VIEW);
                            Bundle aExtraData = new Bundle();
                            aExtraData.putString("playlist", String.valueOf(playlistId));
                            playMusicIntent.putExtras(aExtraData);
                        playMusicIntent.setType("vnd.android.cursor.dir/playlist");
                        startActivity(playMusicIntent);
                        } catch (ActivityNotFoundException e3) {
                            Log.d(TAG, "onOptionsItemSelected, No activity " +
                                "respond playlist view intent");
                        }
                    }
                }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
            int[] grantResults) {
        boolean granted = true;
        boolean mShowPermission = true;
        if (permissions.length <= 0 || grantResults.length <= 0) {
            Log.d(TAG, "permission length not sufficient");
            showToast(getString(R.string.missing_required_permission));
            return;
        }
        if (requestCode == PERMISSION_REQUEST_POWER_ON) {
            granted = (grantResults[0] == PackageManager.PERMISSION_GRANTED);
            if (!granted) {
                mShowPermission = shouldShowRequestPermissionRationale(permissions[0]);
            }
            Log.i(TAG, "<onRequestPermissionsResult> Power on fm granted" + granted);
            if (granted == true) {
                if (mService != null) {
                    mService.setRecordingPermission(true);
                    powerUpFm();
                }
            } else if (!mShowPermission) {
                showToast(getString(R.string.missing_required_permission));
            }
        } else if (requestCode == PERMISSION_REQUEST_CODE_RECORDING) {
            for (int counter = 0; counter < permissions.length; counter++) {
                boolean permissionGranted = false;
                permissionGranted = (grantResults[counter] ==
                                         PackageManager.PERMISSION_GRANTED);
                granted = granted && permissionGranted;
                if (!permissionGranted) {
                    mShowPermission = mShowPermission && shouldShowRequestPermissionRationale(
                                      permissions[counter]);
                }
            }
            Log.i(TAG, "<onRequestPermissionsResult> Record audio granted" + granted);
            if (granted == true) {
                startRecording();
            } else if (!mShowPermission) {
                showToast(getString(R.string.missing_required_permission));
            }
        } else if (requestCode == PERMISSION_REQUEST_CODE_SAVED_RECORDING) {
            for (int counter = 0; counter < permissions.length; counter++) {
                boolean permissionGranted = false;
                permissionGranted = (grantResults[counter] ==
                                             PackageManager.PERMISSION_GRANTED);
                granted = granted && permissionGranted;
                if (!permissionGranted) {
                    mShowPermission = mShowPermission && shouldShowRequestPermissionRationale(
                                      permissions[counter]);
                }
            }
            Log.i(TAG, "<onRequestPermissionsResult> Read/Write permission granted" + granted);
            if (granted == true) {
                openSavedRecordings();
            } else if (!mShowPermission) {
                showToast(getString(R.string.missing_required_permission));
            }
        }
    }

}
