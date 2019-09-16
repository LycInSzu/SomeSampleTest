package com.cydroid.ota.logic;

import android.content.Context;
import com.cydroid.ota.logic.sync.ISyncCallback;
import com.cydroid.ota.logic.sync.QuestionnaireJob;

/**
 * Created by liuyanfeng on 15-6-30.
 */
public class QuestionnaireExecutor extends AbstractExecutor implements IQuestionnaireExecutor {
    private IExcutorCallback mCallback;
    private QuestionnaireJob mJob;
    private Context mContext;

    protected QuestionnaireExecutor(Context context, IExcutorCallback callback) {
        mCallback = callback;
        mContext = context;
    }

    @Override
    public void check() {
        mJob = new QuestionnaireJob(mQuestionnaireCallback, mContext);
        syncexe(mJob);
    }

    @Override
    protected void handler() {
        mJob = null;
    }


    private ISyncCallback mQuestionnaireCallback = new ISyncCallback() {
        @Override
        public void onResult(Object... objects) {
            if (mCallback != null) {
                mCallback.onResult(objects);
            }
        }

        @Override
        public void onError(int errorCode) {
            if (mCallback != null) {
                mCallback.onError(errorCode);
            }
        }
    };
}
