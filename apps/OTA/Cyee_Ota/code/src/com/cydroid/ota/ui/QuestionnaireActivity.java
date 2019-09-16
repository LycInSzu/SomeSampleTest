package com.cydroid.ota.ui;

import cyee.app.CyeeActionBar;
import cyee.widget.CyeeTextView;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.webkit.*;
import android.widget.RelativeLayout;
import com.cydroid.ota.SettingUpdateApplication;
import com.cydroid.ota.logic.IQuestionnaire;
import com.cydroid.ota.logic.bean.IQuestionnaireInfo;
import com.cydroid.ota.logic.SystemUpdateFactory;
import com.cydroid.ota.Log;
import com.cydroid.ota.utils.NetworkUtils;
import com.cydroid.ota.utils.SystemPropertiesUtils;
import com.cydroid.ota.R;

/**
 * Created by liuyanfeng on 15-6-30.
 */
public class QuestionnaireActivity extends AbsActivity {
    private static final String TAG = "QuestionnaireActivity";
    private static final int MSG_LOAD_QUESTIONNAIRE_START = 0x1001;
    private static final int MSG_LOAD_URL_START = 0x1002;
    private static final int MSG_LOAD_URL_ERROR = 0x1003;
    private static final int MSG_LOAD_URL_END = 0x1004;
    private WebView mQuestionnaireView;
    private RelativeLayout mLoadingLayout;
    private CyeeTextView mErrorView;
    private String mUrl;
    private boolean isFromNotify;
    private Context mContext;
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SettingUpdateApplication application = (SettingUpdateApplication) getApplication();
        if (!application.getSystemTheme().isNeedChangeColor()) {
            setTheme(R.style.Theme_Light_NormalTheme);
        }
        super.onCreate(savedInstanceState);
        initActionBar();
        setContentView(R.layout.gn_su_layout_questionnaire);
        mContext = QuestionnaireActivity.this;
        parseIntent();
        initViews();
        init();
    }

    private void parseIntent() {
        Intent intent = getIntent();
        isFromNotify = intent.getBooleanExtra("fromNotify", false);
    }

    private void initActionBar() {
        CyeeActionBar actionBar = getCyeeActionBar();
        if (actionBar == null) {
            return;
        }
        actionBar.setDisplayOptions(CyeeActionBar.DISPLAY_SHOW_TITLE |
                CyeeActionBar.DISPLAY_HOME_AS_UP | CyeeActionBar.DISPLAY_SHOW_HOME);
        actionBar.setTitle(getString(R.string.gn_su_string_settings_feedback));
    }

    private void init() {
        mHandler = new Handler(mHandlerCallback);
        //start loading
        mHandler.sendEmptyMessage(MSG_LOAD_QUESTIONNAIRE_START);

        SystemUpdateFactory.questionnaire(mContext).registerDataChange(mDataChange);

    }

    private void initViews() {
        mQuestionnaireView = (WebView) findViewById(R.id.questionnaireview);
        mLoadingLayout = (RelativeLayout) findViewById(R.id.gn_su_id_layout_questionnaire_loading);
        mErrorView = (CyeeTextView) findViewById(R.id.gn_su_id_questionnaire_network_error);
    }

    private void initData() {
        mQuestionnaireView.setScrollBarStyle(0);
        mQuestionnaireView.setOnLongClickListener(
                new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        return true;
                    }
                });
        mQuestionnaireView.addJavascriptInterface(new ProxyBridge(), "ProxyBridge");
        WebSettings webSettings = mQuestionnaireView.getSettings();
        webSettings.setAllowFileAccess(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setJavaScriptEnabled(true);
        mQuestionnaireView.loadUrl(mUrl);
        mQuestionnaireView.requestFocus();
        mQuestionnaireView.setWebViewClient(mWebViewClient);
    }

    private IQuestionnaire.QuestionnaireDataChange mDataChange =
            new IQuestionnaire.QuestionnaireDataChange() {
        @Override
        public void onDataChange() {
            IQuestionnaireInfo questionnaireInfo = SystemUpdateFactory.questionnaire(mContext)
                    .getQuestionnaireInfo();
            if (questionnaireInfo == null ||
                    TextUtils.isEmpty(questionnaireInfo.getQuestionnaireUrl())) {
                mHandler.sendEmptyMessage(MSG_LOAD_URL_ERROR);
            } else {
                mUrl = questionnaireInfo.getQuestionnaireUrl();
                mHandler.sendEmptyMessage(MSG_LOAD_URL_START);
            }
        }
    };

    private Handler.Callback mHandlerCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            switch (message.what) {
            case MSG_LOAD_QUESTIONNAIRE_START:
                mLoadingLayout.setVisibility(View.VISIBLE);
                mQuestionnaireView.setVisibility(View.GONE);
                break;
            case MSG_LOAD_URL_START:
                initData();
                break;
            case MSG_LOAD_URL_ERROR:
                mQuestionnaireView.setVisibility(View.GONE);
                mLoadingLayout.setVisibility(View.GONE);
                mErrorView.setVisibility(View.VISIBLE);
                break;
            case MSG_LOAD_URL_END:
                mLoadingLayout.setVisibility(View.GONE);
                mQuestionnaireView.setVisibility(View.VISIBLE);
                break;
            default:
                break;
            }
            return false;
        }
    };

    private WebViewClient mWebViewClient = new WebViewClient() {
        private boolean mIsError = false;

        @Override
        public void onReceivedError(WebView view, int errorCode,
                String description, String failingUrl) {
            mHandler.sendEmptyMessage(MSG_LOAD_URL_ERROR);
            mQuestionnaireView.stopLoading();
            mQuestionnaireView.setWebViewClient(null);
            mIsError = true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            if (!mIsError) {
                mHandler.sendEmptyMessage(MSG_LOAD_URL_END);
            }
        }
    };

    class ProxyBridge {

        @JavascriptInterface
        public String getDeviceInfo() {
            StringBuffer deviceInfo = new StringBuffer();
            deviceInfo.append(SystemPropertiesUtils.getImei(mContext)).append("&");
            deviceInfo.append(SystemPropertiesUtils.getInternalVersion()).append("&");
            deviceInfo.append(SystemPropertiesUtils.getModel());
            Log.i(TAG, "getDeviceInfo  info = " + deviceInfo.toString());
            SystemUpdateFactory.questionnaire(mContext).questionnaireRead();
            return deviceInfo.toString();
        }

        @JavascriptInterface
        public boolean isNetworkAvailable() {
            return NetworkUtils.isNetworkAvailable(mContext);
        }

    }
}
