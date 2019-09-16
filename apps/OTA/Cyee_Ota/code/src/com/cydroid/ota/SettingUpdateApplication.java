package com.cydroid.ota;

import com.cydroid.ota.logic.SystemUpdateFactory;
import com.cydroid.ota.storage.IStorage;
import com.cydroid.ota.storage.Key;
import com.cydroid.ota.storage.SettingUpdateDataInvoker;
import com.cydroid.ota.utils.Util;
import android.app.Application;

import java.io.File;

import cyee.changecolors.ChameleonColorManager;
//Chenyee <CY_Req> <xuyongji> <20180417> modify for CSW1702SE-197 begin
import com.cydroid.ota.utils.StatisticsHelper;
//Chenyee <CY_Req> <xuyongji> <20180417> modify for CSW1702SE-197 end

/**
 * Created by liuyanfeng on 15-4-21.
 */
public class SettingUpdateApplication  extends Application{
    private static final String TAG = "SettingUpdateApplication";

    private SystemTheme mSystemTheme;
	//Chenyee <CY_Req> <xuyongji> <20180417> modify for CSW1702SE-197 begin
    public static StatisticsHelper sStatisticsHelper = null;
	//Chenyee <CY_Req> <xuyongji> <20180417> modify for CSW1702SE-197 end
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate()");
        IStorage setting = SettingUpdateDataInvoker.getInstance(getApplicationContext()).settingStorage();
        boolean isFirstStart = setting.getBoolean(Key.Setting.KEY_FIRST_START_SYSTEM_UPDATE_FLAG,true);
        if (isFirstStart) {
            setting.putBoolean(Key.Setting.KEY_FIRST_START_SYSTEM_UPDATE_FLAG,false);
            clearLaucherBadge();
            clearCache(new File(getCacheFilePath()));
        }
        ChameleonColorManager.getInstance().register(this,false);
		//Chenyee <CY_Req> <xuyongji> <20180417> modify for CSW1702SE-197 begin
        if (sStatisticsHelper == null) {
            //Log.d(TAG, "StatisticsHelper getInstance()");
            sStatisticsHelper = StatisticsHelper.getInstance(this);
        }
		//Chenyee <CY_Req> <xuyongji> <20180417> modify for CSW1702SE-197 end
        SystemUpdateFactory.autoUpgrade(this).registerBatteryReceiver();
        SystemUpdateFactory.localVersionCheck(this).checkLocalVersion();
    }

    public SystemTheme getSystemTheme() {
        if (mSystemTheme == null) {
            mSystemTheme = new SystemTheme(this);
        }
        return mSystemTheme;
    }

    private String getCacheFilePath(){
        StringBuffer buffer = new StringBuffer();
        buffer.append("data/data/");
        buffer.append(getPackageName());
        buffer.append("/shared_prefs");
        return buffer.toString();
    }

    private void clearCache(File file) {
        if (null != file && file.exists() && file.isFile()) {
            file.delete();
            return;
        }
        if (null != file && file.exists() && file.isDirectory()) {

            for (File item : file.listFiles()) {
                //Log.d(TAG, "item:" + item.getAbsolutePath());
                clearCache(item);
            }

        }

    }

    private void clearLaucherBadge() {
        Util.notifyLaucherShowBadge(getApplicationContext(),0);
    }

}
