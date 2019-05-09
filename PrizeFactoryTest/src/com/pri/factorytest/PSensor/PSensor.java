package com.pri.factorytest.PSensor;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.widget.TextView;
import android.widget.Toast;

import com.pri.factorytest.PrizeBaseActivity;
import com.pri.factorytest.R;
import com.pri.factorytest.sensorlib.SensorLib;
import com.pri.factorytest.util.Utils;

import java.util.Optional;

public class PSensor extends PrizeBaseActivity {

    private static final int UPDATE_INTERVAL = 50;
    private SensorManager mSensorManager = null;
    private Sensor mPSensor = null;
    private PSensorListener mPSensorListener;
    private TextView mTextView;
    private final static int SENSOR_TYPE = Sensor.TYPE_PROXIMITY;
    private final static int SENSOR_DELAY = SensorManager.SENSOR_DELAY_FASTEST;
    private float lastValues = 0;
    private int ccount = 0;
    private Handler mHandler;
    private Handler mUiHandler = new Handler();
    private int mPData = -1;
    private String mSensorTest;
    private static final int MSG_GET_DATA = 0;
    private static final int MSG_UPDATE_DATA = 1;

    @Override
    public void finish() {
        try {
            mSensorManager.unregisterListener(mPSensorListener, mPSensor);
        } catch (Exception e) {
        }
        super.finish();
    }

    private void getService() {
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (mSensorManager == null) {
            fail(getString(R.string.service_get_fail));
        }

        mPSensor = mSensorManager.getDefaultSensor(SENSOR_TYPE);
        if (mPSensor == null) {
            fail(getString(R.string.sensor_get_fail));
        }

        mPSensorListener = new PSensorListener();
        if (!mSensorManager.registerListener(mPSensorListener, mPSensor,
                SENSOR_DELAY)) {
            mSensorManager.registerListener(mPSensorListener, mPSensor,
                    SENSOR_DELAY);
            fail(getString(R.string.sensor_register_fail));
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.psensor);
        mTextView = (TextView) findViewById(R.id.psensor_result);
        getService();
        mTextView.setText(getString(R.string.psensor_result));
        confirmButtonNonEnable();
        Utils.paddingLayout(findViewById(R.id.passButton), 0, ACTIVITY_TOP_PADDING, 0, 0);

        HandlerThread mHandlerThread = new HandlerThread("async_handler");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper()){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what){
                    case MSG_GET_DATA:
                        mPData = SensorLib.getPsensorData();
                        mHandler.sendEmptyMessageDelayed(MSG_UPDATE_DATA, UPDATE_INTERVAL);
                        break;
                    case MSG_UPDATE_DATA:
                        mPData = SensorLib.getPsensorData();
                        android.util.Log.i("Psensor", "-----mPData:" + mPData);
                        mUiHandler.post(updateTextRun);
                        mHandler.sendEmptyMessageDelayed(MSG_UPDATE_DATA, UPDATE_INTERVAL);
                        break;
                    default:
                        break;
                }
            }
        };
        mHandler.sendEmptyMessageDelayed(MSG_UPDATE_DATA, UPDATE_INTERVAL);
    }

    private Runnable updateTextRun = () -> {
        String text = "";
        if (mPData <= -1) {
            text = mSensorTest;
        } else {
            text = mSensorTest + String.format("%18s", "PS:") + mPData;
        }
        mTextView.setText(text);
    };

    private void fail(String msg) {
        toast(msg);
        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mSensorManager == null || mPSensorListener == null
                || mPSensor == null) {
            return;
        }
        mSensorManager.unregisterListener(mPSensorListener, mPSensor);
        mHandler.removeMessages(MSG_GET_DATA);
        mHandler.removeMessages(MSG_UPDATE_DATA);
        mUiHandler.removeCallbacks(updateTextRun);
    }

    public class PSensorListener implements SensorEventListener {
        @Override
        public void onSensorChanged(SensorEvent event) {
            synchronized (this) {
                if (event.sensor.getType() == SENSOR_TYPE) {
                    mSensorTest = getString(R.string.psensor_result) + event.values[0];
                    mUiHandler.post(updateTextRun);
                    if (event.values[0] != lastValues) {
                        ccount++;
                        lastValues = event.values[0];
                    }
                    if (ccount >= 2) {
                        mButtonPass.setEnabled(true);
                    }
                }
            }
        }
        @Override
        public void onAccuracyChanged(Sensor arg0, int arg1) {
        }
    }

    public void toast(String s) {
        Optional.ofNullable(s).ifPresent(x -> Toast.makeText(this, x, Toast.LENGTH_SHORT).show());
    }
}
