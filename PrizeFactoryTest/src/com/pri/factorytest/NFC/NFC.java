package com.pri.factorytest.NFC;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.pri.factorytest.PrizeBaseActivity;
import com.pri.factorytest.R;
import com.pri.factorytest.util.Utils;

public class NFC extends PrizeBaseActivity {
    private static final String TAG = "NFC";
    private PowerManager powerManager = null;
    private WakeLock wakeLock = null;

    private boolean mNfcIsEnable;
    private boolean mNfcSupport;
    private NfcAdapter mNfcAdapter;
    private PendingIntent mPendingIntent;
    private IntentFilter[] mIntentFilters;
    private String[][] mTechLists;

    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nfc);
        powerManager = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
        wakeLock = this.powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "My Lock");
        confirmButtonNonEnable();
        Utils.paddingLayout(findViewById(R.id.passButton), 0, ACTIVITY_TOP_PADDING, 0, 0);

        mPendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, this.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        mIntentFilters = new IntentFilter[]{
                new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED),
                new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED),
                new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED),
        };
        mTechLists = new String[][]{
                new String[]{android.nfc.tech.NfcA.class.getName()},
                new String[]{android.nfc.tech.NfcB.class.getName()},
                new String[]{android.nfc.tech.NfcF.class.getName()},
                new String[]{android.nfc.tech.NfcV.class.getName()},
                new String[]{android.nfc.tech.Ndef.class.getName()},
                new String[]{android.nfc.tech.MifareClassic.class.getName()},
                new String[]{android.nfc.tech.NfcBarcode.class.getName()},
                new String[]{android.nfc.tech.MifareUltralight.class.getName()},
        };

        Log.i("pss", "onCreate");

    }


    @Override
    protected void onPause() {
        Log.i("pss", "onPause");
        wakeLock.release();
        if (mNfcAdapter != null) {
            mNfcAdapter.disableForegroundDispatch(this);
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        Log.i("pss", "onResume");
        initNfc();
        wakeLock.acquire();
        super.onResume();
    }

    @Override
    public void onNewIntent(Intent intent) {
        judgeNfcTag(intent);
    }

    private void judgeNfcTag(Intent intent) {
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag != null) {
            mButtonPass.setEnabled(true);
        }
    }

    private void initNfc() {
        Log.i("pss", "initNfc");
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        mNfcSupport = mNfcAdapter != null;
        if (mNfcSupport) {
            mNfcIsEnable = mNfcAdapter.isEnabled();
            if (mNfcIsEnable) {
                mNfcAdapter.enableForegroundDispatch(this, mPendingIntent, mIntentFilters, mTechLists);
            } else {
                Toast.makeText(getApplicationContext(), "CAN'T OPEN NFC", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getApplicationContext(), "CAN'T SUPPORT NFC", Toast.LENGTH_SHORT).show();
        }
    }
}
