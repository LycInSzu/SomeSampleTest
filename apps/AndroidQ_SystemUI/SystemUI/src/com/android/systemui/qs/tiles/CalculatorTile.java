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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.service.quicksettings.Tile;
import android.widget.Switch;
import com.android.systemui.Dependency;
import com.android.systemui.R;
import com.android.systemui.qs.QSHost;
import com.android.systemui.plugins.qs.QSTile.BooleanState;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import com.android.systemui.qs.tileimpl.QSTileImpl.ResourceIcon;
import com.android.systemui.plugins.ActivityStarter;

/** Quick settings tile: CHENYEE Calculator **/
public class CalculatorTile extends QSTileImpl<BooleanState> {
	//com.android.calculator2/.Calculator
	static final String CALCULATOR_PKG = "com.android.calculator2";
    static final String GOOGLE_CALCULATOR_PKG = "com.google.android.calculator";
    static final String  CLS_PM = "android.content.pm.PackageManager";    
	private final Icon mIcon = ResourceIcon.get(R.drawable.ic_qs_calculator);
	private final ActivityStarter mActivityStarter;
    private PackageManager mPm;
    
    public CalculatorTile(QSHost host) {
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
    protected void handleClick() {
    	if(isAvailable(CALCULATOR_PKG)){
        	launchAppByPkg(CALCULATOR_PKG);
        }else if(isAvailable(GOOGLE_CALCULATOR_PKG)){
        	launchAppByPkg(GOOGLE_CALCULATOR_PKG);
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
        return mContext.getString(R.string.quick_settings_calculator_label);
    }

    @Override
    protected void handleUpdateState(BooleanState state, Object arg) {
        state.value = true;
        state.label = mContext.getString(R.string.quick_settings_calculator_label);
        state.icon = mIcon;
        state.expandedAccessibilityClassName = Switch.class.getName();
        state.contentDescription = state.label;
        state.state = Tile.STATE_INACTIVE;
    }

    @Override
    public int getMetricsCategory() {
        return -1;
    }

	public Object invokeSingleParameterMethod(String className,Object receiver,String methodName,Class<?> parameterType,Object arg){
		try {
			Class<?> cls = Class.forName(className);
			Method mMethod = cls.getDeclaredMethod(methodName,parameterType);
			if(mMethod!=null && receiver!=null){
				mMethod.setAccessible(true);
				return mMethod.invoke(receiver,arg);
			}
		} catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NullPointerException | SecurityException e) {
            android.util.Log.d(TAG, "invokeSingleParameterMethod occur Exception!");
		}
		
		return null;
	}
	
	private void launchAppByPkg(String pkg){
		try {
			Object obj = invokeSingleParameterMethod(CLS_PM, getPackageManager(), "getLaunchIntentForPackage", String.class, pkg);
			if(obj!=null){
				mActivityStarter.postStartActivityDismissingKeyguard((Intent)obj, 0);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
    
	private PackageManager getPackageManager(){
    	if(mPm == null){
    		mPm = mContext.getPackageManager();
    	}
    	return mPm;
    }
    
    private boolean isAvailable(String pkg){   	
		 try {
			PackageInfo mPI = getPackageManager().getPackageInfo(pkg, 0);
			return mPI!=null;
		} catch (NameNotFoundException e) {
             android.util.Log.d(TAG, "The app of pkg[" + pkg + "] is not installed.");
		}
		 return false;
	}
}
