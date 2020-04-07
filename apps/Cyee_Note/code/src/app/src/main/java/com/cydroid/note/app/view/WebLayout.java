package com.cydroid.note.app.view;

import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebView;
import android.widget.FrameLayout;

import com.cydroid.note.R;

/**
 * Created by spc on 16-10-18.
 */
public class WebLayout extends FrameLayout {

    public WebLayout(Context context) {
        super(context);
    }

    public WebLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDetachedFromWindow() {
        WebView webView = (WebView) findViewById(R.id.web_show_view);
        if (null != webView) {
            removeView(webView);
            webView.setDownloadListener(null);
            webView.destroy();
        }
        super.onDetachedFromWindow();
    }
}
