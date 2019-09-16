package com.cydroid.ota.utils;

import android.content.Context;
import android.os.SystemProperties;
import android.telephony.TelephonyManager;
import com.cyee.utils.DecodeUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.cydroid.ota.Log;
//Chenyee <CY_bug> <xuyongji> <20171212> modify for SW17W16A-2470 begin
import android.Manifest;
import android.content.pm.PackageManager;
//Chenyee <CY_bug> <xuyongji> <20171212> modify for SW17W16A-2470 end

/**
 * Created by borney on 14-10-16.
 */
public class SystemPropertiesUtils {
    public static final int ANDROID_SDK_LEVEL = SystemProperties.getInt("ro.build.version.sdk", 3);
    public static final boolean IS_LOW_ANDROID_SDK_LEVEL = (ANDROID_SDK_LEVEL < 11);
    public static String mMEID = SystemProperties.get("persist.radio.meid");
    public static String mIMEI1 = SystemProperties.get("persist.radio.imei");
    public static String mIMEI2 = SystemProperties.get("persist.radio.imei1");
    public static String sCustom = SystemProperties.get("ro.cy.custom");
    //Chenyee <CY_Bug> <xuyongji> <20171212> modify for SW17W16A-2460 begin
    public static final String NOTIFICATION_CHANNEL_ID_OTA = "defaultNoti";
    public static final String NOTIFICATION_CHANNEL_NAME_OTA = "System Update";
    public static final int NOTIFICATION_ID = 1;
	//Chenyee <CY_Bug> <xuyongji> <20171212> modify for SW17W16A-2460 end

    public static String getProp(String propName, String def) {
        return SystemProperties.get(propName, def);
    }

    public static String getCurrentVersion() {
        String currentVersion = "";
        currentVersion =  getProp("ro.cy.vernumber", "");
        currentVersion = getVersionNum(currentVersion);
        return currentVersion;
    }
    
    public static String getVersionNum(String version) {
        if ((null != version) && !("".equals(version))) {
            String[] gnvernumbers = version.split("_");
            String splitGnvernumbers = "";
            if (gnvernumbers != null && gnvernumbers.length > 0) {
				//Chenyee <CY_Bug> <xuyongji> <20161222> modify for CSW1702A-1008 begin
                splitGnvernumbers += gnvernumbers[gnvernumbers.length - 1];
				//Chenyee <CY_Bug> <xuyongji> <20161222> modify for CSW1702A-1008 end
                version = splitGnvernumbers;
            }
        }
        try {
            version = URLEncoder.encode(version, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return version;
    }

    /**
     * get gionee internal number
     *
     * @return internal number
     */
    public static String getInternalVersion() {
        return getProp("ro.cy.znvernumber", "");
    }

    public static String getModel() {
        return getProp("ro.product.model", "");
    }

    public static String getAndroidVersion() {
        return getProp("ro.build.version.release", "");
    }

    public static String getGioneeRomProp() {
        return getProp("ro.cy.rom.vernumber", "");
    }

    public static String getBuildTime() {
        return getProp("ro.build.date.utc", "0");
    }

    public static String getfingerPrint() {
        return getProp("ro.build.fingerprint", "");
    }
    
    //Chenyee <CY_Bug> <xuyongji> <20171109> modify for SW17W16A-305 begin
    public static String getDeviceName() {
        return getProp("ro.product.device", "");
    }
    //Chenyee <CY_Bug> <xuyongji> <20171109> modify for SW17W16A-305 end

    public static String getMtkRelease() {
        return getProp("ro.mediatek.version.release", "");
    }

    public static String getMtkGemini() {
        return getProp("ro.mediatek.gemini_support", "");
    }

    public static String getQcomMultisim() {
        return getProp("persist.multisim.config", "");
    }
    public static boolean getImitateTCard() {
        return  SystemProperties.getBoolean("ro.gn.emulated.storage", false);
    }
    public static String getPlatform() {
        if (isMtkPlatform()) {
            return getProp("ro.mediatek.platform", "");

        } else if (isQcomPlatform()) {
            return getProp("ro.hw_platform", "");

        } else {
            return "";
        }
    }

    public static String getGioneeRomVersion() {
        String gioneeRomPropString = getGioneeRomProp();
        int index = getFirstNumIndex(gioneeRomPropString);
        return gioneeRomPropString.substring(index);
    }

    public static int getFirstNumIndex(String string) {
        String regex = "\\d";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(string);
        int index = 0;
        if (matcher.find() && !"".equals(matcher.group())) {
            index = matcher.start();
        }
        return index;
    }

    public static boolean isMtkPlatform() {
        String mtkRelease = getMtkRelease();
        return mtkRelease != null && !mtkRelease.equals("");
    }

    /*
     * need modify after
     */
    public static boolean isQcomPlatform() {
        if (isMtkPlatform()) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * get encrypted imei
     *
     * @param imei
     * @return encrypted imei
     */
    public static String getEncryptionImei(String imei) {
        return DecodeUtils.get(imei);
    }

    /**
     * get imei of phone
     *
     * @param context
     * @return imei
     */
    public static String getImei(Context context) {
        String imei = "";
        imei = getIMEI(context,0);
        if (imei == null || imei.equals(""))  {
            imei = getIMEI(context,1);
        }
        return imei;
    }
    public  static String getIMEI(Context context,int simId) {
        String imei = "";
		//Chenyee <CY_bug> <xuyongji> <20171212> modify for SW17W16A-2470 begin
        if (PackageManager.PERMISSION_DENIED == context.getApplicationContext().checkCallingOrSelfPermission(Manifest.permission.READ_PHONE_STATE)) {
        	return imei;
        }
		//Chenyee <CY_bug> <xuyongji> <20171212> modify for SW17W16A-2470 end
        if (context != null) {          
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (tm != null) {               
                if (simId == 0) {           
                    if (mIMEI1 == null || mIMEI1.equals("")) {
                        mIMEI1 = tm.getImei(simId);
                    }
                    return mIMEI1;
                }
                if (simId == 1) {           
                    if (mIMEI2 == null || mIMEI2.equals("")) {
                        mIMEI2 = tm.getImei(simId);
                    }
                    return mIMEI2;
                }
                imei = tm.getImei(simId);
            }
        }
        return imei;
    }
    
     public static boolean getBlueStyle() {

        if (sCustom.equals("ALGERIA_CONDOR")) {
            return true;
        }
        if (sCustom.equals("ARGENTINA_SOLNIK")) {
            return true;
        }
        return false;
    }

    //Chenyee <CY_Req> <xuyongji> <20180930> modify for CSW1803DP-43 begin
	public static boolean isDPFlag() {
        return sCustom.equals("ECUADOR_DOPPIO");
    }
	//Chenyee <CY_Req> <xuyongji> <20180930> modify for CSW1803DP-43 end
}
