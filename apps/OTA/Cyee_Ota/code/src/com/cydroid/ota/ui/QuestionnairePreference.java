package com.cydroid.ota.ui;

import cyee.preference.CyeePreference;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.cydroid.ota.SettingUpdateApplication;
import com.cydroid.ota.logic.bean.IQuestionnaireInfo;
import com.cydroid.ota.logic.SystemUpdateFactory;
import com.cydroid.ota.storage.Key;
import com.cydroid.ota.storage.SettingUpdateDataInvoker;
import com.cydroid.ota.ui.widget.ITheme;
import com.cydroid.ota.Log;
import com.cydroid.ota.R;


public class QuestionnairePreference extends CyeePreference {
    private static final String TAG = "QuestionnairePreference";
    private TextView mTextView = null;
    private ImageView badge = null;
    private Context mContext = null;
    private ITheme.Chameleon mChameleon;

    public QuestionnairePreference(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public QuestionnairePreference(Context context, AttributeSet attrs,
            int defStyle) {
        super(context, attrs, defStyle);
        this.mContext = context;
        mChameleon = ((SettingUpdateApplication)context.getApplicationContext()).getSystemTheme().getChameleon();
    }

    /**
     * @param context
     */
    public QuestionnairePreference(Context context) {
        this(context, null, 0);
    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        LayoutInflater layoutInflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View layout = layoutInflater.inflate(R.layout.questionnaire_perference, parent, false);
        Log.d(TAG, "onCreateView is null" + (layout == null));
        return layout;
    }

    @Override
    protected void onBindView(View arg0) {
        super.onBindView(arg0);
        mTextView = (TextView) arg0.findViewById(R.id.title);
        badge = (ImageView) arg0.findViewById(R.id.badge);
        Log.d(TAG, "onBindView is null" + (mTextView == null));
        if (mTextView != null) {
            Log.d(TAG, "onBindView is null" + (mTextView == null));
            mTextView.setTextColor(mChameleon.ContentColorPrimaryOnBackgroud_C1);
            mTextView.setText(getTitle());
            IQuestionnaireInfo questionnaireInfo = SystemUpdateFactory
                    .questionnaire(mContext).getQuestionnaireInfo();
            boolean isRead = SettingUpdateDataInvoker.getInstance(mContext).settingStorage()
                    .getBoolean(Key.Setting.KEY_SETTING_QUESTIONNAIRE_READ,
                            false);
            if (questionnaireInfo != null && questionnaireInfo.getStatus() == 0
                    && !isRead){
                setBadgeViewVisibility(true);
            } else {
                setBadgeViewVisibility(false);
            }
        }
    }

    @Override
    protected void onAttachedToActivity() {
        super.onAttachedToActivity();
    }

    public void setBadgeViewVisibility(Boolean isDisplay) {
        if (badge == null) {
            return;
        }
        if (isDisplay) {
            badge.setVisibility(View.VISIBLE);
        } else {
            badge.setVisibility(View.GONE);
        }
    }
}
