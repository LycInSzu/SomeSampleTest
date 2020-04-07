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
 * limitations under the License
 */

package com.android.systemui.qs.tiles;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.Settings;
import android.service.quicksettings.Tile;
import android.widget.Switch;
import android.widget.Toast;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.systemui.R;
import com.android.systemui.plugins.qs.QSTile.BooleanState;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import com.android.systemui.statusbar.policy.FlashlightController;

import javax.inject.Inject;
import com.android.systemui.SystemUIApplication;

/** Quick settings tile: Control flashlight **/
public class FlashlightTile extends QSTileImpl<BooleanState> implements
        FlashlightController.FlashlightListener {
    // A: Bug_id:CQYJ-225 chenchunyong 20180727 {
    private final int MSG_CHECK_BATTERY = 222;
    // A: }

    //A: Bug_id:XWELY-282 chenchunyong 20170104 {
    private static final String FLASH_LIGHT_STATE = "flash_light_state";
    private static final String SOS_STATE = "sos_state";
    private Toast mToast;
    private ContentObserver mObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            boolean newState = Settings.Global.getInt(mContext.getContentResolver(),
                    FLASH_LIGHT_STATE, 0) == 1;
            refreshState(newState);
            mFlashlightController.setFlashlight(newState);
        };
    };
    //A: }

    private final Icon mIcon = ResourceIcon.get(com.android.internal.R.drawable.ic_qs_flashlight);
    private final FlashlightController mFlashlightController;

    @Inject
    public FlashlightTile(QSHost host, FlashlightController flashlightController) {
        super(host);

        //A: Bug_id:XWELY-282 chenchunyong 20170104 {
        Uri uri = Settings.Global.getUriFor(FLASH_LIGHT_STATE);
        mContext.getContentResolver().registerContentObserver(uri, true, mObserver);
        //A: }

        mFlashlightController = flashlightController;
        mFlashlightController.observe(getLifecycle(), this);
    }

    @Override
    protected void handleDestroy() {
        super.handleDestroy();
        //A: Bug_id:XWELY-282 chenchunyong 20170104 {
        mContext.getContentResolver().unregisterContentObserver(mObserver);
        //A: }

    }

    @Override
    public BooleanState newTileState() {
        BooleanState state = new BooleanState();
        state.handlesLongClick = false;
        return state;
    }

    @Override
    public void handleSetListening(boolean listening) {
    }

    @Override
    protected void handleUserSwitch(int newUserId) {
    }

    @Override
    public Intent getLongClickIntent() {
        return new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);
    }

    @Override
    public boolean isAvailable() {
        return mFlashlightController.hasFlashlight();
    }

    @Override
    protected void handleClick() {
        // M: Bug_id:XBLWY-162 chenchunyong 20170718 {
        if (ActivityManager.isUserAMonkey() || !mFlashlightController.isAvailable()) {
        //if (ActivityManager.isUserAMonkey()) {
        // M: }
            return;
        }
        boolean newState = !mState.value;
             
        // A: Bug_id:CQYJ-225 chenchunyong 20180727 {
        if (mContext.getResources().getBoolean(R.bool.config_show_lowdattery_flashlight_toast) && isLowBattery() && newState) {
            if(mToast == null){
                 mToast = Toast.makeText(mContext, mContext.getString(R.string.lowdattery_flashlight_disable), Toast.LENGTH_SHORT);
            }else {
                mToast.setText(mContext.getString(R.string.lowdattery_flashlight_disable));
            }
            mToast.show();
            return;
        }
        // A: }

        refreshState(newState);
        mFlashlightController.setFlashlight(newState);
        //A: Bug_id:XWELY-282 chenchunyong 20170104 {
        if (Settings.Global.getInt(mContext.getContentResolver(), SOS_STATE, 0) == 0) {
            Settings.Global.putInt(mContext.getContentResolver(),
                    FLASH_LIGHT_STATE,newState ? 1 : 0);
        }
        //A: }
    }

    @Override
    public CharSequence getTileLabel() {
        return mContext.getString(R.string.quick_settings_flashlight_label);
    }

    @Override
    protected void handleLongClick() {
        handleClick();
    }

    @Override
    protected void handleUpdateState(BooleanState state, Object arg) {
        if (state.slash == null) {
            state.slash = new SlashState();
        }
        state.label = mHost.getContext().getString(R.string.quick_settings_flashlight_label);
        //M: Bug_id:XWELY-282 chenchunyong 20170104 {
        //if (!mFlashlightController.isAvailable()) {
        if (!mFlashlightController.isAvailable() ||
                Settings.Global.getInt(mContext.getContentResolver(), SOS_STATE, 0) == 1) {
        // M: }
            //modify for EJQQQ-80 by liyuchong 20191204 begin
            //state.icon = mIcon;
            if (SystemUIApplication.useDgCustomUI||SystemUIApplication.useDkCustomUI) {
                state.icon = ResourceIcon.get(R.drawable.ic_flash_off);
            }else if(SystemUIApplication.useBluCustomUI){
                state.icon = ResourceIcon.get(R.drawable.ic_qs_flashlight_blu);
            }else {
                state.icon = mIcon;
            }
            //modify for EJQQQ-80 by liyuchong 20191204 end
            state.slash.isSlashed = true;
            state.contentDescription = mContext.getString(
                    R.string.accessibility_quick_settings_flashlight_unavailable);
            state.state = Tile.STATE_UNAVAILABLE;
            return;
        }
        if (arg instanceof Boolean) {
            boolean value = (Boolean) arg;
            if (value == state.value) {
                return;
            }
            state.value = value;
        } else {
            state.value = mFlashlightController.isEnabled();
        }
        //modify for EJQQQ-80 by liyuchong 20191204 begin
        //state.icon = mIcon;
        if (SystemUIApplication.useDgCustomUI||SystemUIApplication.useDkCustomUI) {
            state.icon = ResourceIcon.get(state.value ? R.drawable.ic_flash_on : R.drawable.ic_flash_off);
        } else {
            state.icon = mIcon;
        }
        //modify for EJQQQ-80 by liyuchong 20191204 end
        state.slash.isSlashed = !state.value;
        state.contentDescription = mContext.getString(R.string.quick_settings_flashlight_label);
        state.expandedAccessibilityClassName = Switch.class.getName();
        state.state = state.value ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.QS_FLASHLIGHT;
    }

    @Override
    protected String composeChangeAnnouncement() {
        if (mState.value) {
            return mContext.getString(R.string.accessibility_quick_settings_flashlight_changed_on);
        } else {
            return mContext.getString(R.string.accessibility_quick_settings_flashlight_changed_off);
        }
    }

    @Override
    public void onFlashlightChanged(boolean enabled) {
        //A: Bug_id:XWELY-282 chenchunyong 20170104 {
        if (Settings.Global.getInt(mContext.getContentResolver(), SOS_STATE, 0) == 0) {
            Settings.Global.putInt(mContext.getContentResolver(),
                    FLASH_LIGHT_STATE,enabled ? 1 : 0);
        }
        //A: }

        // A: Bug_id:CQYJ-225 chenchunyong 20180727 {
        if (enabled) {
            mHandler.sendEmptyMessage(MSG_CHECK_BATTERY);
        } else {
            mHandler.removeMessages(MSG_CHECK_BATTERY);
        }
        // A: }
        
        refreshState(enabled);
    }

    @Override
    public void onFlashlightError() {
        refreshState(false);
    }

    @Override
    public void onFlashlightAvailabilityChanged(boolean available) {
        refreshState();
    }
    
    // A: Bug_id:CQYJ-225 chenchunyong 20180727 {
    private boolean isLowBattery() {
        BatteryManager batteryManager = (BatteryManager)mContext.getSystemService(Context.BATTERY_SERVICE);
        int battery = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        if(battery <= 15) {
            return true;
        }
        return false;
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == MSG_CHECK_BATTERY) {
                if (isLowBattery()){
                    refreshState(false);
                    mFlashlightController.setFlashlight(false);
                }
                mHandler.removeMessages(MSG_CHECK_BATTERY);
                mHandler.sendEmptyMessageDelayed(MSG_CHECK_BATTERY, 1500);
            }
        }
    };
    // A: }
}
