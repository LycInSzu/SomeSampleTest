package com.cydroid.screenrecorder;

import android.text.TextUtils;
import android.os.RemoteException;
import android.os.SystemProperties;

/**
 * @author lixh
 * @since 2017-08-04
 */
public class Constant {
	public static final boolean CUSTOMER_INDIA =  SystemProperties.get("ro.cy.custom").equals("INDIA_GIONEE");
	public static final boolean CUSTOMER_VISUALFAN = SystemProperties.get("ro.cy.custom").equals("VISUALFAN");
	public static final boolean HAS_NAVIGATIONBAR = hasNavigationBar();
	///////////////////////////////////////////////////////////////////
	public static final String  POWER_MODE_SETTING = "cyee_powermode";
	public static final int POWER_MODE_NORMAL = 0;
    public static final int POWER_MODE_GENERAL = 1;
    public static final int POWER_MODE_EXTREME = 2;
    ///////////////////////////////////////////////////////////////////
    public static final int TIMEOUT = 3000;//3 seconds
    public static final boolean DEBUG = false;
	private static boolean  isSupportETN() {
    	String version = SystemProperties.get("ro.gn.gnznvernumber");
    	if(!TextUtils.isEmpty(version)){
    		if(version.startsWith("SW17W08")){
    			return true;
    		}
    	}
        return false;
    }
	
	private static boolean hasNavigationBar(){
		try{
			return android.view.WindowManagerGlobal.getWindowManagerService().hasNavigationBar();
		} catch (RemoteException ex) {
	       ex.printStackTrace();
	    }
		return false;
	}
}
