package com.pri.factorytest.Key;

import android.os.Bundle;
import android.os.SystemProperties;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.pri.factorytest.PrizeBaseActivity;
import com.pri.factorytest.R;
import com.pri.factorytest.util.Utils;

public class Key extends PrizeBaseActivity {

    private boolean bKeyHome = false;
    private boolean bKeyMenu = false;
    private boolean bKeyBack = false;
    private boolean bKeyVoUp = false;
    private boolean bKeyVoDn = false;
    private boolean bKeyPower = false;
    private boolean bKeyMute = false;
    private boolean bKeyPttRec = false;
    private boolean bKeyCustom = false;
    private boolean bKeySos = false;
    private boolean bKeyCamera = false;

    private static final int KEYCODE_PTT_RECORD = 301;
    private static final int KEYCODE_CUSTOM = 302;
    private static final int KEYCODE_SOS = 303;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.key);
        if ("1".equals(SystemProperties.get("ro.support_hiding_navbar"))) {
            TextView mKeyMenu = (TextView) findViewById(R.id.menu);
            if (!"1".equals(SystemProperties.get("ro.pri_menu_function_key"))) {
                mKeyMenu.setVisibility(View.GONE);
                bKeyMenu = true;
            }
        }
        if ("0".equals(android.os.SystemProperties.get("qemu.hw.mainkeys"))) {
            TextView menuTv = (TextView) findViewById(R.id.menu);
            TextView homeTv = (TextView) findViewById(R.id.home);
            TextView backTv = (TextView) findViewById(R.id.back);

            if (!"1".equals(SystemProperties.get("ro.pri_home_function_key"))) {
                homeTv.setVisibility(View.GONE);
                bKeyHome = true;
            }

            if (!"1".equals(SystemProperties.get("ro.pri_back_function_key"))) {
                backTv.setVisibility(View.GONE);
                bKeyBack = true;
            }

            if (!"1".equals(SystemProperties.get("ro.pri_menu_function_key"))) {
                menuTv.setVisibility(View.GONE);
                bKeyMenu = true;
            }
        }
        /*prize-[KEY TEST]-add function key test item YZHD 20171228 start */
        if (!"1".equals(SystemProperties.get("ro.pri_mute_function_key"))) {
            TextView mKeyMute = (TextView) findViewById(R.id.mute);
            mKeyMute.setVisibility(View.GONE);
            bKeyMute = true;
        }
        if (!"1".equals(SystemProperties.get("ro.pri_ptt_rec_function_key"))) {
            TextView mKeyPttRec = (TextView) findViewById(R.id.ptt_record);
            mKeyPttRec.setVisibility(View.GONE);
            bKeyPttRec = true;
        }
        if (!"1".equals(SystemProperties.get("ro.pri_sos_function_key"))) {
            TextView mKeySos = (TextView) findViewById(R.id.sos);
            mKeySos.setVisibility(View.GONE);
            bKeySos = true;
        }
        if (!"1".equals(SystemProperties.get("ro.pri_custom_function_key"))) {
            TextView mKeyCustom = (TextView) findViewById(R.id.custom);
            mKeyCustom.setVisibility(View.GONE);
            bKeyCustom = true;
        }
        if (!"1".equals(SystemProperties.get("ro.pri_camera_function_key"))) {
            TextView mKeyCamera = (TextView) findViewById(R.id.camera);
            mKeyCamera.setVisibility(View.GONE);
            bKeyCamera = true;
        }
        if (!"1".equals(SystemProperties.get("ro.pri_power_function_key"))) {
            TextView mKeyPower = (TextView) findViewById(R.id.power_key);
            mKeyPower.setVisibility(View.GONE);
            bKeyPower = true;
        }
        /*prize-[KEY TEST]-add function key test item YZHD 20171228 end*/
        confirmButtonNonEnable();
        Utils.paddingLayout(findViewById(R.id.passButton), 0, ACTIVITY_TOP_PADDING, 0, 0);

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        android.util.Log.i("Key", "-----keycode----" + keyCode + "--is press down");
        return true;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        android.util.Log.i("Key", "-----keycode----" + keyCode + "--is up");
        TextView keyText = null;

        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                keyText = (TextView) findViewById(R.id.volume_up);
                bKeyVoUp = true;
                break;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                keyText = (TextView) findViewById(R.id.volume_down);
                bKeyVoDn = true;
                break;
            case KeyEvent.KEYCODE_MENU:
                keyText = (TextView) findViewById(R.id.menu);
                bKeyMenu = true;
                break;
            case KeyEvent.KEYCODE_HOME:
                keyText = (TextView) findViewById(R.id.home);
                bKeyHome = true;
                break;
            case KeyEvent.KEYCODE_BACK:
                keyText = (TextView) findViewById(R.id.back);
                bKeyBack = true;
                break;
            case KeyEvent.KEYCODE_MUTE:
                keyText = (TextView) findViewById(R.id.mute);
                bKeyMute = true;
                break;
        /*prize-[KEY TEST]-add function key test item YZHD 20171228 start */
            case KEYCODE_PTT_RECORD:
                keyText = (TextView) findViewById(R.id.ptt_record);
                bKeyPttRec = true;
                break;
            case KEYCODE_SOS:
                keyText = (TextView) findViewById(R.id.sos);
                bKeySos = true;
                break;
            case KEYCODE_CUSTOM:
                keyText = (TextView) findViewById(R.id.custom);
                bKeyCustom = true;
                break;
            case KeyEvent.KEYCODE_CAMERA:
                keyText = (TextView) findViewById(R.id.camera);
                bKeyCamera = true;
                break;
        /*prize-[KEY TEST]-add function key test item YZHD 20171228 end*/
            case KeyEvent.KEYCODE_POWER:
                keyText = (TextView) findViewById(R.id.power_key);
                bKeyPower = true;
                break;
        }
        if (null != keyText) {
            keyText.setBackgroundResource(R.color.green);
            mButtonPass.setEnabled(bKeyVoUp && bKeyVoDn &&
                    bKeyBack && bKeyHome && bKeyMenu &&
                    bKeyMute && bKeyCustom && bKeySos &&
                    bKeyPttRec && bKeyCamera && bKeyPower);
        }
        doPass2NextTest();
        return true;
    }
}
