package com.cydroid.ota.logic.net;

import android.content.Context;

import com.cydroid.ota.Log;
import com.cydroid.ota.logic.config.EnvConfig;
import com.cydroid.ota.logic.config.EnvConfigHelper;
import com.cydroid.ota.logic.config.NetConfig;
import com.cydroid.ota.utils.FileUtils;
import com.cydroid.ota.utils.SystemPropertiesUtils;
import com.cydroid.ota.utils.Util;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreProtocolPNames;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by borney on 14-9-12.
 */
public class HttpUtils {
    private static final String TAG = "HttpUtils";
    private static final boolean DEBUG = true;
    public static final int MAX_REQ_LENGTH = 8 * 1024 * 1024;
    public static final int CONNECTION_MOBILE_DEFAULT_PORT = 80;
    public static final String CONNECTION_MOBILE_DEFAULT_HOST = "10.0.0.172";

    public static String getServerHost(Context context) {
        String host;
        System.out.println("EnvConfig.isTestEnv() = " + EnvConfig.isTestEnv());
        System.out.println("EnvConfig.isChangeip() = " + EnvConfig.isChangeip());
        //chenyee yewq 2017-10-26 modify for 247205 begin
        /*if (EnvConfigHelper.isTestEnv(context)) {
            host =  NetConfig.TEST_HOST;
        } else if (EnvConfigHelper.isChangeip(context)) {
            host =  EnvConfigHelper.initHttpCommunicatorHost(context);*/
        if (EnvConfigHelper.isChangeip(context)) {
            host =  EnvConfigHelper.initHttpCommunicatorHost(context);
        } else if (EnvConfigHelper.isTestEnv(context)) {
            host =  NetConfig.TEST_HOST;
        //chenyee yewq 2017-10-26 modify for 247205 end
        }else {
            host =  NetConfig.NORMAL_HOST;
        }
        System.out.println("getServerHost host = " + host);
        return host;
    }

    public static String getQuestionnaireServerHost() {
        if (EnvConfig.isTestEnv()) {
            return NetConfig.QUESTIONNAIRE_TEST_HOST;
        } else {
            return NetConfig.QUESTIONNAIRE_NOMAL_HOST;
        }
    }

    public static void assembleNameValuePair(String key, String value,
                                             List<NameValuePair> pairs) {
        Log.d(TAG, "assembleNameValuePair value =  : " + value);
        if (value != null) {
            NameValuePair pair = new BasicNameValuePair(key, value);
            pairs.add(pair);
        }
    }

    public static Map<String, String> getUAHeader(Context context) {
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("User-Agent", Util.getUaString(
                SystemPropertiesUtils.getImei(context)));
        return headers;
    }
}
