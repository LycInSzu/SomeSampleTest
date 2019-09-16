package com.cydroid.ota.logic;

import com.cydroid.ota.logic.bean.IQuestionnaireInfo;

/**
 * Created by liuyanfeng on 15-6-30.
 */
public interface IQuestionnaire {
    IQuestionnaireInfo getQuestionnaireInfo();

    IQuestionnaireNotification questionnaireNotification();

    void registerDataChange(QuestionnaireDataChange dataChange);

    void unregisterDataChange(QuestionnaireDataChange dataChange);

    void questionnaireRead();

    interface QuestionnaireDataChange{
        void onDataChange();
    }
}
