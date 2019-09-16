package com.cydroid.ota.ui;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.text.TextUtils;
import android.webkit.*;
import android.widget.Toast;
import com.cydroid.ota.Log;
import com.cydroid.ota.utils.*;
import com.cydroid.ota.R;
import com.cydroid.ota.bean.IUpdateInfo;
import com.cydroid.ota.storage.IStorage;
import com.cydroid.ota.storage.IStorageUpdateInfo;
import com.cydroid.ota.storage.Key;
import com.cydroid.ota.storage.SettingUpdateDataInvoker;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class CacheLoadingManager {
    private static final String TAG = "CacheLoadingManager";
    private LoadingStateChangeListener mLoadingStateChangeListener;
    private WebView mWebView;
    private Context mContext;
    private InternalWebViewClient mWebViewClient;
    private volatile LoadingState mLoadingState;

    private static final String CACHER_TYPE_HTML = ".html";
    private static final String CACHER_TYPE_DESC = ".desc";
    private static final String CACHER_TYPE_PNG = ".png";
    private String mInternalVersion = "";

    public CacheLoadingManager(WebView webView, Context context,
            String internalVersion) {
        mWebView = webView;
        mContext = context;
        mInternalVersion = internalVersion;
        if (!NetworkUtils.isNetworkAvailable(mContext) && !isHasCache()) {
            setLoadingState(LoadingState.ERROR);
        } else {
            setLoadingState(LoadingState.LOADING);
        }

        mWebViewClient = new InternalWebViewClient();
        mWebView.setWebViewClient(mWebViewClient);
        mWebView.setWebChromeClient(mWebChromeClient);
    }

    public void registerLoadingStateChangeListener(
            LoadingStateChangeListener listener) {
        mLoadingStateChangeListener = listener;
        if (mLoadingStateChangeListener != null) {
            mLoadingStateChangeListener.onLoadingStateChange(mLoadingState);
        }
    }

    public void unRegisterLoadingStateChangeListener() {
        mLoadingStateChangeListener = null;
        mLoadingState = LoadingState.LOADING;
    }

    public void setLoadingState(LoadingState state) {
        synchronized (this) {
            if (mLoadingState == state) {
                return;
            }
            mLoadingState = state;
        }
        if (mLoadingStateChangeListener != null) {
            mLoadingStateChangeListener.onLoadingStateChange(state);
        }
    }

    public boolean isLoadingError() {
        return mWebViewClient.isLoadError();
    }

    private WebChromeClient mWebChromeClient = new WebChromeClient() {
        public void onProgressChanged(WebView view, int progress) {
            Log.d(TAG, "progress = " + progress);
            if (progress == 100) {
                setLoadingState(LoadingState.SUCCESS);
            } else {
                setLoadingState(LoadingState.LOADING);
            }
        }

    };

    class InternalWebViewClient extends WebViewClient {
        private boolean mIsError = false;

        @Override
        public void onReceivedError(WebView view, int errorCode,
                String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            Log.d(TAG, "WebViewClient onReceivedError() errorCode = "
                    + errorCode + " description = " + description
                    + "failingUrl = " + failingUrl);
            setLoadingState(LoadingState.ERROR);
            mWebView.stopLoading();
            mWebView.setWebChromeClient(null);
            mIsError = true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            Log.d(TAG, "onPageFinished mIsError:"
                    + mIsError);
            if (!mIsError) {
                setLoadingState(LoadingState.SUCCESS);
                mWebView.loadUrl(
                        "javascript:window.handler.cacherHtml(document.getElementsByTagName('html')[0].innerHTML);");
                mWebView.loadUrl("javascript:window.handler.cacheDesc(getsharedesc());");
                mWebView.loadUrl("javascript:window.handler.cacherIcon(getshareimg());");
            }

        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (!NetworkUtils.isNetworkAvailable(mContext)) {
                showNoNetworkToast(mContext);
                mWebView.stopLoading();
                return true;
            }
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setPackage("com.android.browser");
                mContext.startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
            } finally {
                Log.d(TAG, "shouldOverrideUrlLoading ");
            }
            return true;
        }

        protected boolean isLoadError() {
            return mIsError;
        }
    }

    public interface LoadingStateChangeListener {
        void onLoadingStateChange(LoadingState state);
    }

    private boolean isHasCache() {
        StringBuilder path = new StringBuilder(mContext.getFilesDir().getAbsolutePath());
        path.append(File.separator);
        path.append(getCacherTypeFileName(CACHER_TYPE_HTML));
        File file = new File(path.toString());
        if (file != null && file.exists()) {
            Log.d(TAG, "has cache file");
            return true;
        }
        Log.d(TAG, "has no cache file !   path = " + path.toString());
        return false;
    }

    public void setWebViewFromCache(String url) {
        deleteCacheFile();
        Log.d(TAG, "setWebViewFromCache url = " + url);
        String content = getDataWithCacherType(CACHER_TYPE_HTML);
        if (content.isEmpty()) {
            webviewLoad(url);
        } else {
            mWebView.loadDataWithBaseURL(null, content, "text/html", "utf-8", null);
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void webviewLoad(String url) {
        Log.d(TAG, "webviewLoad");
        if (!NetworkUtils.isNetworkAvailable(mContext)) {
            setLoadingState(LoadingState.ERROR);
            showNoNetworkToast(mContext);
            return;
        }
        setLoadingState(LoadingState.LOADING);
        WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        mWebView.addJavascriptInterface(new CacherHandler(), "handler");

        mWebView.loadUrl(url);
    }

    public void loadTextImproveInfoFromCacher(boolean isCurrentVersionInfo) {
        Log.d(TAG, " loadTextImproveInfoFromCacher isCurrentVersionInfo:"
                + isCurrentVersionInfo);
        String improveInfo = "";
        IStorageUpdateInfo storageUpdateInfo = SettingUpdateDataInvoker.getInstance(mContext).settingUpdateInfoStorage();
        IUpdateInfo updateInfo =  storageUpdateInfo.getUpdateInfo();
        IStorage storage = SettingUpdateDataInvoker.getInstance(mContext).settingStorage();
        if (isCurrentVersionInfo) {
            improveInfo = storage.getString(Key.Setting.KEY_SETTING_UPDATE_CURRENT_RELEASENOTE, "");
        } else {
            improveInfo = updateInfo.getReleaseNote();
        }
        //cuijiuyu modify for CR01749382
        improveInfo = improveInfo.replace("\n", "<br>");
        Log.d(TAG, " loadTextImproveInfoFromCacher improveInfo:" + improveInfo);
        mWebView.loadDataWithBaseURL(null, improveInfo, "text/html", "utf-8",
                null);
    }

    private void deleteCacheFile() {
        String currentVersion = SystemPropertiesUtils.getInternalVersion();
        IStorageUpdateInfo storageUpdateInfo = SettingUpdateDataInvoker.getInstance(mContext).settingUpdateInfoStorage();
        IUpdateInfo updateInfo =  storageUpdateInfo.getUpdateInfo();
        String newVersion = updateInfo.getInternalVer();
        String[] fileList = mContext.fileList();
        Log.d(TAG, "cacher files = " + fileList.length);
        for (String path : fileList) {
            //Log.d(TAG, "cacher file : " + path);
            if (!path.contains(currentVersion) && !path.contains(newVersion)) {
                mContext.deleteFile(path);
            }
        }
    }

    public Bitmap getBitmap() {
        Bitmap bitmap = null;
        InputStream is = null;
        try {
            is = mContext.openFileInput(getCacherTypeFileName(CACHER_TYPE_PNG));
            bitmap = BitmapFactory.decodeStream(is);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }
        }
        return bitmap;
    }

    public String getDesc() {
        return getDataWithCacherType(CACHER_TYPE_DESC);
    }

    private String getDataWithCacherType(String type) {
        FileInputStream fis = null;
        BufferedReader in = null;
        try {
            String fileName = getCacherTypeFileName(type);
            Log.d(TAG, "getDataWithCacherType file = " + fileName);
            fis = mContext.openFileInput(fileName);
            int length = fis.available();
            Log.d(TAG, "available = " + length);
            if (length > 0) {
                in = new BufferedReader(new InputStreamReader(fis,
                        Constants.UTF_8_CHARSET));
                Log.d(TAG, "start get this file content");
                char[] buffer = new char[length];
                in.read(buffer);
                String content = new String(buffer);
                return content;
            }
        } catch (FileNotFoundException e) {
            Log.e(TAG, type + " file not found!!!");
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException io) {
                Log.e(TAG, io.getMessage());
            }
        }
        return "";
    }

    private String getCacherTypeFileName(String type) {
        if (!TextUtils.isEmpty(mInternalVersion)) {
            return mInternalVersion + type;
        }
        return "";
    }

    class CacherHandler {
        @JavascriptInterface
        public void cacherHtml(String data) {
            Log.d(TAG, "data:" + data);
            FileOutputStream fos = null;
            try {
                fos = mContext.openFileOutput(getCacherTypeFileName(CACHER_TYPE_HTML),
                        Context.MODE_PRIVATE);
                fos.write(data.getBytes(Constants.UTF_8_CHARSET));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (fos != null) {
                        fos.flush();
                        fos.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        @JavascriptInterface
        public void cacheDesc(String desc) {
            Log.d(TAG, "desc :" + desc);
            FileOutputStream fos = null;
            try {
                fos = mContext.openFileOutput(getCacherTypeFileName(CACHER_TYPE_DESC),
                        Context.MODE_PRIVATE);
                fos.write(desc.getBytes(Constants.UTF_8_CHARSET));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (fos != null) {
                        fos.flush();
                        fos.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        @JavascriptInterface
        public void cacherIcon(String data) {
            Log.d(TAG, "cacherIcon icon url :" + data);
            FileOutputStream fos = null;
            InputStream in = null;
            try {
                fos = mContext.openFileOutput(getCacherTypeFileName(CACHER_TYPE_PNG),
                        Context.MODE_PRIVATE);
                URL url = new URL(data);
                HttpURLConnection conn = (HttpURLConnection) url
                        .openConnection();
                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    in = conn.getInputStream();
                    Log.d(TAG, "" + in.available());
                    int c = 0;
                    byte[] buffer = new byte[1024];
                    while ((c = in.read(buffer)) != -1) {
                        fos.write(buffer, 0, c);
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, e.toString());
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            } finally {
                try {
                    if (null != in) {
                        in.close();
                    }
                    if (null != fos) {
                        fos.flush();
                        fos.close();
                    }
                } catch (IOException e) {
                    Log.e(TAG, e.toString());
                }

            }
        }
    }

    private void showNoNetworkToast(Context context) {
        Toast.makeText(context, context.getString(R.string.gn_su_layout_main_expend_text_network_error),
                Toast.LENGTH_SHORT).show();
    }

    public enum LoadingState {
        LOADING, SUCCESS, ERROR;
    }
}
