package com.pri.factorytest.SIM;

import android.os.Bundle;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pri.factorytest.PrizeBaseActivity;
import com.pri.factorytest.R;
import com.pri.factorytest.util.Utils;

//import com.mediatek.telephony.TelephonyManagerEx;

public class SIM extends PrizeBaseActivity {
    String TAG1 = "SIM1";
    String TAG2 = "SIM2";
    String simString = "";
    String sim1String = "";
    String sim2String = "";
    TextView mSim;
    private SubscriptionManager mSubscriptionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSubscriptionManager = SubscriptionManager.from(this);
        final SubscriptionInfo IMSI1 = mSubscriptionManager.getActiveSubscriptionInfoForSimSlotIndex(0);
        final SubscriptionInfo IMSI2 = mSubscriptionManager.getActiveSubscriptionInfoForSimSlotIndex(1);
        if (IMSI1 != null && !IMSI1.equals("")) {
            sim1String = TAG1 + ":pass";
        } else {
            sim1String = TAG1 + ":fail";
        }
        if (IMSI2 != null && !IMSI2.equals("")) {
            sim2String = TAG2 + ":pass";
        } else {
            sim2String = TAG2 + ":fail";
        }
        simString = sim1String + "\n" + sim2String;
        LinearLayout VersionLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.sim, null);
        setContentView(VersionLayout);
        mSim = (TextView) findViewById(R.id.sim_show);
        mSim.setText(simString);
        confirmButton();
        if (sim1String.contains("fail") || sim2String.contains("fail")) {
            mButtonPass.setEnabled(false);
        } else {
            mButtonPass.setEnabled(true);
        }
        Utils.paddingLayout(findViewById(R.id.passButton), 0, ACTIVITY_TOP_PADDING, 0, 0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        doPass2NextTest();
    }
}
