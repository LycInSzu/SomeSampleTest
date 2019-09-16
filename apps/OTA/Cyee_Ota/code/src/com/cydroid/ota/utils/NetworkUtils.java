package com.cydroid.ota.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import com.cydroid.ota.Log;
import com.cydroid.ota.R;
import com.cydroid.ota.logic.config.NetConfig;
import com.cydroid.ota.storage.IStorage;
import com.cydroid.ota.storage.Key;
import com.cydroid.ota.storage.SettingUpdateDataInvoker;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by liuyanfeng on 15-6-8.
 */
public class NetworkUtils {
    private static final String TAG = "NetworkUtils";
    public static final boolean GEMINI_SUPPORT = isGeminiSupport();

   public static boolean isNetCanUse(Context context){
        if (NetworkUtils.isWIFIConnection(context)) {
            IStorage wlanAutoStorage = SettingUpdateDataInvoker
                    .getInstance(context).wlanAutoStorage();
			//Chenyee <CY_Req> <xuyongji> <20180822> modify for SW17W16SE-128 begin		
            return  wlanAutoStorage
                    .getBoolean(Key.WlanAuto.KEY_WLAN_AUTO_UPGRADE_SWITCH, context.getResources().getBoolean(
                            R.bool.auto_download_only_wlan));
			//Chenyee <CY_Req> <xuyongji> <20180822> modify for SW17W16SE-128 end				
        }

        if (NetworkUtils.isMobileNetwork(context)) {
            IStorage mSettingStorage = SettingUpdateDataInvoker.getInstance(context).settingStorage();
            return mSettingStorage.getBoolean(Key.Setting.KEY_MOBILE_NET_ENABLE, false);
        }
        return false;
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        }
        return false;
    }

    public static boolean isMobileNetwork(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = manager.getActiveNetworkInfo();

        if (activeNetworkInfo != null) {
            Log.d(TAG,
                    "isMobileNetwork  getType: " + activeNetworkInfo.getType()
                            + " activeNetworkInfo.getState(): "
                            + activeNetworkInfo.getState());
            if (activeNetworkInfo.getType() == ConnectivityManager.TYPE_MOBILE
                    && activeNetworkInfo.getState() == android.net.NetworkInfo.State.CONNECTED) {
                return true;
            }
        }

        return false;
    }

    public static boolean isWIFIConnection(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = manager.getActiveNetworkInfo();
        if (activeNetworkInfo != null) {
            int netWorkType = activeNetworkInfo.getType();
            if ((ConnectivityManager.TYPE_WIFI == netWorkType
                    || ConnectivityManager.TYPE_WIMAX == netWorkType)
                    && activeNetworkInfo.getState()
                    == NetworkInfo.State.CONNECTED) {
                return true;
            }
        }
        return false;
    }

    public static boolean isWapConnection(Context context) {
        ConnectivityManager lcm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo.State mobile = lcm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
        if (mobile == NetworkInfo.State.CONNECTED) {
            return TelephonyManager.getNetworkClass(
                    TelephonyManager.getDefault().getNetworkType())
                    == TelephonyManager.NETWORK_CLASS_2_G;
        }
        return false;
    }

    public static String getNetStatisticsInfo(Context context) {
        NetConfig.ConnectionType connectType = getConnectionType(context);
        if (connectType.equals(NetConfig.ConnectionType.CONNECTION_TYPE_WIFI)) {
            return "WIFI";
        } else if (connectType.equals(NetConfig.ConnectionType.CONNECTION_TYPE_2G)) {
            return "2G";
        } else if (connectType.equals(NetConfig.ConnectionType.CONNECTION_TYPE_3G)) {
            return  "3G";
        } else if (connectType.equals(NetConfig.ConnectionType.CONNECTION_TYPE_4G)){
            return "4G";
        }
        return "unKnown";
    }

    public static NetConfig.ConnectionType getConnectionType(Context context) {
        NetConfig.ConnectionType type = NetConfig.ConnectionType.CONNECTION_TYPE_IDLE;

        if (NetworkUtils.isNetworkAvailable(context)) {
            if (NetworkUtils.isWIFIConnection(context)) {
                type = NetConfig.ConnectionType.CONNECTION_TYPE_WIFI;
            } else {
                type = getMobileConnectionType(context);
            }
        }

        return type;
    }
    public static boolean isGeminiSupport() {
        String qcomMultisim = SystemPropertiesUtils.getQcomMultisim();
        return ("true".equals(SystemPropertiesUtils.getMtkGemini())) || ("dsda".equals(qcomMultisim))
                || ("dsds".equals(qcomMultisim));
    }


    private static NetConfig.ConnectionType getMobileConnectionType(Context context) {

        NetConfig.ConnectionType type = NetConfig.ConnectionType.CONNECTION_TYPE_IDLE;
        int specificType = TelephonyManager.NETWORK_TYPE_UNKNOWN;

        TelephonyManager telephonyManager = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager == null) {
            return type;
        }

        if (GEMINI_SUPPORT) {
            specificType = getNetworkTypeGemini(context, telephonyManager);
        } else {
            specificType = getNetworkSingleSim(context, telephonyManager);
        }

        switch (TelephonyManager.getNetworkClass(specificType)) {
        case TelephonyManager.NETWORK_CLASS_2_G:
            type = NetConfig.ConnectionType.CONNECTION_TYPE_2G;
            break;
        case TelephonyManager.NETWORK_CLASS_3_G:
            type = NetConfig.ConnectionType.CONNECTION_TYPE_3G;
            break;
        case TelephonyManager.NETWORK_CLASS_4_G:
            type = NetConfig.ConnectionType.CONNECTION_TYPE_4G;
            break;
        default:
            break;
        }

        return type;
    }
    private static int getNetworkSingleSim(Context context, TelephonyManager telephonyManager) {
        int type = telephonyManager.getNetworkType();
        Log.d(TAG, "getNetworkSingleSim  type =  " + type);
        return type;
    }

    private static int getNetworkTypeGemini(Context context, TelephonyManager telephonyManager) {
        if (SystemPropertiesUtils.isMtkPlatform()) {
            return getNetworkTypeGeminiForMTK(context, telephonyManager);
        } else {
            return getNetworkTypeGeminiForQcom(context, telephonyManager);
        }
    }

    private static int getNetworkTypeGeminiForMTK(Context context, TelephonyManager telephonyManager) {
        int type = TelephonyManager.NETWORK_TYPE_UNKNOWN;

        try {
            Class<?> systemClass = Settings.System.class;
            Class<?> simInfoClass = Class.forName("android.provider.Telephony$SIMInfo");
            Field gprsConnectionSimSettingField = systemClass.getField("GPRS_CONNECTION_SIM_SETTING");
            String gprsConnectionSimSettingString = (String) gprsConnectionSimSettingField.get(null);
            Method getNetworkTypeGemini = TelephonyManager.class.getMethod("getNetworkTypeGemini", int.class);
            Method getSlotByIdMethod = simInfoClass.getMethod("getSlotById", Context.class, long.class);

            long gprsConnectionSimId = Settings.System.getLong(context.getContentResolver(),
                    gprsConnectionSimSettingString, 0);
            // soltid is the real device id of the sim card
            int soltId = (Integer) getSlotByIdMethod.invoke(null, context, gprsConnectionSimId);
            Log.d(TAG, "getNetworkTypeGeminiForMTK() gprsConnectionSimId = " + gprsConnectionSimId);
            type = (Integer) getNetworkTypeGemini.invoke(telephonyManager, soltId);
            Log.d(TAG, "getNetworkTypeGeminiForMTK ( " + soltId + ")= " + type);
            return type;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return telephonyManager.getNetworkType();
    }

    private static int getNetworkTypeGeminiForQcom(Context context, TelephonyManager telephonyManager) {
        int type = 0;
        try {
            Class<?> mSimTelephonyManagerClass = Class.forName("android.telephony.MSimTelephonyManager");
            Method getNetworkTypeMethod = mSimTelephonyManagerClass.getMethod("getNetworkType", int.class);
            Method getPreferredDataSubscriptionMethod = mSimTelephonyManagerClass
                    .getMethod("getPreferredDataSubscription");
            Object mSimTelephonyManager = mSimTelephonyManagerClass.getConstructor(Context.class)
                    .newInstance(context);
            int simId = (Integer) getPreferredDataSubscriptionMethod.invoke(mSimTelephonyManager);

            type = (Integer) getNetworkTypeMethod.invoke(mSimTelephonyManager, simId);

            Log.d(TAG, "getNetworkTypeGeminiForQcom = " + type);
            return type;

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        return telephonyManager.getNetworkType();
    }
}
