package com.cydroid.ota.ui;

import cyee.app.CyeeActionBar;
import cyee.widget.CyeeTextView;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import com.cydroid.ota.Log;
import com.cydroid.ota.SettingUpdateApplication;
import com.cydroid.ota.utils.*;

import com.cydroid.ota.R;

public class DetailsInfoActivity extends AbsActivity {
    private static final String TAG = "DetailsInfoActivity";
    public static final String MROE_INFO_URL = "moreInfoURL";
    public static final String IS_CURRENT_VERSION_INFO = "isCurrentVersionInfo";
    public static final String INTERNAL_VERSION = "internalVersion";
    public static final String RELEASE_NOTE_ID = "releaseNotesId";

    private WebView mMoreWebView = null;
    private CyeeTextView mErrorView = null;
    private LinearLayout mDetailLinearLayout = null;
    private ProgressBar mProgressBar = null;
    private TextView mTextView = null;
    private View mView;
    private String mUrl;
    private String mInternalVersion = "";
    private boolean mIsCurrentVersionInfo = false;
    public static String mReleaseNotesId = "";
    private CacheLoadingManager mCacheLoadingManager;
    private CacheLoadingManager.LoadingState mLoadingState = CacheLoadingManager.LoadingState.ERROR;

    @Override
    protected final void onCreate(Bundle savedInstanceState) {
        SettingUpdateApplication application = (SettingUpdateApplication) getApplication();
        if (!application.getSystemTheme().isNeedChangeColor()) {
            setTheme(R.style.Theme_Light_NormalTheme);
            if (SystemPropertiesUtils.getBlueStyle()) {
                setTheme(R.style.Theme_Light_BlueTheme);
            }
        }
        super.onCreate(savedInstanceState);
        parseIntent();
        initActionBar();
        setContentView(R.layout.gn_su_layout_version_detail_info);
        initView();
        init();
    }

    private void parseIntent() {
        Intent intent = getIntent();
        String url = intent.getStringExtra(MROE_INFO_URL);
        mUrl = url;
        // just for test!
        //mUrl = "http://dl.gionee.com/ota/res/cm/802/1431686841480_EuC2p.html";

        mIsCurrentVersionInfo = intent.getBooleanExtra(IS_CURRENT_VERSION_INFO, false);
        mInternalVersion = intent.getStringExtra(INTERNAL_VERSION);
        mReleaseNotesId = getIntent().getStringExtra(RELEASE_NOTE_ID);
        Log.d(TAG, "mReleaseNotesId = " + mReleaseNotesId);
    }

    protected void init() {
        Log.d(TAG, "onCreate() mUrl = " + mUrl + " mIsCurrentVersionInfo "
                + mIsCurrentVersionInfo);
        mCacheLoadingManager = new CacheLoadingManager(mMoreWebView, DetailsInfoActivity.this,
                mInternalVersion);
        setWebView();
    }

    protected void initView() {
        Log.d(TAG, "initView");
        mView = (View) findViewById(R.id.info_more);
        mView.setBackgroundColor(
                getResources().getColor(R.color.gn_su_new_version_page_background));
        mView.setOnClickListener(mOnClickListener);
        mMoreWebView = (WebView) findViewById(R.id.ready_webview);
        mErrorView = (CyeeTextView) findViewById(R.id.gn_su_id_network_error);
        mDetailLinearLayout = (LinearLayout) findViewById(R.id.picture_more);
        mProgressBar = (ProgressBar) findViewById(R.id.progress_webview);
        mTextView = (TextView) findViewById(R.id.progress_textview);
        mTextView.setTextColor(Color.GRAY);
        mProgressBar.setMax(100);
        updateLoadingProgressView(View.GONE);
        mMoreWebView.setVisibility(View.INVISIBLE);
        mDetailLinearLayout.setVisibility(View.INVISIBLE);
        mMoreWebView.setBackgroundColor(Color.TRANSPARENT);
        mDetailLinearLayout.setBackgroundColor(Color.TRANSPARENT);
        mMoreWebView.setOnLongClickListener(
                new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        return true;
                    }
                });
        //add by cuijiuyu
        mDetailLinearLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
              Intent intent = new Intent(DetailsInfoActivity.this, ImageReleaseNoteActivity.class);
              intent.putExtra(ImageReleaseNoteActivity.RELEASE_NOTE_ID,mReleaseNotesId);
              startActivity(intent);
            }
        });
    }

    protected void initActionBar() {
        CyeeActionBar actionBar = getCyeeActionBar();
        if (actionBar == null) {
            return;
        }
        actionBar.setDisplayOptions(CyeeActionBar.DISPLAY_SHOW_TITLE |
                CyeeActionBar.DISPLAY_HOME_AS_UP | CyeeActionBar.DISPLAY_SHOW_HOME);
        if (mIsCurrentVersionInfo) {
            actionBar.setTitle(R.string.gn_su_string_settings_curVersion_desc);
        } else {
            actionBar.setTitle(R.string.gn_su_string_setting_newVersion_desc);
        }
    }

    private void updateLoadingProgressView(int visibility) {
        mTextView.setVisibility(visibility);
        mProgressBar.setVisibility(visibility);
    }

    @Override
    protected final void onDestroy() {
        mCacheLoadingManager.unRegisterLoadingStateChangeListener();
        mCacheLoadingManager = null;
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    public void loadWebView() {
        Log.d(TAG, "loadWebView mUrl = " + mUrl );
        if (TextUtils.isEmpty(mUrl)) {
            mCacheLoadingManager.loadTextImproveInfoFromCacher(mIsCurrentVersionInfo);
        } else {
            mCacheLoadingManager.setWebViewFromCache(mUrl);
        }
    }

    private void setWebView() {
        Log.d(TAG, "setWebView");
        WebSettings webSettings = mMoreWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setRenderPriority(WebSettings.RenderPriority.HIGH);
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        mMoreWebView.setHorizontalScrollBarEnabled(false);
        mMoreWebView.setVerticalScrollBarEnabled(true);
        mMoreWebView.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return true;
            }
        });

        Log.d(TAG, "mMoreWebView.isHardwareAccelerated() : " + mMoreWebView
                .isHardwareAccelerated());
        mCacheLoadingManager.registerLoadingStateChangeListener(mLoadingStateChangeListener);
        loadWebView();
    }

    private void updateContentView(CacheLoadingManager.LoadingState state) {
        if (mCacheLoadingManager == null) {
            return;
        }
        mLoadingState = state;
        Log.d(TAG, "updateContentView LoadingState = " + state);
        switch (state) {
            case LOADING:
                updateLoadingProgressView(View.VISIBLE);
                mMoreWebView.setVisibility(View.INVISIBLE);
                mDetailLinearLayout.setVisibility(View.INVISIBLE);
                mErrorView.setVisibility(View.INVISIBLE);
                break;
            case ERROR:
                updateLoadingProgressView(View.GONE);
                mMoreWebView.setVisibility(View.INVISIBLE);
                mDetailLinearLayout.setVisibility(View.INVISIBLE);
                mErrorView.setVisibility(View.VISIBLE);
                break;
            case SUCCESS:
                updateLoadingProgressView(View.GONE);
                mMoreWebView.setVisibility(View.VISIBLE);
                if (mReleaseNotesId == null || mReleaseNotesId.equals("") || mReleaseNotesId.equals("0") || mReleaseNotesId.equals("null"))  {
		    //Gionee zhouhuiquan 2017-02-22 add for 69729 begin
                    FrameLayout.LayoutParams frameParams = (FrameLayout.LayoutParams) mMoreWebView.getLayoutParams();
                    frameParams.height = FrameLayout.LayoutParams.WRAP_CONTENT;
                    mMoreWebView.setLayoutParams(frameParams);
                    //Gionee zhouhuiquan 2017-02-22 add for 69729 end
                    mDetailLinearLayout.setVisibility(View.INVISIBLE);
                } else {                   
                    mDetailLinearLayout.setVisibility(View.VISIBLE);
                }
                mErrorView.setVisibility(View.INVISIBLE);

                break;
            default:
                break;
        }
        invalidateOptionsMenu();
    }

    private CacheLoadingManager.LoadingStateChangeListener mLoadingStateChangeListener
            = new CacheLoadingManager.LoadingStateChangeListener() {

        @Override
        public void onLoadingStateChange(CacheLoadingManager.LoadingState state) {
            updateContentView(state);
        }

    };

    private OnClickListener mOnClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            int id = v.getId();
            switch (id) {
                case R.id.info_more:
                    if (mErrorView.getVisibility() == View.VISIBLE
                            && NetworkUtils.isNetworkAvailable(
                            getApplicationContext())) {
                        loadWebView();
                    }
                    break;
                default:
                    break;
            }
        }
    };
}
