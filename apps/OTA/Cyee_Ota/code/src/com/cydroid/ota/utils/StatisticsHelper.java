package com.cydroid.ota.utils;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import com.cydroid.ota.Log;

import com.cydroid.statistics.StatisticsSession;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import cyee.provider.CyeeSettings;
import android.os.SystemProperties;

/**
 * Created by yewq on 2018-1-16.
 */

public class StatisticsHelper {
    private static final String TAG = "Statistics";
    private static final boolean DEBUG_LOG = true;

    private static final boolean mUserExperienceSupport= SystemProperties.get("cy.userexperience.support", "no").equals("yes");

    public static final String EVENT_CHECK_NEW = "ManualCheckNew";//手动检测
    public static final String EVENT_START_DOWNLOAD = "StartDownload";//开始下载
    public static final String EVENT_FAIL_DOWNLOAD = "FailDownload";//下载失败
    public static final String EVENT_FINISH_DOWNLOAD = "FinishDownload";//下载完成
    public static final String EVENT_START_UPGRADE = "StartUpgrade";//开始升级
    public static final String EVENT_FINISH_UPGRADE = "FinishUpgrade";//升级完成

	//Chenyee <CY_Req> <xuyongji> <20180629> modify for CSW1803A-681 begin
    public static final String KEY_NET_TYPE = "NetType"; //开始下载参数，网络类型：数据或wifi
    public static final String KEY_DOWNLOAD_FAIL_REASON = "DownloadFailReason"; //下载失败参数：失败原因
    public static final String KEY_UPGRADED_VERSION_INFO = "UpgradeVersionInfo"; //下载失败/成功和升级完成参数：升级后版本号
    public static final String KEY_CURRENT_VERSION = "CurrentVersionInfo"; //手动检测参数：当前软件版本
	//Chenyee <CY_Req> <xuyongji> <20180629> modify for CSW1803A-681 end


    private static StatisticsHelper sInstance = null;
    private final Context mContext;
    private StatisticsSession mSession;
    private final AtomicBoolean mIsUserExperienceOpen = new AtomicBoolean(false);
    private final ContentObserver mUserExperienceObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            Log.d(TAG, "User experience switch changed to " + selfChange);
            mIsUserExperienceOpen.set(isUserExperienceOpen(mContext));
            init(mContext);
        }
    };

    private StatisticsHelper(Context context) {
        mContext = context;
        if (mUserExperienceSupport) {
            mIsUserExperienceOpen.set(isUserExperienceOpen(context));
            context.getContentResolver().
                    registerContentObserver(CyeeSettings.getUriFor(CyeeSettings.USER_EXPERIENCE),
                            false, mUserExperienceObserver);
        }
    }

    private boolean isUserExperienceOpen(Context context){
        return CyeeSettings.getInt(context.getContentResolver(), CyeeSettings.USER_EXPERIENCE, 0) == 1;
    }

    private boolean isUserExperienceOpen() {
        return mIsUserExperienceOpen.get();
    }


    private void init(Context context) {
        if (mSession != null) return;

        if (isUserExperienceOpen()) {
            mSession = new StatisticsSession(context.getApplicationContext());
            mSession.open();
            mSession.upload();
        }
    }

    public static StatisticsHelper getInstance(Context context) {
        if (!mUserExperienceSupport) {
            return null;
        }
        if (sInstance == null) {
            sInstance = new StatisticsHelper(context);
        }

        sInstance.init(context);

        return sInstance;
    }

    public void onDestroy() {
        if (mUserExperienceSupport) {
            mContext.getContentResolver().
                    unregisterContentObserver(mUserExperienceObserver);
        }

        if (sInstance != null){
            sInstance = null;
        }
    }

    public void tagEvent(String event) {
        if (isUserExperienceOpen()) {
            mSession.tagEvent(event);

            if (DEBUG_LOG) {
                Log.d(TAG, "<" + event + " />");
            }
        }
    }

    public void tagEvent(String event, String key, String value){
        if (isUserExperienceOpen()) {
            Map<String, String> attrs = new HashMap<String, String>();
            attrs.put(key, value);
            mSession.tagEvent(event, attrs);

            if (DEBUG_LOG) {
                StringBuilder sb = new StringBuilder();
                for (Map.Entry<String, String> attr : attrs.entrySet()) {
                    sb.append(attr.getKey()).append("=\"").append(attr.getValue()).append("\" ");
                }
                Log.d(TAG, "<" + event + " " + sb.toString() + " />");
            }
        }
    }

    //Chenyee <CY_Req> <xuyongji> <20180629> modify for CSW1803A-681 begin
	public void tagEvent(String event, Map<String, String> attrs) {
        if (isUserExperienceOpen()) {
            mSession.tagEvent(event, attrs);

            if (DEBUG_LOG) {
                StringBuilder sb = new StringBuilder();
                for (Map.Entry<String, String> attr : attrs.entrySet()) {
                    sb.append(attr.getKey()).append("=\"").append(attr.getValue()).append("\" ");
                }
                Log.d(TAG, "<" + event + " " + sb.toString() + " />");
            }
        }
    }
	//Chenyee <CY_Req> <xuyongji> <20180629> modify for CSW1803A-681 end

    public void onResume() {
        if (isUserExperienceOpen()) {
            mSession.open();
        }
    }

    public void onPause() {
        if (isUserExperienceOpen()) {
            mSession.close();
            mSession.upload();
        }
    }

    public void writeStatisticsData(String event) {
        onResume();
        tagEvent(event);
        onPause();
    }

    public void writeStatisticsData(String event, String key, String value) {
        onResume();
        tagEvent(event, key, value);
        onPause();
    }

	//Chenyee <CY_Req> <xuyongji> <20180629> modify for CSW1803A-681 begin
    public void writeStatisticsData(String event, Map<String, String> attrs) {
        onResume();
        tagEvent(event,attrs);
        onPause();
    }
	//Chenyee <CY_Req> <xuyongji> <20180629> modify for CSW1803A-681 end
}
