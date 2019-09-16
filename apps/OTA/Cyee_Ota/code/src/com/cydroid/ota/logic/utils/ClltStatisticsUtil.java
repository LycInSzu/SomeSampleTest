package com.cydroid.ota.logic.utils;

import android.content.Context;
import com.cydroid.ota.logic.config.EnvConfig;
import com.cydroid.ota.logic.config.NetConfig;
import com.cydroid.ota.logic.net.HttpUtils;
import com.cydroid.ota.storage.IStorage;
import com.cydroid.ota.storage.Key;
import com.cydroid.ota.storage.SettingUpdateDataInvoker;
import com.cydroid.ota.utils.NetworkUtils;
import com.cydroid.ota.utils.SystemPropertiesUtils;
import org.apache.http.NameValuePair;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kangjj on 15-7-25.
 */
public class ClltStatisticsUtil {
    public static final String CLLT_SERVER_HOST = "http://red.gionee.com";

    public static String createRequestURl(boolean isDownload, boolean isStartDownload, boolean isInstallSucess, String downloadPackagename) {

        StringBuffer urlString = new StringBuffer();

        if (EnvConfig.isTestEnv()) {
            urlString.append(NetConfig.TEST_HOST);
        } else {
            urlString.append(CLLT_SERVER_HOST);
        }

        urlString.append("/cllt/ota/");
        if (isDownload) {
            if (isStartDownload) {
                urlString.append("12100");
            } else {
                urlString.append("11100");
            }
        } else {
            if (isInstallSucess) {
                urlString.append("13100");
            } else {
                urlString.append("14100");
            }
        }
        urlString.append(downloadPackagename);
        urlString.append("?");
        return urlString.toString();
    }

    public static List<NameValuePair> createVersionPairs(Context context, boolean isAutoDownload, boolean isRoot, boolean isDownload) {
        List<NameValuePair> pairs = new ArrayList<NameValuePair>();
        IStorage settingStorage = SettingUpdateDataInvoker.getInstance(
                context).settingStorage();
        String ver = "";
        if (isDownload) {
            ver = settingStorage.getString(
                    Key.Setting.KEY_SETTING_UPDATE_CURRENT_VERSION, "");
        } else {

            ver = settingStorage.getString(
                    Key.Setting.KEY_SETTING_UPDATE_LAST_VERSION, "");
        }

        boolean isOnline = settingStorage.getBoolean(
            Key.Setting.KEY_UPGRADE_UPGRADE_ONLINE_FLAG, false);
        HttpUtils.assembleNameValuePair("ver", ver, pairs);
        HttpUtils.assembleNameValuePair("nt", NetworkUtils.getNetStatisticsInfo(context), pairs);
        String imei = SystemPropertiesUtils.getEncryptionImei(SystemPropertiesUtils.getImei(context));
        HttpUtils.assembleNameValuePair("imei", imei, pairs);
        HttpUtils.assembleNameValuePair("pid", getPid(isAutoDownload), pairs);
        HttpUtils.assembleNameValuePair("root", String.valueOf(isRoot), pairs);
        HttpUtils.assembleNameValuePair("online",String.valueOf(isOnline),pairs);
        return pairs;
    }

    private static String getPid(boolean isAutoDownload) {
        if (isAutoDownload) {
            return "-1";
        }
        return "-2";
    }

}
