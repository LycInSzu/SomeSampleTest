package com.cydroid.note.web;

import android.app.DownloadManager;
import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import com.cydroid.note.R;
import com.cydroid.note.app.utils.ToastManager;
import com.cydroid.note.common.Constants;

public class NoteWebView extends WebView {

    private Context mContext;

    public NoteWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initWebSetting();
        setDownloadListener(mDownloadListener);
    }

    public void initWebSetting() {
        requestFocus();
        setSelected(true);
        setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
        WebSettings webSettings = getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setSupportZoom(false);
        webSettings.setBuiltInZoomControls(false);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUserAgentString(ProductConfigration.getUAString());
        webSettings.setTextZoom(100);
        initCache();
    }

    private void initCache() {
        WebSettings webSettings = getSettings();
        webSettings.setAppCacheEnabled(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        webSettings.setAppCachePath(getContext().getDir("web_appcache", Context.MODE_PRIVATE)
                .getPath());
    }


    private DownloadListener mDownloadListener = new DownloadListener() {

        @Override
        public void onDownloadStart(String url, String userAgent, String contentDisposition,
                                    String mimetype, long contentLength) {
            if (!"application/octet-stream".equals(mimetype) && (TextUtils.isEmpty(mimetype)
                    || !mimetype.startsWith("image/"))) {
                Toast.makeText(mContext, R.string.download_unsurpport_hint, Toast.LENGTH_SHORT)
                        .show();
                return;
            }
            String fileName = "";
            if (!TextUtils.isEmpty(contentDisposition)) {
                String[] typeArray = contentDisposition.split(";");
                fileName = typeArray[1];
            }
            DownloadManager downloadManager = (DownloadManager) mContext.getSystemService(
                    Context.DOWNLOAD_SERVICE);
            OnlineImageDownloadHandler.download(downloadManager, url, fileName.replace("filename=",""), getResources()
                    .getString(R.string.notification_title));
            StringBuilder hint = new StringBuilder();
            hint.append(mContext.getResources().getString(R.string.download_poto_hint))
                    .append(Constants.ROOT_FILE.getPath())
                    .append(Constants.NOTE);
            new ToastManager(mContext.getApplicationContext()).showToast(hint.toString());
        }
    };

}
