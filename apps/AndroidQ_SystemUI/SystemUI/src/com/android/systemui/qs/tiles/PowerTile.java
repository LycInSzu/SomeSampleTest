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

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.service.quicksettings.Tile;
import android.widget.Switch;
import com.android.systemui.Dependency;
import com.android.systemui.R;
import com.android.systemui.qs.QSHost;
import com.android.systemui.plugins.qs.QSTile.BooleanState;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import com.android.systemui.qs.tileimpl.QSTileImpl.ResourceIcon;
import com.android.systemui.plugins.ActivityStarter;

/** Quick settings tile: CHENYEE Power **/
public class PowerTile extends QSTileImpl<BooleanState> {
	
	static final String PKG = "com.cydroid.softmanager";
    static final String CLS = "com.cydroid.softmanager.powersaver.activities.PowerManagerMainActivity";
    static final Intent INTENT = new Intent("cydroid.intent.action.SUPERMODE_OPEN");
	private final Icon mIcon = ResourceIcon.get(R.drawable.ic_qs_power);
	private final ActivityStarter mActivityStarter;
    private Intent mPowerIntent;
    
    public PowerTile(QSHost host) {
        super(host);
        mActivityStarter = Dependency.get(ActivityStarter.class);
    }

    @Override
    public BooleanState newTileState() {
        return new BooleanState();
    }

    @Override
    public void handleSetListening(boolean listening) {
        //DON'T CARE
    }


    @Override
    public Intent getLongClickIntent() {
        return null;
    }

    @Override
    protected void handleClick() {
    	try {
    		if(INTENT.resolveActivity(mContext.getPackageManager())!=null){
    			mActivityStarter.postStartActivityDismissingKeyguard(INTENT, 0);
        	}
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

    private Intent getPowerIntent() {
    	if(mPowerIntent == null){
    		mPowerIntent = new Intent();
    		mPowerIntent.setClassName(PKG,CLS);
    	}
		return mPowerIntent;
	}
    
    @Override
    protected void handleLongClick() {
    	try {
    		Intent intent = getPowerIntent();
    		if(intent.resolveActivity(mContext.getPackageManager())!=null){
    			mActivityStarter.postStartActivityDismissingKeyguard(intent, 0);
        	}
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
	
    @Override
    public CharSequence getTileLabel() {
        return mContext.getString(R.string.quick_settings_power_label);
    }

    @Override
    protected void handleUpdateState(BooleanState state, Object arg) {
        state.value = true;
        state.label = mContext.getString(R.string.quick_settings_power_label);
        state.icon = mIcon;
        state.expandedAccessibilityClassName = Switch.class.getName();
        state.contentDescription = state.label;
        state.state = Tile.STATE_INACTIVE;
    }

    @Override
    public int getMetricsCategory() {
        return -1;
    }

}
