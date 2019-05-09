package com.pri.factorytest.GySensor;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.pri.factorytest.PrizeBaseActivity;
import com.pri.factorytest.R;
import com.pri.factorytest.util.Utils;

public class GySensor extends PrizeBaseActivity {

    private SensorManager mSensorManager = null;
    private Sensor mMSensor = null;
    private GySensorListener mGySensorListener;
    private TextView mTextView;
    private final static String INIT_VALUE = "";
    private static String value = INIT_VALUE;
    private final static int SENSOR_TYPE = Sensor.TYPE_GYROSCOPE;
    private final static int SENSOR_DELAY = SensorManager.SENSOR_DELAY_FASTEST;
    private boolean gyUpDown = false;
    private boolean gyLeftRight = false;

    @Override
    public void finish() {
        try {
            mSensorManager.unregisterListener(mGySensorListener, mMSensor);
        } catch (Exception e) {
        }
        super.finish();
    }

    void getService() {

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (mSensorManager == null) {
            fail(getString(R.string.service_get_fail));
        }

        mMSensor = mSensorManager.getDefaultSensor(SENSOR_TYPE);
        if (mMSensor == null) {
            fail(getString(R.string.sensor_get_fail));
        }

        mGySensorListener = new GySensorListener(this);
        if (!mSensorManager.registerListener(mGySensorListener, mMSensor,
                SENSOR_DELAY)) {
            mSensorManager.registerListener(mGySensorListener, mMSensor,
                    SENSOR_DELAY);
            fail(getString(R.string.sensor_register_fail));
        }
    }

    void updateView(Object s) {

        mTextView.setText(getString(R.string.gysensor_name) + " : " + s);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        //getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_GESTURE_ISOLATED);
        setContentView(R.layout.gysensor);
        mTextView = (TextView) findViewById(R.id.gysensor_result);
        getService();
        Utils.paddingLayout(findViewById(R.id.passButton), 0, ACTIVITY_TOP_PADDING, 0, 0);

        updateView(value);
        confirmButtonNonEnable();

    }

    void fail(String msg) {
        toast(msg);
        setResult(RESULT_CANCELED);
        finish();
    }

    void pass() {

        setResult(RESULT_OK);
        finish();
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

        if (mSensorManager == null || mGySensorListener == null
                || mMSensor == null)
            return;
        mSensorManager.unregisterListener(mGySensorListener, mMSensor);
    }

    public class GySensorListener implements SensorEventListener {
        public GySensorListener(Context context) {
            super();
        }

        public void onSensorChanged(SensorEvent event) {

            synchronized (this) {
                TextView msensor = (TextView) findViewById(R.id.gysensor_result);
                ImageView gysensor = (ImageView) findViewById(R.id.gysensor_image);
                if (event.sensor.getType() == SENSOR_TYPE) {
                    msensor.setText(getString(R.string.gysensor_data) + "\n"
                            + "x: " + event.values[0]
                            + "y: " + event.values[1]
                            + "z: " + event.values[2]);
                    if (event.values[0] > 3) {
                        gyUpDown = true;
                        gysensor.setBackgroundResource(R.drawable.gsensor_up);
                    } else if (event.values[0] < -3) {
                        gysensor.setBackgroundResource(R.drawable.gsensor_down);
                    }
                    if (event.values[1] > 3) {
                        gyLeftRight = true;
                        gysensor.setBackgroundResource(R.drawable.gsensor_right);
                    } else if (event.values[1] < -3) {
                        gysensor.setBackgroundResource(R.drawable.gsensor_left);
                    }
                    if (event.values[2] > 3) {
                        gyLeftRight = true;
                        gysensor.setBackgroundResource(R.drawable.gsensor_left);
                    } else if (event.values[2] < -3) {
                        gysensor.setBackgroundResource(R.drawable.gsensor_right);
                    }
                    if (gyLeftRight && gyUpDown) {
                        mButtonPass.setEnabled(true);
                        doPass2NextTest();
                    }

                }
            }
        }

        public void onAccuracyChanged(Sensor arg0, int arg1) {

        }
    }

    public void toast(String s) {
        if (s == null)
            return;
        Toast.makeText(this, s + "", Toast.LENGTH_SHORT).show();
    }
}
