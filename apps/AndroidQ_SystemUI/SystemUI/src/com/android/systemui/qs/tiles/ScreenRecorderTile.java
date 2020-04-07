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

import java.util.List;
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
import com.android.systemui.statusbar.policy.KeyguardMonitor;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.statusbar.phone.UnlockMethodCache;

/** Quick settings tile: ScreenRecorder **/
public class ScreenRecorderTile extends QSTileImpl<BooleanState> {
	
	final static String PKG = "com.cydroid.screenrecorder";
    final static String SERVICE = "com.cydroid.screenrecorder.ScreenRecorderService";
	private final Icon mIcon = ResourceIcon.get(R.drawable.ic_qs_screen_recorder);
	private final ActivityStarter mActivityStarter;
    private final KeyguardMonitor mKeyguard;
    
    public ScreenRecorderTile(QSHost host) {
        super(host);
        mKeyguard = Dependency.get(KeyguardMonitor.class);
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
    protected void handleClick() {
    	if (mKeyguard.isSecure() && !UnlockMethodCache.getInstance(mContext).canSkipBouncer()) {
            mActivityStarter.postQSRunnableDismissingKeyguard(this::launchScreenRecorder);
            return;
        }
    	launchScreenRecorder();
    }

    @Override
    public Intent getLongClickIntent() {
        return null;
    }
    
    @Override
    protected void handleLongClick() {
    	handleClick();
    }

    private void startScreenRecorderService(){
    	try {
			Intent intent = new Intent();
			intent.setClassName(PKG, SERVICE);
			if(isSafesServiceIntent(intent)){
			    mContext.startService(intent);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    protected void launchScreenRecorder(){
    	startScreenRecorderService();
        mHost.forceCollapsePanels();
	}
	
	private boolean isSafesServiceIntent(Intent mServiceIntent){
    	PackageManager packageManager = mContext.getPackageManager();
    	List<ResolveInfo> services = packageManager.queryIntentServices(mServiceIntent, 0);
    	return services.size() > 0;
    }
	
    @Override
    public CharSequence getTileLabel() {
        return mContext.getString(R.string.quick_settings_screenrecorder_label);
    }

    @Override
    protected void handleUpdateState(BooleanState state, Object arg) {
        state.value = true;
        state.label = mContext.getString(R.string.quick_settings_screenrecorder_label);
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
