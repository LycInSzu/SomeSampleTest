package com.pri.factorytest.Version;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemProperties;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;

import com.pri.factorytest.PrizeBaseActivity;
import com.pri.factorytest.R;
import com.pri.factorytest.util.Utils;

import java.io.IOException;
import java.util.stream.Stream;

import static com.pri.factorytest.FactoryTestApplication.NETWORK_TYPE;

public class Version extends PrizeBaseActivity {
    private static final String TAG = "Version";

    private TextView mVersion;
    private TelephonyManager mTelMgr;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();
        boolean bSoftinfo = intent.getBooleanExtra("softinfo", false);
        if (!bSoftinfo) {
            setContentView(R.layout.version_info);
            mVersion = (TextView) findViewById(R.id.version_show);
            confirmButton();
        } else {
            setContentView(R.layout.version);
            mVersion = (TextView) findViewById(R.id.version_show);
        }
        Utils.paddingLayout(findViewById(R.id.version_show), 0, ACTIVITY_TOP_PADDING, 0, 0);
        String message = getVersionInfo();
        mVersion.setText(message);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Utils.clearNvBuffCache();
    }

    private boolean getCalibrationInfo(String temp) {
        return koobeeCalibrationInfo(temp);
    }

    private boolean odmCalibrationInfo(String temp) {
        if (!NETWORK_TYPE.contains("GB") && !NETWORK_TYPE.contains("WB")
                && !NETWORK_TYPE.contains("LtB") && !NETWORK_TYPE.contains("LfB")) {
            return false;
        }
        if (null != temp && temp.length() >= 63) {
            if (!temp.substring(62, 63).equals("P"))
                return false;
        } else
            return false;

        if (null != temp && temp.length() >= 61) {
            if (!temp.substring(60, 61).equals("P"))
                return false;
        } else
            return false;

        if (NETWORK_TYPE.contains("LtB")) {
            if (null != temp && temp.length() >= 55) {
                if (!temp.substring(54, 55).equals("P"))
                    return false;
            } else
                return false;
        }
        if (NETWORK_TYPE.contains("LfB")) {
            if (null != temp && temp.length() >= 53) {
                if (!temp.substring(52, 53).equals("P"))
                    return false;
            } else
                return false;
        }
        return true;
    }

    private boolean koobeeCalibrationInfo(String temp) {
        boolean hasElement = Stream.of("GB", "WB", "TB", "CB", "LtB", "LfB").anyMatch(x -> NETWORK_TYPE.contains(x));
        if (!hasElement) {
            return false;
        }
        boolean gb = true;
        boolean wb = true;
        boolean tb = true;
        boolean cb = true;
        boolean ltb = true;
        boolean lfb = true;
        if (NETWORK_TYPE.contains("GB")) {
            gb = Utils.getAntPass(temp, 62);
        }

        if (NETWORK_TYPE.contains("WB")) {
            wb = Utils.getAntPass(temp, 60);
        }

        if (NETWORK_TYPE.contains("TB")) {
            tb = Utils.getAntPass(temp, 58);
        }

        if (NETWORK_TYPE.contains("CB")) {
            cb = Utils.getAntPass(temp, 56);
        }

        if (NETWORK_TYPE.contains("LtB")) {
            ltb = Utils.getAntPass(temp, 54);
        }

        if (NETWORK_TYPE.contains("LfB")) {
            lfb = Utils.getAntPass(temp, 52);
        }

        return gb && wb && cb && ltb && lfb;
    }

    private String getVersionInfo() {
        String temp = null;
        StringBuilder info = new StringBuilder();
        mTelMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        info.append("[SN] : ");
        temp = Utils.readProInfo(0, 63);
        info.append(temp);

        info.append("\n" + getString(R.string.calibration));
        boolean showCalibration01 = "1".equals(SystemProperties.get("ro.pri_factory_calibration01"));
        if (getCalibrationInfo(temp)) {
            info.append(showCalibration01 ? "1" : getString(R.string.calibration_yes));
        } else {
            info.append(showCalibration01 ? "0" : getString(R.string.calibration_no));
        }

        if (NETWORK_TYPE.contains("CB")) {
            String meid = SystemProperties.get("gsm.mtk.meid");
            info.append("\n[MEID] : ");
            temp = null;
            if (TextUtils.isEmpty(meid)) {
                //meid = TelephonyManagerEx.getDefault().getMeid(0);
                temp = meid;
            } else {
                temp = meid.toUpperCase();
            }
            info.append(temp);
        }
        String imei1 = SystemProperties.get("gsm.mtk.imei1");
        String imei2 = SystemProperties.get("gsm.mtk.imei2");

        info.append("\n[IMEI1] : ");
        temp = null;
        temp = imei1;//mTelMgr.getDeviceId(0);
        info.append(temp);

        info.append("\n[IMEI2] : ");
        temp = null;
        temp = imei2;//mTelMgr.getDeviceId(1);
        info.append(temp);

        info.append("\n[Build Type] : ");
        temp = null;
        temp = Build.TYPE;
        info.append(temp);

        info.append("\n[Build Brand] : ");
        temp = null;
        temp = Build.BRAND;
        info.append(temp);

        info.append("\n[Build Model] : ");
        temp = null;
        temp = Build.MODEL;
        info.append(temp);

        info.append("\n[Android Version] : ");
        temp = null;
        temp = Build.VERSION.RELEASE;
        info.append(temp);

        info.append("\n[MTK Version] : \n");
        temp = null;
        temp = SystemProperties.get("ro.mediatek.version.release");
        info.append(temp);

        info.append("\n[Build Date] : ");
        temp = null;
        temp = SystemProperties.get("ro.build.date");
        info.append(temp);


        info.append("\n[Baseband Version] : \n");
        temp = null;
        temp = SystemProperties.get("gsm.version.baseband");
        info.append(temp);

		info.append("\n[Build number] : \n");
        temp = null;
        temp = Build.DISPLAY;
        info.append(temp);

        /*prize-[Dialer]-chensenquan 20170329 add Build number start*/
		info.append("\n[Version] : \n");
        temp = null;
		temp = SystemProperties.get("ro.pri_custom_build_version");
        if (temp != null) {
            if (!"".equals(temp)) {
                info.append(temp);
            } else {
                info.append(Build.DISPLAY);
            }
        } else {
            info.append(Build.DISPLAY);
        }
        /*prize-[Dialer]-chensenquan 20170329 add Build number end*/

        /*prize-[Dialer]-YZHD 20171223 add fingerprint display start*/
        info.append("\n[cts-fingerprint] : \n");
        temp = null;
        temp = SystemProperties.get("ro.build.fingerprint");
        if (null == temp || "".equals(temp)) {
            try {
                temp = Utils.readContextFromStream(this.getAssets().open("prize_fingerprint.txt"));
            } catch (IOException e) {
                Log.e(TAG, "ERROR:open the fingerprint file Failed!!");
                e.printStackTrace();
            }
        }
        info.append(temp);
        /*prize-[Dialer]-YZHD 20171223 add fingerprint display end*/

        info.append("\n[gms version] : ");
        temp = null;
        temp = SystemProperties.get("ro.com.google.gmsversion");
        info.append(temp);

        return info.toString();
    }
}
