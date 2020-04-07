/*
 * Copyright (c) 2016, The Android Open Source Project
 * Contributed by the Paranoid Android Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.systemui.qs.tiles;

import static android.media.AudioManager.RINGER_MODE_NORMAL;
import static android.media.AudioManager.RINGER_MODE_SILENT;
import static android.media.AudioManager.RINGER_MODE_VIBRATE;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Looper;
import android.os.Handler;
import android.os.VibrationEffect;
import android.service.quicksettings.Tile;
import android.widget.Switch;
import com.android.systemui.Dependency;
import com.android.systemui.Prefs;
import com.android.systemui.R;
import com.android.systemui.SysUIToast;
import com.android.systemui.qs.QSHost;
import com.android.systemui.plugins.qs.QSTile.BooleanState;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import com.android.systemui.qs.tileimpl.QSTileImpl.ResourceIcon;
import com.android.systemui.volume.VolumePrefs;
import com.android.settingslib.Utils;
import android.provider.Settings;
import android.provider.Settings.Global;
import com.android.systemui.plugins.VolumeDialogController;
import com.android.systemui.plugins.VolumeDialogController.State;
import com.android.systemui.plugins.VolumeDialogController.StreamState;
/**
 * Quick settings tile: CHENYEE control voice,use google design
 * @author lixh
 * @since 20180901
 */
public class VoiceTile extends QSTileImpl<BooleanState> {
	
	static final Intent VOICE_SETTINGS = new Intent(Settings.ACTION_SOUND_SETTINGS);
	private final Icon mDIcon = ResourceIcon.get(R.drawable.ic_qs_voice_normal);
	private final VolumeDialogController mController;
	private final Handler mHandler = new Handler(Looper.getMainLooper());
    private com.android.systemui.plugins.VolumeDialogController.State mVState;
    
    public VoiceTile(QSHost host) {
        super(host);
        mController = Dependency.get(VolumeDialogController.class);
    }

    @Override
    public BooleanState newTileState() {
        return new BooleanState();
    }

    @Override
    public void handleSetListening(boolean listening) {
        if(listening){
        	mController.addCallback(mCallback, mHandler);
        }else{
        	mController.removeCallback(mCallback);
        }
    }

    @Override
    public Intent getLongClickIntent() {
        return VOICE_SETTINGS;
    }

    @Override
    protected void handleClick() {
    	final StreamState ss = mVState.states.get(AudioManager.STREAM_RING);
        if (ss == null) {
            return;
        }
    	int newRingerMode;
        final boolean hasVibrator = mController.hasVibrator();
        if (mVState.ringerModeInternal == AudioManager.RINGER_MODE_NORMAL) {
            if (hasVibrator) {
                newRingerMode = AudioManager.RINGER_MODE_VIBRATE;
            } else {
                newRingerMode = AudioManager.RINGER_MODE_SILENT;
            }
        } else if (mVState.ringerModeInternal == AudioManager.RINGER_MODE_VIBRATE) {
            newRingerMode = AudioManager.RINGER_MODE_SILENT;
        } else {
            newRingerMode = AudioManager.RINGER_MODE_NORMAL;
            if (ss.level == 0) {
                mController.setStreamVolume(AudioManager.STREAM_RING, 1);
            }
        }
        provideTouchFeedbackH(newRingerMode);
        mController.setRingerMode(newRingerMode, false);
        maybeShowToastH(newRingerMode);
    }
	
    @Override
    public CharSequence getTileLabel() {
        return mContext.getString(R.string.quick_settings_sound_label);
    }

    @Override
    protected void handleUpdateState(BooleanState state, Object arg) {
    	state.state = Tile.STATE_ACTIVE;
    	state.icon = mDIcon;
    	state.label = mContext.getString(R.string.quick_settings_sound_label);
    	state.contentDescription = state.label;
    	state.expandedAccessibilityClassName = Switch.class.getName();
    	if(mVState == null){
    	    return;
    	}
    	
    	final StreamState ss = mVState.states.get(AudioManager.STREAM_RING);
        if (ss == null) {
            return;
        }
        boolean isZenMuted = mVState.zenMode == Global.ZEN_MODE_ALARMS
                || mVState.zenMode == Global.ZEN_MODE_NO_INTERRUPTIONS
                || (mVState.zenMode == Global.ZEN_MODE_IMPORTANT_INTERRUPTIONS
                    && mVState.disallowRinger);
        switch (mVState.ringerModeInternal) {
        case AudioManager.RINGER_MODE_VIBRATE:
        	state.icon = ResourceIcon.get(R.drawable.ic_qs_voice_vibrate);
        	state.label = mContext.getString(R.string.quick_settings_vibrate_label);
        	state.contentDescription = mContext.getString(R.string.volume_ringer_hint_mute);
            break;
        case AudioManager.RINGER_MODE_SILENT:
        	state.icon = ResourceIcon.get(R.drawable.ic_qs_voice_mute);
            state.label = mContext.getString(R.string.quick_settings_silent_label);
        	state.contentDescription = mContext.getString(R.string.volume_ringer_hint_unmute);
            break;
        case AudioManager.RINGER_MODE_NORMAL:
        default:
            boolean muted = (/*mAutomute && */ss.level == 0) || ss.muted;
            if (!isZenMuted && muted) {
            	state.icon = ResourceIcon.get(R.drawable.ic_qs_voice_mute);
            	state.label = mContext.getString(R.string.quick_settings_silent_label);
            	state.contentDescription = mContext.getString(R.string.volume_ringer_hint_unmute);
            } else {
                if (mController.hasVibrator()) {
                    state.contentDescription = mContext.getString(R.string.volume_ringer_hint_vibrate);
                } else {
                    state.contentDescription = mContext.getString(R.string.volume_ringer_hint_mute);
                }
            }
            break;
        }  
    }

    @Override
    public int getMetricsCategory() {
        return -1;
    }

    private void onStateChangedH(com.android.systemui.plugins.VolumeDialogController.State state){
    	if (mVState != null && state != null
                && mVState.ringerModeInternal != state.ringerModeInternal
                && state.ringerModeInternal == AudioManager.RINGER_MODE_VIBRATE) {
            mController.vibrate(VibrationEffect.get(VibrationEffect.EFFECT_HEAVY_CLICK));
        }
    	mVState = state;
    	refreshState();
    }
    
    private void provideTouchFeedbackH(int newRingerMode) {
        VibrationEffect effect = null;
        switch (newRingerMode) {
            case RINGER_MODE_NORMAL:
                mController.scheduleTouchFeedback();
                break;
            case RINGER_MODE_SILENT:
                effect = VibrationEffect.get(VibrationEffect.EFFECT_CLICK);
                break;
            case RINGER_MODE_VIBRATE:
            default:
                effect = VibrationEffect.get(VibrationEffect.EFFECT_DOUBLE_CLICK);
        }
        if (effect != null) {
            mController.vibrate(effect);
        }
    }
    
    private void maybeShowToastH(int newRingerMode) {
        int seenToastCount = Prefs.getInt(mContext, Prefs.Key.SEEN_RINGER_GUIDANCE_COUNT, 0);

        if (seenToastCount > VolumePrefs.SHOW_RINGER_TOAST_COUNT) {
            return;
        }
        CharSequence toastText = null;
        switch (newRingerMode) {
            case RINGER_MODE_NORMAL:
                final StreamState ss = mVState.states.get(AudioManager.STREAM_RING);
                if (ss != null) {
                    toastText = mContext.getString(R.string.volume_dialog_ringer_guidance_ring,Utils.formatPercentage(ss.level, ss.levelMax));
                }
                break;
            case RINGER_MODE_SILENT:
                toastText = mContext.getString(com.android.internal.R.string.volume_dialog_ringer_guidance_silent);
                break;
            case RINGER_MODE_VIBRATE:
            default:
                toastText = mContext.getString(com.android.internal.R.string.volume_dialog_ringer_guidance_vibrate);
        }
        if(toastText!=null){
            SysUIToast.showToast(mContext, toastText);
        }
        seenToastCount++;
        Prefs.putInt(mContext, Prefs.Key.SEEN_RINGER_GUIDANCE_COUNT, seenToastCount);
    }
    
	private final VolumeDialogController.Callbacks mCallback = new VolumeDialogController.Callbacks() {
		@Override
		public void onShowRequested(int reason) {}

		@Override
		public void onDismissRequested(int reason) {}

		@Override
		public void onScreenOff() {}

		@Override
		public void onStateChanged(com.android.systemui.plugins.VolumeDialogController.State state) {
			onStateChangedH(state);
		}

		@Override
		public void notifyStateChanged(com.android.systemui.plugins.VolumeDialogController.State state){
			onStateChangedH(state);
		}
		
		@Override
		public void onLayoutDirectionChanged(int layoutDirection) {}

		@Override
		public void onConfigurationChanged() {}

		@Override
		public void onShowVibrateHint() {}

		@Override
		public void onShowSilentHint() {}

		@Override
		public void onShowSafetyWarning(int flags) {}

		@Override
		public void onAccessibilityModeChanged(Boolean showA11yStream) {}

        @Override
        public void onCaptionComponentStateChanged(Boolean isComponentEnabled, Boolean fromTooltip) {}
	};
	
}
