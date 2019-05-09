package com.pri.factorytest;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.widget.TextView;

import com.pri.factorytest.util.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Optional;
import java.util.stream.Stream;

import static com.pri.factorytest.FactoryTestApplication.PRIZE_CUSTOMER;

public class PrizeHwInfo extends PrizeBaseActivity {
    private static final String[] ARGS_HW = {"cat", "sys/class/hw_info/hw_info_data/hw_info_read"};
    private TextView localTextView;

    private final static int FINISH_BIT_SET = 0x0000;
    private final static int UPLOAD_BIT_SET = 0x0012;
    private final static int GEN_KEY_BIT_SET = 0x0013;
    private final static int NOT_BIT_SET = 0x0014;
    private final static int BAD_STATE = 0x00115;
    /**
     * prize tee flag,1:PinBo,2:DouJia
     */
    public final static int PRIZE_TEE_PRODUCT = Optional.ofNullable(SystemProperties
            .get("ro.pri_tee")).map(x -> {
        try {
            return Integer.parseInt(x.trim());
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return -1;
        }
    }).orElse(-1);

    static {
        //prize-add tee properties-lanwm-20171114
        if (PRIZE_TEE_PRODUCT == 1) {
            System.loadLibrary("teeinfo");
        }
    }

    public static String getResultHwinfo() {
        ShellExecute localShellExecute = new ShellExecute();
        String result = null;
        try {
            result = localShellExecute.execute(ARGS_HW, "/");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    protected void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        setContentView(R.layout.hw_info);
        localTextView = (TextView) findViewById(R.id.showresult);
        Utils.paddingLayout(findViewById(R.id.hw_info_scroll), 0, ACTIVITY_TOP_PADDING, 0, 0);
    }

    /*prize-*#*#888#*#*and*#*#8812#*#*HwInfo Change with system language-yaoshu-20190124-start*/
    @Override
    protected void attachBaseContext(Context context) {
        if (Stream.of("BLU").noneMatch(x -> PRIZE_CUSTOMER.equals(x))) {
            super.attachBaseContext(context);
        } else {
            super.superAttachBaseContext(context);
        }
    }
    /*prize-*#*#888#*#*and*#*#8812#*#*HwInfo Change with system language-yaoshu-20190124-end*/

    @Override
    protected void onResume() {
        super.onResume();
        String str1 = getResultHwinfo();
        //android.util.Log.e("PrizeHwInfo", "getResultHwinfo  str1 = " + str1);
        String teeInfo = getTeeInfo(this);
        localTextView.setText(TextUtils.isEmpty(teeInfo) ? str1 : str1 + "\n" + teeInfo);
    }

    public static String getTeeInfo(Context context) {
        String teeInfo = null;
        switch (PRIZE_TEE_PRODUCT) {
            case 1:
                teeInfo = getPinBoState(context);
                break;
            case 2:
                teeInfo = getDouJiaState(context);
                break;
            default:
                break;
        }
        return teeInfo;
    }

    private static String getDouJiaState(Context context) {
        final boolean isSDKPreP = Build.VERSION.SDK_INT < Build.VERSION_CODES.P;
        StringBuilder strBld = new StringBuilder();
        String teeState = isSDKPreP ? SystemProperties.get("soter.encrypt.state")
                : SystemProperties.get("vendor.soter.teei.init");
        String teeInit = isSDKPreP ? SystemProperties.get("soter.teei.thh.init")
                : SystemProperties.get("vendor.soter.teei.thh.init");
        String googleKeyState = isSDKPreP ? SystemProperties.get("soter.teei.googlekey.status")
                : SystemProperties.get("vendor.soter.teei.googlekey.status");
        String faceIDState = isSDKPreP ? SystemProperties.get("soter.teei.active.faceid")
                : SystemProperties.get("vendor.soter.teei.active.faceid");
        android.util.Log.e("PrizeHwInfo", "DunTaiState teeState = " + teeState);
        android.util.Log.e("PrizeHwInfo", "DunTaiState teeInit = " + teeInit);
        android.util.Log.e("PrizeHwInfo", "DunTaiState googleKeyState = " + googleKeyState);
        android.util.Log.e("PrizeHwInfo", "DunTaiState faceIDState = " + faceIDState);
        Optional.ofNullable(teeState).ifPresent(x -> strBld.append(context.getString(R.string.doujia_finger_key_title2) + x + "\n"));
        Optional.ofNullable(teeInit).ifPresent(x -> strBld.append(context.getString(R.string.doujia_finger_key_title1) + x + "\n"));
        Optional.ofNullable(googleKeyState).ifPresent(x -> strBld.append(context.getString(R.string.google_key_title) + x + "\n"));
        Optional.ofNullable(faceIDState).ifPresent(x -> strBld.append(context.getString(R.string.face_id_key_title) + x));
        return strBld.toString();
    }

    private static String getPinBoState(Context context) {
        StringBuilder strBld = new StringBuilder();
        final String fingerKeyOk = context.getString(R.string.finger_key_ok);
        final String fingerKeyFail = context.getString(R.string.finger_key_faile);
        final boolean statusOK = verifyStatus() == 0;
        final boolean teeKeyOK = verifyTeeKey() == 0;
        final boolean keyboxOK = verifyKeyboxState() == 0;
        android.util.Log.e("PrizeHwInfo", "status---ok--" + statusOK);
        android.util.Log.e("PrizeHwInfo", "teeKey---ok--" + teeKeyOK);
        android.util.Log.e("PrizeHwInfo", "keybox---ok--" + keyboxOK);
        strBld.append(context.getString(R.string.finger_key_title) + "\n");

        strBld.append(context.getString(R.string.finger_key_title1));
        strBld.append((statusOK ? fingerKeyOk : fingerKeyFail) + "\n");

        strBld.append(context.getString(R.string.finger_key_title2));
        strBld.append((teeKeyOK ? fingerKeyOk : fingerKeyFail) + "\n");

        strBld.append((context.getString(R.string.google_key_title)));
        strBld.append(keyboxOK ? fingerKeyOk : fingerKeyFail);

        return strBld.toString();
    }

    static class ShellExecute {
        private ShellExecute() {
        }

        public String execute(String[] paramArrayOfString, String paramString)
                throws IOException {
            StringBuilder strBui = new StringBuilder();
            BufferedReader bufferedReader;
            try {
                ProcessBuilder localProcessBuilder = new ProcessBuilder(paramArrayOfString);
                if (paramString != null) {
                    File localFile = new File(paramString);
                    localProcessBuilder.directory(localFile);
                }
                localProcessBuilder.redirectErrorStream(true);
                InputStream localInputStream = localProcessBuilder.start().getInputStream();
                bufferedReader = new BufferedReader(new InputStreamReader(localInputStream));
                String lineStr;
                while ((lineStr = bufferedReader.readLine()) != null) {
                    strBui.append(lineStr + "\n");
                }
                localInputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return strBui.toString();
        }
    }

    public static native int verifyTeeKey();

    public static native int verifyStatus();

    public static native int verifyKeyboxState();
    // public native int verifyTruststore();
}
