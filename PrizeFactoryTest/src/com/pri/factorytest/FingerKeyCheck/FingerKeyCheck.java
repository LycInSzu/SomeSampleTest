package com.pri.factorytest.FingerKeyCheck;

import android.os.Bundle;
import android.os.SystemProperties;
import android.widget.TextView;

import com.pri.factorytest.PrizeBaseActivity;
import com.pri.factorytest.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Optional;

public class FingerKeyCheck extends PrizeBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fingerkey);
        TextView fingerKeyTV = (TextView) findViewById(R.id.fingerkey_check);
        confirmButtonNonEnable();

        boolean keyFlay = getSoterThhInfo_st_ifaa();
        fingerKeyTV.setText(String.format(getString(R.string.key_write_disc),
                keyFlay ? getString(R.string.pass) : getString(R.string.fail)));

        mButtonPass.setEnabled(keyFlay);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void finish() {
        super.finish();
    }

    private boolean getSoterThhInfo_st_ifaa() {
        String info = SystemProperties.get("vendor.soter.teei.thh.info");
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(info);
        }catch (Exception e) {
            e.printStackTrace();
        }
        return Optional.ofNullable(jsonObject).map(x -> {
            try {
                return x.getBoolean("st_ifaa");
            } catch (JSONException e) {
                e.printStackTrace();
                return false;
            }
        }).orElse(false);
    }
}