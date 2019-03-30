/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.launcher3;

//add by zhouerlong prize launcher 20180906
import android.app.Activity;
import android.os.Build;

//prize add by zhouerlong badgeUnread begin
import com.android.launcher3.badge.BadgeTool;
//prize add by zhouerlong badgeUnread begin
import com.android.launcher3.theme.tools.AppConfig;
import com.android.launcher3.theme.tools.DefaultConfig;

import org.xutils.DbManager;
import org.xutils.ex.DbException;
import org.xutils.x;
//add by zhouerlong prize launcher 20180906
import android.content.ComponentName;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Looper;
import android.util.Log;

import com.android.launcher3.compat.LauncherAppsCompat;
import com.android.launcher3.compat.PackageInstallerCompat;
import com.android.launcher3.compat.UserManagerCompat;
import com.android.launcher3.config.FeatureFlags;
import com.android.launcher3.notification.NotificationListener;
import com.android.launcher3.util.ConfigMonitor;
import com.android.launcher3.util.Preconditions;
import com.android.launcher3.util.SettingsObserver;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import static com.android.launcher3.SettingsActivity.NOTIFICATION_BADGING;

public class LauncherAppState {

    public static final String ACTION_FORCE_ROLOAD = "force-reload-launcher";

    // We do not need any synchronization for this variable as its only written on UI thread.
    private static LauncherAppState INSTANCE;
    //prize add by lihuangyuan,for bug 71909,2019-02-27-start
    private static Object sWaitObject = new Object();
    //prize add by lihuangyuan,for bug 71909,2019-02-27-end
    private final Context mContext;
    private final LauncherModel mModel;
    private final IconCache mIconCache;
    private final WidgetPreviewLoader mWidgetCache;
    private final InvariantDeviceProfile mInvariantDeviceProfile;
    private final SettingsObserver mNotificationBadgingObserver;
//add by zhouerlong prize launcher 20180906
    public String mPath ="";
//add by zhouerlong prize launcher 20180906
    public static LauncherAppState getInstance(final Context context) {
        if (INSTANCE == null) {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                INSTANCE = new LauncherAppState(context.getApplicationContext());
                //prize add by lihuangyuan,for bug 71909,2019-02-27-start
                synchronized (sWaitObject)                
                {
                    //notify wait
                    sWaitObject.notifyAll();
                }
                //prize add by lihuangyuan,for bug 71909,2019-02-27-end
            } else {
                //prize modified by lihuangyuan,for bug 71909,2019-02-27-start
                //when launcher crashed or killed,eg. change font
                //this will cause thread deadlock,the main thread & the binder thread
                /*try {
                    return new MainThreadExecutor().submit(new Callable<LauncherAppState>() {
                        @Override
                        public LauncherAppState call() throws Exception {
                            return LauncherAppState.getInstance(context);
                        }
                    }).get();
                } catch (InterruptedException|ExecutionException e) {
                    throw new RuntimeException(e);
                }*/
                synchronized (sWaitObject)
                {
                    new MainThreadExecutor().execute(new Runnable(){
                        @Override
                        public void run(){
                            INSTANCE = LauncherAppState.getInstance(context.getApplicationContext());                            
                            synchronized (sWaitObject)
                            {
                                sWaitObject.notifyAll();
                            }
                        }
                    });
                        
                    try
                    {
                        //wait main thread init LauncherAppState
                        while(INSTANCE == null)
                        {                        
                            //Thread.sleep(100);
                            sWaitObject.wait();
                        }
                    }
                    catch(InterruptedException e)
                    {
                        throw new RuntimeException(e);
                    }                    
                }
                //prize modified by lihuangyuan,for bug 71909,2019-02-27-end
            }
        }
        return INSTANCE;
    }

    public static LauncherAppState getInstanceNoCreate() {
        return INSTANCE;
    }

    public Context getContext() {
        return mContext;
    }

    private LauncherAppState(Context context) {
        if (getLocalProvider(context) == null) {
            throw new RuntimeException(
                    "Initializing LauncherAppState in the absence of LauncherProvider");
        }
        Log.v(Launcher.TAG, "LauncherAppState initiated");
        Preconditions.assertUIThread();
        mContext = context;

        mInvariantDeviceProfile = new InvariantDeviceProfile(mContext);
        mIconCache = new IconCache(mContext, mInvariantDeviceProfile);
        mWidgetCache = new WidgetPreviewLoader(mContext, mIconCache);
        mModel = new LauncherModel(this, mIconCache, AppFilter.newInstance(mContext));

        LauncherAppsCompat.getInstance(mContext).addOnAppsChangedCallback(mModel);

        // Register intent receivers
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_LOCALE_CHANGED);
        // For handling managed profiles
        filter.addAction(Intent.ACTION_MANAGED_PROFILE_ADDED);
        filter.addAction(Intent.ACTION_MANAGED_PROFILE_REMOVED);
        filter.addAction(Intent.ACTION_MANAGED_PROFILE_AVAILABLE);
        filter.addAction(Intent.ACTION_MANAGED_PROFILE_UNAVAILABLE);
        filter.addAction(Intent.ACTION_MANAGED_PROFILE_UNLOCKED);

        if (FeatureFlags.IS_DOGFOOD_BUILD) {
            filter.addAction(ACTION_FORCE_ROLOAD);
        }

        mContext.registerReceiver(mModel, filter);
        UserManagerCompat.getInstance(mContext).enableAndResetCache();
        new ConfigMonitor(mContext).register();

        if (!mContext.getResources().getBoolean(R.bool.notification_badging_enabled)) {
            mNotificationBadgingObserver = null;
        } else {
            // Register an observer to rebind the notification listener when badging is re-enabled.
            mNotificationBadgingObserver = new SettingsObserver.Secure(
                    mContext.getContentResolver()) {
                @Override
                public void onSettingChanged(boolean isNotificationBadgingEnabled) {
                    if (isNotificationBadgingEnabled) {
                        NotificationListener.requestRebind(new ComponentName(
                                mContext, NotificationListener.class));
                    }
                }
            };
            mNotificationBadgingObserver.register(NOTIFICATION_BADGING);
        }
    }

    /**
     * Call from Application.onTerminate(), which is not guaranteed to ever be called.
     */
    public void onTerminate() {
        mContext.unregisterReceiver(mModel);
        final LauncherAppsCompat launcherApps = LauncherAppsCompat.getInstance(mContext);
        launcherApps.removeOnAppsChangedCallback(mModel);
        PackageInstallerCompat.getInstance(mContext).onStop();
        if (mNotificationBadgingObserver != null) {
            mNotificationBadgingObserver.unregister();
        }
    }

    LauncherModel setLauncher(Launcher launcher) {
        getLocalProvider(mContext).setLauncherProviderChangeListener(launcher);
        mModel.initialize(launcher);
//add by zhouerlong prize launcher 20180906
        initXUtils(launcher);
        DefaultConfig.findOverIcons(DefaultConfig.default_config+"overlay_icon_koobee.xml");
//add by zhouerlong prize launcher 20180906
        return mModel;
    }

    public IconCache getIconCache() {
        return mIconCache;
    }

    public LauncherModel getModel() {
        return mModel;
    }

    public WidgetPreviewLoader getWidgetCache() {
        return mWidgetCache;
    }

    public InvariantDeviceProfile getInvariantDeviceProfile() {
        return mInvariantDeviceProfile;
    }

    /**
     * Shorthand for {@link #getInvariantDeviceProfile()}
     */
    public static InvariantDeviceProfile getIDP(Context context) {
        return LauncherAppState.getInstance(context).getInvariantDeviceProfile();
    }
//prize add by zhouerlong 20190116
			//add by zhouerlong 20180123
    public  static  boolean isDisableAllApps(){
//add by zhouerlong 20190116
        if(Launcher.screen_style) {
            return mIsDisableAllApps;
        }
        return Utilities.getSystemProperty("ro.prize_launcher_singleanddouble","0").equals("1");
//add by zhouerlong 20190116
    }
			//add by zhouerlong 20180123

    public static void setmIsDisableAllApps(boolean mIsDisableAllApps) {
        LauncherAppState.mIsDisableAllApps = mIsDisableAllApps;
    }

    public static boolean mIsDisableAllApps = false;

//prize add by zhouerlong 20190116
    private static LauncherProvider getLocalProvider(Context context) {
//add by zhouerlong prize launcher 20180906
        if(Build.VERSION.SDK_INT> Build.VERSION_CODES.N) {
            try (ContentProviderClient cl = context.getContentResolver()
                    .acquireContentProviderClient(LauncherProvider.AUTHORITY)) {
                return (LauncherProvider) cl.getLocalContentProvider();
            }
        }
        return LauncherProvider.sLauncherProvider;
    }
//add by zhouerlong prize launcher 20180906

//add by zhouerlong prize launcher 20180906
    private final int DB_VERSION = 5;

    private DbManager.DaoConfig daoConfig = new DbManager.DaoConfig()
            .setDbName("leftPage.db")
            // .setDbDir(new File("/sdcard"))
            .setDbVersion(DB_VERSION)
            .setDbUpgradeListener(new DbManager.DbUpgradeListener() {
                @Override
                public void onUpgrade(DbManager db, int oldVersion,
                                      int newVersion) {
                    // TODO: upgrade
                    Log.v("LK", "oldVersion=="+oldVersion+"newVersion=="+newVersion);
                    if (newVersion>oldVersion) {
                        try {
                            db.dropDb();
                            db.getDaoConfig();
                        } catch (DbException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }

                    }
                }
            });

    private void initXUtils(Activity a) {
        if(a==null) {
            return;
        }
        x.Ext.init(a.getApplication());
        //x.Ext.setDebug(true);
        try {
            xDbManager = x.getDb(daoConfig);
        } catch (Exception e) {
            // TODO: handle exception
        }
        x.Ext.setDebug(AppConfig.ISDEBUG);

//prize add by zhouerlong badgeUnread begin
        if(Launcher.sBadge) {
            BadgeTool.getInstance().init();
        }
//prize add by zhouerlong badgeUnread begin
    }

    public static DbManager getDbManager() {
        return xDbManager;
    }
//prize add by liyuchong, adapte Condor theme park, 20190218-begin
    public void clearIcon(){
        mIconCache.clear();
    }
//prize add by liyuchong, adapte Condor theme park, 20190218-end
    private static DbManager xDbManager = null;
//add by zhouerlong prize launcher 20180906
}
