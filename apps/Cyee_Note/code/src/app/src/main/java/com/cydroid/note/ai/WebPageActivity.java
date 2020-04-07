package com.cydroid.note.ai;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Bundle;
import com.cydroid.note.common.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.cydroid.note.R;
import com.cydroid.note.app.view.StandardActivity;
import com.cydroid.note.web.NoteWebView;

public class WebPageActivity extends StandardActivity implements StandardActivity.StandardAListener {
    public final static String DATA_TITLE = "data_title";
    public final static String DATA_URL = "data_url";

    private NoteWebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        String title = intent.getExtras().getString(DATA_TITLE);
        String url = intent.getExtras().getString(DATA_URL);
        setTitle(title);
        setStandardAListener(this);
        setNoteContentView(R.layout.web_page_activity_content_ly);
        mWebView = (NoteWebView) findViewById(R.id.web_show_view);
        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.web_load_progress);
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                progressBar.setVisibility(View.VISIBLE);
                view.getSettings().setBlockNetworkImage(true);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.GONE);
                view.getSettings().setBlockNetworkImage(false);
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();
            }
        });
        mWebView.loadUrl(url);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mWebView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mWebView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClickHomeBack() {
        finish();
    }

    @Override
    public void onClickRightView() {
    }

    @Override
    public boolean onKeyDown(int key, KeyEvent event) {
        if (key == KeyEvent.KEYCODE_BACK) {
            if (mWebView.canGoBack()) {
                mWebView.goBack();
                return true;
            }
        }
        return super.onKeyDown(key, event);
    }
}
