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

import java.util.Locale;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.provider.AlarmClock;
import android.service.quicksettings.Tile;
import android.text.format.DateFormat;
import android.widget.Switch;
import com.android.systemui.Dependency;
import com.android.systemui.R;
import com.android.systemui.qs.QSHost;
import com.android.systemui.plugins.qs.QSTile.BooleanState;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import com.android.systemui.qs.tileimpl.QSTileImpl.ResourceIcon;
import com.android.systemui.statusbar.policy.NextAlarmController;
import com.android.systemui.plugins.ActivityStarter;

/** Quick settings tile: CHENYEE Alarm **/
public class AlarmTile extends QSTileImpl<BooleanState> implements NextAlarmController.NextAlarmChangeCallback{
	
	private final Icon mIcon = ResourceIcon.get(R.drawable.ic_qs_alarm);
	private final ActivityStarter mActivityStarter;
    private NextAlarmController mNextAlarmController;
    private AlarmManager.AlarmClockInfo mNextAlarm;
    private boolean mAlarmEnabled;
    private String mAlarmText;
    
    public AlarmTile(QSHost host) {
        super(host);
        mActivityStarter = Dependency.get(ActivityStarter.class);
        mNextAlarmController = Dependency.get(NextAlarmController.class);
    }

    @Override
    public BooleanState newTileState() {
        return new BooleanState();
    }

    @Override
    public void handleSetListening(boolean listening) {
    	if (listening) {
        	mNextAlarmController.addCallback(this);
        } else {
        	mNextAlarmController.removeCallback(this);
        }
    }

    @Override
    protected void handleClick() {
    	if (mNextAlarm != null) {
            PendingIntent showIntent = mNextAlarm.getShowIntent();
            mActivityStarter.postStartActivityDismissingKeyguard(showIntent);
        } else {
            mActivityStarter.postStartActivityDismissingKeyguard(new Intent(AlarmClock.ACTION_SHOW_ALARMS), 0);
        }
    }

    @Override
    public Intent getLongClickIntent() {
        return null;
    }
    
    @Override
    protected void handleLongClick() {
    	handleClick();
    }

    @Override
    public CharSequence getTileLabel() {
    	if(mAlarmEnabled){
    		return mAlarmText;
    	}
        return mContext.getString(R.string.quick_settings_alarm_label);
    }

    @Override
    protected void handleUpdateState(BooleanState state, Object arg) {
        state.value = mAlarmEnabled;
        state.label = state.value?mAlarmText:mContext.getString(R.string.quick_settings_alarm_label);
        state.icon = mIcon;
        state.expandedAccessibilityClassName = Switch.class.getName();
        state.contentDescription = mContext.getString(R.string.quick_settings_alarm_label);
        state.state = state.value ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE;
    }

    @Override
    public int getMetricsCategory() {
        return -1;
    }
    
    @Override
    public void onNextAlarmChanged(AlarmManager.AlarmClockInfo nextAlarm){
    	//refresh now
    	mNextAlarm = nextAlarm;
    	mAlarmEnabled = (mNextAlarm!=null) && (mNextAlarm.getTriggerTime() > 0);
    	if(mNextAlarm!=null){
	        mAlarmText = formatNextAlarm(mContext, mNextAlarm);
    	}
    	refreshState();
    }
    
    private String formatNextAlarm(Context context, AlarmManager.AlarmClockInfo info) {
        if (info == null) {
            return "";
        }
        String skeleton = DateFormat.is24HourFormat(context, ActivityManager.getCurrentUser())
                ? "EHm"
                : "Ehma";
        String pattern = DateFormat.getBestDateTimePattern(Locale.getDefault(), skeleton);
        return DateFormat.format(pattern, info.getTriggerTime()).toString();
    }
}
