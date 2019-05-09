package com.pri.factorytest.PressureSensor;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.pri.factorytest.PrizeBaseActivity;
import com.pri.factorytest.R;
import com.pri.factorytest.util.Utils;

public class PressureSensor extends PrizeBaseActivity {

    private SensorManager mSensorManager = null;
    private Sensor mPressureSensor = null;
    private PressureSensorListener mPressureSensorListener;
    private TextView mTextView;
    private final static int SENSOR_TYPE = Sensor.TYPE_PRESSURE;//TYPE_LIGHT;
    private final static int SENSOR_DELAY = SensorManager.SENSOR_DELAY_FASTEST;
    private float lastValues = 0;
    private int ccount = 0;

    @Override
    public void finish() {
        try {
            mSensorManager.unregisterListener(mPressureSensorListener, mPressureSensor);
        } catch (Exception e) {
        }
        super.finish();
    }

    void getService() {

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (mSensorManager == null) {
            fail(getString(R.string.service_get_fail));
        }

        mPressureSensor = mSensorManager.getDefaultSensor(SENSOR_TYPE);
        if (mPressureSensor == null) {
            fail(getString(R.string.sensor_get_fail));
        }

        mPressureSensorListener = new PressureSensorListener(this);
        if (!mSensorManager.registerListener(mPressureSensorListener, mPressureSensor,
                SENSOR_DELAY)) {
            mSensorManager.registerListener(mPressureSensorListener, mPressureSensor,
                    SENSOR_DELAY);
            fail(getString(R.string.sensor_register_fail));
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.pressure_sensor);
        mTextView = (TextView) findViewById(R.id.pressure_sensor_result);
        getService();
        mTextView.setText(getString(R.string.pressure_sensor_result) + "\n" + "10");
        confirmButtonNonEnable();
        Utils.paddingLayout(findViewById(R.id.passButton), 0, ACTIVITY_TOP_PADDING, 0, 0);

    }

    void fail(Object msg) {
        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mSensorManager == null || mPressureSensorListener == null
                || mPressureSensor == null)
            return;
        mSensorManager.unregisterListener(mPressureSensorListener, mPressureSensor);
    }

    public class PressureSensorListener implements SensorEventListener {
        public PressureSensorListener(Context context) {
            super();
        }

        public void onSensorChanged(SensorEvent event) {

            synchronized (this) {
                if (event.sensor.getType() == SENSOR_TYPE) {
                    Log.d("PressureSensor", "event.values[0]=" + event.values[0] + "  lastValues=" + lastValues + "  ccount=" + ccount);

                    if (event.values[0] != lastValues) {
                        ccount++;
                        lastValues = event.values[0];
                        mTextView.setText(getString(R.string.pressure_sensor_result) + event.values[0] + "hPa");
                    }
                    if (ccount >= 3 && event.values[0] >= 990 && event.values[0] <= 1300) {
                        mButtonPass.setEnabled(true);
                    }
                }
            }
        }

        public void onAccuracyChanged(Sensor arg0, int arg1) {

        }
    }
}
