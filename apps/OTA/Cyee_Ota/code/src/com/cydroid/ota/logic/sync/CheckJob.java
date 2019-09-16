package com.cydroid.ota.logic.sync;

import android.content.Context;
import android.os.Build;
import android.os.Message;
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
import com.cydroid.ota.utils.Error;
import com.cydroid.ota.Log;
import com.cydroid.ota.utils.SystemPropertiesUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.protocol.HTTP;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by borney on 4/14/15.
 */
public class CheckJob extends Job {
    private static final String TAG = "CheckJob";
    protected CheckInfo mCheckInfo;
    protected Context mContext;
    protected volatile boolean isHandleError = false;

    protected CheckJob(Context context, CheckInfo checkInfo, ISyncCallback callback) {
        super(callback);
        mContext = context;
        mCheckInfo = checkInfo;
    }

    @Override
    public SettingUpdateInfo run() {
        isHandleError = false;
        String url = createCheckUrl();
        Log.d(TAG, "createCheckUrl() url = " + url);
        List<NameValuePair> pairs = createNameValuePairs(mCheckInfo);
        try {
            HttpEntity resultEntity = HttpHelper.executeHttpPost(url,
                    pairs, true, null, HttpUtils.getUAHeader(mContext));
            String entity = parseEntity(resultEntity, HTTP.UTF_8);
            if (isRightResult(entity)) {
                OtaUpgradeInfoParser parser = new OtaUpgradeInfoParser();
                SettingUpdateInfo upgradeInfo = parser.parser(entity);
                return upgradeInfo;
            }
        } catch (SettingUpdateParserException e) {
            isHandleError = true;
            sendMessage(MSG.MSG_CHECK_PARSER_EXCEPTION);
            Log.e(TAG, "SettingUpdateParserException:" + e);
        } catch (SettingUpdateNetException e) {
            isHandleError = true;
            sendMessage(MSG.MSG_CHECK_NETWORK_EXCEPTION);
            Log.e(TAG, "SettingUpdateException:" + e);
        }
        return null;
    }

    protected boolean isRightResult(String result) {
        if (TextUtils.isEmpty(result)) {
            return false;
        } else if (!result.contains("extPkg")) {
            return false;
        }
        return true;
    }

    @Override
    void handleJobMessage(Message msg) {
        Log.d(TAG, "msg:" + msg.what);
        switch (msg.what) {
            case MSG.MSG_CHECK_HAS_NEW_VERSION:
                SettingUpdateInfo info = (SettingUpdateInfo) msg.obj;
                mJobCallback.onResult(mCheckInfo.isRoot(), true, info);
                break;
            case MSG.MSG_CHECK_HAS_NO_VERSION:
                mJobCallback.onResult(mCheckInfo.isRoot(), false, null);
                break;
            case MSG.MSG_CHECK_NETWORK_EXCEPTION:
                mJobCallback.onError(Error.ERROR_CODE_NETWORK_ERROR);
                break;
            case MSG.MSG_CHECK_PARSER_EXCEPTION:
                mJobCallback.onError(Error.ERROR_CODE_PARSER_ERROR);
                break;
            default:
                break;
        }
    }

    @Override
    protected void handleJobSyncMessage(Message message) {
        switch (message.what) {
            case MSG.MSG_HTTP_IO_CLOSE:
                InputStream is = (InputStream) message.obj;
                try {
                    if (is != null) {
                        is.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case MSG.MSG_ENTITY_STREAMING_CLOSE:
                HttpEntity entity = (HttpEntity) message.obj;
                consume(entity);
                break;
            default:
                break;
        }
    }

    protected String parseEntity(HttpEntity entity, String charset)
            throws SettingUpdateParserException {
        Log.d(TAG, "parseEntity==" + entity.isStreaming());
        if (!entity.isStreaming())
            throw new SettingUpdateParserException("INVALID_RECEIVE_DATA");
        InputStream is = null;
        StringBuffer sb = new StringBuffer();
        BufferedReader reader = null;
        String jsonResult;
        String line;
        try {
            is = entity.getContent();
            reader = new BufferedReader(
                    new InputStreamReader(is, charset));
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            jsonResult = sb.toString();
        } catch (IOException ex) {
            Log.e(TAG, "IOException:" + ex);
            throw new SettingUpdateParserException("INVALID_NEWWORK");
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Message ioCloseMessage = obtainSyncMessage(MSG.MSG_HTTP_IO_CLOSE);
            ioCloseMessage.obj = is;
            sendSyncMessage(ioCloseMessage);
            Message entityCloseMessage = obtainMessage(MSG.MSG_ENTITY_STREAMING_CLOSE);
            entityCloseMessage.obj = entity;
            sendSyncMessage(entityCloseMessage);
        }
        Log.debug(TAG, "jsonResult:" + jsonResult);
        return jsonResult;
    }

    private String createCheckUrl() {
        StringBuilder uriBuilder = new StringBuilder();
        uriBuilder.append(HttpUtils.getServerHost(mContext));
        uriBuilder.append(NetConfig.GIONEE_HTTP_CHECK);
        return uriBuilder.toString();
    }

    private List<NameValuePair> createNameValuePairs(CheckInfo checkInfo) {
        List<NameValuePair> pairs = new ArrayList<NameValuePair>();
        HttpUtils.assembleNameValuePair("pkg",
                "COM.GIONEE." + SystemPropertiesUtils.getModel().toString().replaceAll(" ", "%20"), pairs);
        HttpUtils.assembleNameValuePair("vc",
                SystemPropertiesUtils.getInternalVersion(),
                pairs);
        if (EnvConfigHelper.isTestModel(mContext) ) {
            HttpUtils.assembleNameValuePair("state", "3", pairs);
        }
        HttpUtils.assembleNameValuePair("patch",
                String.valueOf(checkInfo.isSupportPatch()),
                pairs);
        HttpUtils.assembleNameValuePair("rom", Build.VERSION.RELEASE, pairs);
        HttpUtils.assembleNameValuePair("pid",
                String.valueOf(checkInfo.getCheckType().value()), pairs);
        HttpUtils.assembleNameValuePair("nt", getNetworkType(checkInfo), pairs);
        HttpUtils.assembleNameValuePair("vi", "2", pairs);
        HttpUtils.assembleNameValuePair("client", "1", pairs);
        HttpUtils.assembleNameValuePair("imei",
                SystemPropertiesUtils.getEncryptionImei(
                        checkInfo.getImei()), pairs);
        for(NameValuePair str : pairs)
            System.out.println("checkjob = " + str);
        return pairs;
    }

    protected String getNetworkType(CheckInfo checkInfo) {
        NetConfig.ConnectionType conType = checkInfo.getConnectionType();
        StringBuilder netType = new StringBuilder();
        switch (conType) {
            case CONNECTION_TYPE_2G:
                netType.append("2G");
                if (checkInfo.isWapNetwork()) {
                    netType.append("&wap");
                }
                break;
            case CONNECTION_TYPE_3G:
                netType.append("3G");
                break;
            case CONNECTION_TYPE_4G:
                netType.append("4G");
                break;
            case CONNECTION_TYPE_WIFI:
                netType.append("WF");
                break;
            default:
                break;
        }
        return netType.toString();
    }

    protected static final class MSG extends Job.MSG {
        private static final int BASE_MSG = BASE * 1;

        static {
            //Log.d(TAG, TAG + " BASE_MSG = " + BASE_MSG);
        }

        static final int MSG_CHECK_HAS_NEW_VERSION = BASE_MSG + 1;
        static final int MSG_CHECK_HAS_NO_VERSION = BASE_MSG + 2;
        static final int MSG_CHECK_PARSER_EXCEPTION = BASE_MSG + 3;
        static final int MSG_CHECK_NETWORK_EXCEPTION = BASE_MSG + 4;
        static final int MSG_ENTITY_STREAMING_CLOSE = BASE_MSG + 5;
        static final int MSG_HTTP_IO_CLOSE = BASE_MSG + 6;
    }
}
