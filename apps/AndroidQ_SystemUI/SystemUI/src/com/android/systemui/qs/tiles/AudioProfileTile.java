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

package com.android.systemui.qs.tiles;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.ComponentName;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManagerGlobal;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.internal.logging.MetricsLogger;
import com.android.systemui.R;
import com.android.systemui.FontSizeUtils;
import com.mediatek.systemui.statusbar.util.SIMHelper;
import com.android.systemui.plugins.qs.QSTile.BooleanState;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.tileimpl.QSTileImpl;

import java.util.ArrayList;
import java.util.List;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.os.SystemProperties;
import android.content.SharedPreferences;

import com.android.systemui.Dependency;
import com.android.systemui.plugins.VolumeDialogController;
import com.android.systemui.plugins.VolumeDialogController.State;
import com.android.systemui.plugins.VolumeDialogController.StreamState;

public class AudioProfileTile extends QSTileImpl<BooleanState> {
    private static final String TAG = "AudioProfileTile";
    private static final boolean DBG = true;

    private static final int PROFILE_SWITCH_DIALOG_LONG_TIMEOUT = 4000;
    private static final int PROFILE_SWITCH_DIALOG_SHORT_TIMEOUT = 2000;
    private static final int SHOW_PROFILE_SWITCH_DIALOG = 9000;
    private static final int UPDATE_RINGER_MODE = 5000;

    private boolean mListening;
    private boolean mUpdating = false;

    private Dialog mProfileSwitchDialog;

    private ImageView mNormalProfileIcon;
    private ImageView mMettingProfileIcon;
    private ImageView mMuteProfileIcon;
    private ImageView mOutdoorSwitchIcon;
    private ImageView mAudioProfileIcon;
    private AudioManager mAudioManager;
    private int mAudioState = R.drawable.ic_qs_general_on;
    private int SelectType = TYPE_NORMAL ;
    private final static int TYPE_NORMAL = 1;
    private final static int TYPE_MEETING = 2;
    private final static int TYPE_OUTDOOR = 3;
    private final static int TYPE_SILENT = 4;
    private int OriginRing;
    private int OriginAlarm;
    private int OriginMedia;
    private int OriginRing1;
    private int OriginAlarm1;
    private int OriginMedia1;
    private final Receiver mReceiver = new Receiver();
    private Intent myintent;
    private int fixOutBug;
    private SharedPreferences sharedPreferences = mContext.getSharedPreferences("default_volume", Context.MODE_PRIVATE);
    private final Icon mIcon = ResourceIcon.get(R.drawable.ic_qs_general_on);

    private final VolumeDialogController mController;

    public AudioProfileTile(QSHost host) {
        super(host);
        createProfileSwitchDialog();
        mAudioManager=(AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE);
        OriginRing1 = mAudioManager.getStreamVolume(AudioManager.STREAM_RING);
        OriginAlarm1 = mAudioManager.getStreamVolume(AudioManager.STREAM_ALARM);
        OriginMedia1 = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        mReceiver.register(true);
        myintent = new Intent(AudioManager.RINGER_MODE_CHANGED_ACTION);
        mController = Dependency.get(VolumeDialogController.class);
    }
    
    @Override
    protected void handleDestroy() {
        super.handleDestroy();
    }
    
    @Override
    public BooleanState newTileState() {
        return new BooleanState();
    }
    
    @Override
    public void handleSetListening(boolean listening) {
    }
    
    @Override
    protected void handleUserSwitch(int newUserId) {
    }
    
    @Override
    public Intent getLongClickIntent() {
        return new Intent("com.android.screen.shot");
    }

    @Override
    protected void handleClick() {
        Log.e(TAG,"handleClick currentmode =" + mAudioManager.getRingerMode());
        Message msg = mHandler.obtainMessage(SHOW_PROFILE_SWITCH_DIALOG);
        mHandler.sendMessage(msg);
    }
    
     @Override
    public CharSequence getTileLabel() {
        return mContext.getString(R.string.audio_profile);
    }
    
    @Override
    protected void handleLongClick() {
    }

    @Override
    protected void handleUpdateState(BooleanState state, Object arg) {
        state.label = mContext.getString(R.string.audio_profile);
        state.icon = ResourceIcon.get(mAudioState);
    }

    @Override
    public int getMetricsCategory() {
        return 251;
    }

    private void showProfileSwitchDialog() {
        createProfileSwitchDialog();
        if (!mProfileSwitchDialog.isShowing()) {
            mProfileSwitchDialog.show();
            dismissProfileSwitchDialog(PROFILE_SWITCH_DIALOG_LONG_TIMEOUT);
        }
    }

    private void createProfileSwitchDialog() {
        if (DBG) {
            Log.i(TAG, "createProfileSwitchDialog");
        }
        mProfileSwitchDialog = null;

        mProfileSwitchDialog = new Dialog(mContext);
        mProfileSwitchDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mProfileSwitchDialog.setContentView(R.layout.quick_settings_profile_switch_dialog);
        mProfileSwitchDialog.setCanceledOnTouchOutside(true);
        mProfileSwitchDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_STATUS_BAR_PANEL);
        mProfileSwitchDialog.getWindow().getAttributes().privateFlags |=
                WindowManager.LayoutParams.PRIVATE_FLAG_SHOW_FOR_ALL_USERS;
        mProfileSwitchDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        mProfileSwitchDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM,
                WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        mProfileSwitchDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED,
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);

        mMettingProfileIcon =
                (ImageView) mProfileSwitchDialog.findViewById(R.id.meeting_profile_icon);
        mOutdoorSwitchIcon =
                (ImageView) mProfileSwitchDialog.findViewById(R.id.outdoor_profile_icon);
        mMuteProfileIcon =
                (ImageView) mProfileSwitchDialog.findViewById(R.id.mute_profile_icon);
        mNormalProfileIcon =
                (ImageView) mProfileSwitchDialog.findViewById(R.id.normal_profile_icon);
        upDateSelectType(SelectType);
        View normalProfile = (View) mProfileSwitchDialog.findViewById(R.id.normal_profile);
        TextView normalProfileText =
                (TextView) mProfileSwitchDialog.findViewById(R.id.normal_profile_text);
        normalProfileText.setText(mContext.getString(R.string.normal));
        FontSizeUtils.updateFontSize(normalProfileText, R.dimen.qs_tile_text_size);
        normalProfile.setOnClickListener(mProfileSwitchListener);

        View muteProfile = (View) mProfileSwitchDialog.findViewById(R.id.mute_profile);
        TextView muteProfileText =
                (TextView) mProfileSwitchDialog.findViewById(R.id.mute_profile_text);
        muteProfileText.setText(mContext.getString(R.string.mute));
        FontSizeUtils.updateFontSize(muteProfileText, R.dimen.qs_tile_text_size);
        muteProfile.setOnClickListener(mProfileSwitchListener);

        View meetingProfile = (View) mProfileSwitchDialog.findViewById(R.id.meeting_profile);
        TextView meetingProfileText =
                (TextView) mProfileSwitchDialog.findViewById(R.id.meeting_profile_text);
        meetingProfileText.setText(mContext.getString(R.string.meeting));
        FontSizeUtils.updateFontSize(meetingProfileText, R.dimen.qs_tile_text_size);
        meetingProfile.setOnClickListener(mProfileSwitchListener);

        View outdoorProfile = (View) mProfileSwitchDialog.findViewById(R.id.outdoor_profile);
        TextView outdoorProfileText =
                (TextView) mProfileSwitchDialog.findViewById(R.id.outdoor_profile_text);
        outdoorProfileText.setText(mContext.getString(R.string.outdoor));
        FontSizeUtils.updateFontSize(outdoorProfileText, R.dimen.qs_tile_text_size);
        outdoorProfile.setOnClickListener(mProfileSwitchListener);
    }

    private View.OnClickListener mProfileSwitchListener = new View.OnClickListener() {
        public void onClick(View v) {
                loadDisabledProfileResouceForAll();
                String isCommit;
                SharedPreferences.Editor editor = sharedPreferences.edit();
                int id = v.getId();
            if (id == R.id.meeting_profile) {
                isCommit = SystemProperties.get("persist.sys.iscommit", "1");
                if ("1".equals(isCommit)) {
                    commitOriginVolume(editor);
                    SystemProperties.set("persist.sys.iscommit", "0");
                }
                SystemProperties.set("persist.sys.isout", "0");
                Log.e(TAG, "poweronoff_audio = " + SystemProperties.get("persist.sys.poweronoff_audio", "100"));
                //mAudioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
                mAudioManager.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER, AudioManager.VIBRATE_SETTING_ON);
                mAudioManager.setVibrateSetting(AudioManager.VIBRATE_TYPE_NOTIFICATION, AudioManager.VIBRATE_SETTING_ON);
                mAudioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
                loadEnabledResource(R.id.meeting_profile);
            } else if (id == R.id.outdoor_profile) {
                isCommit = SystemProperties.get("persist.sys.iscommit", "1");
                SystemProperties.set("persist.sys.isout", "1");
                SystemProperties.set("persist.sys.poweronoff_audio", "1");
                fixOutBug = 1;
                //mAudioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                if ("1".equals(isCommit)) {
                    commitOriginVolume(editor);
                    SystemProperties.set("persist.sys.iscommit", "0");
                }
                mAudioManager.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER, AudioManager.VIBRATE_SETTING_ON);
                mAudioManager.setVibrateSetting(AudioManager.VIBRATE_TYPE_NOTIFICATION, AudioManager.VIBRATE_SETTING_ON);
                mAudioManager.setStreamVolume(AudioManager.STREAM_RING, mAudioManager.getStreamMaxVolume(AudioManager.STREAM_RING), AudioManager.FLAG_PLAY_SOUND);
                mAudioManager.setStreamVolume(AudioManager.STREAM_ALARM, mAudioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM), AudioManager.FLAG_PLAY_SOUND);
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), AudioManager.FLAG_PLAY_SOUND);
                mAudioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                loadEnabledResource(R.id.outdoor_profile);
            } else if (id == R.id.mute_profile) {//add BUG_ID:AQJB-795 sunshiwei 20190314 start
                isCommit = SystemProperties.get("persist.sys.iscommit", "1");
                if ("1".equals(isCommit)) {
                    commitOriginVolume(editor);
                    SystemProperties.set("persist.sys.iscommit", "0");
                }
                //add BUG_ID:AQJB-795 sunshiwei 20190314 end
                SystemProperties.set("persist.sys.isout", "0");
                //mAudioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                mAudioManager.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER, AudioManager.VIBRATE_SETTING_OFF);
                mAudioManager.setVibrateSetting(AudioManager.VIBRATE_TYPE_NOTIFICATION, AudioManager.VIBRATE_SETTING_OFF);
                //modify BUG_ID:AQJJ-410 sunshiwei 20181210 start
                //mAudioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                mController.setRingerMode(AudioManager.RINGER_MODE_SILENT, false);
                //modify BUG_ID:AQJJ-410 sunshiwei 20181210 end
                loadEnabledResource(R.id.mute_profile);
            } else if (id == R.id.normal_profile) {
                SystemProperties.set("persist.sys.iscommit", "1");
                String isout = SystemProperties.get("persist.sys.isout", "0");
                SystemProperties.set("persist.sys.isout", "0");
                //mAudioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                if ("1".equals(isout)) {
                    OriginRing = sharedPreferences.getInt("OriginRing", OriginRing1);
                    OriginAlarm = sharedPreferences.getInt("OriginAlarm", OriginAlarm1);
                    OriginMedia = sharedPreferences.getInt("OriginMedia", OriginMedia1);
                    mAudioManager.setStreamVolume(AudioManager.STREAM_RING, OriginRing, AudioManager.FLAG_PLAY_SOUND);
                    mAudioManager.setStreamVolume(AudioManager.STREAM_ALARM, OriginAlarm, AudioManager.FLAG_PLAY_SOUND);
                    mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, OriginMedia, AudioManager.FLAG_PLAY_SOUND);
                }

                mAudioManager.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER, AudioManager.VIBRATE_SETTING_OFF);
                mAudioManager.setVibrateSetting(AudioManager.VIBRATE_TYPE_NOTIFICATION, AudioManager.VIBRATE_SETTING_OFF);
                loadEnabledResource(R.id.normal_profile);
                mAudioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
            } else {
                loadEnabledResource(100);
            }
                if (mProfileSwitchDialog != null) {
                    mProfileSwitchDialog.dismiss();
                }
        }
    };

    private void commitOriginVolume(SharedPreferences.Editor editor){
        OriginRing = mAudioManager.getStreamVolume(AudioManager.STREAM_RING);
        OriginAlarm = mAudioManager.getStreamVolume(AudioManager.STREAM_ALARM);
        OriginMedia = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        editor.putInt("OriginRing", OriginRing);
        editor.putInt("OriginAlarm", OriginAlarm);
        editor.putInt("OriginMedia", OriginMedia);
        editor.commit();
    }

     private void loadEnabledResource(int id) {
         if (id == R.id.normal_profile) {
             SelectType = TYPE_NORMAL;
             mNormalProfileIcon.setImageResource(R.drawable.ic_qs_normal_profile_enable);
             mAudioState = R.drawable.ic_qs_general_on;
         } else if (id == R.id.meeting_profile) {
             SelectType = TYPE_MEETING;
             mMettingProfileIcon.setImageResource(R.drawable.ic_qs_meeting_profile_enable);
             mAudioState = R.drawable.ic_qs_meeting_on;
         } else if (id == R.id.outdoor_profile) {
             SelectType = TYPE_OUTDOOR;
             mOutdoorSwitchIcon.setImageResource(R.drawable.ic_qs_outdoor_profile_enable);
             mAudioState = R.drawable.ic_qs_outdoor_on;
         } else if (id == R.id.mute_profile) {
             SelectType = TYPE_SILENT;
             mMuteProfileIcon.setImageResource(R.drawable.ic_qs_mute_profile_enable);
             mAudioState = R.drawable.ic_qs_silent_on;
         } else {
             mAudioState = R.drawable.ic_qs_custom_on;
         }
        refreshState();
    }
    
    private void upDateSelectType(int type) {
        switch (type) {
            case TYPE_NORMAL:
                mNormalProfileIcon.setImageResource(R.drawable.ic_qs_normal_profile_enable);
                break;
            case TYPE_MEETING:
                mMettingProfileIcon.setImageResource(R.drawable.ic_qs_meeting_profile_enable);
                break;
            case TYPE_OUTDOOR:
                mOutdoorSwitchIcon.setImageResource(R.drawable.ic_qs_outdoor_profile_enable);
                break;
            case TYPE_SILENT:
                mMuteProfileIcon.setImageResource(R.drawable.ic_qs_mute_profile_enable);
                break;
        }
    }


    private void loadDisabledProfileResouceForAll() {
        if (DBG) {
            Log.d(TAG, "loadDisabledProfileResouceForAll");
        }
        mNormalProfileIcon.setImageResource(R.drawable.ic_qs_normal_off);
        mMettingProfileIcon.setImageResource(R.drawable.ic_qs_meeting_profile_off);
        mOutdoorSwitchIcon.setImageResource(R.drawable.ic_qs_outdoor_off);
        mMuteProfileIcon.setImageResource(R.drawable.ic_qs_mute_profile_off);
    }

    private void dismissProfileSwitchDialog(int timeout) {
        removeAllProfileSwitchDialogCallbacks();
        if (mProfileSwitchDialog != null) {
            mHandler.postDelayed(mDismissProfileSwitchDialogRunnable, timeout);
        }
    }

    private Runnable mDismissProfileSwitchDialogRunnable = new Runnable() {
        public void run() {
            if (DBG) {
                Log.d(TAG, "mDismissProfileSwitchDialogRunnable");
            }
            if (mProfileSwitchDialog != null && mProfileSwitchDialog.isShowing()) {
                mProfileSwitchDialog.dismiss();
            }
            removeAllProfileSwitchDialogCallbacks();
        };
    };

    private void removeAllProfileSwitchDialogCallbacks() {
        mHandler.removeCallbacks(mDismissProfileSwitchDialogRunnable);
    }


    Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case SHOW_PROFILE_SWITCH_DIALOG:
                showProfileSwitchDialog();
                break;
            case UPDATE_RINGER_MODE:
                updateRingerMode();
                break;
            default:
                break;
            }
        }
    };
     private class Receiver extends BroadcastReceiver {
        private boolean mRegistered;

        public void register(boolean register) {
            if (mRegistered == register) return;
            if (register) {
                final IntentFilter filter = new IntentFilter();
                filter.addAction(AudioManager.RINGER_MODE_CHANGED_ACTION);
                mContext.registerReceiver(this, filter);
            } else {
                mContext.unregisterReceiver(this);
            }
            mRegistered = register;
        }
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.e(TAG,"onreceive action = " + action);
            String comeFrom = intent.getStringExtra("comefrom"); 
            if (AudioManager.RINGER_MODE_CHANGED_ACTION.equals(action)) {
                    mHandler.sendEmptyMessage(UPDATE_RINGER_MODE);    
            }
        }
    }
    public void updateRingerMode(){
        loadDisabledProfileResouceForAll();
        int mode = mAudioManager.getRingerMode();
        Log.e(TAG,"mode================" + mode);
        if(mode == AudioManager.RINGER_MODE_SILENT){
            Log.e(TAG,"yhm================silent");
            SystemProperties.set("persist.sys.poweronoff_audio", "0");
            SystemProperties.set("persist.sys.iscommit", "0");
            loadEnabledResource(R.id.mute_profile);
            SystemProperties.set("persist.sys.isout", "0");
        }else if(mode == AudioManager.RINGER_MODE_VIBRATE){
            Log.e(TAG,"yhm================vibrate");
            SystemProperties.set("persist.sys.poweronoff_audio", "0");
            loadEnabledResource(R.id.meeting_profile);
            SystemProperties.set("persist.sys.isout", "0");
            SystemProperties.set("persist.sys.iscommit", "0");
        }else if(mode == AudioManager.RINGER_MODE_NORMAL ){
            String isoutstyle = SystemProperties.get("persist.sys.isout","0");
            if("1".equals(isoutstyle)){
                Log.e(TAG,"yhm================out22222");
                SystemProperties.set("persist.sys.poweronoff_audio", "1");
                loadEnabledResource(R.id.outdoor_profile);
                SystemProperties.set("persist.sys.isout", "1");
                SystemProperties.set("persist.sys.iscommit", "0");
            }else{
                Log.e(TAG,"yhm++++++================normal");
                String    out = SystemProperties.get("persist.sys.isout","0");
                if("1".equals(out)){
                    OriginRing = sharedPreferences.getInt("OriginRing",OriginRing1);
                    OriginAlarm = sharedPreferences.getInt("OriginAlarm",OriginAlarm1);
                    OriginMedia = sharedPreferences.getInt("OriginMedia",OriginMedia1);
                    mAudioManager.setStreamVolume(AudioManager.STREAM_RING, OriginRing, AudioManager.FLAG_PLAY_SOUND);
                    mAudioManager.setStreamVolume(AudioManager.STREAM_ALARM, OriginAlarm, AudioManager.FLAG_PLAY_SOUND);
                    mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, OriginMedia, AudioManager.FLAG_PLAY_SOUND);
                }
                SystemProperties.set("persist.sys.poweronoff_audio", "1");
                loadEnabledResource(R.id.normal_profile);
                SystemProperties.set("persist.sys.isout", "0");
                SystemProperties.set("persist.sys.iscommit", "1");
            }
            
        }
    }
}
