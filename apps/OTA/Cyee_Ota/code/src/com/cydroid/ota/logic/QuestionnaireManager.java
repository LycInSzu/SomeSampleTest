package com.cydroid.ota.logic;

import android.content.Context;
import android.text.TextUtils;

import com.cydroid.ota.logic.bean.IQuestionnaireInfo;
import com.cydroid.ota.logic.bean.QuestionnaireInfo;
import com.cydroid.ota.storage.IStorage;
import com.cydroid.ota.storage.Key;
import com.cydroid.ota.storage.SettingUpdateDataInvoker;
import com.cydroid.ota.utils.SystemPropertiesUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.Vector;

/**
 * Created by liuyanfeng on 15-6-30.
 */
public class QuestionnaireManager implements IQuestionnaire {

    private static final int QUESTIONNAIRE_ALREADY_COMMIT = 1;
    private static final int QUESTIONNAIRE_NOTIFY_MAX_COUNT = 3;
    private IQuestionnaireExecutor mQuestionnaireExecutor;
    private Context mContext;
    private static IQuestionnaire sInstance = null;
    private QuestionnaireInfo mQuestionnaireInfo;
    private Vector<WeakReference<QuestionnaireDataChange>> mDataChanges;
    private IQuestionnaireNotification mQuestionnaireNotification;

    private QuestionnaireManager(Context context) {
        mContext = context.getApplicationContext();
        mQuestionnaireNotification = new QuestionnaireNotificationImpl(mContext);
        if (mQuestionnaireExecutor == null) {
            mQuestionnaireExecutor = new QuestionnaireExecutor(mContext,
                    mCheckCallback);
        }
        mDataChanges = new Vector<WeakReference<QuestionnaireDataChange>>();
    }

    protected synchronized static IQuestionnaire getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new QuestionnaireManager(context);
        }
        return sInstance;
    }

    @Override
    public IQuestionnaireInfo getQuestionnaireInfo() {
        if (mQuestionnaireInfo == null) {
            IStorage settings = SettingUpdateDataInvoker
                    .getInstance(mContext).settingStorage();
            String questionInfo = settings.getString(
                    Key.Setting.KEY_SETTING_QUESTIONNAIRE_INFO, "");
            String questionSysVersion = settings.getString(
                    Key.Setting.KEY_SETTING_QUESTIONNAIRE_SYS_VERSION, "");
            if (TextUtils.isEmpty(questionInfo) || !questionSysVersion.equals(
                    SystemPropertiesUtils.getInternalVersion())) {
                return null;
            }
            mQuestionnaireInfo = parseQuestionnaireInfo(questionInfo);
        }
        return mQuestionnaireInfo;
    }

    @Override
    public void questionnaireRead() {
        IStorage settings = SettingUpdateDataInvoker
                .getInstance(mContext).settingStorage();
        settings.putBoolean(Key.Setting.KEY_SETTING_QUESTIONNAIRE_READ, true);
    }

    @Override
    public IQuestionnaireNotification questionnaireNotification() {
        return mQuestionnaireNotification;
    }

    @Override
    public void registerDataChange(QuestionnaireDataChange dataChange) {
        mQuestionnaireExecutor.check();
        mDataChanges.add(new WeakReference<QuestionnaireDataChange>(dataChange));
    }

    @Override
    public void unregisterDataChange(QuestionnaireDataChange dataChange) {
        if (dataChange != null) {
            for (WeakReference<QuestionnaireDataChange> reference : mDataChanges) {
                QuestionnaireDataChange change = reference.get();
                if (change != null && change.equals(dataChange)) {
                    mDataChanges.remove(reference);
                    break;
                }
            }
        }
    }

    private QuestionnaireInfo parseQuestionnaireInfo(String questionInfo) {
        try {
            JSONObject object = new JSONObject(questionInfo);
            int status = object.getInt("t");
            String url = object.getString("url");
            QuestionnaireInfo info = new QuestionnaireInfo();
            info.setStatus(status);
            info.setQuestionnaireUrl(url);
            return info;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private IExcutorCallback mCheckCallback = new IExcutorCallback() {
        @Override
        public void onResult(Object... objects) {
            String result = (String) objects[0];
            mQuestionnaireInfo = parseQuestionnaireInfo(result);

            IStorage settings = SettingUpdateDataInvoker
                    .getInstance(mContext).settingStorage();
            if (mQuestionnaireInfo != null &&
                    mQuestionnaireInfo.getStatus() == QUESTIONNAIRE_ALREADY_COMMIT) {
                settings.putInt(Key.Setting.KEY_SETTING_QUESTIONNAIRE_COUNT,
                        QUESTIONNAIRE_NOTIFY_MAX_COUNT);
            }

            String questionInfo = settings.getString(
                    Key.Setting.KEY_SETTING_QUESTIONNAIRE_INFO, "");
            QuestionnaireInfo questionnaireInfo = parseQuestionnaireInfo(questionInfo);

            if (questionnaireInfo != null && questionnaireInfo.getStatus() == 0) {
                settings.putBoolean(Key.Setting.KEY_SETTING_QUESTIONNAIRE_READ,
                        false);
            } else {
                settings.putBoolean(Key.Setting.KEY_SETTING_QUESTIONNAIRE_READ,
                        true);
            }
            settings.putString(Key.Setting.KEY_SETTING_QUESTIONNAIRE_INFO, result);
            settings.putString(
                    Key.Setting.KEY_SETTING_QUESTIONNAIRE_SYS_VERSION,
                    SystemPropertiesUtils.getInternalVersion());
            notifyDataChange();
        }

        private void notifyDataChange() {
            for (WeakReference<QuestionnaireDataChange> dataChangeWeakReference
                    : mDataChanges) {
                QuestionnaireDataChange questionnaireDataChange = dataChangeWeakReference
                        .get();
                if (questionnaireDataChange != null) {
                    questionnaireDataChange.onDataChange();
                }
            }
        }

        @Override
        public void onError(int errorCode) {
            mQuestionnaireInfo = null;
            notifyDataChange();
        }
    };
}
