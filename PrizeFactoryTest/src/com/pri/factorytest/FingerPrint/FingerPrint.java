package com.pri.factorytest.FingerPrint;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemProperties;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;

import com.pri.factorytest.PrizeBaseActivity;
import com.pri.factorytest.R;
import com.pri.factorytest.util.Utils;

import java.util.Optional;
import java.util.stream.Stream;

public class FingerPrint extends PrizeBaseActivity {

    private final static String PRIZE_FINGERPRINT_CUSOTMER = Optional.ofNullable(SystemProperties
            .get("ro.pri.fingerprint")).map(x -> x.trim()).orElse("");

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fingerprint);
        startFingerPrint();
        confirmButtonNonEnable();
        Utils.paddingLayout(findViewById(R.id.passButton), 0, ACTIVITY_TOP_PADDING, 0, 0);
    }

    void startFingerPrint() {
        try {
            if (Stream.of("gw9518").anyMatch(x -> PRIZE_FINGERPRINT_CUSOTMER.contains(x.trim()))) {
                if (Utils.toStartAutoTest) {
                    startActivityForResult(new Intent(FingerPrint.this, EnrollActivity.class), 0);
                } else {
                    startActivityForResult(new Intent(FingerPrint.this, HuidingFingerActivity.class), 0);
                }
            } else {
                startActivity(new Intent(FingerPrint.this, FingerPrintActivity.class));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 20) {
            Utils.writeProInfo("P", Utils.PRIZE_HUIDING_FINGERPRINT_CALI);
            startActivityForResult(new Intent(FingerPrint.this, EnrollActivity.class), 0);
        }
        if (resultCode == 1) {
            mButtonPass.setEnabled(true);
        }
    }

    @Override
    protected void onResume() {
        if (SystemProperties.get("persist.sys.prize_fp_enable").equals("1")) {
            mButtonPass.setEnabled(true);
            SystemProperties.set("persist.sys.prize_fp_enable", "0");
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void finish() {
        super.finish();
    }

}
