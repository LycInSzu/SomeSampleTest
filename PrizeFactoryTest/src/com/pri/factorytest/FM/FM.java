package com.pri.factorytest.FM;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;

import com.pri.factorytest.PrizeBaseActivity;
import com.pri.factorytest.R;
import com.pri.factorytest.util.Utils;

public class FM extends PrizeBaseActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fm);
        startFM();
        confirmButton();
        Utils.paddingLayout(findViewById(R.id.passButton), 0, ACTIVITY_TOP_PADDING, 0, 0);
    }

    void startFM() {
        Intent intent = new Intent();
        intent.setClassName("com.android.fmradio",
                "com.android.fmradio.FmMainActivity");
        intent.putExtra("FACTORYFM", "factoryfm");
        startActivity(intent);
        return;
    }
}
