package com.pri.factorytest.GSensor;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.os.SystemProperties;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.pri.factorytest.PrizeBaseActivity;
import com.pri.factorytest.R;
import com.pri.factorytest.util.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Optional;

public class GSensor extends PrizeBaseActivity {

    private static final String TAG = "GSensor_1";
    private SensorManager mSensorManager = null;
    private Sensor mGSensor = null;
    private GSensorListener mGSensorListener;
    private TextView mTextView;
    private final static String INIT_VALUE = "";
    private static String pre_value = INIT_VALUE;
    private final int MIN_COUNT = 15;
    private final double DEVIATION = 3;//0.2; //added by tangan for deviation
    private final static int SENSOR_TYPE = Sensor.TYPE_ACCELEROMETER;
    private final static int SENSOR_DELAY = SensorManager.SENSOR_DELAY_FASTEST;

    private boolean bLeft = false;
    private boolean bRight = false;
    private boolean bsensor = true;

    /*add by liuyang-20180226 for gsensor calibration --start */
    private static final String filePath = "/data/prize_backup/";
    private static final String fileName = "prize_factory_gsensor";
    /*add by liuyang-20180226 for gsensor calibration --end */
    private final static boolean HAS_HOR_CALI = "1".equals(SystemProperties.get("ro.hor_cali"));

    @Override
    public void finish() {
        Optional.ofNullable(mSensorManager).ifPresent(x -> x.unregisterListener(mGSensorListener, mGSensor));
        super.finish();
    }

    private void getService() {
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (mSensorManager == null) {
            fail(getString(R.string.service_get_fail));
        }

        mGSensor = mSensorManager.getDefaultSensor(SENSOR_TYPE);
        if (mGSensor == null) {
            fail(getString(R.string.sensor_get_fail));
        }

        mGSensorListener = new GSensorListener();
        if (!mSensorManager.registerListener(mGSensorListener, mGSensor,
                SENSOR_DELAY)) {
            fail(getString(R.string.sensor_register_fail));
        }
    }

    private void updateView(String s) {
        mTextView.setText(getString(R.string.gsensor_name) + " : " + s);
        mButtonPass.setEnabled(bLeft && bRight);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gsensor);
        mTextView = (TextView) findViewById(R.id.gsensor_result);
        Utils.paddingLayout(findViewById(R.id.passButton), 0, ACTIVITY_TOP_PADDING, 0, 0);
        if (HAS_HOR_CALI) {
            Intent intent = new Intent();
            intent.setClassName("com.android.HorCali",
                    "com.android.HorCali.sensor.SensorCalibration");
            intent.putExtra("gsensor_factorytest", true);
            intent.putExtra("gsensor_autotest", false);
            try {
                startActivityForResult(intent, 0);
            } catch (Exception e) {
                e.printStackTrace();
                getService();
            }
        } else {
            getService();
        }
        confirmButtonNonEnable();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            getService();
        } else {
            mButtonPass.setEnabled(false);
            mTextView.setText(getString(R.string.gsensor_name) + " : " + "sensorCalibration fail!");
        }
    }

    private void creatFile() {
        File file = null;
        try {
            file = new File(filePath);
            if (!file.exists()) {
                file.mkdirs();
            }
        } catch (Exception e) {
        }

        file = new File(filePath + fileName);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
        }

        int status = -1;
        try {
            Process p = Runtime.getRuntime().exec("chmod 777 " + filePath + fileName);
            status = p.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (status == 0) {
            Log.e("liup", "chmod succeed");
        } else {
            Log.e("liup", "chmod failed");
        }

    }

    private void writeFile(String data) {
        try {
            FileOutputStream fout = new FileOutputStream(filePath + fileName);
            byte[] bytes = data.getBytes();
            fout.write(bytes);
            fout.flush();
            fout.close();
            Log.e("liup", "writeFile succcess");
        } catch (Exception e) {
        }
		/*add by liuyang-20180226 for gsensor calibration --end */
    }

    private void fail(String msg) {
        toast(msg);
        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mSensorManager == null || mGSensorListener == null
                || mGSensor == null) {
            return;
        }
        mSensorManager.unregisterListener(mGSensorListener, mGSensor);
    }

    public class GSensorListener implements SensorEventListener {

        private int count = 0;

        public void onSensorChanged(SensorEvent event) {
            synchronized (this) {
                ImageView gsensor = (ImageView) findViewById(R.id.gsensor_image);
                if (event.sensor.getType() == SENSOR_TYPE) {
                    String value = "(x:" + event.values[0] + ", y:"
                            + event.values[1] + ", z:" + event.values[2] + ")";
                    Log.d(TAG, "value=" + value);
/*
					if (event.values[0] > event.values[1]+DEVIATION
							&& event.values[1] > 0) {
						bLeft = true;
						gsensor.setBackgroundResource(R.drawable.gsensor_left);
					}
					if (event.values[1] > event.values[0]+DEVIATION
							&& event.values[0] > 0) {
						gsensor.setBackgroundResource(R.drawable.gsensor_down);
					}
					if (event.values[1] < 0
							&& event.values[0] +DEVIATION< event.values[1]) {
						bRight = true;
						gsensor.setBackgroundResource(R.drawable.gsensor_right);
					}
					if (event.values[0] < 0
							&& event.values[1]+DEVIATION < event.values[0]) {
						gsensor.setBackgroundResource(R.drawable.gsensor_up);
					}
					if (event.values[0] > 0 && event.values[1] < 0) {
						if (event.values[0] > Math.abs(event.values[1])+DEVIATION) {
							bLeft = true;
							gsensor.setBackgroundResource(R.drawable.gsensor_left);
						} else {
							gsensor.setBackgroundResource(R.drawable.gsensor_up);
						}
					}
					if (event.values[0] < 0 && event.values[1] > 0) {
						if (Math.abs(event.values[0]) > event.values[1]+DEVIATION) {
							bRight = true;
							gsensor.setBackgroundResource(R.drawable.gsensor_right);
						} else {
							gsensor.setBackgroundResource(R.drawable.gsensor_down);
						}
					}
*/
//add begin 20170912
                    if (event.values[0] > 0 + DEVIATION && event.values[0] > Math.abs(event.values[1])) {
                        bLeft = true;
                        gsensor.setBackgroundResource(R.drawable.gsensor_left);
                        Log.d(TAG, "bLeft=true");
                    } else if (event.values[0] < 0 - DEVIATION && Math.abs(event.values[0]) > Math.abs(event.values[1])) {
                        bRight = true;
                        gsensor.setBackgroundResource(R.drawable.gsensor_right);
                        Log.d(TAG, "bRight=true");
                    } else if (event.values[1] < 0 - DEVIATION && Math.abs(event.values[0]) < Math.abs(event.values[1])) {
                        gsensor.setBackgroundResource(R.drawable.gsensor_up);
                    } else if (event.values[1] > 0 + DEVIATION && Math.abs(event.values[0]) < event.values[1]) {
                        gsensor.setBackgroundResource(R.drawable.gsensor_down);
                    }
                    if (bsensor) {
                        bsensor = false;
                        gsensor.setBackgroundResource(R.drawable.gsensor_down);
                    }
//add end 20170912
                    updateView(value);
                    doPass2NextTest();
                    //Log.d(TAG,"updateView()--2--buttonPass.setEnabled(true) bLeft="+bLeft+" bRight="+bRight);
                    //mButtonPass.setEnabled(true);

                    if (value != pre_value)
                        count++;
                    if (count >= MIN_COUNT)
                        pre_value = value;
                }
            }
        }

        public void onAccuracyChanged(Sensor arg0, int arg1) {

        }
    }

    public void toast(String s) {
        Optional.ofNullable(s).ifPresent(x -> Toast.makeText(this, x, Toast.LENGTH_SHORT).show());
    }
}
