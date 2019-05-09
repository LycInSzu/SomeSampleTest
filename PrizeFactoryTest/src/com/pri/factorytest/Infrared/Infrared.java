package com.pri.factorytest.Infrared;

import android.content.Context;
import android.hardware.ConsumerIrManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.pri.factorytest.PrizeBaseActivity;
import com.pri.factorytest.R;
import com.pri.factorytest.util.Utils;

public class Infrared extends PrizeBaseActivity {
    private TextView mFreqsText;
    private ConsumerIrManager mCIR;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        mCIR = (ConsumerIrManager) getSystemService(Context.CONSUMER_IR_SERVICE);
        setContentView(R.layout.infrared);

        findViewById(R.id.send_button).setOnClickListener(mSendClickListener);
        findViewById(R.id.get_freqs_button).setOnClickListener(mGetFreqsClickListener);
        mFreqsText = (TextView) findViewById(R.id.freqs_text);
        Utils.paddingLayout(findViewById(R.id.passButton), 0, ACTIVITY_TOP_PADDING, 0, 0);

        confirmButtonNonEnable();
    }

    View.OnClickListener mSendClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            if (!mCIR.hasIrEmitter()) {
                return;
            }
            int[] pattern = {1901, 4453, 625, 1614, 625, 1588, 625, 1614, 625, 442, 625, 442, 625,
                    468, 625, 442, 625, 494, 572, 1614, 625, 1588, 625, 1614, 625, 494, 572, 442, 651,
                    442, 625, 442, 625, 442, 625, 1614, 625, 1588, 651, 1588, 625, 442, 625, 494, 598,
                    442, 625, 442, 625, 520, 572, 442, 625, 442, 625, 442, 651, 1588, 625, 1614, 625,
                    1588, 625, 1614, 625, 1588, 625, 48958};
            mCIR.transmit(38400, pattern);
            mButtonPass.setEnabled(true);
        }
    };

    View.OnClickListener mGetFreqsClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            StringBuilder b = new StringBuilder();

            if (!mCIR.hasIrEmitter()) {
                mFreqsText.setText("No IR Emitter found!");
                return;
            }
            ConsumerIrManager.CarrierFrequencyRange[] freqs = mCIR.getCarrierFrequencies();
            b.append("IR Carrier Frequencies:\n");
            for (ConsumerIrManager.CarrierFrequencyRange range : freqs) {
                b.append(String.format("    %d - %d\n", range.getMinFrequency(),
                        range.getMaxFrequency()));
            }
            mFreqsText.setText(b.toString());
        }
    };
}
