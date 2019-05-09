package com.pri.factorytest.CameraBack;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;

import com.pri.factorytest.PrizeBaseActivity;
import com.pri.factorytest.R;
import com.pri.factorytest.util.Utils;

import java.util.List;
import java.util.Optional;

public class DoubleCameraOperation extends PrizeBaseActivity {

    private static final String ACTION_DOUBLE_CAMERA_STANDAR = "com.kb.action.FACTORY_CAMERA";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.double_camer_standar);
        confirmButtonNonEnable();
        Utils.paddingLayout(findViewById(R.id.passButton), 0, ACTIVITY_TOP_PADDING, 0, 0);

        mButtonFail.setOnClickListener(view -> {
            if (Utils.isNoNFastClick()) {
                if (Utils.toStartAutoTest == true) {
                    Utils.mItemPosition++;
                }
                Utils.writeProInfo("F", Utils.PRIZE_DOUBLE_CAMERA_STANDAR);
                setResult(RESULT_CANCELED);
                finish();
            }
        });

        if (!isActionSupport()) {
            Utils.writeProInfo("F", Utils.PRIZE_DOUBLE_CAMERA_STANDAR);
            setResult(RESULT_CANCELED);
            finish();
            return;
        }
        Intent intent = new Intent(ACTION_DOUBLE_CAMERA_STANDAR);
        //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivityForResult(intent, 0);
    }

    private boolean isActionSupport() {
        PackageManager pm = this.getPackageManager();
        Intent intent = new Intent(ACTION_DOUBLE_CAMERA_STANDAR);
        List<ResolveInfo> list = pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 1) {
            boolean result = Optional.ofNullable(data).map(x -> x.getBooleanExtra("result", false)).orElse(false);
            Utils.writeProInfo(result ? "P" : "F", Utils.PRIZE_DOUBLE_CAMERA_STANDAR);
            mButtonPass.setEnabled(result);
        }
    }
}
