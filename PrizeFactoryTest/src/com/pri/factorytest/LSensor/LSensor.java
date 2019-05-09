package com.pri.factorytest.LSensor;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.pri.factorytest.PrizeBaseActivity;
import com.pri.factorytest.R;
import com.pri.factorytest.util.Utils;

public class LSensor extends PrizeBaseActivity {

    private SensorManager mSensorManager = null;
    private Sensor mLSensor = null;
    private LSensorListener mLSensorListener;
    private TextView mTextView;
    private final static int SENSOR_TYPE = Sensor.TYPE_LIGHT;
    private final static int SENSOR_DELAY = SensorManager.SENSOR_DELAY_FASTEST;
    private float lastValues = 0.f;
    private int ccount = 0;

    @Override
    public void finish() {
        try {
            mSensorManager.unregisterListener(mLSensorListener, mLSensor);
        } catch (Exception e) {
        }
        super.finish();
    }

    void getService() {

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (mSensorManager == null) {
            fail(getString(R.string.service_get_fail));
        }

        mLSensor = mSensorManager.getDefaultSensor(SENSOR_TYPE);
        if (mLSensor == null) {
            fail(getString(R.string.sensor_get_fail));
        }

        mLSensorListener = new LSensorListener(this);
        if (!mSensorManager.registerListener(mLSensorListener, mLSensor,
                SENSOR_DELAY)) {
            mSensorManager.registerListener(mLSensorListener, mLSensor,
                    SENSOR_DELAY);
            fail(getString(R.string.sensor_register_fail));
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.lsensor);
        mTextView = (TextView) findViewById(R.id.lsensor_result);
        getService();
        mTextView.setText(getString(R.string.lsensor_result) + "\n" + "10");
        confirmButtonNonEnable();
        Utils.paddingLayout(findViewById(R.id.passButton), 0, ACTIVITY_TOP_PADDING, 0, 0);
        ccount = 0;
    }

    void fail(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mSensorManager == null || mLSensorListener == null
                || mLSensor == null)
            return;
        mSensorManager.unregisterListener(mLSensorListener, mLSensor);
    }

    public class LSensorListener implements SensorEventListener {
        public LSensorListener(Context context) {
            super();
        }

        public void onSensorChanged(SensorEvent event) {

            synchronized (this) {
                if (event.sensor.getType() == SENSOR_TYPE) {

                    if (event.values[0] != lastValues) {
                        ccount++;
                        lastValues = event.values[0];
                        mTextView.setText(getString(R.string.lsensor_result)
                                + "\n" + event.values[0]);
                    }
                    android.util.Log.e("peisaisai", "event.values[0] = " + event.values[0]);
                    if (ccount >= 3 && event.values[0] <= 200 && event.values[0] >= 11) {
                        mButtonPass.setEnabled(true);
                    }
                }
            }
        }

        public void onAccuracyChanged(Sensor arg0, int arg1) {

        }
    }
}
