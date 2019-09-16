package com.cydroid.ota.utils;

import com.cydroid.ota.R;

public class Constants {

    public static final String DATEFORMATE = "yyyy-MM-dd";
    public static final String DATE_FORMAT_BY_DAY = "yyyy.MM.dd";
    public static final String APP_DOWNLOAD_PATH = "cyee/";

    public static final int GN_SU_ERROR_PUSH_ID = -1;
    public static final long GN_SU_ERROR_DOWNLOAD_ID = -1;

    public static final String UTF_8_CHARSET = "utf-8";
    public static final String NULL = "null";
    public static final String AUTO_SCANNER = "auto_scanner";
    public static final long ONE_DAY_MILLISECOND = 24 * 60 * 60 * 1000;
    public static final int LOWER_CHARGE = 40;
    public static final int MINI_CHARGE = 20;

    public static final int RESTART_DIALOG_INTERVAL = 1000;
	//Chenyee <CY_Bug> <xuyongji> <20171120> modify for SW17W16A-859 begin
    public static final int RESTART_DIALOG_COUNTDODWN_TIME = 5000;
	//Chenyee <CY_Bug> <xuyongji> <20171120> modify for SW17W16A-859 end

    public static final int PUSH_NOTIFICATION_ID = 10002;

    public static final String  SYSTEM_UPDATE_LAUNCHER_UNREAD_KEY = "gn.com.android.update_gn.com.android.update.ui.AnimOtaActivity.miss_infos";
    public static final String PUSH_NOTIFICATION_ACTION = "com.gionee.intent.action.PUSH_NOTIFICATION";
    public static final int GN_SU_DOWNLOAD_NOTIFICATION_ID = R.id.gn_su_id_download_notification;
    public final static int GN_SU_AUTO_UPGRADE_NOTIFICATION_ID = R.id.gn_su_id_auto_upgrade_notification;
}
