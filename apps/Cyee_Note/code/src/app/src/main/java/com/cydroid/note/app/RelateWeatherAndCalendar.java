package com.cydroid.note.app;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import com.cydroid.note.common.Log;

import com.gionee.amiweather.library.QueryConstant;
import com.gionee.amiweather.library.WeatherData;
import com.cydroid.note.R;
import com.cydroid.note.app.utils.APKUtils;
import com.cydroid.note.app.utils.PackageUtils;
import com.cydroid.note.app.utils.ToastManager;
import com.cydroid.note.common.NoteUtils;
import com.cydroid.note.common.PlatformUtil;
import com.cydroid.note.common.ThreadPool;

import java.io.File;

import cyee.app.CyeeAlertDialog;

/**
 * Created by wuguangjie on 16-7-13.
 */
public class RelateWeatherAndCalendar {
    private static final String PACKAGE_NAME_CALENDAR_OUTER = "com.gionee.amicalendar";
    private static final String PACKAGE_NAME_WEATHER_OUTER = "com.gionee.amiweather";
    private static final String PACKAGE_NAME_CALENDAR_INNER = "com.android.calendar";
    private static final String PACKAGE_NAME_WEATHER_INNER = "com.coolwind.weather";
    private static final String URL_DOWNLOAD_AMICALENDAR = "http://res.weather.gionee.com/apk/Ami_Calendar.apk";
    private static final String URL_DOWNLOAD_AMIWEATHER = "http://res.weather.gionee.com/apk/Ami_Weather.apk";
    private static final String ACTION_OUTER_WEATHER = "com.gionee.amiweather.SplashActivity_ACTION";
    public static final String PATH_WEATHER_FILE = Build.VERSION.SDK_INT > 21 ? "cyee/AmiNote" : "Cyee/AmiNote";
    public static final String APK_STORED_PATH = PATH_WEATHER_FILE + "/downloadApk";
    private static final String AMI_WEATHER_APK = "/Ami_Weather.apk";
    private static final int UNKNOW_STATUS = 0;
    private Context mContext;
    private OnRelateDataListener mListener;
    private ToastManager mToastManager;
    private static final String TAG = "RelateWeatherAndCalendar";
    
    public RelateWeatherAndCalendar(OnRelateDataListener listener) {
        mContext = NoteAppImpl.getContext();
        mListener = listener;
        mToastManager = new ToastManager(mContext);
    }

    public interface OnRelateDataListener {
        void onRelateDataFinished(WeatherData situation);
    }

    public void queryWeatherInfo() {
        NoteAppImpl.getContext().getThreadPool().submit(new ThreadPool.Job<Object>() {
            @Override
            public Object run(ThreadPool.JobContext jc) {
                Uri uri = PlatformUtil.isGioneeDevice() ? QueryConstant.QUERY_WEATHER_UNCASE_LANGUAGE_FOR_GIONEE
                        : QueryConstant.QUERY_WEATHER_UNCASE_LANGUAGE;
                Cursor cursor = null;
                try {
                    cursor = mContext.getContentResolver().query(uri, null, null, null, null);
                    if (null != cursor && cursor.moveToFirst()) {
                        WeatherData situation = WeatherData.obtain(cursor);
                        if (situation != null && mListener != null) {
                            mListener.onRelateDataFinished(situation);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    NoteUtils.closeSilently(cursor);
                }
                return null;
            }
        });
    }

    public void goToWeather(Activity activity) {
        if (PlatformUtil.isGioneeDevice()) {
            try {
                Intent intent = getWeatherActivityIntent();
                if (intent != null) {
                    activity.startActivity(intent);
                } else {
                    mToastManager.showToast(R.string.goto_weather_failed);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            if (PackageUtils.isPackageInstalled(mContext, PACKAGE_NAME_WEATHER_OUTER)) {
                try {
                    Intent intent = PackageUtils.getAppLaunchIntent(mContext, PACKAGE_NAME_WEATHER_OUTER);
                    intent.setAction(ACTION_OUTER_WEATHER);
                    intent.addCategory(Intent.CATEGORY_DEFAULT);
                    activity.startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                File apkFile = new File(Environment.getExternalStorageDirectory().getPath() + "/" + APK_STORED_PATH + AMI_WEATHER_APK);
                boolean isApkExist = DownloadManager.STATUS_SUCCESSFUL == getApkStatus(apkFile.getPath()) && apkFile.exists();
                if (isApkExist) {
                    installAmiWeatherApk(apkFile.getPath());
                    return;
                }

                if (!isDownloadingApk(apkFile.getPath())) {
                    downloadAmiAPKS(URL_DOWNLOAD_AMIWEATHER, activity);
                }
            }
        }
    }

    private boolean isDownloadingApk(String apkFilePath) {
        boolean isDownloading = false;
        int status = getApkStatus(apkFilePath);
        switch (status) {
            case DownloadManager.STATUS_PENDING:
            case DownloadManager.STATUS_PAUSED:
                isDownloading = true;
                mToastManager.showToast(R.string.down_load_paused_msg);
                break;
            case DownloadManager.STATUS_RUNNING: {
                isDownloading = true;
                mToastManager.showToast(R.string.down_load_dialog_msg);
                break;
            }
            case DownloadManager.STATUS_FAILED:
                new File(apkFilePath).delete();
                mToastManager.showToast(R.string.down_load_fail_msg);
                break;
            default:
                break;
        }
        return isDownloading;
    }

    private int getApkStatus(String path) {
        DownloadManager manager = (DownloadManager) NoteAppImpl.getContext().getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Query query = new DownloadManager.Query();
        Cursor cursor = null;
        try {
            cursor = manager.query(query);
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                final String filePath = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME));
                if (filePath.endsWith(path)) {
                    return cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return UNKNOW_STATUS;
    }

    public void goToCalender(Activity activity) {
        if (PlatformUtil.isGioneeDevice()) {
            try {
                activity.startActivity(getCalendarActivityIntent());
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            if (PackageUtils.isPackageInstalled(mContext, PACKAGE_NAME_CALENDAR_OUTER)) {
                try {
                    Intent intent = PackageUtils.getAppLaunchIntent(mContext, PACKAGE_NAME_CALENDAR_OUTER);
                    intent.setAction(Intent.ACTION_MAIN);
                    intent.addCategory(Intent.CATEGORY_APP_CALENDAR);
                    intent.addCategory(Intent.CATEGORY_DEFAULT);
                    activity.startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                downloadAmiAPKS(URL_DOWNLOAD_AMICALENDAR, activity);
            }
        }
    }

    private void downloadAmiAPKS(String url, Activity activity) {
        File file = new File(APK_STORED_PATH);
        if (!file.exists()) {
            file.mkdir(); //NOSONAR
        }
        if (APKUtils.isWifiConnected(mContext)) {
            downloadApks(mContext, url, file.getPath());
        } else if (APKUtils.isMobileConnected(mContext)) {
            showDownloadTipsDialog(url, file.getPath(), activity);
        } else {
            mToastManager.showToast(R.string.no_network);
        }
    }

    private void installAmiWeatherApk(String apkPath) {
        Intent install = new Intent(Intent.ACTION_VIEW);
        install.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        install.setDataAndType(Uri.parse("file://" + apkPath), "application/vnd.android.package-archive");
        NoteAppImpl.getContext().startActivity(install);
    }

    private void downloadApks(final Context context, final String url, final String path) {
        NoteAppImpl.getContext().getThreadPool().submit(new ThreadPool.Job<Object>() {
            @Override
            public Object run(ThreadPool.JobContext jc) {
                APKUtils.downloadApk(context, url, path);
                return null;
            }
        });
    }

    private void showDownloadTipsDialog(final String url, final String path, Activity activity) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.download_aim_apks_tip_dialog, null);
        CyeeAlertDialog.Builder builder = new CyeeAlertDialog.Builder(activity);
        TextView textView = (TextView) view.findViewById(R.id.authority_alert_user_title);
        int id = url.equals(URL_DOWNLOAD_AMIWEATHER) ? R.string.install_amiweather_tip : R.string.install_amicalendar_tip;
        textView.setText(mContext.getResources().getString(id));
        builder.setView(view);
        builder.setTitle(R.string.alert_user_title_str);
        builder.setPositiveButton(R.string.download, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                downloadApks(mContext, url, path);
            }
        });
        builder.setNegativeButton(R.string.alert_user_cancle, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        CyeeAlertDialog downloadDialog = builder.create();
        downloadDialog.setCanceledOnTouchOutside(false);
	    //Gionee wanghaiyan 2017-3-23 modify for 90801 begin
        downloadDialog.setCancelable(true);
	    //Gionee wanghaiyan 2017-3-23 modify for 90801 end
        Window window = downloadDialog.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setGravity(Gravity.BOTTOM);
        downloadDialog.show();
    }

    public Intent getCalendarActivityIntent() {
        Intent rtnIntent = PackageUtils.getAppLaunchIntent(mContext, PACKAGE_NAME_CALENDAR_INNER);
        if (rtnIntent == null) {
            rtnIntent = new Intent(Intent.ACTION_MAIN);
            rtnIntent.addCategory(Intent.CATEGORY_APP_CALENDAR);
            rtnIntent.addCategory(Intent.CATEGORY_DEFAULT);
        }
        rtnIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        rtnIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        return rtnIntent;
    }

    public Intent getWeatherActivityIntent() {
        Intent rtnIntent = PackageUtils.getAppLaunchIntent(mContext, PACKAGE_NAME_WEATHER_INNER);
        if (rtnIntent != null) {
        rtnIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        rtnIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        return rtnIntent;
    }

    public void destroy() {
        if (null != mToastManager) {
            mToastManager.destroy();
        }
    }
}
