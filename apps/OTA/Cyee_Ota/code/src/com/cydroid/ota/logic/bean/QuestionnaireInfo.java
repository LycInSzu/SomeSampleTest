package com.cydroid.ota.logic.bean;

/**
 * Created by liuyanfeng on 15-7-7.
 */
public class QuestionnaireInfo implements IQuestionnaireInfo {

    private String mQuestionnaireUrl;
    private int status;

    public void setQuestionnaireUrl(String url) {
        mQuestionnaireUrl = url;
    }

    @Override
    public String getQuestionnaireUrl() {
        return mQuestionnaireUrl;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public int getStatus() {
        return status;
    }
}
