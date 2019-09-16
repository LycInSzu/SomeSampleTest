package com.cydroid.ota.logic.sync;

import android.content.Context;
import android.os.Message;
import android.text.TextUtils;
import com.cydroid.ota.execption.SettingUpdateNetException;
import com.cydroid.ota.execption.SettingUpdateParserException;
import com.cydroid.ota.logic.config.NetConfig;
import com.cydroid.ota.logic.net.HttpHelper;
import com.cydroid.ota.logic.net.HttpUtils;
import com.cydroid.ota.storage.IStorage;
import com.cydroid.ota.storage.Key;
import com.cydroid.ota.storage.SettingUpdateDataInvoker;
import com.cydroid.ota.utils.Error;
import com.cydroid.ota.Log;
import com.cydroid.ota.utils.NetworkUtils;
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
 * Created by liuyanfeng on 15-6-30.
 */
public class QuestionnaireJob extends Job {
    private static final String TAG = "QuestionnaireJob";
    private Context mContext;

    public QuestionnaireJob(ISyncCallback callback, Context context) {
        super(callback);
        mContext = context;
    }

    @Override
    public String run() {
        if (!NetworkUtils.isNetworkAvailable(mContext)) {
            return null;
        }
        IStorage settingStorage = SettingUpdateDataInvoker.getInstance(mContext).settingStorage();
        if (!NetworkUtils.isWIFIConnection(mContext) && !settingStorage.getBoolean(Key.Setting.KEY_MOBILE_NET_ENABLE, false)) {
            return null;
        }
        String url = createUrl();
        List<NameValuePair> pairs = createValuePairs();
        try {
            HttpEntity resultEntity = HttpHelper.executeHttpPost(url,
                    pairs, true, null, null);
            String entity = parseEntity(resultEntity, HTTP.UTF_8);
            if (isRightResult(entity)) {
                sendMessage(MSG.MSG_CHECK_HAS_QUESTIONNAIRE, entity);
                return entity;
            }
        } catch (SettingUpdateParserException e) {
            sendMessage(MSG.MSG_CHECK_PARSER_EXCEPTION);
            Log.e(TAG, "SettingUpdateParserException:" + e);
        } catch (SettingUpdateNetException e) {
            sendMessage(MSG.MSG_CHECK_NETWORK_EXCEPTION);
            Log.e(TAG, "SettingUpdateException:" + e);
        }
        return null;
    }

    @Override
    void handleJobMessage(Message msg) {
        Log.d(TAG, "msg:" + msg.what);
        switch (msg.what) {
        case MSG.MSG_CHECK_HAS_QUESTIONNAIRE:
            mJobCallback.onResult(msg.obj);
            break;
        case MSG.MSG_CHECK_HAS_NO_QUESTIONNAIRE:
            mJobCallback.onError(Error.ERROR_CODE_NO_QUESTIONNAIRE);
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

    private boolean isRightResult(String result) {
        if (TextUtils.isEmpty(result)) {
            sendMessage(MSG.MSG_CHECK_HAS_NO_QUESTIONNAIRE);
            return false;
        }
        return true;
    }

    private String parseEntity(HttpEntity entity, String charset)
            throws SettingUpdateParserException {
        Log.d(TAG, "parseEntity==" + entity.isStreaming());
        if (!entity.isStreaming())
            throw new SettingUpdateParserException("INVALID_RECEIVE_DATA");
        InputStream is = null;
        StringBuffer sb = new StringBuffer();
        String jsonResult;
        String line;
        BufferedReader reader = null;
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
        Log.d(TAG, "jsonResult:" + jsonResult);
        return jsonResult;
    }

    private String createUrl() {
        StringBuilder uriBuilder = new StringBuilder();
        uriBuilder.append(HttpUtils.getQuestionnaireServerHost());
        uriBuilder.append(NetConfig.GIONEE_QUESTIONNAIRE_CHECK);
        return uriBuilder.toString();
    }

    private List<NameValuePair> createValuePairs() {
        List<NameValuePair> pairs = new ArrayList<NameValuePair>();
        //Log.d(TAG, "rom=" + SystemPropertiesUtils.getInternalVersion());
        //Log.d(TAG, "imei=" + SystemPropertiesUtils.getImei(mContext));
        //Log.d(TAG, "model=" + SystemPropertiesUtils.getModel());
        HttpUtils.assembleNameValuePair("rom",
                SystemPropertiesUtils.getInternalVersion(), pairs);
        HttpUtils.assembleNameValuePair("model", SystemPropertiesUtils.getModel(), pairs);
        HttpUtils.assembleNameValuePair("imei", SystemPropertiesUtils.getImei(mContext), pairs);
        HttpUtils.assembleNameValuePair("group", "otarom", pairs);
        return pairs;
    }

    protected static final class MSG extends Job.MSG {
        private static final int BASE_MSG = BASE * 6;

        /*static {
            Log.d(TAG, TAG + " BASE_MSG = " + BASE_MSG);
        }*/

        static final int MSG_CHECK_HAS_QUESTIONNAIRE = BASE_MSG + 1;
        static final int MSG_CHECK_HAS_NO_QUESTIONNAIRE = BASE_MSG + 2;
        static final int MSG_CHECK_PARSER_EXCEPTION = BASE_MSG + 3;
        static final int MSG_CHECK_NETWORK_EXCEPTION = BASE_MSG + 4;
        static final int MSG_ENTITY_STREAMING_CLOSE = BASE_MSG + 5;
        static final int MSG_HTTP_IO_CLOSE = BASE_MSG + 6;
    }
}
