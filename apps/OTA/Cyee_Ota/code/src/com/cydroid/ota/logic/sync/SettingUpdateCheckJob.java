package com.cydroid.ota.logic.sync;

import android.content.Context;
import android.text.TextUtils;
import com.cydroid.ota.bean.SettingUpdateInfo;
import com.cydroid.ota.execption.SettingUpdateNetException;
import com.cydroid.ota.execption.SettingUpdateParserException;
import com.cydroid.ota.logic.bean.CheckInfo;
import com.cydroid.ota.logic.config.EnvConfig;
import com.cydroid.ota.logic.config.EnvConfigHelper;
import com.cydroid.ota.logic.config.NetConfig;
import com.cydroid.ota.logic.net.HttpHelper;
import com.cydroid.ota.logic.net.HttpUtils;
import com.cydroid.ota.logic.parser.OtaUpgradeInfoParser;
import com.cydroid.ota.Log;
import com.cydroid.ota.utils.SystemPropertiesUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.protocol.HTTP;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liuyanfeng on 15-4-16.
 */
public class SettingUpdateCheckJob extends CheckJob {
    private static final String TAG = "SettingUpdateCheckJob";

    public SettingUpdateCheckJob(Context context, CheckInfo checkInfo, ISyncCallback callback) {
        super(context, checkInfo, callback);
    }

    @Override
    public SettingUpdateInfo run() {
        SettingUpdateInfo info = super.run();
        Log.d(TAG, "isRoot = " + mCheckInfo.isRoot());
        if (mCheckInfo.isRoot() && info == null) {
            String url = createCurrentVersionUrl();
            List<NameValuePair> pairs = createCurrentVersionPairs();
            try {
                HttpEntity result = HttpHelper.executeHttpPost(url, pairs, true, null, HttpUtils.getUAHeader(mContext));
                String entity = parseEntity(result, HTTP.UTF_8);
                if (isRightResult(entity)) {
                    OtaUpgradeInfoParser parser = new OtaUpgradeInfoParser();
                    info = parser.parser(entity);
                }
            } catch (SettingUpdateParserException e) {
                isHandleError = true;
                sendMessage(MSG.MSG_CHECK_PARSER_EXCEPTION);
                Log.e(TAG, "SettingUpdateParserException:" + e);
            } catch (SettingUpdateNetException e) {
                isHandleError = true;
                sendMessage(MSG.MSG_CHECK_NETWORK_EXCEPTION);
                e.printStackTrace();
            }
        }
        Log.debug(TAG, "isHandleError = " + isHandleError + " info = " + info);
        if (!isHandleError) {
            handleCheckResult(info);
        }
        return null;
    }

    protected boolean isRightResult(String result) {
        if (TextUtils.isEmpty(result)) {
            return false;
        }
        return true;
    }

    private void handleCheckResult(SettingUpdateInfo info) {
        if (info == null) {
            sendMessage(MSG.MSG_CHECK_HAS_NO_VERSION);
        } else {
            sendMessage(MSG.MSG_CHECK_HAS_NEW_VERSION, info);
        }
    }

    private List<NameValuePair> createCurrentVersionPairs() {
        List<NameValuePair> pairs = new ArrayList<NameValuePair>();
        HttpUtils.assembleNameValuePair("pkg", "COM.GIONEE." + SystemPropertiesUtils.getModel(), pairs);
        HttpUtils.assembleNameValuePair("vc", SystemPropertiesUtils.getInternalVersion(), pairs);
        HttpUtils.assembleNameValuePair("vi", "2", pairs);
        HttpUtils.assembleNameValuePair("client", "1", pairs);
       if (EnvConfigHelper.isTestEnv(mContext)) {
            HttpUtils.assembleNameValuePair("state", "3", pairs);
        }
        HttpUtils.assembleNameValuePair("pid",
                String.valueOf(mCheckInfo.getCheckType().value()), pairs);
        HttpUtils.assembleNameValuePair("nt",getNetworkType(mCheckInfo) , pairs);
        HttpUtils.assembleNameValuePair("imei",
                SystemPropertiesUtils.getEncryptionImei(
                        mCheckInfo.getImei()), pairs);
        for(NameValuePair str : pairs)
            System.out.println("setingupdatecheck = " + str);
        return pairs;
    }

    private String createCurrentVersionUrl() {
        StringBuilder currentUrl = new StringBuilder();
        currentUrl.append(HttpUtils.getServerHost(mContext));
        currentUrl.append(NetConfig.GIONEE_HTTP_QUERY);
        return currentUrl.toString();
    }
}
