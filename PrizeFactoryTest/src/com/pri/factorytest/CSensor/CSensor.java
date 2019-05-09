package com.pri.factorytest.CSensor;

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
import android.widget.Toast;

import com.pri.factorytest.PrizeBaseActivity;
import com.pri.factorytest.R;
import com.pri.factorytest.util.Utils;

import java.io.IOException;

public class CSensor extends PrizeBaseActivity {

    private SensorManager mSensorManager = null;
    private Sensor mCSensor = null;
    private CSensorListener mCSensorListener;
    TextView mTextView;
    private final static String INIT_VALUE = "";
    private static String value = INIT_VALUE;
    private final static int SENSOR_TYPE = Sensor.TYPE_STEP_COUNTER;
    private final static int SENSOR_DELAY = SensorManager.SENSOR_DELAY_GAME;
    private float lastValues = 0;
    private int ccount = 0;

    @Override
    public void finish() {
        try {
            mSensorManager.unregisterListener(mCSensorListener, mCSensor);
        } catch (Exception e) {
        }
        super.finish();
    }

    void getService() {
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (mSensorManager == null) {
            fail(getString(R.string.service_get_fail));
        }

        mCSensor = mSensorManager.getDefaultSensor(SENSOR_TYPE);
        if (mCSensor == null) {
            fail(getString(R.string.sensor_get_fail));
        }

        mCSensorListener = new CSensorListener(this);
        if (!mSensorManager.registerListener(mCSensorListener, mCSensor,
                SENSOR_DELAY)) {
            mSensorManager.registerListener(mCSensorListener, mCSensor,
                    SENSOR_DELAY);
            fail(getString(R.string.sensor_register_fail));
        }
    }

    void updateView(String s) {
        mTextView.setText(getString(R.string.csensor_data) + " : " + s);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.csensor);
        mTextView = (TextView) findViewById(R.id.csensor_result);
        Utils.paddingLayout(findViewById(R.id.passButton), 0, ACTIVITY_TOP_PADDING, 0, 0);
        getService();

        confirmButtonNonEnable();
    }

    @Override
    protected void onResume() {
        Log.e("liup", "onResume");
        try {
            String[] cmdMode = new String[]{"/system/bin/sh", "-c", "echo" + " " + 0 + " > /proc/dummystep"};
            Runtime.getRuntime().exec(cmdMode);
            Log.e("liup", "onResume success");
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.onResume();
    }

    @Override
    public void onPause() {
        Log.e("liup", "onPause");
        try {
            String[] cmdMode = new String[]{"/system/bin/sh", "-c", "echo" + " " + 1 + " > /proc/dummystep"};
            Runtime.getRuntime().exec(cmdMode);
            Log.e("liup", "onPause success");
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.onPause();
    }

    void fail(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mSensorManager == null || mCSensorListener == null
                || mCSensor == null)
            return;
        mSensorManager.unregisterListener(mCSensorListener, mCSensor);
    }

    public class CSensorListener implements SensorEventListener {
        public CSensorListener(Context context) {
            super();
        }

        public void onSensorChanged(SensorEvent event) {
            synchronized (this) {
                Log.i("zengke", "----onSensorChanged:" + event.values[0]);
                if (event.sensor.getType() == SENSOR_TYPE) {
                    if (event.values[0] != lastValues) {
                        ccount++;
                        lastValues = event.values[0];
                        updateView(String.valueOf(lastValues));
                    }
                    if (lastValues >= 10) {
                        mButtonPass.setEnabled(true);
                    }
                }
            }
        }

        public void onAccuracyChanged(Sensor arg0, int arg1) {
        }
    }
}
