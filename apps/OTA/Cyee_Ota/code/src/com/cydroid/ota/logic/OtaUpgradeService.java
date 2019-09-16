package com.cydroid.ota.logic;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.text.TextUtils;
import com.cydroid.ota.Log;

/**
 * Created by liuyanfeng on 15-6-10.
 */
public class OtaUpgradeService extends Service {
    private static final String TAG = "OtaUpgradeService";
    public static final String GN_COM_ANDROID_ATION_OTA = "gn.com.android.ation.OTA";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "start OtaUpgradeService!");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null && GN_COM_ANDROID_ATION_OTA.equals(intent.getAction())) {
            autoUpgrade(intent);
        }
        return super.onStartCommand(intent, START_NOT_STICKY, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private void autoUpgrade(Intent intent) {
        Log.d(TAG, "action :" + intent.getAction());
        String filePath = intent.getStringExtra("filePath");

        Log.d(TAG, "filePath :" + filePath);
        if (TextUtils.isEmpty(filePath)) {
            return;
        }
        if (!isAsci(filePath)) {
            return;
        }

        ILocalUpdateExecutor localUpdateExecutor = SystemUpdateFactory.
                localUpdate(OtaUpgradeService.this, null);
        localUpdateExecutor.install(filePath);
    }

    private static boolean isAsci(String s) {
        if (s == null || s.length() == 0) {
            return false;
        }

        char[] chars = s.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] > 126 || chars[i] < 32) {
                return false;
            }
        }

        return true;
    }
}
